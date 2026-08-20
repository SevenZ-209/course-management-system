package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "grading_results",
        uniqueConstraints = @UniqueConstraint(
                columnNames = "attempt_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class GradingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private AssignmentAttempt assignmentAttempt;

    @Column(name = "auto_score", precision = 5, scale = 2)
    private BigDecimal autoScore = BigDecimal.ZERO;

    @Column(name = "essay_score", precision = 5, scale = 2)
    private BigDecimal essayScore = BigDecimal.ZERO;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
}