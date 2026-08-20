package com.lmdk.course_management_system.dto.admin.question;

import java.math.BigDecimal;

public record CreateQuestionRequest(
        Integer assignmentId,
        String content,
        String type,
        BigDecimal score,
        Integer orderNumber
) {
}