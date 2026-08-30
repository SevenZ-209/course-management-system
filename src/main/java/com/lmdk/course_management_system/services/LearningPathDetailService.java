package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.LearningPathDetail;

import java.util.List;
import java.util.Map;

public interface LearningPathDetailService {

    LearningPathDetail getDetailById(Integer id);

    LearningPathDetail addDetail(LearningPathDetail detail);

    void updateDetail(LearningPathDetail detail);

    List<LearningPathDetail> getDetails(Map<String, String> params);

    List<LearningPathDetail> getDetailsByLearningPath(Integer learningPathId);

    List<LearningPathDetail> getDetailsByLearningPaths(List<Integer> learningPathIds);

    long countDetails(Map<String, String> params);

    LearningPathDetail getNextDetail(Integer learningPathId, Integer currentOrderNumber);

    LearningPathDetail getFirstDetail(Integer learningPathId);
}