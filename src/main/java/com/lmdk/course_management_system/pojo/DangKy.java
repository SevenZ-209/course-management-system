package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "dang_ky",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dang_ky_lop_hoc_vien",
                        columnNames = {"lop_hoc_id", "hoc_vien_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DangKy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lop_hoc_id",
            nullable = false
    )
    private LopHoc lopHoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "hoc_vien_id",
            nullable = false
    )
    private NguoiDung hocVien;

    @CreationTimestamp
    @Column(
            name = "ngay_dang_ky",
            nullable = false,
            updatable = false
    )
    private LocalDateTime ngayDangKy;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiDangKy trangThai =
            TrangThaiDangKy.CHO_THANH_TOAN;

    public enum TrangThaiDangKy {
        CHO_THANH_TOAN,
        DA_THANH_TOAN,
        DA_HUY
    }
}