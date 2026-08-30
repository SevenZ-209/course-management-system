package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.answer.*;
import com.lmdk.course_management_system.mappers.admin.AdminAnswerMapper;
import com.lmdk.course_management_system.pojo.Answer;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.services.AnswerService;
import com.lmdk.course_management_system.services.QuestionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/answers")
@RequiredArgsConstructor
public class ApiAdminAnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;
    private final AdminAnswerMapper adminAnswerMapper;

    @Value("${answers.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminAnswerPageResponse getAnswers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer assignmentId,
            @RequestParam(required = false) Integer questionId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean correct
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

        if (questionId != null)
            params.put(
                    "questionId",
                    String.valueOf(questionId)
            );

        if (type != null && !type.isBlank())
            params.put("type", type);

        if (correct != null)
            params.put(
                    "correct",
                    String.valueOf(correct)
            );

        long totalRecords =
                answerService.countAnswers(params);

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

        return new AdminAnswerPageResponse(
                answerService
                        .getAnswers(params)
                        .stream()
                        .map(adminAnswerMapper::toResponse)
                        .toList(),

                page,
                totalPages,
                totalRecords
        );
    }

    @PostMapping
    public AdminAnswerActionResponse addAnswer(
            @RequestBody CreateAnswerRequest request
    ) {
        if(request.questionId() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn câu hỏi!"
            );

        Question question =
                questionService.getQuestionById(
                        request.questionId()
                );

        if (question == null)
            throw new IllegalArgumentException(
                    "Câu hỏi không tồn tại!"
            );

        if (request.correct() == null)
            throw new IllegalArgumentException(
                    "Trạng thái đáp án không được để trống!"
            );

        Answer answer = new Answer();

        answer.setQuestion(question);
        answer.setContent(request.content());
        answer.setOrderNumber(
                request.orderNumber()
        );
        answer.setCorrect(request.correct());

        answerService.addAnswer(answer);

        return new AdminAnswerActionResponse(
                answer.getId(),
                "Thêm đáp án thành công!"
        );
    }

    @PostMapping("/bulk")
    public AdminAnswerActionResponse addBulk(
            @RequestBody BulkAnswerRequest request
    ){

        answerService.addBulk(request);

        return new AdminAnswerActionResponse(
                null,
                "Lưu đáp án thành công!"
        );
    }

    @PutMapping("/{answerId}")
    public AdminAnswerActionResponse updateAnswer(
            @PathVariable Integer answerId,
            @RequestBody UpdateAnswerRequest request
    ) {
        Answer answer =
                answerService.getAnswerById(answerId);

        if (answer == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy đáp án!"
            );

        if (request.correct() == null)
            throw new IllegalArgumentException(
                    "Trạng thái đáp án không được để trống!"
            );

        answer.setContent(request.content());
        answer.setOrderNumber(
                request.orderNumber()
        );
        answer.setCorrect(request.correct());

        answerService.updateAnswer(answer);

        return new AdminAnswerActionResponse(
                answerId,
                "Cập nhật đáp án thành công!"
        );
    }
}