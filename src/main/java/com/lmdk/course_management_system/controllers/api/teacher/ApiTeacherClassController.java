package com.lmdk.course_management_system.controllers.api.teacher;

import com.lmdk.course_management_system.dto.teacher.assignment.TeacherAssignedAssignmentResponse;
import com.lmdk.course_management_system.dto.teacher.assignment.TeacherAvailableAssignmentResponse;
import com.lmdk.course_management_system.dto.teacher.assignment.TeacherManualAssignmentRequest;
import com.lmdk.course_management_system.dto.teacher.assignment.TeacherReleaseCurrentAssignmentRequest;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherClassResponse;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentProgressResponse;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentResponse;
import com.lmdk.course_management_system.dto.teacher.session.TeacherOnlineSessionResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.helpers.TeacherAccessHelper;
import com.lmdk.course_management_system.mappers.teacher.*;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;
import com.lmdk.course_management_system.dto.teacher.attendance.TeacherAttendanceResponse;
import com.lmdk.course_management_system.dto.teacher.attendance.UpdateTeacherAttendanceRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        return enrollmentService
                .getActiveEnrollmentsByClass(classId)
                .stream()
                .map(enrollment -> {

                    User student =
                            enrollment.getStudent();

                    Attendance attendance =
                            attendanceService.getAttendance(
                                    session.getId(),
                                    student.getId()
                            );

                    return teacherAttendanceMapper
                            .toResponse(
                                    student,
                                    attendance
                            );
                })
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
    public List<TeacherAssignedAssignmentResponse> getAssignments(
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

        return assignedAssignmentService
                .getAssignedAssignmentsByClass(classId)
                .stream()
                .map(assigned -> {

                    AssignmentAttempt latest =
                            assignmentAttemptService
                                    .getLatestAttempt(
                                            assigned.getId()
                                    );

                    return teacherAssignedAssignmentMapper
                            .toResponse(
                                    assigned,
                                    latest
                            );
                })
                .toList();
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
    public List<TeacherStudentResponse> getStudents(
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

        return enrollmentService
                .getActiveEnrollmentsByClass(classId)
                .stream()
                .map(teacherStudentMapper::toResponse)
                .toList();
    }

    @GetMapping("/{classId}/progress")
    public List<TeacherStudentProgressResponse> getStudentProgress(
            @PathVariable Integer classId,
            Authentication authentication
    ) {

        User teacher =
                currentUserHelper.getCurrentUser(
                        authentication
                );

        CourseClass courseClass =
                teacherAccessHelper.requireTeacherClass(
                        teacher,
                        classId
                );

        Integer courseId =
                courseClass.getCourse().getId();

        return enrollmentService
                .getActiveEnrollmentsByClass(classId)
                .stream()
                .map(enrollment -> {

                    User student =
                            enrollment.getStudent();

                    StudentLearningPath progress =
                            studentLearningPathService
                                    .getStudentLearningPathsByStudent(
                                            student.getId()
                                    )
                                    .stream()
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

                    return teacherStudentProgressMapper
                            .toResponse(
                                    student,
                                    progress,
                                    details
                            );
                })
                .toList();
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