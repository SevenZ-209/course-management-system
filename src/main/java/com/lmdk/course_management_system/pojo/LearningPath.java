package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "learning_paths")
@Getter
@Setter
@NoArgsConstructor
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String name;

    @Column(name = "assignments_per_day", nullable = false)
    private Integer assignmentsPerDay = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LearningPathStatus status = LearningPathStatus.ACTIVE;

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber ASC")
    private Set<LearningPathDetail> details = new LinkedHashSet<>();

    public enum LearningPathStatus {
        ACTIVE, INACTIVE
    }
}