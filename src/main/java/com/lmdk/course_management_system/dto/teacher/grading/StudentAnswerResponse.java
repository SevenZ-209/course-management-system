package com.lmdk.course_management_system.dto.teacher.grading;

import java.math.BigDecimal;
import java.util.List;

public record StudentAnswerResponse(
        Integer studentAnswerId,
        Integer questionId,
        Integer orderNumber,
        String questionContent,
        String questionType,
        BigDecimal maximumScore,
        String answerContent,
        String selectedAnswer,
        BigDecimal score,
        String teacherComment,
        List<String> referenceAnswers
) {
} 