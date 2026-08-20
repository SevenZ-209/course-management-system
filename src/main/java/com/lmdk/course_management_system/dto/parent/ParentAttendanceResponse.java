package com.lmdk.course_management_system.dto.parent;

import java.time.LocalDateTime;

public record ParentAttendanceResponse(
        Integer classId,
        String className,
        Integer courseId,
        String courseName,

        Integer sessionId,
        String sessionTitle,
        LocalDateTime startTime,
        LocalDateTime endTime,

        String sessionStatus,
        String attendanceStatus,
        LocalDateTime attendedAt,
        String note
) {
}