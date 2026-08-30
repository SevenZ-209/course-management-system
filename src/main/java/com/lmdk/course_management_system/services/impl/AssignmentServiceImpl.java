package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.repository.AssignmentRepository;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.pojo.Lesson;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;

    @Override
    public Assignment getAssignmentById(Integer id) {
        return assignmentRepository.getAssignmentById(id);
    }

    @Override
    public Assignment addAssignment(Assignment assignment) {

        validateAssignment(assignment);

        if (assignment.getLesson() != null
                && assignmentRepository.existsByLessonId(
                assignment.getLesson().getId()
        )) {

            throw new IllegalStateException(
                    "Bài học này đã có bài tập!"
            );
        }

        if (assignment.getStatus() == null)
            assignment.setStatus(
                    Assignment.AssignmentStatus.ACTIVE
            );

        return assignmentRepository.addAssignment(assignment);
    }

    @Override
    public void updateAssignment(Assignment assignment) {
        validateAssignment(assignment);
        assignmentRepository.updateAssignment(assignment);
    }

    @Override
    public List<Assignment> getAssignments(Map<String, String> params) {
        return assignmentRepository.getAssignments(params);
    }

    @Override
    public List<Assignment> getAssignmentsByCourse(Integer courseId) {
        return assignmentRepository.getAssignmentsByCourse(courseId);
    }

    @Override
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.getAllAssignments();
    }

    @Override
    public long countAssignments(Map<String, String> params) {
        return assignmentRepository.countAssignments(params);
    }

    private void validateAssignment(Assignment assignment) {
        if (assignment.getName() == null || assignment.getName().trim().isBlank())
            throw new IllegalArgumentException("Tên bài tập không được để trống!");

        if (assignment.getCourse() == null)
            throw new IllegalArgumentException("Vui lòng chọn khóa học!");

        if (assignment.getType() == null)
            throw new IllegalArgumentException("Vui lòng chọn loại bài tập!");

        if (assignment.getMaximumScore() == null
                || assignment.getMaximumScore().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Điểm tối đa phải lớn hơn 0!");

        if (assignment.getType() == Assignment.AssignmentType.TEST
                && (assignment.getDurationMinutes() == null || assignment.getDurationMinutes() <= 0))
            throw new IllegalArgumentException("Bài kiểm tra phải có thời gian làm bài!");

        if (assignment.getDurationMinutes() != null && assignment.getDurationMinutes() <= 0)
            throw new IllegalArgumentException("Thời gian làm bài phải lớn hơn 0!");

        assignment.setName(assignment.getName().trim());
    }
}