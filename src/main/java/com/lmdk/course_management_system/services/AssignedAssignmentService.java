package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AssignedAssignmentService {

    AssignedAssignment getAssignedAssignmentById(Integer id);

    AssignedAssignment getAssignedAssignmentByIdForUpdate(Integer id);

    AssignedAssignment assignCurrentDetail(
            Integer studentLearningPathId,
            LocalDateTime availableAt,
            LocalDateTime dueAt
    );


    AssignedAssignment assignManual(
            Integer studentId,
            Integer assignmentId,
            User assignedBy,
            LocalDateTime availableAt,
            LocalDateTime dueAt
    );

    void updateAvailabilityStatus(Integer id, AssignedAssignment.AssignedStatus status);

    List<AssignedAssignment> getAssignedAssignments(Map<String, String> params);

    List<AssignedAssignment> getAssignedAssignmentsByStudent(Integer studentId);

    List<AssignedAssignment> getAssignedAssignmentsByStudentAndStatus(
            Integer studentId,
            AssignedAssignment.AssignedStatus status
    );

    long countAssignedAssignments(Map<String, String> params);

    List<AssignedAssignment> getAssignedAssignmentsByClass(
            Integer classId
    );

    void assignAllLearningPathAssignments(
            Integer studentLearningPathId
    );
}