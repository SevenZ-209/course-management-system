package com.lmdk.course_management_system.dto.admin.attendance;

import java.time.LocalDateTime;

public record AdminAttendanceRosterResponse(
        Integer studentId,
        String studentName,
        String username,
        Integer attendanceId,
        String attendanceStatus,
        LocalDateTime attendedAt,
        String note
) {
}
