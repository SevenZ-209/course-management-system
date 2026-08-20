package com.lmdk.course_management_system.dto.admin.learningpath;

import java.util.List;

public record AdminLearningPathPageResponse(
        List<AdminLearningPathResponse> learningPaths,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}