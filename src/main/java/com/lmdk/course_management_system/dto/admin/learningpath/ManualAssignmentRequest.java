package com.lmdk.course_management_system.dto.admin.learningpath;

import java.time.LocalDateTime;

public record ManualAssignmentRequest(
        Integer studentId,
        Integer assignmentId,
        LocalDateTime availableAt,
        LocalDateTime dueAt
) {
}