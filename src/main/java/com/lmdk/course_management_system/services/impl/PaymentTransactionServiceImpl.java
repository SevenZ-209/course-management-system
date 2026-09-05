package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.repository.PaymentTransactionRepository;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import com.lmdk.course_management_system.services.StudentLearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final EnrollmentService enrollmentService;
    private final StudentLearningPathService studentLearningPathService;

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
        if(transaction == null || transaction.getEnrollment() == null
                || transaction.getEnrollment().getId() == null)
            throw new IllegalArgumentException("Giao dịch không hợp lệ!");

        Enrollment lockedEnrollment = enrollmentService
                .getEnrollmentByIdForUpdate(transaction.getEnrollment().getId());
        if(lockedEnrollment == null)
            throw new IllegalArgumentException("Đăng ký không tồn tại!");

        transaction.setEnrollment(lockedEnrollment);
        validateTransaction(transaction);

        if(transaction.getTransactionCode() == null || transaction.getTransactionCode().trim().isBlank())
            transaction.setTransactionCode(generateTransactionCode());

        transaction.setTransactionCode(transaction.getTransactionCode().trim());

        if(transactionRepository.existsByTransactionCode(transaction.getTransactionCode()))
            throw new IllegalArgumentException("Mã giao dịch đã tồn tại!");

        if(transaction.getStatus() == null)
            transaction.setStatus(PaymentTransaction.TransactionStatus.PENDING);

        if(transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS
                && transactionRepository.existsSuccessfulTransaction(transaction.getEnrollment().getId()))
            throw new IllegalArgumentException("Đăng ký này đã có giao dịch thanh toán thành công!");

        processStatus(transaction);

        return transactionRepository.addTransaction(transaction);
    }

    @Override
    public void updateTransaction(PaymentTransaction transaction) {
        if(transaction == null || transaction.getId() == null)
            throw new IllegalArgumentException("Giao dịch không hợp lệ!");

        transactionRepository.updateTransaction(transaction);
    }

    @Override
    public PaymentTransaction updateTransactionStatus(Integer id, PaymentTransaction.TransactionStatus newStatus) {
        PaymentTransaction current = transactionRepository.getTransactionByIdForUpdate(id);

        if(current == null)
            throw new IllegalArgumentException("Không tìm thấy giao dịch!");

        Enrollment lockedEnrollment = enrollmentService
                .getEnrollmentByIdForUpdate(current.getEnrollment().getId());
        if(lockedEnrollment == null)
            throw new IllegalArgumentException("Đăng ký không tồn tại!");
        current.setEnrollment(lockedEnrollment);

        if(current.getStatus() != PaymentTransaction.TransactionStatus.PENDING)
            throw new IllegalArgumentException("Chỉ giao dịch đang chờ thanh toán mới được cập nhật!");

        if(newStatus == null || newStatus == PaymentTransaction.TransactionStatus.PENDING)
            throw new IllegalArgumentException("Trạng thái mới phải là SUCCESS hoặc FAILED!");

        if(newStatus == PaymentTransaction.TransactionStatus.SUCCESS
                && lockedEnrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING_PAYMENT)
            throw new IllegalArgumentException(
                    "Chỉ có thể xác nhận thanh toán cho đăng ký đang chờ thanh toán!");

        if(newStatus == PaymentTransaction.TransactionStatus.SUCCESS
                && transactionRepository.existsSuccessfulTransactionExceptId(
                current.getEnrollment().getId(), current.getId()))
            throw new IllegalArgumentException("Đăng ký này đã có giao dịch thanh toán thành công!");

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
    public List<PaymentTransaction> getTransactionsByStudent(Integer studentId) {
        return transactionRepository.getTransactionsByStudent(studentId);
    }

    @Override
    public long countTransactions(Map<String, String> params) {
        return transactionRepository.countTransactions(params);
    }

    @Override
    public BigDecimal sumTransactionAmounts(Map<String, String> params) {
        return transactionRepository.sumTransactionAmounts(params);
    }

    @Override
    public List<Object[]> sumTransactionAmountsByPeriod(Map<String, String> params, String dateFormat) {
        return transactionRepository.sumTransactionAmountsByPeriod(params, dateFormat);
    }

    @Override
    public PaymentTransaction createAutoSuccess(PaymentTransaction transaction) {

        transaction.setStatus(
                PaymentTransaction.TransactionStatus.SUCCESS
        );

        PaymentTransaction saved =
                addTransaction(transaction);

        return saved;
    }

    private void validateTransaction(PaymentTransaction transaction) {
        if(transaction.getEnrollment() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn đăng ký!"
            );

        Enrollment enrollment =
                transaction.getEnrollment();

        if(enrollment.getStatus()
                != Enrollment.EnrollmentStatus.PENDING_PAYMENT)
            throw new IllegalArgumentException(
                    "Chỉ có thể thanh toán cho đăng ký đang chờ thanh toán!"
            );

        if(transaction.getAmount() == null
                || transaction.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException(
                    "Số tiền thanh toán phải lớn hơn 0!"
            );

        if(transaction.getPaymentMethod() == null
                || transaction.getPaymentMethod()
                .trim()
                .isBlank())
            throw new IllegalArgumentException(
                    "Vui lòng nhập phương thức thanh toán!"
            );

        BigDecimal coursePrice =
                enrollment
                        .getCourseClass()
                        .getCourse()
                        .getTuitionFee();

        if(transaction.getAmount()
                .compareTo(coursePrice) != 0)
            throw new IllegalArgumentException(
                    "Số tiền thanh toán không đúng!"
            );

        transaction.setPaymentMethod(
                transaction.getPaymentMethod().trim()
        );
    }

    private void processStatus(PaymentTransaction transaction) {
        if(transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            if(transaction.getPaidAt() == null)
                transaction.setPaidAt(LocalDateTime.now());

            Enrollment enrollment = transaction.getEnrollment();

            if(enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
                enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                enrollmentService.updateEnrollment(enrollment);
            }

            studentLearningPathService.createStudentLearningPath(
                    enrollment.getStudent(), enrollment.getCourseClass().getCourse());
        }

        if(transaction.getStatus() == PaymentTransaction.TransactionStatus.PENDING
                || transaction.getStatus() == PaymentTransaction.TransactionStatus.FAILED)
            transaction.setPaidAt(null);
    }

    private String generateTransactionCode() {
        return "PAY-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}