package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.repository.AssignmentAttemptRepository;
import com.lmdk.course_management_system.repository.QuestionRepository;
import com.lmdk.course_management_system.repository.StudentAnswerRepository;
import com.lmdk.course_management_system.services.AssignedAssignmentService;
import com.lmdk.course_management_system.services.AssignmentAttemptService;

import com.lmdk.course_management_system.services.EnrollmentService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentAttemptServiceImpl implements AssignmentAttemptService {

    private final AssignmentAttemptRepository assignmentAttemptRepository;
    private final AssignedAssignmentService assignedAssignmentService;
    private final QuestionRepository questionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final EnrollmentService enrollmentService;
    



    @Override
    public AssignmentAttempt getAttemptById(Integer id) {
        return assignmentAttemptRepository.getAttemptById(id);
    }

    @Override
    public AssignmentAttempt getLatestAttempt(Integer assignedAssignmentId) {
        return assignmentAttemptRepository.getLatestAttempt(assignedAssignmentId);
    }

    @Override
    public AssignmentAttempt getInProgressAttempt(Integer assignedAssignmentId) {
        return assignmentAttemptRepository.getInProgressAttempt(assignedAssignmentId);
    }

    @Override
    public AssignmentAttempt startAttempt(Integer assignedAssignmentId, Integer studentId) {
        AssignedAssignment assigned =
                assignedAssignmentService.getAssignedAssignmentById(assignedAssignmentId);

        validateCanStart(assigned, studentId);

        AssignmentAttempt inProgress =
                assignmentAttemptRepository.getInProgressAttempt(assignedAssignmentId);

        if (inProgress != null)
            return inProgress;

        AssignmentAttempt latest =
                assignmentAttemptRepository.getLatestAttempt(assignedAssignmentId);

        validatePreviousAttempt(latest);
        validateMaxAttempts(assigned, latest);

        int attemptNumber = latest == null
                ? 1
                : latest.getAttemptNumber() + 1;

        AssignmentAttempt attempt = new AssignmentAttempt();
        attempt.setAssignedAssignment(assigned);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setSubmittedAt(null);
        attempt.setDurationSeconds(null);
        attempt.setPassed(false);
        attempt.setStatus(AssignmentAttempt.AttemptStatus.IN_PROGRESS);

        return assignmentAttemptRepository.addAttempt(attempt);
    }

    @Override
    public AssignmentAttempt submitAttempt(Integer attemptId, Integer studentId) {
        AssignmentAttempt attempt =
                assignmentAttemptRepository.getAttemptById(attemptId);

        if (attempt == null)
            throw new IllegalArgumentException("Lần làm bài không tồn tại!");

        AssignedAssignment assigned = attempt.getAssignedAssignment();

        if (!assigned.getStudent().getId().equals(studentId))
            throw new IllegalArgumentException(
                    "Bạn không có quyền nộp bài làm này!"
            );

        if (attempt.getStatus() != AssignmentAttempt.AttemptStatus.IN_PROGRESS)
            throw new IllegalArgumentException(
                    "Bài làm không ở trạng thái đang thực hiện!"
            );

        if (attempt.getStartedAt() == null)
            throw new IllegalStateException(
                    "Lần làm bài không có thời gian bắt đầu!"
            );

        validateSubmissionTime(attempt);

        Integer assignmentId = assigned.getAssignment().getId();

        long totalQuestions =
                questionRepository.getQuestionsByAssignment(assignmentId).size();

        long totalAnswers =
                studentAnswerRepository.countStudentAnswersByAttempt(attemptId);

        if (totalAnswers < totalQuestions)
            throw new IllegalArgumentException(
                    "Vui lòng trả lời đầy đủ các câu hỏi trước khi nộp bài!"
            );

        LocalDateTime now = LocalDateTime.now();

        attempt.setSubmittedAt(now);

        long duration = Duration.between(
                attempt.getStartedAt(),
                now
        ).getSeconds();

        attempt.setDurationSeconds(
                Math.toIntExact(Math.max(duration, 0))
        );

        attempt.setStatus(AssignmentAttempt.AttemptStatus.SUBMITTED);

        assignmentAttemptRepository.updateAttempt(attempt);

        return attempt;
    }

    private void validateSubmissionTime(AssignmentAttempt attempt) {
        LocalDateTime now = LocalDateTime.now();
        AssignedAssignment assigned = attempt.getAssignedAssignment();

        if (assigned.getDueAt() != null
                && now.isAfter(assigned.getDueAt()))
            throw new IllegalArgumentException(
                    "Bài tập đã quá hạn nộp!"
            );

        Integer durationMinutes =
                assigned.getAssignment().getDurationMinutes();

        if (durationMinutes != null && durationMinutes > 0) {
            LocalDateTime endTime =
                    attempt.getStartedAt().plusMinutes(durationMinutes);

            if (now.isAfter(endTime))
                throw new IllegalArgumentException(
                        "Đã hết thời gian làm bài!"
                );
        }
    }

    @Override
    public List<AssignmentAttempt> getAttempts(Map<String, String> params) {
        return assignmentAttemptRepository.getAttempts(params);
    }

    @Override
    public List<AssignmentAttempt> getAttemptsByAssignedAssignment(
            Integer assignedAssignmentId) {
        return assignmentAttemptRepository
                .getAttemptsByAssignedAssignment(assignedAssignmentId);
    }

    @Override
    public long countAttempts(Map<String, String> params) {
        return assignmentAttemptRepository.countAttempts(params);
    }

    private void validateCanStart(AssignedAssignment assigned,
                                  Integer studentId) {
        if (assigned == null)
            throw new IllegalArgumentException(
                    "Bài được giao không tồn tại!"
            );

        if (!assigned.getStudent().getId().equals(studentId))
            throw new IllegalArgumentException(
                    "Bạn không có quyền làm bài này!"
            );

        Integer courseId =
                assigned.getAssignment()
                        .getCourse()
                        .getId();

        if (!enrollmentService.existsActiveEnrollmentByStudentAndCourse(
                studentId,
                courseId))
            throw new IllegalArgumentException(
                    "Bạn không còn đăng ký hợp lệ với khóa học này!"
            );

        if (assigned.getStatus()
                == AssignedAssignment.AssignedStatus.COMPLETED)
            throw new IllegalArgumentException(
                    "Bài tập đã được hoàn thành!"
            );

        LocalDateTime now = LocalDateTime.now();

        if (assigned.getAvailableAt() != null
                && now.isBefore(assigned.getAvailableAt()))
            throw new IllegalArgumentException(
                    "Bài tập chưa đến thời gian mở!"
            );

        if (assigned.getDueAt() != null
                && now.isAfter(assigned.getDueAt()))
            throw new IllegalArgumentException(
                    "Bài tập đã hết hạn!"
            );

        if (assigned.getStatus()
                == AssignedAssignment.AssignedStatus.LOCKED) {
            assignedAssignmentService.updateAvailabilityStatus(
                    assigned.getId(),
                    AssignedAssignment.AssignedStatus.AVAILABLE
            );
        }

        if (assigned.getStatus()
                != AssignedAssignment.AssignedStatus.AVAILABLE
                && assigned.getStatus()
                != AssignedAssignment.AssignedStatus.LOCKED)
            throw new IllegalArgumentException(
                    "Bài tập hiện không thể thực hiện!"
            );
    }

    private void validatePreviousAttempt(AssignmentAttempt latest) {
        if (latest == null)
            return;

        if (latest.getStatus() == AssignmentAttempt.AttemptStatus.SUBMITTED)
            throw new IllegalArgumentException(
                    "Bài làm trước đang chờ xử lý!"
            );

        if (latest.getStatus() == AssignmentAttempt.AttemptStatus.PENDING_GRADING)
            throw new IllegalArgumentException(
                    "Bài làm trước đang chờ giáo viên chấm!"
            );

        if (latest.getStatus() == AssignmentAttempt.AttemptStatus.GRADED
                && Boolean.TRUE.equals(latest.getPassed()))
            throw new IllegalArgumentException(
                    "Bạn đã đạt yêu cầu của bài tập!"
            );
    }

    private void validateMaxAttempts(AssignedAssignment assigned,
                                     AssignmentAttempt latest) {
        LearningPathDetail detail = assigned.getLearningPathDetail();

        if (detail == null)
            return;

        Integer maxAttempts = detail.getMaxAttempts();

        if (maxAttempts == null || maxAttempts < 1)
            throw new IllegalStateException(
                    "Số lần làm tối đa của lộ trình không hợp lệ!"
            );

        int currentAttempts = latest == null
                ? 0
                : latest.getAttemptNumber();

        if (currentAttempts >= maxAttempts)
            throw new IllegalArgumentException(
                    "Bạn đã sử dụng hết số lần làm bài!"
            );
    }
}