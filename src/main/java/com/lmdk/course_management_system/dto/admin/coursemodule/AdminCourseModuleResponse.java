package com.lmdk.course_management_system.dto.admin.coursemodule;

public record AdminCourseModuleResponse(
        Integer id,
        String name,
        Integer courseId,
        String courseName,
        Integer orderNumber,
        String status
) {
}