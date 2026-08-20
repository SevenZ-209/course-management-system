package com.lmdk.course_management_system.dto.admin.assignment;

import java.math.BigDecimal;

public record AdminAssignmentResponse(
        Integer id,
        String name,
        Integer courseId,
        String courseName,
        String type,
        BigDecimal maximumScore,
        Integer durationMinutes,
        String status
) {
}