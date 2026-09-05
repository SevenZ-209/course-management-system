package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationReportServiceImplTest {

    @Mock private PaymentTransactionService transactionService;
    @Mock private EnrollmentService enrollmentService;

    private OperationReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OperationReportServiceImpl(transactionService, enrollmentService);
        lenient().when(transactionService.sumTransactionAmountsByPeriod(anyMap(), anyString())).thenReturn(List.of());
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

        var response = service.getReport(null, null, null);

        assertEquals(new BigDecimal("12500000"), response.successfulRevenue());
        assertEquals(8L, response.successfulPayments());
        assertEquals(2L, response.pendingPayments());
        assertEquals(1L, response.failedPayments());
        assertEquals(10L, response.activeEnrollments());
        assertEquals(5L, response.inProgressCount());
        assertEquals(1L, response.pausedCount());
        assertEquals(3L, response.completedCount());
        assertEquals(1L, response.noPathCount());
        assertEquals("MONTH", response.revenueGranularity());
    }

    @Test
    void getReport_courseAndDateFilters_areAppliedToAllQueries() {
        when(transactionService.countTransactions(anyMap())).thenReturn(0L);
        when(transactionService.sumTransactionAmounts(anyMap())).thenReturn(BigDecimal.ZERO);
        when(enrollmentService.countEnrollments(anyMap())).thenReturn(0L);

        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 31);
        service.getReport(15, from, to);

        verify(transactionService, times(3)).countTransactions(argThat(params ->
                "15".equals(params.get("courseId"))
                        && "2026-08-01".equals(params.get("fromDate"))
                        && "2026-08-31".equals(params.get("toDate"))));
        verify(transactionService).sumTransactionAmounts(argThat(params ->
                "15".equals(params.get("courseId"))
                        && "SUCCESS".equals(params.get("status"))
                        && "2026-08-01".equals(params.get("fromDate"))
                        && "2026-08-31".equals(params.get("toDate"))));
        verify(enrollmentService, times(6)).countEnrollments(argThat(params ->
                "15".equals(params.get("courseId"))
                        && "2026-08-01".equals(params.get("fromDate"))
                        && "2026-08-31".equals(params.get("toDate"))));
        verify(transactionService).sumTransactionAmountsByPeriod(anyMap(), eq("%Y-%m-%d"));
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

        var response = service.getReport(null, null, null);
        assertEquals(0L, response.noPathCount());
    }

    @Test
    void getReport_revenueTrend_fillsMissingDaysWithZero() {
        when(transactionService.countTransactions(anyMap())).thenReturn(0L);
        when(transactionService.sumTransactionAmounts(anyMap())).thenReturn(new BigDecimal("100000"));
        when(enrollmentService.countEnrollments(anyMap())).thenReturn(0L);
        when(transactionService.sumTransactionAmountsByPeriod(anyMap(), eq("%Y-%m-%d")))
                .thenReturn(List.<Object[]>of(new Object[]{"2026-08-02", new BigDecimal("100000")}));

        var response = service.getReport(null, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3));

        assertEquals(3, response.revenueTrend().size());
        assertEquals(BigDecimal.ZERO, response.revenueTrend().get(0).revenue());
        assertEquals(new BigDecimal("100000"), response.revenueTrend().get(1).revenue());
        assertEquals(BigDecimal.ZERO, response.revenueTrend().get(2).revenue());
        assertEquals("02/08", response.revenueTrend().get(1).label());
    }

    @Test
    void getReport_invalidDateRange_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.getReport(
                null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 8, 31)));
        verifyNoInteractions(transactionService, enrollmentService);
    }
}
