package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.admin.category.AdminCategoryResponse;
import com.lmdk.course_management_system.dto.admin.course.AdminCourseResponse;
import com.lmdk.course_management_system.dto.admin.courseclass.AdminCourseClassResponse;
import com.lmdk.course_management_system.dto.admin.user.AdminStudentOptionResponse;
import com.lmdk.course_management_system.dto.admin.user.AdminTeacherOptionResponse;
import com.lmdk.course_management_system.mappers.admin.AdminCategoryMapper;
import com.lmdk.course_management_system.mappers.admin.AdminCourseClassMapper;
import com.lmdk.course_management_system.mappers.admin.AdminCourseMapper;
import com.lmdk.course_management_system.mappers.admin.AdminUserMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CategoryService;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager/options")
@RequiredArgsConstructor
public class ApiManagerOptionsController {

    private final CategoryService categoryService;
    private final CourseService courseService;
    private final CourseClassService classService;
    private final UserService userService;
    private final AdminCategoryMapper categoryMapper;
    private final AdminCourseMapper courseMapper;
    private final AdminCourseClassMapper classMapper;
    private final AdminUserMapper userMapper;

    @GetMapping("/categories")
    public List<AdminCategoryResponse> getCategories() {
        return categoryService.getAllCategories().stream().map(categoryMapper::toResponse).toList();
    }

    @GetMapping("/courses")
    public List<AdminCourseResponse> getCourses() {
        return courseService.getAllCourses().stream().map(courseMapper::toResponse).toList();
    }

    @GetMapping("/classes")
    public List<AdminCourseClassResponse> getClasses(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(defaultValue = "false") boolean availableOnly
    ) {
        var classes = courseId == null ? classService.getAllClasses() : classService.getClassesByCourse(courseId);
        return classes.stream()
                .filter(courseClass -> !availableOnly
                        || (courseClass.getStatus() != CourseClass.ClassStatus.COMPLETED
                        && courseClass.getStatus() != CourseClass.ClassStatus.CANCELED))
                .map(classMapper::toResponse)
                .toList();
    }

    @GetMapping("/teachers")
    public List<AdminTeacherOptionResponse> getTeachers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var users = hasQuery(q)
                ? userService.searchUsersByRole(User.UserRole.TEACHER, q.trim(), safePage(page), safeSize(size))
                : List.<User>of();
        return users.stream().map(userMapper::toTeacherOptionResponse).toList();
    }

    @GetMapping("/students")
    public List<AdminStudentOptionResponse> getStudents(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var users = hasQuery(q)
                ? userService.searchUsersByRole(User.UserRole.STUDENT, q.trim(), safePage(page), safeSize(size))
                : List.<User>of();
        return users.stream()
                .map(user -> new AdminStudentOptionResponse(
                        user.getId(), user.getUsername(), user.getFullName(), user.getStatus().name()
                ))
                .toList();
    }

    @GetMapping("/teachers/{userId}")
    public AdminTeacherOptionResponse getTeacher(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        if (user == null || user.getRole() != User.UserRole.TEACHER)
            throw new IllegalArgumentException("Giáo viên không tồn tại!");
        return userMapper.toTeacherOptionResponse(user);
    }

    @GetMapping("/students/{userId}")
    public AdminStudentOptionResponse getStudent(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        if (user == null || user.getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException("Học viên không tồn tại!");
        return new AdminStudentOptionResponse(user.getId(), user.getUsername(), user.getFullName(), user.getStatus().name());
    }

    private boolean hasQuery(String q) {
        return q != null && !q.isBlank();
    }

    private int safePage(Integer page) {
        return page == null ? 1 : Math.max(page, 1);
    }

    private int safeSize(Integer size) {
        return size == null ? 20 : Math.min(Math.max(size, 1), 50);
    }
}
