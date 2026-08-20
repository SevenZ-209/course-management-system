package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/admin/payment-transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService transactionService;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    @Value("${payment-transactions.page-size:10}")
    private int pageSize;

    @GetMapping
    public String transactions(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = transactionService.countTransactions(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("transactions", transactionService.getTransactions(params));
        model.addAttribute("pendingEnrollments", enrollmentService.getPendingEnrollments());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));
        model.addAttribute("date", params.getOrDefault("date", ""));

        return "admin/payment-transactions";
    }

    @PostMapping("/add")
    public String addTransaction(@RequestParam Integer enrollmentId,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam String paymentMethod,
                                 @RequestParam(required = false) String transactionCode,
                                 @RequestParam String status,
                                 RedirectAttributes redirectAttributes) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đăng ký không tồn tại!");
            return "redirect:/admin/payment-transactions";
        }

        try {
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setEnrollment(enrollment);
            transaction.setAmount(amount);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setTransactionCode(transactionCode);
            transaction.setStatus(PaymentTransaction.TransactionStatus.valueOf(status));

            transactionService.addTransaction(transaction);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm giao dịch thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/payment-transactions";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer transactionId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        PaymentTransaction transaction = transactionService.getTransactionById(transactionId);

        if (transaction == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch!");
            return "redirect:/admin/payment-transactions";
        }

        try {
            transaction.setStatus(PaymentTransaction.TransactionStatus.valueOf(status));
            transactionService.updateTransaction(transaction);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS
                            ? "Thanh toán thành công, học viên đã được kích hoạt vào lớp!"
                            : "Cập nhật giao dịch thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/payment-transactions";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}