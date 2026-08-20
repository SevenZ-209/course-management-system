package com.lmdk.course_management_system.dto.admin.learningpath;

public record UpdateLearningPathRequest(
        String name,
        Integer assignmentsPerDay
) {
}