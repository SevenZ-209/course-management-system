package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentProgressResponse;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeacherStudentProgressMapper {

    public TeacherStudentProgressResponse toResponse(
            User student,
            StudentLearningPath progress,
            List<LearningPathDetail> details
    ) {

        if(progress == null) {
            return new TeacherStudentProgressResponse(
                    student.getId(),
                    student.getFullName(),
                    student.getUsername(),

                    null,
                    null,
                    null,
                    null,

                    null,
                    null,
                    null,

                    0,
                    0
            );
        }

        Integer completedDetails =
                calculateCompletedDetails(
                        progress,
                        details
                );

        Integer currentDetailId =
                progress.getCurrentDetail() == null
                        ? null
                        : progress.getCurrentDetail().getId();

        Integer currentAssignmentId =
                progress.getCurrentDetail() == null
                        || progress.getCurrentDetail().getAssignment() == null
                        ? null
                        : progress.getCurrentDetail().getAssignment().getId();

        String currentAssignmentName =
                progress.getCurrentDetail() == null
                        || progress.getCurrentDetail().getAssignment() == null
                        ? null
                        : progress.getCurrentDetail().getAssignment().getName();

        return new TeacherStudentProgressResponse(
                student.getId(),
                student.getFullName(),
                student.getUsername(),

                progress.getId(),
                progress.getLearningPath().getId(),
                progress.getLearningPath().getName(),
                progress.getStatus().name(),

                currentDetailId,
                currentAssignmentId,
                currentAssignmentName,

                completedDetails,
                details.size()
        );
    }

    private Integer calculateCompletedDetails(
            StudentLearningPath progress,
            List<LearningPathDetail> details
    ) {

        if(progress.getStatus()
                == StudentLearningPath.ProgressStatus.COMPLETED)
            return details.size();

        if(progress.getCurrentDetail() == null)
            return 0;

        Integer currentOrder =
                progress.getCurrentDetail()
                        .getOrderNumber();

        return (int) details.stream()
                .filter(detail ->
                        detail.getOrderNumber()
                                < currentOrder
                )
                .count();
    }
}