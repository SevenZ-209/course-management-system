package com.lmdk.course_management_system.mappers.common;

import com.lmdk.course_management_system.dto.payment.PaymentTransactionResponse;
import com.lmdk.course_management_system.pojo.PaymentTransaction;

import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {

    public PaymentTransactionResponse toResponse(
            PaymentTransaction transaction
    ) {

        return new PaymentTransactionResponse(
                transaction.getId(),
                transaction.getEnrollment().getId(),
                transaction.getAmount(),
                transaction.getPaymentMethod(),
                transaction.getTransactionCode(),
                transaction.getStatus().name(),
                transaction.getPaidAt(),
                transaction.getCreatedAt()
        );
    }
}