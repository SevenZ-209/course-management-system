package com.lmdk.course_management_system.classes;

import com.lmdk.course_management_system.controllers.CourseClassController;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseClassLifecycleTest {

    @Test
    void futureClass_isUpcomingEvenIfStoredStatusWasActive() {
        LocalDate today = LocalDate.of(2026, 9, 2);
        CourseClass courseClass = courseClass(today.plusDays(1), today.plusDays(10));
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);

        assertEquals(CourseClass.ClassStatus.UPCOMING, courseClass.resolveStatus(today));
    }

    @Test
    void classIsActive_onStartAndEndDateBoundaries() {
        LocalDate today = LocalDate.of(2026, 9, 2);

        CourseClass startsToday = courseClass(today, today.plusDays(5));
        CourseClass endsToday = courseClass(today.minusDays(5), today);

        assertEquals(CourseClass.ClassStatus.ACTIVE, startsToday.resolveStatus(today));
        assertEquals(CourseClass.ClassStatus.ACTIVE, endsToday.resolveStatus(today));
    }

    @Test
    void pastClass_isCompletedEvenIfStoredStatusWasUpcoming() {
        LocalDate today = LocalDate.of(2026, 9, 2);
        CourseClass courseClass = courseClass(today.minusDays(10), today.minusDays(1));
        courseClass.setStatus(CourseClass.ClassStatus.UPCOMING);

        assertEquals(CourseClass.ClassStatus.COMPLETED, courseClass.resolveStatus(today));
    }

    @Test
    void canceledClass_alwaysRemainsCanceled() {
        LocalDate today = LocalDate.of(2026, 9, 2);
        CourseClass courseClass = courseClass(today.minusDays(2), today.plusDays(2));
        courseClass.setStatus(CourseClass.ClassStatus.CANCELED);

        assertEquals(CourseClass.ClassStatus.CANCELED, courseClass.resolveStatus(today));
    }

    @Test
    void nullDates_fallBackToStoredStatus() {
        CourseClass courseClass = new CourseClass();
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);

        assertEquals(CourseClass.ClassStatus.ACTIVE,
                courseClass.resolveStatus(LocalDate.of(2026, 9, 2)));
    }

    @Test
    void thymeleafStatusEndpoint_rejectsManualLifecycleStatus() {
        CourseClassService classService = mock(CourseClassService.class);
        CourseClass courseClass = courseClass(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        when(classService.getClassById(1)).thenReturn(courseClass);

        CourseClassController controller = new CourseClassController(
                classService, mock(CourseService.class), mock(UserService.class));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/classes", controller.updateStatus(1, "UPCOMING", redirect));
        assertEquals(
                "Trạng thái Sắp mở / Đang học / Hoàn thành được tự động theo thời gian lớp học!",
                redirect.getFlashAttributes().get("errorMessage")
        );
        verify(classService, never()).updateClass(any(CourseClass.class));
    }

    @Test
    void thymeleafStatusEndpoint_allowsCancel() {
        CourseClassService classService = mock(CourseClassService.class);
        CourseClass courseClass = courseClass(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        when(classService.getClassById(1)).thenReturn(courseClass);

        CourseClassController controller = new CourseClassController(
                classService, mock(CourseService.class), mock(UserService.class));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/classes", controller.updateStatus(1, "CANCELED", redirect));
        assertEquals(CourseClass.ClassStatus.CANCELED, courseClass.getStatus());
        assertEquals("Hủy lớp học thành công!", redirect.getFlashAttributes().get("successMessage"));
        verify(classService).updateClass(courseClass);
    }

    private static CourseClass courseClass(LocalDate startDate, LocalDate endDate) {
        CourseClass courseClass = new CourseClass();
        courseClass.setStartDate(startDate);
        courseClass.setEndDate(endDate);
        courseClass.setStatus(CourseClass.ClassStatus.UPCOMING);
        return courseClass;
    }
}
