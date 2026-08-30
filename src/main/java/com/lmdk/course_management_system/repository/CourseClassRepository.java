package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.CourseClass;

import java.util.List;
import java.util.Map;

public interface CourseClassRepository {

    CourseClass getClassById(Integer id);

    CourseClass getClassByIdForUpdate(Integer id);

    CourseClass addClass(CourseClass courseClass);

    void updateClass(CourseClass courseClass);

    List<CourseClass> getClasses(Map<String, String> params);

    List<CourseClass> getClassesByCourse(Integer courseId);

    List<CourseClass> getAllClasses();

    long countClasses(Map<String, String> params);

    List<CourseClass> getClassesByTeacher(Integer teacherId);
}