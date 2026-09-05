package com.lmdk.course_management_system.controllers.api.common;

import com.lmdk.course_management_system.dto.payment.PaymentTransactionRequest;
import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.common.PaymentTransactionMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiPaymentTransactionControllerOwnershipTest {

    @Mock private PaymentTransactionService transactionService;
    @Mock private EnrollmentService enrollmentService;
    @Mock private CurrentUserHelper currentUserHelper;
    @Mock private PaymentTransactionMapper mapper;
    @Mock private Authentication authentication;

    private ApiPaymentTransactionController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiPaymentTransactionController(
                transactionService,
                enrollmentService,
                currentUserHelper,
                mapper
        );
    }

    @Test
    void getTransaction_blocksAnotherStudentTransaction() {
        User student = user(1, User.UserRole.STUDENT);
        PaymentTransaction transaction = transactionForStudent(2);

        when(currentUserHelper.getCurrentUser(authentication)).thenReturn(student);
        when(transactionService.getTransactionById(50)).thenReturn(transaction);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> controller.get(50, authentication)
        );

        assertEquals("Bạn không có quyền xem giao dịch này!", ex.getMessage());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getTransaction_allowsOwnerStudent() {
        User student = user(1, User.UserRole.STUDENT);
        PaymentTransaction transaction = transactionForStudent(1);

        when(currentUserHelper.getCurrentUser(authentication)).thenReturn(student);
        when(transactionService.getTransactionById(50)).thenReturn(transaction);

        assertDoesNotThrow(() -> controller.get(50, authentication));
        verify(mapper).toResponse(transaction);
    }

    @Test
    void getTransaction_allowsAdmin() {
        User admin = user(9, User.UserRole.ADMIN);
        PaymentTransaction transaction = transactionForStudent(1);

        when(currentUserHelper.getCurrentUser(authentication)).thenReturn(admin);
        when(transactionService.getTransactionById(50)).thenReturn(transaction);

        assertDoesNotThrow(() -> controller.get(50, authentication));
        verify(mapper).toResponse(transaction);
    }

    @Test
    void getTransaction_blocksParentRole() {
        User parent = user(3, User.UserRole.PARENT);
        PaymentTransaction transaction = transactionForStudent(1);

        when(currentUserHelper.getCurrentUser(authentication)).thenReturn(parent);
        when(transactionService.getTransactionById(50)).thenReturn(transaction);

        assertThrows(
                ForbiddenException.class,
                () -> controller.get(50, authentication)
        );
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void createPayment_blocksForeignEnrollment() {
        User student = user(1, User.UserRole.STUDENT);
        Enrollment enrollment = enrollmentForStudent(2);
        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setEnrollmentId(10);
        request.setAmount(new BigDecimal("1000000"));
        request.setPaymentMethod("BANK");

        when(currentUserHelper.getCurrentUser(authentication)).thenReturn(student);
        when(enrollmentService.getEnrollmentById(10)).thenReturn(enrollment);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> controller.create(request, authentication)
        );

        assertEquals("Bạn không có quyền thanh toán cho đăng ký này!", ex.getMessage());
        verify(transactionService, never()).addTransaction(any());
    }

    private User user(Integer id, User.UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private Enrollment enrollmentForStudent(Integer studentId) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(10);
        enrollment.setStudent(user(studentId, User.UserRole.STUDENT));
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);
        return enrollment;
    }

    private PaymentTransaction transactionForStudent(Integer studentId) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(50);
        transaction.setEnrollment(enrollmentForStudent(studentId));
        transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
        return transaction;
    }
}
