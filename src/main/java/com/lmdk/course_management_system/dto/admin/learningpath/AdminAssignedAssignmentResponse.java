package com.lmdk.course_management_system.dto.admin.learningpath;

import java.time.LocalDateTime;

public record AdminAssignedAssignmentResponse(
        Integer id,

        Integer studentId,
        String studentName,
        String username,

        Integer assignmentId,
        String assignmentName,

        Integer courseId,
        String courseName,

        Integer learningPathDetailId,
        Integer learningPathId,
        String learningPathName,
        Integer orderNumber,

        Integer assignedById,
        String assignedByName,

        LocalDateTime assignedAt,
        LocalDateTime availableAt,
        LocalDateTime dueAt,

        String status
) {
}