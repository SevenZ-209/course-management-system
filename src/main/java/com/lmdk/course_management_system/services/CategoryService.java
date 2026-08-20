package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    Category getCategoryById(Integer id);

    Category getCategoryByName(String name);

    Category addCategory(Category category);

    void updateCategory(Category category);

    List<Category> getCategories(Map<String, String> params);

    List<Category> getAllCategories();

    long countCategories(Map<String, String> params);

    boolean existsByName(String name);
}