package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.StudentAnswer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StudentAnswerService {

    StudentAnswer getStudentAnswerById(Integer id);

    StudentAnswer getStudentAnswer(Integer attemptId, Integer questionId);

    StudentAnswer saveAnswer(
            Integer attemptId,
            Integer studentId,
            Integer questionId,
            Integer selectedAnswerId,
            String answerContent
    );

    List<StudentAnswer> getStudentAnswers(Map<String, String> params);

    List<StudentAnswer> getStudentAnswersByAttempt(Integer attemptId);

    long countStudentAnswers(Map<String, String> params);

    long countStudentAnswersByAttempt(Integer attemptId);

    BigDecimal sumScoreByAttempt(Integer attemptId);
}