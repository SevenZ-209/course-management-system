package com.lmdk.course_management_system.dto.teacher.grading;

import java.math.BigDecimal;

public record FinalizeResponse(
        Integer gradingResultId,
        Integer attemptId,
        BigDecimal autoScore,
        BigDecimal essayScore,
        BigDecimal totalScore,
        boolean passed,
        String status
) {
}