package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.admin.enrollment.*;
import com.lmdk.course_management_system.mappers.admin.AdminEnrollmentMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/enrollments")
@RequiredArgsConstructor
public class ApiManagerEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseClassService classService;
    private final UserService userService;
    private final AdminEnrollmentMapper enrollmentMapper;

    @Value("${enrollments.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminEnrollmentPageResponse getEnrollments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        if(kw != null && !kw.isBlank()) params.put("kw", kw.trim());
        if(courseId != null) params.put("courseId", String.valueOf(courseId));
        if(classId != null) params.put("classId", String.valueOf(classId));
        if(status != null && !status.isBlank()) params.put("status", status);

        long totalRecords = enrollmentService.countEnrollments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);
        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminEnrollmentPageResponse(
                enrollmentService.getEnrollments(params).stream().map(enrollmentMapper::toResponse).toList(),
                page, totalPages, totalRecords
        );
    }

    @PostMapping
    public AdminEnrollmentActionResponse addEnrollment(@RequestBody CreateAdminEnrollmentRequest request) {
        User student = requireStudent(request.studentId());
        CourseClass courseClass = requireClass(request.classId());

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourseClass(courseClass);
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);

        Enrollment saved = enrollmentService.addEnrollment(enrollment);
        return new AdminEnrollmentActionResponse(saved.getId(), "Đăng ký lớp học thành công!");
    }

    @PatchMapping("/{enrollmentId}/cancel")
    public AdminEnrollmentActionResponse cancelEnrollment(@PathVariable Integer enrollmentId) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);
        if(enrollment == null) throw new IllegalArgumentException("Không tìm thấy đăng ký!");
        if(enrollment.getStatus() == Enrollment.EnrollmentStatus.CANCELED)
            throw new IllegalArgumentException("Đăng ký này đã bị hủy!");

        enrollment.setStatus(Enrollment.EnrollmentStatus.CANCELED);
        enrollmentService.updateEnrollment(enrollment);
        return new AdminEnrollmentActionResponse(enrollmentId, "Hủy đăng ký thành công!");
    }

    private User requireStudent(Integer studentId) {
        if(studentId == null) throw new IllegalArgumentException("Vui lòng chọn học viên!");
        User student = userService.getUserById(studentId);
        if(student == null) throw new IllegalArgumentException("Học viên không tồn tại!");
        if(student.getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException("Người dùng không phải học viên!");
        return student;
    }

    private CourseClass requireClass(Integer classId) {
        if(classId == null) throw new IllegalArgumentException("Vui lòng chọn lớp học!");
        CourseClass courseClass = classService.getClassById(classId);
        if(courseClass == null) throw new IllegalArgumentException("Lớp học không tồn tại!");
        return courseClass;
    }
}
