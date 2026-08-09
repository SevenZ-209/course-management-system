package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "module_hoc",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_module_khoa_hoc_thu_tu",
                        columnNames = {
                                "khoa_hoc_id",
                                "thu_tu"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ModuleHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "ten_module",
            nullable = false,
            length = 255
    )
    private String tenModule;

    @Column(
            name = "thu_tu",
            nullable = false
    )
    private Integer thuTu;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false,
            length = 30
    )
    private TrangThaiModule trangThai = TrangThaiModule.ACTIVE;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "khoa_hoc_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_module_khoa_hoc"
            )
    )
    private KhoaHoc khoaHoc;

    public enum TrangThaiModule {
        ACTIVE,
        INACTIVE
    }
}