package com.lmdk.course_management_system.selectionux;

import com.lmdk.course_management_system.controllers.AssignmentController;
import com.lmdk.course_management_system.controllers.LearningPathDetailController;
import com.lmdk.course_management_system.controllers.LessonController;
import com.lmdk.course_management_system.controllers.OnlineSessionController;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.services.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectionUxBatch4DependentControllerTest {

    @Test
    void thymeleafLessonModules_areLoadedOnlyForSelectedCourse() {
        CourseModuleService moduleService = mock(CourseModuleService.class);
        CourseModule module = new CourseModule();
        module.setId(5);
        module.setName("Data JPA");
        module.setOrderNumber(2);
        when(moduleService.getModulesByCourse(9)).thenReturn(List.of(module));
        LessonController controller = new LessonController(
                mock(LessonService.class), moduleService, mock(CourseService.class), mock(CloudinaryService.class));

        List<Map<String, Object>> result = controller.getModules(9);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).get("id"));
        verify(moduleService).getModulesByCourse(9);
    }

    @Test
    void onlineSessionClassOptions_availableOnly_removesCompletedAndCanceledClasses() {
        CourseClassService classService = mock(CourseClassService.class);
        CourseClass upcoming = courseClass(1, LocalDate.now().plusDays(2), LocalDate.now().plusDays(10), CourseClass.ClassStatus.UPCOMING);
        CourseClass completed = courseClass(2, LocalDate.now().minusDays(10), LocalDate.now().minusDays(2), CourseClass.ClassStatus.ACTIVE);
        CourseClass canceled = courseClass(3, LocalDate.now().plusDays(2), LocalDate.now().plusDays(10), CourseClass.ClassStatus.CANCELED);
        when(classService.getClassesByCourse(4)).thenReturn(List.of(upcoming, completed, canceled));
        OnlineSessionController controller = new OnlineSessionController(
                mock(OnlineSessionService.class), classService, mock(CourseService.class), mock(UserService.class));

        List<Map<String, Object>> result = controller.getClasses(4, true);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get("id"));
    }

    @Test
    void assignmentLessonOptions_followSelectedCourseModules() {
        CourseModuleService moduleService = mock(CourseModuleService.class);
        LessonService lessonService = mock(LessonService.class);
        CourseModule module = new CourseModule();
        module.setId(8);
        module.setName("Spring MVC");
        Lesson lesson = new Lesson();
        lesson.setId(14);
        lesson.setName("Controller");
        when(moduleService.getModulesByCourse(6)).thenReturn(List.of(module));
        when(lessonService.getLessonsByModule(8)).thenReturn(List.of(lesson));
        AssignmentController controller = new AssignmentController(
                mock(AssignmentService.class), mock(CourseService.class), moduleService, lessonService);

        List<Map<String, Object>> result = controller.getLessons(6);

        assertEquals(1, result.size());
        assertEquals(14, result.get(0).get("id"));
        assertEquals("Spring MVC - Controller", result.get(0).get("name"));
    }

    @Test
    void learningPathOptions_areScopedToSelectedCourse() {
        LearningPathService pathService = mock(LearningPathService.class);
        LearningPath path = new LearningPath();
        path.setId(12);
        path.setName("Backend path");
        when(pathService.getLearningPathsByCourse(7)).thenReturn(List.of(path));
        LearningPathDetailController controller = new LearningPathDetailController(
                mock(LearningPathDetailService.class), pathService, mock(AssignmentService.class), mock(CourseService.class));

        List<Map<String, Object>> result = controller.getPaths(7);

        assertEquals(1, result.size());
        assertEquals(12, result.get(0).get("id"));
        verify(pathService).getLearningPathsByCourse(7);
    }

    private CourseClass courseClass(int id, LocalDate start, LocalDate end, CourseClass.ClassStatus status) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(id);
        courseClass.setName("Class " + id);
        courseClass.setStartDate(start);
        courseClass.setEndDate(end);
        courseClass.setStatus(status);
        return courseClass;
    }
}
