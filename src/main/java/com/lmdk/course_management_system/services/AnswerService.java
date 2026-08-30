package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.dto.admin.answer.BulkAnswerRequest;
import com.lmdk.course_management_system.pojo.Answer;

import java.util.List;
import java.util.Map;

public interface AnswerService {

    Answer getAnswerById(Integer id);

    Answer addAnswer(Answer answer);

    void updateAnswer(Answer answer);

    List<Answer> getAnswers(Map<String, String> params);

    List<Answer> getAnswersByQuestion(Integer questionId);

    List<Answer> getCorrectAnswersByQuestion(Integer questionId);

    long countAnswers(Map<String, String> params);

    void addBulk(BulkAnswerRequest request);
}