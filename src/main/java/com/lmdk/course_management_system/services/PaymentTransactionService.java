package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.PaymentTransaction;

import java.math.BigDecimal;
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

    List<PaymentTransaction> getTransactionsByStudent(Integer studentId);

    List<PaymentTransaction> getTransactions(Map<String, String> params);

    List<PaymentTransaction> getTransactionsByEnrollment(Integer enrollmentId);

    long countTransactions(Map<String, String> params);

    BigDecimal sumTransactionAmounts(Map<String, String> params);

    List<Object[]> sumTransactionAmountsByPeriod(Map<String, String> params, String dateFormat);

    PaymentTransaction createAutoSuccess(PaymentTransaction transaction);
}