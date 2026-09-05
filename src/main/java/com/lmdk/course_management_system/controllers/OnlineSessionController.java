package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/online-sessions")
@RequiredArgsConstructor
public class OnlineSessionController {

    private final OnlineSessionService sessionService;
    private final CourseClassService classService;
    private final CourseService courseService;
    private final UserService userService;

    @Value("${online-sessions.page-size:10}")
    private int pageSize;

    @GetMapping
    public String sessions(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        Integer selectedCourseId = parseInteger(params.get("courseId"));
        normalizeClassFilter(params, selectedCourseId);

        long totalRecords = sessionService.countSessions(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("sessions", sessionService.getSessions(params));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("classes", selectedCourseId == null ? List.of() : classService.getClassesByCourse(selectedCourseId));
        model.addAttribute("selectedTeacher", selectedTeacher(params.get("teacherId")));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("classId", params.getOrDefault("classId", ""));
        model.addAttribute("teacherId", params.getOrDefault("teacherId", ""));
        model.addAttribute("date", params.getOrDefault("date", ""));

        return "admin/online-sessions";
    }

    @GetMapping("/classes")
    @ResponseBody
    public List<Map<String, Object>> getClasses(@RequestParam Integer courseId,
                                                @RequestParam(defaultValue = "false") boolean availableOnly) {
        if (courseId == null)
            return List.of();

        return classService.getClassesByCourse(courseId).stream()
                .filter(courseClass -> !availableOnly
                        || (courseClass.getStatus() != CourseClass.ClassStatus.COMPLETED
                        && courseClass.getStatus() != CourseClass.ClassStatus.CANCELED))
                .map(courseClass -> Map.<String, Object>of(
                        "id", courseClass.getId(),
                        "name", courseClass.getName(),
                        "status", courseClass.getStatus().name()
                ))
                .toList();
    }

    @GetMapping("/teachers")
    @ResponseBody
    public List<Map<String, Object>> searchTeachers(@RequestParam String q) {
        if (q == null || q.trim().length() < 2)
            return List.of();

        return userService.searchUsersByRole(User.UserRole.TEACHER, q.trim(), 1, 20).stream()
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "fullName", user.getFullName()
                ))
                .toList();
    }

    @PostMapping("/add")
    public String addSession(@RequestParam String title,
                             @RequestParam Integer classId,
                             @RequestParam Integer teacherId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                             @RequestParam String meetingUrl,
                             RedirectAttributes redirectAttributes) {
        CourseClass courseClass = classService.getClassById(classId);
        User teacher = userService.getUserById(teacherId);

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp học không tồn tại!");
            return "redirect:/admin/online-sessions";
        }

        if (!validTeacher(teacher)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giáo viên không hợp lệ!");
            return "redirect:/admin/online-sessions";
        }

        try {
            OnlineSession session = new OnlineSession();
            session.setTitle(title);
            session.setCourseClass(courseClass);
            session.setTeacher(teacher);
            session.setStartTime(startTime);
            session.setEndTime(endTime);
            session.setMeetingUrl(meetingUrl);

            sessionService.addSession(session);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm buổi học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/online-sessions";
    }

    @PostMapping("/update")
    public String updateSession(@RequestParam Integer sessionId,
                                @RequestParam String title,
                                @RequestParam Integer classId,
                                @RequestParam Integer teacherId,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                @RequestParam String meetingUrl,
                                RedirectAttributes redirectAttributes) {
        OnlineSession session = sessionService.getSessionById(sessionId);
        CourseClass courseClass = classService.getClassById(classId);
        User teacher = userService.getUserById(teacherId);

        if (session == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy buổi học!");
            return "redirect:/admin/online-sessions";
        }

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp học không tồn tại!");
            return "redirect:/admin/online-sessions";
        }

        if (!validTeacher(teacher)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giáo viên không hợp lệ!");
            return "redirect:/admin/online-sessions";
        }

        try {
            OnlineSession candidate = new OnlineSession();
            candidate.setId(sessionId);
            candidate.setTitle(title);
            candidate.setCourseClass(courseClass);
            candidate.setTeacher(teacher);
            candidate.setStartTime(startTime);
            candidate.setEndTime(endTime);
            candidate.setMeetingUrl(meetingUrl);

            sessionService.updateSession(candidate);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật buổi học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/online-sessions";
    }

    private void normalizeClassFilter(Map<String, String> params, Integer courseId) {
        Integer classId = parseInteger(params.get("classId"));
        if (courseId == null || classId == null)
            return;

        CourseClass courseClass = classService.getClassById(classId);
        if (courseClass == null || courseClass.getCourse() == null || !courseId.equals(courseClass.getCourse().getId()))
            params.remove("classId");
    }

    private User selectedTeacher(String teacherId) {
        Integer id = parseInteger(teacherId);
        if (id == null)
            return null;
        User teacher = userService.getUserById(id);
        return validTeacher(teacher) ? teacher : null;
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean validTeacher(User teacher) {
        return teacher != null
                && teacher.getRole() == User.UserRole.TEACHER
                && teacher.getStatus() == User.UserStatus.ACTIVE;
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}