package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.common.report.OperationReportResponse;
import com.lmdk.course_management_system.services.OperationReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ApiAdminReportController {

    private final OperationReportService reportService;

    @GetMapping
    public OperationReportResponse getReport(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return reportService.getReport(courseId, fromDate, toDate);
    }
}
