package com.lmdk.course_management_system.dto.admin.enrollment;

public record CreateAdminEnrollmentRequest(
        Integer studentId,
        Integer classId
) {
}