package com.lmdk.course_management_system.dto.admin.courseclass;

import java.util.List;

public record AdminCourseClassPageResponse(
        List<AdminCourseClassResponse> classes,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}