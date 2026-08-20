package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule courseModule;

    @Column(nullable = false)
    private String name;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "file_public_id", length = 500)
    private String filePublicId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;
}