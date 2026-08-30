package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.course.AdminCourseResponse;
import com.lmdk.course_management_system.pojo.Course;
import org.springframework.stereotype.Component;

@Component
public class AdminCourseMapper {

    public AdminCourseResponse toResponse(Course course) {
        return new AdminCourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getTuitionFee(),
                course.getImageUrl(),
                course.getCategory().getId(),
                course.getCategory().getName(),
                course.getStatus().name()
        );
    }
}