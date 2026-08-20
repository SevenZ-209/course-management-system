package com.lmdk.course_management_system.dto.parent;

import java.time.LocalDateTime;

public record CreateParentLinkRequest(
        LocalDateTime expiresAt
) {
}