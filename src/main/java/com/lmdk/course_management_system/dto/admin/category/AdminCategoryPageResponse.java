package com.lmdk.course_management_system.dto.admin.category;

import java.util.List;

public record AdminCategoryPageResponse(
        List<AdminCategoryResponse> categories,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}