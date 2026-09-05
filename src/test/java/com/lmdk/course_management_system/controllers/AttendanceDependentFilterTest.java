package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class AttendanceDependentFilterTest {

    @Test
    void sessionsEndpoint_filtersBySelectedClass() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AttendanceController controller = controller(sessionService);
        OnlineSession spring = session(10, 1, "SPRING-01", "Buổi 1");
        OnlineSession java = session(20, 2, "JAVA-01", "Buổi 1");
        when(sessionService.getAllSessions()).thenReturn(List.of(spring, java));

        List<Map<String, Object>> result = controller.getSessionsByClass(1);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).get("id"));
        assertEquals("SPRING-01", result.get(0).get("className"));
        assertEquals("Buổi 1", result.get(0).get("title"));
    }

    @Test
    void attendanceList_mismatchedClassAndSession_clearsSessionFilter() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        CourseClassService classService = mock(CourseClassService.class);
        AttendanceController controller = controller(attendanceService, sessionService, classService);
        ReflectionTestUtils.setField(controller, "pageSize", 10);
        when(sessionService.getSessionById(20)).thenReturn(session(20, 2, "JAVA-01", "Buổi 1"));
        when(sessionService.getAllSessions()).thenReturn(List.of());
        when(classService.getAllClasses()).thenReturn(List.of());
        when(attendanceService.countAttendances(anyMap())).thenReturn(0L);
        when(attendanceService.getAttendances(anyMap())).thenReturn(List.of());
        Map<String, String> params = new HashMap<>(Map.of("classId", "1", "sessionId", "20"));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.attendances(params, model);

        assertFalse(params.containsKey("sessionId"));
        assertEquals("", model.get("sessionId"));
    }

    @Test
    void attendanceList_matchingClassAndSession_keepsSessionFilter() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        CourseClassService classService = mock(CourseClassService.class);
        AttendanceController controller = controller(attendanceService, sessionService, classService);
        ReflectionTestUtils.setField(controller, "pageSize", 10);
        when(sessionService.getSessionById(10)).thenReturn(session(10, 1, "SPRING-01", "Buổi 1"));
        when(sessionService.getAllSessions()).thenReturn(List.of());
        when(classService.getAllClasses()).thenReturn(List.of());
        when(attendanceService.countAttendances(anyMap())).thenReturn(0L);
        when(attendanceService.getAttendances(anyMap())).thenReturn(List.of());
        Map<String, String> params = new HashMap<>(Map.of("classId", "1", "sessionId", "10"));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.attendances(params, model);

        assertEquals("10", params.get("sessionId"));
        assertEquals("10", model.get("sessionId"));
    }

    @Test
    void attendanceTemplate_hasDependentClassSessionFilter() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/attendances.html"));

        assertTrue(html.contains("id=\"attendanceFilterClass\""));
        assertTrue(html.contains("id=\"attendanceFilterSession\""));
        assertTrue(html.contains("/admin/attendances/sessions?classId="));
        assertTrue(html.contains("filterClassSelect.addEventListener(\"change\""));
    }

    private AttendanceController controller(OnlineSessionService sessionService) {
        return controller(mock(AttendanceService.class), sessionService, mock(CourseClassService.class));
    }

    private AttendanceController controller(AttendanceService attendanceService,
                                            OnlineSessionService sessionService,
                                            CourseClassService classService) {
        return new AttendanceController(
                attendanceService,
                sessionService,
                classService,
                mock(UserService.class),
                mock(EnrollmentService.class)
        );
    }

    private OnlineSession session(int id, int classId, String className, String title) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(classId);
        courseClass.setName(className);
        OnlineSession session = new OnlineSession();
        session.setId(id);
        session.setCourseClass(courseClass);
        session.setTitle(title);
        return session;
    }
}
