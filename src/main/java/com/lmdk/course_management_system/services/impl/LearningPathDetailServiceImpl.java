package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.repository.LearningPathDetailRepository;
import com.lmdk.course_management_system.services.LearningPathDetailService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningPathDetailServiceImpl implements LearningPathDetailService {

    private final LearningPathDetailRepository detailRepository;

    @Override
    public LearningPathDetail getDetailById(Integer id) {
        return detailRepository.getDetailById(id);
    }

    @Override
    public LearningPathDetail addDetail(LearningPathDetail detail) {
        validateDetail(detail);

        Integer learningPathId = detail.getLearningPath().getId();
        Integer assignmentId = detail.getAssignment().getId();

        if (detailRepository.existsOrderNumber(learningPathId, detail.getOrderNumber()))
            throw new IllegalArgumentException("Thứ tự này đã tồn tại trong lộ trình!");

        if (detailRepository.existsAssignment(learningPathId, assignmentId))
            throw new IllegalArgumentException("Bài tập này đã có trong lộ trình!");

        return detailRepository.addDetail(detail);
    }

    @Override
    public void updateDetail(LearningPathDetail detail) {
        validateDetail(detail);

        Integer learningPathId = detail.getLearningPath().getId();
        Integer assignmentId = detail.getAssignment().getId();

        if (detailRepository.existsOrderNumberExceptId(
                learningPathId,
                detail.getOrderNumber(),
                detail.getId()
        ))
            throw new IllegalArgumentException("Thứ tự này đã tồn tại trong lộ trình!");

        if (detailRepository.existsAssignmentExceptId(
                learningPathId,
                assignmentId,
                detail.getId()
        ))
            throw new IllegalArgumentException("Bài tập này đã có trong lộ trình!");

        detailRepository.updateDetail(detail);
    }

    @Override
    public List<LearningPathDetail> getDetails(Map<String, String> params) {
        return detailRepository.getDetails(params);
    }

    @Override
    public List<LearningPathDetail> getDetailsByLearningPath(Integer learningPathId) {
        return detailRepository.getDetailsByLearningPath(learningPathId);
    }

    @Override
    public List<LearningPathDetail> getDetailsByLearningPaths(List<Integer> learningPathIds) {
        return detailRepository.getDetailsByLearningPaths(learningPathIds);
    }

    @Override
    public long countDetails(Map<String, String> params) {
        return detailRepository.countDetails(params);
    }

    private void validateDetail(LearningPathDetail detail) {
        LearningPath learningPath = detail.getLearningPath();
        Assignment assignment = detail.getAssignment();

        if (learningPath == null)
            throw new IllegalArgumentException("Vui lòng chọn lộ trình!");

        if (assignment == null)
            throw new IllegalArgumentException("Vui lòng chọn bài tập!");

        if (learningPath.getStatus() != LearningPath.LearningPathStatus.ACTIVE)
            throw new IllegalArgumentException("Lộ trình đang ngừng hoạt động!");

        if (assignment.getStatus() != Assignment.AssignmentStatus.ACTIVE)
            throw new IllegalArgumentException("Bài tập đang ngừng hoạt động!");

        if (!learningPath.getCourse().getId().equals(assignment.getCourse().getId()))
            throw new IllegalArgumentException("Bài tập phải thuộc cùng khóa học với lộ trình!");

        if (detail.getOrderNumber() == null || detail.getOrderNumber() < 1)
            throw new IllegalArgumentException("Thứ tự bài phải lớn hơn 0!");

        if (detail.getMinimumScore() == null
                || detail.getMinimumScore().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Điểm đạt tối thiểu không được nhỏ hơn 0!");

        if (detail.getMinimumScore().compareTo(assignment.getMaximumScore()) > 0)
            throw new IllegalArgumentException("Điểm đạt tối thiểu không được lớn hơn điểm tối đa!");

        if (detail.getMaxAttempts() == null || detail.getMaxAttempts() < 1)
            throw new IllegalArgumentException("Số lần làm tối đa phải lớn hơn 0!");
    }

    @Override
    public LearningPathDetail getNextDetail(Integer learningPathId, Integer currentOrderNumber) {
        return detailRepository.getNextDetail(
                learningPathId,
                currentOrderNumber
        );
    }

    @Override
    public LearningPathDetail getFirstDetail(Integer learningPathId) {

        List<LearningPathDetail> details =
                getDetailsByLearningPath(learningPathId);

        if(details.isEmpty())
            return null;

        return details.get(0);
    }
}