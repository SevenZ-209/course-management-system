package com.lmdk.course_management_system.dto.admin.assignment;

import java.math.BigDecimal;

public record CreateAssignmentRequest(
        String name,
        Integer courseId,
        String type,
        BigDecimal maximumScore,
        Integer durationMinutes
) {
}