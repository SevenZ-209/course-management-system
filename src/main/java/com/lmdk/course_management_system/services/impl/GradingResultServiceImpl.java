package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.repository.*;
import com.lmdk.course_management_system.services.AssignedAssignmentService;
import com.lmdk.course_management_system.services.AssignmentAttemptService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.GradingResultService;

import com.lmdk.course_management_system.services.StudentLearningPathService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GradingResultServiceImpl implements GradingResultService {

    private final GradingResultRepository gradingResultRepository;
    private final AssignmentAttemptRepository assignmentAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final QuestionRepository questionRepository;
    private final AssignedAssignmentRepository assignedAssignmentRepository;
    private final StudentLearningPathService studentLearningPathService;
    private final EnrollmentService enrollmentService;
    private final AssignedAssignmentService assignedAssignmentService;
    private final AssignmentAttemptService assignmentAttemptService;

    @Override
    public GradingResult getGradingResultById(Integer id) {
        return gradingResultRepository.getGradingResultById(id);
    }

    @Override
    public GradingResult getGradingResultByAttempt(Integer attemptId) {
        return gradingResultRepository.getGradingResultByAttempt(attemptId);
    }

    @Override
    public Map<Integer, GradingResult> getGradingResultsByAttemptIds(List<Integer> attemptIds) {
        return gradingResultRepository
                .getGradingResultsByAttemptIds(attemptIds)
                .stream()
                .collect(Collectors.toMap(
                        result -> result.getAssignmentAttempt().getId(),
                        Function.identity()
                ));
    }

    @Override
    public GradingResult processSubmittedAttempt(Integer attemptId) {
        AssignmentAttempt attempt =
                assignmentAttemptRepository.getAttemptByIdForUpdate(attemptId);

        if (attempt == null)
            throw new IllegalArgumentException("Lần làm bài không tồn tại!");

        if (attempt.getStatus() != AssignmentAttempt.AttemptStatus.SUBMITTED)
            throw new IllegalArgumentException(
                    "Bài làm chưa ở trạng thái đã nộp!"
            );

        List<StudentAnswer> studentAnswers =
                studentAnswerRepository.getStudentAnswersByAttempt(attemptId);

        BigDecimal autoScore = calculateAutoScore(studentAnswers);

        boolean hasPendingEssay = studentAnswers.stream()
                .anyMatch(answer ->
                        answer.getQuestion().getType() == Question.QuestionType.ESSAY
                                && answer.getScore() == null
                );

        GradingResult result =
                gradingResultRepository.getGradingResultByAttempt(attemptId);

        boolean isNew = result == null;

        if (isNew) {
            result = new GradingResult();
            result.setAssignmentAttempt(attempt);
        }

        result.setAutoScore(autoScore);
        result.setEssayScore(BigDecimal.ZERO);
        result.setTotalScore(autoScore);
        result.setTeacher(null);
        result.setComment(null);
        result.setGradedAt(null);

        if (isNew)
            gradingResultRepository.addGradingResult(result);
        else
            gradingResultRepository.updateGradingResult(result);

        if (hasPendingEssay) {
            attempt.setStatus(
                    AssignmentAttempt.AttemptStatus.PENDING_GRADING
            );

            assignmentAttemptRepository.updateAttempt(attempt);

            return result;
        }

        return finalizeWithoutEssay(attempt, result);
    }

    @Override
    public GradingResult submitAndProcessAttempt(Integer attemptId, Integer studentId) {
        AssignmentAttempt attempt = assignmentAttemptService.submitAttempt(attemptId, studentId);
        return processSubmittedAttempt(attempt.getId());
    }

    @Override
    public void gradeEssayAnswer(Integer studentAnswerId,
                                 BigDecimal score,
                                 String teacherComment,
                                 User teacher) {
        StudentAnswer studentAnswer =
                studentAnswerRepository.getStudentAnswerById(studentAnswerId);

        if (studentAnswer == null)
            throw new IllegalArgumentException(
                    "Câu trả lời của học viên không tồn tại!"
            );

        if (studentAnswer.getQuestion().getType()
                != Question.QuestionType.ESSAY)
            throw new IllegalArgumentException(
                    "Chỉ có thể chấm thủ công câu hỏi tự luận!"
            );

        AssignmentAttempt attempt = assignmentAttemptRepository.getAttemptByIdForUpdate(
                studentAnswer.getAssignmentAttempt().getId()
        );

        if (attempt.getStatus()
                != AssignmentAttempt.AttemptStatus.PENDING_GRADING)
            throw new IllegalArgumentException(
                    "Bài làm không ở trạng thái chờ chấm!"
            );

        validateGraderAccess(attempt, teacher);
        validateEssayScore(studentAnswer.getQuestion(), score);

        studentAnswer.setScore(score);
        studentAnswer.setTeacherComment(
                teacherComment == null || teacherComment.trim().isBlank()
                        ? null
                        : teacherComment.trim()
        );

        studentAnswerRepository.updateStudentAnswer(studentAnswer);
    }

    @Override
    public GradingResult finalizeGrading(Integer attemptId,
                                         User teacher,
                                         String comment) {
        AssignmentAttempt attempt =
                assignmentAttemptRepository.getAttemptByIdForUpdate(attemptId);

        if (attempt == null)
            throw new IllegalArgumentException(
                    "Lần làm bài không tồn tại!"
            );

        if (attempt.getStatus()
                != AssignmentAttempt.AttemptStatus.PENDING_GRADING)
            throw new IllegalArgumentException(
                    "Bài làm không ở trạng thái chờ chấm!"
            );

        validateGraderAccess(attempt, teacher);

        List<StudentAnswer> studentAnswers =
                studentAnswerRepository.getStudentAnswersByAttempt(attemptId);

        boolean hasPendingEssay = studentAnswers.stream()
                .anyMatch(answer ->
                        answer.getQuestion().getType()
                                == Question.QuestionType.ESSAY
                                && answer.getScore() == null
                );

        if (hasPendingEssay)
            throw new IllegalArgumentException(
                    "Vẫn còn câu tự luận chưa được chấm!"
            );

        BigDecimal autoScore = calculateAutoScore(studentAnswers);
        BigDecimal essayScore = calculateEssayScore(studentAnswers);
        BigDecimal totalScore = autoScore.add(essayScore);

        validateTotalScore(attempt, totalScore);

        GradingResult result =
                gradingResultRepository.getGradingResultByAttempt(attemptId);

        if (result == null) {
            result = new GradingResult();
            result.setAssignmentAttempt(attempt);
        }

        result.setAutoScore(autoScore);
        result.setEssayScore(essayScore);
        result.setTotalScore(totalScore);
        result.setTeacher(teacher);
        result.setComment(
                comment == null || comment.trim().isBlank()
                        ? null
                        : comment.trim()
        );
        result.setGradedAt(LocalDateTime.now());

        if (result.getId() == null)
            gradingResultRepository.addGradingResult(result);
        else
            gradingResultRepository.updateGradingResult(result);

        finishAttempt(attempt, totalScore);

        return result;
    }

    @Override
    public List<GradingResult> getGradingResults(
            Map<String, String> params) {
        return gradingResultRepository.getGradingResults(params);
    }

    @Override
    public long countGradingResults(Map<String, String> params) {
        return gradingResultRepository.countGradingResults(params);
    }

    private GradingResult finalizeWithoutEssay(
            AssignmentAttempt attempt,
            GradingResult result) {

        BigDecimal totalScore = result.getAutoScore();

        validateTotalScore(attempt, totalScore);

        result.setEssayScore(BigDecimal.ZERO);
        result.setTotalScore(totalScore);
        result.setTeacher(null);
        result.setGradedAt(LocalDateTime.now());

        gradingResultRepository.updateGradingResult(result);

        finishAttempt(attempt, totalScore);

        return result;
    }

    private void finishAttempt(AssignmentAttempt attempt,
                               BigDecimal totalScore) {

        AssignedAssignment assigned =
                attempt.getAssignedAssignment();

        LearningPathDetail detail =
                assigned.getLearningPathDetail();

        if (detail == null) {
            attempt.setPassed(null);
            attempt.setStatus(
                    AssignmentAttempt.AttemptStatus.GRADED
            );

            assigned.setStatus(
                    AssignedAssignment.AssignedStatus.COMPLETED
            );

            assignmentAttemptRepository.updateAttempt(attempt);
            assignedAssignmentRepository.updateAssignedAssignment(assigned);

            return;
        }

        if (detail.getMinimumScore() == null)
            throw new IllegalStateException(
                    "Lộ trình chưa cấu hình điểm đạt!"
            );

        boolean scorePassed =
                totalScore.compareTo(
                        detail.getMinimumScore()
                ) >= 0;

        Integer durationMinutes =
                assigned.getAssignment()
                        .getDurationMinutes();

        boolean withinTime =
                durationMinutes == null
                        || attempt.getDurationSeconds() <= durationMinutes * 60;

        boolean passed =
                scorePassed && withinTime;

        attempt.setPassed(passed);
        attempt.setStatus(
                AssignmentAttempt.AttemptStatus.GRADED
        );

        assignmentAttemptRepository.updateAttempt(attempt);

        if (!passed)
            return;

        assigned.setStatus(
                AssignedAssignment.AssignedStatus.COMPLETED
        );

        assignedAssignmentRepository.updateAssignedAssignment(assigned);

        studentLearningPathService.advanceAfterPassedDetail(
                assigned.getStudent().getId(),
                detail.getId()
        );
    }

    private BigDecimal calculateAutoScore(
            List<StudentAnswer> studentAnswers) {

        return studentAnswers.stream()
                .filter(answer ->
                        answer.getQuestion().getType()
                                != Question.QuestionType.ESSAY
                )
                .map(StudentAnswer::getScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateEssayScore(
            List<StudentAnswer> studentAnswers) {

        return studentAnswers.stream()
                .filter(answer ->
                        answer.getQuestion().getType()
                                == Question.QuestionType.ESSAY
                )
                .map(StudentAnswer::getScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateEssayScore(Question question,
                                    BigDecimal score) {
        if (score == null)
            throw new IllegalArgumentException(
                    "Vui lòng nhập điểm tự luận!"
            );

        if (score.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(
                    "Điểm tự luận không được nhỏ hơn 0!"
            );

        if (score.compareTo(question.getScore()) > 0)
            throw new IllegalArgumentException(
                    "Điểm tự luận không được vượt quá điểm tối đa của câu hỏi!"
            );
    }

    private void validateTotalScore(AssignmentAttempt attempt,
                                    BigDecimal totalScore) {
        BigDecimal maximumScore = attempt
                .getAssignedAssignment()
                .getAssignment()
                .getMaximumScore();

        if (totalScore.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(
                    "Tổng điểm không hợp lệ!"
            );

        if (totalScore.compareTo(maximumScore) > 0)
            throw new IllegalArgumentException(
                    "Tổng điểm không được vượt quá điểm tối đa của bài!"
            );
    }

    private void validateTeacher(User teacher) {
        if (teacher == null)
            throw new IllegalArgumentException(
                    "Không xác định được người chấm bài!"
            );

        if (teacher.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Tài khoản người chấm không hoạt động!"
            );

        if (teacher.getRole() != User.UserRole.TEACHER
                && teacher.getRole() != User.UserRole.MANAGER
                && teacher.getRole() != User.UserRole.ADMIN)
            throw new ForbiddenException(
                    "Tài khoản không có quyền chấm bài!"
            );
    }

    @Override
    public List<AssignmentAttempt> getPendingAttempts(User grader) {
        validateTeacher(grader);

        if (grader.getRole() == User.UserRole.ADMIN
                || grader.getRole() == User.UserRole.MANAGER)
            return assignmentAttemptRepository.getPendingGradingAttempts();

        return assignmentAttemptRepository
                .getPendingGradingAttemptsByTeacher(grader.getId());
    }

    @Override
    public List<AssignmentAttempt> getPendingAttempts(
            User grader,
            Map<String, String> params
    ) {
        validateTeacher(grader);

        params.put(
                "status",
                AssignmentAttempt.AttemptStatus.PENDING_GRADING.name()
        );
        params.put("order", "submittedAtAsc");

        if(grader.getRole() == User.UserRole.TEACHER)
            params.put("teacherId", String.valueOf(grader.getId()));
        else
            params.remove("teacherId");

        return assignmentAttemptRepository.getAttempts(params);
    }

    @Override
    public long countPendingAttempts(
            User grader,
            Map<String, String> params
    ) {
        validateTeacher(grader);

        params.put(
                "status",
                AssignmentAttempt.AttemptStatus.PENDING_GRADING.name()
        );
        params.put("order", "submittedAtAsc");

        if(grader.getRole() == User.UserRole.TEACHER)
            params.put("teacherId", String.valueOf(grader.getId()));
        else
            params.remove("teacherId");

        return assignmentAttemptRepository.countAttempts(params);
    }

    @Override
    public AssignmentAttempt getAttemptForGrading(
            Integer attemptId,
            User grader) {

        AssignmentAttempt attempt =
                assignmentAttemptRepository.getAttemptById(attemptId);

        if (attempt == null)
            throw new IllegalArgumentException(
                    "Lần làm bài không tồn tại!"
            );

        if (attempt.getStatus()
                != AssignmentAttempt.AttemptStatus.PENDING_GRADING)
            throw new IllegalArgumentException(
                    "Bài làm không ở trạng thái chờ chấm!"
            );

        validateGraderAccess(attempt, grader);

        return attempt;
    }

    private void validateGraderAccess(
            AssignmentAttempt attempt,
            User grader) {

        validateTeacher(grader);

        if (grader.getRole() == User.UserRole.ADMIN
                || grader.getRole() == User.UserRole.MANAGER)
            return;

        Integer studentId = attempt
                .getAssignedAssignment()
                .getStudent()
                .getId();

        Integer courseId = attempt
                .getAssignedAssignment()
                .getAssignment()
                .getCourse()
                .getId();

        boolean allowed = enrollmentService
                .existsActiveEnrollmentByStudentCourseAndTeacher(
                        studentId,
                        courseId,
                        grader.getId()
                );

        if (!allowed)
            throw new ForbiddenException(
                    "Bạn không được phân công chấm bài của học viên này!"
            );
    }
}