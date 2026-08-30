package com.lmdk.course_management_system.dto.student.assignment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AttemptDetailResponse(
        Integer attemptId,
        Integer attemptNumber,
        Integer assignmentId,
        String assignmentName,
        Integer courseId,
        String courseName,
        Integer enrollmentId,
        BigDecimal maximumScore,
        LocalDateTime startedAt,
        LocalDateTime endTime,
        Long remainingSeconds,
        List<QuestionResponse> questions
) {
}