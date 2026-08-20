package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.repository.LessonRepository;
import com.lmdk.course_management_system.services.LessonService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    @Override
    public Lesson getLessonById(Integer id) {
        return lessonRepository.getLessonById(id);
    }

    @Override
    public Lesson addLesson(Lesson lesson) {
        validateLesson(lesson);

        if (lessonRepository.existsOrderNumber(
                lesson.getCourseModule().getId(),
                lesson.getOrderNumber()
        ))
            throw new IllegalArgumentException("Thứ tự bài học đã tồn tại trong module!");

        return lessonRepository.addLesson(lesson);
    }

    @Override
    public void updateLesson(Lesson lesson) {
        validateLesson(lesson);

        if (lessonRepository.existsOrderNumberExceptId(
                lesson.getCourseModule().getId(),
                lesson.getOrderNumber(),
                lesson.getId()
        ))
            throw new IllegalArgumentException("Thứ tự bài học đã tồn tại trong module!");

        lessonRepository.updateLesson(lesson);
    }

    @Override
    public List<Lesson> getLessons(Map<String, String> params) {
        return lessonRepository.getLessons(params);
    }

    @Override
    public List<Lesson> getLessonsByModule(Integer moduleId) {
        return lessonRepository.getLessonsByModule(moduleId);
    }

    @Override
    public long countLessons(Map<String, String> params) {
        return lessonRepository.countLessons(params);
    }

    private void validateLesson(Lesson lesson) {
        if (lesson.getName() == null || lesson.getName().trim().isBlank())
            throw new IllegalArgumentException("Tên bài học không được để trống!");

        if (lesson.getCourseModule() == null)
            throw new IllegalArgumentException("Vui lòng chọn module!");

        if (lesson.getOrderNumber() == null || lesson.getOrderNumber() < 1)
            throw new IllegalArgumentException("Thứ tự bài học phải lớn hơn 0!");

        lesson.setName(lesson.getName().trim());
    }
}