package com.lmdk.course_management_system.dto.student.assignment;

public record SaveAnswerRequest(
        Integer questionId,
        Integer selectedAnswerId,
        String answerContent
) {
}