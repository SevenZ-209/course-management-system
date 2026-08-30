package com.lmdk.course_management_system.dto.teacher.grading;

import java.util.List;

public record PendingAttemptPageResponse(
        List<PendingAttemptResponse> attempts,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
