package com.lmdk.course_management_system.dto.student.course;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class StudentCourseDetailResponse {

    private Integer enrollmentId;

    private Integer courseId;
    private String courseName;

    private Integer classId;
    private String className;

    private LocalDate startDate;
    private LocalDate endDate;

    private String classStatus;
    private String enrollmentStatus;

    private Integer studentLearningPathId;
    private Integer learningPathId;
    private String learningPathName;
    private String learningPathStatus;

    private Integer currentDetailId;
    private Integer currentAssignmentId;
}