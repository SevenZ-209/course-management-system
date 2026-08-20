package com.lmdk.course_management_system.dto.admin.user;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Integer id,
        String username,
        String fullName,
        String email,
        String role,
        String status,
        LocalDateTime createdAt
) {
}