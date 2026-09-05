package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.LearningPathService;
import com.lmdk.course_management_system.services.StudentLearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/student-learning-paths")
@RequiredArgsConstructor
public class StudentLearningPathController {

    private final StudentLearningPathService studentLearningPathService;
    private final LearningPathService learningPathService;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    @Value("${student-learning-paths.page-size:10}")
    private int pageSize;

    @GetMapping
    public String studentLearningPaths(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = studentLearningPathService.countStudentLearningPaths(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute(
                "studentLearningPaths",
                studentLearningPathService.getStudentLearningPaths(params)
        );
        Integer selectedCourseId = parseInteger(params.get("courseId"));
        model.addAttribute("learningPaths", selectedCourseId == null
                ? List.of()
                : learningPathService.getLearningPathsByCourse(selectedCourseId));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("learningPathId", params.getOrDefault("learningPathId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/student-learning-paths";
    }

    @GetMapping("/available-paths")
    @ResponseBody
    public List<Map<String, Object>> getAvailablePaths(@RequestParam Integer studentId) {
        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollmentsByStudent(studentId);

        Map<Integer, Map<String, Object>> result = new LinkedHashMap<>();

        for (Enrollment enrollment : enrollments) {
            Integer courseId = enrollment.getCourseClass().getCourse().getId();

            for (LearningPath path : learningPathService.getLearningPathsByCourse(courseId)) {
                if (studentLearningPathService.getStudentLearningPath(studentId, path.getId()) != null)
                    continue;

                result.putIfAbsent(
                        path.getId(),
                        Map.<String, Object>of(
                                "id", path.getId(),
                                "name", path.getName(),
                                "courseName", path.getCourse().getName()
                        )
                );
            }
        }

        return List.copyOf(result.values());
    }

    @PostMapping("/assign")
    public String assignLearningPath(@RequestParam Integer studentId,
                                     @RequestParam Integer learningPathId,
                                     RedirectAttributes redirectAttributes) {
        try {
            studentLearningPathService.assignLearningPath(studentId, learningPathId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Gán lộ trình cho học viên thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/student-learning-paths";
    }

    @PostMapping("/pause")
    public String pauseLearningPath(@RequestParam Integer id,
                                    RedirectAttributes redirectAttributes) {
        try {
            studentLearningPathService.pauseLearningPath(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã tạm dừng lộ trình!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/student-learning-paths";
    }

    @PostMapping("/resume")
    public String resumeLearningPath(@RequestParam Integer id,
                                     RedirectAttributes redirectAttributes) {
        try {
            studentLearningPathService.resumeLearningPath(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã tiếp tục lộ trình!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/student-learning-paths";
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