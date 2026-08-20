package com.lmdk.course_management_system.dto.teacher.classinfo;

import java.time.LocalDateTime;

public record TeacherStudentResponse(
        Integer enrollmentId,
        Integer studentId,
        String fullName,
        String username,
        String enrollmentStatus,
        LocalDateTime enrolledAt
) {
}