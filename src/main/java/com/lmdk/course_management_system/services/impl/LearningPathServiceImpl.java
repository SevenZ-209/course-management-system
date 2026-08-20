package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.repository.LearningPathRepository;
import com.lmdk.course_management_system.services.LearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathRepository learningPathRepository;

    @Override
    public LearningPath getLearningPathById(Integer id) {
        return learningPathRepository.getLearningPathById(id);
    }

    @Override
    public LearningPath addLearningPath(LearningPath learningPath) {
        validateLearningPath(learningPath);

        if (learningPath.getStatus() == null)
            learningPath.setStatus(LearningPath.LearningPathStatus.ACTIVE);

        return learningPathRepository.addLearningPath(learningPath);
    }

    @Override
    public void updateLearningPath(LearningPath learningPath) {
        validateLearningPath(learningPath);
        learningPathRepository.updateLearningPath(learningPath);
    }

    @Override
    public List<LearningPath> getLearningPaths(Map<String, String> params) {
        return learningPathRepository.getLearningPaths(params);
    }

    @Override
    public List<LearningPath> getLearningPathsByCourse(Integer courseId) {
        return learningPathRepository.getLearningPathsByCourse(courseId);
    }

    @Override
    public List<LearningPath> getAllLearningPaths() {
        return learningPathRepository.getAllLearningPaths();
    }

    @Override
    public long countLearningPaths(Map<String, String> params) {
        return learningPathRepository.countLearningPaths(params);
    }

    private void validateLearningPath(LearningPath learningPath) {
        if (learningPath.getName() == null || learningPath.getName().trim().isBlank())
            throw new IllegalArgumentException("Tên lộ trình không được để trống!");

        if (learningPath.getCourse() == null)
            throw new IllegalArgumentException("Vui lòng chọn khóa học!");

        if (learningPath.getAssignmentsPerDay() == null || learningPath.getAssignmentsPerDay() < 1)
            throw new IllegalArgumentException("Số bài mỗi ngày phải lớn hơn 0!");

        learningPath.setName(learningPath.getName().trim());
    }
}