package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.ParentLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnershipHelpersTest {

    @Mock private EnrollmentService enrollmentService;
    @Mock private ParentLinkService parentLinkService;
    @Mock private CourseClassService classService;

    private StudentAccessHelper studentAccessHelper;
    private ParentAccessHelper parentAccessHelper;
    private TeacherAccessHelper teacherAccessHelper;

    @BeforeEach
    void setUp() {
        studentAccessHelper = new StudentAccessHelper(enrollmentService);
        parentAccessHelper = new ParentAccessHelper(parentLinkService);
        teacherAccessHelper = new TeacherAccessHelper(classService);
    }

    @Test
    void studentActiveCourse_blocksStudentWithoutActiveEnrollment() {
        when(enrollmentService.existsActiveEnrollmentByStudentAndCourse(1, 100)).thenReturn(false);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> studentAccessHelper.requireActiveCourse(1, 100)
        );

        assertEquals("Bạn chưa được kích hoạt trong khóa học này!", ex.getMessage());
    }

    @Test
    void studentOwnedEnrollment_blocksForeignEnrollment() {
        Enrollment enrollment = enrollment(2, Enrollment.EnrollmentStatus.ACTIVE);
        when(enrollmentService.getEnrollmentById(10)).thenReturn(enrollment);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> studentAccessHelper.requireOwnedActiveEnrollment(1, 10)
        );

        assertEquals("Bạn không có quyền xem khóa học này!", ex.getMessage());
    }

    @Test
    void studentOwnedEnrollment_returnsOwnActiveEnrollment() {
        Enrollment enrollment = enrollment(1, Enrollment.EnrollmentStatus.ACTIVE);
        when(enrollmentService.getEnrollmentById(10)).thenReturn(enrollment);

        assertSame(
                enrollment,
                studentAccessHelper.requireOwnedActiveEnrollment(1, 10)
        );
    }

    @Test
    void parentAccess_returnsLinkedStudent() {
        User parent = user(3, User.UserRole.PARENT);
        User student = user(8, User.UserRole.STUDENT);
        ParentLink link = parentLink(parent, student, ParentLink.ParentLinkStatus.USED);

        when(parentLinkService.getParentLinksByParent(3)).thenReturn(List.of(link));

        assertSame(student, parentAccessHelper.requireLinkedStudent(parent, 8));
    }

    @Test
    void parentAccess_blocksUnlinkedStudentId() {
        User parent = user(3, User.UserRole.PARENT);
        User linkedStudent = user(8, User.UserRole.STUDENT);
        ParentLink link = parentLink(parent, linkedStudent, ParentLink.ParentLinkStatus.USED);

        when(parentLinkService.getParentLinksByParent(3)).thenReturn(List.of(link));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> parentAccessHelper.requireLinkedStudent(parent, 99)
        );

        assertEquals("Bạn không có quyền xem học viên này!", ex.getMessage());
    }

    @Test
    void parentAccess_doesNotAcceptUnlinkedHistoryRecord() {
        User parent = user(3, User.UserRole.PARENT);
        User student = user(8, User.UserRole.STUDENT);
        ParentLink link = parentLink(parent, student, ParentLink.ParentLinkStatus.UNLINKED);

        when(parentLinkService.getParentLinksByParent(3)).thenReturn(List.of(link));

        assertThrows(
                ForbiddenException.class,
                () -> parentAccessHelper.requireLinkedStudent(parent, 8)
        );
    }

    @Test
    void teacherAccess_returnsOwnedClass() {
        User teacher = user(4, User.UserRole.TEACHER);
        CourseClass courseClass = courseClass(20, teacher);

        when(classService.getClassById(20)).thenReturn(courseClass);

        assertSame(courseClass, teacherAccessHelper.requireTeacherClass(teacher, 20));
    }

    @Test
    void teacherAccess_blocksClassOwnedByAnotherTeacher() {
        User teacherA = user(4, User.UserRole.TEACHER);
        User teacherB = user(5, User.UserRole.TEACHER);
        CourseClass courseClass = courseClass(20, teacherB);

        when(classService.getClassById(20)).thenReturn(courseClass);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> teacherAccessHelper.requireTeacherClass(teacherA, 20)
        );

        assertEquals("Bạn không có quyền truy cập lớp học này!", ex.getMessage());
    }

    @Test
    void teacherAccess_blocksNonTeacherEvenWhenClassExists() {
        User manager = user(4, User.UserRole.MANAGER);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> teacherAccessHelper.requireTeacherClass(manager, 20)
        );

        assertEquals("Tài khoản không phải giáo viên!", ex.getMessage());
        verify(classService, never()).getClassById(anyInt());
    }

    private User user(Integer id, User.UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private Enrollment enrollment(Integer studentId, Enrollment.EnrollmentStatus status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(10);
        enrollment.setStudent(user(studentId, User.UserRole.STUDENT));
        enrollment.setStatus(status);
        return enrollment;
    }

    private ParentLink parentLink(
            User parent,
            User student,
            ParentLink.ParentLinkStatus status
    ) {
        ParentLink link = new ParentLink();
        link.setId(1);
        link.setParent(parent);
        link.setStudent(student);
        link.setStatus(status);
        return link;
    }

    private CourseClass courseClass(Integer id, User teacher) {
        CourseClass courseClass = new CourseClass();
        courseClass.setId(id);
        courseClass.setTeacher(teacher);
        return courseClass;
    }
}
