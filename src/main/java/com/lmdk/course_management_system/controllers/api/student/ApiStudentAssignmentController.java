package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.assignment.*;
import com.lmdk.course_management_system.helpers.*;
import com.lmdk.course_management_system.mappers.student.StudentAssignmentMapper;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/assignments")
@RequiredArgsConstructor
public class ApiStudentAssignmentController {

    private final AssignedAssignmentService assignedAssignmentService;
    private final AssignmentAttemptService assignmentAttemptService;
    private final StudentAnswerService studentAnswerService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final GradingResultService gradingResultService;
    private final StudentLearningPathService studentLearningPathService;

    private final CurrentUserHelper currentUserHelper;
    private final StudentAccessHelper studentAccessHelper;
    private final AssignedAssignmentHelper assignedAssignmentHelper;
    private final AssignmentAttemptHelper assignmentAttemptHelper;
    private final StudentAssignmentMapper studentAssignmentMapper;

    @GetMapping
    public List<AssignedAssignmentResponse> getAssignments(
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        return assignedAssignmentService
                .getAssignedAssignmentsByStudent(student.getId())
                .stream()
                .map(this::mapAssignedAssignment)
                .toList();
    }

    @GetMapping("/current")
    public AssignedAssignmentResponse getCurrentAssignment(
            @RequestParam Integer courseId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        studentAccessHelper.requireActiveCourse(
                student.getId(),
                courseId
        );

        StudentLearningPath progress =
                studentLearningPathService
                        .getStudentLearningPathsByStudent(student.getId())
                        .stream()
                        .filter(p ->
                                p.getLearningPath()
                                        .getCourse()
                                        .getId()
                                        .equals(courseId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bạn chưa được gán lộ trình cho khóa học này!"
                                )
                        );

        if(progress.getStatus()
                == StudentLearningPath.ProgressStatus.PAUSED)
            throw new IllegalArgumentException(
                    "Lộ trình đang tạm dừng!"
            );

        if(progress.getStatus()
                == StudentLearningPath.ProgressStatus.COMPLETED)
            throw new IllegalArgumentException(
                    "Lộ trình đã hoàn thành!"
            );

        if(progress.getCurrentDetail() == null)
            throw new IllegalArgumentException(
                    "Không xác định được bài hiện tại!"
            );

        Integer currentDetailId =
                progress.getCurrentDetail().getId();

        AssignedAssignment assigned =
                assignedAssignmentService
                        .getAssignedAssignmentsByStudent(student.getId())
                        .stream()
                        .filter(a ->
                                a.getLearningPathDetail() != null
                        )
                        .filter(a ->
                                a.getLearningPathDetail()
                                        .getId()
                                        .equals(currentDetailId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bài hiện tại chưa được phát!"
                                )
                        );

        return mapAssignedAssignment(assigned);
    }

    @PostMapping("/{assignedAssignmentId}/start")
    public AttemptResponse startAttempt(
            @PathVariable Integer assignedAssignmentId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        AssignmentAttempt attempt =
                assignmentAttemptService.startAttempt(
                        assignedAssignmentId,
                        student.getId()
                );

        return studentAssignmentMapper
                .toAttemptResponse(attempt);
    }

    @GetMapping("/attempts/{attemptId}")
    public AttemptDetailResponse getAttempt(
            @PathVariable Integer attemptId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        AssignmentAttempt attempt =
                assignmentAttemptService.getAttemptById(attemptId);

        assignmentAttemptHelper.validateOwner(
                attempt,
                student.getId()
        );

        if(attempt.getStatus()
                != AssignmentAttempt.AttemptStatus.IN_PROGRESS)
            throw new IllegalArgumentException(
                    "Bài làm không còn ở trạng thái đang thực hiện!"
            );

        Assignment assignment =
                attempt.getAssignedAssignment()
                        .getAssignment();

        List<StudentAnswer> savedAnswers =
                studentAnswerService
                        .getStudentAnswersByAttempt(attemptId);

        List<QuestionResponse> questions =
                questionService
                        .getQuestionsByAssignment(assignment.getId())
                        .stream()
                        .map(question -> {
                            List<Answer> answers =
                                    question.getType()
                                            == Question.QuestionType.MULTIPLE_CHOICE
                                            ? answerService.getAnswersByQuestion(
                                            question.getId()
                                    )
                                            : List.of();

                            return studentAssignmentMapper
                                    .toQuestionResponse(
                                            question,
                                            savedAnswers,
                                            answers
                                    );
                        })
                        .toList();

        return studentAssignmentMapper
                .toAttemptDetailResponse(
                        attempt,
                        questions
                );
    }

    @PutMapping("/attempts/{attemptId}/answers")
    public StudentAnswerResponse saveAnswer(
            @PathVariable Integer attemptId,
            @RequestBody SaveAnswerRequest request,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        StudentAnswer answer =
                studentAnswerService.saveAnswer(
                        attemptId,
                        student.getId(),
                        request.questionId(),
                        request.selectedAnswerId(),
                        request.answerContent()
                );

        return studentAssignmentMapper
                .toAnswerResponse(answer);
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public SubmitResponse submitAttempt(
            @PathVariable Integer attemptId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        AssignmentAttempt attempt =
                assignmentAttemptService.submitAttempt(
                        attemptId,
                        student.getId()
                );

        GradingResult result =
                gradingResultService
                        .processSubmittedAttempt(
                                attempt.getId()
                        );

        AssignmentAttempt updated =
                assignmentAttemptService
                        .getAttemptById(
                                attempt.getId()
                        );

        return studentAssignmentMapper
                .toSubmitResponse(
                        updated,
                        result
                );
    }

    @GetMapping("/attempts/{attemptId}/result")
    public AttemptResultResponse getResult(
            @PathVariable Integer attemptId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        AssignmentAttempt attempt =
                assignmentAttemptService
                        .getAttemptById(attemptId);

        assignmentAttemptHelper.validateOwner(
                attempt,
                student.getId()
        );

        GradingResult result =
                gradingResultService
                        .getGradingResultByAttempt(attemptId);

        return studentAssignmentMapper
                .toResultResponse(
                        attempt,
                        result
                );
    }

    private AssignedAssignmentResponse mapAssignedAssignment(
            AssignedAssignment assigned
    ) {
        AssignmentAttempt latest =
                assignmentAttemptService
                        .getLatestAttempt(
                                assigned.getId()
                        );

        boolean canStart =
                assignedAssignmentHelper
                        .canStart(
                                assigned,
                                latest
                        );

        return studentAssignmentMapper
                .toAssignedResponse(
                        assigned,
                        latest,
                        canStart
                );
    }
}