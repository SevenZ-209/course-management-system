package com.lmdk.course_management_system.dto.admin.parentlink;

import java.time.LocalDateTime;

public record CreateParentLinkRequest(
        Integer studentId,
        LocalDateTime expiresAt
) {
}
