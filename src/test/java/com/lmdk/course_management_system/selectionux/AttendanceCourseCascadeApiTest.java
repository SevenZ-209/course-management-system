package com.lmdk.course_management_system.selectionux;

import com.lmdk.course_management_system.controllers.api.admin.ApiAdminAttendanceController;
import com.lmdk.course_management_system.controllers.api.manager.ApiManagerAttendanceController;
import com.lmdk.course_management_system.helpers.AttendanceRosterHelper;
import com.lmdk.course_management_system.mappers.admin.AdminAttendanceMapper;
import com.lmdk.course_management_system.services.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class AttendanceCourseCascadeApiTest {

    @Test
    void adminAttendanceFilter_forwardsCourseIdToRepositoryQuery() {
        AttendanceService attendanceService = mock(AttendanceService.class);
        when(attendanceService.countAttendances(anyMap())).thenReturn(0L);
        when(attendanceService.getAttendances(anyMap())).thenReturn(java.util.List.of());

        ApiAdminAttendanceController controller = new ApiAdminAttendanceController(
                attendanceService, mock(OnlineSessionService.class), mock(UserService.class),
                mock(EnrollmentService.class), mock(AdminAttendanceMapper.class), mock(AttendanceRosterHelper.class)
        );

        controller.getAttendances(1, null, null, null, 9, null);

        verify(attendanceService).countAttendances(argThat(params -> "9".equals(params.get("courseId"))));
        verify(attendanceService).getAttendances(argThat(params -> "9".equals(params.get("courseId"))));
    }

    @Test
    void managerAttendanceFilter_forwardsCourseIdToRepositoryQuery() {
        AttendanceService attendanceService = mock(AttendanceService.class);
        when(attendanceService.countAttendances(anyMap())).thenReturn(0L);
        when(attendanceService.getAttendances(anyMap())).thenReturn(java.util.List.of());

        ApiManagerAttendanceController controller = new ApiManagerAttendanceController(
                attendanceService, mock(OnlineSessionService.class), mock(UserService.class),
                mock(EnrollmentService.class), mock(AdminAttendanceMapper.class), mock(AttendanceRosterHelper.class)
        );

        controller.getAttendances(1, null, null, null, 11, null);

        verify(attendanceService).countAttendances(argThat(params -> "11".equals(params.get("courseId"))));
        verify(attendanceService).getAttendances(argThat(params -> "11".equals(params.get("courseId"))));
    }
}
