package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.services.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentAccessHelper {

    private final EnrollmentService enrollmentService;

    public void requireActiveCourse(Integer studentId, Integer courseId) {
        if(!enrollmentService.existsActiveEnrollmentByStudentAndCourse(studentId, courseId))
            throw new IllegalArgumentException(
                    "Bạn chưa được kích hoạt trong khóa học này!"
            );
    }

    public Enrollment requireOwnedActiveEnrollment(Integer studentId, Integer enrollmentId) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);

        if(enrollment == null)
            throw new IllegalArgumentException("Đăng ký không tồn tại!");

        if(!enrollment.getStudent().getId().equals(studentId))
            throw new IllegalArgumentException(
                    "Bạn không có quyền xem khóa học này!"
            );

        if(enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Khóa học chưa được kích hoạt!"
            );

        return enrollment;
    }
}