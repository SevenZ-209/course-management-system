package com.lmdk.course_management_system.selectionux;

import com.lmdk.course_management_system.dto.admin.attendance.AdminAttendanceRosterResponse;
import com.lmdk.course_management_system.dto.admin.attendance.BulkAttendanceItemRequest;
import com.lmdk.course_management_system.dto.admin.attendance.BulkAttendanceSaveRequest;
import com.lmdk.course_management_system.helpers.AttendanceRosterHelper;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.AttendanceService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SelectionUxBatch2AttendanceRosterTest {

    @Test
    void roster_returnsActiveStudentsWithExistingAndNotMarkedState() {
        AttendanceService attendanceService = mock(AttendanceService.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AttendanceRosterHelper helper = new AttendanceRosterHelper(attendanceService, enrollmentService, sessionService);

        OnlineSession session = session(10, 1);
        User a = student(101, "Nguyen A", "studenta");
        User b = student(102, "Nguyen B", "studentb");
        Attendance existing = attendance(501, session, a, true, "Đúng giờ");

        when(sessionService.getSessionById(10)).thenReturn(session);
        when(enrollmentService.getActiveEnrollmentsByClass(1)).thenReturn(List.of(enrollment(a), enrollment(b)));
        when(attendanceService.getAttendancesBySession(10)).thenReturn(List.of(existing));

        List<AdminAttendanceRosterResponse> result = helper.getRoster(10);

        assertEquals(2, result.size());
        assertEquals("PRESENT", result.get(0).attendanceStatus());
        assertEquals(501, result.get(0).attendanceId());
        assertEquals("NOT_MARKED", result.get(1).attendanceStatus());
        assertNull(result.get(1).attendanceId());
    }

    @Test
    void bulkSave_updatesExistingAndCreatesMissingAttendance() {
        AttendanceService attendanceService = mock(AttendanceService.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AttendanceRosterHelper helper = new AttendanceRosterHelper(attendanceService, enrollmentService, sessionService);

        OnlineSession session = session(10, 1);
        User a = student(101, "Nguyen A", "studenta");
        User b = student(102, "Nguyen B", "studentb");
        Attendance existing = attendance(501, session, a, true, null);

        when(sessionService.getSessionById(10)).thenReturn(session);
        when(enrollmentService.getActiveEnrollmentsByClass(1)).thenReturn(List.of(enrollment(a), enrollment(b)));
        when(attendanceService.getAttendancesBySession(10)).thenReturn(List.of(existing));
        when(attendanceService.saveAttendances(anyList(), eq(1), eq(10)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new BulkAttendanceSaveRequest(10, List.of(
                new BulkAttendanceItemRequest(101, false, "Vắng có phép"),
                new BulkAttendanceItemRequest(102, true, "Đúng giờ")
        ));

        List<AdminAttendanceRosterResponse> result = helper.saveBulk(request);

        assertEquals(2, result.size());
        assertEquals("ABSENT", result.get(0).attendanceStatus());
        assertEquals("Vắng có phép", result.get(0).note());
        assertEquals("PRESENT", result.get(1).attendanceStatus());
        verify(attendanceService).saveAttendances(argThat(list -> list.size() == 2), eq(1), eq(10));
    }

    @Test
    void bulkSave_rejectsDuplicateStudent() {
        AttendanceService attendanceService = mock(AttendanceService.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AttendanceRosterHelper helper = new AttendanceRosterHelper(attendanceService, enrollmentService, sessionService);
        OnlineSession session = session(10, 1);
        User a = student(101, "Nguyen A", "studenta");

        when(sessionService.getSessionById(10)).thenReturn(session);
        when(enrollmentService.getActiveEnrollmentsByClass(1)).thenReturn(List.of(enrollment(a)));
        when(attendanceService.getAttendancesBySession(10)).thenReturn(List.of());

        var request = new BulkAttendanceSaveRequest(10, List.of(
                new BulkAttendanceItemRequest(101, true, null),
                new BulkAttendanceItemRequest(101, false, null)
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> helper.saveBulk(request));
        assertEquals("Danh sách điểm danh có học viên bị trùng!", ex.getMessage());
        verify(attendanceService, never()).saveAttendances(anyList(), anyInt(), anyInt());
    }

    @Test
    void bulkSave_rejectsStudentOutsideActiveRoster() {
        AttendanceService attendanceService = mock(AttendanceService.class);
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AttendanceRosterHelper helper = new AttendanceRosterHelper(attendanceService, enrollmentService, sessionService);

        when(sessionService.getSessionById(10)).thenReturn(session(10, 1));
        when(enrollmentService.getActiveEnrollmentsByClass(1)).thenReturn(List.of());
        when(attendanceService.getAttendancesBySession(10)).thenReturn(List.of());

        var request = new BulkAttendanceSaveRequest(10, List.of(
                new BulkAttendanceItemRequest(999, true, null)
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> helper.saveBulk(request));
        assertTrue(ex.getMessage().contains("không thuộc lớp học này"));
        verify(attendanceService, never()).saveAttendances(anyList(), anyInt(), anyInt());
    }

    private OnlineSession session(Integer id, Integer classId) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(classId);
        courseClass.setName("SPRING-01");
        OnlineSession session = new OnlineSession();
        session.setId(id);
        session.setTitle("Buổi 1");
        session.setCourseClass(courseClass);
        return session;
    }

    private User student(Integer id, String name, String username) {
        User student = new User();
        student.setId(id);
        student.setFullName(name);
        student.setUsername(username);
        student.setRole(User.UserRole.STUDENT);
        student.setStatus(User.UserStatus.ACTIVE);
        return student;
    }

    private Enrollment enrollment(User student) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        return enrollment;
    }

    private Attendance attendance(Integer id, OnlineSession session, User student, boolean present, String note) {
        Attendance attendance = new Attendance();
        attendance.setId(id);
        attendance.setOnlineSession(session);
        attendance.setStudent(student);
        attendance.setPresent(present);
        attendance.setNote(note);
        return attendance;
    }
}
