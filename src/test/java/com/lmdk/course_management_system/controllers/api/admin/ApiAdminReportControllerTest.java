package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.common.report.OperationReportResponse;
import com.lmdk.course_management_system.services.OperationReportService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ApiAdminReportControllerTest {

    @Test
    void getReport_delegatesToSharedReportService() {
        OperationReportService service = mock(OperationReportService.class);
        ApiAdminReportController controller = new ApiAdminReportController(service);
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 1);
        OperationReportResponse expected = response(null, from, to);
        when(service.getReport(null, from, to)).thenReturn(expected);

        assertSame(expected, controller.getReport(null, from, to));
        verify(service).getReport(null, from, to);
    }

    private OperationReportResponse response(Integer courseId, LocalDate from, LocalDate to) {
        return new OperationReportResponse(courseId, from, to, "DAY", BigDecimal.ZERO,
                0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, List.of());
    }
}
