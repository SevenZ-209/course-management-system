package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.assignment.AdminAssignmentResponse;
import com.lmdk.course_management_system.pojo.Assignment;

import org.springframework.stereotype.Component;

@Component
public class AdminAssignmentMapper {

    public AdminAssignmentResponse toResponse(
            Assignment assignment
    ) {
        return new AdminAssignmentResponse(
                assignment.getId(),
                assignment.getName(),

                assignment.getCourse().getId(),
                assignment.getCourse().getName(),

                assignment.getLesson() != null
                        ? assignment.getLesson().getId()
                        : null,

                assignment.getLesson() != null
                        ? assignment.getLesson().getName()
                        : null,

                assignment.getType().name(),
                assignment.getMaximumScore(),
                assignment.getDurationMinutes(),
                assignment.getStatus().name()
        );
    }
}