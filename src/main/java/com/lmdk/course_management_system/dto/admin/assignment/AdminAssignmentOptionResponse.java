package com.lmdk.course_management_system.dto.admin.assignment;

import java.math.BigDecimal;

public record AdminAssignmentOptionResponse(
        Integer id,
        String name,
        BigDecimal maximumScore
) {
}