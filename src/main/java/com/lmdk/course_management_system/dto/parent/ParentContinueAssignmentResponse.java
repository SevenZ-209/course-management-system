package com.lmdk.course_management_system.dto.parent;

public record ParentContinueAssignmentResponse(
        Integer assignedAssignmentId,
        Integer assignmentId,
        String assignmentName,

        Integer courseId,
        String courseName,

        Integer attemptId,
        Integer attemptNumber,
        String attemptStatus
) {
}