package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bai_tap_duoc_giao")
@Getter
@Setter
@NoArgsConstructor
public class BaiTapDuocGiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bai_tap_id",
            nullable = false
    )
    private BaiTap baiTap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "hoc_vien_id",
            nullable = false
    )
    private NguoiDung hocVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_giao_id")
    private NguoiDung nguoiGiao;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "nguon_giao",
            nullable = false
    )
    private NguonGiao nguonGiao = NguonGiao.HE_THONG;

    @CreationTimestamp
    @Column(
            name = "ngay_giao",
            nullable = false,
            updatable = false
    )
    private LocalDateTime ngayGiao;

    @Column(name = "han_nop")
    private LocalDateTime hanNop;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiBaiDuocGiao trangThai =
            TrangThaiBaiDuocGiao.CHUA_LAM;

    public enum NguonGiao {
        HE_THONG,
        GIAO_VIEN
    }

    public enum TrangThaiBaiDuocGiao {
        CHUA_LAM,
        DANG_LAM,
        DA_NOP,
        HOAN_THANH
    }
}