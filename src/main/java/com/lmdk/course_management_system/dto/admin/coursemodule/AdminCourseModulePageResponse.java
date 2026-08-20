package com.lmdk.course_management_system.dto.admin.coursemodule;

import java.util.List;

public record AdminCourseModulePageResponse(
        List<AdminCourseModuleResponse> modules,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}