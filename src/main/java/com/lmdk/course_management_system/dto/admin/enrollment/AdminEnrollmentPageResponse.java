package com.lmdk.course_management_system.dto.admin.enrollment;

import java.util.List;

public record AdminEnrollmentPageResponse(
        List<AdminEnrollmentResponse> enrollments,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}