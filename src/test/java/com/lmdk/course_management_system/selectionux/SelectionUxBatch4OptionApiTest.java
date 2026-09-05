package com.lmdk.course_management_system.selectionux;

import com.lmdk.course_management_system.controllers.api.admin.ApiAdminCourseModuleController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminEnrollmentController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminLessonController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminUserController;
import com.lmdk.course_management_system.controllers.api.manager.ApiManagerOptionsController;
import com.lmdk.course_management_system.dto.admin.lesson.AdminLessonOptionResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.admin.*;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.repository.EnrollmentRepository;
import com.lmdk.course_management_system.repository.UserRepository;
import com.lmdk.course_management_system.repository.CourseClassRepository;
import com.lmdk.course_management_system.services.*;
import com.lmdk.course_management_system.services.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectionUxBatch4OptionApiTest {

    @Test
    void adminTeacherOptions_withoutQuery_doesNotLoadAllUsers() {
        UserService userService = mock(UserService.class);
        ApiAdminUserController controller = new ApiAdminUserController(
                userService, mock(CurrentUserHelper.class), mock(AdminUserMapper.class));

        assertTrue(controller.getTeacherOptions(null, 1, 20).isEmpty());
        verifyNoInteractions(userService);
    }

    @Test
    void managerStudentOptions_withoutQuery_doesNotLoadAllUsers() {
        UserService userService = mock(UserService.class);
        ApiManagerOptionsController controller = new ApiManagerOptionsController(
                mock(CategoryService.class), mock(CourseService.class), mock(CourseClassService.class), userService,
                mock(AdminCategoryMapper.class), mock(AdminCourseMapper.class), mock(AdminCourseClassMapper.class),
                mock(AdminUserMapper.class));

        assertTrue(controller.getStudents(null, 1, 20).isEmpty());
        verifyNoInteractions(userService);
    }

    @Test
    void courseModuleOptions_withCourseId_loadOnlyThatCoursesModules() {
        CourseModuleService moduleService = mock(CourseModuleService.class);
        CourseModule module = new CourseModule();
        module.setId(7);
        when(moduleService.getModulesByCourse(4)).thenReturn(List.of(module));
        ApiAdminCourseModuleController controller = new ApiAdminCourseModuleController(
                moduleService, mock(CourseService.class), mock(AdminCourseModuleMapper.class));

        assertEquals(1, controller.getModuleOptions(4).size());
        verify(moduleService).getModulesByCourse(4);
        verify(moduleService, never()).getAllModules();
    }

    @Test
    void lessonOptions_loadEveryLessonFromModulesOfSelectedCourse() {
        CourseModuleService moduleService = mock(CourseModuleService.class);
        LessonService lessonService = mock(LessonService.class);
        CourseModule module = new CourseModule();
        module.setId(3);
        module.setName("Spring Core");
        Lesson lesson = new Lesson();
        lesson.setId(11);
        lesson.setName("Bean lifecycle");
        when(moduleService.getModulesByCourse(2)).thenReturn(List.of(module));
        when(lessonService.getLessonsByModule(3)).thenReturn(List.of(lesson));
        ApiAdminLessonController controller = new ApiAdminLessonController(
                lessonService, moduleService, mock(CloudinaryService.class), mock(AdminLessonMapper.class));

        List<AdminLessonOptionResponse> result = controller.getLessonOptions(2);

        assertEquals(1, result.size());
        assertEquals(11, result.get(0).id());
        assertEquals(3, result.get(0).moduleId());
        assertEquals("Spring Core", result.get(0).moduleName());
    }

    @Test
    void pendingEnrollmentOptions_withoutQuery_doesNotLoadPendingTable() {
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        ApiAdminEnrollmentController controller = new ApiAdminEnrollmentController(
                enrollmentService, mock(CourseClassService.class), mock(UserService.class), mock(AdminEnrollmentMapper.class));

        assertTrue(controller.getPendingOptions("  ", 1, 20).isEmpty());
        verifyNoInteractions(enrollmentService);
    }

    @Test
    void pendingEnrollmentSearch_clampsPageAndSizeBeforeRepository() {
        EnrollmentRepository repository = mock(EnrollmentRepository.class);
        EnrollmentServiceImpl service = new EnrollmentServiceImpl(
                repository, mock(UserRepository.class), mock(CourseClassRepository.class));
        when(repository.searchPendingEnrollments("student", 1, 50)).thenReturn(List.of());

        assertTrue(service.searchPendingEnrollments("  student  ", -5, 999).isEmpty());
        verify(repository).searchPendingEnrollments("student", 1, 50);
    }
}
