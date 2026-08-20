package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.question.*;
import com.lmdk.course_management_system.mappers.admin.AdminQuestionMapper;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.QuestionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class ApiAdminQuestionController {

    private final QuestionService questionService;
    private final AssignmentService assignmentService;
    private final AdminQuestionMapper adminQuestionMapper;

    @Value("${questions.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminQuestionPageResponse getQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer assignmentId,
            @RequestParam(required = false) String type
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if (kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if (courseId != null)
            params.put(
                    "courseId",
                    String.valueOf(courseId)
            );

        if (assignmentId != null)
            params.put(
                    "assignmentId",
                    String.valueOf(assignmentId)
            );

        if (type != null && !type.isBlank())
            params.put("type", type);

        long totalRecords =
                questionService.countQuestions(params);

        int totalPages = Math.max(
                (int) Math.ceil(
                        (double) totalRecords / pageSize
                ),
                1
        );

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminQuestionPageResponse(
                questionService
                        .getQuestions(params)
                        .stream()
                        .map(adminQuestionMapper::toResponse)
                        .toList(),

                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/options")
    public List<AdminQuestionOptionResponse> getOptions(
            @RequestParam Integer assignmentId
    ) {
        return questionService
                .getQuestionsByAssignment(assignmentId)
                .stream()
                .map(question ->
                        new AdminQuestionOptionResponse(
                                question.getId(),
                                question.getContent(),
                                question.getType().name(),
                                question.getOrderNumber()
                        )
                )
                .toList();
    }

    @PostMapping
    public AdminQuestionActionResponse addQuestion(
            @RequestBody CreateQuestionRequest request
    ) {
        if(request.assignmentId() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn bài tập!"
            );

        Assignment assignment =
                assignmentService.getAssignmentById(
                        request.assignmentId()
                );

        if (assignment == null)
            throw new IllegalArgumentException(
                    "Bài tập không tồn tại!"
            );

        Question question = new Question();

        question.setAssignment(assignment);
        question.setContent(request.content());
        question.setType(
                parseType(request.type())
        );
        question.setScore(request.score());
        question.setOrderNumber(
                request.orderNumber()
        );

        questionService.addQuestion(question);

        return new AdminQuestionActionResponse(
                question.getId(),
                "Thêm câu hỏi thành công!"
        );
    }

    @PutMapping("/{questionId}")
    public AdminQuestionActionResponse updateQuestion(
            @PathVariable Integer questionId,
            @RequestBody UpdateQuestionRequest request
    ) {
        Question question =
                requireQuestion(questionId);

        question.setContent(request.content());
        question.setType(
                parseType(request.type())
        );
        question.setScore(request.score());
        question.setOrderNumber(
                request.orderNumber()
        );

        questionService.updateQuestion(question);

        return new AdminQuestionActionResponse(
                questionId,
                "Cập nhật câu hỏi thành công!"
        );
    }

    private Question requireQuestion(
            Integer questionId
    ) {
        Question question =
                questionService.getQuestionById(
                        questionId
                );

        if (question == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy câu hỏi!"
            );

        return question;
    }

    private Question.QuestionType parseType(
            String type
    ) {
        if (type == null || type.isBlank())
            throw new IllegalArgumentException(
                    "Loại câu hỏi không được để trống!"
            );

        try {
            return Question.QuestionType.valueOf(
                    type.trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Loại câu hỏi không hợp lệ!"
            );
        }
    }
}