package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.dto.common.report.OperationReportResponse;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.OperationReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final OperationReportService reportService;
    private final CourseService courseService;

    @GetMapping
    public String reports(@RequestParam(required = false) Integer courseId,
                          @RequestParam(defaultValue = "YEAR") String range,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                          Model model) {
        String normalizedRange = normalizeRange(range);
        LocalDate[] dates = resolveRange(normalizedRange, fromDate, toDate);
        OperationReportResponse report = reportService.getReport(courseId, dates[0], dates[1]);

        long paymentTotal = report.successfulPayments() + report.pendingPayments() + report.failedPayments();
        long enrollmentTotal = report.activeEnrollments() + report.pendingEnrollments() + report.canceledEnrollments();
        long progressTotal = report.inProgressCount() + report.pausedCount() + report.completedCount() + report.noPathCount();

        model.addAttribute("report", report);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("courseId", courseId);
        model.addAttribute("range", normalizedRange);
        model.addAttribute("fromDate", dates[0]);
        model.addAttribute("toDate", dates[1]);
        model.addAttribute("scopeName", resolveScopeName(courseId));
        model.addAttribute("paymentTotal", paymentTotal);
        model.addAttribute("enrollmentTotal", enrollmentTotal);
        model.addAttribute("progressTotal", progressTotal);
        model.addAttribute("paymentSuccessRate", percent(report.successfulPayments(), paymentTotal));
        model.addAttribute("paymentPendingRate", percent(report.pendingPayments(), paymentTotal));
        model.addAttribute("paymentFailedRate", percent(report.failedPayments(), paymentTotal));
        model.addAttribute("activeEnrollmentRate", percent(report.activeEnrollments(), enrollmentTotal));
        model.addAttribute("pendingEnrollmentRate", percent(report.pendingEnrollments(), enrollmentTotal));
        model.addAttribute("canceledEnrollmentRate", percent(report.canceledEnrollments(), enrollmentTotal));
        model.addAttribute("inProgressRate", percent(report.inProgressCount(), progressTotal));
        model.addAttribute("pausedRate", percent(report.pausedCount(), progressTotal));
        model.addAttribute("completedProgressRate", percent(report.completedCount(), progressTotal));
        model.addAttribute("noPathRate", percent(report.noPathCount(), progressTotal));
        model.addAttribute("completedRate", percent(report.completedCount(), report.activeEnrollments()));
        return "admin/reports";
    }

    LocalDate[] resolveRange(String range, LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        return switch(normalizeRange(range)) {
            case "TODAY" -> new LocalDate[]{today, today};
            case "LAST_7_DAYS" -> new LocalDate[]{today.minusDays(6), today};
            case "LAST_30_DAYS" -> new LocalDate[]{today.minusDays(29), today};
            case "MONTH" -> new LocalDate[]{today.withDayOfMonth(1), today};
            case "QUARTER" -> {
                int firstMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                yield new LocalDate[]{LocalDate.of(today.getYear(), firstMonth, 1), today};
            }
            case "PREVIOUS_QUARTER" -> {
                LocalDate previous = today.minusMonths(3);
                int firstMonth = ((previous.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate start = LocalDate.of(previous.getYear(), firstMonth, 1);
                yield new LocalDate[]{start, start.plusMonths(3).minusDays(1)};
            }
            case "PREVIOUS_YEAR" -> new LocalDate[]{LocalDate.of(today.getYear() - 1, Month.JANUARY, 1), LocalDate.of(today.getYear() - 1, Month.DECEMBER, 31)};
            case "CUSTOM" -> new LocalDate[]{fromDate == null ? today.withDayOfYear(1) : fromDate, toDate == null ? today : toDate};
            default -> new LocalDate[]{today.with(TemporalAdjusters.firstDayOfYear()), today};
        };
    }

    private String normalizeRange(String range) {
        if(range == null || range.isBlank()) return "YEAR";
        String normalized = range.trim().toUpperCase();
        return switch(normalized) {
            case "TODAY", "LAST_7_DAYS", "LAST_30_DAYS", "MONTH", "QUARTER", "YEAR",
                 "PREVIOUS_QUARTER", "PREVIOUS_YEAR", "CUSTOM" -> normalized;
            default -> "YEAR";
        };
    }

    private String resolveScopeName(Integer courseId) {
        if(courseId == null) return "Toàn hệ thống";
        Course course = courseService.getCourseById(courseId);
        return course == null ? "Khóa học #" + courseId : course.getName();
    }

    private long percent(long value, long total) {
        if(total <= 0) return 0;
        return java.math.BigDecimal.valueOf(value * 100.0 / total)
                .setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
