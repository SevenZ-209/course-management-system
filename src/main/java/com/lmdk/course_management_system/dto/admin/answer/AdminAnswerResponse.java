package com.lmdk.course_management_system.dto.admin.answer;

public record AdminAnswerResponse(
        Integer id,

        Integer questionId,
        String questionContent,
        String questionType,

        Integer assignmentId,
        String assignmentName,

        Integer courseId,
        String courseName,

        String content,
        Integer orderNumber,
        Boolean correct
) {
}