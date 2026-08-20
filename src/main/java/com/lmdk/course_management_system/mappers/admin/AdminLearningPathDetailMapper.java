package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.learningpath.AdminLearningPathDetailResponse;
import com.lmdk.course_management_system.pojo.LearningPathDetail;

import org.springframework.stereotype.Component;

@Component
public class AdminLearningPathDetailMapper {

    public AdminLearningPathDetailResponse toResponse(
            LearningPathDetail detail
    ) {
        var learningPath = detail.getLearningPath();
        var course = learningPath.getCourse();
        var assignment = detail.getAssignment();

        return new AdminLearningPathDetailResponse(
                detail.getId(),

                learningPath.getId(),
                learningPath.getName(),

                course.getId(),
                course.getName(),

                assignment.getId(),
                assignment.getName(),
                assignment.getMaximumScore(),

                detail.getOrderNumber(),
                detail.getMinimumScore(),
                detail.getMaxAttempts()
        );
    }
}