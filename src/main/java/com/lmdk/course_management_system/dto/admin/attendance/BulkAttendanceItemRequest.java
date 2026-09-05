package com.lmdk.course_management_system.dto.admin.attendance;

public record BulkAttendanceItemRequest(
        Integer studentId,
        Boolean present,
        String note
) {
}
