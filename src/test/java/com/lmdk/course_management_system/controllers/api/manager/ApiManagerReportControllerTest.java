package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiManagerReportControllerTest {

    @Mock private PaymentTransactionService transactionService;
    @Mock private EnrollmentService enrollmentService;

    private ApiManagerReportController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiManagerReportController(transactionService, enrollmentService);
    }

    @Test
    void getReport_returnsOperationalSummary() {
        when(transactionService.countTransactions(anyMap())).thenAnswer(invocation -> {
            Map<String, String> params = invocation.getArgument(0);
            return switch(params.get("status")) {
                case "SUCCESS" -> 8L;
                case "PENDING" -> 2L;
                case "FAILED" -> 1L;
                default -> 0L;
            };
        });
        when(transactionService.sumTransactionAmounts(anyMap())).thenReturn(new BigDecimal("12500000"));
        when(enrollmentService.countEnrollments(anyMap())).thenAnswer(invocation -> {
            Map<String, String> params = invocation.getArgument(0);
            if(params.containsKey("progressStatus")) {
                return switch(params.get("progressStatus")) {
                    case "IN_PROGRESS" -> 5L;
                    case "PAUSED" -> 1L;
                    case "COMPLETED" -> 3L;
                    default -> 0L;
                };
            }
            return switch(params.get("status")) {
                case "ACTIVE" -> 10L;
                case "PENDING_PAYMENT" -> 2L;
                case "CANCELED" -> 4L;
                default -> 0L;
            };
        });

        var response = controller.getReport(null);

        assertEquals(new BigDecimal("12500000"), response.successfulRevenue());
        assertEquals(8L, response.successfulPayments());
        assertEquals(2L, response.pendingPayments());
        assertEquals(1L, response.failedPayments());
        assertEquals(10L, response.activeEnrollments());
        assertEquals(5L, response.inProgressCount());
        assertEquals(1L, response.pausedCount());
        assertEquals(3L, response.completedCount());
        assertEquals(1L, response.noPathCount());
    }

    @Test
    void getReport_courseFilter_isAppliedToAllQueries() {
        when(transactionService.countTransactions(anyMap())).thenReturn(0L);
        when(transactionService.sumTransactionAmounts(anyMap())).thenReturn(BigDecimal.ZERO);
        when(enrollmentService.countEnrollments(anyMap())).thenReturn(0L);

        controller.getReport(15);

        verify(transactionService, times(3)).countTransactions(argThat(params -> "15".equals(params.get("courseId"))));
        verify(transactionService).sumTransactionAmounts(argThat(params ->
                "15".equals(params.get("courseId")) && "SUCCESS".equals(params.get("status"))
        ));
        verify(enrollmentService, times(6)).countEnrollments(argThat(params -> "15".equals(params.get("courseId"))));
    }

    @Test
    void getReport_noPathCount_neverBecomesNegative() {
        when(transactionService.countTransactions(anyMap())).thenReturn(0L);
        when(transactionService.sumTransactionAmounts(anyMap())).thenReturn(BigDecimal.ZERO);
        when(enrollmentService.countEnrollments(anyMap())).thenAnswer(invocation -> {
            Map<String, String> params = invocation.getArgument(0);
            if(!params.containsKey("progressStatus") && "ACTIVE".equals(params.get("status"))) return 2L;
            if(params.containsKey("progressStatus")) return 2L;
            return 0L;
        });

        var response = controller.getReport(null);

        assertEquals(0L, response.noPathCount());
    }
}
