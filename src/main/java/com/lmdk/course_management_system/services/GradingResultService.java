package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.GradingResult;
import com.lmdk.course_management_system.pojo.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GradingResultService {

    GradingResult getGradingResultById(Integer id);

    GradingResult getGradingResultByAttempt(Integer attemptId);

    Map<Integer, GradingResult> getGradingResultsByAttemptIds(List<Integer> attemptIds);

    GradingResult processSubmittedAttempt(Integer attemptId);

    GradingResult submitAndProcessAttempt(Integer attemptId, Integer studentId);

    void gradeEssayAnswer(
            Integer studentAnswerId,
            BigDecimal score,
            String teacherComment,
            User teacher
    );

    GradingResult finalizeGrading(
            Integer attemptId,
            User teacher,
            String comment
    );

    List<GradingResult> getGradingResults(Map<String, String> params);

    long countGradingResults(Map<String, String> params);

    List<AssignmentAttempt> getPendingAttempts(User grader);

    List<AssignmentAttempt> getPendingAttempts(User grader, Map<String, String> params);

    long countPendingAttempts(User grader, Map<String, String> params);

    AssignmentAttempt getAttemptForGrading(Integer attemptId, User grader);
}