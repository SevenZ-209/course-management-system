package com.lmdk.course_management_system.dto.admin.lesson;

public record AdminLessonResponse(
        Integer id,
        String name,
        Integer courseId,
        String courseName,
        Integer moduleId,
        String moduleName,
        Integer orderNumber,
        String fileName,
        String fileUrl
) {
}