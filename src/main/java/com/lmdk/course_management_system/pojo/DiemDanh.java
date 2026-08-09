package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "diem_danh",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_diem_danh_buoi_hoc_vien",
                        columnNames = {"buoi_hoc_id", "hoc_vien_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DiemDanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "buoi_hoc_id",
            nullable = false
    )
    private BuoiHocOnline buoiHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "hoc_vien_id",
            nullable = false
    )
    private NguoiDung hocVien;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiDiemDanh trangThai;

    @Column(
            name = "ghi_chu",
            length = 500
    )
    private String ghiChu;

    public enum TrangThaiDiemDanh {
        CO_MAT,
        VANG_MAT,
        DI_MUON,
        CO_PHEP
    }
}