package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "bai_hoc",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bai_hoc_module_thu_tu",
                        columnNames = {"module_id", "thu_tu"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BaiHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "module_id",
            nullable = false
    )
    private ModuleHoc moduleHoc;

    @Column(
            name = "ten_bai_hoc",
            nullable = false,
            length = 255
    )
    private String tenBaiHoc;

    @Column(
            name = "noi_dung",
            columnDefinition = "TEXT"
    )
    private String noiDung;

    @Column(
            name = "video_url",
            length = 500
    )
    private String videoUrl;

    @Column(
            name = "thu_tu",
            nullable = false
    )
    private Integer thuTu;
}