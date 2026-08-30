package com.lmdk.course_management_system.controllers.api.common;

import com.lmdk.course_management_system.dto.enrollment.EnrollmentRequest;
import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.dto.enrollment.EnrollmentResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.common.EnrollmentMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.EnrollmentService;

import com.lmdk.course_management_system.services.StudentLearningPathService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/enrollments")
@RequiredArgsConstructor
public class ApiEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseClassService classService;
    private final CurrentUserHelper currentUserHelper;
    private final EnrollmentMapper enrollmentMapper;
    private final StudentLearningPathService studentLearningPathService;

    @PostMapping
    public EnrollmentResponse enroll(
            @RequestBody EnrollmentRequest request,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        CourseClass courseClass =
                classService.getClassById(request.getClassId());

        if(courseClass == null)
            throw new IllegalArgumentException(
                    "Lớp học không tồn tại!"
            );

        Enrollment enrollment = new Enrollment();

        enrollment.setStudent(student);
        enrollment.setCourseClass(courseClass);
        enrollment.setStatus(
                Enrollment.EnrollmentStatus.PENDING_PAYMENT
        );

        Enrollment saved =
                enrollmentService.addEnrollment(enrollment);

        return enrollmentMapper.toResponse(saved);
    }

    @GetMapping("/{id}")
    public EnrollmentResponse getEnrollment(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        Enrollment enrollment =
                enrollmentService.getEnrollmentById(id);

        if(enrollment == null)
            throw new IllegalArgumentException(
                    "Đăng ký không tồn tại!"
            );

        if(!enrollment.getStudent()
                .getId()
                .equals(student.getId()))
            throw new ForbiddenException(
                    "Bạn không có quyền xem đăng ký này!"
            );

        return enrollmentMapper.toResponse(enrollment);
    }

    @GetMapping
    public List<EnrollmentResponse> getMyEnrollments(
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        return enrollmentService
                .getEnrollmentsByStudent(student.getId())
                .stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }
}