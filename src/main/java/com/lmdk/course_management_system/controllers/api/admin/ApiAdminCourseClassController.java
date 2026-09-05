package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.courseclass.*;
import com.lmdk.course_management_system.mappers.admin.AdminCourseClassMapper;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/classes")
@RequiredArgsConstructor
public class ApiAdminCourseClassController {

    private final CourseClassService classService;
    private final CourseService courseService;
    private final UserService userService;
    private final AdminCourseClassMapper adminCourseClassMapper;

    @Value("${classes.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminCourseClassPageResponse getClasses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if(teacherId != null)
            params.put("teacherId", String.valueOf(teacherId));

        if(status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords =
                classService.countClasses(params);

        int totalPages = Math.max(
                (int) Math.ceil(
                        (double) totalRecords / pageSize
                ),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminCourseClassPageResponse(
                classService.getClasses(params)
                        .stream()
                        .map(adminCourseClassMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @PostMapping
    public AdminCourseClassActionResponse addClass(
            @RequestBody CourseClassRequest request
    ) {
        Course course =
                requireCourse(request.courseId());

        User teacher =
                getTeacher(request.teacherId());

        CourseClass courseClass = new CourseClass();
        courseClass.setName(request.name());
        courseClass.setCourse(course);
        courseClass.setTeacher(teacher);
        courseClass.setStartDate(request.startDate());
        courseClass.setEndDate(request.endDate());
        courseClass.setMaxStudents(request.maxStudents());

        classService.addClass(courseClass);

        return new AdminCourseClassActionResponse(
                courseClass.getId(),
                "Thêm lớp học thành công!"
        );
    }

    @PutMapping("/{classId}")
    public AdminCourseClassActionResponse updateClass(
            @PathVariable Integer classId,
            @RequestBody CourseClassRequest request
    ) {
        CourseClass courseClass =
                requireClass(classId);

        Course course =
                requireCourse(request.courseId());

        User teacher =
                getTeacher(request.teacherId());

        courseClass.setName(request.name());
        courseClass.setCourse(course);
        courseClass.setTeacher(teacher);
        courseClass.setStartDate(request.startDate());
        courseClass.setEndDate(request.endDate());
        courseClass.setMaxStudents(request.maxStudents());

        classService.updateClass(courseClass);

        return new AdminCourseClassActionResponse(
                classId,
                "Cập nhật lớp học thành công!"
        );
    }

    @PatchMapping("/{classId}/status")
    public AdminCourseClassActionResponse updateStatus(
            @PathVariable Integer classId,
            @RequestBody UpdateCourseClassStatusRequest request
    ) {
        CourseClass courseClass = requireClass(classId);

        if (request.status() == null || request.status().isBlank())
            throw new IllegalArgumentException("Trạng thái không được để trống!");

        if (!CourseClass.ClassStatus.CANCELED.name().equalsIgnoreCase(request.status().trim()))
            throw new IllegalArgumentException(
                    "Trạng thái Sắp mở / Đang học / Hoàn thành được tự động theo thời gian lớp học!"
            );

        courseClass.setStatus(CourseClass.ClassStatus.CANCELED);
        classService.updateClass(courseClass);

        return new AdminCourseClassActionResponse(
                classId,
                "Hủy lớp học thành công!"
        );
    }

    @GetMapping("/options")
    public List<AdminCourseClassResponse> getClassOptions(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(defaultValue = "false") boolean availableOnly
    ) {
        List<CourseClass> classes = courseId == null
                ? classService.getAllClasses()
                : classService.getClassesByCourse(courseId);

        return classes.stream()
                .filter(courseClass -> !availableOnly
                        || (courseClass.getStatus() != CourseClass.ClassStatus.COMPLETED
                        && courseClass.getStatus() != CourseClass.ClassStatus.CANCELED))
                .map(adminCourseClassMapper::toResponse)
                .toList();
    }

    private CourseClass requireClass(Integer classId) {
        CourseClass courseClass =
                classService.getClassById(classId);

        if(courseClass == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy lớp học!"
            );

        return courseClass;
    }

    private Course requireCourse(Integer courseId) {
        if(courseId == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn khóa học!"
            );

        Course course =
                courseService.getCourseById(courseId);

        if(course == null)
            throw new IllegalArgumentException(
                    "Khóa học không tồn tại!"
            );

        return course;
    }

    private User getTeacher(Integer teacherId) {
        if(teacherId == null)
            return null;

        User teacher =
                userService.getUserById(teacherId);

        if(teacher == null
                || teacher.getRole() != User.UserRole.TEACHER
                || teacher.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Giáo viên không hợp lệ!"
            );

        return teacher;
    }
}