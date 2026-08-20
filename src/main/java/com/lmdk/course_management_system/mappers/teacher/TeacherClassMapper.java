package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherClassResponse;
import com.lmdk.course_management_system.pojo.CourseClass;

import org.springframework.stereotype.Component;

@Component
public class TeacherClassMapper {

    public TeacherClassResponse toResponse(
            CourseClass courseClass,
            Integer studentCount
    ) {
        return new TeacherClassResponse(
                courseClass.getId(),
                courseClass.getName(),

                courseClass.getCourse().getId(),
                courseClass.getCourse().getName(),

                courseClass.getStartDate(),
                courseClass.getEndDate(),

                courseClass.getMaxStudents(),
                studentCount,

                courseClass.getStatus().name()
        );
    }
}