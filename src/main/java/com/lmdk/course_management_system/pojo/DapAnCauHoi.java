package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dap_an_cau_hoi")
@Getter
@Setter
@NoArgsConstructor
public class DapAnCauHoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cau_hoi_id",
            nullable = false
    )
    private CauHoi cauHoi;

    @Column(
            name = "noi_dung_dap_an",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String noiDungDapAn;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "loai_dap_an",
            nullable = false
    )
    private LoaiDapAn loaiDapAn;

    @Column(
            name = "la_dap_an_dung",
            nullable = false
    )
    private Boolean laDapAnDung = false;

    @Column(name = "thu_tu")
    private Integer thuTu;

    public enum LoaiDapAn {
        LUA_CHON,
        TRA_LOI_NGAN,
        DAP_AN_THAM_KHAO
    }
}