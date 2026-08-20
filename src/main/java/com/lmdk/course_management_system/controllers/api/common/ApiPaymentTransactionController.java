package com.lmdk.course_management_system.controllers.api.common;

import com.lmdk.course_management_system.dto.payment.PaymentTransactionRequest;
import com.lmdk.course_management_system.dto.payment.PaymentTransactionResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.common.PaymentTransactionMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-transactions")
@RequiredArgsConstructor
public class ApiPaymentTransactionController {

    private final PaymentTransactionService transactionService;
    private final EnrollmentService enrollmentService;
    private final CurrentUserHelper currentUserHelper;
    private final PaymentTransactionMapper paymentTransactionMapper;

    @PostMapping
    public PaymentTransactionResponse create(
            @RequestBody PaymentTransactionRequest request,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentUser(authentication);

        if(student.getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException(
                    "Chỉ học viên mới được tạo giao dịch!"
            );

        Enrollment enrollment =
                enrollmentService.getEnrollmentById(
                        request.getEnrollmentId()
                );

        if(enrollment == null)
            throw new IllegalArgumentException(
                    "Đăng ký không tồn tại!"
            );

        if(!enrollment.getStudent()
                .getId()
                .equals(student.getId()))
            throw new IllegalArgumentException(
                    "Bạn không có quyền thanh toán cho đăng ký này!"
            );

        if(enrollment.getStatus()
                != Enrollment.EnrollmentStatus.PENDING_PAYMENT)
            throw new IllegalArgumentException(
                    "Đăng ký này không ở trạng thái chờ thanh toán!"
            );

        PaymentTransaction transaction =
                new PaymentTransaction();

        transaction.setEnrollment(enrollment);
        transaction.setAmount(request.getAmount());
        transaction.setPaymentMethod(
                request.getPaymentMethod()
        );
        transaction.setTransactionCode(
                request.getTransactionCode()
        );
        transaction.setStatus(
                PaymentTransaction.TransactionStatus.PENDING
        );

        PaymentTransaction saved =
                transactionService.addTransaction(transaction);

        return paymentTransactionMapper
                .toResponse(saved);
    }

    @PutMapping("/{id}/status")
    public PaymentTransactionResponse updateStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ) {
        PaymentTransaction.TransactionStatus newStatus;

        try {
            newStatus =
                    PaymentTransaction.TransactionStatus
                            .valueOf(status.toUpperCase());
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái giao dịch không hợp lệ!"
            );
        }

        PaymentTransaction updated =
                transactionService
                        .updateTransactionStatus(
                                id,
                                newStatus
                        );

        return paymentTransactionMapper
                .toResponse(updated);
    }

    @GetMapping("/{id}")
    public PaymentTransactionResponse get(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        User currentUser =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        PaymentTransaction transaction =
                transactionService
                        .getTransactionById(id);

        if(transaction == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy giao dịch!"
            );

        if(currentUser.getRole()
                == User.UserRole.STUDENT) {

            Integer ownerId =
                    transaction.getEnrollment()
                            .getStudent()
                            .getId();

            if(!ownerId.equals(currentUser.getId()))
                throw new IllegalArgumentException(
                        "Bạn không có quyền xem giao dịch này!"
                );
        }

        return paymentTransactionMapper
                .toResponse(transaction);
    }
}