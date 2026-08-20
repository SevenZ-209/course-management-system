package com.lmdk.course_management_system.mappers.parent;

import com.lmdk.course_management_system.dto.parent.ParentAssignmentResponse;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.GradingResult;

import org.springframework.stereotype.Component;

@Component
public class ParentAssignmentMapper {

    public ParentAssignmentResponse toResponse(
            AssignedAssignment assigned,
            AssignmentAttempt latest,
            GradingResult result
    ) {
        Assignment assignment =
                assigned.getAssignment();

        boolean graded =
                latest != null
                        && latest.getStatus()
                        == AssignmentAttempt.AttemptStatus.GRADED;

        return new ParentAssignmentResponse(
                assigned.getId(),

                assignment.getId(),
                assignment.getName(),
                assignment.getType().name(),

                assignment.getCourse().getId(),
                assignment.getCourse().getName(),

                assignment.getMaximumScore(),

                assigned.getStatus().name(),

                assigned.getAssignedAt(),
                assigned.getAvailableAt(),
                assigned.getDueAt(),

                assigned.getLearningPathDetail() == null
                        ? "MANUAL"
                        : "LEARNING_PATH",

                latest == null
                        ? null
                        : latest.getId(),

                latest == null
                        ? null
                        : latest.getAttemptNumber(),

                latest == null
                        ? null
                        : latest.getStatus().name(),

                latest == null
                        ? null
                        : latest.getSubmittedAt(),

                result == null
                        ? null
                        : result.getAutoScore(),

                result == null
                        ? null
                        : result.getEssayScore(),

                graded && result != null
                        ? result.getTotalScore()
                        : null,

                graded
                        ? latest.getPassed()
                        : null
        );
    }
}