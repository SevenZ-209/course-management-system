package com.lmdk.course_management_system.dto.admin.parentlink;

import java.util.List;

public record AdminParentLinkPageResponse(
        List<AdminParentLinkResponse> parentLinks,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
