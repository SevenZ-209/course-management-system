package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.grading.FinalizeResponse;
import com.lmdk.course_management_system.dto.teacher.grading.GradingDetailResponse;
import com.lmdk.course_management_system.dto.teacher.grading.PendingAttemptResponse;
import com.lmdk.course_management_system.dto.teacher.grading.StudentAnswerResponse;
import com.lmdk.course_management_system.pojo.*;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TeacherGradingMapper {

    public PendingAttemptResponse toPendingResponse(
            AssignmentAttempt attempt
    ) {
        AssignedAssignment assigned =
                attempt.getAssignedAssignment();

        return new PendingAttemptResponse(
                attempt.getId(),
                attempt.getAttemptNumber(),
                assigned.getStudent().getId(),
                assigned.getStudent().getFullName(),
                assigned.getAssignment().getId(),
                assigned.getAssignment().getName(),
                assigned.getAssignment()
                        .getCourse()
                        .getName(),
                attempt.getSubmittedAt()
        );
    }

    public StudentAnswerResponse toStudentAnswerResponse(
            StudentAnswer studentAnswer,
            List<String> references
    ) {
        Question question =
                studentAnswer.getQuestion();

        return new StudentAnswerResponse(
                studentAnswer.getId(),
                question.getId(),
                question.getOrderNumber(),
                question.getContent(),
                question.getType().name(),
                question.getScore(),
                studentAnswer.getAnswerContent(),

                studentAnswer.getSelectedAnswer() == null
                        ? null
                        : studentAnswer
                          .getSelectedAnswer()
                          .getContent(),

                studentAnswer.getScore(),
                studentAnswer.getTeacherComment(),
                references
        );
    }

    public GradingDetailResponse toDetailResponse(
            AssignmentAttempt attempt,
            GradingResult result,
            List<StudentAnswerResponse> answers
    ) {
        AssignedAssignment assigned =
                attempt.getAssignedAssignment();

        return new GradingDetailResponse(
                attempt.getId(),
                attempt.getAttemptNumber(),
                assigned.getStudent().getId(),
                assigned.getStudent().getFullName(),
                assigned.getAssignment().getId(),
                assigned.getAssignment().getName(),
                assigned.getAssignment().getMaximumScore(),

                result == null
                        ? BigDecimal.ZERO
                        : result.getAutoScore(),

                answers
        );
    }

    public FinalizeResponse toFinalizeResponse(
            GradingResult result
    ) {
        AssignmentAttempt attempt =
                result.getAssignmentAttempt();

        return new FinalizeResponse(
                result.getId(),
                attempt.getId(),
                result.getAutoScore(),
                result.getEssayScore(),
                result.getTotalScore(),
                attempt.getPassed(),
                attempt.getStatus().name()
        );
    }
}