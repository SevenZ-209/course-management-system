package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/assigned-assignments")
@RequiredArgsConstructor
public class AssignedAssignmentController {

    private final AssignedAssignmentService assignedAssignmentService;
    private final StudentLearningPathService studentLearningPathService;
    private final LearningPathService learningPathService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final UserService userService;

    @Value("${assigned-assignments.page-size:10}")
    private int pageSize;

    @GetMapping
    public String assignedAssignments(@RequestParam Map<String, String> params,
                                      Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = assignedAssignmentService.countAssignedAssignments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute(
                "assignedAssignments",
                assignedAssignmentService.getAssignedAssignments(params)
        );
        model.addAttribute(
                "progresses",
                studentLearningPathService.getInProgressStudentLearningPaths()
        );
        model.addAttribute(
                "students",
                userService.getUsersByRole(User.UserRole.STUDENT)
        );
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("learningPaths", learningPathService.getAllLearningPaths());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("learningPathId", params.getOrDefault("learningPathId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));
        model.addAttribute("date", params.getOrDefault("date", ""));

        return "admin/assigned-assignments";
    }

    @GetMapping("/available-assignments")
    @ResponseBody
    public List<Map<String, Object>> getAvailableAssignments(
            @RequestParam Integer studentId) {

        List<Enrollment> enrollments =
                enrollmentService.getActiveEnrollmentsByStudent(studentId);

        Map<Integer, Map<String, Object>> result = new LinkedHashMap<>();

        for (Enrollment enrollment : enrollments) {
            Integer courseId = enrollment.getCourseClass().getCourse().getId();

            for (Assignment assignment : assignmentService.getAssignmentsByCourse(courseId)) {
                result.putIfAbsent(
                        assignment.getId(),
                        Map.<String, Object>of(
                                "id", assignment.getId(),
                                "name", assignment.getName(),
                                "courseName", assignment.getCourse().getName()
                        )
                );
            }
        }

        return List.copyOf(result.values());
    }

    @PostMapping("/release-current")
    public String releaseCurrent(
            @RequestParam Integer studentLearningPathId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime availableAt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dueAt,
            RedirectAttributes redirectAttributes) {

        try {
            assignedAssignmentService.assignCurrentDetail(
                    studentLearningPathId,
                    availableAt,
                    dueAt
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Phát bài theo lộ trình thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/assigned-assignments";
    }

    @PostMapping("/manual")
    public String assignManual(
            @RequestParam Integer studentId,
            @RequestParam Integer assignmentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime availableAt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dueAt,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User assignedBy = userService.getUserByUsername(authentication.getName());

        try {
            assignedAssignmentService.assignManual(
                    studentId,
                    assignmentId,
                    assignedBy,
                    availableAt,
                    dueAt
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Giao bài thủ công thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/assigned-assignments";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            assignedAssignmentService.updateAvailabilityStatus(
                    id,
                    AssignedAssignment.AssignedStatus.valueOf(status)
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật trạng thái bài thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/assigned-assignments";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}