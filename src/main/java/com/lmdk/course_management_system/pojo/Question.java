package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "questions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"assignment_id", "order_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal score;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNumber ASC")
    private Set<Answer> answers = new LinkedHashSet<>();

    public enum QuestionType {
        MULTIPLE_CHOICE,
        SHORT_ANSWER,
        ESSAY
    }
}