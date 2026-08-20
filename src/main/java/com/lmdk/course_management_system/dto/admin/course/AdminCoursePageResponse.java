package com.lmdk.course_management_system.dto.admin.course;

import java.util.List;

public record AdminCoursePageResponse(
        List<AdminCourseResponse> courses,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}