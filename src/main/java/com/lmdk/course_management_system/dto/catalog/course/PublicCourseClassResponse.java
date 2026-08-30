package com.lmdk.course_management_system.dto.catalog.course;

import java.time.LocalDate;

public record PublicCourseClassResponse(
        Integer id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Long currentStudents,
        Integer maxStudents,
        String status
) {
}