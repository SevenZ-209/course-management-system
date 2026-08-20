package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.PaymentTransaction;

import java.util.List;
import java.util.Map;

public interface PaymentTransactionService {

    PaymentTransaction getTransactionById(Integer id);

    PaymentTransaction getTransactionByCode(String transactionCode);

    PaymentTransaction addTransaction(PaymentTransaction transaction);

    void updateTransaction(PaymentTransaction transaction);

    PaymentTransaction updateTransactionStatus(
            Integer id,
            PaymentTransaction.TransactionStatus status
    );

    List<PaymentTransaction> getTransactions(Map<String, String> params);

    List<PaymentTransaction> getTransactionsByEnrollment(Integer enrollmentId);

    long countTransactions(Map<String, String> params);
}