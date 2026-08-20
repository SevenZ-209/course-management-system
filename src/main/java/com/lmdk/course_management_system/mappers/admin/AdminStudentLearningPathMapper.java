package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.learningpath.AdminStudentLearningPathResponse;
import com.lmdk.course_management_system.pojo.StudentLearningPath;

import org.springframework.stereotype.Component;

@Component
public class AdminStudentLearningPathMapper {

    public AdminStudentLearningPathResponse toResponse(
            StudentLearningPath studentLearningPath
    ) {
        var student =
                studentLearningPath.getStudent();

        var learningPath =
                studentLearningPath.getLearningPath();

        var course =
                learningPath.getCourse();

        var currentDetail =
                studentLearningPath.getCurrentDetail();

        return new AdminStudentLearningPathResponse(
                studentLearningPath.getId(),

                student.getId(),
                student.getFullName(),
                student.getUsername(),

                learningPath.getId(),
                learningPath.getName(),

                course.getId(),
                course.getName(),

                currentDetail != null
                        ? currentDetail.getId()
                        : null,

                currentDetail != null
                        ? currentDetail.getOrderNumber()
                        : null,

                currentDetail != null
                        ? currentDetail.getAssignment().getId()
                        : null,

                currentDetail != null
                        ? currentDetail.getAssignment().getName()
                        : null,

                studentLearningPath.getStatus().name(),
                studentLearningPath.getStartedAt(),
                studentLearningPath.getCompletedAt()
        );
    }
}