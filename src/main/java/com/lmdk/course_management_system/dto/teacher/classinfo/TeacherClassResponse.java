package com.lmdk.course_management_system.dto.teacher.classinfo;

import java.time.LocalDate;

public record TeacherClassResponse(
        Integer classId,
        String className,
        Integer courseId,
        String courseName,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxStudents,
        Integer studentCount,
        String status
) {
}