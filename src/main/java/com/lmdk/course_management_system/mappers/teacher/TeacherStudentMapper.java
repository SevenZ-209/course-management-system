package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentResponse;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

@Component
public class TeacherStudentMapper {

    public TeacherStudentResponse toResponse(
            Enrollment enrollment
    ) {
        User student = enrollment.getStudent();

        return new TeacherStudentResponse(
                enrollment.getId(),
                student.getId(),
                student.getFullName(),
                student.getUsername(),
                enrollment.getStatus().name(),
                enrollment.getCreatedAt()
        );
    }
}