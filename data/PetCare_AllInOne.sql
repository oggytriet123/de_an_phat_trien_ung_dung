-- ============================================================================
-- PETCARE MANAGEMENT SYSTEM - MASTER DATABASE SCRIPT (ALL-IN-ONE)
-- Hệ thống Quản Lý Cửa Hàng & Dịch Vụ Chăm Sóc Thú Cưng PetCare
-- ============================================================================
-- Tác giả: Nhóm phát triển PetCare
-- Hệ quản trị CSDL: Microsoft SQL Server (2016 trở lên)
-- Hướng dẫn: Mở file này trong SSMS (SQL Server Management Studio) hoặc
--            Azure Data Studio và nhấn Execute (F5) để khởi tạo toàn bộ CSDL.
-- ============================================================================

USE master;
GO

-- 1. Xóa CSDL cũ nếu đã tồn tại để tránh xung đột
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'PetCare')
BEGIN
    ALTER DATABASE PetCare SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PetCare;
END
GO

-- 2. Tạo CSDL mới
CREATE DATABASE PetCare;
GO

USE PetCare;
GO

-- ============================================================================
-- PHẦN I: KHỞI TẠO BẢNG HỆ THỐNG & PHÂN QUYỀN
-- ============================================================================

-- Bảng PHANQUYEN - Các nhóm quyền / vai trò
CREATE TABLE PHANQUYEN (
    maQuyen     INT IDENTITY(1,1) PRIMARY KEY,
    tenQuyen    NVARCHAR(50) NOT NULL
);
GO

-- Bảng CHUCNANG - Danh mục các module chức năng hệ thống
CREATE TABLE CHUCNANG (
    maChucNang  INT IDENTITY(1,1) PRIMARY KEY,
    tenChucNang NVARCHAR(100) NOT NULL,
    moTa        NVARCHAR(200) NULL
);
GO

-- Bảng QUYEN_CHUCNANG - Phân quyền chức năng cho từng nhóm quyền
CREATE TABLE QUYEN_CHUCNANG (
    maQuyen     INT NOT NULL,
    maChucNang  INT NOT NULL,
    PRIMARY KEY (maQuyen, maChucNang),
    FOREIGN KEY (maQuyen) REFERENCES PHANQUYEN(maQuyen) ON DELETE CASCADE,
    FOREIGN KEY (maChucNang) REFERENCES CHUCNANG(maChucNang) ON DELETE CASCADE
);
GO

-- ============================================================================
-- PHẦN II: KHỞI TẠO BẢNG NHÂN SỰ
-- ============================================================================

-- Bảng NHANVIEN - Bảng cha chứa hồ sơ nhân viên & tài khoản đăng nhập
CREATE TABLE NHANVIEN (
    maNV        NVARCHAR(20) PRIMARY KEY,
    hoTen       NVARCHAR(100) NOT NULL,
    tenDangNhap NVARCHAR(50) NOT NULL UNIQUE,
    matKhau     NVARCHAR(256) NOT NULL,   -- Lưu băm SHA-256
    maQuyen     INT NULL,
    soDienThoai NVARCHAR(15) NULL,
    ngaySinh    DATE NULL,
    email       NVARCHAR(100) NULL,
    trangThai   NVARCHAR(30) DEFAULT (N'Đang làm việc'),
    caLamViec   NVARCHAR(30) DEFAULT (N'Toàn thời gian'),
    ngayVaoLam  DATE DEFAULT (CAST(GETDATE() AS DATE)),
    FOREIGN KEY (maQuyen) REFERENCES PHANQUYEN(maQuyen)
);
GO

