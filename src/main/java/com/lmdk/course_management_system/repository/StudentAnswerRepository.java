package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.StudentAnswer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StudentAnswerRepository {

    StudentAnswer getStudentAnswerById(Integer id);

    StudentAnswer getStudentAnswer(Integer attemptId, Integer questionId);

    StudentAnswer addStudentAnswer(StudentAnswer studentAnswer);

    void updateStudentAnswer(StudentAnswer studentAnswer);

    List<StudentAnswer> getStudentAnswers(Map<String, String> params);

    List<StudentAnswer> getStudentAnswersByAttempt(Integer attemptId);

    long countStudentAnswers(Map<String, String> params);

    long countStudentAnswersByAttempt(Integer attemptId);

    boolean existsStudentAnswer(Integer attemptId, Integer questionId);

    BigDecimal sumScoreByAttempt(Integer attemptId);
}