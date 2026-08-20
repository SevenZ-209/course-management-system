package com.lmdk.course_management_system.mappers.parent;

import com.lmdk.course_management_system.dto.parent.ParentContinueAssignmentResponse;
import com.lmdk.course_management_system.dto.parent.ParentDashboardResponse;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ParentDashboardMapper {

    public ParentContinueAssignmentResponse toContinueAssignmentResponse(
            AssignedAssignment assigned,
            AssignmentAttempt attempt
    ) {
        return new ParentContinueAssignmentResponse(
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

    public ParentDashboardResponse toResponse(
            User student,

            Integer activeCourses,

            Integer inProgressLearningPaths,
            Integer completedLearningPaths,

            Integer inProgressAssignments,
            Integer pendingGradingAssignments,

            Integer presentSessions,
            Integer absentSessions,
            Integer notMarkedSessions,

            BigDecimal latestScore,

            ParentContinueAssignmentResponse continueAssignment
    ) {
        return new ParentDashboardResponse(
                student.getId(),
                student.getFullName(),

                activeCourses,

                inProgressLearningPaths,
                completedLearningPaths,

                inProgressAssignments,
                pendingGradingAssignments,

                presentSessions,
                absentSessions,
                notMarkedSessions,

                latestScore,

                continueAssignment
        );
    }
}