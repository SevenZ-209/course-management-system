package com.lmdk.course_management_system.dto.student.assignment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssignedAssignmentResponse(
        Integer assignedAssignmentId,
        Integer assignmentId,
        String assignmentName,
        String courseName,
        String assignmentType,
        BigDecimal maximumScore,
        Integer durationMinutes,
        LocalDateTime availableAt,
        LocalDateTime dueAt,
        String status,
        boolean canStart,
        Integer latestAttemptId,
        Integer latestAttemptNumber,
        String latestAttemptStatus
) {
}