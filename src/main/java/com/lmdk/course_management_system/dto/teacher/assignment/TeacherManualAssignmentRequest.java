package com.lmdk.course_management_system.dto.teacher.assignment;

import java.time.LocalDateTime;

public record TeacherManualAssignmentRequest(
        Integer studentId,
        Integer assignmentId,
        LocalDateTime availableAt,
        LocalDateTime dueAt
) {
}