package com.lmdk.course_management_system.dto.admin.payment;

public record AdminPaymentTransactionActionResponse(
        Integer transactionId,
        String message
) {
}