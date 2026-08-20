package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.schedule.StudentOnlineSessionResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.student.StudentOnlineSessionMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.OnlineSessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/classes")
@RequiredArgsConstructor
public class ApiStudentOnlineSessionController {

    private final OnlineSessionService onlineSessionService;
    private final EnrollmentService enrollmentService;
    private final CurrentUserHelper currentUserHelper;
    private final StudentOnlineSessionMapper studentOnlineSessionMapper;

    @GetMapping("/{classId}/online-sessions")
    public List<StudentOnlineSessionResponse> getOnlineSessions(
            @PathVariable Integer classId,
            Authentication authentication
    ) {

        User student =
                currentUserHelper.getCurrentStudent(
                        authentication
                );

        Enrollment enrollment =
                enrollmentService.getEnrollment(
                        student.getId(),
                        classId
                );

        if(enrollment == null)
            throw new IllegalArgumentException(
                    "Bạn chưa đăng ký lớp học này!"
            );

        if(enrollment.getStatus()
                != Enrollment.EnrollmentStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Bạn chưa được kích hoạt trong lớp học này!"
            );

        return onlineSessionService
                .getSessionsByClass(classId)
                .stream()
                .map(studentOnlineSessionMapper::toResponse)
                .toList();
    }
} 