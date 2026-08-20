package com.lmdk.course_management_system.dto.admin.user;

public record AdminStudentOptionResponse(
        Integer id,
        String username,
        String fullName,
        String status
) {
}