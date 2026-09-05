package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.admin.enrollment.CreateAdminEnrollmentRequest;
import com.lmdk.course_management_system.mappers.admin.AdminEnrollmentMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiManagerEnrollmentControllerTest {

    @Mock private EnrollmentService enrollmentService;
    @Mock private CourseClassService classService;
    @Mock private UserService userService;
    @Mock private AdminEnrollmentMapper enrollmentMapper;

    private ApiManagerEnrollmentController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiManagerEnrollmentController(
                enrollmentService, classService, userService, enrollmentMapper
        );
    }

    @Test
    void addEnrollment_alwaysCreatesPendingPayment() {
        User student = student(1);
        CourseClass courseClass = courseClass(10);
        when(userService.getUserById(1)).thenReturn(student);
        when(classService.getClassById(10)).thenReturn(courseClass);
        when(enrollmentService.addEnrollment(any(Enrollment.class))).thenAnswer(i -> {
            Enrollment enrollment = i.getArgument(0);
            enrollment.setId(50);
            return enrollment;
        });

        var response = controller.addEnrollment(new CreateAdminEnrollmentRequest(1, 10));

        assertEquals(50, response.enrollmentId());
        verify(enrollmentService).addEnrollment(argThat(e ->
                e.getStudent() == student
                        && e.getCourseClass() == courseClass
                        && e.getStatus() == Enrollment.EnrollmentStatus.PENDING_PAYMENT
        ));
    }

    @Test
    void addEnrollment_rejectsNonStudentUser() {
        User teacher = new User();
        teacher.setId(2);
        teacher.setRole(User.UserRole.TEACHER);
        teacher.setStatus(User.UserStatus.ACTIVE);
        when(userService.getUserById(2)).thenReturn(teacher);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.addEnrollment(new CreateAdminEnrollmentRequest(2, 10))
        );

        assertEquals("Người dùng không phải học viên!", ex.getMessage());
        verifyNoInteractions(classService);
        verify(enrollmentService, never()).addEnrollment(any());
    }

    @Test
    void cancelEnrollment_changesStatusToCanceled() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(50);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        when(enrollmentService.getEnrollmentById(50)).thenReturn(enrollment);

        var response = controller.cancelEnrollment(50);

        assertEquals(50, response.enrollmentId());
        assertEquals(Enrollment.EnrollmentStatus.CANCELED, enrollment.getStatus());
        verify(enrollmentService).updateEnrollment(enrollment);
    }

    @Test
    void cancelEnrollment_rejectsAlreadyCanceledEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(50);
        enrollment.setStatus(Enrollment.EnrollmentStatus.CANCELED);
        when(enrollmentService.getEnrollmentById(50)).thenReturn(enrollment);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.cancelEnrollment(50)
        );

        assertEquals("Đăng ký này đã bị hủy!", ex.getMessage());
        verify(enrollmentService, never()).updateEnrollment(any());
    }

    private User student(Integer id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.UserRole.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private CourseClass courseClass(Integer id) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(id);
        return courseClass;
    }
}
