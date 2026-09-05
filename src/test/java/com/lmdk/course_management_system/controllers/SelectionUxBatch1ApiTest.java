package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.controllers.api.admin.ApiAdminCourseClassController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminUserController;
import com.lmdk.course_management_system.controllers.api.manager.ApiManagerOptionsController;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.admin.AdminCourseClassMapper;
import com.lmdk.course_management_system.mappers.admin.AdminUserMapper;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CategoryService;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SelectionUxBatch1ApiTest {

    @Test
    void adminStudentOptions_withQueryUsesBoundedServerSearch() {
        UserService userService = mock(UserService.class);
        User student = user(8, "seed_student01", "Học viên test 01", User.UserRole.STUDENT);
        when(userService.searchUsersByRole(User.UserRole.STUDENT, "seed", 1, 20)).thenReturn(List.of(student));

        ApiAdminUserController controller = new ApiAdminUserController(
                userService, mock(CurrentUserHelper.class), new AdminUserMapper());

        var result = controller.getStudentOptions("seed", 1, 20);
        assertEquals(1, result.size());
        assertEquals(8, result.get(0).id());
        verify(userService).searchUsersByRole(User.UserRole.STUDENT, "seed", 1, 20);
        verify(userService, never()).getUsersByRole(User.UserRole.STUDENT);
    }

    @Test
    void adminTeacherOptions_clampsPageAndSizeForAsyncLookup() {
        UserService userService = mock(UserService.class);
        User teacher = user(2, "teacher", "Giáo viên", User.UserRole.TEACHER);
        when(userService.searchUsersByRole(User.UserRole.TEACHER, "bao", 1, 50)).thenReturn(List.of(teacher));

        ApiAdminUserController controller = new ApiAdminUserController(
                userService, mock(CurrentUserHelper.class), new AdminUserMapper());

        var result = controller.getTeacherOptions("bao", 0, 999);
        assertEquals(1, result.size());
        verify(userService).searchUsersByRole(User.UserRole.TEACHER, "bao", 1, 50);
    }

    @Test
    void adminClassOptions_filtersByCourseAndAvailableLifecycle() {
        CourseClassService classService = mock(CourseClassService.class);
        Course course = course(4, "Spring Boot");
        CourseClass available = courseClass(10, "SPRING-01", course, CourseClass.ClassStatus.ACTIVE);
        CourseClass completed = courseClass(11, "SPRING-OLD", course, CourseClass.ClassStatus.COMPLETED);
        when(classService.getClassesByCourse(4)).thenReturn(List.of(available, completed));

        ApiAdminCourseClassController controller = new ApiAdminCourseClassController(
                classService, mock(CourseService.class), mock(UserService.class), new AdminCourseClassMapper());

        var result = controller.getClassOptions(4, true);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).id());
        verify(classService).getClassesByCourse(4);
        verify(classService, never()).getAllClasses();
    }

    @Test
    void managerStudentOptions_withQueryUsesBoundedServerSearch() {
        UserService userService = mock(UserService.class);
        User student = user(9, "student02", "Học viên 02", User.UserRole.STUDENT);
        when(userService.searchUsersByRole(User.UserRole.STUDENT, "student", 1, 20)).thenReturn(List.of(student));

        ApiManagerOptionsController controller = new ApiManagerOptionsController(
                mock(CategoryService.class), mock(CourseService.class), mock(CourseClassService.class), userService,
                null, null, null, new AdminUserMapper());

        var result = controller.getStudents("student", 1, 20);
        assertEquals(1, result.size());
        assertEquals(9, result.get(0).id());
        verify(userService).searchUsersByRole(User.UserRole.STUDENT, "student", 1, 20);
        verify(userService, never()).getUsersByRole(User.UserRole.STUDENT);
    }

    @Test
    void managerClassOptions_filtersBySelectedCourse() {
        CourseClassService classService = mock(CourseClassService.class);
        Course course = course(7, "Git");
        CourseClass courseClass = courseClass(20, "GIT-01", course, CourseClass.ClassStatus.UPCOMING);
        when(classService.getClassesByCourse(7)).thenReturn(List.of(courseClass));

        ApiManagerOptionsController controller = new ApiManagerOptionsController(
                mock(CategoryService.class), mock(CourseService.class), classService, mock(UserService.class),
                null, null, new AdminCourseClassMapper(), new AdminUserMapper());

        var result = controller.getClasses(7, false);
        assertEquals(1, result.size());
        assertEquals(20, result.get(0).id());
        verify(classService).getClassesByCourse(7);
        verify(classService, never()).getAllClasses();
    }

    private User user(Integer id, String username, String fullName, User.UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(username + "@example.com");
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private Course course(Integer id, String name) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        return course;
    }

    private CourseClass courseClass(Integer id, String name, Course course, CourseClass.ClassStatus status) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(id);
        courseClass.setName(name);
        courseClass.setCourse(course);
        courseClass.setMaxStudents(30);
        courseClass.setStatus(status);
        return courseClass;
    }
}
