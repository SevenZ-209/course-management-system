package com.lmdk.course_management_system.dto.student.assignment;

public record StudentAnswerResponse(
        Integer studentAnswerId,
        Integer questionId,
        Integer selectedAnswerId,
        String answerContent
) {
}