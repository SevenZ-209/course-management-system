package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.AssignmentAttempt;

import java.util.List;
import java.util.Map;

public interface AssignmentAttemptService {

    AssignmentAttempt getAttemptById(Integer id);

    AssignmentAttempt getLatestAttempt(Integer assignedAssignmentId);

    AssignmentAttempt getInProgressAttempt(Integer assignedAssignmentId);

    AssignmentAttempt startAttempt(Integer assignedAssignmentId, Integer studentId);

    AssignmentAttempt submitAttempt(Integer attemptId, Integer studentId);

    List<AssignmentAttempt> getAttempts(Map<String, String> params);

    List<AssignmentAttempt> getAttemptsByAssignedAssignment(Integer assignedAssignmentId);

    long countAttempts(Map<String, String> params);
}