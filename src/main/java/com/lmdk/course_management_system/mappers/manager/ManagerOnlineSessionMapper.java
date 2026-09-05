package com.lmdk.course_management_system.mappers.manager;

import com.lmdk.course_management_system.dto.manager.dashboard.ManagerOnlineSessionResponse;
import com.lmdk.course_management_system.pojo.OnlineSession;
import org.springframework.stereotype.Component;

@Component
public class ManagerOnlineSessionMapper {

    public ManagerOnlineSessionResponse toResponse(OnlineSession session) {
        var courseClass = session.getCourseClass();
        var course = courseClass.getCourse();
        var teacher = session.getTeacher();

        return new ManagerOnlineSessionResponse(
                session.getId(), session.getTitle(),
                courseClass.getId(), courseClass.getName(),
                course.getId(), course.getName(),
                teacher == null ? null : teacher.getId(),
                teacher == null ? null : teacher.getFullName(),
                session.getStartTime(), session.getEndTime(), session.getMeetingUrl()
        );
    }
}
