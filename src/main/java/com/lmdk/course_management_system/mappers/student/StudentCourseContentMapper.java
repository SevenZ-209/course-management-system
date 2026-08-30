package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.course.StudentCourseContentResponse;
import com.lmdk.course_management_system.dto.student.course.StudentLessonResponse;
import com.lmdk.course_management_system.dto.student.course.StudentModuleResponse;
import com.lmdk.course_management_system.pojo.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentCourseContentMapper {

    public StudentCourseContentResponse toResponse(
            Course course,
            List<StudentModuleResponse> modules
    ) {
        return new StudentCourseContentResponse(
                course.getId(),
                course.getName(),
                modules
        );
    }

    public StudentModuleResponse toModuleResponse(
            CourseModule module,
            List<StudentLessonResponse> lessons
    ) {

        boolean locked = lessons.stream()
                .allMatch(StudentLessonResponse::locked);

        String status = locked ? "LOCKED" : "OPEN";

        return new StudentModuleResponse(
                module.getId(),
                module.getName(),
                module.getOrderNumber(),
                locked,
                status,
                lessons
        );
    }

    public StudentLessonResponse toLessonResponse(
            Lesson lesson,
            StudentLearningPath learningPath,
            List<LearningPathDetail> details
    ) {

        LearningPathDetail lessonDetail = details.stream()
                .filter(d ->
                        d.getAssignment() != null
                                && d.getAssignment().getLesson() != null
                                && d.getAssignment()
                                .getLesson()
                                .getId()
                                .equals(lesson.getId())
                )
                .findFirst()
                .orElse(null);

        if (lessonDetail == null) {
            return new StudentLessonResponse(
                    lesson.getId(),
                    lesson.getName(),
                    lesson.getOrderNumber(),
                    true,
                    "LOCKED"
            );
        }

        Integer lessonOrder = lessonDetail.getOrderNumber();

        Integer currentOrder =
                learningPath.getCurrentDetail() != null
                        ? learningPath.getCurrentDetail().getOrderNumber()
                        : Integer.MAX_VALUE;


        System.out.println(
                "MAPPER CURRENT DETAIL = "
                        + (learningPath.getCurrentDetail() != null
                        ? learningPath.getCurrentDetail().getId()
                        : "COMPLETED")
        );

        System.out.println(
                "LESSON ID = "
                        + lesson.getId()
                        + " DETAIL ID = "
                        + lessonDetail.getId()
                        + " DETAIL ORDER = "
                        + lessonOrder
        );


        if (lessonOrder <= currentOrder) {
            return new StudentLessonResponse(
                    lesson.getId(),
                    lesson.getName(),
                    lesson.getOrderNumber(),
                    false,
                    lessonOrder.equals(currentOrder)
                            ? "CURRENT"
                            : "COMPLETED"
            );
        }


        System.out.println("RESULT = LOCKED");

        return new StudentLessonResponse(
                lesson.getId(),
                lesson.getName(),
                lesson.getOrderNumber(),
                true,
                "LOCKED"
        );
    }

}