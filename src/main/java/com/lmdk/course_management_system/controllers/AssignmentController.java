package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.CourseModuleService;
import com.lmdk.course_management_system.services.LessonService;

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
@RequestMapping("/admin/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final CourseModuleService moduleService;
    private final LessonService lessonService;

    @Value("${assignments.page-size:10}")
    private int pageSize;

    @GetMapping
    public String assignments(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = assignmentService.countAssignments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("assignments", assignmentService.getAssignments(params));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("type", params.getOrDefault("type", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/assignments";
    }

    @GetMapping("/lessons")
    @ResponseBody
    public List<Map<String, Object>> getLessons(@RequestParam Integer courseId) {
        if (courseId == null)
            return List.of();

        return moduleService.getModulesByCourse(courseId).stream()
                .flatMap(module -> lessonService.getLessonsByModule(module.getId()).stream()
                        .map(lesson -> Map.<String, Object>of(
                                "id", lesson.getId(),
                                "name", module.getName() + " - " + lesson.getName()
                        )))
                .toList();
    }

    @PostMapping("/add")
    public String addAssignment(@RequestParam String name,
                                @RequestParam Integer courseId,
                                @RequestParam Integer lessonId,
                                @RequestParam String type,
                                @RequestParam BigDecimal maximumScore,
                                @RequestParam(required = false) Integer durationMinutes,
                                RedirectAttributes redirectAttributes) {
        Course course = courseService.getCourseById(courseId);
        Lesson lesson = lessonService.getLessonById(lessonId);

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khóa học không tồn tại!");
            return "redirect:/admin/assignments";
        }
        if (lesson == null || lesson.getCourseModule() == null || lesson.getCourseModule().getCourse() == null
                || !courseId.equals(lesson.getCourseModule().getCourse().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bài học không thuộc khóa học đã chọn!");
            return "redirect:/admin/assignments";
        }

        try {
            Assignment assignment = new Assignment();
            assignment.setName(name);
            assignment.setCourse(course);
            assignment.setLesson(lesson);
            assignment.setType(Assignment.AssignmentType.valueOf(type));
            assignment.setMaximumScore(maximumScore);
            assignment.setDurationMinutes(durationMinutes);
            assignment.setStatus(Assignment.AssignmentStatus.ACTIVE);

            assignmentService.addAssignment(assignment);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bài tập thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/assignments";
    }

    @PostMapping("/update")
    public String updateAssignment(@RequestParam Integer assignmentId,
                                   @RequestParam String name,
                                   @RequestParam String type,
                                   @RequestParam BigDecimal maximumScore,
                                   @RequestParam(required = false) Integer durationMinutes,
                                   RedirectAttributes redirectAttributes) {
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if (assignment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bài tập!");
            return "redirect:/admin/assignments";
        }

        try {
            assignment.setName(name);
            assignment.setType(Assignment.AssignmentType.valueOf(type));
            assignment.setMaximumScore(maximumScore);
            assignment.setDurationMinutes(durationMinutes);

            assignmentService.updateAssignment(assignment);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bài tập thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/assignments";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer assignmentId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if (assignment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bài tập!");
            return "redirect:/admin/assignments";
        }

        try {
            assignment.setStatus(Assignment.AssignmentStatus.valueOf(status));
            assignmentService.updateAssignment(assignment);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/assignments";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}