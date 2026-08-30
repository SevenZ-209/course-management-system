package com.lmdk.course_management_system.dto.parent;

import java.util.List;

public record ParentAttendancePageResponse(
        List<ParentAttendanceResponse> attendance,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
