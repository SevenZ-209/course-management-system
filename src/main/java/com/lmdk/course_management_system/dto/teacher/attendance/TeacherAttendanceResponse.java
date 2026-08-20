package com.lmdk.course_management_system.dto.teacher.attendance;

import java.time.LocalDateTime;

public record TeacherAttendanceResponse(
        Integer studentId,
        String studentName,
        String username,
        Integer attendanceId,
        String attendanceStatus,
        LocalDateTime attendedAt,
        String note
) {
}