package com.lmdk.course_management_system.dto.teacher.classinfo;

public record TeacherStudentProgressResponse(
        Integer studentId,
        String studentName,
        String username,

        Integer studentLearningPathId,
        Integer learningPathId,
        String learningPathName,
        String learningPathStatus,

        Integer currentDetailId,
        Integer currentAssignmentId,
        String currentAssignmentName,

        Integer completedDetails,
        Integer totalDetails
) {
}