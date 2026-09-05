package com.lmdk.course_management_system.dto.admin.parentlink;

import java.time.LocalDateTime;

public record AdminParentLinkResponse(
        Integer id,
        String verificationCode,
        Integer studentId,
        String studentName,
        String studentUsername,
        Integer parentId,
        String parentName,
        String parentUsername,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        String status
) {
}
