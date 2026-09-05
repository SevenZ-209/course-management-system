package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.CourseClassRepository;
import com.lmdk.course_management_system.repository.EnrollmentRepository;
import com.lmdk.course_management_system.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseClassRepository courseClassRepository;

    private EnrollmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EnrollmentServiceImpl(enrollmentRepository, userRepository, courseClassRepository);
    }

    @Test
    void addEnrollment_blocksAnotherClassOfSameCourse() {
        User student = student(1);
        CourseClass courseClass = courseClass(11, 100, 30);
        Enrollment enrollment = enrollment(student, courseClass, Enrollment.EnrollmentStatus.PENDING_PAYMENT);

        when(userRepository.getUserByIdForUpdate(1)).thenReturn(student);
        when(courseClassRepository.getClassByIdForUpdate(11)).thenReturn(courseClass);
        when(enrollmentRepository.existsEnrollment(1, 11)).thenReturn(false);
        when(enrollmentRepository.existsBlockingEnrollmentByStudentAndCourse(1, 100)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addEnrollment(enrollment));

        assertEquals("Học viên đã có đăng ký đang hoạt động hoặc chờ thanh toán trong khóa học này!", ex.getMessage());
        verify(enrollmentRepository, never()).addEnrollment(any());
    }

    @Test
    void addEnrollment_blocksWhenClassIsFull() {
        User student = student(1);
        CourseClass courseClass = courseClass(11, 100, 2);
        Enrollment enrollment = enrollment(student, courseClass, Enrollment.EnrollmentStatus.PENDING_PAYMENT);

        when(userRepository.getUserByIdForUpdate(1)).thenReturn(student);
        when(courseClassRepository.getClassByIdForUpdate(11)).thenReturn(courseClass);
        when(enrollmentRepository.existsEnrollment(1, 11)).thenReturn(false);
        when(enrollmentRepository.existsBlockingEnrollmentByStudentAndCourse(1, 100)).thenReturn(false);
        when(enrollmentRepository.countOccupiedByClass(11)).thenReturn(2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addEnrollment(enrollment));

        assertEquals("Lớp học đã đủ số lượng học viên!", ex.getMessage());
        verify(enrollmentRepository, never()).addEnrollment(any());
    }

    @Test
    void addEnrollment_successCreatesPendingPaymentEnrollment() {
        User student = student(1);
        CourseClass courseClass = courseClass(11, 100, 30);
        Enrollment enrollment = enrollment(student, courseClass, null);

        when(userRepository.getUserByIdForUpdate(1)).thenReturn(student);
        when(courseClassRepository.getClassByIdForUpdate(11)).thenReturn(courseClass);
        when(enrollmentRepository.existsEnrollment(1, 11)).thenReturn(false);
        when(enrollmentRepository.existsBlockingEnrollmentByStudentAndCourse(1, 100)).thenReturn(false);
        when(enrollmentRepository.countOccupiedByClass(11)).thenReturn(5L);
        when(enrollmentRepository.addEnrollment(any(Enrollment.class))).thenAnswer(i -> i.getArgument(0));

        Enrollment saved = service.addEnrollment(enrollment);

        assertSame(student, saved.getStudent());
        assertSame(courseClass, saved.getCourseClass());
        assertEquals(Enrollment.EnrollmentStatus.PENDING_PAYMENT, saved.getStatus());
        verify(enrollmentRepository).addEnrollment(saved);
    }

    @Test
    void addCanceledEnrollment_doesNotConsumeCapacityOrBlockSameCourse() {
        User student = student(1);
        CourseClass courseClass = courseClass(11, 100, 1);
        Enrollment enrollment = enrollment(student, courseClass, Enrollment.EnrollmentStatus.CANCELED);

        when(userRepository.getUserByIdForUpdate(1)).thenReturn(student);
        when(courseClassRepository.getClassByIdForUpdate(11)).thenReturn(courseClass);
        when(enrollmentRepository.existsEnrollment(1, 11)).thenReturn(false);
        when(enrollmentRepository.addEnrollment(any(Enrollment.class))).thenAnswer(i -> i.getArgument(0));

        Enrollment saved = service.addEnrollment(enrollment);

        assertEquals(Enrollment.EnrollmentStatus.CANCELED, saved.getStatus());
        verify(enrollmentRepository, never()).existsBlockingEnrollmentByStudentAndCourse(anyInt(), anyInt());
        verify(enrollmentRepository, never()).countOccupiedByClass(anyInt());
    }

    private User student(Integer id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.UserRole.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private CourseClass courseClass(Integer classId, Integer courseId, Integer maxStudents) {
        Course course = new Course();
        course.setId(courseId);

        CourseClass courseClass = new CourseClass();
        courseClass.setId(classId);
        courseClass.setCourse(course);
        courseClass.setMaxStudents(maxStudents);
        courseClass.setStatus(CourseClass.ClassStatus.ACTIVE);
        return courseClass;
    }

    private Enrollment enrollment(User student, CourseClass courseClass, Enrollment.EnrollmentStatus status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourseClass(courseClass);
        enrollment.setStatus(status);
        return enrollment;
    }
}
