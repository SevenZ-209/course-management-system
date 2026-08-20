package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Category;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.services.CategoryService;
import com.lmdk.course_management_system.services.CourseService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CategoryService categoryService;

    @Value("${courses.page-size:10}")
    private int pageSize;

    @GetMapping
    public String courses(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = courseService.countCourses(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("courses", courseService.getCourses(params));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("categoryId", params.getOrDefault("categoryId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/courses";
    }

    @PostMapping("/add")
    public String addCourse(@RequestParam String name,
                            @RequestParam(required = false) String description,
                            @RequestParam BigDecimal tuitionFee,
                            @RequestParam Integer categoryId,
                            RedirectAttributes redirectAttributes) {
        Category category = categoryService.getCategoryById(categoryId);

        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Danh mục không tồn tại!");
            return "redirect:/admin/courses";
        }

        try {
            Course course = new Course();
            course.setName(name);
            course.setDescription(description);
            course.setTuitionFee(tuitionFee);
            course.setCategory(category);

            courseService.addCourse(course);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm khóa học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/courses";
    }

    @PostMapping("/update")
    public String updateCourse(@RequestParam Integer courseId,
                               @RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam BigDecimal tuitionFee,
                               @RequestParam Integer categoryId,
                               RedirectAttributes redirectAttributes) {
        Course course = courseService.getCourseById(courseId);
        Category category = categoryService.getCategoryById(categoryId);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học!");
            return "redirect:/admin/courses";
        }

        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Danh mục không tồn tại!");
            return "redirect:/admin/courses";
        }

        try {
            course.setName(name);
            course.setDescription(description);
            course.setTuitionFee(tuitionFee);
            course.setCategory(category);

            courseService.updateCourse(course);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khóa học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/courses";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer courseId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Course course = courseService.getCourseById(courseId);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học!");
            return "redirect:/admin/courses";
        }

        try {
            course.setStatus(Course.CourseStatus.valueOf(status));
            courseService.updateCourse(course);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ!");
        }

        return "redirect:/admin/courses";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}