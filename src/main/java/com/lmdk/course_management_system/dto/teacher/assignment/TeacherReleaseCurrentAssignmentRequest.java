package com.lmdk.course_management_system.dto.teacher.assignment;

import java.time.LocalDateTime;

public record TeacherReleaseCurrentAssignmentRequest(
        LocalDateTime availableAt,
        LocalDateTime dueAt
) {
}