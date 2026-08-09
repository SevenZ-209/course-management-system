package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "buoi_hoc_online")
@Getter
@Setter
@NoArgsConstructor
public class BuoiHocOnline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "lop_hoc_id",
            nullable = false
    )
    private LopHoc lopHoc;

    @Column(
            name = "bat_dau_luc",
            nullable = false
    )
    private LocalDateTime batDauLuc;

    @Column(name = "ket_thuc_luc")
    private LocalDateTime ketThucLuc;

    @Column(
            name = "meeting_url",
            length = 500
    )
    private String meetingUrl;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiBuoiHoc trangThai =
            TrangThaiBuoiHoc.SAP_DIEN_RA;

    public enum TrangThaiBuoiHoc {
        SAP_DIEN_RA,
        DANG_DIEN_RA,
        DA_KET_THUC,
        DA_HUY
    }
}