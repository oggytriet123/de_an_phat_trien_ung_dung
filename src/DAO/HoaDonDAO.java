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
        } catch (SQLException ignored) {
        }
    }

    public List<HoaDon> layLichSu() {
        List<HoaDon> ds = new ArrayList<>();
        String sql = "SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, "
                + "       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, "
                + "       k.hangThe, k.diemTichLuy, k.tongChiTieu, "
                + "       h.maNV_BH, nv.hoTen AS tenNV, nv.tenDangNhap AS tkNV, nv.maQuyen, nv.soDienThoai AS sdtNV, "
                + "       h.trangThai, h.phuongThuc, h.ngayTao, h.ngayThanhToan, h.maGiamGia, h.tienGiam "
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

    public Map<Integer, HoaDon> layCacODangMo() {
        Map<Integer, HoaDon> map = new HashMap<>();
        String sql = "SELECT h.maHD, h.soO, ISNULL(h.tenKH, k.hoTen) AS tenKhach, "
                + "       ISNULL(h.soDienThoai, k.soDienThoai) AS sdt, h.maKH, "
                + "       k.hangThe, k.diemTichLuy, k.tongChiTieu, "
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
        return h;
    }

    private void napChiTiet(Connection con, HoaDon h) throws SQLException {
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
                    String maCTHD = rs.getString("maCTHD");
                    String sku = rs.getString("sku");
                    if (sku == null || sku.trim().isEmpty()) sku = rs.getString("maDV");
                    if (sku == null || sku.trim().isEmpty()) sku = "SP";
                    String ten = rs.getString("ten");
                    if (ten == null) ten = "Sản phẩm";
                    String dm = rs.getString("dm");
                    if (dm == null) dm = "Chung";
                    double gia = rs.getDouble("donGia");
                    double thanhTien = rs.getDouble("thanhTien");
                    boolean isDv = rs.getInt("isDV") == 1;
                    int sl = rs.getInt("soLuong");

                    SanPham spObj = new SanPham(sku, ten, dm, Math.round(gia), isDv);
                    ChiTietHoaDon ct = new ChiTietHoaDon(maCTHD, h, spObj, sl, gia, thanhTien > 0 ? thanhTien : gia * sl);
                    ct.setSku(isDv ? null : sku);
                    ct.setMaDV(isDv ? sku : null);
                    ct.setTenSP(ten);
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
                        ps.setInt(5, h.getSoO());
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
                        ps.setInt(3, h.getSoO());
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

                String insCt = "INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV, tenSP, danhMuc, isDichVu) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insCt)) {
                    int stt = 1;
                    for (ChiTietHoaDon d : h.getDanhSachChiTiet()) {
                        String ctId = d.getMaCTHD();
                        if (ctId == null || ctId.trim().isEmpty()) {
                            ctId = h.getMaHD() + "-CT" + String.format("%02d", stt);
                            d.setMaCTHD(ctId);
                        }
                        stt++;
                        ps.setString(1, ctId);
                        ps.setInt(2, d.getSoLuong());
                        ps.setDouble(3, d.getDonGia());
                        ps.setDouble(4, d.thanhTien());
                        ps.setString(5, h.getMaHD());
                        boolean isDv = d.isDichVu();
                        String sku = d.getSku();
                        if (sku == null && d.getSanPham() != null && !isDv) sku = d.getSanPham().getSku();
                        String maDV = d.getMaDV();
                        if (maDV == null && d.getSanPham() != null && isDv) maDV = d.getSanPham().getSku();

                        ps.setString(6, isDv ? null : sku);
                        ps.setString(7, isDv ? (maDV != null ? maDV : sku) : null);
                        ps.setString(8, d.getTenSP() != null ? d.getTenSP() : (d.getSanPham() != null ? d.getSanPham().getTen() : ""));
                        ps.setString(9, d.getDanhMuc() != null ? d.getDanhMuc() : (d.getSanPham() != null ? d.getSanPham().getDanhMuc() : ""));
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
        h.setTrangThai(HoaDon.DA_THANH_TOAN);
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
