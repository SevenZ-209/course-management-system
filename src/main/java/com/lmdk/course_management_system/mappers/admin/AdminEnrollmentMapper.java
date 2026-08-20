package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.enrollment.AdminEnrollmentResponse;
import com.lmdk.course_management_system.dto.admin.enrollment.AdminPendingEnrollmentOptionResponse;
import com.lmdk.course_management_system.pojo.Enrollment;

import org.springframework.stereotype.Component;

@Component
public class AdminEnrollmentMapper {

    public AdminEnrollmentResponse toResponse(Enrollment enrollment) {
        var student = enrollment.getStudent();
        var courseClass = enrollment.getCourseClass();
        var course = courseClass.getCourse();

        return new AdminEnrollmentResponse(
                enrollment.getId(),
                student.getId(),
                student.getFullName(),
                student.getUsername(),
                courseClass.getId(),
                courseClass.getName(),
                course.getId(),
                course.getName(),
                enrollment.getStatus().name(),
                enrollment.getCreatedAt()
        );
    }

    public AdminPendingEnrollmentOptionResponse toPendingOption(
            Enrollment enrollment
    ) {
        var student = enrollment.getStudent();
        var courseClass = enrollment.getCourseClass();
        var course = courseClass.getCourse();

        return new AdminPendingEnrollmentOptionResponse(
                enrollment.getId(),
                student.getId(),
                student.getFullName(),
                courseClass.getId(),
                courseClass.getName(),
                course.getId(),
                course.getName(),
                course.getTuitionFee()
        );
    }
}