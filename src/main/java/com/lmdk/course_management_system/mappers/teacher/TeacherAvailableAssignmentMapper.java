package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.assignment.TeacherAvailableAssignmentResponse;
import com.lmdk.course_management_system.pojo.Assignment;

import org.springframework.stereotype.Component;

@Component
public class TeacherAvailableAssignmentMapper {

    public TeacherAvailableAssignmentResponse toResponse(
            Assignment assignment
    ) {
        return new TeacherAvailableAssignmentResponse(
                assignment.getId(),
                assignment.getName(),
                assignment.getType().name(),
                assignment.getMaximumScore(),
                assignment.getDurationMinutes()
        );
    }
}