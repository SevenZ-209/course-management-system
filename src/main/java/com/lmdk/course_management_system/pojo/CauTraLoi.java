package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "cau_tra_loi",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cau_tra_loi_lan_lam_cau_hoi",
                        columnNames = {
                                "lan_lam_bai_id",
                                "cau_hoi_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CauTraLoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lan_lam_bai_id",
            nullable = false
    )
    private LanLamBai lanLamBai;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cau_hoi_id",
            nullable = false
    )
    private CauHoi cauHoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dap_an_id")
    private DapAnCauHoi dapAn;

    @Column(
            name = "noi_dung_tra_loi",
            columnDefinition = "TEXT"
    )
    private String noiDungTraLoi;

    @Column(
            name = "diem_dat_duoc",
            precision = 5,
            scale = 2
    )
    private BigDecimal diemDatDuoc;

    @Column(
            name = "nhan_xet_giao_vien",
            columnDefinition = "TEXT"
    )
    private String nhanXetGiaoVien;
}