package com.lmdk.course_management_system.dto.admin.category;

public record AdminCategoryResponse(
        Integer id,
        String name,
        String description,
        String status
) {
}