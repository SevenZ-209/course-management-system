package com.lmdk.course_management_system.dto.admin.question;

import java.util.List;

public record AdminQuestionPageResponse(
        List<AdminQuestionResponse> questions,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}