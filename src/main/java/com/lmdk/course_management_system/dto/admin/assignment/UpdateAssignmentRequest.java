package com.lmdk.course_management_system.dto.admin.assignment;

import java.math.BigDecimal;

public record UpdateAssignmentRequest(
        String name,
        String type,
        BigDecimal maximumScore,
        Integer durationMinutes
) {
}