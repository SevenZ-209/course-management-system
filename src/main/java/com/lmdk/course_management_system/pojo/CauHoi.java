package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "cau_hoi",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cau_hoi_bai_tap_thu_tu",
                        columnNames = {"bai_tap_id", "thu_tu"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CauHoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bai_tap_id",
            nullable = false
    )
    private BaiTap baiTap;

    @Column(
            name = "noi_dung",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String noiDung;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "loai_cau_hoi",
            nullable = false
    )
    private LoaiCauHoi loaiCauHoi;

    @Column(
            name = "diem_toi_da",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal diemToiDa;

    @Column(
            name = "thu_tu",
            nullable = false
    )
    private Integer thuTu;

    @OneToMany(
            mappedBy = "cauHoi",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("thuTu ASC")
    private List<DapAnCauHoi> danhSachDapAn =
            new ArrayList<>();

    public enum LoaiCauHoi {
        TRAC_NGHIEM,
        TRA_LOI_NGAN,
        TU_LUAN
    }
}