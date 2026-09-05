package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"online_session_id", "student_id"}
        ),
        indexes = @Index(
                name = "idx_attendance_student_session",
                columnList = "student_id,online_session_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "online_session_id", nullable = false)
    private OnlineSession onlineSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private Boolean present = false;

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    private String note;
}