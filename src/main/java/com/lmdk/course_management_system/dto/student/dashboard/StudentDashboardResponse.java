package com.lmdk.course_management_system.dto.student.dashboard;

import com.lmdk.course_management_system.dto.student.assignment.ContinueAssignmentResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentDashboardResponse {

    private Integer activeCourses;

    private Integer inProgressAssignments;

    private Integer pendingGradingAssignments;

    private Integer completedLearningPaths;

    private ContinueAssignmentResponse continueAssignment;
}