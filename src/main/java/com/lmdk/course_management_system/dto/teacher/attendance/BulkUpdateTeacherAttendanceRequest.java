package com.lmdk.course_management_system.dto.teacher.attendance;

import java.util.List;

public record BulkUpdateTeacherAttendanceRequest(
        List<UpdateTeacherAttendanceItemRequest> attendances
) {
}
