package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.EnrollmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AssignedAssignmentHelper {

    private final EnrollmentService enrollmentService;

    public boolean canStart(
            AssignedAssignment assigned,
            AssignmentAttempt latest
    ) {
        if(assigned.getStatus() == AssignedAssignment.AssignedStatus.COMPLETED)
            return false;

        Integer studentId = assigned.getStudent().getId();
        Integer courseId = assigned.getAssignment().getCourse().getId();

        if(!enrollmentService.existsActiveEnrollmentByStudentAndCourse(
                studentId,
                courseId
        ))
            return false;

        LocalDateTime now = LocalDateTime.now();

        if(assigned.getAvailableAt() != null
                && now.isBefore(assigned.getAvailableAt()))
            return false;

        if(assigned.getDueAt() != null
                && now.isAfter(assigned.getDueAt()))
            return false;

        if(latest == null)
            return true;

        if(latest.getStatus() == AssignmentAttempt.AttemptStatus.IN_PROGRESS)
            return true;

        if(latest.getStatus() == AssignmentAttempt.AttemptStatus.SUBMITTED
                || latest.getStatus() == AssignmentAttempt.AttemptStatus.PENDING_GRADING)
            return false;

        if(latest.getStatus() == AssignmentAttempt.AttemptStatus.GRADED
                && Boolean.TRUE.equals(latest.getPassed()))
            return false;

        LearningPathDetail detail = assigned.getLearningPathDetail();

        if(detail == null)
            return true;

        return latest.getAttemptNumber() < detail.getMaxAttempts();
    }
}