package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Category;
import com.lmdk.course_management_system.repository.CategoryRepository;
import com.lmdk.course_management_system.services.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category getCategoryById(Integer id) {
        return categoryRepository.getCategoryById(id);
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.getCategoryByName(name);
    }

    @Override
    public Category addCategory(Category category) {
        String name = category.getName().trim();

        if (name.isBlank())
            throw new IllegalArgumentException("Tên danh mục không được để trống!");

        if (categoryRepository.existsByName(name))
            throw new IllegalArgumentException("Tên danh mục đã tồn tại!");

        category.setName(name);

        if (category.getStatus() == null)
            category.setStatus(Category.CategoryStatus.ACTIVE);

        return categoryRepository.addCategory(category);
    }

    @Override
    public void updateCategory(Category category) {
        categoryRepository.updateCategory(category);
    }

    @Override
    public List<Category> getCategories(Map<String, String> params) {
        return categoryRepository.getCategories(params);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    @Override
    public long countCategories(Map<String, String> params) {
        return categoryRepository.countCategories(params);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}