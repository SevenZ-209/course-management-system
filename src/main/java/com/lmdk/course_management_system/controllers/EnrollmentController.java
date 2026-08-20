package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseClassService classService;
    private final CourseService courseService;
    private final UserService userService;

    @Value("${enrollments.page-size:10}")
    private int pageSize;

    @GetMapping
    public String enrollments(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = enrollmentService.countEnrollments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("enrollments", enrollmentService.getEnrollments(params));
        model.addAttribute("students", userService.getUsersByRole(User.UserRole.STUDENT));
        model.addAttribute("classes", classService.getAllClasses());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("classId", params.getOrDefault("classId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/enrollments";
    }

    @PostMapping("/add")
    public String addEnrollment(@RequestParam Integer studentId,
                                @RequestParam Integer classId,
                                RedirectAttributes redirectAttributes) {
        User student = userService.getUserById(studentId);
        CourseClass courseClass = classService.getClassById(classId);

        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Học viên không tồn tại!");
            return "redirect:/admin/enrollments";
        }

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp học không tồn tại!");
            return "redirect:/admin/enrollments";
        }

        try {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourseClass(courseClass);
            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);

            enrollmentService.addEnrollment(enrollment);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký lớp học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/enrollments";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer enrollmentId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đăng ký!");
            return "redirect:/admin/enrollments";
        }

        try {
            enrollment.setStatus(Enrollment.EnrollmentStatus.valueOf(status));
            enrollmentService.updateEnrollment(enrollment);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/enrollments";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}