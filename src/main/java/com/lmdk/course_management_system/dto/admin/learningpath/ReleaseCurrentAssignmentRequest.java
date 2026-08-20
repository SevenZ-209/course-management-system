package com.lmdk.course_management_system.dto.admin.learningpath;

import java.time.LocalDateTime;

public record ReleaseCurrentAssignmentRequest(
        Integer studentLearningPathId,
        LocalDateTime availableAt,
        LocalDateTime dueAt
) {
}