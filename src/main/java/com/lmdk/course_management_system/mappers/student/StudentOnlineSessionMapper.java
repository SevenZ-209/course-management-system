package com.lmdk.course_management_system.mappers.student;

import com.lmdk.course_management_system.dto.student.schedule.StudentOnlineSessionResponse;
import com.lmdk.course_management_system.helpers.OnlineSessionHelper;
import com.lmdk.course_management_system.pojo.OnlineSession;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentOnlineSessionMapper {

    private final OnlineSessionHelper onlineSessionHelper;

    public StudentOnlineSessionResponse toResponse(
            OnlineSession session
    ) {

        return new StudentOnlineSessionResponse(

                session.getId(),

                session.getTitle(),

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