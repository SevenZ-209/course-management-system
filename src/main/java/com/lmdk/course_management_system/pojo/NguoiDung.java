package com.lmdk.course_management_system.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "nguoi_dung",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_nguoi_dung_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "ten_user",
            nullable = false,
            length = 255
    )
    private String tenUser;

    @Column(
            name = "email",
            nullable = false,
            length = 255
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "loai",
            nullable = false,
            length = 30
    )
    private LoaiNguoiDung loai;

    @Column(
            name = "avatar",
            length = 500
    )
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trang_thai",
            nullable = false,
            length = 30
    )
    private TrangThaiNguoiDung trangThai = TrangThaiNguoiDung.ACTIVE;

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

    public enum LoaiNguoiDung {
        HOC_VIEN,
        GIAO_VIEN,
        QUAN_LY,
        ADMIN,
        PHU_HUYNH
    }

    public enum TrangThaiNguoiDung {
        ACTIVE,
        INACTIVE,
        LOCKED
    }
}