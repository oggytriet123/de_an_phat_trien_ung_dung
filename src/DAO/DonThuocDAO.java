package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.ChiTietDonThuoc;
import Entity.DonThuoc;

/**
 * Quản lý dữ liệu Đơn thuốc (bảng DONTHUOC và CHITIET_DONTHUOC) trong SQL Server.
 */
public class DonThuocDAO {

    public List<DonThuoc> layTatCa() {
        List<DonThuoc> ds = new ArrayList<>();
        String sql = "SELECT maDonThuoc, maThuCung, tenThuCung, loaiThuCung, giongLoai, canNang, "
                + "       tenChuNuoi, soDienThoai, bacSi, ngayKe, gioKe, chanDoan, trieuChung, "
                + "       trangThai, loiDan, ngayTaiKham, maPhacDo, nhatKyLog "
                + "FROM DONTHUOC ORDER BY ngayKe DESC, maDonThuoc DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String maDT = rs.getString("maDonThuoc");
                String maTC = rs.getString("maThuCung");
                String tenTC = rs.getString("tenThuCung");
                String loai = rs.getString("loaiThuCung");
                String giong = rs.getString("giongLoai");
                double canNang = rs.getDouble("canNang");
                String tenChu = rs.getString("tenChuNuoi");
                String sdt = rs.getString("soDienThoai");
                String bacSi = rs.getString("bacSi");
                Date ngayKeSql = rs.getDate("ngayKe");
                LocalDate ngayKe = ngayKeSql != null ? ngayKeSql.toLocalDate() : LocalDate.now();
                String gioStr = rs.getString("gioKe");
                LocalTime gioKe = LocalTime.now();
                if (gioStr != null && !gioStr.trim().isEmpty()) {
                    try { gioKe = LocalTime.parse(gioStr.trim()); } catch (Exception ignored) {}
                }
                String chanDoan = rs.getString("chanDoan");
                String trieuChung = rs.getString("trieuChung");
                String trangThai = rs.getString("trangThai");
                String loiDan = rs.getString("loiDan");
                Date ngayTaiKhamSql = rs.getDate("ngayTaiKham");
                LocalDate ngayTaiKham = ngayTaiKhamSql != null ? ngayTaiKhamSql.toLocalDate() : null;
                String maPD = rs.getString("maPhacDo");

                DonThuoc dt = new DonThuoc(maDT, maTC, tenTC, loai, giong, canNang, tenChu, sdt, bacSi,
                        ngayKe, gioKe, chanDoan, trieuChung, trangThai, loiDan, ngayTaiKham, maPD);

                String log = rs.getString("nhatKyLog");
                if (log != null && !log.trim().isEmpty()) {
                    String[] lines = log.split("\\|");
                    for (String l : lines) if (!l.trim().isEmpty()) dt.addLichSuRaw(l.trim());
                }
                ds.add(dt);
            }
        } catch (SQLException e) {
            // Khi ngắt kết nối SQL thì trả về danh sách rỗng, không tự tạo data ảo
            return ds;
        }

        // Đọc chi tiết thuốc
        String sqlCT = "SELECT maDonThuoc, maSKU, tenThuoc, donViTinh, soLuong, donGia, huongDan "
                + "FROM CHITIET_DONTHUOC ORDER BY id ASC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sqlCT)) {
            while (rs.next()) {
                String maDT = rs.getString("maDonThuoc");
                for (DonThuoc dt : ds) {
                    if (dt.getMaDonThuoc().equalsIgnoreCase(maDT)) {
                        dt.themChiTiet(new ChiTietDonThuoc(
                                rs.getString("maSKU"),
                                rs.getString("tenThuoc"),
                                rs.getString("donViTinh"),
                                rs.getInt("soLuong"),
                                rs.getLong("donGia"),
                                rs.getString("huongDan")
                        ));
                        break;
                    }
                }
            }
        } catch (SQLException ignored) {}

        return ds;
    }

    public void them(DonThuoc dt) throws SQLException {
        String sql = "INSERT INTO DONTHUOC (maDonThuoc, maThuCung, tenThuCung, loaiThuCung, giongLoai, canNang, "
                + "  tenChuNuoi, soDienThoai, bacSi, ngayKe, gioKe, chanDoan, trieuChung, trangThai, loiDan, "
                + "  ngayTaiKham, maPhacDo, nhatKyLog) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dt.getMaDonThuoc());
            ps.setString(2, dt.getMaTC());
            ps.setString(3, dt.getTenThuCung());
            ps.setString(4, dt.getLoai());
            ps.setString(5, dt.getGiong());
            ps.setDouble(6, dt.getCanNang());
            ps.setString(7, dt.getTenChu());
            ps.setString(8, dt.getSdtChu());
            ps.setString(9, dt.getBacSi());
            ps.setDate(10, dt.getNgayKe() != null ? Date.valueOf(dt.getNgayKe()) : Date.valueOf(LocalDate.now()));
            ps.setString(11, dt.getGioKe() != null ? dt.getGioKe().toString() : "08:00");
            ps.setString(12, dt.getChanDoan());
            ps.setString(13, dt.getTrieuChung());
            ps.setString(14, dt.getTrangThai());
            ps.setString(15, dt.getLoiDan());
            ps.setDate(16, dt.getNgayTaiKham() != null ? Date.valueOf(dt.getNgayTaiKham()) : null);
            ps.setString(17, dt.getMaPhacDo());
            ps.setString(18, String.join("|", dt.getLichSu()));
            ps.executeUpdate();
        }

        luuChiTiet(dt);
    }

    public void capNhat(DonThuoc dt) throws SQLException {
        String sql = "UPDATE DONTHUOC SET maThuCung = ?, tenThuCung = ?, loaiThuCung = ?, giongLoai = ?, canNang = ?, "
                + "  tenChuNuoi = ?, soDienThoai = ?, bacSi = ?, ngayKe = ?, gioKe = ?, chanDoan = ?, trieuChung = ?, "
                + "  trangThai = ?, loiDan = ?, ngayTaiKham = ?, maPhacDo = ?, nhatKyLog = ? "
                + "WHERE maDonThuoc = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dt.getMaTC());
            ps.setString(2, dt.getTenThuCung());
            ps.setString(3, dt.getLoai());
            ps.setString(4, dt.getGiong());
            ps.setDouble(5, dt.getCanNang());
            ps.setString(6, dt.getTenChu());
            ps.setString(7, dt.getSdtChu());
            ps.setString(8, dt.getBacSi());
            ps.setDate(9, dt.getNgayKe() != null ? Date.valueOf(dt.getNgayKe()) : Date.valueOf(LocalDate.now()));
            ps.setString(10, dt.getGioKe() != null ? dt.getGioKe().toString() : "08:00");
            ps.setString(11, dt.getChanDoan());
            ps.setString(12, dt.getTrieuChung());
            ps.setString(13, dt.getTrangThai());
            ps.setString(14, dt.getLoiDan());
            ps.setDate(15, dt.getNgayTaiKham() != null ? Date.valueOf(dt.getNgayTaiKham()) : null);
            ps.setString(16, dt.getMaPhacDo());
            ps.setString(17, String.join("|", dt.getLichSu()));
            ps.setString(18, dt.getMaDonThuoc());
            ps.executeUpdate();
        }

        luuChiTiet(dt);
    }

    public void xoa(String maDT) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM CHITIET_DONTHUOC WHERE maDonThuoc = ?")) {
                ps.setString(1, maDT);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM DONTHUOC WHERE maDonThuoc = ?")) {
                ps.setString(1, maDT);
                ps.executeUpdate();
            }
        }
    }

    private void luuChiTiet(DonThuoc dt) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            try (PreparedStatement del = con.prepareStatement("DELETE FROM CHITIET_DONTHUOC WHERE maDonThuoc = ?")) {
                del.setString(1, dt.getMaDonThuoc());
                del.executeUpdate();
            }
            if (dt.getDanhSachThuoc() != null && !dt.getDanhSachThuoc().isEmpty()) {
                String ins = "INSERT INTO CHITIET_DONTHUOC (maDonThuoc, maSKU, tenThuoc, donViTinh, soLuong, donGia, huongDan) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(ins)) {
                    for (ChiTietDonThuoc ct : dt.getDanhSachThuoc()) {
                        ps.setString(1, dt.getMaDonThuoc());
                        ps.setString(2, ct.getMaSKU());
                        ps.setString(3, ct.getTenThuoc());
                        ps.setString(4, ct.getDonViTinh());
                        ps.setInt(5, ct.getSoLuong());
                        ps.setLong(6, ct.getDonGia());
                        ps.setString(7, ct.getCachDung());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }
}
