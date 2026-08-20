package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.EnrollmentRepository;
import com.lmdk.course_management_system.services.EnrollmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Enrollment getEnrollmentById(Integer id) {
        return enrollmentRepository.getEnrollmentById(id);
    }

    @Override
    public Enrollment getEnrollment(Integer studentId, Integer classId) {
        return enrollmentRepository.getEnrollment(studentId, classId);
    }

    @Override
    public Enrollment addEnrollment(Enrollment enrollment) {
        validateEnrollment(enrollment);

        if (enrollmentRepository.existsEnrollment(
                enrollment.getStudent().getId(),
                enrollment.getCourseClass().getId()
        ))
            throw new IllegalArgumentException("Học viên đã đăng ký lớp học này!");

        if (enrollmentRepository.countOccupiedByClass(
                enrollment.getCourseClass().getId()
        ) >= enrollment.getCourseClass().getMaxStudents())
            throw new IllegalArgumentException("Lớp học đã đủ số lượng học viên!");

        if (enrollment.getStatus() == null)
            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);

        return enrollmentRepository.addEnrollment(enrollment);
    }

    @Override
    public void updateEnrollment(Enrollment enrollment) {
        validateEnrollment(enrollment);

        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.CANCELED
                && enrollmentRepository.countOccupiedByClassExceptId(
                enrollment.getCourseClass().getId(),
                enrollment.getId()
        ) >= enrollment.getCourseClass().getMaxStudents())
            throw new IllegalArgumentException("Lớp học đã đủ số lượng học viên!");

        enrollmentRepository.updateEnrollment(enrollment);
    }

    @Override
    public List<Enrollment> getEnrollments(Map<String, String> params) {
        return enrollmentRepository.getEnrollments(params);
    }

    @Override
    public List<Enrollment> getEnrollmentsByClass(Integer classId) {
        return enrollmentRepository.getEnrollmentsByClass(classId);
    }

    @Override
    public List<Enrollment> getActiveEnrollmentsByClass(Integer classId) {
        return enrollmentRepository.getActiveEnrollmentsByClass(classId);
    }

    @Override
    public long countEnrollments(Map<String, String> params) {
        return enrollmentRepository.countEnrollments(params);
    }

    private void validateEnrollment(Enrollment enrollment) {
        if (enrollment.getStudent() == null)
            throw new IllegalArgumentException("Vui lòng chọn học viên!");

        if (enrollment.getStudent().getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException("Người dùng được chọn không phải học viên!");

        if (enrollment.getStudent().getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("Tài khoản học viên không hoạt động!");

        if (enrollment.getCourseClass() == null)
            throw new IllegalArgumentException("Vui lòng chọn lớp học!");

        if (enrollment.getCourseClass().getStatus() == CourseClass.ClassStatus.COMPLETED)
            throw new IllegalArgumentException("Lớp học đã hoàn thành!");

        if (enrollment.getCourseClass().getStatus() == CourseClass.ClassStatus.CANCELED)
            throw new IllegalArgumentException("Lớp học đã bị hủy!");
    }

    @Override
    public List<Enrollment> getPendingEnrollments() {
        return enrollmentRepository.getPendingEnrollments();
    }

    @Override
    public List<Enrollment> getActiveEnrollmentsByStudent(Integer studentId) {
        return enrollmentRepository.getActiveEnrollmentsByStudent(studentId);
    }

    @Override
    public boolean existsActiveEnrollmentByStudentAndCourse(Integer studentId, Integer courseId) {
        return enrollmentRepository.existsActiveEnrollmentByStudentAndCourse(studentId, courseId);
    }

    @Override
    public boolean existsActiveEnrollmentByStudentCourseAndTeacher(
            Integer studentId,
            Integer courseId,
            Integer teacherId) {
        return enrollmentRepository
                .existsActiveEnrollmentByStudentCourseAndTeacher(
                        studentId,
                        courseId,
                        teacherId
                );
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudent(Integer studentId) {
        return enrollmentRepository.getEnrollmentsByStudent(studentId);
    }
}