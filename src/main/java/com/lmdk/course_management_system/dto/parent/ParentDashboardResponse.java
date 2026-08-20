package com.lmdk.course_management_system.dto.parent;

import java.math.BigDecimal;

public record ParentDashboardResponse(
        Integer studentId,
        String studentName,

        Integer activeCourses,

        Integer inProgressLearningPaths,
        Integer completedLearningPaths,

        Integer inProgressAssignments,
        Integer pendingGradingAssignments,

        Integer presentSessions,
        Integer absentSessions,
        Integer notMarkedSessions,

        BigDecimal latestScore,

        ParentContinueAssignmentResponse continueAssignment
) {
}