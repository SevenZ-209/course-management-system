package com.lmdk.course_management_system.dto.teacher.attendance;

public record UpdateTeacherAttendanceRequest(
        Boolean present,
        String note
) {
}