package com.lmdk.course_management_system.dto.teacher.grading;

import java.time.LocalDateTime;

public record PendingAttemptResponse(
        Integer attemptId,
        Integer attemptNumber,
        Integer studentId,
        String studentName,
        Integer assignmentId,
        String assignmentName,
        String courseName,
        LocalDateTime submittedAt
) {
}