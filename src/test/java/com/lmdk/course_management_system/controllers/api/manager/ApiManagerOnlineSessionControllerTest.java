package com.lmdk.course_management_system.controllers.api.api.manager;

import com.lmdk.course_management_system.controllers.api.manager.ApiManagerOnlineSessionController;
import com.lmdk.course_management_system.dto.admin.onlinesession.OnlineSessionRequest;
import com.lmdk.course_management_system.mappers.admin.AdminOnlineSessionMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiManagerOnlineSessionControllerTest {

    @Mock private OnlineSessionService sessionService;
    @Mock private CourseClassService classService;
    @Mock private UserService userService;
    @Mock private AdminOnlineSessionMapper sessionMapper;

    private ApiManagerOnlineSessionController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiManagerOnlineSessionController(
                sessionService, classService, userService, sessionMapper
        );
    }

    @Test
    void addSession_createsValidSession() {
        CourseClass courseClass = activeClass(10);
        User teacher = activeTeacher(20);
        when(classService.getClassById(10)).thenReturn(courseClass);
        when(userService.getUserById(20)).thenReturn(teacher);
        doAnswer(i -> {
            OnlineSession session = i.getArgument(0);
            session.setId(30);
            return null;
        }).when(sessionService).addSession(any(OnlineSession.class));

        var response = controller.addSession(request(10, 20));

        assertEquals(30, response.sessionId());
        verify(sessionService).addSession(argThat(session ->
                session.getCourseClass() == courseClass
                        && session.getTeacher() == teacher
                        && "Buổi 1".equals(session.getTitle())
        ));
    }

    @Test
    void addSession_rejectsCanceledClass() {
        CourseClass courseClass = activeClass(10);
        courseClass.setStatus(CourseClass.ClassStatus.CANCELED);
        when(classService.getClassById(10)).thenReturn(courseClass);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.addSession(request(10, 20))
        );

        assertEquals("Không thể tạo buổi học cho lớp đã hủy!", ex.getMessage());
        verifyNoInteractions(userService);
        verify(sessionService, never()).addSession(any());
    }

    @Test
    void addSession_rejectsInvalidTeacher() {
        when(classService.getClassById(10)).thenReturn(activeClass(10));
        User manager = new User();
        manager.setId(20);
        manager.setRole(User.UserRole.MANAGER);
        manager.setStatus(User.UserStatus.ACTIVE);
        when(userService.getUserById(20)).thenReturn(manager);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.addSession(request(10, 20))
        );

        assertEquals("Giáo viên không hợp lệ!", ex.getMessage());
        verify(sessionService, never()).addSession(any());
    }

    @Test
    void updateSession_rejectsMissingSession() {
        when(sessionService.getSessionById(99)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateSession(99, request(10, 20))
        );

        assertEquals("Không tìm thấy buổi học!", ex.getMessage());
        verifyNoInteractions(classService, userService);
        verify(sessionService, never()).updateSession(any());
    }

    private OnlineSessionRequest request(Integer classId, Integer teacherId) {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 8, 0);
        return new OnlineSessionRequest(
                "Buổi 1", classId, teacherId, start, start.plusHours(2), "https://meet.test/1"
        );
    }

    private CourseClass activeClass(Integer id) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(id);
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);
        return courseClass;
    }

    private User activeTeacher(Integer id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.UserRole.TEACHER);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }
}
