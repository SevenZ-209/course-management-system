package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.GradingResult;

import java.util.List;
import java.util.Map;

public interface GradingResultRepository {

    GradingResult getGradingResultById(Integer id);

    GradingResult getGradingResultByAttempt(Integer attemptId);

    List<GradingResult> getGradingResultsByAttemptIds(List<Integer> attemptIds);

    GradingResult addGradingResult(GradingResult gradingResult);

    void updateGradingResult(GradingResult gradingResult);

    List<GradingResult> getGradingResults(Map<String, String> params);

    long countGradingResults(Map<String, String> params);

    boolean existsByAttempt(Integer attemptId);
}