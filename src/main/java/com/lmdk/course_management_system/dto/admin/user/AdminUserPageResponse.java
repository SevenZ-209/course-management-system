package com.lmdk.course_management_system.dto.admin.user;

import java.util.List;

public record AdminUserPageResponse(
        List<AdminUserResponse> users,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}