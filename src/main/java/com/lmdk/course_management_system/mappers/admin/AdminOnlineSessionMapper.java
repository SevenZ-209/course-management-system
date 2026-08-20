package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.onlinesession.AdminOnlineSessionResponse;
import com.lmdk.course_management_system.pojo.OnlineSession;

import org.springframework.stereotype.Component;

@Component
public class AdminOnlineSessionMapper {

    public AdminOnlineSessionResponse toResponse(
            OnlineSession session
    ) {
        return new AdminOnlineSessionResponse(
                session.getId(),
                session.getTitle(),

                session.getCourseClass().getId(),
                session.getCourseClass().getName(),

                session.getCourseClass()
                        .getCourse()
                        .getId(),

                session.getCourseClass()
                        .getCourse()
                        .getName(),

                session.getTeacher().getId(),
                session.getTeacher().getFullName(),

                session.getStartTime(),
                session.getEndTime(),

                session.getMeetingUrl()
        );
    }
}