package com.lmdk.course_management_system.dto.admin.learningpath;

import java.util.List;

public record AdminAssignedAssignmentPageResponse(
        List<AdminAssignedAssignmentResponse> assignedAssignments,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}