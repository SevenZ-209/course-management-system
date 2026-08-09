package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lien_ket_phu_huynh")
@Getter
@Setter
@NoArgsConstructor
public class LienKetPhuHuynh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phu_huynh_id")
    private NguoiDung phuHuynh;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "hoc_vien_id",
            nullable = false
    )
    private NguoiDung hocVien;

    @Column(
            name = "ma_xac_thuc",
            nullable = false,
            unique = true,
            length = 30
    )
    private String maXacThuc;

    @Column(
            name = "het_han_luc",
            nullable = false
    )
    private LocalDateTime hetHanLuc;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiLienKet trangThai =
            TrangThaiLienKet.CHUA_SU_DUNG;

    @CreationTimestamp
    @Column(
            name = "ngay_tao",
            nullable = false,
            updatable = false
    )
    private LocalDateTime ngayTao;

    @Column(name = "ngay_lien_ket")
    private LocalDateTime ngayLienKet;

    public enum TrangThaiLienKet {
        CHUA_SU_DUNG,
        DA_SU_DUNG,
        HET_HAN
    }
}