package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lan_lam_bai",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lan_lam_bai_lan_thu",
                        columnNames = {
                                "bai_tap_duoc_giao_id",
                                "lan_thu"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class LanLamBai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bai_tap_duoc_giao_id",
            nullable = false
    )
    private BaiTapDuocGiao baiTapDuocGiao;

    @Column(
            name = "lan_thu",
            nullable = false
    )
    private Integer lanThu;

    @Column(
            name = "bat_dau_luc",
            nullable = false
    )
    private LocalDateTime batDauLuc;

    @Column(name = "nop_luc")
    private LocalDateTime nopLuc;

    @Column(name = "thoi_gian_lam")
    private Integer thoiGianLam;

    @Column(name = "dat_yeu_cau")
    private Boolean datYeuCau;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiLanLam trangThai =
            TrangThaiLanLam.DANG_LAM;

    public enum TrangThaiLanLam {
        DANG_LAM,
        DA_NOP,
        CHO_GIAO_VIEN_CHAM,
        DA_CHAM
    }
}