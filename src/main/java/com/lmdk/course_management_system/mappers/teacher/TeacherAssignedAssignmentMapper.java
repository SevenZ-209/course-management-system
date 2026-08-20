package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.assignment.TeacherAssignedAssignmentResponse;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

@Component
public class TeacherAssignedAssignmentMapper {

    public TeacherAssignedAssignmentResponse toResponse(
            AssignedAssignment assigned,
            AssignmentAttempt latest
    ) {
        Assignment assignment =
                assigned.getAssignment();

        User student =
                assigned.getStudent();

        User assignedBy =
                assigned.getAssignedBy();

        return new TeacherAssignedAssignmentResponse(
                assigned.getId(),

                assignment.getId(),
                assignment.getName(),
                assignment.getType().name(),
                assignment.getMaximumScore(),

                student.getId(),
                student.getFullName(),
                student.getUsername(),

                assigned.getStatus().name(),

                assigned.getAssignedAt(),
                assigned.getAvailableAt(),
                assigned.getDueAt(),

                assigned.getLearningPathDetail() == null
                        ? null
                        : assigned.getLearningPathDetail().getId(),

                assigned.getLearningPathDetail() == null
                        ? "MANUAL"
                        : "LEARNING_PATH",

                assignedBy == null
                        ? null
                        : assignedBy.getId(),

                assignedBy == null
                        ? null
                        : assignedBy.getFullName(),

                latest == null
                        ? null
                        : latest.getId(),

                latest == null
                        ? null
                        : latest.getAttemptNumber(),

                latest == null
                        ? null
                        : latest.getStatus().name()
        );
    }
}