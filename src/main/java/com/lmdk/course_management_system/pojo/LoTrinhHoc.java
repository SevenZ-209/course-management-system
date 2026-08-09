package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lo_trinh_hoc")
@Getter
@Setter
@NoArgsConstructor
public class LoTrinhHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "khoa_hoc_id", nullable = false)
    private KhoaHoc khoaHoc;

    @Column(
            name = "ten_lo_trinh",
            nullable = false,
            length = 255
    )
    private String tenLoTrinh;

    @Column(
            name = "so_bai_moi_ngay",
            nullable = false
    )
    private Integer soBaiMoiNgay = 1;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiLoTrinh trangThai = TrangThaiLoTrinh.ACTIVE;

    @OneToMany(
            mappedBy = "loTrinh",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("thuTu ASC")
    private List<ChiTietLoTrinh> danhSachChiTiet = new ArrayList<>();

    public enum TrangThaiLoTrinh {
        ACTIVE,
        INACTIVE
    }
}