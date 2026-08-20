package com.lmdk.course_management_system.dto.teacher.grading;

import java.math.BigDecimal;

public record GradeEssayRequest(
        BigDecimal score,
        String teacherComment
) {
}