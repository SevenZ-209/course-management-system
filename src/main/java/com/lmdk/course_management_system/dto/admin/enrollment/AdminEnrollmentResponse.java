package com.lmdk.course_management_system.dto.admin.enrollment;

import java.time.LocalDateTime;

public record AdminEnrollmentResponse(
        Integer id,
        Integer studentId,
        String studentName,
        String username,
        Integer classId,
        String className,
        Integer courseId,
        String courseName,
        String status,
        LocalDateTime createdAt
) {
}