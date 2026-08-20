package com.lmdk.course_management_system.dto.teacher.grading;

import java.math.BigDecimal;
import java.util.List;

public record GradingDetailResponse(
        Integer attemptId,
        Integer attemptNumber,
        Integer studentId,
        String studentName,
        Integer assignmentId,
        String assignmentName,
        BigDecimal maximumScore,
        BigDecimal autoScore,
        List<StudentAnswerResponse> answers
) {
}