package com.lmdk.course_management_system.dto.teacher.classinfo;

import java.util.List;

public record TeacherStudentProgressPageResponse(
        List<TeacherStudentProgressResponse> progress,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
