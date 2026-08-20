package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.course.StudentCourseContentResponse;
import com.lmdk.course_management_system.dto.student.course.StudentLessonResponse;
import com.lmdk.course_management_system.dto.student.course.StudentModuleResponse;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.pojo.Lesson;
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
        return new StudentModuleResponse(
                module.getId(),
                module.getName(),
                module.getOrderNumber(),
                lessons
        );
    }

    public StudentLessonResponse toLessonResponse(Lesson lesson) {
        return new StudentLessonResponse(
                lesson.getId(),
                lesson.getName(),
                lesson.getOrderNumber()
        );
    }
}