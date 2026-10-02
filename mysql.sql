create database quanlysieuthi

use quanlysieuthi

-- Bảng cha: CHUCVU (cần tạo trước vì NHANVIEN có FK tham chiếu)
CREATE TABLE CHUCVU (
    maChucVu    VARCHAR(36)  NOT NULL,
    tenChucVu   VARCHAR(50),

    PRIMARY KEY (maChucVu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng NHANVIEN theo ERD
CREATE TABLE NHANVIEN (
    maNhanVien      VARCHAR(36)  NOT NULL,
    maChucVu        VARCHAR(36)  NOT NULL,
    hoTen           VARCHAR(100) NOT NULL,
    tenDangNhap     VARCHAR(255) NOT NULL,
    matKhau         VARCHAR(255) NOT NULL,
    soDienThoai     VARCHAR(15)  NOT NULL,
    trangThai       BOOLEAN      DEFAULT TRUE,

    PRIMARY KEY (maNhanVien),
    UNIQUE KEY uk_nhanvien_tendangnhap (tenDangNhap),
    UNIQUE KEY uk_nhanvien_sodienthoai (soDienThoai),

    CONSTRAINT fk_nhanvien_chucvu
        FOREIGN KEY (maChucVu) REFERENCES CHUCVU(maChucVu)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;