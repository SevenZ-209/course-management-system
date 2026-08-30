package com.lmdk.course_management_system.controllers.api.parent;

import com.lmdk.course_management_system.dto.parent.*;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.helpers.OnlineSessionHelper;
import com.lmdk.course_management_system.helpers.ParentAccessHelper;
import com.lmdk.course_management_system.mappers.parent.*;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final OnlineSessionHelper onlineSessionHelper;

    @Value("${assigned-assignments.page-size:10}")
    private int assignmentPageSize;

    @Value("${online-sessions.page-size:10}")
    private int attendancePageSize;

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
    public ParentAttendancePageResponse getStudentAttendance(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "1") Integer page,
            Authentication authentication
    ) {
        User parent = currentUserHelper.getCurrentParent(authentication);
        parentAccessHelper.requireLinkedStudent(parent, studentId);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(Math.max(page, 1)));
        params.put("studentId", String.valueOf(studentId));

        long totalRecords = onlineSessionService.countSessions(params);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / attendancePageSize));
        int currentPage = Math.min(Math.max(page, 1), totalPages);

        params.put("page", String.valueOf(currentPage));

        List<OnlineSession> sessions = onlineSessionService.getSessions(params);
        Map<Integer, Attendance> attendanceBySessionId = new HashMap<>();
        for (Attendance item : attendanceService.getAttendancesByStudentAndSessionIds(
                studentId,
                sessions.stream().map(OnlineSession::getId).toList()
        ))
            attendanceBySessionId.put(item.getOnlineSession().getId(), item);

        List<ParentAttendanceResponse> attendance = sessions.stream()
                .map(session -> parentAttendanceMapper.toResponse(
                        session,
                        attendanceBySessionId.get(session.getId())
                ))
                .toList();

        return new ParentAttendancePageResponse(
                attendance,
                currentPage,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/students/{studentId}/assignments")
    public ParentAssignmentPageResponse getStudentAssignments(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        User parent = currentUserHelper.getCurrentParent(authentication);
        parentAccessHelper.requireLinkedStudent(parent, studentId);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(Math.max(page, 1)));
        params.put("studentId", String.valueOf(studentId));

        if (kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if (status != null && !status.isBlank())
            params.put("status", status.trim().toUpperCase());

        long totalRecords = assignedAssignmentService.countAssignedAssignments(params);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / assignmentPageSize));
        int currentPage = Math.min(Math.max(page, 1), totalPages);

        params.put("page", String.valueOf(currentPage));

        List<AssignedAssignment> assignedAssignments =
                assignedAssignmentService.getAssignedAssignments(params);

        Map<Integer, AssignmentAttempt> latestAttempts = assignmentAttemptService
                .getLatestAttemptsByAssignedAssignmentIds(
                        assignedAssignments.stream().map(AssignedAssignment::getId).toList()
                );

        List<Integer> latestAttemptIds = latestAttempts.values().stream()
                .map(AssignmentAttempt::getId)
                .toList();

        Map<Integer, GradingResult> gradingResults =
                gradingResultService.getGradingResultsByAttemptIds(latestAttemptIds);

        List<ParentAssignmentResponse> assignments = assignedAssignments
                .stream()
                .map(assigned -> {
                    AssignmentAttempt latest = latestAttempts.get(assigned.getId());
                    GradingResult result = latest == null
                            ? null
                            : gradingResults.get(latest.getId());

                    return parentAssignmentMapper.toResponse(assigned, latest, result);
                })
                .toList();

        return new ParentAssignmentPageResponse(
                assignments,
                currentPage,
                totalPages,
                totalRecords
        );
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

        List<AssignmentAttempt> gradedAttemptList = assignmentAttempts.stream()
                .map(AssignmentWithAttempt::attempt)
                .filter(attempt -> attempt != null
                        && attempt.getStatus() == AssignmentAttempt.AttemptStatus.GRADED)
                .toList();

        Map<Integer, GradingResult> gradedResults = gradingResultService
                .getGradingResultsByAttemptIds(
                        gradedAttemptList.stream().map(AssignmentAttempt::getId).toList()
                );

        List<AttemptWithResult> gradedAttempts = gradedAttemptList.stream()
                .map(attempt -> new AttemptWithResult(
                        attempt,
                        gradedResults.get(attempt.getId())
                ))
                .filter(item -> item.result() != null
                        && item.result().getTotalScore() != null)
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
    private AttendanceSummary calculateAttendance(Integer studentId) {
        List<OnlineSession> endedSessions = onlineSessionService.getEndedSessionsByStudent(studentId);
        Map<Integer, Attendance> attendanceBySessionId = new HashMap<>();

        for (Attendance attendance : attendanceService.getAttendancesByStudentAndSessionIds(
                studentId,
                endedSessions.stream().map(OnlineSession::getId).toList()
        ))
            attendanceBySessionId.put(attendance.getOnlineSession().getId(), attendance);

        int present = 0;
        int absent = 0;
        int notMarked = 0;

        for (OnlineSession session : endedSessions) {
            Attendance attendance = attendanceBySessionId.get(session.getId());

            if (attendance == null)
                notMarked++;
            else if (Boolean.TRUE.equals(attendance.getPresent()))
                present++;
            else
                absent++;
        }

        return new AttendanceSummary(present, absent, notMarked);
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