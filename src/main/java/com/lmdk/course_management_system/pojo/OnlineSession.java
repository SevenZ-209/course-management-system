package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "online_sessions",
        indexes = {
                @Index(name = "idx_session_class_start", columnList = "class_id,start_time"),
                @Index(name = "idx_session_start_time", columnList = "start_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OnlineSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private CourseClass courseClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "meeting_url", length = 1000)
    private String meetingUrl;
}