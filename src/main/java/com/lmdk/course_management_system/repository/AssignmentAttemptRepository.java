package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.AssignmentAttempt;

import java.util.List;
import java.util.Map;

public interface AssignmentAttemptRepository {

    AssignmentAttempt getAttemptById(Integer id);

    AssignmentAttempt getAttempt(Integer assignedAssignmentId, Integer attemptNumber);

    AssignmentAttempt getLatestAttempt(Integer assignedAssignmentId);

    AssignmentAttempt addAttempt(AssignmentAttempt attempt);

    void updateAttempt(AssignmentAttempt attempt);

    List<AssignmentAttempt> getAttempts(Map<String, String> params);

    List<AssignmentAttempt> getAttemptsByAssignedAssignment(Integer assignedAssignmentId);

    long countAttempts(Map<String, String> params);

    long countAttemptsByAssignedAssignment(Integer assignedAssignmentId);

    boolean existsAttemptNumber(Integer assignedAssignmentId, Integer attemptNumber);

    boolean existsInProgressAttempt(Integer assignedAssignmentId);

    AssignmentAttempt getInProgressAttempt(Integer assignedAssignmentId);

    List<AssignmentAttempt> getPendingGradingAttempts();

    List<AssignmentAttempt> getPendingGradingAttemptsByTeacher(Integer teacherId);
}