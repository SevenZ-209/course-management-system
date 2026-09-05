package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.controllers.AdminReportController;
import com.lmdk.course_management_system.controllers.EnrollmentController;
import com.lmdk.course_management_system.controllers.PaymentTransactionController;
import com.lmdk.course_management_system.dto.common.report.OperationReportResponse;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ThymeleafAdminFunctionalControllerTest {

    @Test
    void enrollmentList_clampsPageAndKeepsFilters() {
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        CourseClassService classService = mock(CourseClassService.class);
        CourseService courseService = mock(CourseService.class);
        UserService userService = mock(UserService.class);
        PaymentTransactionService transactionService = mock(PaymentTransactionService.class);
        EnrollmentController controller = new EnrollmentController(enrollmentService, classService, courseService, userService, transactionService);
        ReflectionTestUtils.setField(controller, "pageSize", 10);
        when(enrollmentService.countEnrollments(anyMap())).thenReturn(11L);
        when(enrollmentService.getEnrollments(anyMap())).thenReturn(List.of());
        when(userService.getUsersByRole(User.UserRole.STUDENT)).thenReturn(List.of());
        when(classService.getAllClasses()).thenReturn(List.of());
        when(courseService.getAllCourses()).thenReturn(List.of());

        Map<String, String> params = new HashMap<>(Map.of("page", "99", "kw", "khoa", "courseId", "4", "classId", "7", "status", "ACTIVE"));
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("admin/enrollments", controller.enrollments(params, model));
        assertEquals(2, model.get("currentPage"));
        assertEquals("khoa", model.get("kw"));
        assertEquals("4", model.get("courseId"));
        assertEquals("7", model.get("classId"));
        assertEquals("ACTIVE", model.get("status"));
        assertEquals("2", params.get("page"));
    }

    @Test
    void addEnrollment_createsPendingEnrollmentAndManualPendingPayment() {
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        CourseClassService classService = mock(CourseClassService.class);
        CourseService courseService = mock(CourseService.class);
        UserService userService = mock(UserService.class);
        PaymentTransactionService transactionService = mock(PaymentTransactionService.class);
        EnrollmentController controller = new EnrollmentController(enrollmentService, classService, courseService, userService, transactionService);

        User student = new User();
        student.setId(10);
        student.setRole(User.UserRole.STUDENT);
        Course course = new Course();
        course.setTuitionFee(new BigDecimal("1500000"));
        CourseClass courseClass = new CourseClass();
        courseClass.setId(20);
        courseClass.setCourse(course);
        when(userService.getUserById(10)).thenReturn(student);
        when(classService.getClassById(20)).thenReturn(courseClass);
        when(enrollmentService.addEnrollment(any())).thenAnswer(invocation -> {
            Enrollment saved = invocation.getArgument(0);
            saved.setId(30);
            return saved;
        });

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/admin/enrollments", controller.addEnrollment(10, 20, redirect));

        ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentService).addEnrollment(enrollmentCaptor.capture());
        assertEquals(Enrollment.EnrollmentStatus.PENDING_PAYMENT, enrollmentCaptor.getValue().getStatus());

        ArgumentCaptor<PaymentTransaction> paymentCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionService).addTransaction(paymentCaptor.capture());
        PaymentTransaction payment = paymentCaptor.getValue();
        assertEquals("MANUAL", payment.getPaymentMethod());
        assertEquals(PaymentTransaction.TransactionStatus.PENDING, payment.getStatus());
        assertEquals(new BigDecimal("1500000"), payment.getAmount());
        assertSame(enrollmentCaptor.getValue(), payment.getEnrollment());
    }

    @Test
    void cancelEnrollment_failsPendingPaymentBeforeCancelingEnrollment() {
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        PaymentTransactionService transactionService = mock(PaymentTransactionService.class);
        EnrollmentController controller = new EnrollmentController(
                enrollmentService, mock(CourseClassService.class), mock(CourseService.class), mock(UserService.class), transactionService);
        Enrollment enrollment = new Enrollment();
        enrollment.setId(30);
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);
        PaymentTransaction pending = new PaymentTransaction();
        pending.setId(40);
        pending.setStatus(PaymentTransaction.TransactionStatus.PENDING);
        when(enrollmentService.getEnrollmentById(30)).thenReturn(enrollment);
        when(transactionService.getTransactionsByEnrollment(30)).thenReturn(List.of(pending));

        assertEquals("redirect:/admin/enrollments", controller.cancelEnrollment(30, new RedirectAttributesModelMap()));
        verify(transactionService).updateTransactionStatus(40, PaymentTransaction.TransactionStatus.FAILED);
        verify(enrollmentService).updateEnrollment(enrollment);
        assertEquals(Enrollment.EnrollmentStatus.CANCELED, enrollment.getStatus());
    }

    @Test
    void paymentList_clampsPageAndKeepsFilters() {
        PaymentTransactionService transactionService = mock(PaymentTransactionService.class);
        CourseService courseService = mock(CourseService.class);
        PaymentTransactionController controller = new PaymentTransactionController(transactionService, courseService);
        ReflectionTestUtils.setField(controller, "pageSize", 10);
        when(transactionService.countTransactions(anyMap())).thenReturn(21L);
        when(transactionService.getTransactions(anyMap())).thenReturn(List.of());
        when(courseService.getAllCourses()).thenReturn(List.of());

        Map<String, String> params = new HashMap<>(Map.of("page", "9", "kw", "PAY", "courseId", "2", "status", "PENDING", "date", "2026-09-01"));
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("admin/payment-transactions", controller.transactions(params, model));
        assertEquals(3, model.get("currentPage"));
        assertEquals("PAY", model.get("kw"));
        assertEquals("2", model.get("courseId"));
        assertEquals("PENDING", model.get("status"));
        assertEquals("2026-09-01", model.get("date"));
    }

    @Test
    void paymentUpdate_usesGuardedStatusWorkflow() {
        PaymentTransactionService transactionService = mock(PaymentTransactionService.class);
        PaymentTransactionController controller = new PaymentTransactionController(transactionService, mock(CourseService.class));
        PaymentTransaction updated = new PaymentTransaction();
        updated.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
        when(transactionService.updateTransactionStatus(40, PaymentTransaction.TransactionStatus.SUCCESS)).thenReturn(updated);

        controller.updateStatus(40, "SUCCESS", new RedirectAttributesModelMap());
        verify(transactionService).updateTransactionStatus(40, PaymentTransaction.TransactionStatus.SUCCESS);
        verify(transactionService, never()).updateTransaction(any());
    }

    @Test
    void paymentUpdate_rejectsUnsupportedStatusBeforeService() {
        PaymentTransactionService transactionService = mock(PaymentTransactionService.class);
        PaymentTransactionController controller = new PaymentTransactionController(transactionService, mock(CourseService.class));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/admin/payment-transactions", controller.updateStatus(40, "REFUNDED", redirect));
        verify(transactionService, never()).updateTransactionStatus(anyInt(), any());
        assertNotNull(redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void reportCustomRange_delegatesSameDatesAndComputesKpis() {
        OperationReportService reportService = mock(OperationReportService.class);
        CourseService courseService = mock(CourseService.class);
        AdminReportController controller = new AdminReportController(reportService, courseService);
        LocalDate from = LocalDate.of(2026, 8, 1), to = LocalDate.of(2026, 9, 1);
        OperationReportResponse response = new OperationReportResponse(
                null, from, to, "DAY", new BigDecimal("20000"),
                2L, 0L, 1L, 2L, 0L, 1L, 0L, 0L, 0L, 2L, new ArrayList<>());
        when(reportService.getReport(null, from, to)).thenReturn(response);
        when(courseService.getAllCourses()).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("admin/reports", controller.reports(null, "CUSTOM", from, to, model));
        verify(reportService).getReport(null, from, to);
        assertEquals(67L, model.get("paymentSuccessRate"));
        assertEquals(3L, model.get("paymentTotal"));
        assertEquals("Toàn hệ thống", model.get("scopeName"));
    }
}
