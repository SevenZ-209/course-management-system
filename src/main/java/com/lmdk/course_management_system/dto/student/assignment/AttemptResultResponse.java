package com.lmdk.course_management_system.dto.student.assignment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AttemptResultResponse(
        Integer attemptId,
        Integer attemptNumber,
        String status,
        LocalDateTime submittedAt,
        Integer durationSeconds,
        BigDecimal autoScore,
        BigDecimal essayScore,
        BigDecimal totalScore,
        Boolean passed,
        String comment
) {
}