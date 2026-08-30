package com.lmdk.course_management_system.dto.catalog.course;

import java.util.List;

public record PublicCoursePageResponse(
        List<PublicCourseResponse> courses,
        Integer page,
        Integer totalPages,
        Long totalRecords
) {
}