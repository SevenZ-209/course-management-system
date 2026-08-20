package com.lmdk.course_management_system.dto.student.course;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentLessonDetailResponse {

    private Integer lessonId;
    private String lessonName;

    private Integer moduleId;
    private String moduleName;

    private Integer courseId;
    private String courseName;

    private Integer orderNumber;

    private String fileName;
    private String fileUrl;
}