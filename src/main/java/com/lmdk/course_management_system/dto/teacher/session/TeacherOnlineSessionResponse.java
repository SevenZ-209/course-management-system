package com.lmdk.course_management_system.dto.teacher.session;

import java.time.LocalDateTime;

public record TeacherOnlineSessionResponse(
        Integer sessionId,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String meetingUrl,
        String status
) {
}