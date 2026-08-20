package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.LearningPathDetail;

import java.util.List;
import java.util.Map;

public interface LearningPathDetailRepository {

    LearningPathDetail getDetailById(Integer id);

    LearningPathDetail addDetail(LearningPathDetail detail);

    void updateDetail(LearningPathDetail detail);

    List<LearningPathDetail> getDetails(Map<String, String> params);

    List<LearningPathDetail> getDetailsByLearningPath(Integer learningPathId);

    long countDetails(Map<String, String> params);

    boolean existsOrderNumber(Integer learningPathId, Integer orderNumber);

    boolean existsOrderNumberExceptId(Integer learningPathId, Integer orderNumber, Integer detailId);

    boolean existsAssignment(Integer learningPathId, Integer assignmentId);

    boolean existsAssignmentExceptId(Integer learningPathId, Integer assignmentId, Integer detailId);

    LearningPathDetail getNextDetail(Integer learningPathId, Integer currentOrderNumber);
}