package com.lmdk.course_management_system.mappers.parent;

import com.lmdk.course_management_system.dto.parent.ParentStudentProgressResponse;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.pojo.StudentLearningPath;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParentStudentProgressMapper {

    public ParentStudentProgressResponse toResponse(
            CourseClass courseClass,
            StudentLearningPath progress,
            List<LearningPathDetail> details
    ) {

        Integer completedDetails = 0;

        if(progress != null) {
            if(progress.getStatus()
                    == StudentLearningPath.ProgressStatus.COMPLETED) {

                completedDetails = details.size();

            } else if(progress.getCurrentDetail() != null) {

                Integer currentOrder =
                        progress.getCurrentDetail()
                                .getOrderNumber();

                completedDetails =
                        (int) details.stream()
                                .filter(detail ->
                                        detail.getOrderNumber()
                                                < currentOrder
                                )
                                .count();
            }
        }

        return new ParentStudentProgressResponse(
                courseClass.getCourse().getId(),
                courseClass.getCourse().getName(),

                courseClass.getId(),
                courseClass.getName(),

                progress == null
                        ? null
                        : progress.getLearningPath().getId(),

                progress == null
                        ? null
                        : progress.getLearningPath().getName(),

                progress == null
                        ? null
                        : progress.getStatus().name(),

                progress == null
                        || progress.getCurrentDetail() == null
                        ? null
                        : progress.getCurrentDetail()
                          .getAssignment()
                          .getId(),

                progress == null
                        || progress.getCurrentDetail() == null
                        ? null
                        : progress.getCurrentDetail()
                          .getAssignment()
                          .getName(),

                completedDetails,

                progress == null
                        ? 0
                        : details.size()
        );
    }
}