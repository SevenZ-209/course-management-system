package com.lmdk.course_management_system.dto.student.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAssignmentResponse {

    private Integer assignedAssignmentId;

    private Integer assignmentId;

    private String assignmentName;

    private Integer orderNumber;

    private String status;

    private Integer attemptId;

    private Integer latestAttemptNumber;

    private String latestAttemptStatus;

    private Boolean canStart;

    private String assignmentType;

    private Integer durationMinutes;
}