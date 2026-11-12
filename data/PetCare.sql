-- ============================================================================
-- PETCARE MANAGEMENT SYSTEM - MASTER DATABASE SCRIPT
-- Hệ thống Quản Lý Cửa Hàng & Dịch Vụ Chăm Sóc Thú Cưng PetCare
-- Mã hóa: UTF-8 with BOM (Mở bằng SSMS tiếng Việt chuẩn 100%)
-- ============================================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'PetCare')
BEGIN
    ALTER DATABASE PetCare SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PetCare;
END;
GO

CREATE DATABASE PetCare;
GO

USE PetCare;
GO

-- ============================================================================
-- I. BẢNG PHÂN QUYỀN & TÀI KHOẢN HỆ THỐNG
-- ============================================================================

CREATE TABLE PHANQUYEN (
    maQuyen     INT IDENTITY(1,1) PRIMARY KEY,
    tenQuyen    NVARCHAR(50) NOT NULL UNIQUE
);
GO

CREATE TABLE CHUCNANG (
    maChucNang  INT IDENTITY(1,1) PRIMARY KEY,
    tenChucNang NVARCHAR(100) NOT NULL UNIQUE,
    moTa        NVARCHAR(255) NULL
);
GO

CREATE TABLE QUYEN_CHUCNANG (
    maQuyen     INT NOT NULL,
    maChucNang  INT NOT NULL,
    PRIMARY KEY (maQuyen, maChucNang),
    CONSTRAINT FK_QCN_Quyen    FOREIGN KEY (maQuyen)    REFERENCES PHANQUYEN(maQuyen) ON DELETE CASCADE,
    CONSTRAINT FK_QCN_ChucNang FOREIGN KEY (maChucNang) REFERENCES CHUCNANG(maChucNang) ON DELETE CASCADE
);
GO

CREATE TABLE NHANVIEN (
    maNV        NVARCHAR(20) PRIMARY KEY,
    hoTen       NVARCHAR(100) NOT NULL,
    tenDangNhap NVARCHAR(50)  NOT NULL UNIQUE,
    matKhau     NVARCHAR(255) NOT NULL,
    maQuyen     INT NOT NULL,
    soDienThoai NVARCHAR(15) NULL,
    ngaySinh    DATE NULL,
    email       NVARCHAR(100) NULL,
    trangThai   NVARCHAR(30) DEFAULT N'Đang làm việc',
    caLamViec   NVARCHAR(50) NULL,
    ngayVaoLam  DATE DEFAULT GETDATE(),
    CONSTRAINT FK_NV_Quyen FOREIGN KEY (maQuyen) REFERENCES PHANQUYEN(maQuyen)
);
GO

