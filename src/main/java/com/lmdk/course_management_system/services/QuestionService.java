package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.Question;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface QuestionService {

    Question getQuestionById(Integer id);

    Question addQuestion(Question question);

    void updateQuestion(Question question);

    List<Question> getQuestions(Map<String, String> params);

    List<Question> getQuestionsByAssignment(Integer assignmentId);

    long countQuestions(Map<String, String> params);

    BigDecimal sumScoresByAssignment(Integer assignmentId);
}