package com.lmdk.course_management_system.dto.student.assignment;

import java.math.BigDecimal;

public record SubmitResponse(
        Integer attemptId,
        String status,
        BigDecimal autoScore,
        BigDecimal totalScore,
        Boolean passed
) {
}