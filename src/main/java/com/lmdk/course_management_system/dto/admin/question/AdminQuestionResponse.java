package com.lmdk.course_management_system.dto.admin.question;

import java.math.BigDecimal;

public record AdminQuestionResponse(
        Integer id,

        Integer assignmentId,
        String assignmentName,

        Integer courseId,
        String courseName,

        String content,
        String type,
        BigDecimal score,
        Integer orderNumber
) {
}