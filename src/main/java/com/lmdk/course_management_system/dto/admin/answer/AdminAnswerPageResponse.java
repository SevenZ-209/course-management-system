package com.lmdk.course_management_system.dto.admin.answer;

import java.util.List;

public record AdminAnswerPageResponse(
        List<AdminAnswerResponse> answers,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}