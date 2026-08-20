package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.learningpath.LearningPathDetailProgressResponse;
import com.lmdk.course_management_system.dto.student.learningpath.StudentLearningPathResponse;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentLearningPathMapper {

    public StudentLearningPathResponse toResponse(
            StudentLearningPath progress,
            List<LearningPathDetail> details
    ) {
        List<LearningPathDetailProgressResponse> detailResponses =
                details.stream()
                        .map(detail -> toDetailResponse(progress, detail))
                        .toList();

        return new StudentLearningPathResponse(
                progress.getId(),
                progress.getLearningPath().getId(),
                progress.getLearningPath().getName(),
                progress.getLearningPath().getCourse().getId(),
                progress.getLearningPath().getCourse().getName(),
                progress.getStatus().name(),

                progress.getCurrentDetail() == null
                        ? null
                        : progress.getCurrentDetail().getId(),

                progress.getStartedAt(),
                progress.getCompletedAt(),
                detailResponses
        );
    }

    private LearningPathDetailProgressResponse toDetailResponse(
            StudentLearningPath progress,
            LearningPathDetail detail
    ) {
        String status;

        if(progress.getStatus() == StudentLearningPath.ProgressStatus.COMPLETED)
            status = "COMPLETED";

        else if(progress.getCurrentDetail() == null)
            status = "LOCKED";

        else {
            Integer currentOrder = progress.getCurrentDetail().getOrderNumber();

            if(detail.getOrderNumber() < currentOrder)
                status = "COMPLETED";
            else if(detail.getOrderNumber().equals(currentOrder))
                status = "CURRENT";
            else
                status = "LOCKED";
        }

        return new LearningPathDetailProgressResponse(
                detail.getId(),
                detail.getAssignment().getId(),
                detail.getOrderNumber(),
                detail.getMinimumScore(),
                detail.getMaxAttempts(),
                status
        );
    }
}