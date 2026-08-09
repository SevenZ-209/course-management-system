package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "danh_muc")
@Getter
@Setter
@NoArgsConstructor
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "ten_danh_muc",
            nullable = false,
            length = 255
    )
    private String tenDanhMuc;

    @Column(
            name = "mo_ta",
            columnDefinition = "TEXT"
    )
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false
    )
    private TrangThaiDanhMuc trangThai = TrangThaiDanhMuc.ACTIVE;

    public enum TrangThaiDanhMuc {
        ACTIVE,
        INACTIVE
    }
}