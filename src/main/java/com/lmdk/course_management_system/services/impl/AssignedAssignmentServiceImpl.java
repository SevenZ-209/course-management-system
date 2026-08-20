package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.repository.AssignedAssignmentRepository;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignedAssignmentServiceImpl implements AssignedAssignmentService {

    private final AssignedAssignmentRepository assignedAssignmentRepository;
    private final StudentLearningPathService studentLearningPathService;
    private final AssignmentService assignmentService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @Override
    public AssignedAssignment getAssignedAssignmentById(Integer id) {
        return assignedAssignmentRepository.getAssignedAssignmentById(id);
    }

    @Override
    public AssignedAssignment assignCurrentDetail(Integer studentLearningPathId,
                                                  LocalDateTime availableAt,
                                                  LocalDateTime dueAt) {
        StudentLearningPath progress =
                studentLearningPathService.getStudentLearningPathById(studentLearningPathId);

        if (progress == null)
            throw new IllegalArgumentException("Không tìm thấy tiến độ học!");

        if (progress.getStatus() != StudentLearningPath.ProgressStatus.IN_PROGRESS)
            throw new IllegalArgumentException("Lộ trình của học viên không ở trạng thái đang học!");

        LearningPathDetail detail = progress.getCurrentDetail();

        if (detail == null)
            throw new IllegalArgumentException("Học viên chưa có bài hiện tại!");

        if (detail.getAssignment() == null)
            throw new IllegalArgumentException("Chi tiết lộ trình chưa có bài tập!");

        if (detail.getAssignment().getStatus() != Assignment.AssignmentStatus.ACTIVE)
            throw new IllegalArgumentException("Bài tập đang ngừng hoạt động!");

        if (assignedAssignmentRepository.existsByStudentAndLearningPathDetail(
                progress.getStudent().getId(),
                detail.getId()
        ))
            throw new IllegalArgumentException("Bài hiện tại đã được phát cho học viên!");

        LocalDateTime now = LocalDateTime.now();

        if (availableAt == null)
            availableAt = now;

        validateTime(availableAt, dueAt);

        AssignedAssignment assigned = new AssignedAssignment();
        assigned.setStudent(progress.getStudent());
        assigned.setAssignment(detail.getAssignment());
        assigned.setLearningPathDetail(detail);
        assigned.setAssignedBy(null);
        assigned.setAssignedAt(now);
        assigned.setAvailableAt(availableAt);
        assigned.setDueAt(dueAt);
        assigned.setStatus(
                availableAt.isAfter(now)
                        ? AssignedAssignment.AssignedStatus.LOCKED
                        : AssignedAssignment.AssignedStatus.AVAILABLE
        );

        return assignedAssignmentRepository.addAssignedAssignment(assigned);
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignmentsByClass(
            Integer classId
    ) {
        return assignedAssignmentRepository
                .getAssignedAssignmentsByClass(classId);
    }

    @Override
    public AssignedAssignment assignManual(Integer studentId,
                                           Integer assignmentId,
                                           User assignedBy,
                                           LocalDateTime availableAt,
                                           LocalDateTime dueAt) {
        User student = userService.getUserById(studentId);
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if (student == null)
            throw new IllegalArgumentException("Học viên không tồn tại!");

        if (student.getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException("Người dùng được chọn không phải học viên!");

        if (student.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("Tài khoản học viên không hoạt động!");

        if (assignment == null)
            throw new IllegalArgumentException("Bài tập không tồn tại!");

        if (assignment.getStatus() != Assignment.AssignmentStatus.ACTIVE)
            throw new IllegalArgumentException("Bài tập đang ngừng hoạt động!");

        if (!enrollmentService.existsActiveEnrollmentByStudentAndCourse(
                studentId,
                assignment.getCourse().getId()
        ))
            throw new IllegalArgumentException(
                    "Học viên không tham gia khóa học chứa bài tập này!"
            );

        validateAssignedBy(assignedBy);

        LocalDateTime now = LocalDateTime.now();

        if (availableAt == null)
            availableAt = now;

        validateTime(availableAt, dueAt);

        AssignedAssignment assigned = new AssignedAssignment();
        assigned.setStudent(student);
        assigned.setAssignment(assignment);
        assigned.setLearningPathDetail(null);
        assigned.setAssignedBy(assignedBy);
        assigned.setAssignedAt(now);
        assigned.setAvailableAt(availableAt);
        assigned.setDueAt(dueAt);
        assigned.setStatus(
                availableAt.isAfter(now)
                        ? AssignedAssignment.AssignedStatus.LOCKED
                        : AssignedAssignment.AssignedStatus.AVAILABLE
        );

        return assignedAssignmentRepository.addAssignedAssignment(assigned);
    }

    @Override
    public void updateAvailabilityStatus(Integer id, AssignedAssignment.AssignedStatus status) {
        AssignedAssignment assigned =
                assignedAssignmentRepository.getAssignedAssignmentById(id);

        if (assigned == null)
            throw new IllegalArgumentException("Không tìm thấy bài đã giao!");

        if (assigned.getStatus() == AssignedAssignment.AssignedStatus.COMPLETED)
            throw new IllegalArgumentException("Bài đã hoàn thành, không thể thay đổi trạng thái!");

        if (status == AssignedAssignment.AssignedStatus.COMPLETED)
            throw new IllegalArgumentException(
                    "Không thể đánh dấu hoàn thành thủ công!"
            );

        if (status == AssignedAssignment.AssignedStatus.AVAILABLE) {
            if (assigned.getDueAt() != null
                    && !assigned.getDueAt().isAfter(LocalDateTime.now()))
                throw new IllegalArgumentException("Bài đã quá hạn!");

            if (assigned.getAvailableAt() == null
                    || assigned.getAvailableAt().isAfter(LocalDateTime.now()))
                assigned.setAvailableAt(LocalDateTime.now());
        }

        assigned.setStatus(status);
        assignedAssignmentRepository.updateAssignedAssignment(assigned);
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignments(Map<String, String> params) {
        return assignedAssignmentRepository.getAssignedAssignments(params);
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignmentsByStudent(Integer studentId) {
        return assignedAssignmentRepository.getAssignedAssignmentsByStudent(studentId);
    }

    @Override
    public List<AssignedAssignment> getAssignedAssignmentsByStudentAndStatus(
            Integer studentId,
            AssignedAssignment.AssignedStatus status) {
        return assignedAssignmentRepository
                .getAssignedAssignmentsByStudentAndStatus(studentId, status);
    }

    @Override
    public long countAssignedAssignments(Map<String, String> params) {
        return assignedAssignmentRepository.countAssignedAssignments(params);
    }

    private void validateAssignedBy(User user) {
        if (user == null)
            throw new IllegalArgumentException("Không xác định được người giao bài!");

        if (user.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("Tài khoản người giao bài không hoạt động!");

        if (user.getRole() != User.UserRole.TEACHER
                && user.getRole() != User.UserRole.MANAGER
                && user.getRole() != User.UserRole.ADMIN)
            throw new IllegalArgumentException("Tài khoản không có quyền giao bài!");
    }

    private void validateTime(LocalDateTime availableAt, LocalDateTime dueAt) {
        if (dueAt != null && !dueAt.isAfter(availableAt))
            throw new IllegalArgumentException(
                    "Hạn nộp phải sau thời gian mở bài!"
            );
    }
}