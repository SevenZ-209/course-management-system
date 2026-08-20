package com.lmdk.course_management_system.dto.admin.attendance;

import java.time.LocalDateTime;

public record AdminAttendanceResponse(
        Integer id,

        Integer sessionId,
        String sessionTitle,

        Integer classId,
        String className,

        Integer courseId,
        String courseName,

        Integer studentId,
        String studentName,
        String username,

        Boolean present,
        LocalDateTime attendedAt,
        String note
) {
}