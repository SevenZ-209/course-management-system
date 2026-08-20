package com.lmdk.course_management_system.dto.admin.answer;

public record CreateAnswerRequest(
        Integer questionId,
        String content,
        Integer orderNumber,
        Boolean correct
) {
}