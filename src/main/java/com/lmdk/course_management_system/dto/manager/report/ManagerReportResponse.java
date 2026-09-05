package com.lmdk.course_management_system.dto.manager.report;

import java.math.BigDecimal;

public record ManagerReportResponse(
        Integer courseId,
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
        Long noPathCount
) {
}