CREATE TABLE QUANLY (
    maNV NVARCHAR(20) PRIMARY KEY,
    CONSTRAINT FK_QL_NV FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

CREATE TABLE NHANVIEN_BANHANG (
    maNV NVARCHAR(20) PRIMARY KEY,
    CONSTRAINT FK_BH_NV FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

CREATE TABLE NHANVIEN_KHO (
    maNV NVARCHAR(20) PRIMARY KEY,
    CONSTRAINT FK_KHO_NV FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

CREATE TABLE NHANVIEN_BACSI (
    maNV       NVARCHAR(20) PRIMARY KEY,
    chuyenMon  NVARCHAR(200) NULL,
    CONSTRAINT FK_BS_NV FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

-- ============================================================================
-- II. KHÁCH HÀNG & THÚ CƯNG
-- ============================================================================

CREATE TABLE KHACHHANG (
    maKH        NVARCHAR(20) PRIMARY KEY,
    hoTen       NVARCHAR(100) NOT NULL,
    soDienThoai NVARCHAR(15)  NOT NULL,
    email       NVARCHAR(100) NULL,
    diemTichLuy INT DEFAULT 0,
    hangThe     NVARCHAR(30) DEFAULT N'Thường',
    maNV_BH     NVARCHAR(20) NULL,
    ngayTao     DATE DEFAULT GETDATE(),
    tongChiTieu BIGINT       DEFAULT 0,
    CONSTRAINT FK_KH_NVBH FOREIGN KEY (maNV_BH) REFERENCES NHANVIEN_BANHANG(maNV)
);
GO

CREATE TABLE THUCUNG (
    petID        NVARCHAR(20) PRIMARY KEY,
    ten          NVARCHAR(100) NOT NULL,
    loai         NVARCHAR(50)  NOT NULL,
    giong        NVARCHAR(100) NULL,
    gioiTinh     NVARCHAR(10)  NULL,
    tuoi         INT NULL,
    canNang      FLOAT NULL,
    theCanhBao   NVARCHAR(255) NULL,
    trangThai    NVARCHAR(50)  DEFAULT N'Khỏe mạnh',
    ngayTiepNhan DATE DEFAULT GETDATE(),
    maKH         NVARCHAR(20) NULL,
    CONSTRAINT FK_TC_KH FOREIGN KEY (maKH) REFERENCES KHACHHANG(maKH) ON DELETE SET NULL
);
GO

-- ============================================================================
-- III. SẢN PHẨM & DỊCH VỤ
-- ============================================================================

CREATE TABLE SANPHAM (
    sku_maSP    NVARCHAR(20) PRIMARY KEY,
    tenSP       NVARCHAR(150) NOT NULL,
    quyCach     NVARCHAR(100) NULL,
    danhMuc     NVARCHAR(100) NULL,
    donViTinh   NVARCHAR(30)  NULL,
    donGia      FLOAT NOT NULL,
    soLuongTon  INT DEFAULT 0,
    hanSuDung   DATE NULL,
    maNV_Kho    NVARCHAR(20) NULL,
    CONSTRAINT FK_SP_NVKho FOREIGN KEY (maNV_Kho) REFERENCES NHANVIEN_KHO(maNV)
);
GO

CREATE TABLE DICHVU (
    maDV             NVARCHAR(20) PRIMARY KEY,
    tenDV            NVARCHAR(100) NOT NULL,
    giaTien          FLOAT NOT NULL,
    loaiDV           NVARCHAR(50) NULL,
    thoiGianThucHien INT NULL,
    moTa             NVARCHAR(500) NULL,
    trangThai        NVARCHAR(30) DEFAULT N'Đang cung cấp',
    doiTuong         NVARCHAR(100) NULL
);
GO

-- ============================================================================
-- IV. MÃ GIẢM GIÁ & ĐỐI TÁC
-- ============================================================================

CREATE TABLE MAGIAMGIA (
    maGiamGia NVARCHAR(20) PRIMARY KEY,
    phanTram  BIT DEFAULT 1,
    giaTri    FLOAT DEFAULT 0,
    hanDung   DATE NULL,
    trangThai NVARCHAR(30) DEFAULT N'Hoạt động',
    ghiChu    NVARCHAR(255) NULL,
    soLuotToiDa INT DEFAULT 0,
    daSuDung INT DEFAULT 0,
    danhMucApDung NVARCHAR(255) DEFAULT N'Tất cả',
    muaX INT DEFAULT 0,
    tangY INT DEFAULT 0
);
GO

CREATE TABLE DOITAC (
    maDT        NVARCHAR(20) PRIMARY KEY,
    tenDT       NVARCHAR(150) NOT NULL,
    loaiDT      NVARCHAR(50) NOT NULL,
    soDienThoai NVARCHAR(20) NULL,
    diaChi      NVARCHAR(200) NULL,
    email       NVARCHAR(100) NULL,
    trangThai   NVARCHAR(30) DEFAULT N'Đang hợp tác'
);
GO

-- ============================================================================
-- V. HÓA ĐƠN & CHI TIẾT HÓA ĐƠN
-- ============================================================================

CREATE TABLE HOADON (
    maHD          NVARCHAR(20) PRIMARY KEY,
    ngayLap       DATE DEFAULT GETDATE(),
    tongTien      FLOAT DEFAULT 0,
    maNV_BH       NVARCHAR(20) NULL,
    maKH          NVARCHAR(20) NULL,
    soO           INT NULL,
    trangThai     NVARCHAR(30) DEFAULT N'Chưa thanh toán',
    phuongThuc    NVARCHAR(30) NULL,
    ngayTao       DATETIME DEFAULT GETDATE(),
    ngayThanhToan DATETIME NULL,
    tienGiam      FLOAT DEFAULT 0,
    maGiamGia     NVARCHAR(20) NULL,
    tenKH         NVARCHAR(100) NULL,
    soDienThoai   NVARCHAR(20) NULL,
    CONSTRAINT FK_HD_NVBH FOREIGN KEY (maNV_BH) REFERENCES NHANVIEN_BANHANG(maNV),
    CONSTRAINT FK_HD_KH   FOREIGN KEY (maKH)    REFERENCES KHACHHANG(maKH)
);
GO

CREATE TABLE CHITIETHOADON (
    maCTHD    NVARCHAR(30) PRIMARY KEY,
    maHD      NVARCHAR(20) NOT NULL,
    sku_maSP  NVARCHAR(20) NULL,
    soLuong   INT DEFAULT 1,
    donGia    FLOAT NOT NULL,
    tenSP     NVARCHAR(150) NULL,
    danhMuc   NVARCHAR(100) NULL,
    isDichVu  BIT DEFAULT 0,
    sku       NVARCHAR(50) NULL,
    maDV      NVARCHAR(50) NULL,
    thanhTien FLOAT NULL,
    CONSTRAINT FK_CTHD_HD FOREIGN KEY (maHD)     REFERENCES HOADON(maHD) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_SP FOREIGN KEY (sku_maSP) REFERENCES SANPHAM(sku_maSP)
);
GO

-- ============================================================================
-- VI. CHUỒNG LƯU TRÚ & PHIẾU LƯU TRÚ
-- ============================================================================

CREATE TABLE CHUONGLUUTRU (
    cageID       NVARCHAR(20) PRIMARY KEY,
    size         NVARCHAR(50) NULL,
    trangThai    NVARCHAR(30) DEFAULT N'Trống',
    giaTheoDem   FLOAT DEFAULT 0,
    maNV_QL      NVARCHAR(20) NULL,
    khuVuc       NVARCHAR(50) NULL,
    canNangToiDa FLOAT NULL,
    tenChuong    NVARCHAR(100) NULL,
    CONSTRAINT FK_CLT_NVQL FOREIGN KEY (maNV_QL) REFERENCES QUANLY(maNV)
);
GO

CREATE TABLE PHIEU_LUUTRU (
    maPhieu          NVARCHAR(50) PRIMARY KEY,
    cageID           NVARCHAR(20) NOT NULL,
    maKH             NVARCHAR(20) NULL,
    petID            NVARCHAR(20) NULL,
    tenPet           NVARCHAR(100) NULL,
    loaiPet          NVARCHAR(50) NULL,
    tenKhach         NVARCHAR(100) NULL,
    sdtKhach         NVARCHAR(20) NULL,
    ngayVao          DATETIME NULL DEFAULT GETDATE(),
    ngayRaDuKien     DATETIME NULL,
    ngayRaThucTe     DATETIME NULL,
    tienCoc          FLOAT DEFAULT 0,
    tongTien         FLOAT DEFAULT 0,
    trangThai        NVARCHAR(50) DEFAULT N'Đang lưu trú',
    ghiChu           NVARCHAR(500) NULL,
    ngayBatDau       DATE NULL,
    ngayKetThuc      DATE NULL,
    maNV_QL          NVARCHAR(20) NULL,
    tinhTrangSucKhoe NVARCHAR(255) NULL,
    CONSTRAINT FK_PLT_Pet    FOREIGN KEY (petID)   REFERENCES THUCUNG(petID),
    CONSTRAINT FK_PLT_Cage   FOREIGN KEY (cageID)  REFERENCES CHUONGLUUTRU(cageID),
    CONSTRAINT FK_PLT_NVQL   FOREIGN KEY (maNV_QL) REFERENCES QUANLY(maNV)
);
GO

CREATE TABLE CHITIET_DICHVU_CHUONG (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    maPhieu    NVARCHAR(50) NOT NULL,
    maDV       NVARCHAR(20) NULL,
    tenDV      NVARCHAR(100) NOT NULL,
    loai       NVARCHAR(50) NULL,
    thoiGian   DATETIME DEFAULT GETDATE(),
    ngaySuDung DATE NULL,
    soLuong    INT DEFAULT 1,
    donGia     FLOAT DEFAULT 0,
    CONSTRAINT FK_CDVC_Phieu FOREIGN KEY (maPhieu) REFERENCES PHIEU_LUUTRU(maPhieu) ON DELETE CASCADE,
    CONSTRAINT FK_CDVC_DV    FOREIGN KEY (maDV)    REFERENCES DICHVU(maDV)
);
GO

-- ============================================================================
-- VII. CÁC BẢNG BỔ TRỢ PHÒNG KHÁM, KHO & LỊCH HẸN
-- ============================================================================

CREATE TABLE DANHMUCBENH (
    maBenh    NVARCHAR(20) PRIMARY KEY,
    tenBenh   NVARCHAR(150) NOT NULL,
    trieuChung NVARCHAR(500) NULL,
    maNV_QL   NVARCHAR(20) NULL,
    CONSTRAINT FK_DMB_NVQL FOREIGN KEY (maNV_QL) REFERENCES QUANLY(maNV)
);
GO

CREATE TABLE PHIEUKHO (
    maPhieu       NVARCHAR(30) PRIMARY KEY,
    loaiPhieu     NVARCHAR(50) NOT NULL,
    ngayLap       DATE NOT NULL,
    gioLap        NVARCHAR(10) NOT NULL,
    nguoiThucHien NVARCHAR(100) NOT NULL,
    doiTac        NVARCHAR(150) NOT NULL,
    tongGiaTri    BIGINT NOT NULL,
    trangThai     NVARCHAR(50) NOT NULL DEFAULT N'Hoàn tất',
    ghiChu        NVARCHAR(500) NULL,
    maNV          NVARCHAR(20) NULL,
    maDT          NVARCHAR(20) NULL,
    CONSTRAINT FK_PK_NV FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV),
    CONSTRAINT FK_PK_DT FOREIGN KEY (maDT) REFERENCES DOITAC(maDT)
);
GO

CREATE TABLE LICHHEN (
    maLH        NVARCHAR(20) PRIMARY KEY,
    ngayHen     DATE NOT NULL,
    khungGio    NVARCHAR(30) NOT NULL,
    trangThai   NVARCHAR(30) DEFAULT N'Chờ xác nhận',
    petID       NVARCHAR(20) NULL,
    maNV_BS     NVARCHAR(20) NULL,
    maDV        NVARCHAR(20) NULL,
    cageID      NVARCHAR(20) NULL,
    tenKH       NVARCHAR(100) NULL,
    soDienThoai NVARCHAR(20) NULL,
    tenPet      NVARCHAR(100) NULL,
    tenDV       NVARCHAR(100) NULL,
    tenBacSi    NVARCHAR(100) NULL,
    gioHen      NVARCHAR(20) NULL,
    ghiChu      NVARCHAR(255) NULL,
    nhatKy      NVARCHAR(500) NULL,
    maKH        NVARCHAR(20) NULL,
    CONSTRAINT FK_LH_Pet  FOREIGN KEY (petID)   REFERENCES THUCUNG(petID),
    CONSTRAINT FK_LH_NVBS FOREIGN KEY (maNV_BS) REFERENCES NHANVIEN_BACSI(maNV),
    CONSTRAINT FK_LH_DV   FOREIGN KEY (maDV)    REFERENCES DICHVU(maDV),
    CONSTRAINT FK_LH_Cage FOREIGN KEY (cageID)  REFERENCES CHUONGLUUTRU(cageID),
    CONSTRAINT FK_LH_KH   FOREIGN KEY (maKH)    REFERENCES KHACHHANG(maKH)
);
GO

CREATE TABLE PHAUTHUAT (
    maCa          NVARCHAR(30) PRIMARY KEY,
    tenThuCung    NVARCHAR(100) NOT NULL,
    loaiThuCung   NVARCHAR(50) NULL,
    tenChu        NVARCHAR(100) NULL,
    sdt           NVARCHAR(20) NULL,
    loaiPhauThuat NVARCHAR(150) NOT NULL,
    ngayMo        DATE NOT NULL,
    gioMo         NVARCHAR(10) NOT NULL,
    bacSiChinh    NVARCHAR(100) NOT NULL,
    chiPhi        BIGINT NOT NULL,
    trangThai     NVARCHAR(50) NOT NULL DEFAULT N'Chờ phẫu thuật',
    ghiChu        NVARCHAR(500) NULL,
    petID         NVARCHAR(20) NULL,
    maKH          NVARCHAR(20) NULL,
    CONSTRAINT FK_PT_Pet FOREIGN KEY (petID) REFERENCES THUCUNG(petID),
    CONSTRAINT FK_PT_KH  FOREIGN KEY (maKH)  REFERENCES KHACHHANG(maKH)
);
GO

CREATE TABLE TIEMPHONG (
    maPhieu     NVARCHAR(30) PRIMARY KEY,
    tenThuCung  NVARCHAR(100) NOT NULL,
    loaiThuCung NVARCHAR(50) NULL,
    tenChu      NVARCHAR(100) NULL,
    sdt         NVARCHAR(20) NULL,
    loaiVaccine NVARCHAR(150) NOT NULL,
    ngayTiem    DATE NOT NULL,
    ngayHenNhac DATE NULL,
    bacSi       NVARCHAR(100) NOT NULL,
    trangThai   NVARCHAR(50) NOT NULL DEFAULT N'Đã tiêm',
    ghiChu      NVARCHAR(500) NULL,
    petID       NVARCHAR(20) NULL,
    maKH        NVARCHAR(20) NULL,
    CONSTRAINT FK_TP_Pet FOREIGN KEY (petID) REFERENCES THUCUNG(petID),
    CONSTRAINT FK_TP_KH  FOREIGN KEY (maKH)  REFERENCES KHACHHANG(maKH)
);
GO

-- ============================================================================
-- VIII. NẠP DỮ LIỆU BAN ĐẦU
-- ============================================================================

-- 1. PHANQUYEN
SET IDENTITY_INSERT PHANQUYEN ON;
INSERT INTO PHANQUYEN (maQuyen, tenQuyen) VALUES
(1, N'Quản lý'),
(2, N'Bán hàng'),
(3, N'Kho'),
(4, N'Bác sĩ');
SET IDENTITY_INSERT PHANQUYEN OFF;
GO

-- 2. CHUCNANG
SET IDENTITY_INSERT CHUCNANG ON;
INSERT INTO CHUCNANG (maChucNang, tenChucNang, moTa) VALUES
(1,  N'Bán Hàng & Thu Ngân',   N'Lập hóa đơn và tính tiền tại quầy'),
(2,  N'Quản Lý Khách Hàng',    N'Thông tin khách hàng và lịch sử giao dịch'),
(3,  N'Quản Lý Nhân Viên',     N'Hồ sơ nhân sự, phân quyền và xếp ca'),
(4,  N'Hồ Sơ Thú Cưng',        N'Quản lý hồ sơ sức khỏe và thông tin thú cưng'),
(5,  N'Khám Bệnh & Tiêm Chủng',N'Ghi nhận khám bệnh, tiêm phòng và kê đơn'),
(6,  N'Lưu Trú Thú Cưng',      N'Đặt chuồng và theo dõi thú cưng lưu trú'),
(7,  N'Quản Lý Kho Hàng',      N'Kiểm kê, nhập xuất kho sản phẩm'),
(8,  N'Báo Cáo & Thống Kê',    N'Báo cáo doanh thu, tồn kho và khách hàng'),
(9,  N'Quản Lý Đối Tác',       N'Nhà cung cấp và khách hàng sỉ'),
(10, N'Quản Lý Lịch Hẹn',      N'Đặt hẹn khám bệnh, tiêm phòng và dịch vụ'),
(11, N'Hồ Sơ Dịch Vụ',         N'Danh mục dịch vụ và bảng giá'),
(12, N'Bệnh Án & Phác Đồ',     N'Phác đồ điều trị và lịch sử bệnh tật'),
(13, N'Lịch Sử Nhập Xuất',     N'Theo dõi lịch sử phiếu nhập xuất kho'),
(14, N'Quản Lý Đơn Thuốc',     N'Kê đơn và xuất thuốc cho thú cưng'),
(15, N'Quản Lý Phẫu Thuật',    N'Theo dõi ca mổ, bác sĩ chính và hậu phẫu'),
(16, N'Quản Lý Tiêm Phòng',    N'Sổ tiêm chủng vaccine và nhắc lịch hẹn'),
(17, N'Khuyến Mãi',           N'Chương trình khuyến mãi và mã giảm giá');
SET IDENTITY_INSERT CHUCNANG OFF;
GO

-- 3. QUYEN_CHUCNANG
INSERT INTO QUYEN_CHUCNANG (maQuyen, maChucNang) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),
(2,1),(2,2),(2,4),(2,5),(2,6),(2,7),(2,8),(2,10),(2,11),(2,14),(2,17),
(3,1),(3,2),(3,7),(3,9),(3,13),(3,14),
(4,1),(4,2),(4,4),(4,5),(4,6),(4,10),(4,11),(4,12),(4,13),(4,14),(4,15),(4,16);
GO

-- 4. NHANVIEN (Mật khẩu: 123456 hash SHA-256)
INSERT INTO NHANVIEN (maNV, hoTen, tenDangNhap, matKhau, maQuyen, soDienThoai, ngaySinh, email, trangThai, caLamViec, ngayVaoLam) VALUES
(N'NV001', N'Hùng Hữu Thịnh', N'admin',      '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 1, '0901000001', '1995-03-15', 'thinh.hh@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2023-01-01'),
(N'NV002', N'Hà Trọng Trung', N'trung.ht',   '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 2, '0901000002', '1998-07-20', 'trung.ht@petcare.vn', N'Đang làm việc', N'Ca sáng (07:30 - 15:30)', '2023-03-15'),
(N'NV003', N'Lê Tuấn Anh',    N'tuananh.lt', '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 3, '0901000003', '1997-11-05', 'tuananh.lt@petcare.vn', N'Đang làm việc', N'Ca sáng (07:30 - 15:30)', '2023-02-01'),
(N'NV004', N'Nguyễn Hoài Anh',N'hoaianh.nh', '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 4, '0901000004', '1992-09-12', 'hoaianh.nh@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2022-10-01'),
(N'NV005', N'Nguyễn Ngọc Toàn',N'toan.nn',   '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 2, '0901000005', '1999-01-25', 'toan.nn@petcare.vn', N'Đang làm việc', N'Ca chiều (14:30 - 21:30)', '2023-05-10'),
(N'NV006', N'Võ Thanh Hùng',  N'hung06',     '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 4, '0901000006', '1990-12-08', 'hung.vt@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2022-08-15'),
(N'NV007', N'Đặng Thị Mai',   N'mai07',      '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 3, '0901000007', '1996-04-18', 'mai.dt@petcare.vn', N'Đang làm việc', N'Ca chiều (14:30 - 21:30)', '2023-04-01'),
(N'NV008', N'Ngô Gia Bảo',    N'bao08',      '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 4, '0901000008', '1994-06-30', 'bao.ng@petcare.vn', N'Đang làm việc', N'Ca xoay', '2023-02-15'),
(N'NV009', N'Đỗ Phương Thảo', N'thao09',     '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 2, '0901000009', '2000-08-14', 'thao.dp@petcare.vn', N'Đang làm việc', N'Ca sáng (07:30 - 15:30)', '2023-07-01'),
(N'NV010', N'Bùi Hữu Thịnh',  N'thinh10',    '8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 1, '0901000010', '1993-02-28', 'thinh.bh@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2022-05-01');
GO

INSERT INTO QUANLY (maNV) VALUES (N'NV001'), (N'NV010');
INSERT INTO NHANVIEN_BANHANG (maNV) VALUES (N'NV002'), (N'NV005'), (N'NV009');
INSERT INTO NHANVIEN_KHO (maNV) VALUES (N'NV003'), (N'NV007');
INSERT INTO NHANVIEN_BACSI (maNV) VALUES (N'NV004'), (N'NV006'), (N'NV008');
GO

-- 5. KHACHHANG
INSERT INTO KHACHHANG (maKH, hoTen, soDienThoai, email, diemTichLuy, hangThe, maNV_BH, ngayTao, tongChiTieu) VALUES
(N'KH001', N'Nguyễn Minh Anh', '0903123456', 'minhanh.ng@gmail.com', 2030, N'VIP',        N'NV002', '2023-01-15', 2030000),
(N'KH002', N'Lê Thị Cẩm Tú',   '0938222111', 'camtu.le@gmail.com',    680,  N'VIP',        N'NV002', '2023-02-20', 680000),
(N'KH003', N'Phạm Gia Hân',    '0977654321', 'giahan.pham@gmail.com', 320,  N'Thân thiết', N'NV005', '2023-03-10', 320000),
(N'KH004', N'Võ Hoàng Long',   '0386909808', 'hoanglong.vo@gmail.com',1910, N'VIP',        N'NV005', '2023-01-28', 1910000),
(N'KH005', N'Hoàng Minh Tuấn', '0945678901', 'minhtuan.h@gmail.com',  150,  N'Thân thiết', N'NV009', '2023-06-15', 150000),
(N'KH006', N'Bùi Anh Khoa',    '0919888000', 'anhkhoa.bui@gmail.com', 3270, N'VIP',        N'NV002', '2023-02-05', 3270000),
(N'KH007', N'Đặng Thu Hà',     '0905777333', 'thuha.dang@gmail.com',  410,  N'Thân thiết', N'NV009', '2023-04-12', 410000),
(N'KH008', N'Hoàng Thị Mai',   '0932111222', 'mai.hoang@gmail.com',   95,   N'Thường',     N'NV005', '2023-07-20', 95000),
(N'KH009', N'Võ Phát Huy',     '0917099576', 'phathuy.vo@gmail.com',  47,   N'Thường',     N'NV002', '2026-11-02', 47000);
GO

-- 6. THUCUNG
INSERT INTO THUCUNG (petID, ten, loai, giong, gioiTinh, tuoi, canNang, theCanhBao, trangThai, ngayTiepNhan, maKH) VALUES
(N'PET001', N'Kitty',      N'Mèo', N'Anh lông ngắn',    N'Cái', 2, 4.2,  N'Bình thường',             N'Khỏe mạnh',       '2023-01-15', N'KH001'),
(N'PET002', N'Milu',       N'Chó', N'Golden Retriever', N'Đực', 3, 28.5, N'Dị ứng hải sản',          N'Đang điều trị',   '2023-02-20', N'KH002'),
(N'PET003', N'Mochi',      N'Mèo', N'Ba Tư (Persian)',  N'Cái', 1, 3.8,  N'Bình thường',             N'Khỏe mạnh',       '2023-03-01', N'KH002'),
(N'PET004', N'Rocky',      N'Chó', N'Rottweiler',       N'Đực', 4, 35.0, N'Hung dữ khi gặp người lạ', N'Hậu phẫu',        '2023-01-28', N'KH004'),
(N'PET005', N'Mimi',       N'Mèo', N'Xiêm (Siamese)',   N'Cái', 2, 3.2,  N'Bình thường',             N'Chờ phẫu thuật',  '2023-02-15', N'KH004'),
(N'PET006', N'Bông Gòn',   N'Chó', N'Samoyed',          N'Đực', 1, 18.0, N'Bình thường',             N'Khỏe mạnh',       '2023-02-05', N'KH006'),
(N'PET007', N'Buddy',      N'Chó', N'Poodle Toy',       N'Đực', 3, 4.5,  N'Viêm da cơ địa',          N'Đang lưu trú',    '2023-06-15', N'KH005'),
(N'PET008', N'Mochi Béo',  N'Mèo', N'Munchkin',         N'Cái', 2, 3.5,  N'Bình thường',             N'Khỏe mạnh',       '2023-07-20', N'KH008'),
(N'PET009', N'Corgi Mông', N'Chó', N'Corgi Pembroke',   N'Đực', 2, 11.2, N'Bình thường',             N'Khỏe mạnh',       '2023-03-12', N'KH001'),
(N'PET010', N'Bánh Bao',   N'Thỏ', N'Thỏ Minilop',      N'Cái', 1, 1.8,  N'Nhạy cảm tiếng ồn',       N'Khỏe mạnh',       '2023-04-10', N'KH003'),
(N'PET011', N'Đậu Phộng',  N'Chó', N'Pomeranian',       N'Cái', 2, 3.0,  N'Bình thường',             N'Khỏe mạnh',       '2023-04-12', N'KH007'),
(N'PET012', N'Bằng Xù',    N'Mèo', N'Mèo Ragdoll',      N'Đực', 3, 5.1,  N'Bình thường',             N'Đã hồi phục',     '2023-05-02', N'KH006'),
(N'PET013', N'bi',         N'Chó', N'Golden Retriever', N'Đực', 3, 36.0, N'Bình thường',             N'Đang theo dõi',   '2026-11-02', N'KH009');
GO

-- 7. DICHVU (Phân loại chuẩn 7 danh mục chuyên môn thú y với dữ liệu phong phú)
INSERT INTO DICHVU (maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong) VALUES
-- Khám & Điều trị
(N'DV001', N'Khám tổng quát',             200000, N'Khám & Điều trị',    30,   N'Khám tổng quát tai mũi họng, tim phổi, kiểm tra thân nhiệt và tư vấn sức khỏe', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV009', N'Khám mắt & tai chuyên sâu',   150000, N'Khám & Điều trị',    25,   N'Soi đáy mắt, đo nhãn áp và kiểm tra sâu bên trong ống tai', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV010', N'Cấp cứu & truyền dịch giải độc', 350000, N'Khám & Điều trị', 60,   N'Xử lý sốc thuốc, ngộ độc, truyền dịch cân bằng điện giải', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV011', N'Tái khám & thay băng định kỳ', 100000, N'Khám & Điều trị',  20,   N'Kiểm tra tiến triển bệnh án, rửa sát trùng và thay băng', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'YT01', N'Đo nhiệt độ & kiểm tra sức khỏe', 30000, N'Khám & Điều trị', 15,    N'Đo thân nhiệt nhịp tim hàng ngày', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'YT02', N'Vệ sinh vết thương',          40000, N'Khám & Điều trị',     20,   N'Rửa sát trùng và bôi thuốc ngoài da', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'YT03', N'Cho uống thuốc theo toa',     20000, N'Khám & Điều trị',     10,   N'Cho bé uống thuốc đúng giờ theo chỉ định BS', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'YT04', N'Nhỏ mắt / bôi thuốc tai',     20000, N'Khám & Điều trị',     10,   N'Vệ sinh và nhỏ dung dịch điều trị', N'Đang cung cấp', N'Tất cả thú cưng'),

-- Tiêm phòng
(N'DV002', N'Tiêm phòng vaccine',         250000, N'Tiêm phòng',         15,   N'Tiêm vaccine định kỳ phòng ngừa các bệnh truyền nhiễm nguy hiểm',              N'Đang cung cấp', N'Chó & Mèo'),
(N'VAC-001', N'Tiêm vaccine phòng Dại (Rabies)', 250000, N'Tiêm phòng', 15,   N'Tiêm vaccine phòng bệnh dại định kỳ cho chó và mèo', N'Đang cung cấp', N'Chó & Mèo'),
(N'VAC-002', N'Tiêm vaccine 4 bệnh mèo (Nobivac Ducat)', 280000, N'Tiêm phòng', 15, N'Vaccine phòng 4 bệnh truyền nhiễm nguy hiểm ở mèo', N'Đang cung cấp', N'Mèo'),
(N'VAC-003', N'Tiêm vaccine 5 bệnh chó',  260000, N'Tiêm phòng',         15,   N'Vaccine phòng 5 bệnh truyền nhiễm nguy hiểm ở chó', N'Đang cung cấp', N'Chó'),
(N'VAC-004', N'Tiêm vaccine 7 bệnh chó (Vanguard Plus 5/L4)', 300000, N'Tiêm phòng', 15, N'Vaccine tổng hợp 7 bệnh phổ biến cho chó', N'Đang cung cấp', N'Chó'),

-- Phẫu thuật
(N'SUR-001', N'Triệt sản mèo cái nội soi', 650000, N'Phẫu thuật',       45,   N'Phẫu thuật nội soi triệt sản ít xâm lấn, hồi phục nhanh', N'Đang cung cấp', N'Mèo'),
(N'SUR-002', N'Triệt sản mèo đực',        350000, N'Phẫu thuật',         30,   N'Tiểu phẫu triệt sản an toàn cho mèo đực', N'Đang cung cấp', N'Mèo'),
(N'SUR-003', N'Phẫu thuật lấy sỏi bàng quang', 1500000, N'Phẫu thuật',   60,   N'Mổ hở gắp sỏi và thông đường tiết niệu', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'SUR-004', N'Phẫu thuật kết hợp xương gãy', 2500000, N'Phẫu thuật',    90,   N'Cố định xương gãy bằng nẹp vis chuyên dụng', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'SUR-005', N'Cắt mộng mắt',             500000, N'Phẫu thuật',         30,   N'Phẫu thuật tạo hình mộng mắt (Cherry eye) cho thú cưng', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'SUR-006', N'Mổ u nang ngoài da',       700000, N'Phẫu thuật',         40,   N'Cắt bỏ khối u ngoài da và khâu thẩm mỹ', N'Đang cung cấp', N'Tất cả thú cưng'),

-- Spa & Grooming
(N'DV003', N'Tắm và cắt tỉa lông',        350000, N'Spa & Grooming',     90,   N'Gói spa toàn diện: tắm sấy, chải lông rụng, cắt móng, vệ sinh tai và tạo kiểu lông', N'Đang cung cấp', N'Chó & Mèo'),
(N'DV006', N'Cắt móng & vệ sinh tai',     80000,  N'Spa & Grooming',     20,   N'Dịch vụ cơ bản cắt mài móng an toàn và vệ sinh làm sạch ống tai ngoài',         N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV017', N'Nhuộm lông tai & đuôi tạo kiểu', 180000, N'Spa & Grooming', 45,  N'Nhuộm màu thảo mộc tự nhiên an toàn tuyệt đối cho thú cưng', N'Đang cung cấp', N'Chó & Mèo'),
(N'DV018', N'Gỡ rối & phục hồi lông hư tổn', 250000, N'Spa & Grooming',  60,  N'Ủ dầu dưỡng collagen phục hồi lông xơ rối mềm mượt', N'Đang cung cấp', N'Chó & Mèo'),
(N'SP01', N'Chải lông cơ bản',            30000,  N'Spa & Grooming',     20,   N'Chải lông gỡ rối và massage', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'SP02', N'Vệ sinh mắt tai',             25000,  N'Spa & Grooming',     15,   N'Lau rửa gỉ mắt và khử khuẩn vành tai', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'SP03', N'Tắm sấy lưu trú',             120000, N'Spa & Grooming',     60,   N'Tắm thơm sấy khô khử mùi', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'SP04', N'Xịt thơm dưỡng lông',         20000,  N'Spa & Grooming',     10,   N'Xịt tinh dầu dưỡng mượt lông', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'SP05', N'Cắt mài móng an toàn',        35000,  N'Spa & Grooming',     15,   N'Cắt gọn móng tránh cào xước', N'Đang cung cấp', N'Tất cả thú cưng'),

-- Lưu trú
(N'DV005', N'Lưu trú thú cưng (theo đêm)',180000, N'Lưu trú',           1440, N'Dịch vụ khách sạn thú cưng tiện nghi, phòng máy lạnh, camera theo dõi 24/7',    N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV019', N'Khách sạn phòng VIP máy lạnh & camera', 280000, N'Lưu trú', 1440, N'Phòng riêng cao cấp trang bị camera 24/7 xem trực tiếp trên điện thoại', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV020', N'Lưu trú khu cách ly chăm sóc đặc biệt', 220000, N'Lưu trú', 1440, N'Khu lưu trú vô trùng dành riêng cho thú cưng đang điều trị hồi phục', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'TA01', N'Hạt khô cao cấp (bữa chính)', 35000,  N'Lưu trú',            15,   N'Hạt khô dinh dưỡng nhập khẩu theo khẩu phần', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'TA02', N'Pate dinh dưỡng',             25000,  N'Lưu trú',            15,   N'Pate thơm ngon bổ sung nước và đạm', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'TA03', N'Nước hầm xương củ quả',       30000,  N'Lưu trú',            15,   N'Nước hầm thanh đạm tốt cho tiêu hóa', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'TA04', N'Ức gà xé sợi luộc',           35000,  N'Lưu trú',            15,   N'Ức gà tươi luộc chín xé nhỏ', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'TA05', N'Cá hồi hấp bí đỏ',            45000,  N'Lưu trú',            15,   N'Cá hồi tươi hấp mềm mượt lông', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'TA06', N'Súp dinh dưỡng bổ sung',      20000,  N'Lưu trú',            10,   N'Súp lỏng bổ sung vitamin cho bé', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'TA07', N'Thảo mộc tiêu hóa',           25000,  N'Lưu trú',            10,   N'Trà thảo mộc hỗ trợ đường ruột', N'Đang cung cấp', N'Tất cả thú cưng'),

-- Xét nghiệm & Chẩn đoán
(N'DV007', N'Xét nghiệm máu tổng quát',   450000, N'Xét nghiệm & Chẩn đoán', 30, N'Tổng phân tích tế bào máu 18 thông số và sinh hóa máu cơ bản', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV008', N'Siêu âm ổ bụng',             300000, N'Xét nghiệm & Chẩn đoán', 25, N'Siêu âm khảo sát các cơ quan nội tạng ổ bụng bằng máy siêu âm màu 4D', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV012', N'Chụp X-quang kỹ thuật số',   250000, N'Xét nghiệm & Chẩn đoán', 20, N'Chụp X-quang tim phổi, cột sống và xương khớp đa tư thế', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV013', N'Test nhanh Parvovirus & Care', 180000, N'Xét nghiệm & Chẩn đoán', 15, N'Bộ kit test nhanh kháng nguyên CPV/CDV độ chính xác cao', N'Đang cung cấp', N'Chó'),
(N'DV014', N'Tổng phân tích nước tiểu & cặn', 120000, N'Xét nghiệm & Chẩn đoán', 20, N'Phân tích 10 thông số nước tiểu và soi cặn tìm tinh thể sỏi', N'Đang cung cấp', N'Tất cả thú cưng'),

-- Nha khoa
(N'DV004', N'Lấy cao răng siêu âm',       400000, N'Nha khoa',           45,   N'Làm sạch mảng bám răng bằng máy siêu âm hiện đại không đau', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV015', N'Nhổ răng sữa tồn dư & răng lung lay', 150000, N'Nha khoa', 25,   N'Nhổ răng sữa mọc kẹt, răng lung lay gây viêm nướu', N'Đang cung cấp', N'Tất cả thú cưng'),
(N'DV016', N'Đánh bóng men răng & khử mùi hôi miệng', 200000, N'Nha khoa', 30, N'Đánh bóng men răng bằng bột chuyên dụng và gel kháng khuẩn', N'Đang cung cấp', N'Tất cả thú cưng');
GO

-- 8. SANPHAM (20 SKU đa dạng 6 danh mục với quy cách, đơn vị tính chuẩn)
INSERT INTO SANPHAM (sku_maSP, tenSP, quyCach, danhMuc, donViTinh, donGia, soLuongTon, hanSuDung, maNV_Kho) VALUES
-- Thức ăn (FOOD)
(N'FOOD001', N'Thức ăn hạt Royal Canin Mini Adult 2kg', N'Túi 2kg', N'Thức ăn', N'bao', 350000.0, 45, '2026-12-31', N'NV003'),
(N'FOOD002', N'Pate Whiskas cá thu & cá hồi 85g', N'Gói 85g', N'Thức ăn', N'gói', 25000.0, 180, '2026-12-31', N'NV003'),
(N'FOOD003', N'Xương gặm sạch răng Pedigree DentaStix 110g', N'Gói 110g', N'Thức ăn', N'gói', 48000.0, 110, '2026-11-10', N'NV003'),
(N'FOOD004', N'Bánh thưởng vị thịt gà SmartHeart 100g', N'Gói 100g', N'Thức ăn', N'gói', 38000.0, 4, '2026-11-20', N'NV003'),
(N'FOOD005', N'Hạt Me-O vị cá ngừ cho mèo lớn 1.2kg', N'Túi 1.2kg', N'Thức ăn', N'bao', 110000.0, 65, '2026-10-30', N'NV003'),
-- Đồ chơi (TOY)
(N'TOY001', N'Cần câu mèo lông vũ gắn chuông', N'Cây 50cm', N'Đồ chơi', N'cái', 35000.0, 60, '2028-01-01', N'NV003'),
(N'TOY002', N'Bóng cao su đồ chơi gặm sạch răng cho chó', N'Quả đk 7cm', N'Đồ chơi', N'cái', 55000.0, 42, '2028-01-01', N'NV003'),
(N'TOY003', N'Cỏ tươi Catnip sấy khô cho mèo', N'Hũ 45g', N'Đồ chơi', N'hũ', 45000.0, 85, '2027-04-15', N'NV003'),
-- Thuốc (MED)
(N'MED001', N'Dung dịch nhỏ mắt Bioline 50ml', N'Lọ 50ml', N'Thuốc', N'chai', 65000.0, 2, '2026-08-05', N'NV007'),
(N'MED002', N'Gel dinh dưỡng Nourse 120g', N'Tuýp 120g', N'Thuốc', N'tuýp', 180000.0, 38, '2026-10-25', N'NV007'),
(N'MED003', N'Thuốc tẩy giun Sanpet cho chó mèo', N'Vỉ 10 viên', N'Thuốc', N'viên', 15000.0, 90, '2027-06-30', N'NV007'),
(N'MED004', N'Bột điện giải và men vi sinh Biotic 5g', N'Gói 5g', N'Thuốc', N'gói', 8000.0, 200, '2027-05-10', N'NV007'),
-- Chăm sóc và vệ sinh (CARE)
(N'CARE001', N'Cát vệ sinh Cature đậu nành tự nhiên 6L', N'Túi 6L', N'Chăm sóc và vệ sinh', N'túi', 145000.0, 75, '2027-01-01', N'NV003'),
(N'CARE002', N'Sữa tắm trị ve rận Bio Derma 250ml', N'Chai 250ml', N'Chăm sóc và vệ sinh', N'chai', 95000.0, 3, '2026-09-20', N'NV007'),
(N'CARE003', N'Xịt khử mùi chuồng trại & khay cát Forcans 300ml', N'Chai 300ml', N'Chăm sóc và vệ sinh', N'chai', 125000.0, 32, '2026-12-15', N'NV003'),
-- Phụ kiện (ACC)
(N'ACC001', N'Vòng cổ trị ve rận Seresto cho chó', N'Hộp 1 cái', N'Phụ kiện', N'cái', 650000.0, 25, '2027-03-15', N'NV007'),
(N'ACC002', N'Dây dắt kèm yếm ngực phản quang cho chó', N'Bộ dây + yếm', N'Phụ kiện', N'bộ', 135000.0, 28, '2028-01-01', N'NV003'),
-- Đồ dùng và sinh hoạt (LIV)
(N'LIV001', N'Bát ăn chống trượt inox 2 ngăn cho thú cưng', N'Cái 2 ngăn', N'Đồ dùng và sinh hoạt', N'cái', 85000.0, 50, '2028-01-01', N'NV003'),
(N'LIV002', N'Lược chải lông tự làm sạch Pet Brush', N'Cây cầm tay', N'Đồ dùng và sinh hoạt', N'cái', 79000.0, 4, '2028-01-01', N'NV003'),
(N'LIV003', N'Tã bỉm thú cưng Dono size M (12 miếng)', N'Bịch 12 miếng', N'Đồ dùng và sinh hoạt', N'gói', 98000.0, 30, '2027-08-20', N'NV003');
GO
-- 9. CHUONGLUUTRU (20 lồng chuồng phân chia 5 khu vực, ràng buộc loài và cân nặng)
-- Kích thước ↔ cân nặng tối đa: Nhỏ ≤5kg | Vừa 5-15kg | Lớn 15-30kg | VIP Suite ≤50kg
INSERT INTO CHUONGLUUTRU (cageID, size, trangThai, giaTheoDem, khuVuc, canNangToiDa, tenChuong, maNV_QL) VALUES
-- Khu Mèo (chỉ nhận mèo)
(N'M-101',  N'Nhỏ (≤ 5kg)',         N'Đang lưu trú', 120000, N'Khu Mèo',        5.0,  N'Lồng M-101',  N'NV001'),
(N'M-102',  N'Nhỏ (≤ 5kg)',         N'Trống',        120000, N'Khu Mèo',        5.0,  N'Lồng M-102',  N'NV001'),
(N'M-103',  N'Vừa (5-15kg)',        N'Đã đặt',       160000, N'Khu Mèo',        15.0, N'Lồng M-103',  N'NV001'),
(N'M-104',  N'Vừa (5-15kg)',        N'Trống',        160000, N'Khu Mèo',        15.0, N'Lồng M-104',  N'NV001'),
-- Khu Chó Nhỏ (chó ≤ 15kg)
(N'CN-101', N'Nhỏ (≤ 5kg)',         N'Đang lưu trú', 140000, N'Khu Chó Nhỏ',    5.0,  N'Lồng CN-101', N'NV001'),
(N'CN-102', N'Nhỏ (≤ 5kg)',         N'Trống',        140000, N'Khu Chó Nhỏ',    5.0,  N'Lồng CN-102', N'NV001'),
(N'CN-103', N'Vừa (5-15kg)',        N'Đang lưu trú', 180000, N'Khu Chó Nhỏ',    15.0, N'Lồng CN-103', N'NV001'),
(N'CN-104', N'Vừa (5-15kg)',        N'Đã đặt',       180000, N'Khu Chó Nhỏ',    15.0, N'Lồng CN-104', N'NV001'),
-- Khu Chó Lớn (chó 15-30kg)
(N'CL-101', N'Lớn (15-30kg)',       N'Đang lưu trú', 250000, N'Khu Chó Lớn',    30.0, N'Lồng CL-101', N'NV001'),
(N'CL-102', N'Lớn (15-30kg)',       N'Trống',        250000, N'Khu Chó Lớn',    30.0, N'Lồng CL-102', N'NV001'),
(N'CL-103', N'Lớn (15-30kg)',       N'Đã đặt',       250000, N'Khu Chó Lớn',    30.0, N'Lồng CL-103', N'NV001'),
(N'CL-104', N'Lớn (15-30kg)',       N'Bảo trì',      250000, N'Khu Chó Lớn',    30.0, N'Lồng CL-104', N'NV001'),
-- Khu VIP Suite (chó/mèo lớn hoặc khách hàng VIP, ≤ 50kg)
(N'VIP-01', N'VIP Suite (≤ 50kg)',  N'Đang lưu trú', 350000, N'Khu VIP',        50.0, N'Lồng VIP-01', N'NV001'),
(N'VIP-02', N'VIP Suite (≤ 50kg)',  N'Trống',        350000, N'Khu VIP',        50.0, N'Lồng VIP-02', N'NV001'),
(N'VIP-03', N'VIP Suite (≤ 50kg)',  N'Đã đặt',       350000, N'Khu VIP',        50.0, N'Lồng VIP-03', N'NV001'),
(N'VIP-04', N'VIP Suite (≤ 50kg)',  N'Bảo trì',      350000, N'Khu VIP',        50.0, N'Lồng VIP-04', N'NV001'),
-- Khu Loài Khác (Thỏ, Hamster, Chim, Bọ...)
(N'LK-101', N'Nhỏ (≤ 5kg)',         N'Đang lưu trú', 100000, N'Khu Loài Khác',  5.0,  N'Lồng LK-101', N'NV001'),
(N'LK-102', N'Nhỏ (≤ 5kg)',         N'Trống',        100000, N'Khu Loài Khác',  5.0,  N'Lồng LK-102', N'NV001'),
(N'LK-103', N'Vừa (5-15kg)',        N'Trống',        140000, N'Khu Loài Khác',  15.0, N'Lồng LK-103', N'NV001'),
(N'LK-104', N'Vừa (5-15kg)',        N'Bảo trì',      140000, N'Khu Loài Khác',  15.0, N'Lồng LK-104', N'NV001');
GO

-- 9.1 PHIEU_LUUTRU (6 đang lưu trú + 4 đã đặt = 10 phiếu, đồng bộ live DB)
INSERT INTO PHIEU_LUUTRU (maPhieu, cageID, maKH, petID, tenPet, loaiPet, tenKhach, sdtKhach, ngayVao, ngayRaDuKien, tienCoc, tongTien, trangThai, ghiChu, maNV_QL) VALUES
-- Đang lưu trú (6 phiếu)
(N'PLT001', N'M-101',  N'KH001', N'PET001', N'Kitty',    N'Mèo', N'Nguyễn Minh Anh', '0912345678', GETDATE(), DATEADD(day,3,GETDATE()), 100000, 360000,  N'Đang lưu trú', N'Ăn pate ngày 2 cữ, chải lông', NULL),
(N'PLT002', N'CN-101', N'KH006', N'PET006', N'Bông Gòn', N'Chó', N'Bùi Anh Khoa',    '0901234567', GETDATE(), DATEADD(day,2,GETDATE()), 100000, 280000,  N'Đang lưu trú', N'Chó ngoan, dắt đi dạo chiều', NULL),
(N'PLT003', N'CN-103', N'KH005', N'PET007', N'Buddy',    N'Chó', N'Hoàng Minh Tuấn', '0956789012', GETDATE(), DATEADD(day,4,GETDATE()), 200000, 720000,  N'Đang lưu trú', N'Uống thuốc bổ khớp mỗi sáng', NULL),
(N'PLT004', N'CL-101', N'KH004', N'PET004', N'Rocky',    N'Chó', N'Võ Hoàng Long',   '0934567890', GETDATE(), DATEADD(day,3,GETDATE()), 200000, 750000,  N'Đang lưu trú', N'Chó lớn, cho ăn hạt Royal Canin', NULL),
(N'PLT005', N'VIP-01', N'KH005', N'PET005', N'Mimi',     N'Mèo', N'Hoàng Minh Tuấn', '0956789012', GETDATE(), DATEADD(day,5,GETDATE()), 500000, 1750000, N'Đang lưu trú', N'Phòng VIP điều hòa 24/7, camera riêng', NULL),
(N'PLT006', N'LK-101', N'KH009', N'PET010', N'Bánh Bao', N'Thỏ', N'Võ Phát Huy',     '0989012345', GETDATE(), DATEADD(day,3,GETDATE()), 100000, 300000,  N'Đang lưu trú', N'Thỏ ăn cỏ khô Alfalfa và củ tươi', NULL),
-- Đã đặt trước (4 phiếu)
(N'PDC001', N'M-103',  N'KH003', N'PET003', N'Mochi',    N'Mèo', N'Phạm Gia Hân',    '0945678901', DATEADD(day,1,GETDATE()), DATEADD(day,3,GETDATE()), 100000, 320000,  N'Đã đặt', N'Hẹn gửi bé chiều mai', NULL),
(N'PDC002', N'CN-104', N'KH008', N'PET009', N'Corgi Mông',N'Chó',N'Hoàng Thị Mai',   '0978901234', DATEADD(day,1,GETDATE()), DATEADD(day,4,GETDATE()), 150000, 540000,  N'Đã đặt', N'Gửi bé dịp cuối tuần', NULL),
(N'PDC003', N'CL-103', N'KH002', N'PET002', N'Milu',     N'Chó', N'Lê Thị Cẩm Tú',   '0923456789', DATEADD(day,2,GETDATE()), DATEADD(day,5,GETDATE()), 200000, 750000,  N'Đã đặt', N'Theo dõi hậu điều trị', NULL),
(N'PDC004', N'VIP-03', N'KH001', N'PET011', N'Đậu Phộng', N'Chó',N'Nguyễn Minh Anh', '0912345678', DATEADD(day,2,GETDATE()), DATEADD(day,6,GETDATE()), 300000, 1400000, N'Đã đặt', N'Đặt trước phòng VIP', NULL);
GO

-- 9.2 CHITIET_DICHVU_CHUONG (dịch vụ chăm sóc cho từng phòng, đồng bộ live DB)
INSERT INTO CHITIET_DICHVU_CHUONG (maPhieu, maDV, tenDV, loai, thoiGian, ngaySuDung, soLuong, donGia) VALUES
(N'PLT001', N'TA01', N'Pate cá ngừ Whiskas lon 400g',             N'Thức ăn & Dinh dưỡng', GETDATE(), CAST(GETDATE() AS DATE), 2, 35000),
(N'PLT001', N'SP02', N'Cắt móng, mài móng & vệ sinh tai mắt',    N'Spa & Grooming',        GETDATE(), CAST(GETDATE() AS DATE), 1, 50000),
(N'PLT003', N'TA03', N'Hạt Royal Canin Mini Adult (phần ngày)',   N'Thức ăn & Dinh dưỡng', GETDATE(), CAST(GETDATE() AS DATE), 3, 40000),
(N'PLT003', N'YT03', N'Cho uống thuốc theo toa bác sĩ dặn',      N'Y tế & Chăm sóc',       GETDATE(), CAST(GETDATE() AS DATE), 2, 20000),
(N'PLT005', N'SP03', N'Combo Spa toàn diện + cắt tỉa tạo kiểu',  N'Spa & Grooming',        GETDATE(), CAST(GETDATE() AS DATE), 1, 220000),
(N'PLT005', N'TA07', N'Thịt ức gà luộc xé sợi dinh dưỡng',       N'Thức ăn & Dinh dưỡng', GETDATE(), CAST(GETDATE() AS DATE), 3, 35000),
(N'PLT002', N'TA01', N'Pate cá ngừ Whiskas lon 400g',             N'Thức ăn & Dinh dưỡng', GETDATE(), CAST(GETDATE() AS DATE), 1, 35000);
GO

-- 10. DANHMUCBENH
INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL) VALUES
(N'B001', N'Bệnh Care ở chó (Canine Distemper)', N'Sốt cao, chảy nước mũi, ho, co giật', N'NV001'),
(N'B002', N'Bệnh Parvo ở chó (Canine Parvovirus)', N'Nôn mửa dữ dội, tiêu chảy ra máu mùi tanh', N'NV001'),
(N'B003', N'Bệnh Giảm bạch cầu ở mèo (FPV)', N'Ủ rũ, bỏ ăn, sốt cao, nôn dịch vàng', N'NV001'),
(N'B004', N'Viêm da do nấm (Dermatophytosis)', N'Rụng lông từng mảng tròn, da đóng vảy ngứa', N'NV001');
GO

-- 11. MAGIAMGIA
INSERT INTO MAGIAMGIA (maGiamGia, phanTram, giaTri, hanDung, trangThai) VALUES
(N'GIAM10', 1, 10,     '2027-12-31', N'Hoạt động'),
(N'PET100K',0, 100000, '2027-12-31', N'Hoạt động'),
(N'PET50K', 0, 50000,  '2027-12-31', N'Hoạt động'),
(N'VIP20',  1, 20,     '2027-12-31', N'Hoạt động');
GO

-- 12. DOITAC
INSERT INTO DOITAC (maDT, tenDT, loaiDT, soDienThoai, diaChi, email, trangThai) VALUES
(N'DT001', N'Công Ty CP Royal Canin VN',       N'Nhà cung cấp thức ăn',   '02838123456', N'KCN Tân Bình, TP.HCM',      'contact@royalcanin.vn', N'Đang hợp tác'),
(N'DT002', N'Công Ty TNHH Zoetis Việt Nam',    N'Nhà cung cấp dược thú y', '02839988776', N'Quận 1, TP.HCM',            'info@zoetis.vn',        N'Đang hợp tác'),
(N'DT003', N'Công Ty TNHH Virbac Việt Nam',    N'Nhà cung cấp vaccine',   '02743789123', N'KCN Sóng Thần, Bình Dương', 'support@virbac.vn',     N'Đang hợp tác'),
(N'DT004', N'Công Ty CP Guyomarc H VN',        N'Nhà cung cấp thức ăn',   '02836223344', N'Quận 7, TP.HCM',            'info@guyomarch.vn',     N'Đang hợp tác'),
(N'DT005', N'Chi nhánh PetCare Quận 7',        N'Chi nhánh nội bộ',       '02838765432', N'Quận 7, TP.HCM',            'q7@petcare.vn',         N'Đang hợp tác'),
(N'DT006', N'Chi nhánh PetCare Thủ Đức',       N'Chi nhánh nội bộ',       '02839112233', N'TP. Thủ Đức, TP.HCM',        'thuduc@petcare.vn',     N'Đang hợp tác'),
(N'DT007', N'Phòng khám thú y Happy Paws',     N'Đối tác phân phối',      '0908123456',  N'Quận Bình Thạnh, TP.HCM',   'happypaws@gmail.com',   N'Đang hợp tác'),
(N'DT008', N'Phòng khám thú y An Việt',        N'Đối tác phân phối',      '0912345678',  N'Quận 10, TP.HCM',           'anviet@gmail.com',      N'Đang hợp tác'),
(N'DT009', N'Thuốc thú y Minh Phương',         N'Nhà cung cấp dược thú y', '0934567890',  N'Quận 5, TP.HCM',            'minhphuong@gmail.com',  N'Đang hợp tác'),
(N'DT010', N'Công Ty Green Pet',              N'Nhà cung cấp thức ăn',   '02838991234', N'Bình Dương',                'greenpet@gmail.com',    N'Đang hợp tác');
GO

-- 13. PHIEUKHO (5 nhập mới + 6 xuất; đồng bộ live DB với giá trị thực tế)
INSERT INTO PHIEUKHO (maPhieu, loaiPhieu, ngayLap, gioLap, nguoiThucHien, doiTac, tongGiaTri, trangThai, ghiChu, maNV, maDT) VALUES
(N'PN-2026-0041', N'Nhập mới', '2026-03-01', '08:30', N'Lê Tuấn Anh',    N'Công Ty CP Royal Canin VN',       15240000, N'Hoàn tất', NULL, N'NV003', N'DT001'),
(N'PN-2026-0042', N'Nhập mới', '2026-03-02', '09:15', N'Đặng Thị Mai',   N'Thuốc thú y Minh Phương',          6820000, N'Hoàn tất', NULL, N'NV007', N'DT009'),
(N'PN-2026-0043', N'Nhập mới', '2026-03-02', '10:00', N'Lê Tuấn Anh',    N'Công Ty CP Royal Canin VN',       21000000, N'Hoàn tất', NULL, N'NV003', N'DT001'),
(N'PN-2026-0044', N'Nhập mới', '2026-03-03', '08:00', N'Đặng Thị Mai',   N'Công Ty TNHH Zoetis Việt Nam',     9540000, N'Hoàn tất', NULL, N'NV007', N'DT002'),
(N'PN-2026-0045', N'Nhập mới', '2026-03-04', '09:45', N'Lê Tuấn Anh',    N'Công Ty Green Pet',               12800000, N'Hoàn tất', NULL, N'NV003', N'DT010'),
(N'PX-2026-0039', N'Xuất bán', '2026-03-01', '11:20', N'Hà Trọng Trung', N'Phòng khám thú y Happy Paws',     19800000, N'Hoàn tất', NULL, N'NV002', N'DT007'),
(N'PX-2026-0040', N'Xuất hủy', '2026-03-02', '15:45', N'Võ Thanh Hùng',  N'Chi nhánh PetCare Thủ Đức',        4750000, N'Đã hủy',   NULL, N'NV006', N'DT006'),
(N'PX-2026-0041', N'Xuất bán', '2026-03-02', '14:10', N'Nguyễn Hoài Anh',N'Phòng khám thú y An Việt',        21500000, N'Hoàn tất', NULL, N'NV004', N'DT008'),
(N'PX-2026-0042', N'Xuất bán', '2026-03-03', '14:10', N'Hà Trọng Trung', N'Chi nhánh PetCare Quận 7',        24350000, N'Hoàn tất', NULL, N'NV002', N'DT005'),
(N'PX-2026-0043', N'Xuất bán', '2026-03-04', '09:30', N'Hà Trọng Trung', N'Khách lẻ & Cửa hàng',             22800000, N'Hoàn tất', NULL, N'NV002', N'DT005'),
(N'PX-2026-0044', N'Xuất bán', '2026-03-04', '16:00', N'Hà Trọng Trung', N'Khách lẻ & Cửa hàng',              6450000, N'Hoàn tất', NULL, N'NV002', N'DT005');
GO

-- 14. LICHHEN
INSERT INTO LICHHEN (maLH, ngayHen, khungGio, trangThai, petID, maNV_BS, maDV, cageID, tenKH, soDienThoai, tenPet, tenDV, tenBacSi, gioHen, ghiChu, nhatKy, maKH) VALUES
(N'LH001', '2026-10-20', '08:00 - 09:00', N'Đã xác nhận', N'PET001', N'NV004', N'DV001', NULL,    N'Nguyễn Minh Anh', '0903123456', N'Kitty', N'Khám tổng quát',             N'BS. Nguyễn Hoài Anh', '08:00', NULL, NULL, N'KH001'),
(N'LH002', '2026-10-20', '09:00 - 10:00', N'Đã xác nhận', N'PET004', N'NV006', N'DV002', NULL,    N'Võ Hoàng Long',   '0386909808', N'Rocky', N'Tiêm phòng vaccine',         N'BS. Võ Thanh Hùng',   '09:00', NULL, NULL, N'KH004'),
(N'LH003', '2026-10-21', '10:00 - 11:00', N'Đã xác nhận', N'PET002', N'NV006', N'DV003', NULL,    N'Lê Thị Cẩm Tú',   '0938222111', N'Milu',  N'Tắm và cắt tỉa lông',        N'BS. Võ Thanh Hùng',   '10:00', NULL, NULL, N'KH002'),
(N'LH004', '2026-10-22', '14:00 - 15:00', N'Chờ xác nhận',N'PET003', N'NV006', N'DV007', NULL,    N'Lê Thị Cẩm Tú',   '0938222111', N'Mochi', N'Xét nghiệm máu tổng quát',   N'BS. Võ Thanh Hùng',   '14:00', NULL, NULL, N'KH002'),
(N'LH005', '2026-10-18', '08:00 - 09:00', N'Hoàn thành',  N'PET005', N'NV004', N'DV001', NULL,    N'Võ Hoàng Long',   '0386909808', N'Mimi',  N'Khám tổng quát',             N'BS. Nguyễn Hoài Anh', '08:00', NULL, NULL, N'KH004'),
(N'LH006', '2026-10-25', '15:00 - 16:00', N'Chờ xác nhận',N'PET007', N'NV006', N'DV005', N'CN-103',N'Hoàng Minh Tuấn', '0945678901', N'Buddy', N'Lưu trú thú cưng (theo dõi)',N'BS. Võ Thanh Hùng',   '15:00', NULL, NULL, N'KH005');
GO

-- 15. PHAUTHUAT (5 ca, đồng bộ live DB)
INSERT INTO PHAUTHUAT (maCa, tenThuCung, loaiThuCung, tenChu, sdt, loaiPhauThuat, ngayMo, gioMo, bacSiChinh, chiPhi, trangThai, ghiChu, petID, maKH) VALUES
(N'PT-001', N'Milu',    N'Chó Golden Retriever', N'Lê Thị Cẩm Tú', '0938222111', N'Phẫu thuật lấy sỏi bàng quang', '2026-10-15', '08:00', N'BS. Võ Thanh Hùng',   1500000, N'Đang phẫu thuật',  N'Gây mê ổn định',   N'PET002', N'KH002'),
(N'PT-002', N'Mimi',    N'Mèo Xiêm (Siamese)',   N'Võ Hoàng Long', '0386909808', N'Triệt sản mèo cái nội soi',      '2026-10-18', '09:00', N'BS. Ngô Gia Bảo',      650000,  N'Đang phẫu thuật',  N'Đã nhịn ăn 8h',    N'PET005', N'KH004'),
(N'PT-003', N'Rocky',   N'Chó Rottweiler',       N'Võ Hoàng Long', '0386909808', N'Phẫu thuật kết hợp xương gãy',  '2026-10-20', '10:00', N'BS. Võ Thanh Hùng',   2500000, N'Hoàn thành',       N'Cố định nẹp xương',N'PET004', N'KH004'),
(N'PT-004', N'Bằng Xù', N'Mèo Ragdoll',          N'Bùi Anh Khoa',  '0919888000', N'Triệt sản mèo đực',             '2026-10-22', '08:30', N'BS. Nguyễn Hoài Anh',  350000,  N'Hoàn thành',       N'Vết mổ khô ráo',   N'PET012', N'KH006'),
(N'PT-005', N'Buddy',   N'Chó Poodle Toy',       NULL,             NULL,          N'Cắt mộng mắt',                  '2026-11-02', '08:30', N'BS. Nguyễn Hoài Anh', 1500000, N'Chờ phẫu thuật',   NULL,                NULL,      NULL);
GO

-- 16. TIEMPHONG
INSERT INTO TIEMPHONG (maPhieu, tenThuCung, loaiThuCung, tenChu, sdt, loaiVaccine, ngayTiem, ngayHenNhac, bacSi, trangThai, ghiChu, petID, maKH) VALUES
(N'TP-001', N'Kitty',    N'Mèo Anh lông ngắn',    N'Nguyễn Minh Anh', '0903123456', N'Tiêm vaccine 4 bệnh mèo (Nobivac Ducat)',       '2026-10-01', '2027-10-01', N'BS. Nguyễn Hoài Anh', N'Đã tiêm',  N'Đã cấp sổ tiêm chủng', N'PET001', N'KH001'),
(N'TP-002', N'Milu',     N'Chó Golden Retriever', N'Lê Thị Cẩm Tú',   '0938222111', N'Tiêm vaccine phòng Dại (Rabies)',               '2026-10-05', '2027-10-05', N'BS. Võ Thanh Hùng',   N'Đã tiêm',  N'Bình thường',           N'PET002', N'KH002'),
(N'TP-003', N'Rocky',    N'Chó Rottweiler',       N'Võ Hoàng Long',   '0386909808', N'Tiêm vaccine 7 bệnh chó (Vanguard Plus 5/L4)', '2026-10-20', '2027-10-20', N'BS. Ngô Gia Bảo',      N'Chờ tiêm', N'Hẹn buổi chiều',        N'PET004', N'KH004'),
(N'TP-004', N'Bông Gòn', N'Chó Samoyed',          N'Bùi Anh Khoa',    '0919888000', N'Tiêm vaccine 5 bệnh chó',                       '2026-09-15', '2026-10-15', N'BS. Nguyễn Hoài Anh', N'Quá hạn',  N'Cần gọi nhắc lịch',     N'PET006', N'KH006');
GO

-- 17. HÓA ĐƠN & CHI TIẾT HÓA ĐƠN DOANH THU THỰC TẾ
INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, soO, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai)
VALUES 
('HD20260301001', '2026-03-01', 14200000, 'NV002', 'KH006', 1, N'Đã thanh toán', N'Chuyển khoản', '2026-03-01 09:15:00', '2026-03-01 09:16:00', N'Bùi Anh Khoa', '0919888000'),
('HD20260301002', '2026-03-01', 12500000, 'NV002', 'KH001', 2, N'Đã thanh toán', N'Tiền mặt', '2026-03-01 14:30:00', '2026-03-01 14:31:00', N'Nguyễn Minh Anh', '0903123456'),
('HD20260302001', '2026-03-02', 8900000,  'NV002', 'KH004', 3, N'Đã thanh toán', N'Chuyển khoản', '2026-03-02 10:20:00', '2026-03-02 10:21:00', N'Võ Hoàng Long', '0386909808'),
('HD20260302002', '2026-03-02', 6800000,  'NV002', 'KH002', 4, N'Đã thanh toán', N'Thẻ', '2026-03-02 16:00:00', '2026-03-02 16:02:00', N'Lê Thị Cẩm Tú', '0938222111'),
('HD20260302003', '2026-03-02', 18500000, 'NV002', 'KH006', NULL, N'Đã thanh toán', N'Chuyển khoản', '2026-03-02 14:15:00', '2026-03-02 14:20:00', N'Bùi Anh Khoa', '0919888000'),
('HD20260303001', '2026-03-03', 4100000,  'NV002', 'KH007', 5, N'Đã thanh toán', N'Tiền mặt', '2026-03-03 11:10:00', '2026-03-03 11:11:00', N'Đặng Thu Hà', '0905777333'),
('HD20260303002', '2026-03-03', 3200000,  'NV002', 'KH003', 6, N'Đã thanh toán', N'Chuyển khoản', '2026-03-03 15:45:00', '2026-03-03 15:46:00', N'Phạm Gia Hân', '0977654321'),
('HD20260303003', '2026-03-03', 7800000,  'NV002', 'KH001', NULL, N'Đã thanh toán', N'Tiền mặt', '2026-03-03 16:30:00', '2026-03-03 16:35:00', N'Nguyễn Minh Anh', '0903123456'),
('HD20260304001', '2026-03-04', 1500000,  'NV002', 'KH005', 7, N'Đã thanh toán', N'Tiền mặt', '2026-03-04 10:00:00', '2026-03-04 10:01:00', N'Hoàng Minh Tuấn', '0945678901'),
('HD20260304002', '2026-03-04', 950000,   'NV002', 'KH008', 8, N'Đã thanh toán', N'Tiền mặt', '2026-03-04 14:15:00', '2026-03-04 14:16:00', N'Hoàng Thị Mai', '0932111222'),
('HD20260304003', '2026-03-04', 10200000, 'NV002', 'KH004', NULL, N'Đã thanh toán', N'Chuyển khoản', '2026-03-04 11:00:00', '2026-03-04 11:05:00', N'Võ Hoàng Long', '0386909808'),
('HD0001',        '2026-11-02', 150000,   'NV002', 'KH009', NULL, N'Đã thanh toán', N'MoMo', '2026-11-02 08:30:00', '2026-11-02 08:35:00', N'Võ Phát Huy', '0917099576'),
('HD0002',        '2026-11-02', 320000,   'NV002', 'KH009', NULL, N'Đã thanh toán', N'ZaloPay', '2026-11-02 10:00:00', '2026-11-02 10:05:00', N'Võ Phát Huy', '0917099576'),
-- Dữ liệu thống kê phân bổ các tháng trong năm 2026
('HD20260115001', '2026-01-15', 4500000,  'NV002', 'KH001', NULL, N'Đã thanh toán', N'Tiền mặt',     '2026-01-15 10:15:00', '2026-01-15 10:20:00', N'Nguyễn Minh Anh', '0903123456'),
('HD20260128001', '2026-01-28', 7800000,  'NV002', 'KH002', NULL, N'Đã thanh toán', N'Chuyển khoản', '2026-01-28 14:30:00', '2026-01-28 14:35:00', N'Lê Thị Cẩm Tú',   '0938222111'),
('HD20260212001', '2026-02-12', 12300000, 'NV002', 'KH003', NULL, N'Đã thanh toán', N'Chuyển khoản', '2026-02-12 09:40:00', '2026-02-12 09:45:00', N'Phạm Gia Hân',    '0977654321'),
('HD20260224001', '2026-02-24', 6400000,  'NV002', 'KH004', NULL, N'Đã thanh toán', N'Tiền mặt',     '2026-02-24 16:10:00', '2026-02-24 16:15:00', N'Võ Hoàng Long',   '0386909808'),
('HD20260315001', '2026-03-15', 8500000,  'NV002', 'KH005', NULL, N'Đã thanh toán', N'Chuyển khoản', '2026-03-15 11:00:00', '2026-03-15 11:05:00', N'Hoàng Minh Tuấn', '0945678901'),
('HD20260322001', '2026-03-22', 11200000, 'NV002', 'KH006', NULL, N'Đã thanh toán', N'Thẻ',          '2026-03-22 15:20:00', '2026-03-22 15:25:00', N'Bùi Anh Khoa',    '0919888000'),
('HD20260410001', '2026-04-10', 9100000,  'NV002', 'KH007', NULL, N'Đã thanh toán', N'Chuyển khoản', '2026-04-10 10:30:00', '2026-04-10 10:35:00', N'Đặng Thu Hà',    '0905777333'),
('HD20260425001', '2026-04-25', 5600000,  'NV002', 'KH008', NULL, N'Đã thanh toán', N'Tiền mặt',     '2026-04-25 14:00:00', '2026-04-25 14:05:00', N'Hoàng Thị Mai',   '0932111222'),
('HD20260515001', '2026-05-15', 10400000, 'NV002', 'KH001', NULL, N'Đã thanh toán', N'Chuyển khoản', '2026-05-15 09:15:00', '2026-05-15 09:20:00', N'Nguyễn Minh Anh', '0903123456');
GO

INSERT INTO CHITIETHOADON (maCTHD, maHD, sku_maSP, sku, tenSP, danhMuc, soLuong, donGia, thanhTien, isDichVu) VALUES
('CT001', 'HD20260301001', 'FOOD001', 'FOOD001', N'Thức ăn hạt Royal Canin Mini Adult 2kg', N'Thức ăn', 20, 350000, 7000000, 0),
('CT002', 'HD20260301001', 'ACC001', 'ACC001', N'Vòng cổ trị ve rận Seresto cho chó', N'Phụ kiện', 11, 650000, 7150000, 0),
('CT003', 'HD20260301002', 'FOOD001', 'FOOD001', N'Thức ăn hạt Royal Canin Mini Adult 2kg', N'Thức ăn', 15, 350000, 5250000, 0),
('CT004', 'HD20260301002', 'ACC001', 'ACC001', N'Vòng cổ trị ve rận Seresto cho chó', N'Phụ kiện', 11, 650000, 7150000, 0),
('CT005', 'HD20260302001', 'FOOD001', 'FOOD001', N'Thức ăn hạt Royal Canin Mini Adult 2kg', N'Thức ăn', 10, 350000, 3500000, 0),
('CT006', 'HD20260302001', 'MED002', 'MED002', N'Gel dinh dưỡng Nourse 120g', N'Thuốc', 30, 180000, 5400000, 0),
('CT-20260302-01', 'HD20260302003', NULL, 'PT001', N'Phẫu thuật chỉnh hình khớp thú cưng', N'Phẫu thuật', 1, 15000000, 15000000, 1),
('CT-20260302-02', 'HD20260302003', NULL, 'LC-VIP', N'Lưu trú phòng VIP Suite 7 ngày', N'Lưu chuồng', 7, 500000, 3500000, 1),
('CT-20260302-03', 'HD20260302002', NULL, 'SPA01', N'Spa chăm sóc lông & móng toàn diện', N'Dịch vụ spa', 1, 800000, 800000, 1),
('CT-20260302-04', 'HD20260302002', NULL, 'LC-MEO', N'Lưu trú phòng Khu Mèo 6 ngày', N'Lưu chuồng', 6, 300000, 1800000, 1),
('CT-20260302-05', 'HD20260302002', 'FOOD001', 'FOOD001', N'Thức ăn hạt Royal Canin Mini Adult 2kg', N'Thức ăn', 6, 350000, 2100000, 0),
('CT-20260302-06', 'HD20260302002', 'ACC001', 'ACC001', N'Vòng cổ trị ve rận Seresto cho chó', N'Phụ kiện', 3, 650000, 1950000, 0),
('CT-20260302-07', 'HD20260302002', 'TOY001', 'TOY001', N'Cần câu mèo lông vũ gắn chuông', N'Đồ chơi', 4, 37500, 150000, 0),
('CT-20260303-05', 'HD20260303001', NULL, 'TP001', N'Tiêm phòng dại & 7 bệnh chó mèo (Mỹ)', N'Tiêm phòng', 1, 400000, 400000, 1),
('CT-20260303-06', 'HD20260303001', NULL, 'LC-VIP', N'Lưu trú phòng VIP Suite 5 ngày', N'Lưu chuồng', 5, 500000, 2500000, 1),
('CT-20260303-07', 'HD20260303001', 'CARE001', 'CARE001', N'Cát vệ sinh Cature đậu nành tự nhiên 6L', N'Chăm sóc và vệ sinh', 4, 145000, 580000, 0),
('CT-20260303-08', 'HD20260303001', 'FOOD003', 'FOOD003', N'Xương gặm sạch răng Pedigree DentaStix 110g', N'Thức ăn', 13, 48000, 620000, 0),
('CT-20260303-09', 'HD20260303002', NULL, 'KB001', N'Khám sức khỏe tổng quát định kỳ thú nhỏ', N'Dịch vụ khám', 1, 300000, 300000, 1),
('CT-20260303-10', 'HD20260303002', NULL, 'LC-LK', N'Lưu trú Khu Loài Khác (Thỏ) 7 ngày', N'Lưu chuồng', 7, 150000, 1050000, 1),
('CT-20260303-11', 'HD20260303002', 'TOY003', 'TOY003', N'Cỏ tươi Catnip sấy khô cho mèo', N'Đồ chơi', 15, 45000, 675000, 0),
('CT-20260303-12', 'HD20260303002', 'LIV001', 'LIV001', N'Bát ăn chống trượt inox 2 ngăn cho thú cưng', N'Đồ dùng và sinh hoạt', 5, 85000, 425000, 0),
('CT-20260303-13', 'HD20260303002', 'ACC002', 'ACC002', N'Dây dắt kèm yếm ngực phản quang cho chó', N'Phụ kiện', 5, 150000, 750000, 0),
('CT-20260303-01', 'HD20260303003', NULL, 'SPA01', N'Gói Spa Cắt tỉa tạo kiểu lông toàn diện', N'Dịch vụ spa', 2, 450000, 900000, 1),
('CT-20260303-02', 'HD20260303003', 'FOOD001', 'FOOD001', N'Thức ăn hạt Royal Canin Mini Adult 2kg', N'Thức ăn', 10, 350000, 3500000, 0),
('CT-20260303-03', 'HD20260303003', 'ACC001', 'ACC001', N'Vòng cổ trị ve rận Seresto cho chó', N'Phụ kiện', 5, 650000, 3250000, 0),
('CT-20260303-04', 'HD20260303003', 'TOY001', 'TOY001', N'Cần câu mèo lông vũ gắn chuông', N'Đồ chơi', 4, 35000, 140000, 0),
('CT-20260304-06', 'HD20260304001', NULL, 'KB002', N'Khám da liễu & tắm trị liệu ve rận', N'Dịch vụ khám', 1, 450000, 450000, 1),
('CT-20260304-07', 'HD20260304001', 'CARE002', 'CARE002', N'Sữa tắm trị ve rận Bio Derma 250ml', N'Chăm sóc và vệ sinh', 3, 95000, 285000, 0),
('CT-20260304-08', 'HD20260304001', 'FOOD001', 'FOOD001', N'Thức ăn hạt Royal Canin Mini Adult 2kg', N'Thức ăn', 2, 350000, 700000, 0),
('CT-20260304-09', 'HD20260304001', 'TOY002', 'TOY002', N'Bóng cao su đồ chơi gặm sạch răng cho chó', N'Đồ chơi', 1, 65000, 65000, 0),
('CT-20260304-10', 'HD20260304002', 'CARE001', 'CARE001', N'Cát vệ sinh Cature đậu nành tự nhiên 6L', N'Chăm sóc và vệ sinh', 3, 145000, 435000, 0),
('CT-20260304-11', 'HD20260304002', 'FOOD002', 'FOOD002', N'Pate Whiskas cá thu & cá hồi 85g', N'Thức ăn', 15, 25000, 375000, 0),
('CT-20260304-12', 'HD20260304002', 'TOY001', 'TOY001', N'Cần câu mèo lông vũ gắn chuông', N'Đồ chơi', 4, 35000, 140000, 0),
('CT-20260304-01', 'HD20260304003', NULL, 'TP001', N'Gói tiêm phòng 7 bệnh chó mèo (Mỹ)', N'Tiêm phòng', 4, 400000, 1600000, 1),
('CT-20260304-02', 'HD20260304003', 'MED002', 'MED002', N'Gel dinh dưỡng Nourse 120g', N'Thuốc', 20, 180000, 3600000, 0),
('CT-20260304-03', 'HD20260304003', 'CARE001', 'CARE001', N'Cát vệ sinh Cature đậu nành tự nhiên 6L', N'Chăm sóc và vệ sinh', 20, 145000, 2900000, 0),
('CT-20260304-04', 'HD20260304003', 'FOOD003', 'FOOD003', N'Xương gặm sạch răng Pedigree DentaStix 110g', N'Thức ăn', 40, 48000, 1920000, 0),
('CT-20260304-05', 'HD20260304003', 'CARE002', 'CARE002', N'Sữa tắm trị ve rận Bio Derma 250ml', N'Chăm sóc và vệ sinh', 2, 90000, 180000, 0),
-- HD0001 & HD0002: Khách mới KH009 – dịch vụ khám & nha khoa
('HD0001-CT01', 'HD0001', NULL, NULL, N'Khám tổng quát',      N'Khám & Điều trị', 1, 200000, 200000, 1),
('HD0002-CT01', 'HD0002', NULL, NULL, N'Lấy cao răng siêu âm', N'Nha khoa',       1, 400000, 400000, 1);
GO

-- ============================================================================
-- IX. CHUẨN HÓA HẠNG THẺ KHÁCH HÀNG
-- ============================================================================
UPDATE KHACHHANG
SET hangThe = CASE
    WHEN diemTichLuy >= 500 THEN N'VIP'
    WHEN diemTichLuy >= 100 THEN N'Thân thiết'
    ELSE N'Thường'
END;
GO

PRINT N'Cơ sở dữ liệu PetCare đã được khởi tạo và nạp dữ liệu mẫu hoàn tất 100%!';
GO


-- ============================================================================
-- X. ĐIỀU TRỊ & ĐƠN THUỐC (ĐỒNG BỘ TRỰC TIẾP CSDL)
-- ============================================================================
IF OBJECT_ID('CHITIET_DONTHUOC') IS NOT NULL DROP TABLE CHITIET_DONTHUOC;
IF OBJECT_ID('DONTHUOC') IS NOT NULL DROP TABLE DONTHUOC;
IF OBJECT_ID('GIAIDOAN_DIEUTRI') IS NOT NULL DROP TABLE GIAIDOAN_DIEUTRI;
IF OBJECT_ID('NHATKY_THEODOI') IS NOT NULL DROP TABLE NHATKY_THEODOI;
IF OBJECT_ID('PHACDO_DIEUTRI') IS NOT NULL DROP TABLE PHACDO_DIEUTRI;
GO

CREATE TABLE DONTHUOC (
    maDonThuoc      NVARCHAR(30) PRIMARY KEY,
    maThuCung       NVARCHAR(20) NULL,
    tenThuCung      NVARCHAR(100) NULL,
    loaiThuCung     NVARCHAR(50) NULL,
    giongLoai       NVARCHAR(100) NULL,
    canNang         FLOAT DEFAULT 0,
    tenChuNuoi      NVARCHAR(100) NULL,
    soDienThoai     NVARCHAR(20) NULL,
    bacSi           NVARCHAR(100) NULL,
    ngayKe          DATE NULL,
    gioKe           VARCHAR(10) NULL,
    chanDoan        NVARCHAR(255) NULL,
    trieuChung      NVARCHAR(255) NULL,
    trangThai       NVARCHAR(50) DEFAULT N'Đang điều trị',
    loiDan          NVARCHAR(500) NULL,
    ngayTaiKham     DATE NULL,
    maPhacDo        NVARCHAR(30) NULL,
    nhatKyLog       NVARCHAR(MAX) NULL
);
GO

CREATE TABLE CHITIET_DONTHUOC (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    maDonThuoc      NVARCHAR(30) NOT NULL,
    maSKU           NVARCHAR(30) NULL,
    tenThuoc        NVARCHAR(150) NOT NULL,
    donViTinh       NVARCHAR(30) NULL,
    soLuong         INT DEFAULT 1,
    donGia          BIGINT DEFAULT 0,
    huongDan        NVARCHAR(255) NULL,
    CONSTRAINT FK_CTDT_DT FOREIGN KEY (maDonThuoc) REFERENCES DONTHUOC(maDonThuoc) ON DELETE CASCADE
);
GO

CREATE TABLE PHACDO_DIEUTRI (
    maPhacDo        NVARCHAR(30) PRIMARY KEY,
    tenPhacDo       NVARCHAR(200) NOT NULL,
    maThuCung       NVARCHAR(20) NULL,
    tenThuCung      NVARCHAR(100) NULL,
    loaiThuCung     NVARCHAR(50) NULL,
    giongLoai       NVARCHAR(100) NULL,
    canNang         FLOAT DEFAULT 0,
    tenChuNuoi      NVARCHAR(100) NULL,
    soDienThoai     NVARCHAR(20) NULL,
    bacSi           NVARCHAR(100) NULL,
    ngayBatDau      DATE NULL,
    ngayKetThuc     DATE NULL,
    mucDo           NVARCHAR(50) NULL,
    chanDoan        NVARCHAR(255) NULL,
    mucTieu         NVARCHAR(255) NULL,
    cheDoDinhDuong  NVARCHAR(500) NULL,
    tienDo          INT DEFAULT 0,
    trangThai       NVARCHAR(50) DEFAULT N'Đang điều trị',
    maDonThuoc      NVARCHAR(30) NULL,
    nhatKyLog       NVARCHAR(MAX) NULL
);
GO

CREATE TABLE GIAIDOAN_DIEUTRI (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    maPhacDo        NVARCHAR(30) NOT NULL,
    soThuTu         INT DEFAULT 1,
    tenGiaiDoan     NVARCHAR(150) NOT NULL,
    thoiGianUocTinh NVARCHAR(50) NULL,
    mucTieuGiaiDoan NVARCHAR(255) NULL,
    phuongPhap      NVARCHAR(500) NULL,
    trangThai       NVARCHAR(50) DEFAULT N'Chưa thực hiện',
    CONSTRAINT FK_GDDT_PD FOREIGN KEY (maPhacDo) REFERENCES PHACDO_DIEUTRI(maPhacDo) ON DELETE CASCADE
);
GO

CREATE TABLE NHATKY_THEODOI (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    maPhacDo        NVARCHAR(30) NOT NULL,
    thoiGian        DATETIME NULL,
    bacSi           NVARCHAR(100) NULL,
    canNang         FLOAT DEFAULT 0,
    nhietDo         FLOAT DEFAULT 0,
    tinhTrang       NVARCHAR(100) NULL,
    ghiChu          NVARCHAR(500) NULL,
    CONSTRAINT FK_NKTD_PD FOREIGN KEY (maPhacDo) REFERENCES PHACDO_DIEUTRI(maPhacDo) ON DELETE CASCADE
);
GO

-- Nạp dữ liệu Đơn Thuốc
INSERT INTO DONTHUOC (maDonThuoc, maThuCung, tenThuCung, loaiThuCung, giongLoai, canNang, tenChuNuoi, soDienThoai, bacSi, ngayKe, gioKe, chanDoan, trieuChung, trangThai, loiDan, ngayTaiKham, maPhacDo, nhatKyLog) VALUES
(N'DT-001', N'PET002', N'Milu', N'Chó', N'Golden Retriever', 28.5, N'Lê Thị Cẩm Tú', '0938222111', N'BS. Võ Thanh Hùng', '2026-10-16', '08:30', N'Hậu phẫu lấy sỏi bàng quang (PT-001) & nhiễm khuẩn tiết niệu', N'Tiểu buốt, tiểu rắt, nước tiểu đục sau phẫu thuật sỏi', N'Đang điều trị', N'Uống thuốc sau ăn no, uống nhiều nước lọc (1.5L/ngày), kiêng thức ăn mặn và hải sản', '2026-10-23', N'PD-001', N'Đã tạo đơn thuốc hậu phẫu liên kết với phác đồ PD-001 bởi BS. Võ Thanh Hùng|Đã xuất kho và cấp phát thuốc cho chủ nuôi Lê Thị Cẩm Tú'),
(N'DT-002', N'PET007', N'Buddy', N'Chó', N'Poodle Toy', 4.5, N'Hoàng Minh Tuấn', '0945678901', N'BS. Nguyễn Hoài Anh', '2026-10-20', '09:15', N'Viêm da cơ địa dị ứng, nhiễm nấm Microsporum canis', N'Ngứa dữ dội, gãi rụng lông từng mảng tròn ở lưng và vành tai', N'Đang điều trị', N'Đeo loa chống liếm 24/24, tắm bằng sữa tắm thuốc 2 lần/tuần theo phác đồ', '2026-10-27', N'PD-002', N'Tạo đơn thuốc điều trị viêm da liên kết với phác đồ PD-002|Đã cấp phát thuốc và chuyển giao quy trình cho khu lưu trú C-101'),
(N'DT-003', N'PET004', N'Rocky', N'Chó', N'Rottweiler', 35.0, N'Võ Hoàng Long', '0386909808', N'BS. Võ Thanh Hùng', '2026-10-21', '10:00', N'Hậu phẫu kết hợp xương đùi gãy (PT-003), phòng ngừa nhiễm trùng huyết', N'Sưng nề vùng đùi phải, hạn chế vận động, đau khi cử động chi', N'Chờ cấp thuốc', N'Bất động tuyệt đối, nhốt chuồng lưu trú tĩnh dưỡng, thay gạc 2 ngày/lần', '2026-10-28', N'PD-003', N'Tạo đơn thuốc kháng sinh & giảm đau hậu phẫu kết hợp xương bởi BS. Võ Thanh Hùng'),
(N'DT-004', N'PET001', N'Kitty', N'Mèo', N'Anh lông ngắn', 4.2, N'Nguyễn Minh Anh', '0903123456', N'BS. Ngô Gia Bảo', '2026-10-15', '14:00', N'Viêm ruột cấp tính, rối loạn tiêu hóa nhẹ do đổi thức ăn', N'Nôn dịch bọt trắng 1 lần, phân lỏng nhẹ, ăn kém', N'Hoàn tất', N'Cho ăn thức ăn mềm dễ tiêu, chia nhỏ 4 bữa/ngày, uống đủ nước', '2026-10-19', N'', N'Đã tạo đơn thuốc khám bệnh ngoại trú|Đã cấp phát đầy đủ thuốc cho chủ nuôi Nguyễn Minh Anh|Tái khám ngày 19/10: Bé Kitty đã hoàn toàn khỏe mạnh, phân khuôn đẹp'),
(N'DT-005', N'PET005', N'Mimi', N'Mèo', N'Xiêm (Siamese)', 3.2, N'Võ Hoàng Long', '0386909808', N'BS. Ngô Gia Bảo', '2026-10-19', '11:30', N'Chăm sóc và giảm đau hậu phẫu triệt sản nội soi (PT-002)', N'Vết mổ nội soi 1cm khô ráo, thân nhiệt ổn định 38.4°C', N'Đã cấp phát', N'Kiểm tra mép vết mổ hàng ngày, không để liếm vết thương, cắt chỉ sau 7 ngày', '2026-10-26', N'', N'Kê đơn thuốc hậu phẫu triệt sản nội soi cho bé Mimi|Đã xuất kho và cấp phát thuốc cho chủ nuôi');
GO

-- Nạp chi tiết thuốc
INSERT INTO CHITIET_DONTHUOC (maDonThuoc, maSKU, tenThuoc, donViTinh, soLuong, donGia, huongDan) VALUES
(N'DT-001', N'MED003', N'Amoxicillin + Clavulanic 250mg', N'viên', 14, 15000, N'Uống 1 viên/lần, ngày 2 lần sáng - chiều sau ăn 30 phút'),
(N'DT-001', N'MED004', N'Meloxicam kháng viêm giảm đau 0.5mg', N'viên', 7, 12000, N'Uống 1 viên/ngày vào buổi sáng sau ăn'),
(N'DT-001', N'CARE003', N'Dung dịch xịt sát trùng Betadine Vet 100ml', N'chai', 1, 75000, N'Xịt vệ sinh mép vết mổ 2 lần/ngày, giữ vết mổ khô ráo'),
(N'DT-001', N'MED002', N'Gel dinh dưỡng cho chó mèo Nourse 120g', N'tuýp', 1, 180000, N'Ăn 5cm gel/ngày giúp bồi bổ thể lực hậu phẫu'),
(N'DT-002', N'CARE002', N'Sữa tắm trị ve rận Bio Derma 250ml', N'chai', 1, 95000, N'Tắm 2 lần/tuần, xoa kỹ bọt ủ 10 phút rồi xả sạch nước ấm'),
(N'DT-002', N'MED001', N'Kem bôi da trị nấm Dermaleen 20g', N'tuýp', 2, 85000, N'Bôi lớp mỏng lên vùng da tổn thương 2 lần/ngày sáng - tối'),
(N'DT-002', N'MED004', N'Kháng Histamin chống dị ứng Cetirizine 5mg', N'viên', 10, 6000, N'Uống 1/2 viên/ngày vào buổi tối giúp giảm ngứa'),
(N'DT-003', N'MED003', N'Cefalexin 500mg kháng sinh phổ rộng', N'viên', 20, 18000, N'Uống 1 viên/lần, ngày 2 lần sáng - tối sau ăn no'),
(N'DT-003', N'MED004', N'Meloxicam kháng viêm giảm đau 0.5mg', N'viên', 14, 12000, N'Uống 2 viên/ngày vào buổi sáng sau ăn no'),
(N'DT-003', N'MED002', N'Canxi nano sinh học Pet Cal Plus', N'lọ', 1, 220000, N'Uống 2 viên/ngày kích thích liền can xương sớm'),
(N'DT-004', N'MED004', N'Men tiêu hóa vi sinh sống Probiotic Bio-Scour', N'gói', 10, 20000, N'Hòa 1 gói vào 10ml nước ấm cho uống ngày 2 lần'),
(N'DT-004', N'MED001', N'Dung dịch nhỏ mắt cho thú cưng Bioline 50ml', N'lọ', 1, 65000, N'Nhỏ 2 giọt/mắt/lần, ngày 2 lần vệ sinh gỉ mắt'),
(N'DT-005', N'MED003', N'Amoxicillin + Clavulanic 125mg', N'viên', 8, 10000, N'Uống 1/2 viên/lần, ngày 2 lần sau ăn no'),
(N'DT-005', N'CARE003', N'Dung dịch xịt sát trùng Betadine Vet 100ml', N'chai', 1, 75000, N'Xịt sát khuẩn vết mổ 1 lần/ngày vào buổi tối');
GO

-- Nạp Phác đồ điều trị
INSERT INTO PHACDO_DIEUTRI (maPhacDo, tenPhacDo, maThuCung, tenThuCung, loaiThuCung, giongLoai, canNang, tenChuNuoi, soDienThoai, bacSi, ngayBatDau, ngayKetThuc, mucDo, chanDoan, mucTieu, cheDoDinhDuong, tienDo, trangThai, maDonThuoc, nhatKyLog) VALUES
(N'PD-001', N'Phác đồ phục hồi chức năng tiết niệu & hậu phẫu sỏi bàng quang', N'PET002', N'Milu', N'Chó', N'Golden Retriever', 28.5, N'Lê Thị Cẩm Tú', '0938222111', N'BS. Võ Thanh Hùng', '2026-10-15', '2026-10-30', N'Nặng', N'Sỏi bàng quang Struvite kích thước 15mm đã phẫu thuật lấy sỏi (PT-001)', N'Kiểm soát nhiễm trùng đường tiểu, hồi phục niêm mạc bàng quang, ngăn ngừa tái phát sỏi', N'Hạt Royal Canin Urinary S/O, uống đủ 1.5 lít nước lọc/ngày, kiêng hoàn toàn hải sản', 60, N'Đang điều trị', N'DT-001', N'Thiết lập phác đồ điều trị bởi BS. Võ Thanh Hùng|Liên kết với ca phẫu thuật PT-001 và đơn thuốc DT-001'),
(N'PD-002', N'Phác đồ điều trị viêm da cơ địa dị ứng & nấm da mãn tính', N'PET007', N'Buddy', N'Chó', N'Poodle Toy', 4.5, N'Hoàng Minh Tuấn', '0945678901', N'BS. Nguyễn Hoài Anh', '2026-10-18', '2026-11-05', N'Trung bình', N'Viêm da Atopic kết hợp nấm kẽ móng và vành tai (đang lưu trú tại lồng C-101)', N'Tiêu diệt vi nấm gây bệnh, dứt cơn ngứa dị ứng, kích thích tái tạo nang lông mới', N'Thức ăn hạt cá hồi giảm dị ứng da, bổ sung Omega 3-6 Derma, kiêng thịt gà và bò', 40, N'Đang điều trị', N'DT-002', N'Thiết lập phác đồ điều trị viêm da bởi BS. Nguyễn Hoài Anh|Liên kết với đơn thuốc DT-002 và phiếu lưu trú lồng C-101');
GO

-- Nạp giai đoạn điều trị
INSERT INTO GIAIDOAN_DIEUTRI (maPhacDo, soThuTu, tenGiaiDoan, thoiGianUocTinh, mucTieuGiaiDoan, phuongPhap, trangThai) VALUES
(N'PD-001', 1, N'Chăm sóc vết mổ & kiểm soát đau cấp', N'3 ngày', N'Giảm đau, kháng viêm, giữ vết mổ khô ráo không nhiễm trùng', N'Meloxicam 0.5mg uống sau ăn no, Betadine Vet xịt vệ sinh vết khâu 2 lần/ngày', N'Đã hoàn thành'),
(N'PD-001', 2, N'Kháng sinh điều trị nhiễm trùng bàng quang', N'7 ngày', N'Diệt khuẩn đường niệu, kiểm soát pH nước tiểu về mức 6.5', N'Amoxicillin + Clavulanic 250mg, nước điện giải, bổ sung Gel Nourse', N'Đang tiến hành'),
(N'PD-001', 3, N'Phục hồi niêm mạc & siêu âm kiểm tra', N'5 ngày', N'Khảo sát hình ảnh siêu âm bàng quang sạch cặn, cắt chỉ vết mổ', N'Siêu âm màu 4D kiểm tra hệ tiết niệu, xét nghiệm nước tiểu 10 thông số', N'Chưa thực hiện'),
(N'PD-002', 1, N'Cắt cơn ngứa cấp & kiểm soát tự cắn phá', N'4 ngày', N'Chấm dứt hành vi cào cấu gây loét da, chống nhiễm khuẩn thứ phát', N'Đeo loa bảo vệ, Cetirizine 5mg uống tối, tắm bọt sát khuẩn Chlorhexidine', N'Đã hoàn thành'),
(N'PD-002', 2, N'Đặc trị vi nấm Microsporum & tái tạo da', N'10 ngày', N'Tiêu diệt bào tử nấm tại các vùng rụng lông, kích thích liền biểu bì', N'Bôi Dermaleen 20g 2 lần/ngày, tắm thuốc Bio Derma 3 ngày/lần tại khu lưu trú', N'Đang tiến hành'),
(N'PD-002', 3, N'Dưỡng lông & đánh giá miễn dịch da', N'5 ngày', N'Lông mới mọc đều, da không còn đỏ rát, xét nghiệm soi tươi nấm âm tính', N'Dầu cá hồi dưỡng lông, kiểm tra soi tươi nang lông dưới kính hiển vi', N'Chưa thực hiện');
GO

-- Nạp nhật ký theo dõi
INSERT INTO NHATKY_THEODOI (maPhacDo, thoiGian, bacSi, canNang, nhietDo, tinhTrang, ghiChu) VALUES
(N'PD-001', '2026-10-18 09:00:00', N'BS. Võ Thanh Hùng', 28.3, 38.6, N'Tiến triển tốt', N'Vết mổ khô ráo, đi tiểu thông suốt không còn rặn buốt hay lẫn máu. Tinh thần hoạt bát'),
(N'PD-001', '2026-10-16 10:30:00', N'BS. Võ Thanh Hùng', 28.5, 38.8, N'Ổn định', N'Hậu phẫu ngày đầu, đã tỉnh mê hoàn toàn, uống được nước đường ấm và ăn nhẹ cháo dinh dưỡng'),
(N'PD-002', '2026-10-21 16:00:00', N'BS. Nguyễn Hoài Anh', 4.5, 38.5, N'Tiến triển tốt', N'Cơn ngứa giảm rõ rệt, bé chịu ăn uống, các vết trợt da ở vành tai bắt đầu se mài khô'),
(N'PD-002', '2026-10-18 09:30:00', N'BS. Nguyễn Hoài Anh', 4.5, 39.0, N'Cần theo dõi sát', N'Tiếp nhận lưu trú lồng C-101. Da vùng sống lưng đỏ tấy, ngứa nhiều, đã đeo loa chống cắn');
GO
