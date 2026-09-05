package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.AssignedAssignmentRepository;
import com.lmdk.course_management_system.repository.StudentLearningPathRepository;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.StudentLearningPathService;
import com.lmdk.course_management_system.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignedAssignmentManualDuplicateGuardTest {

    @Mock private AssignedAssignmentRepository assignedAssignmentRepository;
    @Mock private StudentLearningPathService studentLearningPathService;
    @Mock private AssignmentService assignmentService;
    @Mock private EnrollmentService enrollmentService;
    @Mock private UserService userService;
    @Mock private StudentLearningPathRepository studentLearningPathRepository;

    private AssignedAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssignedAssignmentServiceImpl(
                assignedAssignmentRepository,
                studentLearningPathService,
                assignmentService,
                enrollmentService,
                userService,
                studentLearningPathRepository
        );
    }

    @Test
    void assignManual_rejectsSameStudentAndAssignmentEvenWhenScheduleDiffers() {
        User student = student(1);
        Assignment assignment = assignment(10, 100);
        User admin = admin(99);

        when(userService.getUserById(1)).thenReturn(student);
        when(assignmentService.getAssignmentById(10)).thenReturn(assignment);
        when(enrollmentService.existsActiveEnrollmentByStudentAndCourse(1, 100)).thenReturn(true);
        when(assignedAssignmentRepository.existsByStudentAndAssignmentForUpdate(1, 10)).thenReturn(true);

        LocalDateTime availableAt = LocalDateTime.now().plusDays(2);
        LocalDateTime dueAt = availableAt.plusHours(3);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.assignManual(1, 10, admin, availableAt, dueAt)
        );

        assertEquals("Bài tập này đã được giao cho học viên!", ex.getMessage());
        verify(assignedAssignmentRepository, never()).addAssignedAssignment(any());
    }

    @Test
    void assignManual_newStudentAssignmentCombinationStillCreatesNormally() {
        User student = student(1);
        Assignment assignment = assignment(10, 100);
        User admin = admin(99);

        when(userService.getUserById(1)).thenReturn(student);
        when(assignmentService.getAssignmentById(10)).thenReturn(assignment);
        when(enrollmentService.existsActiveEnrollmentByStudentAndCourse(1, 100)).thenReturn(true);
        when(assignedAssignmentRepository.existsByStudentAndAssignmentForUpdate(1, 10)).thenReturn(false);
        when(assignedAssignmentRepository.addAssignedAssignment(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime availableAt = LocalDateTime.now().plusDays(1);
        LocalDateTime dueAt = availableAt.plusHours(2);

        AssignedAssignment result = service.assignManual(1, 10, admin, availableAt, dueAt);

        assertSame(student, result.getStudent());
        assertSame(assignment, result.getAssignment());
        assertSame(admin, result.getAssignedBy());
        assertEquals(availableAt, result.getAvailableAt());
        assertEquals(dueAt, result.getDueAt());
        assertEquals(AssignedAssignment.AssignedStatus.LOCKED, result.getStatus());
        verify(assignedAssignmentRepository).addAssignedAssignment(result);
    }

    private User student(Integer id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.UserRole.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private User admin(Integer id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.UserRole.ADMIN);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private Assignment assignment(Integer id, Integer courseId) {
        Course course = new Course();
        course.setId(courseId);

        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setCourse(course);
        assignment.setStatus(Assignment.AssignmentStatus.ACTIVE);
        return assignment;
    }
}
