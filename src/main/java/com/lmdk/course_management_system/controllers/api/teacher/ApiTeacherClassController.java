package com.lmdk.course_management_system.controllers.api.teacher;

import com.lmdk.course_management_system.dto.teacher.assignment.TeacherAssignedAssignmentPageResponse;
import com.lmdk.course_management_system.dto.teacher.assignment.TeacherAssignedAssignmentResponse;
import com.lmdk.course_management_system.dto.teacher.assignment.TeacherAvailableAssignmentResponse;
import com.lmdk.course_management_system.dto.teacher.assignment.TeacherManualAssignmentRequest;
import com.lmdk.course_management_system.dto.teacher.assignment.TeacherReleaseCurrentAssignmentRequest;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherClassResponse;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentProgressPageResponse;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentProgressResponse;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentPageResponse;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentResponse;
import com.lmdk.course_management_system.dto.teacher.session.TeacherOnlineSessionResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.helpers.TeacherAccessHelper;
import com.lmdk.course_management_system.mappers.teacher.*;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;
import com.lmdk.course_management_system.dto.teacher.attendance.BulkUpdateTeacherAttendanceRequest;
import com.lmdk.course_management_system.dto.teacher.attendance.TeacherAttendanceResponse;
import com.lmdk.course_management_system.dto.teacher.attendance.UpdateTeacherAttendanceItemRequest;
import com.lmdk.course_management_system.dto.teacher.attendance.UpdateTeacherAttendanceRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/classes")
@RequiredArgsConstructor
public class ApiTeacherClassController {

    private final CourseClassService classService;
    private final EnrollmentService enrollmentService;
    private final StudentLearningPathService studentLearningPathService;
    private final LearningPathDetailService learningPathDetailService;
    private final TeacherStudentProgressMapper teacherStudentProgressMapper;
    private final CurrentUserHelper currentUserHelper;
    private final TeacherAccessHelper teacherAccessHelper;
    private final AssignedAssignmentService assignedAssignmentService;
    private final AssignmentAttemptService assignmentAttemptService;
    private final TeacherAssignedAssignmentMapper teacherAssignedAssignmentMapper;
    private final OnlineSessionService onlineSessionService;
    private final TeacherOnlineSessionMapper teacherOnlineSessionMapper;
    private final AssignmentService assignmentService;
    private final TeacherAvailableAssignmentMapper teacherAvailableAssignmentMapper;
    private final TeacherClassMapper teacherClassMapper;
    private final TeacherStudentMapper teacherStudentMapper;
    private final AttendanceService attendanceService;
    private final TeacherAttendanceMapper teacherAttendanceMapper;

    @Value("${enrollments.page-size:10}")
    private int enrollmentPageSize;

    @Value("${assigned-assignments.page-size:10}")
    private int assignedAssignmentPageSize;

    @GetMapping
    public List<TeacherClassResponse> getMyClasses(
            Authentication authentication
    ) {
        User teacher =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        if(teacher.getRole() != User.UserRole.TEACHER)
            throw new IllegalArgumentException(
                    "Tài khoản không phải giáo viên!"
            );

        return classService
                .getClassesByTeacher(teacher.getId())
                .stream()
                .map(courseClass -> {

                    Integer studentCount =
                            enrollmentService
                                    .getActiveEnrollmentsByClass(
                                            courseClass.getId()
                                    )
                                    .size();

                    return teacherClassMapper
                            .toResponse(
                                    courseClass,
                                    studentCount
                            );
                })
                .toList();
    }

    @GetMapping("/{classId}")
    public TeacherClassResponse getClass(
            @PathVariable Integer classId,
            Authentication authentication
    ) {
        User teacher = currentUserHelper.getCurrentUser(authentication);

        CourseClass courseClass = teacherAccessHelper.requireTeacherClass(
                teacher,
                classId
        );

        Integer studentCount = enrollmentService
                .getActiveEnrollmentsByClass(classId)
                .size();

        return teacherClassMapper.toResponse(
                courseClass,
                studentCount
        );
    }

