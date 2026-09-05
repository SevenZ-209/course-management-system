package com.lmdk.course_management_system.dto.manager.report;

import java.math.BigDecimal;

public record ManagerRevenuePointResponse(
        String period,
        String label,
        BigDecimal revenue
) {
}
