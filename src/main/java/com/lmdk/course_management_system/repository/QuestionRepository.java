package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.Question;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface QuestionRepository {

    Question getQuestionById(Integer id);

    Question addQuestion(Question question);

    void updateQuestion(Question question);

    List<Question> getQuestions(Map<String, String> params);

    List<Question> getQuestionsByAssignment(Integer assignmentId);

    long countQuestions(Map<String, String> params);

    boolean existsOrderNumber(Integer assignmentId, Integer orderNumber);

    boolean existsOrderNumberExceptId(
            Integer assignmentId,
            Integer orderNumber,
            Integer questionId
    );

    BigDecimal sumScoresByAssignment(Integer assignmentId);

    BigDecimal sumScoresByAssignmentExceptQuestion(
            Integer assignmentId,
            Integer questionId
    );
}