package com.lmdk.course_management_system.controllers.api;

import com.lmdk.course_management_system.controllers.LessonController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminLessonController;
import com.lmdk.course_management_system.mappers.admin.AdminLessonMapper;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.services.CloudinaryService;
import com.lmdk.course_management_system.services.CourseModuleService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.LessonService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LessonUpdateDuplicateGuardTest {

    @Test
    void thymeleafUpdate_duplicateOrder_doesNotMutateLoadedLesson() {
        LessonService lessonService = mock(LessonService.class);
        CourseModuleService moduleService = mock(CourseModuleService.class);
        CourseModule currentModule = module(1);
        CourseModule targetModule = module(1);
        Lesson existing = lesson(21, "Original", currentModule, 4);

        when(lessonService.getLessonById(21)).thenReturn(existing);
        when(moduleService.getModuleById(1)).thenReturn(targetModule);
        doThrow(new IllegalArgumentException("Thứ tự bài học đã tồn tại trong module!"))
                .when(lessonService).updateLesson(any(Lesson.class));

        LessonController controller = new LessonController(
                lessonService, moduleService, mock(CourseService.class), mock(CloudinaryService.class));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/lessons",
                controller.updateLesson(21, "Changed", 1, 1, null, redirect));

        assertEquals("Original", existing.getName());
        assertEquals(4, existing.getOrderNumber());
        assertSame(currentModule, existing.getCourseModule());
        assertEquals("Thứ tự bài học đã tồn tại trong module!", redirect.getFlashAttributes().get("errorMessage"));

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonService).updateLesson(captor.capture());
        Lesson candidate = captor.getValue();
        assertNotSame(existing, candidate);
        assertEquals(21, candidate.getId());
        assertEquals("Changed", candidate.getName());
        assertEquals(1, candidate.getOrderNumber());
        assertSame(targetModule, candidate.getCourseModule());
    }

    @Test
    void apiUpdate_duplicateOrder_doesNotMutateLoadedLesson() {
        LessonService lessonService = mock(LessonService.class);
        CourseModuleService moduleService = mock(CourseModuleService.class);
        CourseModule currentModule = module(1);
        CourseModule targetModule = module(1);
        Lesson existing = lesson(21, "Original", currentModule, 4);

        when(lessonService.getLessonById(21)).thenReturn(existing);
        when(moduleService.getModuleById(1)).thenReturn(targetModule);
        doThrow(new IllegalArgumentException("Thứ tự bài học đã tồn tại trong module!"))
                .when(lessonService).updateLesson(any(Lesson.class));

        ApiAdminLessonController controller = new ApiAdminLessonController(
                lessonService, moduleService, mock(CloudinaryService.class), mock(AdminLessonMapper.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateLesson(21, "Changed", 1, 1, null));

        assertEquals("Thứ tự bài học đã tồn tại trong module!", ex.getMessage());
        assertEquals("Original", existing.getName());
        assertEquals(4, existing.getOrderNumber());
        assertSame(currentModule, existing.getCourseModule());

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonService).updateLesson(captor.capture());
        Lesson candidate = captor.getValue();
        assertNotSame(existing, candidate);
        assertEquals(21, candidate.getId());
        assertEquals("Changed", candidate.getName());
        assertEquals(1, candidate.getOrderNumber());
        assertSame(targetModule, candidate.getCourseModule());
    }

    private static CourseModule module(Integer id) {
        CourseModule module = new CourseModule();
        module.setId(id);
        return module;
    }

    private static Lesson lesson(Integer id, String name, CourseModule module, Integer orderNumber) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setName(name);
        lesson.setCourseModule(module);
        lesson.setOrderNumber(orderNumber);
        lesson.setFilePublicId("old-public-id");
        lesson.setFileUrl("old-url");
        lesson.setFileName("old.pdf");
        return lesson;
    }
}
