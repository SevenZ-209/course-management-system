package com.lmdk.course_management_system.dto.admin.lesson;

public record AdminLessonOptionResponse(
        Integer id,
        String name,
        Integer moduleId,
        String moduleName
) {}
