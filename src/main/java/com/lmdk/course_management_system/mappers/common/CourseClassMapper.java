package com.lmdk.course_management_system.mappers.common;

import com.lmdk.course_management_system.dto.student.classinfo.ClassResponse;
import com.lmdk.course_management_system.pojo.CourseClass;
import org.springframework.stereotype.Component;

@Component
public class CourseClassMapper {

    public ClassResponse toResponse(CourseClass courseClass) {
        return new ClassResponse(
                courseClass.getId(),
                courseClass.getName(),
                courseClass.getCourse().getId(),
                courseClass.getCourse().getName(),
                courseClass.getStartDate(),
                courseClass.getEndDate(),
                courseClass.getMaxStudents(),
                courseClass.getStatus().name()
        );
    }
}