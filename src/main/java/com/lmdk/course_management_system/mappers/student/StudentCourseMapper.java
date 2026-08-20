package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.course.StudentCourseDetailResponse;
import com.lmdk.course_management_system.dto.student.course.StudentCourseResponse;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import org.springframework.stereotype.Component;

@Component
public class StudentCourseMapper {

    public StudentCourseResponse toResponse(Enrollment enrollment) {
        CourseClass courseClass = enrollment.getCourseClass();

        return new StudentCourseResponse(
                enrollment.getId(),
                courseClass.getCourse().getId(),
                courseClass.getCourse().getName(),
                courseClass.getId(),
                courseClass.getName(),
                courseClass.getStartDate(),
                courseClass.getEndDate(),
                courseClass.getStatus().name()
        );
    }

    public StudentCourseDetailResponse toDetailResponse(
            Enrollment enrollment,
            StudentLearningPath progress
    ) {
        CourseClass courseClass = enrollment.getCourseClass();

        Integer currentDetailId = null;
        Integer currentAssignmentId = null;

        if(progress != null && progress.getCurrentDetail() != null) {
            currentDetailId = progress.getCurrentDetail().getId();
            currentAssignmentId = progress.getCurrentDetail()
                    .getAssignment().getId();
        }

        return new StudentCourseDetailResponse(
                enrollment.getId(),

                courseClass.getCourse().getId(),
                courseClass.getCourse().getName(),

                courseClass.getId(),
                courseClass.getName(),

                courseClass.getStartDate(),
                courseClass.getEndDate(),

                courseClass.getStatus().name(),
                enrollment.getStatus().name(),

                progress == null ? null : progress.getId(),
                progress == null ? null : progress.getLearningPath().getId(),
                progress == null ? null : progress.getLearningPath().getName(),
                progress == null ? null : progress.getStatus().name(),

                currentDetailId,
                currentAssignmentId
        );
    }
}