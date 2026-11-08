package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ConnectDB.ConnectDB;
import Entity.DongGioHang;
import Entity.HoaDon;
import Entity.MaGiamGia;
import Entity.SanPham;

/**
 * Xử lý truy xuất và lưu trữ hóa đơn trực tiếp vào SQL Server (HOADON, CHITIETHOADON).
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

            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'tenSP') IS NULL ALTER TABLE CHITIETHOADON ADD tenSP NVARCHAR(150) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'danhMuc') IS NULL ALTER TABLE CHITIETHOADON ADD danhMuc NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'isDichVu') IS NULL ALTER TABLE CHITIETHOADON ADD isDichVu BIT DEFAULT 0");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'sku') IS NULL ALTER TABLE CHITIETHOADON ADD sku NVARCHAR(50) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'maDV') IS NULL ALTER TABLE CHITIETHOADON ADD maDV NVARCHAR(50) NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'thanhTien') IS NULL ALTER TABLE CHITIETHOADON ADD thanhTien FLOAT NULL");
            st.executeUpdate("IF COL_LENGTH('CHITIETHOADON', 'sku_maSP') IS NOT NULL EXEC('UPDATE CHITIETHOADON SET sku = sku_maSP WHERE sku IS NULL AND sku_maSP IS NOT NULL')");
        } catch (SQLException ignored) {
        }
    }

    public List<HoaDon> layLichSu() {
        List<HoaDon> ds = new ArrayList<>();
        String sql = "SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, "
                + "       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, "
                + "       h.trangThai, h.phuongThuc, h.ngayTao, h.ngayThanhToan, h.maGiamGia, h.tienGiam "
                + "FROM HOADON h "
                + "LEFT JOIN KHACHHANG k ON h.maKH = k.maKH "
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

    public Map<Integer, HoaDon> layCacODangMo() {
        Map<Integer, HoaDon> map = new HashMap<>();
        String sql = "SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, "
                + "       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, "
                + "       h.trangThai, h.phuongThuc, h.ngayTao, h.ngayThanhToan, h.maGiamGia, h.tienGiam "
                + "FROM HOADON h "
                + "LEFT JOIN KHACHHANG k ON h.maKH = k.maKH "
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
        h.setTenKH(rs.getString("tenKhach") == null ? "" : rs.getString("tenKhach"));
        h.setSdt(rs.getString("sdt") == null ? "" : rs.getString("sdt"));
        h.setMaKH(rs.getString("maKH") == null ? "" : rs.getString("maKH"));
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
        return h;
    }

    private void napChiTiet(Connection con, HoaDon h) {
        String sql = "SELECT c.maCTHD, c.soLuong, c.donGia, c.thanhTien, c.sku, c.maDV, "
                + "       ISNULL(c.tenSP, ISNULL(sp.tenSP, dv.tenDV)) AS ten, "
                + "       ISNULL(c.danhMuc, ISNULL(dv.loaiDV, N'Sản phẩm')) AS dm, "
                + "       ISNULL(c.isDichVu, CASE WHEN c.maDV IS NOT NULL THEN 1 ELSE 0 END) AS isDV "
                + "FROM CHITIETHOADON c "
                + "LEFT JOIN SANPHAM sp ON c.sku = sp.sku_maSP "
                + "LEFT JOIN DICHVU dv ON c.maDV = dv.maDV "
                + "WHERE c.maHD = ? ORDER BY c.maCTHD";

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
                    long gia = Math.round(rs.getDouble("donGia"));
                    boolean isDv = rs.getInt("isDV") == 1;
                    int sl = rs.getInt("soLuong");

                    SanPham spObj = new SanPham(sku, ten, dm, gia, isDv);
                    h.getDong().add(new DongGioHang(spObj, sl));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

                // Đảm bảo maKH tồn tại trong bảng KHACHHANG để không vi phạm ràng buộc ngoại FK_HD_KH
                String maKH = (h.getMaKH() != null && !h.getMaKH().trim().isEmpty()) ? h.getMaKH() : null;
                if (maKH != null) {
                    try (PreparedStatement chk = con.prepareStatement("SELECT 1 FROM KHACHHANG WHERE maKH = ?")) {
                        chk.setString(1, maKH);
                        try (ResultSet rsChk = chk.executeQuery()) {
                            if (!rsChk.next()) {
                                // Không tìm thấy maKH trong CSDL, thử tra cứu theo số điện thoại
                                String sdtChiSo = h.getSdt() == null ? "" : h.getSdt().replaceAll("\\D", "");
                                String foundMaKH = null;
                                if (!sdtChiSo.isEmpty()) {
                                    try (PreparedStatement chkSdt = con.prepareStatement(
                                            "SELECT maKH FROM KHACHHANG WHERE REPLACE(soDienThoai, ' ', '') = ?")) {
                                        chkSdt.setString(1, sdtChiSo);
                                        try (ResultSet rsSdt = chkSdt.executeQuery()) {
                                            if (rsSdt.next()) foundMaKH = rsSdt.getString(1);
                                        }
                                    }
                                }
                                if (foundMaKH == null && h.getTenKH() != null && !h.getTenKH().trim().isEmpty()) {
                                    try {
                                        foundMaKH = new KhachHangDAO().them(h.getTenKH().trim(), sdtChiSo, 0, new java.util.ArrayList<>());
                                    } catch (Exception ignored) {}
                                }
                                maKH = foundMaKH;
                                h.setMaKH(maKH);
                            }
                        }
                    }
                }

                if (!exists) {
                    String insSql = "INSERT INTO HOADON (maHD, ngayLap, tongTien, maKH, soO, trangThai, "
                            + "phuongThuc, ngayTao, ngayThanhToan, tienGiam, maGiamGia, tenKH, soDienThoai) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(insSql)) {
                        ps.setString(1, h.getMaHD());
                        ps.setDate(2, Date.valueOf(h.getNgayTao().toLocalDate()));
                        ps.setDouble(3, h.canThanhToan());
                        ps.setString(4, maKH);
                        ps.setInt(5, h.getSoO());
                        ps.setString(6, h.getTrangThai());
                        ps.setString(7, h.getPhuongThuc());
                        ps.setTimestamp(8, Timestamp.valueOf(h.getNgayTao()));
                        ps.setTimestamp(9, h.getNgayThanhToan() != null ? Timestamp.valueOf(h.getNgayThanhToan()) : null);
                        ps.setDouble(10, h.tienGiam());
                        ps.setString(11, h.getMaGiamGia() != null ? h.getMaGiamGia().getMa() : null);
                        ps.setString(12, h.getTenKH());
                        ps.setString(13, h.getSdt());
                        ps.executeUpdate();
                    }
                } else {
                    String upSql = "UPDATE HOADON SET tongTien = ?, maKH = ?, soO = ?, trangThai = ?, "
                            + "phuongThuc = ?, ngayThanhToan = ?, tienGiam = ?, maGiamGia = ?, "
                            + "tenKH = ?, soDienThoai = ? WHERE maHD = ?";
                    try (PreparedStatement ps = con.prepareStatement(upSql)) {
                        ps.setDouble(1, h.canThanhToan());
                        ps.setString(2, maKH);
                        ps.setInt(3, h.getSoO());
                        ps.setString(4, h.getTrangThai());
                        ps.setString(5, h.getPhuongThuc());
                        ps.setTimestamp(6, h.getNgayThanhToan() != null ? Timestamp.valueOf(h.getNgayThanhToan()) : null);
                        ps.setDouble(7, h.tienGiam());
                        ps.setString(8, h.getMaGiamGia() != null ? h.getMaGiamGia().getMa() : null);
                        ps.setString(9, h.getTenKH());
                        ps.setString(10, h.getSdt());
                        ps.setString(11, h.getMaHD());
                        ps.executeUpdate();
                    }
                }

                // Cập nhật chi tiết hóa đơn
                try (PreparedStatement del = con.prepareStatement("DELETE FROM CHITIETHOADON WHERE maHD = ?")) {
                    del.setString(1, h.getMaHD());
                    del.executeUpdate();
                }

                String insCt = "INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV, tenSP, danhMuc, isDichVu) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insCt)) {
                    int stt = 1;
                    for (DongGioHang d : h.getDong()) {
                        String ctId = h.getMaHD() + "-CT" + String.format("%02d", stt++);
                        ps.setString(1, ctId);
                        ps.setInt(2, d.getSoLuong());
                        ps.setDouble(3, d.getSanPham().getGia());
                        ps.setDouble(4, d.thanhTien());
                        ps.setString(5, h.getMaHD());
                        boolean isDv = d.getSanPham().isDichVu();
                        ps.setString(6, isDv ? null : d.getSanPham().getSku());
                        ps.setString(7, isDv ? d.getSanPham().getSku() : null);
                        ps.setString(8, d.getSanPham().getTen());
                        ps.setString(9, d.getSanPham().getDanhMuc());
                        ps.setInt(10, isDv ? 1 : 0);
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
        luuHoaDon(h);
        String sql = "UPDATE HOADON SET trangThai = N'Đã thanh toán', soO = NULL, phuongThuc = ?, ngayThanhToan = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, h.getPhuongThuc());
            ps.setTimestamp(2, Timestamp.valueOf(h.getNgayThanhToan() != null ? h.getNgayThanhToan() : LocalDateTime.now()));
            ps.setString(3, h.getMaHD());
            ps.executeUpdate();
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