-- Bảng con: QUANLY
CREATE TABLE QUANLY (
    maNV        NVARCHAR(20) PRIMARY KEY,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

-- Bảng con: NHANVIEN_BANHANG
CREATE TABLE NHANVIEN_BANHANG (
    maNV        NVARCHAR(20) PRIMARY KEY,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

-- Bảng con: NHANVIEN_KHO
CREATE TABLE NHANVIEN_KHO (
    maNV        NVARCHAR(20) PRIMARY KEY,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

-- Bảng con: NHANVIEN_BACSI
CREATE TABLE NHANVIEN_BACSI (
    maNV        NVARCHAR(20) PRIMARY KEY,
    chuyenMon   NVARCHAR(100) NULL,
    FOREIGN KEY (maNV) REFERENCES NHANVIEN(maNV) ON DELETE CASCADE
);
GO

-- ============================================================================
-- PHẦN III: KHỞI TẠO BẢNG KHÁCH HÀNG & THÚ CƯNG
-- ============================================================================

-- Bảng KHACHHANG - Hồ sơ khách hàng & tích điểm thành viên
CREATE TABLE KHACHHANG (
    maKH        NVARCHAR(20) PRIMARY KEY,
    hoTen       NVARCHAR(100) NOT NULL,
    soDienThoai NVARCHAR(15) NULL,
    email       NVARCHAR(100) NULL,
    diemTichLuy INT DEFAULT 0,
    hangThe     NVARCHAR(20) DEFAULT N'Thường',
    maNV_BH     NVARCHAR(20) NULL,
    ngayTao     DATE DEFAULT (CAST(GETDATE() AS DATE)),
    FOREIGN KEY (maNV_BH) REFERENCES NHANVIEN_BANHANG(maNV)
);
GO

-- Bảng THUCUNG - Hồ sơ thú cưng thuộc sở hữu khách hàng
CREATE TABLE THUCUNG (
    petID       NVARCHAR(20) PRIMARY KEY,
    ten         NVARCHAR(100) NOT NULL,
    loai        NVARCHAR(50) NULL,
    giong       NVARCHAR(100) NULL,
    gioiTinh    NVARCHAR(10) NULL,
    tuoi        INT NULL,
    canNang     FLOAT NULL,
    theCanhBao  NVARCHAR(200) NULL,
    trangThai   NVARCHAR(50) DEFAULT N'Khỏe mạnh',
    ngayTiepNhan DATE DEFAULT (CAST(GETDATE() AS DATE)),
    maKH        NVARCHAR(20) NULL,
    FOREIGN KEY (maKH) REFERENCES KHACHHANG(maKH)
);
GO

-- ============================================================================
-- PHẦN IV: KHỞI TẠO BẢNG SẢN PHẨM & DỊCH VỤ
-- ============================================================================

-- Bảng SANPHAM - Hàng hóa, thức ăn, phụ kiện trong kho
CREATE TABLE SANPHAM (
    sku_maSP    NVARCHAR(20) PRIMARY KEY,
    tenSP       NVARCHAR(100) NOT NULL,
    donGia      FLOAT NOT NULL,
    soLuongTon  INT DEFAULT 0,
    hanSuDung   DATE NULL,
    maNV_Kho    NVARCHAR(20) NULL,
    FOREIGN KEY (maNV_Kho) REFERENCES NHANVIEN_KHO(maNV)
);
GO

-- Bảng DICHVU - Danh mục dịch vụ spa, y tế, chăm sóc
CREATE TABLE DICHVU (
    maDV        NVARCHAR(20) PRIMARY KEY,
    tenDV       NVARCHAR(100) NOT NULL,
    giaTien     FLOAT NOT NULL,
    loaiDV      NVARCHAR(50) DEFAULT N'Khám & Điều trị',
    thoiGianThucHien INT DEFAULT 30,             -- Số phút thực hiện
    moTa        NVARCHAR(500) NULL,
    trangThai   NVARCHAR(30) DEFAULT N'Đang cung cấp',
    doiTuong    NVARCHAR(100) DEFAULT N'Tất cả thú cưng'
);
GO

-- ============================================================================
-- PHẦN V: KHỞI TẠO BẢNG HÓA ĐƠN & CHI TIẾT
-- ============================================================================

-- Bảng HOADON - Hóa đơn thanh toán
CREATE TABLE HOADON (
    maHD        NVARCHAR(20) PRIMARY KEY,
    ngayLap     DATE NOT NULL,
    tongTien    FLOAT DEFAULT 0,
    maKH        NVARCHAR(20) NULL,
    FOREIGN KEY (maKH) REFERENCES KHACHHANG(maKH)
);
GO

-- Bảng CHITIETHOADON - Chi tiết mặt hàng hoặc dịch vụ trong hóa đơn
CREATE TABLE CHITIETHOADON (
    maCTHD      NVARCHAR(20) PRIMARY KEY,
    soLuong     INT NOT NULL,
    donGia      FLOAT NOT NULL,
    thanhTien   FLOAT NOT NULL,
    maHD        NVARCHAR(20) NOT NULL,
    sku         NVARCHAR(20) NULL,
    maDV        NVARCHAR(20) NULL,
    FOREIGN KEY (maHD) REFERENCES HOADON(maHD) ON DELETE CASCADE,
    FOREIGN KEY (sku) REFERENCES SANPHAM(sku_maSP),
    FOREIGN KEY (maDV) REFERENCES DICHVU(maDV)
);
GO

-- ============================================================================
-- PHẦN VI: KHỞI TẠO BẢNG CHUỒNG LƯU TRÚ & PHIẾU LƯU TRÚ
-- ============================================================================

-- Bảng CHUONGLUUTRU - Danh mục lồng/chuồng khách sạn thú cưng
CREATE TABLE CHUONGLUUTRU (
    cageID      NVARCHAR(20) PRIMARY KEY,
    size        NVARCHAR(30) NULL,
    trangThai   NVARCHAR(30) DEFAULT N'Trống',
    giaTheoDem  FLOAT NOT NULL,
    maNV_QL     NVARCHAR(20) NULL,
    FOREIGN KEY (maNV_QL) REFERENCES QUANLY(maNV)
);
GO

-- Bảng PHIEU_LUUTRU - Phiếu giao nhận và đặt chỗ lồng chuồng
CREATE TABLE PHIEU_LUUTRU (
    maPhieu         NVARCHAR(20) PRIMARY KEY,
    cageID          NVARCHAR(20) NOT NULL,
    petID           NVARCHAR(20) NULL,
    tenPet          NVARCHAR(100) NULL,
    loaiPet         NVARCHAR(50) NULL,
    tenKhach        NVARCHAR(100) NULL,
    sdtKhach        NVARCHAR(15) NULL,
    ngayVao         DATETIME DEFAULT GETDATE(),
    ngayRaDuKien    DATETIME NULL,
    ngayRaThucTe    DATETIME NULL,
    tienCoc         FLOAT DEFAULT 0,
    trangThai       NVARCHAR(30) DEFAULT N'Đang lưu trú',
    ghiChu          NVARCHAR(255) NULL,
    maKH            NVARCHAR(20) NULL,
    FOREIGN KEY (cageID) REFERENCES CHUONGLUUTRU(cageID),
    FOREIGN KEY (petID) REFERENCES THUCUNG(petID),
    FOREIGN KEY (maKH) REFERENCES KHACHHANG(maKH)
);
GO

-- Bảng CHITIET_DICHVU_CHUONG - Chi tiết dịch vụ & thức ăn phát sinh trong thời gian lưu trú
CREATE TABLE CHITIET_DICHVU_CHUONG (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    maPhieu     NVARCHAR(20) NOT NULL,
    maDV        NVARCHAR(20) NULL,
    tenDV       NVARCHAR(100) NOT NULL,
    loai        NVARCHAR(50) NULL,
    donGia      FLOAT NOT NULL,
    soLuong     INT DEFAULT 1,
    thoiGian    DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (maPhieu) REFERENCES PHIEU_LUUTRU(maPhieu) ON DELETE CASCADE
);
GO

-- ============================================================================
-- PHẦN VII: KHỞI TẠO BẢNG DANH MỤC BỆNH & LỊCH HẸN
-- ============================================================================

-- Bảng DANHMUCBENH - Từ điển bệnh lý thú cưng
CREATE TABLE DANHMUCBENH (
    maBenh      NVARCHAR(20) PRIMARY KEY,
    tenBenh     NVARCHAR(100) NOT NULL,
    trieuChung  NVARCHAR(500) NULL,
    maNV_QL     NVARCHAR(20) NULL,
    FOREIGN KEY (maNV_QL) REFERENCES QUANLY(maNV)
);
GO

-- Bảng LICHHEN - Lịch khám bệnh, tiêm phòng và đặt lịch trước
CREATE TABLE LICHHEN (
    maLH        NVARCHAR(20) PRIMARY KEY,
    ngayHen     DATE NOT NULL,
    khungGio    NVARCHAR(20) NULL,
    trangThai   NVARCHAR(30) DEFAULT N'Chờ xác nhận',
    petID       NVARCHAR(20) NULL,
    maNV_BS     NVARCHAR(20) NULL,
    maDV        NVARCHAR(20) NULL,
    cageID      NVARCHAR(20) NULL,
    FOREIGN KEY (petID) REFERENCES THUCUNG(petID),
    FOREIGN KEY (maNV_BS) REFERENCES NHANVIEN_BACSI(maNV),
    FOREIGN KEY (maDV) REFERENCES DICHVU(maDV),
    FOREIGN KEY (cageID) REFERENCES CHUONGLUUTRU(cageID)
);
GO

-- ============================================================================
-- PHẦN VIII: KHỞI TẠO CÁC VIEW TRUY VẤN TIỆN ÍCH (ĂN ĐIỂM CSDL)
-- ============================================================================

-- 1. View phục vụ Đăng nhập & Xác thực quyền
CREATE VIEW V_DANGNHAP AS
SELECT 
    nv.maNV,
    nv.hoTen,
    nv.tenDangNhap,
    nv.matKhau,
    pq.tenQuyen,
    pq.maQuyen,
    nv.trangThai
FROM NHANVIEN nv
LEFT JOIN PHANQUYEN pq ON nv.maQuyen = pq.maQuyen;
GO

-- 2. View chi tiết Thú cưng kèm thông tin Chủ nuôi
CREATE VIEW V_THUCUNG_CHITIET AS
SELECT 
    t.petID,
    t.ten,
    t.loai,
    t.giong,
    t.gioiTinh,
    t.tuoi,
    t.canNang,
    t.theCanhBao,
    t.trangThai,
    t.ngayTiepNhan,
    t.maKH,
    k.hoTen AS tenChuNuoi,
    k.soDienThoai AS sdtChuNuoi,
    k.hangThe
FROM THUCUNG t
LEFT JOIN KHACHHANG k ON t.maKH = k.maKH;
GO

-- 3. View chi tiết Nhân viên kèm Vai trò & Chuyên môn bác sĩ
CREATE VIEW V_NHANVIEN_CHITIET AS
SELECT 
    nv.maNV,
    nv.hoTen,
    nv.tenDangNhap,
    nv.soDienThoai,
    nv.ngaySinh,
    nv.email,
    nv.maQuyen,
    pq.tenQuyen,
    nv.trangThai,
    nv.caLamViec,
    nv.ngayVaoLam,
    bs.chuyenMon
FROM NHANVIEN nv
LEFT JOIN PHANQUYEN pq ON nv.maQuyen = pq.maQuyen
LEFT JOIN NHANVIEN_BACSI bs ON nv.maNV = bs.maNV;
GO

-- ============================================================================
-- PHẦN IX: KHỞI TẠO CÁC CHỈ MỤC TĂNG TỐC TRUY VẤN (INDEXES)
-- ============================================================================

CREATE INDEX IX_THUCUNG_MAKH ON THUCUNG(maKH);
CREATE INDEX IX_THUCUNG_TEN_LOAI ON THUCUNG(ten, loai);
CREATE INDEX IX_HOADON_MAKH ON HOADON(maKH);
CREATE INDEX IX_PHIEU_LUUTRU_CAGE ON PHIEU_LUUTRU(cageID);
CREATE INDEX IX_PHIEU_LUUTRU_MAKH ON PHIEU_LUUTRU(maKH);
GO

-- ============================================================================
-- PHẦN X: NẠP DỮ LIỆU MẪU ĐẦY ĐỦ (SEED DATA)
-- ============================================================================

-- 1. PHANQUYEN
INSERT INTO PHANQUYEN (tenQuyen) VALUES 
    (N'Quản lý'),       -- maQuyen = 1
    (N'Bán hàng'),      -- maQuyen = 2
    (N'Kho'),           -- maQuyen = 3
    (N'Bác sĩ');        -- maQuyen = 4
GO

-- 2. CHUCNANG (Đầy đủ 17 module chức năng hệ thống)
INSERT INTO CHUCNANG (tenChucNang, moTa) VALUES 
    (N'Trang Chủ', N'Trang chủ hệ thống'),                          -- 1
    (N'Tài Khoản', N'Quản lý tài khoản cá nhân'),                   -- 2
    (N'Đăng Xuất', N'Đăng xuất khỏi hệ thống'),                     -- 3
    (N'Thú Cưng', N'Quản lý hồ sơ thú cưng'),                       -- 4
    (N'Khách Hàng', N'Quản lý khách hàng & thành viên'),            -- 5
    (N'Nhân Viên', N'Quản lý nhân viên & phân công'),               -- 6
    (N'Sản Phẩm', N'Quản lý sản phẩm & tồn kho'),                   -- 7
    (N'Dịch Vụ', N'Quản lý danh mục dịch vụ'),                      -- 8
    (N'Danh Mục Bệnh', N'Quản lý danh mục bệnh thú y'),             -- 9
    (N'Chuồng Lưu Trữ', N'Quản lý lồng chuồng khách sạn thú cưng'), -- 10
    (N'Hóa Đơn', N'Quản lý hóa đơn bán hàng'),                      -- 11
    (N'Lịch Hẹn', N'Quản lý lịch hẹn khám & spa'),                  -- 12
    (N'Tìm Kiếm', N'Tìm kiếm thông tin đa năng'),                   -- 13
    (N'Thống Kê Doanh Thu', N'Báo cáo doanh thu bán hàng'),         -- 14
    (N'Thống Kê Khách Hàng', N'Báo cáo & phân tích khách hàng'),    -- 15
    (N'Khuyến Mãi', N'Quản lý chương trình khuyến mãi'),            -- 16
    (N'Thống Kê Thú Cưng', N'Xem thống kê mật độ thú cưng');        -- 17
GO

-- 3. QUYEN_CHUCNANG
-- Quản lý (maQuyen=1): Toàn quyền tất cả các chức năng (1 -> 17)
INSERT INTO QUYEN_CHUCNANG (maQuyen, maChucNang)
SELECT 1, maChucNang FROM CHUCNANG;

-- Bán hàng (maQuyen=2): Trang Chủ, Tài Khoản, Đăng Xuất, Thú Cưng, Khách Hàng, Hóa Đơn, Tìm Kiếm
INSERT INTO QUYEN_CHUCNANG (maQuyen, maChucNang) VALUES 
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 11), (2, 13);

-- Kho (maQuyen=3): Trang Chủ, Tài Khoản, Đăng Xuất, Sản Phẩm, Tìm Kiếm
INSERT INTO QUYEN_CHUCNANG (maQuyen, maChucNang) VALUES 
    (3, 1), (3, 2), (3, 3), (3, 7), (3, 13);

-- Bác sĩ (maQuyen=4): Trang Chủ, Tài Khoản, Đăng Xuất, Thú Cưng, Dịch Vụ, Danh Mục Bệnh, Chuồng Lưu Trữ, Lịch Hẹn, Tìm Kiếm
INSERT INTO QUYEN_CHUCNANG (maQuyen, maChucNang) VALUES 
    (4, 1), (4, 2), (4, 3), (4, 4), (4, 8), (4, 9), (4, 10), (4, 12), (4, 13);
GO

-- 4. NHANVIEN (Mật khẩu mặc định là '123456' - băm SHA-256)
-- Hash: 8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92
INSERT INTO NHANVIEN (maNV, hoTen, tenDangNhap, matKhau, maQuyen, soDienThoai, ngaySinh, email, trangThai, caLamViec, ngayVaoLam) VALUES 
    (N'NV001', N'Hùng Hữu Thịnh',  N'admin',      N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 1, N'0901000001', '1990-03-15', N'an.nguyen@petcare.vn',   N'Đang làm việc', N'Toàn thời gian', '2023-01-10'),
    (N'NV002', N'Hà Trọng Trung',   N'trung.ht',   N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 2, N'0901000002', '1995-07-22', N'bich.tran@petcare.vn',   N'Đang làm việc', N'Ca sáng',         '2023-05-15'),
    (N'NV003', N'Lê Tuấn Anh',      N'tuananh.lt', N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 3, N'0901000003', '1992-11-05', N'cuong.le@petcare.vn',    N'Đang làm việc', N'Toàn thời gian', '2023-03-20'),
    (N'NV004', N'Nguyễn Hoài Anh',  N'hoaianh.nh', N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 4, N'0901000004', '1988-01-30', N'duc.pham@petcare.vn',    N'Đang làm việc', N'Ca chiều',        '2023-02-12'),
    (N'NV005', N'Nguyễn Ngọc Toàn', N'toan.nn',    N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 2, N'0901000005', '1997-09-12', N'lan.hoang@petcare.vn',   N'Đang làm việc', N'Ca tối',          '2023-08-01'),
    (N'NV006', N'Võ Thanh Hùng',    N'hung06',     N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 4, N'0901000006', '1991-06-18', N'hung.vo@petcare.vn',     N'Đang làm việc', N'Toàn thời gian', '2023-09-10'),
    (N'NV007', N'Đặng Thị Mai',     N'mai07',      N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 3, N'0901000007', '1996-12-25', N'mai.dang@petcare.vn',    N'Tạm nghỉ',      N'Ca sáng',         '2023-11-20'),
    (N'NV008', N'Ngô Gia Bảo',      N'bao08',      N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 4, N'0901000008', '1993-04-18', N'bao.ngo@petcare.vn',     N'Đang làm việc', N'Toàn thời gian', '2024-01-15'),
    (N'NV009', N'Đỗ Phương Thảo',   N'thao09',     N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 2, N'0901000009', '1999-08-25', N'thao.do@petcare.vn',     N'Đang làm việc', N'Ca chiều',        '2024-03-01'),
    (N'NV010', N'Bùi Hữu Thịnh',    N'thinh10',    N'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 1, N'0901000010', '1996-12-10', N'thinh.bui@petcare.vn',    N'Đang làm việc', N'Toàn thời gian', '2024-04-10');
GO

-- 5. BẢNG PHÂN HỆ NHÂN VIÊN
INSERT INTO QUANLY (maNV) VALUES (N'NV001'), (N'NV010');
INSERT INTO NHANVIEN_BANHANG (maNV) VALUES (N'NV002'), (N'NV005'), (N'NV009');
INSERT INTO NHANVIEN_KHO (maNV) VALUES (N'NV003'), (N'NV007');
INSERT INTO NHANVIEN_BACSI (maNV, chuyenMon) VALUES 
    (N'NV004', N'Nội khoa thú y'),
    (N'NV006', N'Phẫu thuật ngoại khoa'),
    (N'NV008', N'Chẩn đoán hình ảnh & Siêu âm');
GO

-- 6. KHACHHANG
INSERT INTO KHACHHANG (maKH, hoTen, soDienThoai, email, diemTichLuy, hangThe, maNV_BH, ngayTao) VALUES 
    (N'KH001', N'Nguyễn Minh Anh',  N'0903123456', N'minhanh.nguyen@gmail.com', 520, N'VIP',    N'NV002', '2025-01-10'),
    (N'KH002', N'Lê Thị Cẩm Tú',    N'0938222111', N'camtu.le@gmail.com',       320, N'Vàng',   N'NV002', '2025-02-15'),
    (N'KH003', N'Phạm Gia Hân',     N'0977654321', N'giahan.pham@gmail.com',     40, N'Thường', N'NV005', '2025-03-20'),
    (N'KH004', N'Võ Hoàng Long',    N'0386909808', N'hoanglong.vo@gmail.com',   150, N'Bạc',    N'NV005', '2025-04-05'),
    (N'KH005', N'Hoàng Minh Tuấn',  N'0945678901', N'tuan.hoang@gmail.com',      80, N'Thường', N'NV002', '2025-05-12'),
    (N'KH006', N'Bùi Anh Khoa',     N'0919888000', N'anhkhoa.bui@gmail.com',    210, N'Vàng',   N'NV002', '2025-06-18'),
    (N'KH007', N'Đặng Thu Hà',      N'0905777333', N'thuha.dang@gmail.com',      20, N'Thường', N'NV005', '2025-07-22'),
    (N'KH008', N'Hoàng Thị Mai',    N'0932111222', N'thimai.hoang@gmail.com',   120, N'Bạc',    N'NV002', '2025-08-30');
GO

-- 7. THUCUNG
INSERT INTO THUCUNG (petID, ten, loai, giong, gioiTinh, tuoi, canNang, theCanhBao, trangThai, ngayTiepNhan, maKH) VALUES 
    (N'PET001', N'Kitty',      N'Mèo', N'Anh lông ngắn',     N'Cái', 2,  4.2, NULL,                                              N'Khỏe mạnh',       '2025-08-01', N'KH001'),
    (N'PET002', N'Milu',       N'Chó', N'Golden Retriever',  N'Đực', 3, 26.5, N'Dị ứng thức ăn hải sản, nhạy cảm với mùi tanh',   N'Dị ứng hải sản',  '2025-08-10', N'KH002'),
    (N'PET003', N'Mochi',      N'Mèo', N'Ba Tư (Persian)',   N'Cái', 1,  3.5, NULL,                                              N'Khỏe mạnh',       '2025-08-15', N'KH002'),
    (N'PET004', N'Rocky',      N'Chó', N'Rottweiler',        N'Đực', 4, 28.0, N'Hung dữ với người lạ, cần đeo rọ mõm',           N'Cần chú ý',       '2025-08-20', N'KH004'),
    (N'PET005', N'Mimi',       N'Mèo', N'Xiêm (Siamese)',    N'Cái', 2,  3.8, NULL,                                              N'Khỏe mạnh',       '2025-09-01', N'KH004'),
    (N'PET006', N'Bông Gòn',   N'Chó', N'Samoyed',           N'Đực', 2, 22.0, N'Lông dày, cần phòng mát điều hòa dưới 25°C',     N'Đang theo dõi',   '2025-09-05', N'KH006'),
    (N'PET007', N'Buddy',      N'Chó', N'Poodle Toy',        N'Đực', 3,  5.5, NULL,                                              N'Khỏe mạnh',       '2025-09-12', N'KH005'),
    (N'PET008', N'Mochi Béo',  N'Mèo', N'Munchkin',          N'Cái', 1,  4.0, NULL,                                              N'Khỏe mạnh',       '2025-09-18', N'KH008'),
    (N'PET009', N'Corgi Mông', N'Chó', N'Corgi Pembroke',    N'Đực', 2, 11.5, N'Đang trong lịch tiêm phòng mũi 2',               N'Đang tiêm phòng', '2025-10-01', N'KH001'),
    (N'PET010', N'Bánh Bao',   N'Thỏ', N'Thỏ Minilop',       N'Cái', 1,  2.3, NULL,                                              N'Khỏe mạnh',       '2025-10-05', N'KH003'),
    (N'PET011', N'Đậu Phộng',  N'Chó', N'Pomeranian',        N'Cái', 1,  3.2, N'Đang điều trị nấm da nhẹ, kiêng tắm nước',       N'Đang điều trị',   '2025-10-10', N'KH007'),
    (N'PET012', N'Bông Xù',    N'Mèo', N'Mèo Ragdoll',       N'Đực', 3,  4.5, NULL,                                              N'Khỏe mạnh',       '2025-10-15', N'KH006');
GO

-- 8. SANPHAM
INSERT INTO SANPHAM (sku_maSP, tenSP, donGia, soLuongTon, hanSuDung, maNV_Kho) VALUES 
    (N'SP001', N'Thức ăn hạt cho chó Royal Canin 2kg', 350000, 50,  '2026-06-15', N'NV003'),
    (N'SP002', N'Thức ăn hạt cho mèo Whiskas 1.5kg',   180000, 80,  '2026-08-20', N'NV003'),
    (N'SP003', N'Sữa tắm cho chó Bio 500ml',           120000, 30,  '2027-01-10', N'NV003'),
    (N'SP004', N'Vòng cổ chống ve rận',                 95000, 45,  '2027-03-25', N'NV007'),
    (N'SP005', N'Cát vệ sinh cho mèo 5L',               85000, 100, '2027-12-31', N'NV007'),
    (N'SP006', N'Bàn chải lông cho thú cưng',           65000, 25,  NULL,         N'NV007'),
    (N'SP007', N'Đồ chơi bóng cao su phát tiếng',       45000, 60,  NULL,         N'NV003'),
    (N'SP008', N'Thuốc nhỏ mắt trị viêm ghèn',          75000, 40,  '2026-05-30', N'NV003');
GO

-- 9. DICHVU
INSERT INTO DICHVU (maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong) VALUES 
    (N'DV001', N'Khám tổng quát',               200000, N'Khám & Điều trị',        30,   N'Khám tổng quát tai mũi họng, tim phổi, kiểm tra thân nhiệt và tư vấn sức khỏe',               N'Đang cung cấp', N'Tất cả thú cưng'),
    (N'DV002', N'Tiêm phòng vaccine',           350000, N'Tiêm phòng & Xét nghiệm', 20,   N'Tiêm vaccine phòng bệnh dại, 5 bệnh / 7 bệnh nhập khẩu chính hãng kèm sổ theo dõi',           N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV003', N'Tắm và cắt tỉa lông',          150000, N'Spa & Grooming',          60,   N'Tắm sấy khử mùi, vắt tuyến hôi, cắt mài móng, vệ sinh tai và cắt tỉa lông tạo kiểu',         N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV004', N'Phẫu thuật triệt sản',        1500000, N'Phẫu thuật',              90,   N'Phẫu thuật triệt sản an toàn bằng máy gây mê nội khí quản, theo dõi hồi sức sau phẫu thuật',  N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV005', N'Lưu trữ thú cưng (theo đêm)',   100000, N'Khách sạn & Lưu trú',    1440, N'Gửi thú cưng theo đêm tại phòng máy lạnh vô trùng, có camera giám sát 24/7 và chế độ ăn',    N'Đang cung cấp', N'Tất cả thú cưng'),
    (N'DV006', N'Siêu âm ổ bụng chẩn đoán',     300000, N'Khám & Điều trị',        25,   N'Siêu âm màu ổ bụng, chẩn đoán thai sớm và tầm soát các bệnh lý nội tạng',                    N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV007', N'Xét nghiệm máu tổng quát',     250000, N'Tiêm phòng & Xét nghiệm', 30,   N'Xét nghiệm huyết học tổng quát 18 chỉ số, sinh hóa máu đánh giá chức năng gan thận',          N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV008', N'Lấy cao răng siêu âm',         450000, N'Spa & Grooming',          45,   N'Vệ sinh mảng bám răng bằng công nghệ sóng siêu âm không đau, xịt thơm miệng',                 N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV009', N'Tắm bồn sục thảo dược O3',     250000, N'Spa & Grooming',          45,   N'Tắm bồn sục nước ấm tinh dầu thảo dược giúp giảm stress, trị viêm da và phục hồi lông',      N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV010', N'Cấp cứu thú cưng 24/7',        500000, N'Khám & Điều trị',        60,   N'Tiếp nhận cấp cứu ngộ độc, khó sinh, chấn thương tai nạn ngoài giờ với bác sĩ trực đêm',      N'Đang cung cấp', N'Tất cả thú cưng'),
    (N'DV011', N'Cấy Microchip định danh',      300000, N'Tiêm phòng & Xét nghiệm', 15,   N'Cấy vi mạch định danh quốc tế chuẩn ISO giúp xác định thông tin chủ nuôi và quản lý số hóa', N'Đang cung cấp', N'Chó & Mèo'),
    (N'DV012', N'Nhuộm lông thời trang',        350000, N'Spa & Grooming',          90,   N'Nhuộm tai đuôi hoặc 4 chân bằng thuốc nhuộm sinh học chiết xuất thực vật an toàn',           N'Tạm ngưng',     N'Chỉ dành cho Chó');
GO

-- 10. CHUONGLUUTRU (15 lồng chuồng hiện đại theo 4 khu vực)
INSERT INTO CHUONGLUUTRU (cageID, size, trangThai, giaTheoDem, maNV_QL) VALUES 
    (N'C-101', N'Nhỏ (<5kg)',    N'Trống',         80000, N'NV001'),
    (N'C-102', N'Nhỏ (<5kg)',    N'Đang lưu trú',  80000, N'NV001'),
    (N'C-103', N'Vừa (5-15kg)',  N'Đã đặt',       100000, N'NV001'),
    (N'C-104', N'Nhỏ (<5kg)',    N'Trống',         80000, N'NV001'),
    (N'C-201', N'Nhỏ (<5kg)',    N'Trống',         90000, N'NV001'),
    (N'C-202', N'Vừa (5-15kg)',  N'Đang lưu trú', 120000, N'NV001'),
    (N'C-203', N'Vừa (5-15kg)',  N'Đang lưu trú', 120000, N'NV001'),
    (N'C-204', N'Vừa (5-15kg)',  N'Bảo trì',      120000, N'NV001'),
    (N'C-301', N'Lớn (>15kg)',   N'Trống',        180000, N'NV001'),
    (N'C-302', N'Lớn (>15kg)',   N'Đang lưu trú', 180000, N'NV001'),
    (N'C-303', N'Lớn (>15kg)',   N'Đã đặt',       180000, N'NV001'),
    (N'C-304', N'Lớn (>15kg)',   N'Trống',        180000, N'NV001'),
    (N'VIP-01', N'VIP Suite',    N'Đang lưu trú', 250000, N'NV001'),
    (N'VIP-02', N'VIP Suite',    N'Trống',        250000, N'NV001'),
    (N'VIP-03', N'VIP Suite',    N'Đã đặt',       250000, N'NV001');
GO

-- 11. PHIEU_LUUTRU
INSERT INTO PHIEU_LUUTRU (maPhieu, cageID, petID, tenPet, loaiPet, tenKhach, sdtKhach, ngayVao, ngayRaDuKien, tienCoc, trangThai, ghiChu, maKH) VALUES 
    (N'PLT-001', N'C-102',  N'PET001', N'Kitty',     N'Mèo Anh lông ngắn', N'Nguyễn Minh Anh', N'0903123456', DATEADD(DAY, -1, GETDATE()), DATEADD(DAY, 2, GETDATE()),      0, N'Đang lưu trú', N'Ăn hạt Royal Canin, thích gãi cằm',                  N'KH001'),
    (N'PLT-002', N'C-202',  N'PET002', N'Milu',      N'Chó Poodle Tiny',   N'Lê Thị Cẩm Tú',   N'0938222111', DATEADD(DAY, -2, GETDATE()), DATEADD(DAY, 1, GETDATE()),      0, N'Đang lưu trú', N'Dị ứng hải sản, uống men tiêu hóa buổi sáng',       N'KH002'),
    (N'PLT-003', N'C-203',  NULL,      N'Đậu Đậu',   N'Chó Pug',           N'Phạm Gia Hân',    N'0977654321', DATEADD(HOUR, -8, GETDATE()), DATEADD(DAY, 2, GETDATE()),     0, N'Đang lưu trú', N'Khá hiếu động, cho uống nhiều nước',                N'KH003'),
    (N'PLT-004', N'C-302',  N'PET004', N'Rocky',     N'Chó Golden',        N'Võ Hoàng Long',   N'0386909808', DATEADD(DAY, -3, GETDATE()), DATEADD(DAY, 4, GETDATE()),      0, N'Đang lưu trú', N'Ăn ngày 2 bữa, dắt đi dạo 15 phút chiều',           N'KH004'),
    (N'PLT-005', N'VIP-01', N'PET006', N'Bông Gòn',  N'Chó Samoyed',       N'Bùi Anh Khoa',    N'0919888000', DATEADD(DAY, -1, GETDATE()), DATEADD(DAY, 5, GETDATE()),      0, N'Đang lưu trú', N'Phòng điều hòa 24 độ, ăn thịt bò tươi hấp',         N'KH006'),
    (N'PLT-006', N'C-103',  N'PET005', N'Mimi',      N'Mèo Ba Tư',         N'Võ Hoàng Long',   N'0386909808', NULL,                        DATEADD(DAY, 3, GETDATE()), 100000, N'Đã đặt',       N'Khách hẹn chiều 15h mang bé tới',                   N'KH004'),
    (N'PLT-007', N'C-303',  NULL,      N'Rex',       N'Chó Husky',         N'Đặng Thu Hà',     N'0905777333', NULL,                        DATEADD(DAY, 2, GETDATE()), 200000, N'Đã đặt',       N'Hẹn gửi 2 ngày cuối tuần',                          N'KH007'),
    (N'PLT-008', N'VIP-03', N'PET008', N'Mochi',     N'Mèo Anh lông dài',  N'Hoàng Thị Mai',   N'0932111222', NULL,                        DATEADD(DAY, 1, GETDATE()), 300000, N'Đã đặt',       N'Khách đặt lưu trú VIP 3 ngày',                      N'KH008');
GO

-- 12. CHITIET_DICHVU_CHUONG
INSERT INTO CHITIET_DICHVU_CHUONG (maPhieu, maDV, tenDV, loai, donGia, soLuong, thoiGian) VALUES 
    (N'PLT-001', N'SP002', N'Pate cá ngừ Whiskas lon 400g',          N'Thức ăn & Dinh dưỡng',  35000, 2, DATEADD(HOUR, -12, GETDATE())),
    (N'PLT-001', N'DV003', N'Tắm gội khử mùi & Cắt móng',           N'Dịch vụ Spa & Grooming', 150000, 1, DATEADD(HOUR, -4, GETDATE())),
    (N'PLT-002', N'DV003', N'Combo Spa Poodle tạo kiểu bông tròn',   N'Dịch vụ Spa & Grooming', 200000, 1, DATEADD(DAY, -1, GETDATE())),
    (N'PLT-002', N'SP001', N'Gói hạt Royal Canin Mini 500g',         N'Thức ăn & Dinh dưỡng',  85000, 1, DATEADD(HOUR, -6, GETDATE())),
    (N'PLT-005', N'DV001', N'Bác sĩ thú y kiểm tra sức khỏe',        N'Y tế & Thuốc',          120000, 1, DATEADD(HOUR, -20, GETDATE())),
    (N'PLT-005', N'SP001', N'Thịt bò Úc tươi hấp củ dền',            N'Thức ăn & Dinh dưỡng',  60000, 2, DATEADD(HOUR, -5, GETDATE()));
GO

-- 13. DANHMUCBENH
INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL) VALUES 
    (N'BNH001', N'Viêm da dị ứng', N'Ngứa ngáy dữ dội, rụng lông thành từng mảng, da ửng đỏ, nổi mẩn li ti', N'NV001'),
    (N'BNH002', N'Parvo virus',     N'Nôn ói liên tục, tiêu chảy ra máu tanh nồng, sốt cao, ủ rũ bỏ ăn',     N'NV001'),
    (N'BNH003', N'Viêm tai ngoài',  N'Lắc đầu liên tục, gãi tai chảy máu, ống tai tiết dịch màu nâu hôi',     N'NV001'),
    (N'BNH004', N'Sỏi bàng quang',  N'Tiểu rắt, tiểu ngắt quãng, có tia máu trong nước tiểu, cong lưng đau', N'NV001'),
    (N'BNH005', N'Cảm cúm hô hấp',  N'Hắt hơi, chảy nước mũi đặc, ho khan, mắt đỏ đổ ghèn, sốt nhẹ',         N'NV001');
GO

-- 14. HOADON
INSERT INTO HOADON (maHD, ngayLap, tongTien, maKH) VALUES 
    (N'HD001', '2025-10-01', 530000, N'KH001'),
    (N'HD002', '2025-10-05', 350000, N'KH002'),
    (N'HD003', '2025-10-08', 425000, N'KH003'),
    (N'HD004', '2025-10-12', 200000, N'KH004'),
    (N'HD005', '2025-10-15', 820000, N'KH001');
GO

-- 15. CHITIETHOADON (Khớp chính xác tổng tiền của HOADON)
-- HD001: 350k (Royal Canin) + 180k (Whiskas) = 530k
INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV) VALUES 
    (N'CT001', 1, 350000, 350000, N'HD001', N'SP001', NULL),
    (N'CT002', 1, 180000, 180000, N'HD001', N'SP002', NULL);

-- HD002: Tiêm phòng vaccine (350k)
INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV) VALUES 
    (N'CT003', 1, 350000, 350000, N'HD002', NULL, N'DV002');

-- HD003: Vòng cổ (95k) + Thức ăn mèo (180k) + Dịch vụ Tắm (150k) = 425k
INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV) VALUES 
    (N'CT004', 1,  95000,  95000, N'HD003', N'SP004', NULL),
    (N'CT005', 1, 180000, 180000, N'HD003', N'SP002', NULL),
    (N'CT006', 1, 150000, 150000, N'HD003', NULL, N'DV003');

