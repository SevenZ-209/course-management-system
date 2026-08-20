package com.lmdk.course_management_system.dto.admin.enrollment;

import java.math.BigDecimal;

public record AdminPendingEnrollmentOptionResponse(
        Integer enrollmentId,
        Integer studentId,
        String studentName,
        Integer classId,
        String className,
        Integer courseId,
        String courseName,
        BigDecimal tuitionFee
) {
}