    @GetMapping("/{classId}/sessions/{sessionId}/attendance")
    public List<TeacherAttendanceResponse> getAttendance(
            @PathVariable Integer classId,
            @PathVariable Integer sessionId,
            Authentication authentication
    ) {
        User teacher =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        teacherAccessHelper.requireTeacherClass(
                teacher,
                classId
        );

        OnlineSession session =
                requireSessionInClass(
                        sessionId,
                        classId
                );

        Map<Integer, Attendance> attendanceByStudentId = new HashMap<>();
        for (Attendance attendance : attendanceService.getAttendancesBySession(session.getId()))
            attendanceByStudentId.put(attendance.getStudent().getId(), attendance);

        return enrollmentService
                .getActiveEnrollmentsByClass(classId)
                .stream()
                .map(enrollment -> {
                    User student = enrollment.getStudent();
                    return teacherAttendanceMapper.toResponse(
                            student,
                            attendanceByStudentId.get(student.getId())
                    );
                })
                .toList();
    }

    @PutMapping("/{classId}/sessions/{sessionId}/attendance")
    public List<TeacherAttendanceResponse> updateAttendances(
            @PathVariable Integer classId,
            @PathVariable Integer sessionId,
            @RequestBody BulkUpdateTeacherAttendanceRequest request,
            Authentication authentication
    ) {
        User teacher = currentUserHelper.getCurrentUser(authentication);
        teacherAccessHelper.requireTeacherClass(teacher, classId);
        OnlineSession session = requireSessionInClass(sessionId, classId);

        if (request == null || request.attendances() == null || request.attendances().isEmpty())
            throw new IllegalArgumentException("Không có thay đổi điểm danh cần lưu!");

        Map<Integer, User> studentsById = new HashMap<>();
        for (Enrollment enrollment : enrollmentService.getActiveEnrollmentsByClass(classId))
            studentsById.put(enrollment.getStudent().getId(), enrollment.getStudent());

        Map<Integer, Attendance> existingByStudentId = new HashMap<>();
        for (Attendance attendance : attendanceService.getAttendancesBySession(sessionId))
            existingByStudentId.put(attendance.getStudent().getId(), attendance);

        Set<Integer> requestedStudentIds = new HashSet<>();
        List<Attendance> changes = new ArrayList<>();

        for (UpdateTeacherAttendanceItemRequest item : request.attendances()) {
            if (item.studentId() == null || item.present() == null)
                throw new IllegalArgumentException("Dữ liệu điểm danh không hợp lệ!");

            if (!requestedStudentIds.add(item.studentId()))
                throw new IllegalArgumentException("Danh sách điểm danh có học viên bị trùng!");

            User student = studentsById.get(item.studentId());
            if (student == null)
                throw new IllegalArgumentException("Học viên không thuộc lớp học này hoặc chưa được kích hoạt!");

            Attendance attendance = existingByStudentId.get(item.studentId());
            if (attendance == null) {
                attendance = new Attendance();
                attendance.setOnlineSession(session);
                attendance.setStudent(student);
            }

            attendance.setPresent(item.present());
            attendance.setNote(item.note() == null || item.note().isBlank() ? null : item.note().trim());
            changes.add(attendance);
        }

        Map<Integer, Attendance> savedByStudentId = new HashMap<>();
        for (Attendance attendance : attendanceService.saveAttendances(changes, classId, sessionId))
            savedByStudentId.put(attendance.getStudent().getId(), attendance);

        return request.attendances().stream()
                .map(item -> teacherAttendanceMapper.toResponse(
                        studentsById.get(item.studentId()),
                        savedByStudentId.get(item.studentId())
                ))
                .toList();
    }

