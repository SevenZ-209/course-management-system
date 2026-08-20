package com.lmdk.course_management_system.dto.admin.learningpath;

public record AdminAvailableAssignmentResponse(
        Integer id,
        String name,
        Integer courseId,
        String courseName
) {
}