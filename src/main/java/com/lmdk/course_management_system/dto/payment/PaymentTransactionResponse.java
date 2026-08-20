package com.lmdk.course_management_system.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentTransactionResponse {

    private Integer id;

    private Integer enrollmentId;

    private BigDecimal amount;

    private String paymentMethod;

    private String transactionCode;

    private String status;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

}