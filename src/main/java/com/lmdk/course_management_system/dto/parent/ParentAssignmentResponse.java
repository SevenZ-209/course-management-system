package com.lmdk.course_management_system.dto.parent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ParentAssignmentResponse(
        Integer assignedAssignmentId,

        Integer assignmentId,
        String assignmentName,
        String assignmentType,

        Integer courseId,
        String courseName,

        BigDecimal maximumScore,

        String status,

        LocalDateTime assignedAt,
        LocalDateTime availableAt,
        LocalDateTime dueAt,

        String assignmentSource,

        Integer latestAttemptId,
        Integer latestAttemptNumber,
        String latestAttemptStatus,

        LocalDateTime submittedAt,

        BigDecimal autoScore,
        BigDecimal essayScore,
        BigDecimal totalScore,

        Boolean passed
) {
}