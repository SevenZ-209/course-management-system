package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.manager.ManagerActionResponse;
import com.lmdk.course_management_system.dto.manager.payment.ManagerPaymentTransactionPageResponse;
import com.lmdk.course_management_system.dto.manager.payment.UpdateManagerPaymentStatusRequest;
import com.lmdk.course_management_system.mappers.manager.ManagerPaymentTransactionMapper;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/payment-transactions")
@RequiredArgsConstructor
public class ApiManagerPaymentTransactionController {

    private final PaymentTransactionService transactionService;
    private final ManagerPaymentTransactionMapper paymentMapper;

    @Value("${payment-transactions.page-size:10}")
    private int pageSize;

    @GetMapping
    public ManagerPaymentTransactionPageResponse getTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date
    ) {
        page = Math.max(page, 1);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank()) params.put("kw", kw.trim());
        if(courseId != null) params.put("courseId", String.valueOf(courseId));
        if(status != null && !status.isBlank()) params.put("status", status.trim().toUpperCase());
        if(date != null && !date.isBlank()) params.put("date", date.trim());

        long totalRecords = transactionService.countTransactions(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new ManagerPaymentTransactionPageResponse(
                transactionService.getTransactions(params).stream().map(paymentMapper::toResponse).toList(),
                page, totalPages, totalRecords
        );
    }

    @PatchMapping("/{transactionId}/status")
    public ManagerActionResponse updateStatus(
            @PathVariable Integer transactionId,
            @RequestBody UpdateManagerPaymentStatusRequest request
    ) {
        if(request.status() == null || request.status().isBlank())
            throw new IllegalArgumentException("Trạng thái giao dịch không được để trống!");

        PaymentTransaction.TransactionStatus status;
        try {
            status = PaymentTransaction.TransactionStatus.valueOf(request.status().trim().toUpperCase());
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái giao dịch không hợp lệ!");
        }

        PaymentTransaction updated = transactionService.updateTransactionStatus(transactionId, status);
        return new ManagerActionResponse(
                updated.getId(),
                updated.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS
                        ? "Thanh toán thành công, học viên đã được kích hoạt vào lớp!"
                        : "Cập nhật giao dịch thành công!"
        );
    }
}
