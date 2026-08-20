package com.lmdk.course_management_system.dto.admin.learningpath;

import java.util.List;

public record AdminLearningPathDetailPageResponse(
        List<AdminLearningPathDetailResponse> details,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}