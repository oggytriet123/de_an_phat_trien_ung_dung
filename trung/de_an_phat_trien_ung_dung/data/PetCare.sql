-- Chạy trong SQL Server Management Studio
IF DB_ID('PetCare') IS NULL CREATE DATABASE PetCare;
GO
USE PetCare;
GO
IF OBJECT_ID('dbo.TaiKhoan') IS NULL
CREATE TABLE dbo.TaiKhoan (
    MaTaiKhoan  VARCHAR(20)   NOT NULL PRIMARY KEY,
    SoDienThoai VARCHAR(15)   NOT NULL UNIQUE,
    MatKhau     CHAR(64)      NOT NULL,          -- SHA-256, hex in hoa
    HoTen       NVARCHAR(100) NOT NULL,
    VaiTro      NVARCHAR(50)  NOT NULL,
    HoatDong    BIT           NOT NULL DEFAULT 1
);
GO
-- Mật khẩu mẫu: 123456
INSERT INTO dbo.TaiKhoan (MaTaiKhoan, SoDienThoai, MatKhau, HoTen, VaiTro)
SELECT 'QL001', '0901234567', CONVERT(CHAR(64), HASHBYTES('SHA2_256', '123456'), 2), N'Hữu Thịnh', N'Quản lý cửa hàng'
WHERE NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE MaTaiKhoan = 'QL001');
INSERT INTO dbo.TaiKhoan (MaTaiKhoan, SoDienThoai, MatKhau, HoTen, VaiTro)
SELECT 'NV01', '0902345678', CONVERT(CHAR(64), HASHBYTES('SHA2_256', '123456'), 2), N'Thu Trang', N'Nhân viên'
WHERE NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE MaTaiKhoan = 'NV01');
GO
