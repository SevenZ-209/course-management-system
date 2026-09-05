package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor
public class CourseClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassStatus status = ClassStatus.UPCOMING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    public ClassStatus getStatus() {
        return resolveStatus(LocalDate.now());
    }

    public ClassStatus resolveStatus(LocalDate today) {
        if (status == ClassStatus.CANCELED)
            return ClassStatus.CANCELED;

        if (startDate == null || endDate == null || today == null)
            return status == null ? ClassStatus.UPCOMING : status;

        if (today.isBefore(startDate))
            return ClassStatus.UPCOMING;

        if (today.isAfter(endDate))
            return ClassStatus.COMPLETED;

        return ClassStatus.ACTIVE;
    }

    public enum ClassStatus {
        UPCOMING, ACTIVE, COMPLETED, CANCELED
    }
}
