package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.StudentLearningPath;

import java.util.List;
import java.util.Map;

public interface StudentLearningPathService {

    StudentLearningPath getStudentLearningPathById(Integer id);

    StudentLearningPath getStudentLearningPath(Integer studentId, Integer learningPathId);

    StudentLearningPath assignLearningPath(Integer studentId, Integer learningPathId);

    void pauseLearningPath(Integer id);

    void resumeLearningPath(Integer id);

    List<StudentLearningPath> getStudentLearningPaths(Map<String, String> params);

    List<StudentLearningPath> getStudentLearningPathsByStudent(Integer studentId);

    List<StudentLearningPath> getStudentLearningPathsByLearningPath(Integer learningPathId);

    long countStudentLearningPaths(Map<String, String> params);

    List<StudentLearningPath> getInProgressStudentLearningPaths();

    StudentLearningPath advanceAfterPassedDetail(
            Integer studentId,
            Integer learningPathDetailId
    );
}