package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final OnlineSessionService sessionService;
    private final CourseClassService classService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;

    @Value("${attendances.page-size:10}")
    private int pageSize;

    @GetMapping
    public String attendances(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = attendanceService.countAttendances(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("attendances", attendanceService.getAttendances(params));
        model.addAttribute("sessions", sessionService.getAllSessions());
        model.addAttribute("classes", classService.getAllClasses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("sessionId", params.getOrDefault("sessionId", ""));
        model.addAttribute("classId", params.getOrDefault("classId", ""));
        model.addAttribute("present", params.getOrDefault("present", ""));

        return "admin/attendances";
    }

    @PostMapping("/add")
    public String addAttendance(@RequestParam Integer sessionId,
                                @RequestParam Integer studentId,
                                @RequestParam boolean present,
                                @RequestParam(required = false) String note,
                                RedirectAttributes redirectAttributes) {
        OnlineSession session = sessionService.getSessionById(sessionId);
        User student = userService.getUserById(studentId);

        if (session == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Buổi học không tồn tại!");
            return "redirect:/admin/attendances";
        }

        if (!validStudent(student)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Học viên không hợp lệ!");
            return "redirect:/admin/attendances";
        }

        Enrollment enrollment = enrollmentService.getEnrollment(
                studentId,
                session.getCourseClass().getId()
        );

        if (enrollment == null || enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Học viên không thuộc lớp học của buổi này!"
            );
            return "redirect:/admin/attendances";
        }

        try {
            Attendance attendance = new Attendance();
            attendance.setOnlineSession(session);
            attendance.setStudent(student);
            attendance.setPresent(present);
            attendance.setNote(note);

            attendanceService.addAttendance(attendance);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Điểm danh thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/attendances";
    }

    @PostMapping("/update")
    public String updateAttendance(@RequestParam Integer attendanceId,
                                   @RequestParam boolean present,
                                   @RequestParam(required = false) String note,
                                   RedirectAttributes redirectAttributes) {
        Attendance attendance = attendanceService.getAttendanceById(attendanceId);

        if (attendance == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dữ liệu điểm danh!");
            return "redirect:/admin/attendances";
        }

        try {
            attendance.setPresent(present);
            attendance.setNote(note);

            attendanceService.updateAttendance(attendance);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật điểm danh thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/attendances";
    }

    @GetMapping("/students")
    @ResponseBody
    public List<Map<String, Object>> getStudentsBySession(@RequestParam Integer sessionId) {
        OnlineSession session = sessionService.getSessionById(sessionId);

        if (session == null)
            return List.of();

        return enrollmentService
                .getActiveEnrollmentsByClass(session.getCourseClass().getId())
                .stream()
                .map(enrollment -> {
                    User student = enrollment.getStudent();

                    return Map.<String, Object>of(
                            "id", student.getId(),
                            "fullName", student.getFullName(),
                            "username", student.getUsername()
                    );
                })
                .toList();
    }

    private boolean validStudent(User student) {
        return student != null
                && student.getRole() == User.UserRole.STUDENT
                && student.getStatus() == User.UserStatus.ACTIVE;
    }


    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}