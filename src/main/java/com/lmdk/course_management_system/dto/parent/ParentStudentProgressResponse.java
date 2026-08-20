package com.lmdk.course_management_system.dto.parent;

public record ParentStudentProgressResponse(
        Integer courseId,
        String courseName,
        Integer classId,
        String className,

        Integer learningPathId,
        String learningPathName,
        String learningPathStatus,

        Integer currentAssignmentId,
        String currentAssignmentName,

        Integer completedDetails,
        Integer totalDetails
) {
}