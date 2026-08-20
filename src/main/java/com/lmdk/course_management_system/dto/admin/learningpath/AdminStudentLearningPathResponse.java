package com.lmdk.course_management_system.dto.admin.learningpath;

import java.time.LocalDateTime;

public record AdminStudentLearningPathResponse(
        Integer id,

        Integer studentId,
        String studentName,
        String username,

        Integer learningPathId,
        String learningPathName,

        Integer courseId,
        String courseName,

        Integer currentDetailId,
        Integer currentOrderNumber,

        Integer currentAssignmentId,
        String currentAssignmentName,

        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}