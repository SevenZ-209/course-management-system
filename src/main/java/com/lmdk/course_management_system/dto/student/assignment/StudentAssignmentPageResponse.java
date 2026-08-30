package com.lmdk.course_management_system.dto.student.assignment;

import java.util.List;

public record StudentAssignmentPageResponse(
        List<AssignedAssignmentResponse> assignments,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
