package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.dto.admin.answer.BulkAnswerRequest;
import com.lmdk.course_management_system.pojo.Answer;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.repository.AnswerRepository;
import com.lmdk.course_management_system.services.AnswerService;

import com.lmdk.course_management_system.services.QuestionService;
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
    private final QuestionService questionService;

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
    @Transactional
    public void addBulk(BulkAnswerRequest request) {

        Question question = questionService.getQuestionById(
                request.getQuestionId()
        );

        if(question == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy câu hỏi!"
            );

        int correctCount = 0;

        for(var item : request.getAnswers()) {

            if(Boolean.TRUE.equals(item.getCorrect()))
                correctCount++;

        }

        if(question.getType() == Question.QuestionType.MULTIPLE_CHOICE
                && correctCount != 1)

            throw new IllegalArgumentException(
                    "Câu hỏi trắc nghiệm phải có đúng 1 đáp án đúng!"
            );

        answerRepository.deleteByQuestionId(
                question.getId()
        );

        for(var item : request.getAnswers()) {

            Answer answer = new Answer();

            answer.setQuestion(question);
            answer.setContent(item.getContent());
            answer.setCorrect(item.getCorrect());
            answer.setOrderNumber(item.getOrderNumber());

            addAnswer(answer);

        }

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