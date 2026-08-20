package com.lmdk.course_management_system.dto.student.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContinueAssignmentResponse {

    private Integer assignedAssignmentId;

    private Integer assignmentId;

    private String assignmentName;

    private Integer courseId;

    private String courseName;

    private Integer attemptId;

    private Integer attemptNumber;

    private String attemptStatus;
}