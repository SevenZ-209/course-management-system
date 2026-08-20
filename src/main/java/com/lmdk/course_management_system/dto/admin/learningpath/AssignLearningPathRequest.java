package com.lmdk.course_management_system.dto.admin.learningpath;

public record AssignLearningPathRequest(
        Integer studentId,
        Integer learningPathId
) {
}