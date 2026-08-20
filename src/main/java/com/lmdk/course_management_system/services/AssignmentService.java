package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.Assignment;

import java.util.List;
import java.util.Map;

public interface AssignmentService {

    Assignment getAssignmentById(Integer id);

    Assignment addAssignment(Assignment assignment);

    void updateAssignment(Assignment assignment);

    List<Assignment> getAssignments(Map<String, String> params);

    List<Assignment> getAssignmentsByCourse(Integer courseId);

    List<Assignment> getAllAssignments();

    long countAssignments(Map<String, String> params);
}