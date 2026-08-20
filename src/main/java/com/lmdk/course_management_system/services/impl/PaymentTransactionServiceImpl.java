package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.repository.PaymentTransactionRepository;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final EnrollmentService enrollmentService;

    @Override
    public PaymentTransaction getTransactionById(Integer id) {
        return transactionRepository.getTransactionById(id);
    }

    @Override
    public PaymentTransaction getTransactionByCode(String transactionCode) {
        return transactionRepository.getTransactionByCode(transactionCode);
    }

    @Override
    public PaymentTransaction addTransaction(PaymentTransaction transaction) {
        validateTransaction(transaction);

        if (transaction.getTransactionCode() == null
                || transaction.getTransactionCode().trim().isBlank())
            transaction.setTransactionCode(generateTransactionCode());

        transaction.setTransactionCode(transaction.getTransactionCode().trim());

        if (transactionRepository.existsByTransactionCode(transaction.getTransactionCode()))
            throw new IllegalArgumentException("Mã giao dịch đã tồn tại!");

        if (transaction.getStatus() == null)
            transaction.setStatus(PaymentTransaction.TransactionStatus.PENDING);

        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS
                && transactionRepository.existsSuccessfulTransaction(
                transaction.getEnrollment().getId()
        ))
            throw new IllegalArgumentException("Đăng ký này đã có giao dịch thanh toán thành công!");

        System.out.println(
                "CREATE PAYMENT STATUS = "
                        + transaction.getStatus()
        );
        processStatus(transaction);

        PaymentTransaction saved =
                transactionRepository.addTransaction(transaction);

        return saved;
    }

    @Override
    public void updateTransaction(PaymentTransaction transaction) {
        if(transaction == null || transaction.getId() == null)
            throw new IllegalArgumentException(
                    "Giao dịch không hợp lệ!"
            );

        transactionRepository.updateTransaction(transaction);
    }

    @Override
    public PaymentTransaction updateTransactionStatus(
            Integer id,
            PaymentTransaction.TransactionStatus newStatus
    ) {

        PaymentTransaction current =
                transactionRepository.getTransactionById(id);

        if (current == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy giao dịch!"
            );

        if (current.getStatus()
                != PaymentTransaction.TransactionStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Chỉ giao dịch đang chờ thanh toán mới được cập nhật!"
            );
        }

        if (newStatus == PaymentTransaction.TransactionStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Trạng thái mới phải là SUCCESS hoặc FAILED!"
            );
        }

        current.setStatus(newStatus);

        processStatus(current);

        transactionRepository.updateTransaction(current);

        return current;
    }

    @Override
    public List<PaymentTransaction> getTransactions(Map<String, String> params) {
        return transactionRepository.getTransactions(params);
    }

    @Override
    public List<PaymentTransaction> getTransactionsByEnrollment(Integer enrollmentId) {
        return transactionRepository.getTransactionsByEnrollment(enrollmentId);
    }

    @Override
    public long countTransactions(Map<String, String> params) {
        return transactionRepository.countTransactions(params);
    }

    private void validateTransaction(PaymentTransaction transaction) {
        if (transaction.getEnrollment() == null)
            throw new IllegalArgumentException("Vui lòng chọn đăng ký!");

        if (transaction.getAmount() == null
                || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0!");

        if (transaction.getPaymentMethod() == null
                || transaction.getPaymentMethod().trim().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập phương thức thanh toán!");

        BigDecimal coursePrice =
                transaction.getEnrollment()
                        .getCourseClass()
                        .getCourse()
                        .getTuitionFee();


        if(transaction.getAmount()
                .compareTo(coursePrice) != 0)

            throw new IllegalArgumentException(
                    "Số tiền thanh toán không đúng!"
            );

        transaction.setPaymentMethod(transaction.getPaymentMethod().trim());
    }

    private void processStatus(PaymentTransaction transaction) {
        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            if (transaction.getPaidAt() == null)
                transaction.setPaidAt(LocalDateTime.now());

            Enrollment enrollment = transaction.getEnrollment();

            if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
                enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                enrollmentService.updateEnrollment(enrollment);
            }
        }

        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.PENDING
                || transaction.getStatus() == PaymentTransaction.TransactionStatus.FAILED)
            transaction.setPaidAt(null);
    }

    private String generateTransactionCode() {
        return "PAY-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}