    @PutMapping(
            "/{classId}/sessions/{sessionId}/attendance/{studentId}"
    )
    public TeacherAttendanceResponse updateAttendance(
            @PathVariable Integer classId,
            @PathVariable Integer sessionId,
            @PathVariable Integer studentId,
            @RequestBody UpdateTeacherAttendanceRequest request,
            Authentication authentication
    ) {
        User teacher =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        teacherAccessHelper.requireTeacherClass(
                teacher,
                classId
        );

        OnlineSession session =
                requireSessionInClass(
                        sessionId,
                        classId
                );

        if(request.present() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn trạng thái điểm danh!"
            );

        Enrollment enrollment =
                enrollmentService.getEnrollment(
                        studentId,
                        classId
                );

        if(enrollment == null
                || enrollment.getStatus()
                != Enrollment.EnrollmentStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Học viên không thuộc lớp học này hoặc chưa được kích hoạt!"
            );

        User student =
                enrollment.getStudent();

        Attendance attendance =
                attendanceService.getAttendance(
                        sessionId,
                        studentId
                );

        if(attendance == null) {

            attendance = new Attendance();

            attendance.setOnlineSession(session);
            attendance.setStudent(student);
            attendance.setPresent(request.present());
            attendance.setNote(request.note());

            attendance =
                    attendanceService.addAttendance(
                            attendance
                    );

        } else {

            attendance.setPresent(
                    request.present()
            );

            attendance.setNote(
                    request.note()
            );

            attendanceService.updateAttendance(
                    attendance
            );

            attendance =
                    attendanceService.getAttendance(
                            sessionId,
                            studentId
                    );
        }

        return teacherAttendanceMapper
                .toResponse(
                        student,
                        attendance
                );
    }

    @GetMapping("/{classId}/available-assignments")
    public List<TeacherAvailableAssignmentResponse> getAvailableAssignments(
            @PathVariable Integer classId,
            Authentication authentication
    ) {
        User teacher =
                currentUserHelper.getCurrentUser(authentication);

        CourseClass courseClass =
                teacherAccessHelper.requireTeacherClass(
                        teacher,
                        classId
                );

        return assignmentService
                .getAssignmentsByCourse(
                        courseClass.getCourse().getId()
                )
                .stream()
                .filter(assignment ->
                        assignment.getStatus()
                                == Assignment.AssignmentStatus.ACTIVE
                )
                .map(teacherAvailableAssignmentMapper::toResponse)
                .toList();
    }

    @PostMapping("/{classId}/assignments/manual")
    public TeacherAssignedAssignmentResponse assignManual(
            @PathVariable Integer classId,
            @RequestBody TeacherManualAssignmentRequest request,
            Authentication authentication
    ) {
        User teacher =
                currentUserHelper.getCurrentUser(authentication);

        CourseClass courseClass =
                teacherAccessHelper.requireTeacherClass(
                        teacher,
                        classId
                );

        Enrollment enrollment =
                enrollmentService.getEnrollment(
                        request.studentId(),
                        classId
                );

        if(enrollment == null
                || enrollment.getStatus()
                != Enrollment.EnrollmentStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Học viên không thuộc lớp học này hoặc chưa được kích hoạt!"
            );

        Assignment assignment =
                assignmentService.getAssignmentById(
                        request.assignmentId()
                );

        if(assignment == null)
            throw new IllegalArgumentException(
                    "Bài tập không tồn tại!"
            );

        if(!assignment.getCourse()
                .getId()
                .equals(
                        courseClass.getCourse().getId()
                ))
            throw new IllegalArgumentException(
                    "Bài tập không thuộc khóa học của lớp này!"
            );

        AssignedAssignment assigned =
                assignedAssignmentService.assignManual(
                        request.studentId(),
                        request.assignmentId(),
                        teacher,
                        request.availableAt(),
                        request.dueAt()
                );

        return teacherAssignedAssignmentMapper
                .toResponse(
                        assigned,
                        null
                );
    }

