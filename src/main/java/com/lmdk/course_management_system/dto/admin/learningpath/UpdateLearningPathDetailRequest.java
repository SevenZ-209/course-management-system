package com.lmdk.course_management_system.dto.admin.learningpath;

import java.math.BigDecimal;

public record UpdateLearningPathDetailRequest(
        Integer assignmentId,
        Integer orderNumber,
        BigDecimal minimumScore,
        Integer maxAttempts
) {
}