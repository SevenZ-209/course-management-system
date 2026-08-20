package com.lmdk.course_management_system.dto.admin.courseclass;

import java.time.LocalDate;

public record AdminCourseClassResponse(
        Integer id,
        String name,

        Integer courseId,
        String courseName,

        Integer teacherId,
        String teacherName,

        LocalDate startDate,
        LocalDate endDate,
        Integer maxStudents,

        String status
) {
}