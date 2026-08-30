package com.lmdk.course_management_system.controllers.api.publicapi;

import com.lmdk.course_management_system.dto.catalog.category.PublicCategoryResponse;
import com.lmdk.course_management_system.pojo.Category;
import com.lmdk.course_management_system.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ApiPublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<PublicCategoryResponse> getCategories() {
        return categoryService.getAllCategories()
                .stream()
                .filter(c -> c.getStatus() == Category.CategoryStatus.ACTIVE)
                .map(c -> new PublicCategoryResponse(
                        c.getId(),
                        c.getName()
                ))
                .toList();
    }
}