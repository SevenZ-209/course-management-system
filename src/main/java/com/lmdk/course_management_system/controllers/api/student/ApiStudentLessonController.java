package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.course.StudentLessonDetailResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.student.StudentLessonMapper;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/lessons")
@RequiredArgsConstructor
public class ApiStudentLessonController {

    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final CurrentUserHelper currentUserHelper;
    private final StudentLessonMapper studentLessonMapper;
    private final StudentLearningPathService studentLearningPathService;
    private final AssignedAssignmentService assignedAssignmentService;

    @GetMapping("/{lessonId}")
    public StudentLessonDetailResponse getLesson(
            @PathVariable Integer lessonId,
            Authentication authentication
    ) {

        User student =
                currentUserHelper.getCurrentStudent(
                        authentication
                );

        Lesson lesson =
                lessonService.getLessonById(
                        lessonId
                );

        if(lesson == null)
            throw new IllegalArgumentException(
                    "Bài học không tồn tại!"
            );

        Integer courseId =
                lesson.getCourseModule()
                        .getCourse()
                        .getId();

        if(!enrollmentService
                .existsActiveEnrollmentByStudentAndCourse(
                        student.getId(),
                        courseId
                ))

            throw new IllegalArgumentException(
                    "Bạn chưa được kích hoạt trong khóa học này!"
            );

        if(!studentLearningPathService
                .canAccessLesson(
                        student.getId(),
                        lessonId
                ))
            throw new IllegalArgumentException(
                    "Bài học này đang bị khóa!"
            );

        if(lesson.getFileUrl() == null
                || lesson.getFileUrl().isBlank())
            throw new IllegalArgumentException(
                    "Bài học chưa có tài liệu PDF!"
            );

        Assignment assignment =
                studentLearningPathService
                        .getCurrentAssignment(
                                student.getId(),
                                courseId
                        );

        AssignedAssignment assigned = null;

        if (assignment != null) {
            assigned =
                    assignedAssignmentService
                            .getAssignedAssignmentsByStudent(student.getId())
                            .stream()
                            .filter(a ->
                                    a.getAssignment()
                                            .getId()
                                            .equals(assignment.getId())
                            )
                            .findFirst()
                            .orElse(null);
        }

        return studentLessonMapper
                .toDetailResponse(
                        lesson,
                        assignment,
                        assigned
                );
    }
}