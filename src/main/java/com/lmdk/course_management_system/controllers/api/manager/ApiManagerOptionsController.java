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
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CategoryService;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<AdminCourseClassResponse> getClasses() {
        return classService.getAllClasses().stream().map(classMapper::toResponse).toList();
    }

    @GetMapping("/teachers")
    public List<AdminTeacherOptionResponse> getTeachers() {
        return userService.getUsersByRole(User.UserRole.TEACHER).stream()
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .map(userMapper::toTeacherOptionResponse)
                .toList();
    }

    @GetMapping("/students")
    public List<AdminStudentOptionResponse> getStudents() {
        return userService.getUsersByRole(User.UserRole.STUDENT).stream()
                .map(user -> new AdminStudentOptionResponse(
                        user.getId(), user.getUsername(), user.getFullName(), user.getStatus().name()
                ))
                .toList();
    }
}
