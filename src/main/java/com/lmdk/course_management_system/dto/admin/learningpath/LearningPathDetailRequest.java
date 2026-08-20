package com.lmdk.course_management_system.dto.admin.learningpath;

import java.math.BigDecimal;

public record LearningPathDetailRequest(
        Integer learningPathId,
        Integer assignmentId,
        Integer orderNumber,
        BigDecimal minimumScore,
        Integer maxAttempts
) {
}