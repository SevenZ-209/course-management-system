package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.attendance.StudentAttendanceResponse;
import com.lmdk.course_management_system.helpers.OnlineSessionHelper;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.OnlineSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentAttendanceMapper {

    private final OnlineSessionHelper onlineSessionHelper;

    public StudentAttendanceResponse toResponse(
            OnlineSession session,
            Attendance attendance
    ) {

        return new StudentAttendanceResponse(

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