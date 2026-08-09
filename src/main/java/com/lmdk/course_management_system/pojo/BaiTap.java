package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bai_tap")
@Getter
@Setter
@NoArgsConstructor
public class BaiTap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "khoa_hoc_id", nullable = false)
    private KhoaHoc khoaHoc;

    @Column(name = "ten_bai_tap", nullable = false, length = 255)
    private String tenBaiTap;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_bai_tap", nullable = false)
    private LoaiBaiTap loaiBaiTap;

    @Column(
            name = "diem_toi_da",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal diemToiDa;

    @Column(name = "thoi_gian_lam")
    private Integer thoiGianLam;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiBaiTap trangThai = TrangThaiBaiTap.ACTIVE;

    public enum LoaiBaiTap {
        LUYEN_TAP,
        KIEM_TRA
    }

    public enum TrangThaiBaiTap {
        ACTIVE,
        INACTIVE
    }
}