package com.lmdk.course_management_system.controllers.api.parent;

import com.lmdk.course_management_system.dto.parent.*;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.helpers.ParentAccessHelper;
import com.lmdk.course_management_system.mappers.parent.*;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ApiParentController {

    private final ParentLinkService parentLinkService;
    private final CurrentUserHelper currentUserHelper;
    private final ParentLinkMapper parentLinkMapper;
    private final ParentAccessHelper parentAccessHelper;
    private final EnrollmentService enrollmentService;
    private final StudentLearningPathService studentLearningPathService;
    private final LearningPathDetailService learningPathDetailService;
    private final ParentStudentProgressMapper parentStudentProgressMapper;
    private final OnlineSessionService onlineSessionService;
    private final AttendanceService attendanceService;
    private final ParentAttendanceMapper parentAttendanceMapper;
    private final AssignedAssignmentService assignedAssignmentService;
    private final AssignmentAttemptService assignmentAttemptService;
    private final GradingResultService gradingResultService;
    private final ParentAssignmentMapper parentAssignmentMapper;
    private final ParentDashboardMapper parentDashboardMapper;

    @PostMapping("/links")
    public ParentStudentResponse linkStudent(
            @RequestBody UseParentLinkRequest request,
            Authentication authentication
    ) {
        User parent =
                currentUserHelper.getCurrentParent(
                        authentication
                );

        ParentLink parentLink =
                parentLinkService.useVerificationCode(
                        request.verificationCode(),
                        parent
                );

        return parentLinkMapper
                .toStudentResponse(parentLink);
    }

    @GetMapping("/students")
    public List<ParentStudentResponse> getStudents(
            Authentication authentication
    ) {
        User parent =
                currentUserHelper.getCurrentParent(
                        authentication
                );

        return parentLinkService
                .getParentLinksByParent(parent.getId())
                .stream()
                .map(parentLinkMapper::toStudentResponse)
                .toList();
    }

    @GetMapping("/students/{studentId}/progress")
    public List<ParentStudentProgressResponse> getStudentProgress(
            @PathVariable Integer studentId,
            Authentication authentication
    ) {
        User parent =
                currentUserHelper.getCurrentParent(
                        authentication
                );

        parentAccessHelper.requireLinkedStudent(
                parent,
                studentId
        );

        List<StudentLearningPath> progresses =
                studentLearningPathService
                        .getStudentLearningPathsByStudent(
                                studentId
                        );

        return enrollmentService
                .getActiveEnrollmentsByStudent(studentId)
                .stream()
                .map(enrollment -> {

                    CourseClass courseClass =
                            enrollment.getCourseClass();

                    Integer courseId =
                            courseClass.getCourse().getId();

                    StudentLearningPath progress =
                            progresses.stream()
                                    .filter(p ->
                                            p.getLearningPath()
                                                    .getCourse()
                                                    .getId()
                                                    .equals(courseId)
                                    )
                                    .findFirst()
                                    .orElse(null);

                    List<LearningPathDetail> details =
                            progress == null
                                    ? List.of()
                                    : learningPathDetailService
                                      .getDetailsByLearningPath(
                                              progress.getLearningPath()
                                              .getId()
                                      );

                    return parentStudentProgressMapper
                            .toResponse(
                                    courseClass,
                                    progress,
                                    details
                            );
                })
                .toList();
    }

    @GetMapping("/students/{studentId}/attendance")
    public List<ParentAttendanceResponse> getStudentAttendance(
            @PathVariable Integer studentId,
            Authentication authentication
    ) {
        User parent =
                currentUserHelper.getCurrentParent(
                        authentication
                );

        parentAccessHelper.requireLinkedStudent(
                parent,
                studentId
        );

        return enrollmentService
                .getActiveEnrollmentsByStudent(studentId)
                .stream()
                .flatMap(enrollment -> {

                    Integer classId =
                            enrollment.getCourseClass()
                                    .getId();

                    return onlineSessionService
                            .getSessionsByClass(classId)
                            .stream();
                })
                .sorted(
                        Comparator.comparing(
                                        OnlineSession::getStartTime
                                )
                                .reversed()
                )
                .map(session -> {

                    Attendance attendance =
                            attendanceService
                                    .getAttendance(
                                            session.getId(),
                                            studentId
                                    );

                    return parentAttendanceMapper
                            .toResponse(
                                    session,
                                    attendance
                            );
                })
                .toList();
    }

    @GetMapping("/students/{studentId}/assignments")
    public List<ParentAssignmentResponse> getStudentAssignments(
            @PathVariable Integer studentId,
            Authentication authentication
    ) {
        User parent =
                currentUserHelper.getCurrentParent(
                        authentication
                );

        parentAccessHelper.requireLinkedStudent(
                parent,
                studentId
        );

        return assignedAssignmentService
                .getAssignedAssignmentsByStudent(studentId)
                .stream()
                .map(assigned -> {

                    AssignmentAttempt latest =
                            assignmentAttemptService
                                    .getLatestAttempt(
                                            assigned.getId()
                                    );

                    GradingResult result =
                            latest == null
                                    ? null
                                    : gradingResultService
                                      .getGradingResultByAttempt(
                                              latest.getId()
                                      );

                    return parentAssignmentMapper
                            .toResponse(
                                    assigned,
                                    latest,
                                    result
                            );
                })
                .toList();
    }

    @DeleteMapping("/links/{linkId}")
    public ParentLinkActionResponse unlinkStudent(
            @PathVariable Integer linkId,
            Authentication authentication
    ) {
        User parent =
                currentUserHelper.getCurrentParent(
                        authentication
                );

        parentLinkService.unlinkParentLink(
                linkId,
                parent
        );

        return new ParentLinkActionResponse(
                linkId,
                "Hủy liên kết với học viên thành công!"
        );
    }

    @GetMapping("/students/{studentId}/dashboard")
    public ParentDashboardResponse getStudentDashboard(
            @PathVariable Integer studentId,
            Authentication authentication
    ) {
        User parent =
                currentUserHelper.getCurrentParent(
                        authentication
                );

        User student =
                parentAccessHelper.requireLinkedStudent(
                        parent,
                        studentId
                );

        Integer activeCourses =
                enrollmentService
                        .getActiveEnrollmentsByStudent(studentId)
                        .size();

        List<StudentLearningPath> learningPaths =
                studentLearningPathService
                        .getStudentLearningPathsByStudent(studentId);

        Integer inProgressLearningPaths =
                (int) learningPaths.stream()
                        .filter(path ->
                                path.getStatus()
                                        == StudentLearningPath
                                        .ProgressStatus.IN_PROGRESS
                        )
                        .count();

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
                        .getAssignedAssignmentsByStudent(studentId);

        List<AssignmentWithAttempt> assignmentAttempts =
                assignments.stream()
                        .map(assigned ->
                                new AssignmentWithAttempt(
                                        assigned,
                                        assignmentAttemptService
                                                .getLatestAttempt(
                                                        assigned.getId()
                                                )
                                )
                        )
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

        ParentContinueAssignmentResponse continueAssignment =
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
                                parentDashboardMapper
                                        .toContinueAssignmentResponse(
                                                item.assigned(),
                                                item.attempt()
                                        )
                        )
                        .orElse(null);

        List<AttemptWithResult> gradedAttempts =
                assignmentAttempts.stream()
                        .filter(item ->
                                item.attempt() != null
                                        && item.attempt().getStatus()
                                        == AssignmentAttempt.AttemptStatus.GRADED
                        )
                        .map(item ->
                                new AttemptWithResult(
                                        item.attempt(),
                                        gradingResultService
                                                .getGradingResultByAttempt(
                                                        item.attempt().getId()
                                                )
                                )
                        )
                        .filter(item ->
                                item.result() != null
                                        && item.result().getTotalScore() != null
                        )
                        .toList();

        BigDecimal latestScore =
                gradedAttempts.stream()
                        .max(
                                Comparator.comparing(
                                        item ->
                                                item.attempt().getSubmittedAt() == null
                                                        ? LocalDateTime.MIN
                                                        : item.attempt().getSubmittedAt()
                                )
                        )
                        .map(item ->
                                item.result().getTotalScore()
                        )
                        .orElse(null);

        AttendanceSummary attendanceSummary =
                calculateAttendance(
                        studentId
                );

        return parentDashboardMapper.toResponse(
                student,

                activeCourses,

                inProgressLearningPaths,
                completedLearningPaths,

                inProgressAssignments,
                pendingGradingAssignments,

                attendanceSummary.present(),
                attendanceSummary.absent(),
                attendanceSummary.notMarked(),

                latestScore,

                continueAssignment
        );
    }
    private AttendanceSummary calculateAttendance(
            Integer studentId
    ) {
        int present = 0;
        int absent = 0;
        int notMarked = 0;

        for(var enrollment :
                enrollmentService
                        .getActiveEnrollmentsByStudent(studentId)) {

            Integer classId =
                    enrollment.getCourseClass().getId();

            for(var session :
                    onlineSessionService
                            .getSessionsByClass(classId)) {

                Attendance attendance =
                        attendanceService.getAttendance(
                                session.getId(),
                                studentId
                        );

                if(attendance == null) {
                    notMarked++;

                } else if(Boolean.TRUE.equals(
                        attendance.getPresent()
                )) {
                    present++;

                } else {
                    absent++;
                }
            }
        }

        return new AttendanceSummary(
                present,
                absent,
                notMarked
        );
    }
    private record AssignmentWithAttempt(
            AssignedAssignment assigned,
            AssignmentAttempt attempt
    ) {
    }

    private record AttemptWithResult(
            AssignmentAttempt attempt,
            GradingResult result
    ) {
    }

    private record AttendanceSummary(
            Integer present,
            Integer absent,
            Integer notMarked
    ) {
    }
}