package com.lmdk.course_management_system.dto.common.report;

import java.math.BigDecimal;

public record OperationRevenuePointResponse(
        String period,
        String label,
        BigDecimal revenue
) {
}
