package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.StudentLearningPath;

import java.util.List;
import java.util.Map;

public interface StudentLearningPathRepository {

    StudentLearningPath getStudentLearningPathById(Integer id);

    StudentLearningPath getStudentLearningPath(Integer studentId, Integer learningPathId);

    StudentLearningPath getStudentLearningPathForUpdate(Integer studentId, Integer learningPathId);

    StudentLearningPath addStudentLearningPath(StudentLearningPath studentLearningPath);

    void updateStudentLearningPath(StudentLearningPath studentLearningPath);

    List<StudentLearningPath> getStudentLearningPaths(Map<String, String> params);

    List<StudentLearningPath> getStudentLearningPathsByStudent(Integer studentId);

    List<StudentLearningPath> getStudentLearningPathsByStudentsAndCourse(List<Integer> studentIds, Integer courseId);

    List<StudentLearningPath> getStudentLearningPathsByStudentsAndCourses(List<Integer> studentIds, List<Integer> courseIds);

    List<StudentLearningPath> getStudentLearningPathsByLearningPath(Integer learningPathId);

    long countStudentLearningPaths(Map<String, String> params);

    boolean existsStudentLearningPath(Integer studentId, Integer learningPathId);

    List<StudentLearningPath> getInProgressStudentLearningPaths();
}