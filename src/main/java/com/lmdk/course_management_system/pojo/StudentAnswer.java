package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "student_answers",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"attempt_id", "question_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssignmentAttempt assignmentAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer selectedAnswer;

    @Column(name = "answer_content", columnDefinition = "TEXT")
    private String answerContent;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "teacher_comment", columnDefinition = "TEXT")
    private String teacherComment;
}