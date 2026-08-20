package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.AssignedAssignment;

import java.util.List;
import java.util.Map;

public interface AssignedAssignmentRepository {

    AssignedAssignment getAssignedAssignmentById(Integer id);

    AssignedAssignment getByStudentAndLearningPathDetail(
            Integer studentId,
            Integer learningPathDetailId
    );

    AssignedAssignment addAssignedAssignment(AssignedAssignment assignedAssignment);

    void updateAssignedAssignment(AssignedAssignment assignedAssignment);

    List<AssignedAssignment> getAssignedAssignments(Map<String, String> params);

    List<AssignedAssignment> getAssignedAssignmentsByStudent(Integer studentId);

    List<AssignedAssignment> getAssignedAssignmentsByStudentAndStatus(
            Integer studentId,
            AssignedAssignment.AssignedStatus status
    );

    long countAssignedAssignments(Map<String, String> params);

    boolean existsByStudentAndLearningPathDetail(
            Integer studentId,
            Integer learningPathDetailId
    );

    List<AssignedAssignment> getAssignedAssignmentsByClass(
            Integer classId
    );
}