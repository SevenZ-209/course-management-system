package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "parent_links",
        indexes = {
                @Index(name = "idx_parent_link_student_status_expires", columnList = "student_id,status,expires_at"),
                @Index(name = "idx_parent_link_parent_status", columnList = "parent_id,status"),
                @Index(name = "idx_parent_link_status_expires", columnList = "status,expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ParentLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "verification_code", nullable = false, unique = true)
    private String verificationCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParentLinkStatus status = ParentLinkStatus.UNUSED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ParentLinkStatus {
        UNUSED,
        USED,
        EXPIRED,
        UNLINKED
    }
}