package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "lop_hoc")
@Getter
@Setter
@NoArgsConstructor
public class LopHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "ten_lop",
            nullable = false,
            length = 255
    )
    private String tenLop;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(
            name = "si_so_toi_da",
            nullable = false
    )
    private Integer siSoToiDa;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false,
            length = 30
    )
    private TrangThaiLopHoc trangThai = TrangThaiLopHoc.SAP_MO;

    /**
     * Nhiều lớp học có thể được mở từ một khóa học.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "khoa_hoc_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_lop_hoc_khoa_hoc"
            )
    )
    private KhoaHoc khoaHoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "giao_vien_id",
            foreignKey = @ForeignKey(
                    name = "fk_lop_hoc_giao_vien"
            )
    )
    private NguoiDung giaoVien;

    public enum TrangThaiLopHoc {
        SAP_MO,
        DANG_HOC,
        DA_KET_THUC,
        DA_HUY
    }
}