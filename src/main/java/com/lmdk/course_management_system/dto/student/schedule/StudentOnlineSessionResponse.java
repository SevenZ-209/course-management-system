package com.lmdk.course_management_system.dto.student.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StudentOnlineSessionResponse {

    private Integer sessionId;

    private String title;

    private Integer classId;
    private String className;

    private Integer teacherId;
    private String teacherName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String meetingUrl;

    private String status;
}