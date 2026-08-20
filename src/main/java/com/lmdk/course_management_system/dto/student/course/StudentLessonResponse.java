package com.lmdk.course_management_system.dto.student.course;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentLessonResponse {

    private Integer lessonId;

    private String lessonName;

    private Integer orderNumber;
}