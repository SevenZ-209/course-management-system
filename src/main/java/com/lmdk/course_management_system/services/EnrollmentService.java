package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.Enrollment;

import java.util.List;
import java.util.Map;

public interface EnrollmentService {

    Enrollment getEnrollmentById(Integer id);

    Enrollment getEnrollment(Integer studentId, Integer classId);

    Enrollment addEnrollment(Enrollment enrollment);

    void updateEnrollment(Enrollment enrollment);

    List<Enrollment> getEnrollments(Map<String, String> params);

    List<Enrollment> getEnrollmentsByClass(Integer classId);

    List<Enrollment> getActiveEnrollmentsByClass(Integer classId);

    long countEnrollments(Map<String, String> params);

    List<Enrollment> getPendingEnrollments();

    List<Enrollment> getActiveEnrollmentsByStudent(Integer studentId);

    boolean existsActiveEnrollmentByStudentAndCourse(Integer studentId, Integer courseId);

    boolean existsActiveEnrollmentByStudentCourseAndTeacher(
            Integer studentId,
            Integer courseId,
            Integer teacherId
    );

    List<Enrollment> getEnrollmentsByStudent(Integer studentId);

}