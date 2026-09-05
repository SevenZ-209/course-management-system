package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Answer;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.services.AnswerService;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.QuestionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;

    @Value("${answers.page-size:10}")
    private int pageSize;

    @GetMapping
    public String answers(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        Integer selectedCourseId = parseInteger(params.get("courseId"));
        normalizeAssignmentFilter(params, selectedCourseId);

        long totalRecords = answerService.countAnswers(params);
        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("answers", answerService.getAnswers(params));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("assignments", selectedCourseId == null
                ? List.of()
                : assignmentService.getAssignmentsByCourse(selectedCourseId));

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);

        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("assignmentId", params.getOrDefault("assignmentId", ""));
        model.addAttribute("questionId", params.getOrDefault("questionId", ""));
        model.addAttribute("type", params.getOrDefault("type", ""));
        model.addAttribute("correct", params.getOrDefault("correct", ""));

        return "admin/answers";
    }

    @GetMapping("/assignments")
    @ResponseBody
    public List<Map<String, Object>> getAssignments(
            @RequestParam Integer courseId) {

        return assignmentService.getAssignmentsByCourse(courseId)
                .stream()
                .map(assignment -> Map.<String, Object>of(
                        "id", assignment.getId(),
                        "name", assignment.getName()
                ))
                .toList();
    }

    @GetMapping("/questions")
    @ResponseBody
    public List<Map<String, Object>> getQuestions(
            @RequestParam Integer assignmentId) {

        return questionService.getQuestionsByAssignment(assignmentId)
                .stream()
                .map(question -> Map.<String, Object>of(
                        "id", question.getId(),
                        "content", question.getContent(),
                        "type", question.getType().name(),
                        "orderNumber", question.getOrderNumber()
                ))
                .toList();
    }

    @PostMapping("/add")
    public String addAnswer(@RequestParam Integer questionId,
                            @RequestParam String content,
                            @RequestParam Integer orderNumber,
                            @RequestParam(defaultValue = "false") boolean correct,
                            RedirectAttributes redirectAttributes) {

        Question question = questionService.getQuestionById(questionId);

        if (question == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Câu hỏi không tồn tại!"
            );
            return "redirect:/admin/answers";
        }

        try {
            Answer answer = new Answer();
            answer.setQuestion(question);
            answer.setContent(content);
            answer.setOrderNumber(orderNumber);
            answer.setCorrect(correct);

            answerService.addAnswer(answer);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm đáp án thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/answers";
    }

    @PostMapping("/update")
    public String updateAnswer(@RequestParam Integer answerId,
                               @RequestParam String content,
                               @RequestParam Integer orderNumber,
                               @RequestParam(defaultValue = "false") boolean correct,
                               RedirectAttributes redirectAttributes) {

        Answer answer = answerService.getAnswerById(answerId);

        if (answer == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không tìm thấy đáp án!"
            );
            return "redirect:/admin/answers";
        }

        try {
            answer.setContent(content);
            answer.setOrderNumber(orderNumber);
            answer.setCorrect(correct);

            answerService.updateAnswer(answer);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật đáp án thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/answers";
    }

    private void normalizeAssignmentFilter(Map<String, String> params, Integer courseId) {
        Integer assignmentId = parseInteger(params.get("assignmentId"));
        if (courseId == null || assignmentId == null)
            return;

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (assignment == null || assignment.getCourse() == null || !courseId.equals(assignment.getCourse().getId()))
            params.remove("assignmentId");
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