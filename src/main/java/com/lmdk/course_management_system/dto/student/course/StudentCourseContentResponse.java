package com.lmdk.course_management_system.dto.student.course;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudentCourseContentResponse {

    private Integer courseId;

    private String courseName;

    private List<StudentModuleResponse> modules;
}