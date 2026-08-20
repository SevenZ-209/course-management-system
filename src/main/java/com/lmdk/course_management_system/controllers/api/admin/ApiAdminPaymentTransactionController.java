package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.payment.AdminPaymentTransactionActionResponse;
import com.lmdk.course_management_system.dto.admin.payment.AdminPaymentTransactionPageResponse;
import com.lmdk.course_management_system.dto.admin.payment.CreateAdminPaymentTransactionRequest;
import com.lmdk.course_management_system.dto.admin.payment.UpdatePaymentTransactionStatusRequest;
import com.lmdk.course_management_system.mappers.admin.AdminPaymentTransactionMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment-transactions")
@RequiredArgsConstructor
public class ApiAdminPaymentTransactionController {

    private final PaymentTransactionService transactionService;
    private final EnrollmentService enrollmentService;
    private final AdminPaymentTransactionMapper adminPaymentTransactionMapper;

    @Value("${payment-transactions.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminPaymentTransactionPageResponse getTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if(status != null && !status.isBlank())
            params.put("status", status);

        if(date != null && !date.isBlank())
            params.put("date", date);

        long totalRecords =
                transactionService.countTransactions(params);

        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminPaymentTransactionPageResponse(
                transactionService.getTransactions(params)
                        .stream()
                        .map(adminPaymentTransactionMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @PostMapping
    public AdminPaymentTransactionActionResponse addTransaction(
            @RequestBody CreateAdminPaymentTransactionRequest request
    ) {
        Enrollment enrollment =
                enrollmentService.getEnrollmentById(
                        request.enrollmentId()
                );

        if(enrollment == null)
            throw new IllegalArgumentException(
                    "Đăng ký không tồn tại!"
            );

        if(request.status() == null
                || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái giao dịch không được để trống!"
            );

        PaymentTransaction.TransactionStatus status;

        try {
            status = PaymentTransaction.TransactionStatus.valueOf(
                    request.status().trim().toUpperCase()
            );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái giao dịch không hợp lệ!"
            );
        }

        PaymentTransaction transaction =
                new PaymentTransaction();

        transaction.setEnrollment(enrollment);
        transaction.setAmount(request.amount());
        transaction.setPaymentMethod(request.paymentMethod());
        transaction.setTransactionCode(request.transactionCode());
        transaction.setStatus(status);

        PaymentTransaction saved =
                transactionService.addTransaction(transaction);

        return new AdminPaymentTransactionActionResponse(
                saved.getId(),
                "Thêm giao dịch thành công!"
        );
    }

    @PatchMapping("/{transactionId}/status")
    public AdminPaymentTransactionActionResponse updateStatus(
            @PathVariable Integer transactionId,
            @RequestBody UpdatePaymentTransactionStatusRequest request
    ) {
        if(request.status() == null
                || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái giao dịch không được để trống!"
            );

        PaymentTransaction.TransactionStatus newStatus;

        try {
            newStatus = PaymentTransaction.TransactionStatus.valueOf(
                    request.status().trim().toUpperCase()
            );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái giao dịch không hợp lệ!"
            );
        }

        PaymentTransaction updated =
                transactionService.updateTransactionStatus(
                        transactionId,
                        newStatus
                );

        return new AdminPaymentTransactionActionResponse(
                updated.getId(),
                updated.getStatus()
                        == PaymentTransaction.TransactionStatus.SUCCESS
                        ? "Thanh toán thành công, học viên đã được kích hoạt vào lớp!"
                        : "Cập nhật giao dịch thành công!"
        );
    }
}