package com.lmdk.course_management_system.dto.admin.question;

public record AdminQuestionOptionResponse(
        Integer id,
        String content,
        String type,
        Integer orderNumber
) {
}