package com.lmdk.course_management_system.onlinesessions;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.OnlineSessionRepository;
import com.lmdk.course_management_system.services.impl.OnlineSessionServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnlineSessionOverlapValidationTest {

    @Test
    void create_sameTeacherDifferentClassOverlap_isBlocked() {
        OnlineSessionRepository repository = mock(OnlineSessionRepository.class);
        OnlineSessionServiceImpl service = new OnlineSessionServiceImpl(repository);
        OnlineSession session = session(null, 2, 10, "2026-09-02T14:39", "2026-09-02T15:39");

        when(repository.existsClassScheduleConflict(2, session.getStartTime(), session.getEndTime(), null)).thenReturn(false);
        when(repository.existsTeacherScheduleConflict(10, session.getStartTime(), session.getEndTime(), null)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addSession(session));

        assertEquals("Giáo viên đã có buổi học khác trong khoảng thời gian này!", ex.getMessage());
        verify(repository, never()).addSession(any());
    }

    @Test
    void create_sameClassDifferentTeacherOverlap_isBlocked() {
        OnlineSessionRepository repository = mock(OnlineSessionRepository.class);
        OnlineSessionServiceImpl service = new OnlineSessionServiceImpl(repository);
        OnlineSession session = session(null, 1, 11, "2026-09-02T15:00", "2026-09-02T16:00");

        when(repository.existsClassScheduleConflict(1, session.getStartTime(), session.getEndTime(), null)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addSession(session));

        assertEquals("Lớp học đã có buổi học khác trong khoảng thời gian này!", ex.getMessage());
        verify(repository, never()).existsTeacherScheduleConflict(anyInt(), any(), any(), any());
        verify(repository, never()).addSession(any());
    }

    @Test
    void create_noConflict_isAllowed() {
        OnlineSessionRepository repository = mock(OnlineSessionRepository.class);
        OnlineSessionServiceImpl service = new OnlineSessionServiceImpl(repository);
        OnlineSession session = session(null, 1, 10, "2026-09-02T17:34", "2026-09-02T18:34");
        when(repository.addSession(session)).thenReturn(session);

        assertSame(session, service.addSession(session));
        verify(repository).addSession(session);
    }

    @Test
    void update_excludesCurrentSessionFromConflictCheck() {
        OnlineSessionRepository repository = mock(OnlineSessionRepository.class);
        OnlineSessionServiceImpl service = new OnlineSessionServiceImpl(repository);
        OnlineSession session = session(19, 1, 10, "2026-09-02T14:34", "2026-09-02T17:34");

        service.updateSession(session);

        verify(repository).existsClassScheduleConflict(1, session.getStartTime(), session.getEndTime(), 19);
        verify(repository).existsTeacherScheduleConflict(10, session.getStartTime(), session.getEndTime(), 19);
        verify(repository).updateSession(session);
    }

    @Test
    void update_teacherOverlap_isBlocked() {
        OnlineSessionRepository repository = mock(OnlineSessionRepository.class);
        OnlineSessionServiceImpl service = new OnlineSessionServiceImpl(repository);
        OnlineSession session = session(19, 1, 10, "2026-09-02T14:40", "2026-09-02T16:00");

        when(repository.existsTeacherScheduleConflict(10, session.getStartTime(), session.getEndTime(), 19)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateSession(session));

        assertTrue(ex.getMessage().contains("Giáo viên đã có buổi học khác"));
        verify(repository, never()).updateSession(any());
    }

    @Test
    void canceledClass_cannotReceiveNewSession() {
        OnlineSessionRepository repository = mock(OnlineSessionRepository.class);
        OnlineSessionServiceImpl service = new OnlineSessionServiceImpl(repository);
        OnlineSession session = session(null, 1, 10, "2026-09-02T14:00", "2026-09-02T15:00");
        session.getCourseClass().setStatus(CourseClass.ClassStatus.CANCELED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addSession(session));

        assertEquals("Không thể tạo buổi học cho lớp đã hủy!", ex.getMessage());
        verify(repository, never()).addSession(any());
    }

    private static OnlineSession session(Integer sessionId, Integer classId, Integer teacherId, String start, String end) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(classId);
        courseClass.setStartDate(LocalDate.of(2026, 8, 1));
        courseClass.setEndDate(LocalDate.of(2026, 12, 31));
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);

        User teacher = new User();
        teacher.setId(teacherId);
        teacher.setRole(User.UserRole.TEACHER);
        teacher.setStatus(User.UserStatus.ACTIVE);

        OnlineSession session = new OnlineSession();
        session.setId(sessionId);
        session.setTitle("UAT Session");
        session.setCourseClass(courseClass);
        session.setTeacher(teacher);
        session.setStartTime(LocalDateTime.parse(start));
        session.setEndTime(LocalDateTime.parse(end));
        session.setMeetingUrl("https://meet.example.com/uat");
        return session;
    }
}
