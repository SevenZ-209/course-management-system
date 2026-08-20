package com.lmdk.course_management_system.dto.admin.answer;

public record UpdateAnswerRequest(
        String content,
        Integer orderNumber,
        Boolean correct
) {
}