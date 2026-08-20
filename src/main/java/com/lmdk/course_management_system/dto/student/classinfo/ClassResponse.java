package com.lmdk.course_management_system.dto.student.classinfo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ClassResponse {

    private Integer id;

    private String name;

    private Integer courseId;

    private String courseName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer maxStudents;

    private String status;
}