package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.course.StudentLessonDetailResponse;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.pojo.Lesson;

import org.springframework.stereotype.Component;

@Component
public class StudentLessonMapper {

    public StudentLessonDetailResponse toDetailResponse(
            Lesson lesson
    ) {

        CourseModule module =
                lesson.getCourseModule();

        return new StudentLessonDetailResponse(

                lesson.getId(),
                lesson.getName(),

                module.getId(),
                module.getName(),

                module.getCourse().getId(),
                module.getCourse().getName(),

                lesson.getOrderNumber(),

                lesson.getFileName(),
                lesson.getFileUrl()
        );
    }
}