package com.lmdk.course_management_system.dto.teacher.assignment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TeacherAssignedAssignmentResponse(
        Integer assignedAssignmentId,

        Integer assignmentId,
        String assignmentName,
        String assignmentType,
        BigDecimal maximumScore,

        Integer studentId,
        String studentName,
        String username,

        String status,

        LocalDateTime assignedAt,
        LocalDateTime availableAt,
        LocalDateTime dueAt,

        Integer learningPathDetailId,
        String assignmentSource,

        Integer assignedById,
        String assignedByName,

        Integer latestAttemptId,
        Integer latestAttemptNumber,
        String latestAttemptStatus
) {
}