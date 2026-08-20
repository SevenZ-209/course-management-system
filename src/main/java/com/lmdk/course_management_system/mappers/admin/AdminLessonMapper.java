package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.lesson.AdminLessonResponse;
import com.lmdk.course_management_system.pojo.Lesson;

import org.springframework.stereotype.Component;

@Component
public class AdminLessonMapper {

    public AdminLessonResponse toResponse(Lesson lesson) {
        return new AdminLessonResponse(
                lesson.getId(),
                lesson.getName(),
                lesson.getCourseModule().getCourse().getId(),
                lesson.getCourseModule().getCourse().getName(),
                lesson.getCourseModule().getId(),
                lesson.getCourseModule().getName(),
                lesson.getOrderNumber(),
                lesson.getFileName(),
                lesson.getFileUrl()
        );
    }
}