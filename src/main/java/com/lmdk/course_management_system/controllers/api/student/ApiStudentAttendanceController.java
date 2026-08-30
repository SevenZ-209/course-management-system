package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.attendance.StudentAttendanceResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.student.StudentAttendanceMapper;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.AttendanceService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.OnlineSessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/classes")
@RequiredArgsConstructor
public class ApiStudentAttendanceController {

    private final AttendanceService attendanceService;
    private final OnlineSessionService onlineSessionService;
    private final EnrollmentService enrollmentService;
    private final CurrentUserHelper currentUserHelper;
    private final StudentAttendanceMapper studentAttendanceMapper;

    @GetMapping("/{classId}/attendance")
    public List<StudentAttendanceResponse> getAttendance(
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

        List<OnlineSession> sessions =
                onlineSessionService.getSessionsByClass(classId);

        Map<Integer, Attendance> attendanceBySessionId = new HashMap<>();
        for (Attendance attendance : attendanceService.getAttendancesByStudentAndSessionIds(
                student.getId(),
                sessions.stream().map(OnlineSession::getId).toList()
        ))
            attendanceBySessionId.put(attendance.getOnlineSession().getId(), attendance);

        return sessions.stream()
                .map(session -> studentAttendanceMapper.toResponse(
                        session,
                        attendanceBySessionId.get(session.getId())
                ))
                .toList();
    }
}