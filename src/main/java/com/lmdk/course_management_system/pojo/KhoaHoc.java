package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "khoa_hoc")
@Getter
@Setter
@NoArgsConstructor
public class KhoaHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "ten_khoa_hoc",
            nullable = false,
            length = 255
    )
    private String tenKhoaHoc;

    @Column(
            name = "mo_ta",
            columnDefinition = "TEXT"
    )
    private String moTa;

    @Column(
            name = "hoc_phi",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal hocPhi;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false,
            length = 30
    )
    private TrangThaiKhoaHoc trangThai = TrangThaiKhoaHoc.ACTIVE;

    @CreationTimestamp
    @Column(
            name = "ngay_tao",
            nullable = false,
            updatable = false
    )
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    @Column(
            name = "ngay_cap_nhat",
            nullable = false
    )
    private LocalDateTime ngayCapNhat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "danh_muc_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_khoa_hoc_danh_muc"
            )
    )
    private DanhMuc danhMuc;

    @OneToMany(
            mappedBy = "khoaHoc",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("thuTu ASC")
    private List<ModuleHoc> danhSachModule = new ArrayList<>();


    public void themModule(ModuleHoc moduleHoc) {
        if (moduleHoc == null) {
            return;
        }

        danhSachModule.add(moduleHoc);
        moduleHoc.setKhoaHoc(this);
    }

    public void xoaModule(ModuleHoc moduleHoc) {
        if (moduleHoc == null) {
            return;
        }

        danhSachModule.remove(moduleHoc);
        moduleHoc.setKhoaHoc(null);
    }

    public enum TrangThaiKhoaHoc {
        ACTIVE,
        INACTIVE,
        HIDDEN
    }
}