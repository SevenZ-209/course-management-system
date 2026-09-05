package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.assignment.CourseAssignmentResponse;
import com.lmdk.course_management_system.dto.student.course.*;
import com.lmdk.course_management_system.dto.student.learningpath.StudentLearningPathResponse;
import com.lmdk.course_management_system.dto.student.classinfo.ClassResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.helpers.StudentAccessHelper;
import com.lmdk.course_management_system.mappers.common.CourseClassMapper;
import com.lmdk.course_management_system.mappers.student.StudentCourseContentMapper;
import com.lmdk.course_management_system.mappers.student.StudentCourseMapper;
import com.lmdk.course_management_system.mappers.student.StudentLearningPathMapper;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class ApiStudentCourseController {

    private final CourseClassService classService;
    private final EnrollmentService enrollmentService;
    private final StudentLearningPathService studentLearningPathService;
    private final LearningPathDetailService learningPathDetailService;
    private final CourseService courseService;
    private final CourseModuleService courseModuleService;
    private final LessonService lessonService;

    private final CurrentUserHelper currentUserHelper;
    private final StudentAccessHelper studentAccessHelper;

    private final CourseClassMapper courseClassMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final StudentCourseContentMapper studentCourseContentMapper;
    private final StudentLearningPathMapper studentLearningPathMapper;

    @GetMapping("/{courseId}/classes")
    public List<ClassResponse> getClassesByCourse(
            @PathVariable Integer courseId
    ) {
        return classService.getClassesByCourse(courseId)
                .stream()
                .map(courseClassMapper::toResponse)
                .toList();
    }

    @GetMapping
    public List<StudentCourseResponse> getMyCourses(
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        return enrollmentService
                .getActiveEnrollmentsByStudent(student.getId())
                .stream()
                .map(studentCourseMapper::toResponse)
                .toList();
    }

    @GetMapping("/{courseId}/learning-paths")
    public List<StudentLearningPathResponse> getMyLearningPaths(
            @PathVariable Integer courseId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        studentAccessHelper.requireActiveCourse(
                student.getId(),
                courseId
        );

        StudentLearningPath progress =
                studentLearningPathService
                        .getStudentLearningPathsByStudent(student.getId())
                        .stream()
                        .filter(p ->
                                p.getLearningPath()
                                        .getCourse()
                                        .getId()
                                        .equals(courseId)
                        )
                        .findFirst()
                        .orElse(null);

        return studentLearningPathService
                .getStudentLearningPathsByStudent(student.getId())
                .stream()
                .filter(path ->
                        path.getLearningPath()
                                .getCourse()
                                .getId()
                                .equals(courseId)
                )
                .map(path -> {
                    List<LearningPathDetail> details =
                            learningPathDetailService
                                    .getDetailsByLearningPath(
                                            progress.getLearningPath().getId()
                                    );

                    return studentLearningPathMapper
                            .toResponse(progress, details);
                })
                .toList();
    }

    @GetMapping("/{courseId}/content")
    public StudentCourseContentResponse getCourseContent(
            @PathVariable Integer courseId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        Course course =
                courseService.getCourseById(courseId);

        if(course == null)
            throw new IllegalArgumentException(
                    "Khóa học không tồn tại!"
            );

        studentAccessHelper.requireActiveCourse(
                student.getId(),
                courseId
        );

        StudentLearningPath progress =
                studentLearningPathService
                        .getStudentLearningPathsByStudent(student.getId())
                        .stream()
                        .filter(p ->
                                p.getLearningPath()
                                        .getCourse()
                                        .getId()
                                        .equals(courseId)
                        )
                        .findFirst()
                        .orElse(null);

        List<LearningPathDetail> details =
                learningPathDetailService
                        .getDetailsByLearningPath(
                                progress.getLearningPath().getId()
                        );

        List<StudentModuleResponse> modules =
                courseModuleService
                        .getModulesByCourse(courseId)
                        .stream()
                        .map(module -> {
                            List<StudentLessonResponse> lessons =
                                    lessonService
                                            .getLessonsByModule(module.getId())
                                            .stream()
                                            .map(lesson ->
                                                    studentCourseContentMapper
                                                            .toLessonResponse(
                                                                    lesson,
                                                                    progress, details
                                                            )
                                            )
                                            .toList();

                            return studentCourseContentMapper
                                    .toModuleResponse(module, lessons);
                        })
                        .toList();

        return studentCourseContentMapper
                .toResponse(course, modules);
    }

    @GetMapping("/{courseId}/assignments")
    public List<CourseAssignmentResponse> getCourseAssignments(
            @PathVariable Integer courseId,
            Authentication authentication
    ) {

        User student =
                currentUserHelper.getCurrentStudent(authentication);

        studentAccessHelper.requireActiveCourse(
                student.getId(),
                courseId
        );

        return studentLearningPathService
                .getCourseAssignments(
                        student.getId(),
                        courseId
                );
    }

    @GetMapping("/detail/{enrollmentId}")
    public StudentCourseDetailResponse getCourseDetail(
            @PathVariable Integer enrollmentId,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(authentication);

        Enrollment enrollment =
                studentAccessHelper
                        .requireOwnedActiveEnrollment(
                                student.getId(),
                                enrollmentId
                        );

        Integer courseId =
                enrollment.getCourseClass()
                        .getCourse()
                        .getId();

        StudentLearningPath progress =
                studentLearningPathService
                        .getStudentLearningPathsByStudent(student.getId())
                        .stream()
                        .filter(p ->
                                p.getLearningPath()
                                        .getCourse()
                                        .getId()
                                        .equals(courseId)
                        )
                        .findFirst()
                        .orElse(null);

        if(progress == null)
            throw new IllegalArgumentException(
                    "Học viên chưa có lộ trình học cho khóa này!"
            );

        return studentCourseMapper
                .toDetailResponse(
                        enrollment,
                        progress
                );
    }
}