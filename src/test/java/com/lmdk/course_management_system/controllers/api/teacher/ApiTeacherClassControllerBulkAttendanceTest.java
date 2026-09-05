package com.lmdk.course_management_system.controllers.api.teacher;

import com.lmdk.course_management_system.dto.teacher.attendance.BulkUpdateTeacherAttendanceRequest;
import com.lmdk.course_management_system.dto.teacher.attendance.TeacherAttendanceResponse;
import com.lmdk.course_management_system.dto.teacher.attendance.UpdateTeacherAttendanceItemRequest;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.helpers.TeacherAccessHelper;
import com.lmdk.course_management_system.mappers.teacher.*;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTeacherClassControllerBulkAttendanceTest {

    @Mock private CourseClassService classService;
    @Mock private EnrollmentService enrollmentService;
    @Mock private StudentLearningPathService studentLearningPathService;
    @Mock private LearningPathDetailService learningPathDetailService;
    @Mock private TeacherStudentProgressMapper teacherStudentProgressMapper;
    @Mock private CurrentUserHelper currentUserHelper;
    @Mock private TeacherAccessHelper teacherAccessHelper;
    @Mock private AssignedAssignmentService assignedAssignmentService;
    @Mock private AssignmentAttemptService assignmentAttemptService;
    @Mock private TeacherAssignedAssignmentMapper teacherAssignedAssignmentMapper;
    @Mock private OnlineSessionService onlineSessionService;
    @Mock private TeacherOnlineSessionMapper teacherOnlineSessionMapper;
    @Mock private AssignmentService assignmentService;
    @Mock private TeacherAvailableAssignmentMapper teacherAvailableAssignmentMapper;
    @Mock private TeacherClassMapper teacherClassMapper;
    @Mock private TeacherStudentMapper teacherStudentMapper;
    @Mock private AttendanceService attendanceService;
    @Mock private TeacherAttendanceMapper teacherAttendanceMapper;
    @Mock private Authentication authentication;

    @InjectMocks
    private ApiTeacherClassController controller;

    @Test
    void updateAttendances_emptyRequestIsRejectedBeforeAnyWrite() {
        prepareTeacherAndSession(10, 20);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateAttendances(
                        10,
                        20,
                        new BulkUpdateTeacherAttendanceRequest(List.of()),
                        authentication
                )
        );

        assertEquals("Không có thay đổi điểm danh cần lưu!", ex.getMessage());
        verify(attendanceService, never()).saveAttendances(anyList(), anyInt(), anyInt());
    }

    @Test
    void updateAttendances_rejectsDuplicateStudentIds() {
        prepareTeacherAndSession(10, 20);
        User student = student(1);

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(student)));
        when(attendanceService.getAttendancesBySession(20)).thenReturn(List.of());

        BulkUpdateTeacherAttendanceRequest request =
                new BulkUpdateTeacherAttendanceRequest(List.of(
                        new UpdateTeacherAttendanceItemRequest(1, true, null),
                        new UpdateTeacherAttendanceItemRequest(1, false, null)
                ));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateAttendances(10, 20, request, authentication)
        );

        assertEquals("Danh sách điểm danh có học viên bị trùng!", ex.getMessage());
        verify(attendanceService, never()).saveAttendances(anyList(), anyInt(), anyInt());
    }

    @Test
    void updateAttendances_rejectsStudentOutsideClass() {
        prepareTeacherAndSession(10, 20);
        User activeStudent = student(1);

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(activeStudent)));
        when(attendanceService.getAttendancesBySession(20)).thenReturn(List.of());

        BulkUpdateTeacherAttendanceRequest request =
                new BulkUpdateTeacherAttendanceRequest(List.of(
                        new UpdateTeacherAttendanceItemRequest(99, true, null)
                ));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateAttendances(10, 20, request, authentication)
        );

        assertEquals("Học viên không thuộc lớp học này hoặc chưa được kích hoạt!", ex.getMessage());
        verify(attendanceService, never()).saveAttendances(anyList(), anyInt(), anyInt());
    }

    @Test
    void updateAttendances_successBuildsOneBatchAndNormalizesNotes() {
        prepareTeacherAndSession(10, 20);
        User student1 = student(1);
        User student2 = student(2);

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(student1), activeEnrollment(student2)));

        Attendance existing = new Attendance();
        existing.setId(100);
        existing.setStudent(student1);
        existing.setOnlineSession(session(20, 10));
        existing.setPresent(true);
        existing.setNote("old");

        when(attendanceService.getAttendancesBySession(20))
                .thenReturn(List.of(existing));
        when(attendanceService.saveAttendances(anyList(), eq(10), eq(20)))
                .thenAnswer(i -> i.getArgument(0));
        when(teacherAttendanceMapper.toResponse(any(User.class), any(Attendance.class)))
                .thenAnswer(i -> {
                    User student = i.getArgument(0);
                    Attendance attendance = i.getArgument(1);
                    return new TeacherAttendanceResponse(
                            student.getId(),
                            "Student " + student.getId(),
                            "student" + student.getId(),
                            attendance.getId(),
                            Boolean.TRUE.equals(attendance.getPresent()) ? "PRESENT" : "ABSENT",
                            attendance.getAttendedAt(),
                            attendance.getNote()
                    );
                });

        BulkUpdateTeacherAttendanceRequest request =
                new BulkUpdateTeacherAttendanceRequest(List.of(
                        new UpdateTeacherAttendanceItemRequest(1, false, "  Có phép  "),
                        new UpdateTeacherAttendanceItemRequest(2, true, "   ")
                ));

        List<TeacherAttendanceResponse> response =
                controller.updateAttendances(10, 20, request, authentication);

        assertEquals(2, response.size());

        ArgumentCaptor<List<Attendance>> captor = ArgumentCaptor.forClass(List.class);
        verify(attendanceService, times(1))
                .saveAttendances(captor.capture(), eq(10), eq(20));

        List<Attendance> changes = captor.getValue();
        assertEquals(2, changes.size());

        Attendance first = changes.get(0);
        Attendance second = changes.get(1);

        assertSame(existing, first);
        assertFalse(first.getPresent());
        assertEquals("Có phép", first.getNote());

        assertEquals(2, second.getStudent().getId());
        assertEquals(20, second.getOnlineSession().getId());
        assertTrue(second.getPresent());
        assertNull(second.getNote());
    }

    @Test
    void updateAttendances_rejectsSessionFromAnotherClassBeforeAttendanceQueries() {
        User teacher = teacher(7);
        CourseClass teacherClass = courseClass(10);
        OnlineSession foreignSession = session(20, 99);

        when(currentUserHelper.getCurrentUser(authentication)).thenReturn(teacher);
        when(teacherAccessHelper.requireTeacherClass(teacher, 10)).thenReturn(teacherClass);
        when(onlineSessionService.getSessionById(20)).thenReturn(foreignSession);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateAttendances(
                        10,
                        20,
                        new BulkUpdateTeacherAttendanceRequest(List.of(
                                new UpdateTeacherAttendanceItemRequest(1, true, null)
                        )),
                        authentication
                )
        );

        assertEquals("Buổi học không thuộc lớp này!", ex.getMessage());
        verifyNoInteractions(attendanceService);
        verify(enrollmentService, never()).getActiveEnrollmentsByClass(anyInt());
    }

    private void prepareTeacherAndSession(Integer classId, Integer sessionId) {
        User teacher = teacher(7);
        CourseClass courseClass = courseClass(classId);
        OnlineSession session = session(sessionId, classId);

        when(currentUserHelper.getCurrentUser(authentication)).thenReturn(teacher);
        when(teacherAccessHelper.requireTeacherClass(teacher, classId)).thenReturn(courseClass);
        when(onlineSessionService.getSessionById(sessionId)).thenReturn(session);
    }

    private User teacher(Integer id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.UserRole.TEACHER);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private User student(Integer id) {
        User user = new User();
        user.setId(id);
        user.setUsername("student" + id);
        user.setFullName("Student " + id);
        user.setRole(User.UserRole.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private CourseClass courseClass(Integer id) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(id);
        return courseClass;
    }

    private OnlineSession session(Integer sessionId, Integer classId) {
        OnlineSession session = new OnlineSession();
        session.setId(sessionId);
        session.setCourseClass(courseClass(classId));
        return session;
    }

    private Enrollment activeEnrollment(User student) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        return enrollment;
    }
}
