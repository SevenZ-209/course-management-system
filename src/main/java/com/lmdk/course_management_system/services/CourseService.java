package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.Course;

import java.util.List;
import java.util.Map;

public interface CourseService {

    Course getCourseById(Integer id);

    Course addCourse(Course course);

    void updateCourse(Course course);

    List<Course> getCourses(Map<String, String> params);

    List<Course> getAllCourses();

    long countCourses(Map<String, String> params);
}