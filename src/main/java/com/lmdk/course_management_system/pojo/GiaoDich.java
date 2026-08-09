package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "giao_dich")
@Getter
@Setter
@NoArgsConstructor
public class GiaoDich {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "dang_ky_id",
            nullable = false
    )
    private DangKy dangKy;

    @Column(
            name = "ma_giao_dich_cong",
            unique = true,
            length = 255
    )
    private String maGiaoDichCong;

    @Column(
            name = "so_tien",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal soTien;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "phuong_thuc",
            nullable = false
    )
    private PhuongThucThanhToan phuongThuc;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiGiaoDich trangThai =
            TrangThaiGiaoDich.KHOI_TAO;

    @CreationTimestamp
    @Column(
            name = "ngay_giao_dich",
            nullable = false,
            updatable = false
    )
    private LocalDateTime ngayGiaoDich;

    public enum PhuongThucThanhToan {
        VNPAY,
        MOMO,
        CHUYEN_KHOAN,
        TIEN_MAT
    }

    public enum TrangThaiGiaoDich {
        KHOI_TAO,
        THANH_CONG,
        THAT_BAI,
        DA_HUY
    }
}