package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "learning_path_details",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"learning_path_id", "order_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class LearningPathDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(name = "minimum_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal minimumScore = BigDecimal.ZERO;

    @Column(name = "minimum_time_seconds")
    private Integer minimumTimeSeconds = 0;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 1;
}