package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.assignment.*;
import com.lmdk.course_management_system.helpers.AssignmentAttemptHelper;
import com.lmdk.course_management_system.pojo.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StudentAssignmentMapper {

    private final AssignmentAttemptHelper assignmentAttemptHelper;

    public AssignedAssignmentResponse toAssignedResponse(
            AssignedAssignment assigned,
            AssignmentAttempt latest,
            boolean canStart
    ) {
        Assignment assignment = assigned.getAssignment();

        return new AssignedAssignmentResponse(
                assigned.getId(),
                assignment.getId(),
                assignment.getName(),
                assignment.getCourse().getName(),
                assignment.getType().name(),
                assignment.getMaximumScore(),
                assignment.getDurationMinutes(),
                assigned.getAvailableAt(),
                assigned.getDueAt(),
                assigned.getStatus().name(),
                canStart,

                latest == null
                        ? null
                        : latest.getId(),

                latest == null
                        ? null
                        : latest.getAttemptNumber(),

                latest == null
                        ? null
                        : latest.getStatus().name()
        );
    }

    public AttemptResponse toAttemptResponse(
            AssignmentAttempt attempt
    ) {
        return new AttemptResponse(
                attempt.getId(),
                attempt.getAssignedAssignment().getId(),
                attempt.getAttemptNumber(),
                attempt.getStartedAt(),
                assignmentAttemptHelper.calculateEndTime(attempt),
                assignmentAttemptHelper.calculateRemainingSeconds(attempt),
                attempt.getStatus().name()
        );
    }

    public AttemptDetailResponse toAttemptDetailResponse(
            AssignmentAttempt attempt,
            List<QuestionResponse> questions
    ) {
        Assignment assignment =
                attempt.getAssignedAssignment().getAssignment();

        return new AttemptDetailResponse(
                attempt.getId(),
                attempt.getAttemptNumber(),
                assignment.getId(),
                assignment.getName(),
                assignment.getMaximumScore(),
                attempt.getStartedAt(),
                assignmentAttemptHelper.calculateEndTime(attempt),
                assignmentAttemptHelper.calculateRemainingSeconds(attempt),
                questions
        );
    }

    public QuestionResponse toQuestionResponse(
            Question question,
            List<StudentAnswer> savedAnswers,
            List<Answer> answers
    ) {
        StudentAnswer saved =
                savedAnswers.stream()
                        .filter(answer ->
                                answer.getQuestion()
                                        .getId()
                                        .equals(question.getId())
                        )
                        .findFirst()
                        .orElse(null);

        List<AnswerOptionResponse> options =
                question.getType() == Question.QuestionType.MULTIPLE_CHOICE
                        ? answers.stream()
                          .filter(answer ->
                                  answer.getType()
                                  == Answer.AnswerType.CHOICE
                          )
                          .map(answer ->
                               new AnswerOptionResponse(
                                       answer.getId(),
                                       answer.getOrderNumber(),
                                       answer.getContent()
                               )
                          )
                          .toList()
                        : List.of();

        return new QuestionResponse(
                question.getId(),
                question.getOrderNumber(),
                question.getContent(),
                question.getType().name(),
                question.getScore(),
                options,

                saved == null
                        || saved.getSelectedAnswer() == null
                        ? null
                        : saved.getSelectedAnswer().getId(),

                saved == null
                        ? null
                        : saved.getAnswerContent()
        );
    }

    public StudentAnswerResponse toAnswerResponse(
            StudentAnswer answer
    ) {
        return new StudentAnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),

                answer.getSelectedAnswer() == null
                        ? null
                        : answer.getSelectedAnswer().getId(),

                answer.getAnswerContent()
        );
    }

    public SubmitResponse toSubmitResponse(
            AssignmentAttempt attempt,
            GradingResult result
    ) {
        boolean graded =
                attempt.getStatus()
                        == AssignmentAttempt.AttemptStatus.GRADED;

        return new SubmitResponse(
                attempt.getId(),
                attempt.getStatus().name(),

                result == null
                        ? null
                        : result.getAutoScore(),

                graded && result != null
                        ? result.getTotalScore()
                        : null,

                graded
                        ? attempt.getPassed()
                        : null
        );
    }

    public AttemptResultResponse toResultResponse(
            AssignmentAttempt attempt,
            GradingResult result
    ) {
        boolean graded =
                attempt.getStatus()
                        == AssignmentAttempt.AttemptStatus.GRADED;

        return new AttemptResultResponse(
                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getStatus().name(),
                attempt.getSubmittedAt(),
                attempt.getDurationSeconds(),

                result == null
                        ? null
                        : result.getAutoScore(),

                result == null
                        ? null
                        : result.getEssayScore(),

                result == null
                        ? null
                        : result.getTotalScore(),

                graded
                        ? attempt.getPassed()
                        : null,

                result == null
                        ? null
                        : result.getComment()
        );
    }
}