package com.lmdk.course_management_system.dto.teacher.classinfo;

import java.util.List;

public record TeacherStudentPageResponse(
        List<TeacherStudentResponse> students,
        Integer currentPage,
        Integer totalPages,
        Long totalRecords
) {
}
