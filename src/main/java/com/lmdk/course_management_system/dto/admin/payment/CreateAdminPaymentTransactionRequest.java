package com.lmdk.course_management_system.dto.admin.payment;

import java.math.BigDecimal;

public record CreateAdminPaymentTransactionRequest(
        Integer enrollmentId,
        BigDecimal amount,
        String paymentMethod,
        String transactionCode,
        String status
) {
}