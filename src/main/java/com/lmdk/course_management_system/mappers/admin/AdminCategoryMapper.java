package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.category.AdminCategoryResponse;
import com.lmdk.course_management_system.pojo.Category;

import org.springframework.stereotype.Component;

@Component
public class AdminCategoryMapper {

    public AdminCategoryResponse toResponse(Category category) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getStatus().name()
        );
    }
}