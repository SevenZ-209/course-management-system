package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.PaymentTransactionRepository;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.StudentLearningPathService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionServiceImplTest {

    @Mock private PaymentTransactionRepository transactionRepository;
    @Mock private EnrollmentService enrollmentService;
    @Mock private StudentLearningPathService studentLearningPathService;

    private PaymentTransactionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentTransactionServiceImpl(transactionRepository, enrollmentService, studentLearningPathService);
    }

    @Test
    void createAutoSuccess_activatesEnrollmentAndCreatesLearningPathOnce() {
        Enrollment enrollment = pendingEnrollment();
        PaymentTransaction transaction = transaction(enrollment, "  BANK  ");

        when(enrollmentService.getEnrollmentByIdForUpdate(1)).thenReturn(enrollment);
        when(transactionRepository.existsByTransactionCode(anyString())).thenReturn(false);
        when(transactionRepository.existsSuccessfulTransaction(1)).thenReturn(false);
        when(transactionRepository.addTransaction(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        PaymentTransaction saved = service.createAutoSuccess(transaction);

        assertEquals(PaymentTransaction.TransactionStatus.SUCCESS, saved.getStatus());
        assertEquals(Enrollment.EnrollmentStatus.ACTIVE, enrollment.getStatus());
        assertEquals("BANK", saved.getPaymentMethod());
        assertNotNull(saved.getTransactionCode());
        assertNotNull(saved.getPaidAt());

        verify(enrollmentService).updateEnrollment(enrollment);
        verify(studentLearningPathService, times(1))
                .createStudentLearningPath(enrollment.getStudent(), enrollment.getCourseClass().getCourse());
        verify(transactionRepository, times(1)).addTransaction(saved);
    }

    @Test
    void addTransaction_rejectsEnrollmentThatIsAlreadyActive() {
        Enrollment enrollment = pendingEnrollment();
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        PaymentTransaction transaction = transaction(enrollment, "BANK");
        transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);

        when(enrollmentService.getEnrollmentByIdForUpdate(1)).thenReturn(enrollment);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addTransaction(transaction));

        assertEquals("Chỉ có thể thanh toán cho đăng ký đang chờ thanh toán!", ex.getMessage());
        verify(transactionRepository, never()).addTransaction(any());
        verify(studentLearningPathService, never()).createStudentLearningPath(any(), any());
    }

    @Test
    void addTransaction_rejectsSecondSuccessfulPaymentForEnrollment() {
        Enrollment enrollment = pendingEnrollment();
        PaymentTransaction transaction = transaction(enrollment, "BANK");
        transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
        transaction.setTransactionCode("PAY-EXISTING");

        when(enrollmentService.getEnrollmentByIdForUpdate(1)).thenReturn(enrollment);
        when(transactionRepository.existsByTransactionCode("PAY-EXISTING")).thenReturn(false);
        when(transactionRepository.existsSuccessfulTransaction(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addTransaction(transaction));

        assertEquals("Đăng ký này đã có giao dịch thanh toán thành công!", ex.getMessage());
        assertEquals(Enrollment.EnrollmentStatus.PENDING_PAYMENT, enrollment.getStatus());
        verify(enrollmentService, never()).updateEnrollment(any());
        verify(studentLearningPathService, never()).createStudentLearningPath(any(), any());
        verify(transactionRepository, never()).addTransaction(any());
    }

    @Test
    void updateTransactionStatus_successActivatesEnrollmentOnce() {
        Enrollment enrollment = pendingEnrollment();

        PaymentTransaction current = transaction(enrollment, "BANK");
        current.setId(20);
        current.setStatus(PaymentTransaction.TransactionStatus.PENDING);
        current.setTransactionCode("PAY-20");

        when(transactionRepository.getTransactionByIdForUpdate(20)).thenReturn(current);
        when(enrollmentService.getEnrollmentByIdForUpdate(1)).thenReturn(enrollment);
        when(transactionRepository.existsSuccessfulTransactionExceptId(1, 20)).thenReturn(false);

        PaymentTransaction updated = service.updateTransactionStatus(20, PaymentTransaction.TransactionStatus.SUCCESS);

        assertEquals(PaymentTransaction.TransactionStatus.SUCCESS, updated.getStatus());
        assertEquals(Enrollment.EnrollmentStatus.ACTIVE, enrollment.getStatus());
        assertNotNull(updated.getPaidAt());
        verify(enrollmentService).updateEnrollment(enrollment);
        verify(studentLearningPathService, times(1))
                .createStudentLearningPath(enrollment.getStudent(), enrollment.getCourseClass().getCourse());
        verify(transactionRepository).updateTransaction(current);
    }

    private Enrollment pendingEnrollment() {
        User student = new User();
        student.setId(1);
        student.setRole(User.UserRole.STUDENT);
        student.setStatus(User.UserStatus.ACTIVE);

        Course course = new Course();
        course.setId(100);
        course.setTuitionFee(new BigDecimal("1500000.00"));

        CourseClass courseClass = new CourseClass();
        courseClass.setId(11);
        courseClass.setCourse(course);
        courseClass.setMaxStudents(30);
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1);
        enrollment.setStudent(student);
        enrollment.setCourseClass(courseClass);
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);
        return enrollment;
    }

    private PaymentTransaction transaction(Enrollment enrollment, String method) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setEnrollment(enrollment);
        transaction.setAmount(new BigDecimal("1500000.00"));
        transaction.setPaymentMethod(method);
        return transaction;
    }
}
