package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.*;
import com.lmdk.course_management_system.services.AssignedAssignmentService;
import com.lmdk.course_management_system.services.AssignmentAttemptService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.StudentLearningPathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradingResultServiceOwnershipTest {

    @Mock private GradingResultRepository gradingResultRepository;
    @Mock private AssignmentAttemptRepository assignmentAttemptRepository;
    @Mock private StudentAnswerRepository studentAnswerRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private AssignedAssignmentRepository assignedAssignmentRepository;
    @Mock private StudentLearningPathService studentLearningPathService;
    @Mock private EnrollmentService enrollmentService;
    @Mock private AssignedAssignmentService assignedAssignmentService;
    @Mock private AssignmentAttemptService assignmentAttemptService;

    private GradingResultServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GradingResultServiceImpl(
                gradingResultRepository,
                assignmentAttemptRepository,
                studentAnswerRepository,
                questionRepository,
                assignedAssignmentRepository,
                studentLearningPathService,
                enrollmentService,
                assignedAssignmentService,
                assignmentAttemptService
        );
    }

    @Test
    void teacherCannotOpenAttemptOutsideAssignedCourse() {
        AssignmentAttempt attempt = pendingAttempt(1, 100);
        User teacher = user(4, User.UserRole.TEACHER);

        when(assignmentAttemptRepository.getAttemptById(50)).thenReturn(attempt);
        when(enrollmentService.existsActiveEnrollmentByStudentCourseAndTeacher(1, 100, 4))
                .thenReturn(false);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> service.getAttemptForGrading(50, teacher)
        );

        assertEquals("Bạn không được phân công chấm bài của học viên này!", ex.getMessage());
    }

    @Test
    void teacherCanOpenAttemptForAssignedCourse() {
        AssignmentAttempt attempt = pendingAttempt(1, 100);
        User teacher = user(4, User.UserRole.TEACHER);

        when(assignmentAttemptRepository.getAttemptById(50)).thenReturn(attempt);
        when(enrollmentService.existsActiveEnrollmentByStudentCourseAndTeacher(1, 100, 4))
                .thenReturn(true);

        assertSame(attempt, service.getAttemptForGrading(50, teacher));
    }

    @Test
    void adminCanOpenPendingAttemptWithoutTeacherAssignment() {
        AssignmentAttempt attempt = pendingAttempt(1, 100);
        User admin = user(9, User.UserRole.ADMIN);

        when(assignmentAttemptRepository.getAttemptById(50)).thenReturn(attempt);

        assertSame(attempt, service.getAttemptForGrading(50, admin));
        verify(enrollmentService, never())
                .existsActiveEnrollmentByStudentCourseAndTeacher(anyInt(), anyInt(), anyInt());
    }

    @Test
    void parentCannotGradeAttempt() {
        AssignmentAttempt attempt = pendingAttempt(1, 100);
        User parent = user(3, User.UserRole.PARENT);

        when(assignmentAttemptRepository.getAttemptById(50)).thenReturn(attempt);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> service.getAttemptForGrading(50, parent)
        );

        assertEquals("Tài khoản không có quyền chấm bài!", ex.getMessage());
    }

    private AssignmentAttempt pendingAttempt(Integer studentId, Integer courseId) {
        User student = user(studentId, User.UserRole.STUDENT);

        Course course = new Course();
        course.setId(courseId);

        Assignment assignment = new Assignment();
        assignment.setId(200);
        assignment.setCourse(course);

        AssignedAssignment assigned = new AssignedAssignment();
        assigned.setId(10);
        assigned.setStudent(student);
        assigned.setAssignment(assignment);

        AssignmentAttempt attempt = new AssignmentAttempt();
        attempt.setId(50);
        attempt.setAssignedAssignment(assigned);
        attempt.setStatus(AssignmentAttempt.AttemptStatus.PENDING_GRADING);
        return attempt;
    }

    private User user(Integer id, User.UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }
}
