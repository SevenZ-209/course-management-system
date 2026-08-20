package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.session.TeacherOnlineSessionResponse;
import com.lmdk.course_management_system.helpers.OnlineSessionHelper;
import com.lmdk.course_management_system.pojo.OnlineSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherOnlineSessionMapper {

    private final OnlineSessionHelper onlineSessionHelper;

    public TeacherOnlineSessionResponse toResponse(
            OnlineSession session
    ) {
        return new TeacherOnlineSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getStartTime(),
                session.getEndTime(),
                session.getMeetingUrl(),
                onlineSessionHelper.getStatus(session)
        );
    }
}