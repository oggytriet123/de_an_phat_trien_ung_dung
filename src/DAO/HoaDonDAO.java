package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ConnectDB.ConnectDB;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.MaGiamGia;
import Entity.NhanVien;
import Entity.SanPham;

/**
 * Xử lý truy xuất và lưu trữ thực thể Hóa đơn và Chi tiết hóa đơn
 * trực tiếp vào cơ sở dữ liệu SQL Server (bảng HOADON và CHITIETHOADON).
 *
 * Chuẩn hóa mô hình đa tầng:
 * - Đảm bảo DAO chỉ đảm nhận nhiệm vụ truy vấn, lưu trữ và ánh xạ dữ liệu (Data Access / Persistence).
 * - Loại bỏ logic kiểm tra nghiệp vụ và xử lý dữ liệu người dùng (thuộc tầng BUS).
 * - Ánh xạ quan hệ thực thể đối tượng hoàn chỉnh: HoaDon -> KhachHang, NhanVien, List<ChiTietHoaDon>.
 */
public class HoaDonDAO {

    public HoaDonDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF COL_LENGTH('HOADON', 'soO') IS NULL ALTER TABLE HOADON ADD soO INT NULL");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'trangThai') IS NULL ALTER TABLE HOADON ADD trangThai NVARCHAR(30) DEFAULT N'Chưa thanh toán'");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'phuongThuc') IS NULL ALTER TABLE HOADON ADD phuongThuc NVARCHAR(30) NULL");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'ngayTao') IS NULL ALTER TABLE HOADON ADD ngayTao DATETIME DEFAULT GETDATE()");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'ngayThanhToan') IS NULL ALTER TABLE HOADON ADD ngayThanhToan DATETIME NULL");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'tienGiam') IS NULL ALTER TABLE HOADON ADD tienGiam FLOAT DEFAULT 0");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'maGiamGia') IS NULL ALTER TABLE HOADON ADD maGiamGia NVARCHAR(20) NULL");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'tenKH') IS NULL ALTER TABLE HOADON ADD tenKH NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'soDienThoai') IS NULL ALTER TABLE HOADON ADD soDienThoai NVARCHAR(20) NULL");
            st.executeUpdate("IF COL_LENGTH('HOADON', 'maNV_BH') IS NULL ALTER TABLE HOADON ADD maNV_BH NVARCHAR(20) NULL");

            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'tenSP') IS NULL ALTER TABLE CHITIETHOADON ADD tenSP NVARCHAR(150) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'danhMuc') IS NULL ALTER TABLE CHITIETHOADON ADD danhMuc NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'isDichVu') IS NULL ALTER TABLE CHITIETHOADON ADD isDichVu BIT DEFAULT 0");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'sku') IS NULL ALTER TABLE CHITIETHOADON ADD sku NVARCHAR(50) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'maDV') IS NULL ALTER TABLE CHITIETHOADON ADD maDV NVARCHAR(50) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'thanhTien') IS NULL ALTER TABLE CHITIETHOADON ADD thanhTien FLOAT NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'sku_maSP') IS NOT NULL EXEC('UPDATE CHITIETHOADON SET sku = sku_maSP WHERE sku IS NULL AND sku_maSP IS NOT NULL')");
            st.executeUpdate("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_HOADON_trangThai_ngay' AND object_id = OBJECT_ID('HOADON')) "
                    + "CREATE INDEX IX_HOADON_trangThai_ngay ON HOADON(trangThai, ngayThanhToan, ngayLap)");
            st.executeUpdate("UPDATE h SET h.tongTien = ISNULL((SELECT SUM(c.thanhTien) FROM CHITIETHOADON c WHERE c.maHD = h.maHD), 0) - ISNULL(h.tienGiam, 0) "
                    + "FROM HOADON h WHERE (h.tongTien = 0 OR h.tongTien IS NULL) AND h.trangThai = N'Đã thanh toán'");
            // Đồng bộ điểm tích lũy và hạng thẻ của khách hàng theo tổng tiền hóa đơn đã thanh toán (1 điểm = 10.000₫)
            st.executeUpdate("UPDATE k SET k.diemTichLuy = ISNULL(hd.tongDiem, 0), "
                    + "    k.hangThe = CASE WHEN ISNULL(hd.tongDiem, 0) >= 500 THEN N'VIP' WHEN ISNULL(hd.tongDiem, 0) >= 100 THEN N'Thân thiết' ELSE N'Thường' END "
                    + "FROM KHACHHANG k "
                    + "OUTER APPLY (SELECT CAST(FLOOR(SUM(h.tongTien) / 10000.0) AS INT) AS tongDiem FROM HOADON h WHERE h.maKH = k.maKH AND h.trangThai = N'Đã thanh toán') hd");
            damBaoDuLieuThongKe(con);
        } catch (SQLException ignored) {
        }
    }

    private void damBaoDuLieuThongKe(Connection con) {
        try (Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM HOADON WHERE maHD IN ('HD20260115001', 'HD20260212001', 'HD20260410001')");
            if (rs.next() && rs.getInt(1) >= 3) {
                return;
            }

            String[] sqlInserts = {
                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260115001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260115001', '2026-01-15', 4500000, 'NV002', 'KH001', N'Đã thanh toán', N'Tiền mặt', '2026-01-15 10:15:00', '2026-01-15 10:20:00', N'Nguyễn Minh Anh', '0903123456')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260128001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260128001', '2026-01-28', 7800000, 'NV002', 'KH002', N'Đã thanh toán', N'Chuyển khoản', '2026-01-28 14:30:00', '2026-01-28 14:35:00', N'Lê Thị Cẩm Tú', '0938222111')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260212001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260212001', '2026-02-12', 12300000, 'NV002', 'KH003', N'Đã thanh toán', N'Chuyển khoản', '2026-02-12 09:40:00', '2026-02-12 09:45:00', N'Phạm Gia Hân', '0977654321')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260224001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260224001', '2026-02-24', 6400000, 'NV002', 'KH004', N'Đã thanh toán', N'Tiền mặt', '2026-02-24 16:10:00', '2026-02-24 16:15:00', N'Võ Hoàng Long', '0386909808')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260315001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260315001', '2026-03-15', 8500000, 'NV002', 'KH005', N'Đã thanh toán', N'Chuyển khoản', '2026-03-15 11:00:00', '2026-03-15 11:05:00', N'Hoàng Minh Tuấn', '0945678901')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260322001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260322001', '2026-03-22', 11200000, 'NV002', 'KH006', N'Đã thanh toán', N'Thẻ', '2026-03-22 15:20:00', '2026-03-22 15:25:00', N'Bùi Anh Khoa', '0919888000')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260410001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260410001', '2026-04-10', 9100000, 'NV002', 'KH007', N'Đã thanh toán', N'Chuyển khoản', '2026-04-10 10:30:00', '2026-04-10 10:35:00', N'Đặng Thu Hà', '0905777333')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260425001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260425001', '2026-04-25', 5600000, 'NV002', 'KH008', N'Đã thanh toán', N'Tiền mặt', '2026-04-25 14:00:00', '2026-04-25 14:05:00', N'Hoàng Thị Mai', '0932111222')",

                "IF NOT EXISTS (SELECT 1 FROM HOADON WHERE maHD = 'HD20260515001') "
                + "INSERT INTO HOADON (maHD, ngayLap, tongTien, maNV_BH, maKH, trangThai, phuongThuc, ngayTao, ngayThanhToan, tenKH, soDienThoai) "
                + "VALUES ('HD20260515001', '2026-05-15', 10400000, 'NV002', 'KH001', N'Đã thanh toán', N'Chuyển khoản', '2026-05-15 09:15:00', '2026-05-15 09:20:00', N'Nguyễn Minh Anh', '0903123456')",

                "IF NOT EXISTS (SELECT 1 FROM PHIEUKHO WHERE maPhieu = 'PN-2026-0010') "
                + "INSERT INTO PHIEUKHO (maPhieu, loaiPhieu, ngayLap, gioLap, nguoiThucHien, doiTac, tongGiaTri, trangThai) "
                + "VALUES ('PN-2026-0010', N'Nhập mới', '2026-01-10', '08:30', N'Lê Tuấn Anh', N'Công Ty CP Royal Canin VN', 8500000, N'Hoàn tất')",

                "IF NOT EXISTS (SELECT 1 FROM PHIEUKHO WHERE maPhieu = 'PN-2026-0020') "
                + "INSERT INTO PHIEUKHO (maPhieu, loaiPhieu, ngayLap, gioLap, nguoiThucHien, doiTac, tongGiaTri, trangThai) "
                + "VALUES ('PN-2026-0020', N'Nhập mới', '2026-02-08', '09:00', N'Đặng Thị Mai', N'Thuốc thú y Minh Phương', 9200000, N'Hoàn tất')",

                "IF NOT EXISTS (SELECT 1 FROM PHIEUKHO WHERE maPhieu = 'PN-2026-0030') "
                + "INSERT INTO PHIEUKHO (maPhieu, loaiPhieu, ngayLap, gioLap, nguoiThucHien, doiTac, tongGiaTri, trangThai) "
                + "VALUES ('PN-2026-0030', N'Nhập mới', '2026-04-05', '10:00', N'Lê Tuấn Anh', N'Phụ kiện thú cưng PetStar', 7600000, N'Hoàn tất')"
            };

            for (String sql : sqlInserts) {
                try {
                    st.executeUpdate(sql);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {
        }
    }

    public List<HoaDon> layLichSu() {
        List<HoaDon> ds = new ArrayList<>();
        String sql = "SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, "
                + "       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, "
                + "       k.hangThe, k.diemTichLuy, "
                + "       ISNULL((SELECT SUM(h2.tongTien) FROM HOADON h2 WHERE h2.maKH = k.maKH AND h2.trangThai = N'Đã thanh toán'), 0) AS tongChiTieu, "
                + "       h.maNV_BH, nv.hoTen AS tenNV, nv.tenDangNhap AS tkNV, nv.maQuyen, nv.soDienThoai AS sdtNV, "
                + "       h.trangThai, h.phuongThuc, h.ngayTao, h.ngayThanhToan, h.maGiamGia, h.tienGiam, h.tongTien "
                + "FROM HOADON h "
                + "LEFT JOIN KHACHHANG k ON h.maKH = k.maKH "
                + "LEFT JOIN NHANVIEN nv ON h.maNV_BH = nv.maNV "
                + "WHERE h.trangThai = N'Đã thanh toán' "
                + "ORDER BY ISNULL(h.ngayThanhToan, h.ngayTao) DESC";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HoaDon h = docHoaDon(rs);
                napChiTiet(con, h);
                ds.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Đếm tổng số hóa đơn đã thanh toán thỏa mãn điều kiện tìm kiếm và khoảng ngày.
     */
    public int demLichSu(String tuKhoa, LocalDate tu, LocalDate den) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM HOADON h ");
        sql.append("LEFT JOIN KHACHHANG k ON h.maKH = k.maKH ");
        sql.append("WHERE h.trangThai = N'Đã thanh toán' ");

        List<Object> params = new ArrayList<>();
        ganDieuKienLoc(sql, params, tuKhoa, tu, den);

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof java.sql.Date) {
                    ps.setDate(i + 1, (java.sql.Date) p);
                } else {
                    ps.setString(i + 1, (String) p);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Phân trang danh sách lịch sử hóa đơn trực tiếp tại SQL Server sử dụng OFFSET và FETCH NEXT.
     * Sắp xếp mới nhất lên trước theo ngày lập hóa đơn (hoặc ngày thanh toán) và mã hóa đơn.
     * Chỉ tải các cột cần thiết của hóa đơn; không nạp chi tiết hàng hóa vào RAM để tối ưu hiệu năng.
     */
    public List<HoaDon> layLichSuPhanTrang(String tuKhoa, LocalDate tu, LocalDate den, int trang, int soLuongMoiTrang) {
        if (trang < 1) trang = 1;
        if (soLuongMoiTrang <= 0) soLuongMoiTrang = 10;
        int offset = (trang - 1) * soLuongMoiTrang;

        List<HoaDon> ds = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, ");
        sql.append("       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, ");
        sql.append("       k.hangThe, k.diemTichLuy, ");
        sql.append("       ISNULL((SELECT SUM(h2.tongTien) FROM HOADON h2 WHERE h2.maKH = k.maKH AND h2.trangThai = N'Đã thanh toán'), 0) AS tongChiTieu, ");
        sql.append("       h.maNV_BH, nv.hoTen AS tenNV, nv.tenDangNhap AS tkNV, nv.maQuyen, nv.soDienThoai AS sdtNV, ");
        sql.append("       h.trangThai, h.phuongThuc, h.ngayTao, h.ngayThanhToan, h.maGiamGia, h.tienGiam, h.tongTien ");
        sql.append("FROM HOADON h ");
        sql.append("LEFT JOIN KHACHHANG k ON h.maKH = k.maKH ");
        sql.append("LEFT JOIN NHANVIEN nv ON h.maNV_BH = nv.maNV ");
        sql.append("WHERE h.trangThai = N'Đã thanh toán' ");

        List<Object> params = new ArrayList<>();
        ganDieuKienLoc(sql, params, tuKhoa, tu, den);

        sql.append("ORDER BY ISNULL(h.ngayThanhToan, ISNULL(h.ngayTao, CAST(h.ngayLap AS DATETIME))) DESC, h.maHD DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) {
                if (p instanceof java.sql.Date) {
                    ps.setDate(idx++, (java.sql.Date) p);
                } else {
                    ps.setString(idx++, (String) p);
                }
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx, soLuongMoiTrang);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDon h = docHoaDon(rs);
                    // Lazy load: không nạp chi tiết hàng hóa ở đây để đảm bảo phân trang cực nhanh
                    ds.add(h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    private void ganDieuKienLoc(StringBuilder sql, List<Object> params, String tuKhoa, LocalDate tu, LocalDate den) {
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String kw = "%" + tuKhoa.trim() + "%";
            sql.append("AND (");
            sql.append("   h.maHD LIKE ? ");
            sql.append("   OR ISNULL(h.tenKH, k.hoTen) LIKE ? ");
            sql.append("   OR ISNULL(h.soDienThoai, k.soDienThoai) LIKE ? ");
            sql.append("   OR REPLACE(REPLACE(ISNULL(h.soDienThoai, k.soDienThoai), ' ', ''), '-', '') LIKE ? ");
            sql.append("   OR ISNULL(h.maKH, k.maKH) LIKE ? ");
            sql.append("   OR CONVERT(VARCHAR(10), ISNULL(h.ngayThanhToan, ISNULL(h.ngayTao, h.ngayLap)), 103) LIKE ? ");
            sql.append(") ");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (tu != null) {
            sql.append("AND CAST(ISNULL(h.ngayThanhToan, ISNULL(h.ngayTao, h.ngayLap)) AS DATE) >= ? ");
            params.add(java.sql.Date.valueOf(tu));
        }

        if (den != null) {
            sql.append("AND CAST(ISNULL(h.ngayThanhToan, ISNULL(h.ngayTao, h.ngayLap)) AS DATE) <= ? ");
            params.add(java.sql.Date.valueOf(den));
        }
    }

    /**
     * Tải chi tiết sản phẩm / dịch vụ của hóa đơn khi người dùng bấm xem chi tiết hoặc in hóa đơn (Lazy Loading).
     */
    public void napChiTietChoHoaDon(HoaDon h) {
        if (h == null || !h.getDanhSachChiTiet().isEmpty()) return;
        try (Connection con = ConnectDB.getConnection()) {
            napChiTiet(con, h);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<HoaDon> layDanhSachHoaDonCho() {
        List<HoaDon> ds = new ArrayList<>();
        String sql = "SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, "
                + "       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, "
                + "       k.hangThe, k.diemTichLuy, "
                + "       ISNULL((SELECT SUM(h2.tongTien) FROM HOADON h2 WHERE h2.maKH = k.maKH AND h2.trangThai = N'Đã thanh toán'), 0) AS tongChiTieu, "
                + "       h.maNV_BH, nv.hoTen AS tenNV, nv.tenDangNhap AS tkNV, nv.maQuyen, nv.soDienThoai AS sdtNV, "
                + "       h.trangThai, h.phuongThuc, h.ngayTao, h.ngayThanhToan, h.maGiamGia, h.tienGiam "
                + "FROM HOADON h "
                + "LEFT JOIN KHACHHANG k ON h.maKH = k.maKH "
                + "LEFT JOIN NHANVIEN nv ON h.maNV_BH = nv.maNV "
                + "WHERE h.trangThai = N'Chưa thanh toán' "
                + "ORDER BY ISNULL(h.ngayTao, GETDATE()) DESC";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HoaDon h = docHoaDon(rs);
                napChiTiet(con, h);
                ds.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public Map<Integer, HoaDon> layCacODangMo() {
        Map<Integer, HoaDon> map = new HashMap<>();
        String sql = "SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, "
                + "       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, "
                + "       k.hangThe, k.diemTichLuy, "
                + "       ISNULL((SELECT SUM(h2.tongTien) FROM HOADON h2 WHERE h2.maKH = k.maKH AND h2.trangThai = N'Đã thanh toán'), 0) AS tongChiTieu, "
                + "       h.maNV_BH, nv.hoTen AS tenNV, nv.tenDangNhap AS tkNV, nv.maQuyen, nv.soDienThoai AS sdtNV, "
                + "       h.trangThai, h.phuongThuc, h.ngayTao, h.ngayThanhToan, h.maGiamGia, h.tienGiam "
                + "FROM HOADON h "
                + "LEFT JOIN KHACHHANG k ON h.maKH = k.maKH "
                + "LEFT JOIN NHANVIEN nv ON h.maNV_BH = nv.maNV "
                + "WHERE h.trangThai = N'Chưa thanh toán' AND h.soO IS NOT NULL";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HoaDon h = docHoaDon(rs);
                napChiTiet(con, h);
                if (h.getSoO() >= 1 && h.getSoO() <= 20) {
                    map.put(h.getSoO() - 1, h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private HoaDon docHoaDon(ResultSet rs) throws SQLException {
        String ma = rs.getString("maHD");
        int soO = rs.getInt("soO");
        if (rs.wasNull()) soO = 0;
        HoaDon h = new HoaDon(ma, soO);

        // 1. Ánh xạ thực thể KhachHang
        String maKH = rs.getString("maKH");
        String tenKhach = rs.getString("tenKhach");
        String sdt = rs.getString("sdt");
        if (maKH != null && !maKH.trim().isEmpty()) {
            String hangThe = rs.getString("hangThe");
            int diem = rs.getInt("diemTichLuy");
            long tongChiTieu = Math.round(rs.getDouble("tongChiTieu"));
            KhachHang kh = new KhachHang(maKH, tenKhach != null ? tenKhach : "",
                    sdt != null ? sdt : "", hangThe != null ? hangThe : "Thường",
                    diem, "", null, tongChiTieu);
            h.setKhachHang(kh);
        } else {
            h.setTenKH(tenKhach == null ? "" : tenKhach);
            h.setSdt(sdt == null ? "" : sdt);
            h.setMaKH("");
        }

        // 2. Ánh xạ thực thể NhanVien
        String maNV = rs.getString("maNV_BH");
        if (maNV != null && !maNV.trim().isEmpty()) {
            String tenNV = rs.getString("tenNV");
            String tkNV = rs.getString("tkNV");
            int maQuyen = rs.getInt("maQuyen");
            String sdtNV = rs.getString("sdtNV");
            NhanVien nv = new NhanVien();
            nv.setMaNV(maNV);
            nv.setHoTen(tenNV != null ? tenNV : "");
            nv.setTenDangNhap(tkNV != null ? tkNV : "");
            nv.setMaQuyen(maQuyen);
            nv.setSoDienThoai(sdtNV != null ? sdtNV : "");
            h.setNhanVien(nv);
        } else {
            h.setMaNV("");
        }

        h.setTrangThai(rs.getString("trangThai") == null ? HoaDon.CHUA_THANH_TOAN : rs.getString("trangThai"));
        h.setPhuongThuc(rs.getString("phuongThuc"));

        Timestamp tsTao = rs.getTimestamp("ngayTao");
        if (tsTao != null) h.setNgayTao(tsTao.toLocalDateTime());
        Timestamp tsTT = rs.getTimestamp("ngayThanhToan");
        if (tsTT != null) h.setNgayThanhToan(tsTT.toLocalDateTime());

        String maGiam = rs.getString("maGiamGia");
        long tienGiam = Math.round(rs.getDouble("tienGiam"));
        if (maGiam != null && !maGiam.trim().isEmpty()) {
            h.setMaGiamGia(new MaGiamGia(maGiam, false, tienGiam, java.time.LocalDate.now().plusYears(1)));
        }

        try {
            double tt = rs.getDouble("tongTien");
            if (!rs.wasNull()) {
                h.setTongTien(tt);
            }
        } catch (SQLException ignored) {
        }
        return h;
    }

    private void napChiTiet(Connection con, HoaDon h) throws SQLException {
        String sql = "SELECT c.soLuong, c.donGia, c.sku, c.maDV, "
                + "       ISNULL(sp.tenSP, dv.tenDV) AS ten, "
                + "       ISNULL(dv.loaiDV, N'Sản phẩm') AS dm, "
                + "       ISNULL(c.isDichVu, CASE WHEN c.maDV IS NOT NULL THEN 1 ELSE 0 END) AS isDV "
                + "FROM CHITIETHOADON c "
                + "LEFT JOIN SANPHAM sp ON c.sku = sp.sku_maSP "
                + "LEFT JOIN DICHVU dv ON c.maDV = dv.maDV "
                + "WHERE c.maHD = ? ORDER BY c.soLuong";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, h.getMaHD());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sku = rs.getString("sku");
                    if (sku == null || sku.trim().isEmpty()) sku = rs.getString("maDV");
                    if (sku == null || sku.trim().isEmpty()) sku = "SP";
                    String ten = rs.getString("ten");
                    if (ten == null) ten = "Sản phẩm";
                    String dm = rs.getString("dm");
                    if (dm == null) dm = "Chung";
                    double gia = rs.getDouble("donGia");
                    boolean isDv = rs.getInt("isDV") == 1;
                    int sl = rs.getInt("soLuong");

                    SanPham spObj = new SanPham(sku, ten, dm, Math.round(gia), isDv);
                    ChiTietHoaDon ct = new ChiTietHoaDon(h, spObj, sl, gia);
                    ct.setSku(isDv ? null : sku);
                    ct.setMaDV(isDv ? sku : null);
                    ct.setDanhMuc(dm);
                    ct.setDichVu(isDv);
                    h.getDanhSachChiTiet().add(ct);
                }
            }
        }
    }

    public void luuHoaDon(HoaDon h) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Kiểm tra xem hóa đơn đã có trong CSDL chưa
                String checkSql = "SELECT COUNT(*) FROM HOADON WHERE maHD = ?";
                boolean exists = false;
                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setString(1, h.getMaHD());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) exists = true;
                    }
                }

                // Lấy mã khách hàng từ thực thể KhachHang hoặc chuỗi lưu trữ
                String maKH = h.getKhachHang() != null && h.getKhachHang().getMaKH() != null
                        ? h.getKhachHang().getMaKH().trim() : (h.getMaKH() != null ? h.getMaKH().trim() : null);
                if (maKH != null && maKH.isEmpty()) maKH = null;

                // Lấy mã nhân viên từ thực thể NhanVien hoặc chuỗi lưu trữ
                String maNV = h.getNhanVien() != null && h.getNhanVien().getMaNV() != null
                        ? h.getNhanVien().getMaNV().trim() : (h.getMaNV() != null ? h.getMaNV().trim() : null);
                if (maNV != null && maNV.isEmpty()) maNV = null;

                // Đảm bảo ràng buộc toàn vẹn khóa ngoại bảng NHANVIEN_BANHANG nếu CSDL yêu cầu
                if (maNV != null) {
                    try (PreparedStatement chk = con.prepareStatement("SELECT 1 FROM NHANVIEN_BANHANG WHERE maNV = ?")) {
                        chk.setString(1, maNV);
                        try (ResultSet rsChk = chk.executeQuery()) {
                            if (!rsChk.next()) maNV = null;
                        }
                    } catch (SQLException ignored) {
                    }
                }

                // Đảm bảo ràng buộc toàn vẹn khóa ngoại bảng KHACHHANG
                if (maKH != null) {
                    try (PreparedStatement chk = con.prepareStatement("SELECT 1 FROM KHACHHANG WHERE maKH = ?")) {
                        chk.setString(1, maKH);
                        try (ResultSet rsChk = chk.executeQuery()) {
                            if (!rsChk.next()) maKH = null;
                        }
                    } catch (SQLException ignored) {
                    }
                }

                if (!exists) {
                    String insSql = "INSERT INTO HOADON (maHD, ngayLap, tongTien, maKH, soO, trangThai, "
                            + "phuongThuc, ngayTao, ngayThanhToan, tienGiam, maGiamGia, tenKH, soDienThoai, maNV_BH) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(insSql)) {
                        ps.setString(1, h.getMaHD());
                        ps.setDate(2, Date.valueOf(h.getNgayTao().toLocalDate()));
                        ps.setDouble(3, h.canThanhToan());
                        ps.setString(4, maKH);
                        if (h.getSoO() > 0) ps.setInt(5, h.getSoO());
                        else ps.setNull(5, java.sql.Types.INTEGER);
                        ps.setString(6, h.getTrangThai());
                        ps.setString(7, h.getPhuongThuc());
                        ps.setTimestamp(8, Timestamp.valueOf(h.getNgayTao()));
                        ps.setTimestamp(9, h.getNgayThanhToan() != null ? Timestamp.valueOf(h.getNgayThanhToan()) : null);
                        ps.setDouble(10, h.tienGiam());
                        ps.setString(11, h.getMaGiamGia() != null ? h.getMaGiamGia().getMa() : null);
                        ps.setString(12, h.getTenKH());
                        ps.setString(13, h.getSdt());
                        ps.setString(14, maNV);
                        ps.executeUpdate();
                    }
                } else {
                    String upSql = "UPDATE HOADON SET tongTien = ?, maKH = ?, soO = ?, trangThai = ?, "
                            + "phuongThuc = ?, ngayThanhToan = ?, tienGiam = ?, maGiamGia = ?, "
                            + "tenKH = ?, soDienThoai = ?, maNV_BH = ? WHERE maHD = ?";
                    try (PreparedStatement ps = con.prepareStatement(upSql)) {
                        ps.setDouble(1, h.canThanhToan());
                        ps.setString(2, maKH);
                        if (h.getSoO() > 0) ps.setInt(3, h.getSoO());
                        else ps.setNull(3, java.sql.Types.INTEGER);
                        ps.setString(4, h.getTrangThai());
                        ps.setString(5, h.getPhuongThuc());
                        ps.setTimestamp(6, h.getNgayThanhToan() != null ? Timestamp.valueOf(h.getNgayThanhToan()) : null);
                        ps.setDouble(7, h.tienGiam());
                        ps.setString(8, h.getMaGiamGia() != null ? h.getMaGiamGia().getMa() : null);
                        ps.setString(9, h.getTenKH());
                        ps.setString(10, h.getSdt());
                        ps.setString(11, maNV);
                        ps.setString(12, h.getMaHD());
                        ps.executeUpdate();
                    }
                }

                // Cập nhật danh sách ChiTietHoaDon tương ứng bảng CHITIETHOADON
                try (PreparedStatement del = con.prepareStatement("DELETE FROM CHITIETHOADON WHERE maHD = ?")) {
                    del.setString(1, h.getMaHD());
                    del.executeUpdate();
                }

                String insCt = "INSERT INTO CHITIETHOADON (soLuong, donGia, thanhTien, maHD, sku, maDV, tenSP, danhMuc, isDichVu) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insCt)) {
                    for (ChiTietHoaDon d : h.getDanhSachChiTiet()) {
                        ps.setInt(1, d.getSoLuong());
                        ps.setDouble(2, d.getDonGia());
                        ps.setDouble(3, d.getSoLuong() * d.getDonGia());
                        ps.setString(4, h.getMaHD());
                        boolean isDv = d.isDichVu();
                        String sku = d.getSku();
                        if (sku == null && d.getSanPham() != null && !isDv) sku = d.getSanPham().getSku();
                        String maDV = d.getMaDV();
                        if (maDV == null && d.getSanPham() != null && isDv) maDV = d.getSanPham().getSku();

                        ps.setString(5, isDv ? null : sku);
                        ps.setString(6, isDv ? (maDV != null ? maDV : sku) : null);
                        ps.setString(7, d.getSanPham() != null ? d.getSanPham().getTen() : "");
                        ps.setString(8, d.getDanhMuc() != null ? d.getDanhMuc() : (d.getSanPham() != null ? d.getSanPham().getDanhMuc() : ""));
                        ps.setInt(9, isDv ? 1 : 0);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void thanhToan(HoaDon h) throws SQLException {
        h.setTrangThai(HoaDon.DA_THANH_TOAN);
        luuHoaDon(h);
        String sql = "UPDATE HOADON SET trangThai = N'Đã thanh toán', soO = NULL, phuongThuc = ?, ngayThanhToan = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, h.getPhuongThuc());
            ps.setTimestamp(2, Timestamp.valueOf(h.getNgayThanhToan() != null ? h.getNgayThanhToan() : LocalDateTime.now()));
            ps.setString(3, h.getMaHD());
            ps.executeUpdate();

            // Tích điểm cho khách hàng (1 điểm cho mỗi 10.000₫ thanh toán)
            String maKH = h.getKhachHang() != null && h.getKhachHang().getMaKH() != null
                    ? h.getKhachHang().getMaKH().trim() : (h.getMaKH() != null ? h.getMaKH().trim() : null);
            if (maKH != null && !maKH.isEmpty()) {
                KhachHangDAO.tichDiem(con, maKH, (long) h.canThanhToan());
            }
        }
    }

    public void xoa(String maHD) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement delCt = con.prepareStatement("DELETE FROM CHITIETHOADON WHERE maHD = ?")) {
                    delCt.setString(1, maHD);
                    delCt.executeUpdate();
                }
                try (PreparedStatement delHd = con.prepareStatement("DELETE FROM HOADON WHERE maHD = ? AND trangThai = N'Chưa thanh toán'")) {
                    delHd.setString(1, maHD);
                    delHd.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public int laySoHoaDonTiepTheo() {
        int max = 0;
        String sql = "SELECT maHD FROM HOADON WHERE maHD LIKE 'HD%'";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String m = rs.getString(1);
                if (m != null && m.length() > 2) {
                    try {
                        int num = Integer.parseInt(m.substring(2).trim());
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return max + 1;
    }
}
