package com.lmdk.course_management_system.selectionux;

import com.lmdk.course_management_system.controllers.api.admin.ApiAdminOnlineSessionController;
import com.lmdk.course_management_system.controllers.api.manager.ApiManagerOnlineSessionController;
import com.lmdk.course_management_system.mappers.admin.AdminOnlineSessionMapper;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SelectionUxBatch2SessionOptionsTest {

    @Test
    void adminSessionOptions_withClassId_loadsOnlyThatClass() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AdminOnlineSessionMapper mapper = mock(AdminOnlineSessionMapper.class);
        when(sessionService.getSessionsByClass(7)).thenReturn(List.of(new OnlineSession()));

        ApiAdminOnlineSessionController controller = new ApiAdminOnlineSessionController(
                sessionService, mock(CourseClassService.class), mock(UserService.class), mapper);

        assertEquals(1, controller.getSessionOptions(7).size());
        verify(sessionService).getSessionsByClass(7);
        verify(sessionService, never()).getAllSessions();
    }

    @Test
    void managerSessionOptions_withClassId_loadsOnlyThatClass() {
        OnlineSessionService sessionService = mock(OnlineSessionService.class);
        AdminOnlineSessionMapper mapper = mock(AdminOnlineSessionMapper.class);
        when(sessionService.getSessionsByClass(9)).thenReturn(List.of(new OnlineSession()));

        ApiManagerOnlineSessionController controller = new ApiManagerOnlineSessionController(
                sessionService, mock(CourseClassService.class), mock(UserService.class), mapper);

        assertEquals(1, controller.getSessionOptions(9).size());
        verify(sessionService).getSessionsByClass(9);
        verify(sessionService, never()).getAllSessions();
    }
}
