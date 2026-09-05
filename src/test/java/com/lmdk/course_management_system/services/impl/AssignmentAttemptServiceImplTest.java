package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.helpers.AssignmentAttemptHelper;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.Question;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.AssignmentAttemptRepository;
import com.lmdk.course_management_system.repository.QuestionRepository;
import com.lmdk.course_management_system.repository.StudentAnswerRepository;
import com.lmdk.course_management_system.services.AssignedAssignmentService;
import com.lmdk.course_management_system.services.EnrollmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentAttemptServiceImplTest {

    @Mock private AssignmentAttemptRepository assignmentAttemptRepository;
    @Mock private AssignedAssignmentService assignedAssignmentService;
    @Mock private QuestionRepository questionRepository;
    @Mock private StudentAnswerRepository studentAnswerRepository;
    @Mock private EnrollmentService enrollmentService;

    private AssignmentAttemptServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssignmentAttemptServiceImpl(
                assignmentAttemptRepository,
                assignedAssignmentService,
                questionRepository,
                studentAnswerRepository,
                enrollmentService,
                new AssignmentAttemptHelper()
        );
    }

    @Test
    void startAttempt_returnsExistingInProgressInsteadOfCreatingDuplicate() {
        AssignedAssignment assigned = assignedAssignment(1);
        AssignmentAttempt existing = attempt(50, assigned, AssignmentAttempt.AttemptStatus.IN_PROGRESS);

        when(assignedAssignmentService.getAssignedAssignmentByIdForUpdate(10)).thenReturn(assigned);
        when(enrollmentService.existsActiveEnrollmentByStudentAndCourse(1, 100)).thenReturn(true);
        when(assignmentAttemptRepository.getInProgressAttempt(10)).thenReturn(existing);

        AssignmentAttempt result = service.startAttempt(10, 1);

        assertSame(existing, result);
        verify(assignmentAttemptRepository, never()).addAttempt(any());
        verify(assignmentAttemptRepository, never()).getLatestAttempt(anyInt());
    }

    @Test
    void startAttempt_rejectsDifferentStudent() {
        AssignedAssignment assigned = assignedAssignment(2);
        when(assignedAssignmentService.getAssignedAssignmentByIdForUpdate(10)).thenReturn(assigned);

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> service.startAttempt(10, 1));

        assertEquals("Bạn không có quyền làm bài này!", ex.getMessage());
        verify(enrollmentService, never()).existsActiveEnrollmentByStudentAndCourse(anyInt(), anyInt());
        verify(assignmentAttemptRepository, never()).addAttempt(any());
    }

    @Test
    void startAttempt_rejectsWhenPreviousAttemptIsStillSubmitted() {
        AssignedAssignment assigned = assignedAssignment(1);
        AssignmentAttempt previous = attempt(49, assigned, AssignmentAttempt.AttemptStatus.SUBMITTED);

        when(assignedAssignmentService.getAssignedAssignmentByIdForUpdate(10)).thenReturn(assigned);
        when(enrollmentService.existsActiveEnrollmentByStudentAndCourse(1, 100)).thenReturn(true);
        when(assignmentAttemptRepository.getInProgressAttempt(10)).thenReturn(null);
        when(assignmentAttemptRepository.getLatestAttempt(10)).thenReturn(previous);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.startAttempt(10, 1));

        assertEquals("Bài làm trước đang chờ xử lý!", ex.getMessage());
        verify(assignmentAttemptRepository, never()).addAttempt(any());
    }

    @Test
    void submitAttempt_rejectsDuplicateSubmit() {
        AssignedAssignment assigned = assignedAssignment(1);
        AssignmentAttempt attempt = attempt(50, assigned, AssignmentAttempt.AttemptStatus.SUBMITTED);

        when(assignmentAttemptRepository.getAttemptByIdForUpdate(50)).thenReturn(attempt);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.submitAttempt(50, 1));

        assertEquals("Bài làm không ở trạng thái đang thực hiện!", ex.getMessage());
        verify(assignmentAttemptRepository, never()).updateAttempt(any());
    }

    @Test
    void submitAttempt_rejectsIncompleteAnswersBeforeDeadline() {
        AssignedAssignment assigned = assignedAssignment(1);
        AssignmentAttempt attempt = attempt(50, assigned, AssignmentAttempt.AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(2));

        when(assignmentAttemptRepository.getAttemptByIdForUpdate(50)).thenReturn(attempt);
        when(questionRepository.getQuestionsByAssignment(200)).thenReturn(List.of(new Question(), new Question()));
        when(studentAnswerRepository.countStudentAnswersByAttempt(50)).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.submitAttempt(50, 1));

        assertEquals("Vui lòng trả lời đầy đủ các câu hỏi trước khi nộp bài!", ex.getMessage());
        assertEquals(AssignmentAttempt.AttemptStatus.IN_PROGRESS, attempt.getStatus());
        verify(assignmentAttemptRepository, never()).updateAttempt(any());
    }

    @Test
    void submitAttempt_completeAnswersMovesToSubmitted() {
        AssignedAssignment assigned = assignedAssignment(1);
        AssignmentAttempt attempt = attempt(50, assigned, AssignmentAttempt.AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(2));

        when(assignmentAttemptRepository.getAttemptByIdForUpdate(50)).thenReturn(attempt);
        when(questionRepository.getQuestionsByAssignment(200)).thenReturn(List.of(new Question(), new Question()));
        when(studentAnswerRepository.countStudentAnswersByAttempt(50)).thenReturn(2L);

        AssignmentAttempt submitted = service.submitAttempt(50, 1);

        assertEquals(AssignmentAttempt.AttemptStatus.SUBMITTED, submitted.getStatus());
        assertNotNull(submitted.getSubmittedAt());
        assertNotNull(submitted.getDurationSeconds());
        assertTrue(submitted.getDurationSeconds() >= 0);
        verify(assignmentAttemptRepository).updateAttempt(submitted);
    }

    @Test
    void submitAttempt_expiredAttemptCanSubmitEvenIfAnswersAreIncomplete() {
        AssignedAssignment assigned = assignedAssignment(1);
        AssignmentAttempt attempt = attempt(50, assigned, AssignmentAttempt.AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(40));

        when(assignmentAttemptRepository.getAttemptByIdForUpdate(50)).thenReturn(attempt);
        when(questionRepository.getQuestionsByAssignment(200)).thenReturn(List.of(new Question(), new Question()));
        when(studentAnswerRepository.countStudentAnswersByAttempt(50)).thenReturn(1L);

        AssignmentAttempt submitted = service.submitAttempt(50, 1);

        assertEquals(AssignmentAttempt.AttemptStatus.SUBMITTED, submitted.getStatus());
        assertEquals(1800, submitted.getDurationSeconds());
        verify(assignmentAttemptRepository).updateAttempt(submitted);
    }

    private AssignedAssignment assignedAssignment(Integer studentId) {
        User student = new User();
        student.setId(studentId);
        student.setRole(User.UserRole.STUDENT);
        student.setStatus(User.UserStatus.ACTIVE);

        Course course = new Course();
        course.setId(100);

        Assignment assignment = new Assignment();
        assignment.setId(200);
        assignment.setCourse(course);
        assignment.setDurationMinutes(30);
        assignment.setStatus(Assignment.AssignmentStatus.ACTIVE);

        AssignedAssignment assigned = new AssignedAssignment();
        assigned.setId(10);
        assigned.setStudent(student);
        assigned.setAssignment(assignment);
        assigned.setStatus(AssignedAssignment.AssignedStatus.AVAILABLE);
        return assigned;
    }

    private AssignmentAttempt attempt(
            Integer id,
            AssignedAssignment assigned,
            AssignmentAttempt.AttemptStatus status) {
        AssignmentAttempt attempt = new AssignmentAttempt();
        attempt.setId(id);
        attempt.setAssignedAssignment(assigned);
        attempt.setAttemptNumber(1);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(1));
        attempt.setStatus(status);
        attempt.setPassed(false);
        return attempt;
    }
}
