package com.lmdk.course_management_system.dto.manager.progress;

import java.util.List;

public record ManagerStudentProgressPageResponse(
        List<ManagerStudentProgressResponse> progress,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords,
        Long inProgressCount,
        Long pausedCount,
        Long completedCount,
        Long noPathCount
) {}
