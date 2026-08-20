package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.repository.CourseClassRepository;
import com.lmdk.course_management_system.services.CourseClassService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseClassServiceImpl implements CourseClassService {

    private final CourseClassRepository classRepository;

    @Override
    public CourseClass getClassById(Integer id) {
        return classRepository.getClassById(id);
    }

    @Override
    public CourseClass addClass(CourseClass courseClass) {
        validateClass(courseClass);

        if (courseClass.getStartDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Ngày bắt đầu không được ở trong quá khứ!");

        if (courseClass.getEndDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Ngày kết thúc không được ở trong quá khứ!");

        if (courseClass.getStatus() == null)
            courseClass.setStatus(CourseClass.ClassStatus.UPCOMING);

        return classRepository.addClass(courseClass);
    }


    @Override
    public List<CourseClass> getClassesByTeacher(Integer teacherId) {
        return classRepository.getClassesByTeacher(teacherId);
    }

    @Override
    public void updateClass(CourseClass courseClass) {
        validateClass(courseClass);
        classRepository.updateClass(courseClass);
    }

    @Override
    public List<CourseClass> getClasses(Map<String, String> params) {
        return classRepository.getClasses(params);
    }

    @Override
    public List<CourseClass> getClassesByCourse(Integer courseId) {
        return classRepository.getClassesByCourse(courseId);
    }

    @Override
    public List<CourseClass> getAllClasses() {
        return classRepository.getAllClasses();
    }

    @Override
    public long countClasses(Map<String, String> params) {
        return classRepository.countClasses(params);
    }

    private void validateClass(CourseClass courseClass) {
        if (courseClass.getName() == null || courseClass.getName().trim().isBlank())
            throw new IllegalArgumentException("Tên lớp học không được để trống!");

        if (courseClass.getCourse() == null)
            throw new IllegalArgumentException("Vui lòng chọn khóa học!");

        if (courseClass.getStartDate() == null || courseClass.getEndDate() == null)
            throw new IllegalArgumentException("Vui lòng chọn thời gian học!");

        if (courseClass.getEndDate().isBefore(courseClass.getStartDate()))
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu!");

        if (courseClass.getMaxStudents() == null || courseClass.getMaxStudents() < 1)
            throw new IllegalArgumentException("Số học viên tối đa phải lớn hơn 0!");

        courseClass.setName(courseClass.getName().trim());
    }
}