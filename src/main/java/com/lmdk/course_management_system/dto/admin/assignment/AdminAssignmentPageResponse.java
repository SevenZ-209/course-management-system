package com.lmdk.course_management_system.dto.admin.assignment;

import java.util.List;

public record AdminAssignmentPageResponse(
        List<AdminAssignmentResponse> assignments,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}