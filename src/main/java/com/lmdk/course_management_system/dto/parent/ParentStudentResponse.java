package com.lmdk.course_management_system.dto.parent;

import java.time.LocalDateTime;

public record ParentStudentResponse(
        Integer linkId,
        Integer studentId,
        String fullName,
        String username,
        LocalDateTime linkedAt
) {
}