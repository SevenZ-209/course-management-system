package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ket_qua_cham")
@Getter
@Setter
@NoArgsConstructor
public class KetQuaCham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lan_lam_bai_id",
            nullable = false,
            unique = true
    )
    private LanLamBai lanLamBai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giao_vien_id")
    private NguoiDung giaoVien;

    @Column(
            name = "diem_tu_dong",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal diemTuDong = BigDecimal.ZERO;

    @Column(
            name = "diem_tu_luan",
            precision = 5,
            scale = 2
    )
    private BigDecimal diemTuLuan;

    @Column(
            name = "diem_tong",
            precision = 5,
            scale = 2
    )
    private BigDecimal diemTong;

    @Column(
            name = "nhan_xet",
            columnDefinition = "TEXT"
    )
    private String nhanXet;

    @Column(name = "ngay_cham")
    private LocalDateTime ngayCham;
}