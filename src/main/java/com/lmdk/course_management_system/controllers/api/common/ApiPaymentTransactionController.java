package com.lmdk.course_management_system.controllers.api.common;

import com.lmdk.course_management_system.dto.payment.PaymentTransactionPageResponse;
import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.dto.payment.PaymentTransactionRequest;
import com.lmdk.course_management_system.dto.payment.PaymentTransactionResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.common.PaymentTransactionMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-transactions")
@RequiredArgsConstructor
public class ApiPaymentTransactionController {

    private final PaymentTransactionService transactionService;
    private final EnrollmentService enrollmentService;
    private final CurrentUserHelper currentUserHelper;
    private final PaymentTransactionMapper paymentTransactionMapper;

    @Value("${payment-transactions.page-size:10}")
    private int pageSize;

    @PostMapping
    public PaymentTransactionResponse create(
            @RequestBody PaymentTransactionRequest request,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentUser(authentication);

        if(student.getRole() != User.UserRole.STUDENT)
            throw new ForbiddenException(
                    "Chỉ học viên mới được tạo giao dịch!"
            );

        Enrollment enrollment =
                enrollmentService.getEnrollmentById(
                        request.getEnrollmentId()
                );

        if(enrollment == null)
            throw new IllegalArgumentException(
                    "Đăng ký không tồn tại!"
            );

        if(!enrollment.getStudent()
                .getId()
                .equals(student.getId()))
            throw new ForbiddenException(
                    "Bạn không có quyền thanh toán cho đăng ký này!"
            );

        if(enrollment.getStatus()
                != Enrollment.EnrollmentStatus.PENDING_PAYMENT)
            throw new IllegalArgumentException(
                    "Đăng ký này không ở trạng thái chờ thanh toán!"
            );

        PaymentTransaction transaction =
                new PaymentTransaction();

        transaction.setEnrollment(enrollment);
        transaction.setAmount(request.getAmount());
        transaction.setPaymentMethod(
                request.getPaymentMethod()
        );
        transaction.setTransactionCode(
                request.getTransactionCode()
        );
        transaction.setStatus(
                PaymentTransaction.TransactionStatus.SUCCESS
        );

        PaymentTransaction saved =
                transactionService.addTransaction(transaction);

        return paymentTransactionMapper
                .toResponse(saved);
    }


    @GetMapping("/me")
    public PaymentTransactionPageResponse getMyTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            Authentication authentication
    ) {
        User student = currentUserHelper.getCurrentStudent(authentication);
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("studentId", String.valueOf(student.getId()));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(status != null && !status.isBlank())
            params.put("status", status.trim().toUpperCase());

        if(date != null && !date.isBlank())
            params.put("date", date.trim());

        long totalRecords = transactionService.countTransactions(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        Map<String, String> summaryParams = new HashMap<>();
        summaryParams.put("studentId", String.valueOf(student.getId()));

        long totalTransactions = transactionService.countTransactions(summaryParams);

        summaryParams.put("status", PaymentTransaction.TransactionStatus.SUCCESS.name());
        long successCount = transactionService.countTransactions(summaryParams);

        summaryParams.put("status", PaymentTransaction.TransactionStatus.PENDING.name());
        long pendingCount = transactionService.countTransactions(summaryParams);

        summaryParams.put("status", PaymentTransaction.TransactionStatus.FAILED.name());
        long failedCount = transactionService.countTransactions(summaryParams);

        return new PaymentTransactionPageResponse(
                transactionService.getTransactions(params)
                        .stream()
                        .map(paymentTransactionMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords,
                totalTransactions,
                successCount,
                pendingCount,
                failedCount
        );
    }

    @GetMapping("/{id}")
    public PaymentTransactionResponse get(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        User currentUser = currentUserHelper.getCurrentUser(authentication);
        PaymentTransaction transaction = transactionService.getTransactionById(id);

        if(transaction == null)
            throw new IllegalArgumentException("Không tìm thấy giao dịch!");

        if(currentUser.getRole() == User.UserRole.STUDENT) {
            Integer ownerId = transaction.getEnrollment().getStudent().getId();

            if(!ownerId.equals(currentUser.getId()))
                throw new ForbiddenException("Bạn không có quyền xem giao dịch này!");
        } else if(currentUser.getRole() != User.UserRole.ADMIN)
            throw new ForbiddenException("Bạn không có quyền xem giao dịch này!");

        return paymentTransactionMapper.toResponse(transaction);
    }
}