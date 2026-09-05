package com.lmdk.course_management_system.dto.manager.dashboard;

import java.time.LocalDateTime;

public record ManagerOnlineSessionResponse(
        Integer id,
        String title,
        Integer classId,
        String className,
        Integer courseId,
        String courseName,
        Integer teacherId,
        String teacherName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String meetingUrl
) {
}
