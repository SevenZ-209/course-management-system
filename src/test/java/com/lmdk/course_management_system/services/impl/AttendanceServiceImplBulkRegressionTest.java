package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.AttendanceRepository;
import com.lmdk.course_management_system.services.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplBulkRegressionTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EnrollmentService enrollmentService;

    private AttendanceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AttendanceServiceImpl(attendanceRepository, enrollmentService);
    }

    @Test
    void saveAttendances_emptyListDoesNothing() {
        List<Attendance> result = service.saveAttendances(List.of(), 10, 20);

        assertTrue(result.isEmpty());
        verifyNoInteractions(enrollmentService, attendanceRepository);
    }

    @Test
    void saveAttendances_requiresClassAndSession() {
        Attendance attendance = attendance(student(1), session(20, 10), true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.saveAttendances(List.of(attendance), null, 20)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.saveAttendances(List.of(attendance), 10, null)
        );

        verify(attendanceRepository, never()).saveAttendances(anyList());
    }

    @Test
    void saveAttendances_successUpdatesAttendedTimeAndSavesOneBatch() {
        User student1 = student(1);
        User student2 = student(2);
        OnlineSession session = session(20, 10);

        Attendance present = attendance(student1, session, true);
        Attendance absent = attendance(student2, session, false);
        absent.setAttendedAt(LocalDateTime.now().minusMinutes(10));

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(student1), activeEnrollment(student2)));
        when(attendanceRepository.saveAttendances(anyList()))
                .thenAnswer(i -> i.getArgument(0));

        List<Attendance> result = service.saveAttendances(
                List.of(present, absent),
                10,
                20
        );

        assertEquals(2, result.size());
        assertNotNull(present.getAttendedAt());
        assertNull(absent.getAttendedAt());
        verify(attendanceRepository, times(1)).saveAttendances(anyList());
    }

    @Test
    void saveAttendances_rejectsAttendanceFromAnotherSessionBeforeSavingAnything() {
        User student = student(1);
        Attendance attendance = attendance(student, session(99, 10), true);

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(student)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveAttendances(List.of(attendance), 10, 20)
        );

        assertEquals("Các dữ liệu điểm danh phải thuộc cùng một buổi học!", ex.getMessage());
        verify(attendanceRepository, never()).saveAttendances(anyList());
    }

    @Test
    void saveAttendances_rejectsInactiveStudentBeforeSavingBatch() {
        User student = student(1);
        student.setStatus(User.UserStatus.INACTIVE);
        Attendance attendance = attendance(student, session(20, 10), true);

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(student)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveAttendances(List.of(attendance), 10, 20)
        );

        assertEquals("Tài khoản học viên không hợp lệ hoặc không hoạt động!", ex.getMessage());
        verify(attendanceRepository, never()).saveAttendances(anyList());
    }

    @Test
    void saveAttendances_rejectsStudentOutsideActiveClass() {
        User requestedStudent = student(1);
        User activeStudent = student(2);
        Attendance attendance = attendance(requestedStudent, session(20, 10), true);

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(activeStudent)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveAttendances(List.of(attendance), 10, 20)
        );

        assertEquals("Học viên không thuộc lớp học này hoặc chưa được kích hoạt!", ex.getMessage());
        verify(attendanceRepository, never()).saveAttendances(anyList());
    }

    @Test
    void saveAttendances_rejectsNullPresentStatus() {
        User student = student(1);
        Attendance attendance = attendance(student, session(20, 10), null);

        when(enrollmentService.getActiveEnrollmentsByClass(10))
                .thenReturn(List.of(activeEnrollment(student)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveAttendances(List.of(attendance), 10, 20)
        );

        assertEquals("Vui lòng chọn trạng thái điểm danh cho tất cả thay đổi!", ex.getMessage());
        verify(attendanceRepository, never()).saveAttendances(anyList());
    }

    private User student(Integer id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.UserRole.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private OnlineSession session(Integer sessionId, Integer classId) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(classId);

        OnlineSession session = new OnlineSession();
        session.setId(sessionId);
        session.setCourseClass(courseClass);
        return session;
    }

    private Attendance attendance(User student, OnlineSession session, Boolean present) {
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setOnlineSession(session);
        attendance.setPresent(present);
        return attendance;
    }

    private Enrollment activeEnrollment(User student) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        return enrollment;
    }
}
