package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "assignment_attempts",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"assigned_assignment_id", "attempt_number"}
        ),
        indexes = @Index(
                name = "idx_attempt_status_submitted",
                columnList = "status,submitted_at"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class AssignmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_assignment_id", nullable = false)
    private AssignedAssignment assignedAssignment;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    public enum AttemptStatus {
        IN_PROGRESS,
        SUBMITTED,
        PENDING_GRADING,
        GRADED
    }
}