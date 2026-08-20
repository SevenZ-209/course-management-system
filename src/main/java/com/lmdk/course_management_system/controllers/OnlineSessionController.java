package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
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
import java.util.Map;

@Controller
@RequestMapping("/admin/online-sessions")
@RequiredArgsConstructor
public class OnlineSessionController {

    private final OnlineSessionService sessionService;
    private final CourseClassService classService;
    private final UserService userService;

    @Value("${online-sessions.page-size:10}")
    private int pageSize;

    @GetMapping
    public String sessions(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = sessionService.countSessions(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("sessions", sessionService.getSessions(params));
        model.addAttribute("classes", classService.getAllClasses());
        model.addAttribute("teachers", userService.getUsersByRole(User.UserRole.TEACHER));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("classId", params.getOrDefault("classId", ""));
        model.addAttribute("teacherId", params.getOrDefault("teacherId", ""));
        model.addAttribute("date", params.getOrDefault("date", ""));

        return "admin/online-sessions";
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
            session.setTitle(title);
            session.setCourseClass(courseClass);
            session.setTeacher(teacher);
            session.setStartTime(startTime);
            session.setEndTime(endTime);
            session.setMeetingUrl(meetingUrl);

            sessionService.updateSession(session);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật buổi học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/online-sessions";
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