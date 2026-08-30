package com.lmdk.course_management_system.dto.catalog.course;

import java.math.BigDecimal;

public record PublicCourseResponse(
        Integer id,
        String name,
        String description,
        BigDecimal tuitionFee,
        String imageUrl,
        Integer categoryId,
        String categoryName
) {
}