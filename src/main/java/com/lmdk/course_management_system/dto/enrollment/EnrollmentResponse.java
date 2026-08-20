package com.lmdk.course_management_system.dto.enrollment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EnrollmentResponse {

    private Integer enrollmentId;

    private Integer studentId;

    private Integer classId;

    private String className;

    private Integer courseId;

    private String courseName;

    private String status;

    private LocalDateTime createdAt;

}