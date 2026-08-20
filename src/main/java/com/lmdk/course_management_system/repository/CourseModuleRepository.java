package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.CourseModule;

import java.util.List;
import java.util.Map;

public interface CourseModuleRepository {

    CourseModule getModuleById(Integer id);

    CourseModule addModule(CourseModule module);

    void updateModule(CourseModule module);

    List<CourseModule> getModules(Map<String, String> params);

    List<CourseModule> getModulesByCourse(Integer courseId);

    long countModules(Map<String, String> params);

    boolean existsOrderNumber(Integer courseId, Integer orderNumber);

    boolean existsOrderNumberExceptId(Integer courseId, Integer orderNumber, Integer moduleId);

    List<CourseModule> getAllModules();
}