package com.lmdk.course_management_system.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentTransactionRequest {

    private Integer enrollmentId;

    private BigDecimal amount;

    private String paymentMethod;

    private String transactionCode;

}