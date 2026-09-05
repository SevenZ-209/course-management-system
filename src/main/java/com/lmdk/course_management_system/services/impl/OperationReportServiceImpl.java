package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.dto.common.report.OperationReportResponse;
import com.lmdk.course_management_system.dto.common.report.OperationRevenuePointResponse;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.OperationReportService;
import com.lmdk.course_management_system.services.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OperationReportServiceImpl implements OperationReportService {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MM/yyyy");

    private final PaymentTransactionService transactionService;
    private final EnrollmentService enrollmentService;

    @Override
    public OperationReportResponse getReport(Integer courseId, LocalDate fromDate, LocalDate toDate) {
        validateRange(fromDate, toDate);

        long successfulPayments = countPayments(courseId, fromDate, toDate, PaymentTransaction.TransactionStatus.SUCCESS);
        long pendingPayments = countPayments(courseId, fromDate, toDate, PaymentTransaction.TransactionStatus.PENDING);
        long failedPayments = countPayments(courseId, fromDate, toDate, PaymentTransaction.TransactionStatus.FAILED);

        Map<String, String> revenueParams = params(courseId, fromDate, toDate);
        revenueParams.put("status", PaymentTransaction.TransactionStatus.SUCCESS.name());
        BigDecimal successfulRevenue = transactionService.sumTransactionAmounts(revenueParams);

        long activeEnrollments = countEnrollments(courseId, fromDate, toDate, Enrollment.EnrollmentStatus.ACTIVE);
        long pendingEnrollments = countEnrollments(courseId, fromDate, toDate, Enrollment.EnrollmentStatus.PENDING_PAYMENT);
        long canceledEnrollments = countEnrollments(courseId, fromDate, toDate, Enrollment.EnrollmentStatus.CANCELED);

        long inProgressCount = countProgress(courseId, fromDate, toDate, StudentLearningPath.ProgressStatus.IN_PROGRESS);
        long pausedCount = countProgress(courseId, fromDate, toDate, StudentLearningPath.ProgressStatus.PAUSED);
        long completedCount = countProgress(courseId, fromDate, toDate, StudentLearningPath.ProgressStatus.COMPLETED);
        long noPathCount = Math.max(activeEnrollments - inProgressCount - pausedCount - completedCount, 0L);

        String granularity = useMonthlyTrend(fromDate, toDate) ? "MONTH" : "DAY";
        List<OperationRevenuePointResponse> revenueTrend = buildRevenueTrend(revenueParams, fromDate, toDate, granularity);

        return new OperationReportResponse(
                courseId, fromDate, toDate, granularity,
                successfulRevenue == null ? BigDecimal.ZERO : successfulRevenue,
                successfulPayments, pendingPayments, failedPayments,
                activeEnrollments, pendingEnrollments, canceledEnrollments,
                inProgressCount, pausedCount, completedCount, noPathCount,
                revenueTrend
        );
    }

    private List<OperationRevenuePointResponse> buildRevenueTrend(
            Map<String, String> revenueParams, LocalDate fromDate, LocalDate toDate, String granularity) {
        String format = "MONTH".equals(granularity) ? "%Y-%m" : "%Y-%m-%d";
        List<Object[]> raw = transactionService.sumTransactionAmountsByPeriod(revenueParams, format);
        Map<String, BigDecimal> values = new HashMap<>();

        if(raw != null) {
            for(Object[] row : raw) {
                if(row == null || row.length < 2 || row[0] == null) continue;
                BigDecimal value = row[1] instanceof BigDecimal amount
                        ? amount : new BigDecimal(String.valueOf(row[1]));
                values.put(String.valueOf(row[0]), value);
            }
        }

        if(fromDate == null || toDate == null)
            return values.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new OperationRevenuePointResponse(
                            entry.getKey(), formatLabel(entry.getKey(), granularity), entry.getValue()))
                    .toList();

        List<OperationRevenuePointResponse> result = new ArrayList<>();
        if("MONTH".equals(granularity)) {
            YearMonth current = YearMonth.from(fromDate);
            YearMonth end = YearMonth.from(toDate);
            while(!current.isAfter(end)) {
                String period = current.toString();
                result.add(new OperationRevenuePointResponse(
                        period, current.format(MONTH_LABEL), values.getOrDefault(period, BigDecimal.ZERO)));
                current = current.plusMonths(1);
            }
        } else {
            LocalDate current = fromDate;
            while(!current.isAfter(toDate)) {
                String period = current.toString();
                result.add(new OperationRevenuePointResponse(
                        period, current.format(DAY_LABEL), values.getOrDefault(period, BigDecimal.ZERO)));
                current = current.plusDays(1);
            }
        }
        return result;
    }

    private String formatLabel(String period, String granularity) {
        try {
            return "MONTH".equals(granularity)
                    ? YearMonth.parse(period).format(MONTH_LABEL)
                    : LocalDate.parse(period).format(DAY_LABEL);
        } catch(Exception ex) {
            return period;
        }
    }

    private boolean useMonthlyTrend(LocalDate fromDate, LocalDate toDate) {
        if(fromDate == null || toDate == null) return true;
        return ChronoUnit.DAYS.between(fromDate, toDate) > 120;
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate) {
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày!");
    }

    private long countPayments(Integer courseId, LocalDate fromDate, LocalDate toDate,
                               PaymentTransaction.TransactionStatus status) {
        Map<String, String> params = params(courseId, fromDate, toDate);
        params.put("status", status.name());
        return transactionService.countTransactions(params);
    }

    private long countEnrollments(Integer courseId, LocalDate fromDate, LocalDate toDate,
                                  Enrollment.EnrollmentStatus status) {
        Map<String, String> params = params(courseId, fromDate, toDate);
        params.put("status", status.name());
        return enrollmentService.countEnrollments(params);
    }

    private long countProgress(Integer courseId, LocalDate fromDate, LocalDate toDate,
                               StudentLearningPath.ProgressStatus status) {
        Map<String, String> params = params(courseId, fromDate, toDate);
        params.put("status", Enrollment.EnrollmentStatus.ACTIVE.name());
        params.put("progressStatus", status.name());
        if(courseId != null) params.put("progressCourseId", String.valueOf(courseId));
        return enrollmentService.countEnrollments(params);
    }

    private Map<String, String> params(Integer courseId, LocalDate fromDate, LocalDate toDate) {
        Map<String, String> params = new LinkedHashMap<>();
        if(courseId != null) params.put("courseId", String.valueOf(courseId));
        if(fromDate != null) params.put("fromDate", fromDate.toString());
        if(toDate != null) params.put("toDate", toDate.toString());
        return params;
    }
}
