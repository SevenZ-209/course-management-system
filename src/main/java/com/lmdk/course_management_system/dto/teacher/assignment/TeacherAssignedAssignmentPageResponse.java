package com.lmdk.course_management_system.dto.teacher.assignment;

import java.util.List;

public record TeacherAssignedAssignmentPageResponse(
        List<TeacherAssignedAssignmentResponse> assignments,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
