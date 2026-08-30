package com.lmdk.course_management_system.dto.admin.assignment;

import java.math.BigDecimal;

public record CreateAssignmentRequest(
        Integer courseId,
        Integer lessonId,
        String name,
        String type,
        BigDecimal maximumScore,
        Integer durationMinutes
) {}