package com.lmdk.course_management_system.dto.admin.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentTransactionResponse(
        Integer id,

        Integer enrollmentId,

        Integer studentId,
        String studentName,
        String username,

        Integer classId,
        String className,

        Integer courseId,
        String courseName,

        BigDecimal amount,
        String paymentMethod,
        String transactionCode,
        String status,

        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
}