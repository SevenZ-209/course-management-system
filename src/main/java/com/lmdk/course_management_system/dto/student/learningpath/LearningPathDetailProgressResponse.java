package com.lmdk.course_management_system.dto.student.learningpath;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LearningPathDetailProgressResponse {

    private Integer detailId;

    private Integer assignmentId;

    private Integer orderNumber;

    private BigDecimal minimumScore;

    private Integer maxAttempts;

    private String status;
}