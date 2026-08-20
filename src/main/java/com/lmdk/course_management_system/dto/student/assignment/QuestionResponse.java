package com.lmdk.course_management_system.dto.student.assignment;

import java.math.BigDecimal;
import java.util.List;

public record QuestionResponse(
        Integer id,
        Integer orderNumber,
        String content,
        String type,
        BigDecimal score,
        List<AnswerOptionResponse> options,
        Integer selectedAnswerId,
        String answerContent
) {
}