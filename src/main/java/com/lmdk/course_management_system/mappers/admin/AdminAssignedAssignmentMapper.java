package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.learningpath.AdminAssignedAssignmentResponse;
import com.lmdk.course_management_system.pojo.AssignedAssignment;

import org.springframework.stereotype.Component;

@Component
public class AdminAssignedAssignmentMapper {

    public AdminAssignedAssignmentResponse toResponse(
            AssignedAssignment assigned
    ) {
        var student = assigned.getStudent();
        var assignment = assigned.getAssignment();
        var course = assignment.getCourse();
        var detail = assigned.getLearningPathDetail();
        var assignedBy = assigned.getAssignedBy();

        return new AdminAssignedAssignmentResponse(
                assigned.getId(),

                student.getId(),
                student.getFullName(),
                student.getUsername(),

                assignment.getId(),
                assignment.getName(),

                course.getId(),
                course.getName(),

                detail != null ? detail.getId() : null,
                detail != null
                        ? detail.getLearningPath().getId()
                        : null,
                detail != null
                        ? detail.getLearningPath().getName()
                        : null,
                detail != null
                        ? detail.getOrderNumber()
                        : null,

                assignedBy != null
                        ? assignedBy.getId()
                        : null,
                assignedBy != null
                        ? assignedBy.getFullName()
                        : null,

                assigned.getAssignedAt(),
                assigned.getAvailableAt(),
                assigned.getDueAt(),

                assigned.getStatus().name()
        );
    }
}