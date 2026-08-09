package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "hoc_vien_lo_trinh",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hoc_vien_lo_trinh",
                        columnNames = {"hoc_vien_id", "lo_trinh_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class HocVienLoTrinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "hoc_vien_id",
            nullable = false
    )
    private NguoiDung hocVien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lo_trinh_id",
            nullable = false
    )
    private LoTrinhHoc loTrinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_tiet_hien_tai_id")
    private ChiTietLoTrinh chiTietHienTai;

    @Column(
            name = "ngay_bat_dau",
            nullable = false
    )
    private LocalDate ngayBatDau;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiHocVienLoTrinh trangThai =
            TrangThaiHocVienLoTrinh.DANG_HOC;

    public enum TrangThaiHocVienLoTrinh {
        DANG_HOC,
        HOAN_THANH,
        TAM_DUNG
    }
}