    @GetMapping("/{classId}/assignments")
    public TeacherAssignedAssignmentPageResponse getAssignments(
            @PathVariable Integer classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer learningPathId,
            @RequestParam(required = false) Integer assignmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String date,
            Authentication authentication
    ) {
        User teacher = currentUserHelper.getCurrentUser(authentication);
        teacherAccessHelper.requireTeacherClass(teacher, classId);

        page = Math.max(page, 1);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("classId", String.valueOf(classId));

        if(kw != null && !kw.isBlank()) params.put("kw", kw.trim());
        if(studentId != null) params.put("studentId", String.valueOf(studentId));
        if(learningPathId != null) params.put("learningPathId", String.valueOf(learningPathId));
        if(assignmentId != null) params.put("assignmentId", String.valueOf(assignmentId));
        if(status != null && !status.isBlank()) params.put("status", status.trim().toUpperCase());
        if(source != null && !source.isBlank()) params.put("source", source.trim().toUpperCase());
        if(date != null && !date.isBlank()) params.put("date", date.trim());

        long totalRecords = assignedAssignmentService.countAssignedAssignments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / assignedAssignmentPageSize), 1);

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        List<AssignedAssignment> assignedAssignments =
                assignedAssignmentService.getAssignedAssignments(params);

        Map<Integer, AssignmentAttempt> latestAttempts = assignmentAttemptService
                .getLatestAttemptsByAssignedAssignmentIds(
                        assignedAssignments.stream().map(AssignedAssignment::getId).toList()
                );

        List<TeacherAssignedAssignmentResponse> assignments = assignedAssignments
                .stream()
                .map(assigned -> teacherAssignedAssignmentMapper.toResponse(
                        assigned, latestAttempts.get(assigned.getId())
                ))
                .toList();

        return new TeacherAssignedAssignmentPageResponse(
                assignments, page, totalPages, totalRecords
        );
    }

    @PostMapping(
            "/{classId}/learning-paths/{studentLearningPathId}/release-current"
    )
    public TeacherAssignedAssignmentResponse releaseCurrentAssignment(
            @PathVariable Integer classId,
            @PathVariable Integer studentLearningPathId,
            @RequestBody(required = false)
            TeacherReleaseCurrentAssignmentRequest request,
            Authentication authentication
    ) {
        User teacher =
                currentUserHelper.getCurrentUser(authentication);

        CourseClass courseClass =
                teacherAccessHelper.requireTeacherClass(
                        teacher,
                        classId
                );

        StudentLearningPath progress =
                studentLearningPathService
                        .getStudentLearningPathById(
                                studentLearningPathId
                        );

        if(progress == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy lộ trình học viên!"
            );

        Integer studentId =
                progress.getStudent().getId();

        Enrollment enrollment =
                enrollmentService.getEnrollment(
                        studentId,
                        classId
                );

        if(enrollment == null
                || enrollment.getStatus()
                != Enrollment.EnrollmentStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Học viên không thuộc lớp học này hoặc chưa được kích hoạt!"
            );

        Integer pathCourseId =
                progress.getLearningPath()
                        .getCourse()
                        .getId();

        if(!pathCourseId.equals(
                courseClass.getCourse().getId()
        ))
            throw new IllegalArgumentException(
                    "Lộ trình không thuộc khóa học của lớp này!"
            );

        AssignedAssignment assigned =
                assignedAssignmentService
                        .assignCurrentDetail(
                                studentLearningPathId,

                                request == null
                                        ? null
                                        : request.availableAt(),

                                request == null
                                        ? null
                                        : request.dueAt()
                        );

        return teacherAssignedAssignmentMapper
                .toResponse(
                        assigned,
                        null
                );
    }

    @GetMapping("/{classId}/online-sessions")
    public List<TeacherOnlineSessionResponse> getOnlineSessions(
            @PathVariable Integer classId,
            Authentication authentication
    ) {

        User teacher =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        teacherAccessHelper.requireTeacherClass(
                teacher,
                classId
        );

        return onlineSessionService
                .getSessionsByClass(classId)
                .stream()
                .map(teacherOnlineSessionMapper::toResponse)
                .toList();
    }

    @GetMapping("/{classId}/students")
    public TeacherStudentPageResponse getStudents(
            @PathVariable Integer classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            Authentication authentication
    ) {
        User teacher = currentUserHelper.getCurrentUser(authentication);
        teacherAccessHelper.requireTeacherClass(teacher, classId);

        page = Math.max(page, 1);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("classId", String.valueOf(classId));
        params.put("status", Enrollment.EnrollmentStatus.ACTIVE.name());
        if(kw != null && !kw.isBlank()) params.put("kw", kw.trim());

        long totalRecords = enrollmentService.countEnrollments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / enrollmentPageSize), 1);

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new TeacherStudentPageResponse(
                enrollmentService.getEnrollments(params)
                        .stream()
                        .map(teacherStudentMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/{classId}/progress")
    public TeacherStudentProgressPageResponse getStudentProgress(
            @PathVariable Integer classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        User teacher = currentUserHelper.getCurrentUser(authentication);
        CourseClass courseClass = teacherAccessHelper.requireTeacherClass(teacher, classId);
        Integer courseId = courseClass.getCourse().getId();

        page = Math.max(page, 1);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("classId", String.valueOf(classId));
        params.put("status", Enrollment.EnrollmentStatus.ACTIVE.name());
        if(kw != null && !kw.isBlank()) params.put("kw", kw.trim());
        if(status != null && !status.isBlank()) {
            params.put("progressCourseId", String.valueOf(courseId));
            params.put("progressStatus", status.trim().toUpperCase());
        }

        long totalRecords = enrollmentService.countEnrollments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / enrollmentPageSize), 1);

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        List<Enrollment> enrollments = enrollmentService.getEnrollments(params);
        List<Integer> studentIds = enrollments.stream()
                .map(enrollment -> enrollment.getStudent().getId())
                .toList();

        Map<Integer, StudentLearningPath> progressByStudent = new HashMap<>();
        studentLearningPathService
                .getStudentLearningPathsByStudentsAndCourse(studentIds, courseId)
                .forEach(progress -> progressByStudent
                        .putIfAbsent(progress.getStudent().getId(), progress));

        List<Integer> learningPathIds = progressByStudent.values().stream()
                .map(progress -> progress.getLearningPath().getId())
                .distinct()
                .toList();

        Map<Integer, List<LearningPathDetail>> detailsByPath = learningPathDetailService
                .getDetailsByLearningPaths(learningPathIds)
                .stream()
                .collect(Collectors.groupingBy(
                        detail -> detail.getLearningPath().getId()
                ));

        List<TeacherStudentProgressResponse> progress = enrollments.stream()
                .map(enrollment -> {
                    User student = enrollment.getStudent();
                    StudentLearningPath studentProgress = progressByStudent.get(student.getId());
                    List<LearningPathDetail> details = studentProgress == null
                            ? List.of()
                            : detailsByPath.getOrDefault(
                                    studentProgress.getLearningPath().getId(), List.of()
                            );

                    return teacherStudentProgressMapper.toResponse(
                            student, studentProgress, details
                    );
                })
                .toList();

        return new TeacherStudentProgressPageResponse(
                progress, page, totalPages, totalRecords
        );
    }

    private OnlineSession requireSessionInClass(
            Integer sessionId,
            Integer classId
    ) {
        OnlineSession session =
                onlineSessionService
                        .getSessionById(sessionId);

        if(session == null)
            throw new IllegalArgumentException(
                    "Buổi học không tồn tại!"
            );

        if(session.getCourseClass() == null
                || !session.getCourseClass()
                .getId()
                .equals(classId))
            throw new IllegalArgumentException(
                    "Buổi học không thuộc lớp này!"
            );

        return session;
    }
}