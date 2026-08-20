package com.lmdk.course_management_system.dto.admin.attendance;

public record UpdateAttendanceRequest(
        Boolean present,
        String note
) {
}