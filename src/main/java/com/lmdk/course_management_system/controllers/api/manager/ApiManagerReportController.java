package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.manager.report.ManagerReportResponse;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/reports")
@RequiredArgsConstructor
public class ApiManagerReportController {

    private final PaymentTransactionService transactionService;
    private final EnrollmentService enrollmentService;

    @GetMapping
    public ManagerReportResponse getReport(@RequestParam(required = false) Integer courseId) {
        long successfulPayments = countPayments(courseId, PaymentTransaction.TransactionStatus.SUCCESS);
        long pendingPayments = countPayments(courseId, PaymentTransaction.TransactionStatus.PENDING);
        long failedPayments = countPayments(courseId, PaymentTransaction.TransactionStatus.FAILED);

        Map<String, String> revenueParams = params(courseId);
        revenueParams.put("status", PaymentTransaction.TransactionStatus.SUCCESS.name());
        BigDecimal successfulRevenue = transactionService.sumTransactionAmounts(revenueParams);

        long activeEnrollments = countEnrollments(courseId, Enrollment.EnrollmentStatus.ACTIVE);
        long pendingEnrollments = countEnrollments(courseId, Enrollment.EnrollmentStatus.PENDING_PAYMENT);
        long canceledEnrollments = countEnrollments(courseId, Enrollment.EnrollmentStatus.CANCELED);

        long inProgressCount = countProgress(courseId, StudentLearningPath.ProgressStatus.IN_PROGRESS);
        long pausedCount = countProgress(courseId, StudentLearningPath.ProgressStatus.PAUSED);
        long completedCount = countProgress(courseId, StudentLearningPath.ProgressStatus.COMPLETED);
        long noPathCount = Math.max(activeEnrollments - inProgressCount - pausedCount - completedCount, 0L);

        return new ManagerReportResponse(
                courseId,
                successfulRevenue == null ? BigDecimal.ZERO : successfulRevenue,
                successfulPayments, pendingPayments, failedPayments,
                activeEnrollments, pendingEnrollments, canceledEnrollments,
                inProgressCount, pausedCount, completedCount, noPathCount
        );
    }

    private long countPayments(Integer courseId, PaymentTransaction.TransactionStatus status) {
        Map<String, String> params = params(courseId);
        params.put("status", status.name());
        return transactionService.countTransactions(params);
    }

    private long countEnrollments(Integer courseId, Enrollment.EnrollmentStatus status) {
        Map<String, String> params = params(courseId);
        params.put("status", status.name());
        return enrollmentService.countEnrollments(params);
    }

    private long countProgress(Integer courseId, StudentLearningPath.ProgressStatus status) {
        Map<String, String> params = params(courseId);
        params.put("status", Enrollment.EnrollmentStatus.ACTIVE.name());
        params.put("progressStatus", status.name());
        if(courseId != null) params.put("progressCourseId", String.valueOf(courseId));
        return enrollmentService.countEnrollments(params);
    }

    private Map<String, String> params(Integer courseId) {
        Map<String, String> params = new HashMap<>();
        if(courseId != null) params.put("courseId", String.valueOf(courseId));
        return params;
    }
}
