package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.PaymentTransaction;

import java.util.List;
import java.util.Map;

public interface PaymentTransactionRepository {

    PaymentTransaction getTransactionById(Integer id);

    PaymentTransaction getTransactionByCode(String transactionCode);

    PaymentTransaction addTransaction(PaymentTransaction transaction);

    void updateTransaction(PaymentTransaction transaction);

    List<PaymentTransaction> getTransactions(Map<String, String> params);

    List<PaymentTransaction> getTransactionsByEnrollment(Integer enrollmentId);

    long countTransactions(Map<String, String> params);

    boolean existsByTransactionCode(String transactionCode);

    boolean existsSuccessfulTransaction(Integer enrollmentId);

    boolean existsSuccessfulTransactionExceptId(Integer enrollmentId, Integer transactionId);
}