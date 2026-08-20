package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.LearningPath;

import java.util.List;
import java.util.Map;

public interface LearningPathService {

    LearningPath getLearningPathById(Integer id);

    LearningPath addLearningPath(LearningPath learningPath);

    void updateLearningPath(LearningPath learningPath);

    List<LearningPath> getLearningPaths(Map<String, String> params);

    List<LearningPath> getLearningPathsByCourse(Integer courseId);

    List<LearningPath> getAllLearningPaths();

    long countLearningPaths(Map<String, String> params);
}