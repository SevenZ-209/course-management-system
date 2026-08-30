package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherAccessHelper {

    private final CourseClassService classService;

    public CourseClass requireTeacherClass(
            User teacher,
            Integer classId
    ) {
        if(teacher.getRole() != User.UserRole.TEACHER)
            throw new ForbiddenException(
                    "Tài khoản không phải giáo viên!"
            );

        CourseClass courseClass =
                classService.getClassById(classId);

        if(courseClass == null)
            throw new IllegalArgumentException(
                    "Lớp học không tồn tại!"
            );

        if(courseClass.getTeacher() == null
                || !courseClass.getTeacher()
                .getId()
                .equals(teacher.getId()))
            throw new ForbiddenException(
                    "Bạn không có quyền truy cập lớp học này!"
            );

        return courseClass;
    }
}