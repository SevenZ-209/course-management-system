package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.services.CourseModuleService;
import com.lmdk.course_management_system.services.CourseService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/course-modules")
@RequiredArgsConstructor
public class CourseModuleController {

    private final CourseModuleService moduleService;
    private final CourseService courseService;

    @Value("${modules.page-size:10}")
    private int pageSize;

    @GetMapping
    public String modules(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = moduleService.countModules(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("modules", moduleService.getModules(params));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/course-modules";
    }

    @PostMapping("/add")
    public String addModule(@RequestParam String name,
                            @RequestParam Integer courseId,
                            @RequestParam Integer orderNumber,
                            RedirectAttributes redirectAttributes) {
        Course course = courseService.getCourseById(courseId);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khóa học không tồn tại!");
            return "redirect:/admin/course-modules";
        }

        try {
            CourseModule module = new CourseModule();
            module.setName(name);
            module.setCourse(course);
            module.setOrderNumber(orderNumber);

            moduleService.addModule(module);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm module thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/course-modules";
    }

    @PostMapping("/update")
    public String updateModule(@RequestParam Integer moduleId,
                               @RequestParam String name,
                               @RequestParam Integer courseId,
                               @RequestParam Integer orderNumber,
                               RedirectAttributes redirectAttributes) {
        CourseModule module = moduleService.getModuleById(moduleId);
        Course course = courseService.getCourseById(courseId);

        if (module == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy module!");
            return "redirect:/admin/course-modules";
        }

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khóa học không tồn tại!");
            return "redirect:/admin/course-modules";
        }

        try {
            module.setName(name);
            module.setCourse(course);
            module.setOrderNumber(orderNumber);

            moduleService.updateModule(module);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật module thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/course-modules";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer moduleId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        CourseModule module = moduleService.getModuleById(moduleId);

        if (module == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy module!");
            return "redirect:/admin/course-modules";
        }

        try {
            module.setStatus(CourseModule.ModuleStatus.valueOf(status));
            moduleService.updateModule(module);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ!");
        }

        return "redirect:/admin/course-modules";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}