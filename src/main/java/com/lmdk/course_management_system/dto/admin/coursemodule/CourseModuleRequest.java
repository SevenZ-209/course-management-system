package com.lmdk.course_management_system.dto.admin.coursemodule;

public record CourseModuleRequest(
        String name,
        Integer courseId,
        Integer orderNumber
) {
}