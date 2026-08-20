package com.lmdk.course_management_system.dto.student.learningpath;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class StudentLearningPathResponse {

    private Integer studentLearningPathId;

    private Integer learningPathId;

    private String learningPathName;

    private Integer courseId;

    private String courseName;

    private String status;

    private Integer currentDetailId;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private List<LearningPathDetailProgressResponse> details;
}