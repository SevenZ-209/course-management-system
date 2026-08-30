package com.lmdk.course_management_system.dto.teacher.attendance;

public record UpdateTeacherAttendanceItemRequest(
        Integer studentId,
        Boolean present,
        String note
) {
}
