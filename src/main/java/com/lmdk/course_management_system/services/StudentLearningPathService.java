package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.dto.student.assignment.CourseAssignmentResponse;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.pojo.User;

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

    List<StudentLearningPath> getStudentLearningPathsByStudentsAndCourse(List<Integer> studentIds, Integer courseId);

    List<StudentLearningPath> getStudentLearningPathsByLearningPath(Integer learningPathId);

    long countStudentLearningPaths(Map<String, String> params);

    List<StudentLearningPath> getInProgressStudentLearningPaths();

    StudentLearningPath advanceAfterPassedDetail(
            Integer studentId,
            Integer learningPathDetailId
    );

    Assignment getCurrentAssignment(Integer studentId, Integer courseId);

    void createStudentLearningPath(User student, Course course);

    boolean canAccessLesson(Integer studentId, Integer lessonId);

    List<CourseAssignmentResponse> getCourseAssignments(
            Integer studentId,
            Integer courseId
    );


}