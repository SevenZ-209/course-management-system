package com.lmdk.course_management_system.mappers.common;

import com.lmdk.course_management_system.dto.enrollment.EnrollmentResponse;
import com.lmdk.course_management_system.pojo.Enrollment;

import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(
            Enrollment enrollment
    ) {

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getCourseClass().getId(),
                enrollment.getCourseClass().getName(),
                enrollment.getCourseClass()
                        .getCourse()
                        .getId(),
                enrollment.getCourseClass()
                        .getCourse()
                        .getName(),
                enrollment.getStatus().name(),
                enrollment.getCreatedAt()
        );
    }
}