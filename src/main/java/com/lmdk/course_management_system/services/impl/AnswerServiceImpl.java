package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Answer;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.repository.AnswerRepository;
import com.lmdk.course_management_system.services.AnswerService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    @Override
    public Answer getAnswerById(Integer id) {
        return answerRepository.getAnswerById(id);
    }

    @Override
    public Answer addAnswer(Answer answer) {
        prepareAndValidate(answer);

        Integer questionId = answer.getQuestion().getId();

        if (answerRepository.existsOrderNumber(questionId, answer.getOrderNumber()))
            throw new IllegalArgumentException("Thứ tự đáp án đã tồn tại!");

        if (answer.getQuestion().getType() == Question.QuestionType.MULTIPLE_CHOICE
                && Boolean.TRUE.equals(answer.getCorrect())
                && answerRepository.countCorrectAnswersByQuestion(questionId) >= 1)
            throw new IllegalArgumentException(
                    "Câu hỏi trắc nghiệm chỉ được có một đáp án đúng!"
            );

        return answerRepository.addAnswer(answer);
    }

    @Override
    public void updateAnswer(Answer answer) {
        prepareAndValidate(answer);

        Integer questionId = answer.getQuestion().getId();

        if (answerRepository.existsOrderNumberExceptId(
                questionId,
                answer.getOrderNumber(),
                answer.getId()
        ))
            throw new IllegalArgumentException("Thứ tự đáp án đã tồn tại!");

        if (answer.getQuestion().getType() == Question.QuestionType.MULTIPLE_CHOICE
                && Boolean.TRUE.equals(answer.getCorrect())) {

            boolean hasOtherCorrectAnswer = answerRepository
                    .getCorrectAnswersByQuestion(questionId)
                    .stream()
                    .anyMatch(a -> !a.getId().equals(answer.getId()));

            if (hasOtherCorrectAnswer)
                throw new IllegalArgumentException(
                        "Câu hỏi trắc nghiệm chỉ được có một đáp án đúng!"
                );
        }

        answerRepository.updateAnswer(answer);
    }

    @Override
    public List<Answer> getAnswers(Map<String, String> params) {
        return answerRepository.getAnswers(params);
    }

    @Override
    public List<Answer> getAnswersByQuestion(Integer questionId) {
        return answerRepository.getAnswersByQuestion(questionId);
    }

    @Override
    public List<Answer> getCorrectAnswersByQuestion(Integer questionId) {
        return answerRepository.getCorrectAnswersByQuestion(questionId);
    }

    @Override
    public long countAnswers(Map<String, String> params) {
        return answerRepository.countAnswers(params);
    }

    private void prepareAndValidate(Answer answer) {
        if (answer.getQuestion() == null)
            throw new IllegalArgumentException("Vui lòng chọn câu hỏi!");

        if (answer.getContent() == null || answer.getContent().trim().isBlank())
            throw new IllegalArgumentException("Nội dung đáp án không được để trống!");

        if (answer.getOrderNumber() == null || answer.getOrderNumber() < 1)
            throw new IllegalArgumentException("Thứ tự đáp án phải lớn hơn 0!");

        switch (answer.getQuestion().getType()) {
            case MULTIPLE_CHOICE -> answer.setType(Answer.AnswerType.CHOICE);

            case SHORT_ANSWER -> {
                answer.setType(Answer.AnswerType.SHORT_ANSWER);
                answer.setCorrect(true);
            }

            case ESSAY -> {
                answer.setType(Answer.AnswerType.REFERENCE_ANSWER);
                answer.setCorrect(false);
            }
        }

        answer.setContent(answer.getContent().trim());
    }
}