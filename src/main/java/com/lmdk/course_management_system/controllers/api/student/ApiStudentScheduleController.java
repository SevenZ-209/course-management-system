package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.student.schedule.StudentSchedulePageResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.student.StudentScheduleMapper;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.OnlineSessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student/schedule")
@RequiredArgsConstructor
public class ApiStudentScheduleController {

    private final OnlineSessionService onlineSessionService;
    private final CurrentUserHelper currentUserHelper;
    private final StudentScheduleMapper studentScheduleMapper;

    @Value("${online-sessions.page-size:10}")
    private int pageSize;

    @GetMapping
    public StudentSchedulePageResponse getSchedule(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "false") boolean includeEnded,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        User student = currentUserHelper.getCurrentStudent(authentication);

        if(from != null && to != null && to.isBefore(from))
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu!");

        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("studentId", String.valueOf(student.getId()));
        params.put("includeEnded", String.valueOf(includeEnded));
        params.put("sort", "asc");

        if(from != null) params.put("from", from.toString());
        if(to != null) params.put("to", to.toString());

        long totalRecords = onlineSessionService.countSessions(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new StudentSchedulePageResponse(
                onlineSessionService.getSessions(params)
                        .stream()
                        .map(studentScheduleMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }
}
