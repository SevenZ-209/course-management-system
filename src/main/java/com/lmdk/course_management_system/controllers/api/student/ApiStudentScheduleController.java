package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.schedule.StudentScheduleResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.helpers.OnlineSessionHelper;
import com.lmdk.course_management_system.mappers.student.StudentScheduleMapper;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.OnlineSessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/student/schedule")
@RequiredArgsConstructor
public class ApiStudentScheduleController {

    private final EnrollmentService enrollmentService;
    private final OnlineSessionService onlineSessionService;
    private final CurrentUserHelper currentUserHelper;
    private final OnlineSessionHelper onlineSessionHelper;
    private final StudentScheduleMapper studentScheduleMapper;

    @GetMapping
    public List<StudentScheduleResponse> getSchedule(
            @RequestParam(defaultValue = "false") boolean includeEnded,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            Authentication authentication
    ) {

        User student =
                currentUserHelper.getCurrentStudent(
                        authentication
                );

        if(from != null
                && to != null
                && to.isBefore(from))
            throw new IllegalArgumentException(
                    "Ngày kết thúc không được trước ngày bắt đầu!"
            );

        return enrollmentService
                .getActiveEnrollmentsByStudent(student.getId())
                .stream()
                .flatMap(enrollment ->
                        onlineSessionService
                                .getSessionsByClass(
                                        enrollment.getCourseClass().getId()
                                )
                                .stream()
                )
                .filter(session ->
                        includeEnded
                                || !"ENDED".equals(
                                onlineSessionHelper.getStatus(session)
                        )
                )
                .filter(session ->
                        from == null
                                || !session.getStartTime()
                                .toLocalDate()
                                .isBefore(from)
                )
                .filter(session ->
                        to == null
                                || !session.getStartTime()
                                .toLocalDate()
                                .isAfter(to)
                )
                .sorted(
                        Comparator.comparing(
                                OnlineSession::getStartTime
                        )
                )
                .map(studentScheduleMapper::toResponse)
                .toList();
    }
}