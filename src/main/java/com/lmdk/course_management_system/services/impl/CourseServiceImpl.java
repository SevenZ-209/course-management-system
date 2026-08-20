package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.repository.CourseRepository;
import com.lmdk.course_management_system.services.CourseService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public Course getCourseById(Integer id) {
        return courseRepository.getCourseById(id);
    }

    @Override
    public Course addCourse(Course course) {
        validateCourse(course);

        if (course.getStatus() == null)
            course.setStatus(Course.CourseStatus.ACTIVE);

        return courseRepository.addCourse(course);
    }

    @Override
    public void updateCourse(Course course) {
        validateCourse(course);
        courseRepository.updateCourse(course);
    }

    @Override
    public List<Course> getCourses(Map<String, String> params) {
        return courseRepository.getCourses(params);
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.getAllCourses();
    }

    @Override
    public long countCourses(Map<String, String> params) {
        return courseRepository.countCourses(params);
    }

    private void validateCourse(Course course) {
        if (course.getName() == null || course.getName().trim().isBlank())
            throw new IllegalArgumentException("Tên khóa học không được để trống!");

        if (course.getCategory() == null)
            throw new IllegalArgumentException("Vui lòng chọn danh mục!");

        if (course.getTuitionFee() == null || course.getTuitionFee().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Học phí không hợp lệ!");

        course.setName(course.getName().trim());
    }
}