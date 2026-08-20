package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.LearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;
    private final CourseService courseService;

    @Value("${learning-paths.page-size:10}")
    private int pageSize;

    @GetMapping
    public String learningPaths(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = learningPathService.countLearningPaths(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("learningPaths", learningPathService.getLearningPaths(params));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/learning-paths";
    }

    @PostMapping("/add")
    public String addLearningPath(@RequestParam String name,
                                  @RequestParam Integer courseId,
                                  @RequestParam Integer assignmentsPerDay,
                                  RedirectAttributes redirectAttributes) {
        Course course = courseService.getCourseById(courseId);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khóa học không tồn tại!");
            return "redirect:/admin/learning-paths";
        }

        try {
            LearningPath learningPath = new LearningPath();
            learningPath.setName(name);
            learningPath.setCourse(course);
            learningPath.setAssignmentsPerDay(assignmentsPerDay);
            learningPath.setStatus(LearningPath.LearningPathStatus.ACTIVE);

            learningPathService.addLearningPath(learningPath);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm lộ trình thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/learning-paths";
    }

    @PostMapping("/update")
    public String updateLearningPath(@RequestParam Integer learningPathId,
                                     @RequestParam String name,
                                     @RequestParam Integer assignmentsPerDay,
                                     RedirectAttributes redirectAttributes) {
        LearningPath learningPath = learningPathService.getLearningPathById(learningPathId);

        if (learningPath == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lộ trình!");
            return "redirect:/admin/learning-paths";
        }

        try {
            learningPath.setName(name);
            learningPath.setAssignmentsPerDay(assignmentsPerDay);

            learningPathService.updateLearningPath(learningPath);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lộ trình thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/learning-paths";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer learningPathId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        LearningPath learningPath = learningPathService.getLearningPathById(learningPathId);

        if (learningPath == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lộ trình!");
            return "redirect:/admin/learning-paths";
        }

        try {
            learningPath.setStatus(LearningPath.LearningPathStatus.valueOf(status));
            learningPathService.updateLearningPath(learningPath);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/learning-paths";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}