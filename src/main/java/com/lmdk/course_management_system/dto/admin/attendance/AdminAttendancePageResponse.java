package com.lmdk.course_management_system.dto.admin.attendance;

import java.util.List;

public record AdminAttendancePageResponse(
        List<AdminAttendanceResponse> attendances,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}