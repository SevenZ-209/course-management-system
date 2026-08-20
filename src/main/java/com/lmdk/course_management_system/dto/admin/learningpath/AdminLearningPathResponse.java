package com.lmdk.course_management_system.dto.admin.learningpath;

public record AdminLearningPathResponse(
        Integer id,
        String name,
        Integer courseId,
        String courseName,
        Integer assignmentsPerDay,
        String status
) {
}