package com.lmdk.course_management_system.dto.admin.lesson;

import java.util.List;

public record AdminLessonPageResponse(
        List<AdminLessonResponse> lessons,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}