package com.lmdk.course_management_system.dto.student.schedule;

import java.util.List;

public record StudentSchedulePageResponse(
        List<StudentScheduleResponse> sessions,
        int currentPage,
        int totalPages,
        long totalRecords
) {
}
