package com.lmdk.course_management_system.dto.common.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OperationReportResponse(
        Integer courseId,
        LocalDate fromDate,
        LocalDate toDate,
        String revenueGranularity,
        BigDecimal successfulRevenue,
        Long successfulPayments,
        Long pendingPayments,
        Long failedPayments,
        Long activeEnrollments,
        Long pendingEnrollments,
        Long canceledEnrollments,
        Long inProgressCount,
        Long pausedCount,
        Long completedCount,
        Long noPathCount,
        List<OperationRevenuePointResponse> revenueTrend
) {
}
