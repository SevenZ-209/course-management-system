package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.learningpath.AdminLearningPathResponse;
import com.lmdk.course_management_system.pojo.LearningPath;

import org.springframework.stereotype.Component;

@Component
public class AdminLearningPathMapper {

    public AdminLearningPathResponse toResponse(
            LearningPath learningPath
    ) {
        return new AdminLearningPathResponse(
                learningPath.getId(),
                learningPath.getName(),
                learningPath.getCourse().getId(),
                learningPath.getCourse().getName(),
                learningPath.getAssignmentsPerDay(),
                learningPath.getStatus().name()
        );
    }
}