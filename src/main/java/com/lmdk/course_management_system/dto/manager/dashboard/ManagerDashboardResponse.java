package com.lmdk.course_management_system.dto.manager.dashboard;

import com.lmdk.course_management_system.dto.manager.payment.ManagerPaymentTransactionResponse;
import java.util.List;

public record ManagerDashboardResponse(
        Long activeCourses,
        Long activeClasses,
        Long activeStudents,
        Long activeTeachers,
        Long activeEnrollments,
        Long pendingEnrollments,
        Long pendingPayments,
        List<ManagerPaymentTransactionResponse> recentPayments,
        List<ManagerOnlineSessionResponse> todaySessions
) {
}
