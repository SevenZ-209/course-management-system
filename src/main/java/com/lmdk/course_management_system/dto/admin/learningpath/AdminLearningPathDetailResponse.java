package com.lmdk.course_management_system.dto.admin.learningpath;

import java.math.BigDecimal;

public record AdminLearningPathDetailResponse(
        Integer id,

        Integer learningPathId,
        String learningPathName,

        Integer courseId,
        String courseName,

        Integer assignmentId,
        String assignmentName,
        BigDecimal maximumScore,

        Integer orderNumber,
        BigDecimal minimumScore,
        Integer maxAttempts
) {
}