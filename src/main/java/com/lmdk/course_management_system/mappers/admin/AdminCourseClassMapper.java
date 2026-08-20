package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.courseclass.AdminCourseClassResponse;
import com.lmdk.course_management_system.pojo.CourseClass;

import org.springframework.stereotype.Component;

@Component
public class AdminCourseClassMapper {

    public AdminCourseClassResponse toResponse(
            CourseClass courseClass
    ) {
        return new AdminCourseClassResponse(
                courseClass.getId(),
                courseClass.getName(),

                courseClass.getCourse().getId(),
                courseClass.getCourse().getName(),

                courseClass.getTeacher() == null
                        ? null
                        : courseClass.getTeacher().getId(),

                courseClass.getTeacher() == null
                        ? null
                        : courseClass.getTeacher().getFullName(),

                courseClass.getStartDate(),
                courseClass.getEndDate(),
                courseClass.getMaxStudents(),

                courseClass.getStatus().name()
        );
    }
}