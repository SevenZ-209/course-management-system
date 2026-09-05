package com.lmdk.course_management_system.classes;

import com.lmdk.course_management_system.controllers.api.admin.ApiAdminCourseClassController;
import com.lmdk.course_management_system.controllers.api.manager.ApiManagerCourseClassController;
import com.lmdk.course_management_system.dto.admin.courseclass.UpdateCourseClassStatusRequest;
import com.lmdk.course_management_system.mappers.admin.AdminCourseClassMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseClassApiStatusGuardTest {

    @Test
    void adminApi_rejectsManualLifecycleStatus() {
        CourseClassService classService = mock(CourseClassService.class);
        when(classService.getClassById(1)).thenReturn(activeClass());

        ApiAdminCourseClassController controller = new ApiAdminCourseClassController(
                classService, mock(CourseService.class), mock(UserService.class), mock(AdminCourseClassMapper.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateStatus(1, new UpdateCourseClassStatusRequest("UPCOMING")));

        assertTrue(ex.getMessage().contains("được tự động theo thời gian"));
        verify(classService, never()).updateClass(any(CourseClass.class));
    }

    @Test
    void managerApi_rejectsManualLifecycleStatus() {
        CourseClassService classService = mock(CourseClassService.class);
        when(classService.getClassById(1)).thenReturn(activeClass());

        ApiManagerCourseClassController controller = new ApiManagerCourseClassController(
                classService, mock(CourseService.class), mock(UserService.class), mock(AdminCourseClassMapper.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateStatus(1, new UpdateCourseClassStatusRequest("COMPLETED")));

        assertTrue(ex.getMessage().contains("được tự động theo thời gian"));
        verify(classService, never()).updateClass(any(CourseClass.class));
    }

    private static CourseClass activeClass() {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(1);
        courseClass.setStartDate(LocalDate.now().minusDays(1));
        courseClass.setEndDate(LocalDate.now().plusDays(1));
        return courseClass;
    }
}
