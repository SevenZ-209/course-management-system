package com.lmdk.course_management_system.dto.admin.attendance;

import java.util.List;

public record BulkAttendanceSaveRequest(
        Integer sessionId,
        List<BulkAttendanceItemRequest> attendances
) {
}
