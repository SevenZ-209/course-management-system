package com.lmdk.course_management_system.dto.student.assignment;

import java.time.LocalDateTime;

public record AttemptResponse(
        Integer attemptId,
        Integer assignedAssignmentId,
        Integer attemptNumber,
        LocalDateTime startedAt,
        LocalDateTime endTime,
        long remainingSeconds,
        String status
) {
}