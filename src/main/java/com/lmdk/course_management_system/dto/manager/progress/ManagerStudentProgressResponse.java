package com.lmdk.course_management_system.dto.manager.progress;

public record ManagerStudentProgressResponse(
        Integer enrollmentId,
        Integer studentId,
        String studentName,
        String username,
        Integer classId,
        String className,
        Integer courseId,
        String courseName,
        Integer teacherId,
        String teacherName,
        Integer studentLearningPathId,
        Integer learningPathId,
        String learningPathName,
        String learningPathStatus,
        Integer currentDetailId,
        Integer currentAssignmentId,
        String currentAssignmentName,
        Integer completedDetails,
        Integer totalDetails
) {}
