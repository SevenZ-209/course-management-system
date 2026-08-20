package com.lmdk.course_management_system.dto.admin.learningpath;

public record CreateLearningPathRequest(
        String name,
        Integer courseId,
        Integer assignmentsPerDay
) {
}