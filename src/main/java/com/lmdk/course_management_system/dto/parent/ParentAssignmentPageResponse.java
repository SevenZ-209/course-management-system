package com.lmdk.course_management_system.dto.parent;

import java.util.List;

public record ParentAssignmentPageResponse(
        List<ParentAssignmentResponse> assignments,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
