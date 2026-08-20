package com.lmdk.course_management_system.dto.teacher.assignment;

import java.math.BigDecimal;

public record TeacherAvailableAssignmentResponse(
        Integer assignmentId,
        String assignmentName,
        String type,
        BigDecimal maximumScore,
        Integer durationMinutes
) {
}