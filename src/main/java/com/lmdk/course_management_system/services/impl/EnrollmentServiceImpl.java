package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.CourseClassRepository;
import com.lmdk.course_management_system.repository.EnrollmentRepository;
import com.lmdk.course_management_system.repository.UserRepository;
import com.lmdk.course_management_system.services.EnrollmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseClassRepository courseClassRepository;

    @Override
    public Enrollment getEnrollmentById(Integer id) {
        return enrollmentRepository.getEnrollmentById(id);
    }

    @Override
    public Enrollment getEnrollmentByIdForUpdate(Integer id) {
        return enrollmentRepository.getEnrollmentByIdForUpdate(id);
    }

    @Override
    public Enrollment getEnrollment(Integer studentId, Integer classId) {
        return enrollmentRepository.getEnrollment(studentId, classId);
    }

    @Override
    public long countOccupiedByClass(Integer classId) {
        return enrollmentRepository.countOccupiedByClass(classId);
    }

    @Override
    public Enrollment addEnrollment(Enrollment enrollment) {
        if(enrollment == null || enrollment.getStudent() == null || enrollment.getCourseClass() == null)
            throw new IllegalArgumentException("Thông tin đăng ký không hợp lệ!");

        User student = userRepository.getUserByIdForUpdate(enrollment.getStudent().getId());
        if(student == null)
            throw new IllegalArgumentException("Học viên không tồn tại!");

        CourseClass courseClass = courseClassRepository
                .getClassByIdForUpdate(enrollment.getCourseClass().getId());
        if(courseClass == null)
            throw new IllegalArgumentException("Lớp học không tồn tại!");

        enrollment.setStudent(student);
        enrollment.setCourseClass(courseClass);

        if(enrollment.getStatus() == null)
            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING_PAYMENT);

        validateEnrollment(enrollment);

        if(enrollmentRepository.existsEnrollment(student.getId(), courseClass.getId()))
            throw new IllegalArgumentException("Học viên đã từng đăng ký lớp học này!");

        if(enrollment.getStatus() != Enrollment.EnrollmentStatus.CANCELED
                && enrollmentRepository.existsBlockingEnrollmentByStudentAndCourse(
                student.getId(), courseClass.getCourse().getId()))
            throw new IllegalArgumentException(
                    "Học viên đã có đăng ký đang hoạt động hoặc chờ thanh toán trong khóa học này!");

        if(enrollment.getStatus() != Enrollment.EnrollmentStatus.CANCELED
                && enrollmentRepository.countOccupiedByClass(courseClass.getId())
                >= courseClass.getMaxStudents())
            throw new IllegalArgumentException("Lớp học đã đủ số lượng học viên!");

        return enrollmentRepository.addEnrollment(enrollment);
    }

    @Override
    public void updateEnrollment(Enrollment enrollment) {
        if(enrollment == null || enrollment.getId() == null)
            throw new IllegalArgumentException("Đăng ký không hợp lệ!");

        Enrollment current = enrollmentRepository.getEnrollmentByIdForUpdate(enrollment.getId());
        if(current == null)
            throw new IllegalArgumentException("Không tìm thấy đăng ký!");

        User student = userRepository.getUserByIdForUpdate(current.getStudent().getId());
        CourseClass courseClass = courseClassRepository.getClassByIdForUpdate(current.getCourseClass().getId());

        current.setStudent(student);
        current.setCourseClass(courseClass);
        current.setStatus(enrollment.getStatus());

        validateEnrollment(current);

        if(current.getStatus() != Enrollment.EnrollmentStatus.CANCELED
                && enrollmentRepository.existsBlockingEnrollmentByStudentAndCourseExceptId(
                student.getId(), courseClass.getCourse().getId(), current.getId()))
            throw new IllegalArgumentException(
                    "Học viên đã có đăng ký đang hoạt động hoặc chờ thanh toán trong khóa học này!");

        if(current.getStatus() != Enrollment.EnrollmentStatus.CANCELED
                && enrollmentRepository.countOccupiedByClassExceptId(
                courseClass.getId(), current.getId()) >= courseClass.getMaxStudents())
            throw new IllegalArgumentException("Lớp học đã đủ số lượng học viên!");

        enrollmentRepository.updateEnrollment(current);
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