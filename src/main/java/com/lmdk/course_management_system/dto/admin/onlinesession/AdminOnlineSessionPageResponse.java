package com.lmdk.course_management_system.dto.admin.onlinesession;

import java.util.List;

public record AdminOnlineSessionPageResponse(
        List<AdminOnlineSessionResponse> sessions,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}