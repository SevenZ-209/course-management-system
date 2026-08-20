package com.lmdk.course_management_system.dto.admin.onlinesession;

import java.time.LocalDateTime;

public record OnlineSessionRequest(
        String title,
        Integer classId,
        Integer teacherId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String meetingUrl
) {
}