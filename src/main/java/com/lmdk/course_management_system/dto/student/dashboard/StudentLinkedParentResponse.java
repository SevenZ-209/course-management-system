package com.lmdk.course_management_system.dto.student.dashboard;

import java.time.LocalDateTime;

public record StudentLinkedParentResponse(
        Integer linkId,
        Integer parentId,
        String fullName,
        String username,
        LocalDateTime linkedAt
) {
}
