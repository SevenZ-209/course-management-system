package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.assignment.ContinueAssignmentResponse;
import com.lmdk.course_management_system.dto.student.dashboard.StudentDashboardResponse;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;

import org.springframework.stereotype.Component;

@Component
public class StudentDashboardMapper {

    public ContinueAssignmentResponse toContinueAssignmentResponse(
            AssignedAssignment assigned,
            AssignmentAttempt attempt
    ) {

        return new ContinueAssignmentResponse(
                assigned.getId(),

                assigned.getAssignment().getId(),
                assigned.getAssignment().getName(),

                assigned.getAssignment()
                        .getCourse()
                        .getId(),

                assigned.getAssignment()
                        .getCourse()
                        .getName(),

                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getStatus().name()
        );
    }

    public StudentDashboardResponse toResponse(
            Integer activeCourses,
            Integer inProgressAssignments,
            Integer pendingGradingAssignments,
            Integer completedLearningPaths,
            ContinueAssignmentResponse continueAssignment
    ) {

        return new StudentDashboardResponse(
                activeCourses,
                inProgressAssignments,
                pendingGradingAssignments,
                completedLearningPaths,
                continueAssignment
        );
    }
}