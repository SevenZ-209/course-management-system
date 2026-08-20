package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Answer;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.pojo.StudentAnswer;
import com.lmdk.course_management_system.repository.StudentAnswerRepository;
import com.lmdk.course_management_system.services.AnswerService;
import com.lmdk.course_management_system.services.AssignmentAttemptService;
import com.lmdk.course_management_system.services.QuestionService;
import com.lmdk.course_management_system.services.StudentAnswerService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentAnswerServiceImpl implements StudentAnswerService {

    private final StudentAnswerRepository studentAnswerRepository;
    private final AssignmentAttemptService assignmentAttemptService;
    private final QuestionService questionService;
    private final AnswerService answerService;

    @Override
    public StudentAnswer getStudentAnswerById(Integer id) {
        return studentAnswerRepository.getStudentAnswerById(id);
    }

    @Override
    public StudentAnswer getStudentAnswer(Integer attemptId, Integer questionId) {
        return studentAnswerRepository.getStudentAnswer(attemptId, questionId);
    }

    @Override
    public StudentAnswer saveAnswer(Integer attemptId,
                                    Integer studentId,
                                    Integer questionId,
                                    Integer selectedAnswerId,
                                    String answerContent) {
        AssignmentAttempt attempt = assignmentAttemptService.getAttemptById(attemptId);
        Question question = questionService.getQuestionById(questionId);

        validateAttempt(attempt, studentId);
        validateQuestion(attempt, question);
        validateTime(attempt);

        StudentAnswer studentAnswer =
                studentAnswerRepository.getStudentAnswer(attemptId, questionId);

        boolean isNew = studentAnswer == null;

        if (isNew) {
            studentAnswer = new StudentAnswer();
            studentAnswer.setAssignmentAttempt(attempt);
            studentAnswer.setQuestion(question);
            studentAnswer.setTeacherComment(null);
        }

        switch (question.getType()) {
            case MULTIPLE_CHOICE ->
                    processMultipleChoice(studentAnswer, question, selectedAnswerId);

            case SHORT_ANSWER ->
                    processShortAnswer(studentAnswer, question, answerContent);

            case ESSAY ->
                    processEssay(studentAnswer, answerContent);
        }

        if (isNew)
            return studentAnswerRepository.addStudentAnswer(studentAnswer);

        studentAnswerRepository.updateStudentAnswer(studentAnswer);
        return studentAnswer;
    }

    @Override
    public List<StudentAnswer> getStudentAnswers(Map<String, String> params) {
        return studentAnswerRepository.getStudentAnswers(params);
    }

    @Override
    public List<StudentAnswer> getStudentAnswersByAttempt(Integer attemptId) {
        return studentAnswerRepository.getStudentAnswersByAttempt(attemptId);
    }

    @Override
    public long countStudentAnswers(Map<String, String> params) {
        return studentAnswerRepository.countStudentAnswers(params);
    }

    @Override
    public long countStudentAnswersByAttempt(Integer attemptId) {
        return studentAnswerRepository.countStudentAnswersByAttempt(attemptId);
    }

    @Override
    public BigDecimal sumScoreByAttempt(Integer attemptId) {
        return studentAnswerRepository.sumScoreByAttempt(attemptId);
    }

    private void processMultipleChoice(StudentAnswer studentAnswer,
                                       Question question,
                                       Integer selectedAnswerId) {
        if (selectedAnswerId == null)
            throw new IllegalArgumentException("Vui lòng chọn đáp án!");

        Answer selectedAnswer = answerService.getAnswerById(selectedAnswerId);

        if (selectedAnswer == null)
            throw new IllegalArgumentException("Đáp án không tồn tại!");

        if (!selectedAnswer.getQuestion().getId().equals(question.getId()))
            throw new IllegalArgumentException(
                    "Đáp án được chọn không thuộc câu hỏi này!"
            );

        if (selectedAnswer.getType() != Answer.AnswerType.CHOICE)
            throw new IllegalArgumentException("Đáp án không hợp lệ!");

        studentAnswer.setSelectedAnswer(selectedAnswer);
        studentAnswer.setAnswerContent(null);

        boolean correct = Boolean.TRUE.equals(selectedAnswer.getCorrect());

        studentAnswer.setScore(
                correct
                        ? question.getScore()
                        : BigDecimal.ZERO
        );
    }

    private void processShortAnswer(StudentAnswer studentAnswer,
                                    Question question,
                                    String answerContent) {
        if (answerContent == null || answerContent.trim().isBlank())
            throw new IllegalArgumentException(
                    "Vui lòng nhập câu trả lời!"
            );

        String studentText = normalize(answerContent);

        boolean correct = answerService
                .getCorrectAnswersByQuestion(question.getId())
                .stream()
                .filter(answer ->
                        answer.getType() == Answer.AnswerType.SHORT_ANSWER
                )
                .anyMatch(answer ->
                        normalize(answer.getContent()).equals(studentText)
                );

        studentAnswer.setSelectedAnswer(null);
        studentAnswer.setAnswerContent(answerContent.trim());
        studentAnswer.setScore(
                correct
                        ? question.getScore()
                        : BigDecimal.ZERO
        );
    }

    private void processEssay(StudentAnswer studentAnswer,
                              String answerContent) {
        if (answerContent == null || answerContent.trim().isBlank())
            throw new IllegalArgumentException(
                    "Vui lòng nhập nội dung bài tự luận!"
            );

        studentAnswer.setSelectedAnswer(null);
        studentAnswer.setAnswerContent(answerContent.trim());
        studentAnswer.setScore(null);
    }

    private void validateAttempt(AssignmentAttempt attempt,
                                 Integer studentId) {
        if (attempt == null)
            throw new IllegalArgumentException(
                    "Lần làm bài không tồn tại!"
            );

        if (studentId == null)
            throw new IllegalArgumentException(
                    "Không xác định được học viên!"
            );

        if (!attempt.getAssignedAssignment()
                .getStudent()
                .getId()
                .equals(studentId))
            throw new IllegalArgumentException(
                    "Bạn không có quyền chỉnh sửa bài làm này!"
            );

        if (attempt.getStatus() != AssignmentAttempt.AttemptStatus.IN_PROGRESS)
            throw new IllegalArgumentException(
                    "Bài làm không còn ở trạng thái đang thực hiện!"
            );
    }

    private void validateQuestion(AssignmentAttempt attempt,
                                  Question question) {
        if (question == null)
            throw new IllegalArgumentException(
                    "Câu hỏi không tồn tại!"
            );

        Integer attemptAssignmentId = attempt
                .getAssignedAssignment()
                .getAssignment()
                .getId();

        Integer questionAssignmentId = question
                .getAssignment()
                .getId();

        if (!attemptAssignmentId.equals(questionAssignmentId))
            throw new IllegalArgumentException(
                    "Câu hỏi không thuộc bài tập đang thực hiện!"
            );
    }

    private void validateTime(AssignmentAttempt attempt) {
        if (attempt.getStartedAt() == null)
            throw new IllegalStateException(
                    "Bài làm chưa có thời gian bắt đầu!"
            );

        LocalDateTime now = LocalDateTime.now();
        AssignedAssignment assigned = attempt.getAssignedAssignment();

        if (assigned.getDueAt() != null
                && now.isAfter(assigned.getDueAt()))
            throw new IllegalArgumentException(
                    "Bài tập đã quá hạn nộp!"
            );

        Integer durationMinutes =
                assigned.getAssignment().getDurationMinutes();

        if (durationMinutes != null && durationMinutes > 0) {
            LocalDateTime endTime =
                    attempt.getStartedAt().plusMinutes(durationMinutes);

            if (now.isAfter(endTime))
                throw new IllegalArgumentException(
                        "Đã hết thời gian làm bài!"
                );
        }
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim()
                  .replaceAll("\\s+", " ")
                  .toLowerCase(Locale.ROOT);
    }
}