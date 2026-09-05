package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import com.lmdk.course_management_system.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseClassService classService;
    private final CourseService courseService;
    private final UserService userService;
    private final PaymentTransactionService transactionService;

    @Value("${enrollments.page-size:10}")
    private int pageSize;

    @GetMapping
    public String enrollments(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = enrollmentService.countEnrollments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);
        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        Integer selectedCourseId = parseInteger(params.get("courseId"));
        model.addAttribute("enrollments", enrollmentService.getEnrollments(params));
        model.addAttribute("classes", selectedCourseId == null
                ? List.of()
                : classService.getClassesByCourse(selectedCourseId));
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
    @Transactional
    public String addEnrollment(@RequestParam Integer studentId,
                                @RequestParam Integer classId,
                                RedirectAttributes redirectAttributes) {
        User student = userService.getUserById(studentId);
        CourseClass courseClass = classService.getClassById(classId);
        if(student == null) return error(redirectAttributes, "Học viên không tồn tại!");
        if(student.getRole() != User.UserRole.STUDENT)
            return error(redirectAttributes, "Người dùng được chọn không phải học viên!");
        if(courseClass == null) return error(redirectAttributes, "Lớp học không tồn tại!");

        try {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourseClass(courseClass);
            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);
            Enrollment saved = enrollmentService.addEnrollment(enrollment);

            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setEnrollment(saved);
            transaction.setAmount(saved.getCourseClass().getCourse().getTuitionFee());
            transaction.setPaymentMethod("MANUAL");
            transaction.setStatus(PaymentTransaction.TransactionStatus.PENDING);
            transactionService.addTransaction(transaction);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Đăng ký thành công, đã tạo giao dịch chờ thanh toán!");
        } catch(IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    @PostMapping("/cancel")
    @Transactional
    public String cancelEnrollment(@RequestParam Integer enrollmentId,
                                   RedirectAttributes redirectAttributes) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);
        if(enrollment == null) return error(redirectAttributes, "Không tìm thấy đăng ký!");
        if(enrollment.getStatus() == Enrollment.EnrollmentStatus.CANCELED)
            return error(redirectAttributes, "Đăng ký này đã bị hủy!");

        try {
            transactionService.getTransactionsByEnrollment(enrollmentId).stream()
                    .filter(transaction -> transaction.getStatus() == PaymentTransaction.TransactionStatus.PENDING)
                    .forEach(transaction -> transactionService.updateTransactionStatus(
                            transaction.getId(), PaymentTransaction.TransactionStatus.FAILED));

            enrollment.setStatus(Enrollment.EnrollmentStatus.CANCELED);
            enrollmentService.updateEnrollment(enrollment);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đăng ký thành công!");
        } catch(IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    private String error(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/admin/enrollments";
    }

    private Integer parseInteger(String value) {
        try { return value == null || value.isBlank() ? null : Integer.valueOf(value); }
        catch(Exception ex) { return null; }
    }

    private int parsePage(String page) {
        try { return Math.max(Integer.parseInt(page), 1); }
        catch(Exception ex) { return 1; }
    }
}
