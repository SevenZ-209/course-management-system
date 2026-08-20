package com.lmdk.course_management_system.dto.student.attendance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StudentAttendanceResponse {

    private Integer sessionId;
    private String sessionTitle;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String sessionStatus;

    private String attendanceStatus;

    private LocalDateTime attendedAt;

    private String note;
}