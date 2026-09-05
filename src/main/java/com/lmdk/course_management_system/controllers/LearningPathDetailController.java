package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.LearningPathDetailService;
import com.lmdk.course_management_system.services.LearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/learning-path-details")
@RequiredArgsConstructor
public class LearningPathDetailController {

    private final LearningPathDetailService detailService;
    private final LearningPathService learningPathService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;

    @Value("${learning-path-details.page-size:10}")
    private int pageSize;

    @GetMapping
    public String details(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        Integer selectedCourseId = parseInteger(params.get("courseId"));
        normalizePathFilter(params, selectedCourseId);

        long totalRecords = detailService.countDetails(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("details", detailService.getDetails(params));
        model.addAttribute("learningPaths", selectedCourseId == null
                ? List.of()
                : learningPathService.getLearningPathsByCourse(selectedCourseId));
        model.addAttribute("assignments", List.of());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("learningPathId", params.getOrDefault("learningPathId", ""));

        return "admin/learning-path-details";
    }

    @GetMapping("/paths")
    @ResponseBody
    public List<Map<String, Object>> getPaths(@RequestParam Integer courseId) {
        if (courseId == null)
            return List.of();

        return learningPathService.getLearningPathsByCourse(courseId).stream()
                .map(path -> Map.<String, Object>of(
                        "id", path.getId(),
                        "name", path.getName()
                ))
                .toList();
    }

    @GetMapping("/assignments")
    @ResponseBody
    public List<Map<String, Object>> getAssignmentsByLearningPath(
            @RequestParam Integer learningPathId) {

        LearningPath learningPath = learningPathService.getLearningPathById(learningPathId);

        if (learningPath == null)
            return List.of();

        return assignmentService
                .getAssignmentsByCourse(learningPath.getCourse().getId())
                .stream()
                .map(assignment -> Map.<String, Object>of(
                        "id", assignment.getId(),
                        "name", assignment.getName(),
                        "maximumScore", assignment.getMaximumScore()
                ))
                .toList();
    }

    @PostMapping("/add")
    public String addDetail(@RequestParam Integer learningPathId,
                            @RequestParam Integer assignmentId,
                            @RequestParam Integer orderNumber,
                            @RequestParam BigDecimal minimumScore,
                            @RequestParam Integer maxAttempts,
                            RedirectAttributes redirectAttributes) {

        LearningPath learningPath = learningPathService.getLearningPathById(learningPathId);
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if (learningPath == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lộ trình không tồn tại!");
            return "redirect:/admin/learning-path-details";
        }

        if (assignment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bài tập không tồn tại!");
            return "redirect:/admin/learning-path-details";
        }

        try {
            LearningPathDetail detail = new LearningPathDetail();
            detail.setLearningPath(learningPath);
            detail.setAssignment(assignment);
            detail.setOrderNumber(orderNumber);
            detail.setMinimumScore(minimumScore);
            detail.setMaxAttempts(maxAttempts);

            detailService.addDetail(detail);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm bài vào lộ trình thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/learning-path-details";
    }

    @PostMapping("/update")
    public String updateDetail(@RequestParam Integer detailId,
                               @RequestParam Integer assignmentId,
                               @RequestParam Integer orderNumber,
                               @RequestParam BigDecimal minimumScore,
                               @RequestParam Integer maxAttempts,
                               RedirectAttributes redirectAttributes) {

        LearningPathDetail detail = detailService.getDetailById(detailId);

        if (detail == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết lộ trình!");
            return "redirect:/admin/learning-path-details";
        }

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if (assignment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bài tập không tồn tại!");
            return "redirect:/admin/learning-path-details";
        }

        try {
            detail.setAssignment(assignment);
            detail.setOrderNumber(orderNumber);
            detail.setMinimumScore(minimumScore);
            detail.setMaxAttempts(maxAttempts);

            detailService.updateDetail(detail);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật chi tiết lộ trình thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/learning-path-details";
    }

    private void normalizePathFilter(Map<String, String> params, Integer courseId) {
        Integer pathId = parseInteger(params.get("learningPathId"));
        if (courseId == null || pathId == null)
            return;

        LearningPath path = learningPathService.getLearningPathById(pathId);
        if (path == null || path.getCourse() == null || !courseId.equals(path.getCourse().getId()))
            params.remove("learningPathId");
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}