-- HD004: Khám tổng quát (200k)
INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV) VALUES 
    (N'CT007', 1, 200000, 200000, N'HD004', NULL, N'DV001');

-- HD005: 2x Cát vệ sinh (170k) + 1x Royal Canin (350k) + 1x Siêu âm (300k) = 820k
INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV) VALUES 
    (N'CT008', 2,  85000, 170000, N'HD005', N'SP005', NULL),
    (N'CT009', 1, 350000, 350000, N'HD005', N'SP001', NULL),
    (N'CT010', 1, 300000, 300000, N'HD005', NULL, N'DV006');
GO

-- 16. LICHHEN
INSERT INTO LICHHEN (maLH, ngayHen, khungGio, trangThai, petID, maNV_BS, maDV, cageID) VALUES 
    (N'LH001', '2025-10-20', N'08:00 - 09:00', N'Đã xác nhận', N'PET001', N'NV004', N'DV001', NULL),
    (N'LH002', '2025-10-20', N'09:00 - 10:00', N'Đã xác nhận', N'PET004', N'NV006', N'DV002', NULL),
    (N'LH003', '2025-10-21', N'10:00 - 11:00', N'Chờ xác nhận', N'PET002', N'NV004', N'DV006', NULL),
    (N'LH004', '2025-10-22', N'14:00 - 15:00', N'Chờ xác nhận', N'PET003', N'NV006', N'DV007', NULL),
    (N'LH005', '2025-10-18', N'08:00 - 09:00', N'Hoàn thành',  N'PET005', N'NV004', N'DV001', NULL),
    (N'LH006', '2025-10-25', N'15:00 - 16:00', N'Chờ xác nhận', N'PET007', N'NV006', N'DV005', N'C-101');
GO

-- ============================================================================
-- PHẦN XI: KIỂM TRA TỔNG QUAN SAU KHI TẠO
-- ============================================================================

PRINT N'====================================================================';
PRINT N'  KHỞI TẠO CƠ SỞ DỮ LIỆU PETCARE THÀNH CÔNG VÀ HOÀN TẤT 100%!';
PRINT N'====================================================================';
PRINT N'- Tài khoản quản trị tối cao: admin / 123456 (Hùng Hữu Thịnh)';
PRINT N'- Tài khoản bác sĩ:           hoaianh.nh / 123456 (Nguyễn Hoài Anh)';
PRINT N'- Tài khoản bán hàng:         trung.ht / 123456 (Hà Trọng Trung)';
PRINT N'- Tài khoản thủ kho:          tuananh.lt / 123456 (Lê Tuấn Anh)';
PRINT N'====================================================================';
GO
