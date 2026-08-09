package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "chi_tiet_lo_trinh",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chi_tiet_lo_trinh_thu_tu",
                        columnNames = {"lo_trinh_id", "thu_tu"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ChiTietLoTrinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lo_trinh_id", nullable = false)
    private LoTrinhHoc loTrinh;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bai_tap_id", nullable = false)
    private BaiTap baiTap;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu;

    @Column(
            name = "diem_toi_thieu",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal diemToiThieu = BigDecimal.ZERO;

    @Column(name = "so_lan_lam_toi_da", nullable = false)
    private Integer soLanLamToiDa = 1;
}