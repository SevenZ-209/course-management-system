package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.dto.common.report.OperationReportResponse;

import java.time.LocalDate;

public interface OperationReportService {
    OperationReportResponse getReport(Integer courseId, LocalDate fromDate, LocalDate toDate);
}
