package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.Answer;

import java.util.List;
import java.util.Map;

public interface AnswerRepository {

    Answer getAnswerById(Integer id);

    Answer addAnswer(Answer answer);

    void updateAnswer(Answer answer);

    List<Answer> getAnswers(Map<String, String> params);

    List<Answer> getAnswersByQuestion(Integer questionId);

    List<Answer> getCorrectAnswersByQuestion(Integer questionId);

    long countAnswers(Map<String, String> params);

    long countAnswersByQuestion(Integer questionId);

    long countCorrectAnswersByQuestion(Integer questionId);

    boolean existsOrderNumber(Integer questionId, Integer orderNumber);

    boolean existsOrderNumberExceptId(
            Integer questionId,
            Integer orderNumber,
            Integer answerId
    );

    void deleteByQuestionId(Integer questionId);
}