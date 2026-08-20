package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.coursemodule.AdminCourseModuleResponse;
import com.lmdk.course_management_system.pojo.CourseModule;

import org.springframework.stereotype.Component;

@Component
public class AdminCourseModuleMapper {

    public AdminCourseModuleResponse toResponse(CourseModule module) {
        return new AdminCourseModuleResponse(
                module.getId(),
                module.getName(),
                module.getCourse().getId(),
                module.getCourse().getName(),
                module.getOrderNumber(),
                module.getStatus().name()
        );
    }
}