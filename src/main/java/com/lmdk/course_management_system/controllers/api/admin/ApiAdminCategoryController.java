package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.category.*;
import com.lmdk.course_management_system.mappers.admin.AdminCategoryMapper;
import com.lmdk.course_management_system.pojo.Category;
import com.lmdk.course_management_system.services.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class ApiAdminCategoryController {

    private final CategoryService categoryService;
    private final AdminCategoryMapper adminCategoryMapper;

    @Value("${categories.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminCategoryPageResponse getCategories(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords =
                categoryService.countCategories(params);

        int totalPages = Math.max(
                (int) Math.ceil(
                        (double) totalRecords / pageSize
                ),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put(
                    "page",
                    String.valueOf(page)
            );
        }

        return new AdminCategoryPageResponse(
                categoryService
                        .getCategories(params)
                        .stream()
                        .map(adminCategoryMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @PostMapping
    public AdminCategoryActionResponse addCategory(
            @RequestBody CategoryRequest request
    ) {
        if(request.name() == null
                || request.name().isBlank())
            throw new IllegalArgumentException(
                    "Tên danh mục không được để trống!"
            );

        Category category = new Category();
        category.setName(
                request.name().trim()
        );
        category.setDescription(
                request.description()
        );

        categoryService.addCategory(category);

        return new AdminCategoryActionResponse(
                category.getId(),
                "Thêm danh mục thành công!"
        );
    }

    @PutMapping("/{categoryId}")
    public AdminCategoryActionResponse updateCategory(
            @PathVariable Integer categoryId,
            @RequestBody CategoryRequest request
    ) {
        Category category =
                requireCategory(categoryId);

        if(request.name() == null
                || request.name().isBlank())
            throw new IllegalArgumentException(
                    "Tên danh mục không được để trống!"
            );

        String name =
                request.name().trim();

        Category existing =
                categoryService
                        .getCategoryByName(name);

        if(existing != null
                && !existing.getId()
                .equals(categoryId))
            throw new IllegalArgumentException(
                    "Tên danh mục đã tồn tại!"
            );

        category.setName(name);
        category.setDescription(
                request.description()
        );

        categoryService
                .updateCategory(category);

        return new AdminCategoryActionResponse(
                categoryId,
                "Cập nhật danh mục thành công!"
        );
    }

    @GetMapping("/options")
    public List<AdminCategoryResponse> getCategoryOptions() {
        return categoryService
                .getAllCategories()
                .stream()
                .map(adminCategoryMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{categoryId}/status")
    public AdminCategoryActionResponse updateStatus(
            @PathVariable Integer categoryId,
            @RequestBody UpdateCategoryStatusRequest request
    ) {
        Category category =
                requireCategory(categoryId);

        if(request.status() == null
                || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái không được để trống!"
            );

        try {
            category.setStatus(
                    Category.CategoryStatus.valueOf(
                            request.status()
                                    .trim()
                                    .toUpperCase()
                    )
            );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ!"
            );
        }

        categoryService
                .updateCategory(category);

        return new AdminCategoryActionResponse(
                categoryId,
                "Cập nhật trạng thái thành công!"
        );
    }

    private Category requireCategory(
            Integer categoryId
    ) {
        Category category =
                categoryService
                        .getCategoryById(categoryId);

        if(category == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy danh mục!"
            );

        return category;
    }
}