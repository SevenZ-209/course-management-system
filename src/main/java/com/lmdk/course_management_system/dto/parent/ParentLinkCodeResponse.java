package com.lmdk.course_management_system.dto.parent;

import java.time.LocalDateTime;

public record ParentLinkCodeResponse(
        Integer linkId,
        String verificationCode,
        LocalDateTime expiresAt,
        String status
) {
}