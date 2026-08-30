package com.lmdk.course_management_system.controllers.api.teacher;

import com.lmdk.course_management_system.dto.teacher.grading.*;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.teacher.TeacherGradingMapper;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.AnswerService;
import com.lmdk.course_management_system.services.GradingResultService;
import com.lmdk.course_management_system.services.StudentAnswerService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/grading")
@RequiredArgsConstructor
public class ApiTeacherGradingController {

    private final GradingResultService gradingResultService;
    private final StudentAnswerService studentAnswerService;
    private final AnswerService answerService;
    private final CurrentUserHelper currentUserHelper;
    private final TeacherGradingMapper teacherGradingMapper;

    @Value("${assignment-attempts.page-size:10}")
    private int pageSize;

    @GetMapping("/pending")
    public PendingAttemptPageResponse getPendingAttempts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            Authentication authentication
    ) {
        User grader = currentUserHelper.getCurrentUser(authentication);

        page = Math.max(page, 1);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        long totalRecords = gradingResultService
                .countPendingAttempts(grader, params);

        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        List<PendingAttemptResponse> attempts = gradingResultService
                .getPendingAttempts(grader, params)
                .stream()
                .map(teacherGradingMapper::toPendingResponse)
                .toList();

        return new PendingAttemptPageResponse(
                attempts,
                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/{attemptId}")
    public GradingDetailResponse getGradingDetail(
            @PathVariable Integer attemptId,
            Authentication authentication
    ) {
        User grader =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        AssignmentAttempt attempt =
                gradingResultService
                        .getAttemptForGrading(
                                attemptId,
                                grader
                        );

        GradingResult result =
                gradingResultService
                        .getGradingResultByAttempt(
                                attemptId
                        );

        List<StudentAnswerResponse> answers =
                studentAnswerService
                        .getStudentAnswersByAttempt(attemptId)
                        .stream()
                        .map(studentAnswer -> {

                            Question question =
                                    studentAnswer.getQuestion();

                            List<String> references =
                                    question.getType()
                                            == Question.QuestionType.ESSAY
                                            ? answerService
                                              .getAnswersByQuestion(
                                                      question.getId()
                                              )
                                              .stream()
                                              .filter(answer ->
                                                      answer.getType()
                                                      == Answer.AnswerType
                                                         .REFERENCE_ANSWER
                                              )
                                              .map(Answer::getContent)
                                              .toList()
                                            : List.of();

                            return teacherGradingMapper
                                    .toStudentAnswerResponse(
                                            studentAnswer,
                                            references
                                    );
                        })
                        .toList();

        return teacherGradingMapper
                .toDetailResponse(
                        attempt,
                        result,
                        answers
                );
    }

    @PostMapping("/answers/{studentAnswerId}")
    public MessageResponse gradeEssay(
            @PathVariable Integer studentAnswerId,
            @RequestBody GradeEssayRequest request,
            Authentication authentication
    ) {
        User grader =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        gradingResultService.gradeEssayAnswer(
                studentAnswerId,
                request.score(),
                request.teacherComment(),
                grader
        );

        return new MessageResponse(
                "Chấm câu tự luận thành công!"
        );
    }

    @PostMapping("/{attemptId}/finalize")
    public FinalizeResponse finalizeGrading(
            @PathVariable Integer attemptId,
            @RequestBody(required = false)
            FinalizeRequest request,
            Authentication authentication
    ) {
        User grader =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        String comment =
                request == null
                        ? null
                        : request.comment();

        GradingResult result =
                gradingResultService
                        .finalizeGrading(
                                attemptId,
                                grader,
                                comment
                        );

        return teacherGradingMapper
                .toFinalizeResponse(result);
    }
}