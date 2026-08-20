package com.lmdk.course_management_system.mappers.parent;

import com.lmdk.course_management_system.dto.parent.ParentAttendanceResponse;
import com.lmdk.course_management_system.helpers.OnlineSessionHelper;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.OnlineSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParentAttendanceMapper {

    private final OnlineSessionHelper onlineSessionHelper;

    public ParentAttendanceResponse toResponse(
            OnlineSession session,
            Attendance attendance
    ) {
        return new ParentAttendanceResponse(
                session.getCourseClass().getId(),
                session.getCourseClass().getName(),

                session.getCourseClass()
                        .getCourse()
                        .getId(),

                session.getCourseClass()
                        .getCourse()
                        .getName(),

                session.getId(),
                session.getTitle(),
                session.getStartTime(),
                session.getEndTime(),

                onlineSessionHelper.getStatus(session),

                getAttendanceStatus(attendance),

                attendance == null
                        ? null
                        : attendance.getAttendedAt(),

                attendance == null
                        ? null
                        : attendance.getNote()
        );
    }

    private String getAttendanceStatus(
            Attendance attendance
    ) {
        if(attendance == null)
            return "NOT_MARKED";

        return Boolean.TRUE.equals(
                attendance.getPresent()
        )
                ? "PRESENT"
                : "ABSENT";
    }
}