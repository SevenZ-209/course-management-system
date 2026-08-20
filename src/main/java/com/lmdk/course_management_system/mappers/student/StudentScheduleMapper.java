package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.schedule.StudentScheduleResponse;
import com.lmdk.course_management_system.helpers.OnlineSessionHelper;
import com.lmdk.course_management_system.pojo.OnlineSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentScheduleMapper {

    private final OnlineSessionHelper onlineSessionHelper;

    public StudentScheduleResponse toResponse(
            OnlineSession session
    ) {

        return new StudentScheduleResponse(

                session.getId(),

                session.getTitle(),

                session.getCourseClass()
                        .getCourse()
                        .getId(),

                session.getCourseClass()
                        .getCourse()
                        .getName(),

                session.getCourseClass().getId(),

                session.getCourseClass().getName(),

                session.getTeacher() == null
                        ? null
                        : session.getTeacher().getId(),

                session.getTeacher() == null
                        ? null
                        : session.getTeacher().getFullName(),

                session.getStartTime(),

                session.getEndTime(),

                session.getMeetingUrl(),

                onlineSessionHelper.getStatus(session)
        );
    }
}