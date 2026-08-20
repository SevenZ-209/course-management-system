package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.repository.QuestionRepository;
import com.lmdk.course_management_system.services.QuestionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public Question getQuestionById(Integer id) {
        return questionRepository.getQuestionById(id);
    }

    @Override
    public Question addQuestion(Question question) {
        validateQuestion(question);

        if (question.getAssignment().getStatus() != Assignment.AssignmentStatus.ACTIVE)
            throw new IllegalArgumentException("Bài tập đang ngừng hoạt động!");

        Integer assignmentId = question.getAssignment().getId();

        if (questionRepository.existsOrderNumber(
                assignmentId,
                question.getOrderNumber()
        ))
            throw new IllegalArgumentException("Thứ tự câu hỏi đã tồn tại!");

        BigDecimal currentTotal =
                questionRepository.sumScoresByAssignment(assignmentId);

        BigDecimal newTotal =
                currentTotal.add(question.getScore());

        if (newTotal.compareTo(
                question.getAssignment().getMaximumScore()
        ) > 0)
            throw new IllegalArgumentException(
                    "Tổng điểm các câu hỏi không được vượt quá điểm tối đa của bài tập!"
            );

        return questionRepository.addQuestion(question);
    }

    @Override
    public void updateQuestion(Question question) {
        validateQuestion(question);

        Integer assignmentId = question.getAssignment().getId();

        if (questionRepository.existsOrderNumberExceptId(
                assignmentId,
                question.getOrderNumber(),
                question.getId()
        ))
            throw new IllegalArgumentException("Thứ tự câu hỏi đã tồn tại!");

        BigDecimal currentTotal =
                questionRepository.sumScoresByAssignmentExceptQuestion(
                        assignmentId,
                        question.getId()
                );

        BigDecimal newTotal =
                currentTotal.add(question.getScore());

        if (newTotal.compareTo(
                question.getAssignment().getMaximumScore()
        ) > 0)
            throw new IllegalArgumentException(
                    "Tổng điểm các câu hỏi không được vượt quá điểm tối đa của bài tập!"
            );

        questionRepository.updateQuestion(question);
    }

    @Override
    public List<Question> getQuestions(Map<String, String> params) {
        return questionRepository.getQuestions(params);
    }

    @Override
    public List<Question> getQuestionsByAssignment(Integer assignmentId) {
        return questionRepository.getQuestionsByAssignment(assignmentId);
    }

    @Override
    public long countQuestions(Map<String, String> params) {
        return questionRepository.countQuestions(params);
    }

    @Override
    public BigDecimal sumScoresByAssignment(Integer assignmentId) {
        return questionRepository.sumScoresByAssignment(assignmentId);
    }

    private void validateQuestion(Question question) {
        if (question.getAssignment() == null)
            throw new IllegalArgumentException("Vui lòng chọn bài tập!");

        if (question.getContent() == null || question.getContent().trim().isBlank())
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống!");

        if (question.getType() == null)
            throw new IllegalArgumentException("Vui lòng chọn loại câu hỏi!");

        if (question.getScore() == null
                || question.getScore().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Điểm câu hỏi phải lớn hơn 0!");

        if (question.getScore().compareTo(
                question.getAssignment().getMaximumScore()
        ) > 0)
            throw new IllegalArgumentException(
                    "Điểm câu hỏi không được lớn hơn điểm tối đa của bài tập!"
            );

        if (question.getOrderNumber() == null || question.getOrderNumber() < 1)
            throw new IllegalArgumentException("Thứ tự câu hỏi phải lớn hơn 0!");

        question.setContent(question.getContent().trim());
    }
}