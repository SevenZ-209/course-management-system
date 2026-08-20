package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.CourseModule;

import java.util.List;
import java.util.Map;

public interface CourseModuleService {

    CourseModule getModuleById(Integer id);

    CourseModule addModule(CourseModule module);

    void updateModule(CourseModule module);

    List<CourseModule> getModules(Map<String, String> params);

    List<CourseModule> getModulesByCourse(Integer courseId);

    long countModules(Map<String, String> params);

    List<CourseModule> getAllModules();

}