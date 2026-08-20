package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.Lesson;

import java.util.List;
import java.util.Map;

public interface LessonService {

    Lesson getLessonById(Integer id);

    Lesson addLesson(Lesson lesson);

    void updateLesson(Lesson lesson);

    List<Lesson> getLessons(Map<String, String> params);

    List<Lesson> getLessonsByModule(Integer moduleId);

    long countLessons(Map<String, String> params);
}