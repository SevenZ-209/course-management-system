package com.lmdk.course_management_system.dto.admin.attendance;

public record CreateAttendanceRequest(
        Integer sessionId,
        Integer studentId,
        Boolean present,
        String note
) {
}