package com.lmdk.course_management_system.dto.admin.course;

import java.math.BigDecimal;

public record CourseRequest(
        String name,
        String description,
        BigDecimal tuitionFee,
        Integer categoryId
) {
}