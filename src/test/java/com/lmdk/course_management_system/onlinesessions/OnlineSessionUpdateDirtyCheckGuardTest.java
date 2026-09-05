package com.lmdk.course_management_system.onlinesessions;

import com.lmdk.course_management_system.controllers.OnlineSessionController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminOnlineSessionController;
import com.lmdk.course_management_system.controllers.api.manager.ApiManagerOnlineSessionController;
import com.lmdk.course_management_system.dto.admin.onlinesession.OnlineSessionRequest;
import com.lmdk.course_management_system.mappers.admin.AdminOnlineSessionMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnlineSessionUpdateDirtyCheckGuardTest {

    @Test
    void thymeleaf_updateConflict_doesNotMutateManagedOriginal() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        CourseClassService classService = mock(CourseClassService.class);
        UserService userService = mock(UserService.class);
        OnlineSession original = originalSession();
        CourseClass targetClass = courseClass(2);
        User targetTeacher = teacher(11);
        when(sessionService.getSessionById(19)).thenReturn(original);
        when(classService.getClassById(2)).thenReturn(targetClass);
        when(userService.getUserById(11)).thenReturn(targetTeacher);
        doThrow(new IllegalArgumentException("Giáo viên đã có buổi học khác trong khoảng thời gian này!"))
                .when(sessionService).updateSession(any(OnlineSession.class));

        OnlineSessionController controller = new OnlineSessionController(sessionService, classService, userService);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String view = controller.updateSession(19, "Changed", 2, 11,
                LocalDateTime.parse("2026-09-02T15:00"), LocalDateTime.parse("2026-09-02T16:00"),
                "https://meet.example.com/new", redirect);

        assertEquals("redirect:/admin/online-sessions", view);
        assertEquals("Original", original.getTitle());
        assertEquals(1, original.getCourseClass().getId());
        assertEquals(10, original.getTeacher().getId());
        assertEquals("Giáo viên đã có buổi học khác trong khoảng thời gian này!", redirect.getFlashAttributes().get("errorMessage"));

        ArgumentCaptor<OnlineSession> captor = ArgumentCaptor.forClass(OnlineSession.class);
        verify(sessionService).updateSession(captor.capture());
        assertNotSame(original, captor.getValue());
        assertEquals(19, captor.getValue().getId());
    }

    @Test
    void adminApi_updateConflict_doesNotMutateManagedOriginal() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        CourseClassService classService = mock(CourseClassService.class);
        UserService userService = mock(UserService.class);
        OnlineSession original = originalSession();
        when(sessionService.getSessionById(19)).thenReturn(original);
        when(classService.getClassById(2)).thenReturn(courseClass(2));
        when(userService.getUserById(11)).thenReturn(teacher(11));
        doThrow(new IllegalArgumentException("conflict")).when(sessionService).updateSession(any(OnlineSession.class));

        ApiAdminOnlineSessionController controller = new ApiAdminOnlineSessionController(
                sessionService, classService, userService, mock(AdminOnlineSessionMapper.class));
        OnlineSessionRequest request = new OnlineSessionRequest("Changed", 2, 11,
                LocalDateTime.parse("2026-09-02T15:00"), LocalDateTime.parse("2026-09-02T16:00"),
                "https://meet.example.com/new");

        assertThrows(IllegalArgumentException.class, () -> controller.updateSession(19, request));
        assertEquals("Original", original.getTitle());
        assertEquals(1, original.getCourseClass().getId());
        assertEquals(10, original.getTeacher().getId());
    }

    @Test
    void managerApi_updateConflict_doesNotMutateManagedOriginal() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        CourseClassService classService = mock(CourseClassService.class);
        UserService userService = mock(UserService.class);
        OnlineSession original = originalSession();
        when(sessionService.getSessionById(19)).thenReturn(original);
        when(classService.getClassById(2)).thenReturn(courseClass(2));
        when(userService.getUserById(11)).thenReturn(teacher(11));
        doThrow(new IllegalArgumentException("conflict")).when(sessionService).updateSession(any(OnlineSession.class));

        ApiManagerOnlineSessionController controller = new ApiManagerOnlineSessionController(
                sessionService, classService, userService, mock(AdminOnlineSessionMapper.class));
        OnlineSessionRequest request = new OnlineSessionRequest("Changed", 2, 11,
                LocalDateTime.parse("2026-09-02T15:00"), LocalDateTime.parse("2026-09-02T16:00"),
                "https://meet.example.com/new");

        assertThrows(IllegalArgumentException.class, () -> controller.updateSession(19, request));
        assertEquals("Original", original.getTitle());
        assertEquals(1, original.getCourseClass().getId());
        assertEquals(10, original.getTeacher().getId());
    }

    private static OnlineSession originalSession() {
        OnlineSession session = new OnlineSession();
        session.setId(19);
        session.setTitle("Original");
        session.setCourseClass(courseClass(1));
        session.setTeacher(teacher(10));
        session.setStartTime(LocalDateTime.parse("2026-09-02T14:34"));
        session.setEndTime(LocalDateTime.parse("2026-09-02T17:34"));
        session.setMeetingUrl("https://meet.example.com/original");
        return session;
    }

    private static CourseClass courseClass(Integer id) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(id);
        courseClass.setStartDate(LocalDate.of(2026, 8, 1));
        courseClass.setEndDate(LocalDate.of(2026, 12, 31));
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);
        return courseClass;
    }

    private static User teacher(Integer id) {
        User teacher = new User();
        teacher.setId(id);
        teacher.setRole(User.UserRole.TEACHER);
        teacher.setStatus(User.UserStatus.ACTIVE);
        return teacher;
    }
}
