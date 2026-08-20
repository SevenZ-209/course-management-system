package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.Lesson;

import java.util.List;
import java.util.Map;

public interface LessonRepository {

    Lesson getLessonById(Integer id);

    Lesson addLesson(Lesson lesson);

    void updateLesson(Lesson lesson);

    List<Lesson> getLessons(Map<String, String> params);

    List<Lesson> getLessonsByModule(Integer moduleId);

    long countLessons(Map<String, String> params);

    boolean existsOrderNumber(Integer moduleId, Integer orderNumber);

    boolean existsOrderNumberExceptId(Integer moduleId, Integer orderNumber, Integer lessonId);
}