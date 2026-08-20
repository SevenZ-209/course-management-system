package com.lmdk.course_management_system.dto.admin.courseclass;

import java.time.LocalDate;

public record CourseClassRequest(
        String name,
        Integer courseId,
        Integer teacherId,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxStudents
) {
}