package com.lmdk.course_management_system.dto.student.course;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudentModuleResponse {

    private Integer moduleId;

    private String moduleName;

    private Integer orderNumber;

    private List<StudentLessonResponse> lessons;
}