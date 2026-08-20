package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Category;
import com.lmdk.course_management_system.services.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Value("${categories.page-size:10}")
    private int pageSize;

    @GetMapping
    public String categories(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = categoryService.countCategories(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("categories", categoryService.getCategories(params));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/categories";
    }

    @PostMapping("/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              RedirectAttributes redirectAttributes) {
        try {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            categoryService.addCategory(category);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @PostMapping("/update")
    public String updateCategory(@RequestParam Integer categoryId,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        Category category = categoryService.getCategoryById(categoryId);

        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục!");
            return "redirect:/admin/categories";
        }

        name = name.trim();

        if (name.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên danh mục không được để trống!");
            return "redirect:/admin/categories";
        }

        Category existing = categoryService.getCategoryByName(name);

        if (existing != null && !existing.getId().equals(categoryId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên danh mục đã tồn tại!");
            return "redirect:/admin/categories";
        }

        category.setName(name);
        category.setDescription(description);
        categoryService.updateCategory(category);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");

        return "redirect:/admin/categories";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer categoryId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Category category = categoryService.getCategoryById(categoryId);

        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục!");
            return "redirect:/admin/categories";
        }

        try {
            category.setStatus(Category.CategoryStatus.valueOf(status));
            categoryService.updateCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ!");
        }

        return "redirect:/admin/categories";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}