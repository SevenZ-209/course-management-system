package com.lmdk.course_management_system.dto.student.course;

public record StudentLessonResponse(
        Integer lessonId,
        String lessonName,
        Integer orderNumber,
        boolean locked,
        String status
) {
}