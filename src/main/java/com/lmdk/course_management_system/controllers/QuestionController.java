package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.QuestionService;

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
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;

    @Value("${questions.page-size:10}")
    private int pageSize;

    @GetMapping
    public String questions(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = questionService.countQuestions(params);
        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("questions", questionService.getQuestions(params));
        model.addAttribute("assignments", assignmentService.getAllAssignments());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("assignmentId", params.getOrDefault("assignmentId", ""));
        model.addAttribute("type", params.getOrDefault("type", ""));

        return "admin/questions";
    }

    @GetMapping("/assignments")
    @ResponseBody
    public List<Map<String, Object>> getAssignmentsByCourse(
            @RequestParam Integer courseId) {

        return assignmentService.getAssignmentsByCourse(courseId)
                .stream()
                .map(assignment -> Map.<String, Object>of(
                        "id", assignment.getId(),
                        "name", assignment.getName(),
                        "maximumScore", assignment.getMaximumScore()
                ))
                .toList();
    }

    @PostMapping("/add")
    public String addQuestion(@RequestParam Integer assignmentId,
                              @RequestParam String content,
                              @RequestParam String type,
                              @RequestParam BigDecimal score,
                              @RequestParam Integer orderNumber,
                              RedirectAttributes redirectAttributes) {

        Assignment assignment =
                assignmentService.getAssignmentById(assignmentId);

        if (assignment == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Bài tập không tồn tại!"
            );
            return "redirect:/admin/questions";
        }

        try {
            Question question = new Question();
            question.setAssignment(assignment);
            question.setContent(content);
            question.setType(Question.QuestionType.valueOf(type));
            question.setScore(score);
            question.setOrderNumber(orderNumber);

            questionService.addQuestion(question);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm câu hỏi thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/questions";
    }

    @PostMapping("/update")
    public String updateQuestion(@RequestParam Integer questionId,
                                 @RequestParam String content,
                                 @RequestParam String type,
                                 @RequestParam BigDecimal score,
                                 @RequestParam Integer orderNumber,
                                 RedirectAttributes redirectAttributes) {

        Question question =
                questionService.getQuestionById(questionId);

        if (question == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không tìm thấy câu hỏi!"
            );
            return "redirect:/admin/questions";
        }

        try {
            question.setContent(content);
            question.setType(Question.QuestionType.valueOf(type));
            question.setScore(score);
            question.setOrderNumber(orderNumber);

            questionService.updateQuestion(question);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật câu hỏi thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/questions";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}