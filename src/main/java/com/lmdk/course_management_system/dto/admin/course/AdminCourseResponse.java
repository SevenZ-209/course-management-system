package com.lmdk.course_management_system.dto.admin.course;

import java.math.BigDecimal;

public record AdminCourseResponse(
        Integer id,
        String name,
        String description,
        BigDecimal tuitionFee,
        String imageUrl,
        Integer categoryId,
        String categoryName,
        String status
) {
}