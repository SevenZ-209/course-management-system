package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.assignment.ContinueAssignmentResponse;
import com.lmdk.course_management_system.dto.student.dashboard.StudentDashboardResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.student.StudentDashboardMapper;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.AssignedAssignmentService;
import com.lmdk.course_management_system.services.AssignmentAttemptService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.StudentLearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/dashboard")
@RequiredArgsConstructor
public class ApiStudentDashboardController {

    private final EnrollmentService enrollmentService;
    private final StudentLearningPathService studentLearningPathService;
    private final AssignedAssignmentService assignedAssignmentService;
    private final AssignmentAttemptService assignmentAttemptService;
    private final CurrentUserHelper currentUserHelper;
    private final StudentDashboardMapper studentDashboardMapper;

    @GetMapping
    public StudentDashboardResponse getDashboard(
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(
                        authentication
                );

        Integer activeCourses =
                enrollmentService
                        .getActiveEnrollmentsByStudent(
                                student.getId()
                        )
                        .size();

        List<StudentLearningPath> learningPaths =
                studentLearningPathService
                        .getStudentLearningPathsByStudent(
                                student.getId()
                        );

        Integer completedLearningPaths =
                (int) learningPaths.stream()
                        .filter(path ->
                                path.getStatus()
                                        == StudentLearningPath
                                        .ProgressStatus.COMPLETED
                        )
                        .count();

        List<AssignedAssignment> assignments =
                assignedAssignmentService
                        .getAssignedAssignmentsByStudent(
                                student.getId()
                        );

        Map<Integer, AssignmentAttempt> latestAttempts = assignmentAttemptService
                .getLatestAttemptsByAssignedAssignmentIds(
                        assignments.stream().map(AssignedAssignment::getId).toList()
                );

        List<AssignmentWithAttempt> assignmentAttempts =
                assignments.stream()
                        .map(assigned -> new AssignmentWithAttempt(
                                assigned,
                                latestAttempts.get(assigned.getId())
                        ))
                        .toList();

        Integer inProgressAssignments =
                (int) assignmentAttempts.stream()
                        .filter(item ->
                                item.attempt() != null
                                        && item.attempt()
                                        .getStatus()
                                        == AssignmentAttempt
                                        .AttemptStatus.IN_PROGRESS
                        )
                        .count();

        Integer pendingGradingAssignments =
                (int) assignmentAttempts.stream()
                        .filter(item ->
                                item.attempt() != null
                                        && (
                                        item.attempt().getStatus()
                                                == AssignmentAttempt
                                                .AttemptStatus.SUBMITTED
                                                ||
                                                item.attempt().getStatus()
                                                        == AssignmentAttempt
                                                        .AttemptStatus.PENDING_GRADING
                                )
                        )
                        .count();

        ContinueAssignmentResponse continueAssignment =
                assignmentAttempts.stream()
                        .filter(item ->
                                item.attempt() != null
                                        && item.attempt()
                                        .getStatus()
                                        == AssignmentAttempt
                                        .AttemptStatus.IN_PROGRESS
                        )
                        .max(
                                Comparator.comparing(
                                        item ->
                                                item.attempt()
                                                        .getStartedAt()
                                )
                        )
                        .map(item ->
                                studentDashboardMapper
                                        .toContinueAssignmentResponse(
                                                item.assigned(),
                                                item.attempt()
                                        )
                        )
                        .orElse(null);

        return studentDashboardMapper.toResponse(
                activeCourses,
                inProgressAssignments,
                pendingGradingAssignments,
                completedLearningPaths,
                continueAssignment
        );
    }

    private record AssignmentWithAttempt(
            AssignedAssignment assigned,
            AssignmentAttempt attempt
    ) {
    }
}