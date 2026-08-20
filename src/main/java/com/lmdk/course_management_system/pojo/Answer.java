package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "answers",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"question_id", "order_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_correct", nullable = false)
    private Boolean correct = false;

    @Column(name = "order_number")
    private Integer orderNumber;

    public enum AnswerType {
        CHOICE,
        SHORT_ANSWER,
        REFERENCE_ANSWER
    }
}