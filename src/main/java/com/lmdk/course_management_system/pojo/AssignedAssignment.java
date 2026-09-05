package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "assigned_assignments",
        indexes = {
                @Index(name = "idx_assigned_student_status_available", columnList = "student_id,status,available_at"),
                @Index(name = "idx_assigned_student_assigned_at", columnList = "student_id,assigned_at"),
                @Index(name = "idx_assigned_student_lpd", columnList = "student_id,learning_path_detail_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AssignedAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_detail_id")
    private LearningPathDetail learningPathDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "available_at")
    private LocalDateTime availableAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignedStatus status = AssignedStatus.AVAILABLE;

    public enum AssignedStatus {
        LOCKED, AVAILABLE, COMPLETED
    }
}