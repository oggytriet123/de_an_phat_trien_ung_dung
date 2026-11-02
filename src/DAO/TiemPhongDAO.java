package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.PhieuTiem;

/**
 * Quan ly tiem phong vaccine thu cung (bang TIEMPHONG) trong SQL Server.
 */
public class TiemPhongDAO {

    public TiemPhongDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'TIEMPHONG') "
                    + "CREATE TABLE TIEMPHONG ("
                    + "  maPhieu NVARCHAR(20) PRIMARY KEY, "
                    + "  tenThuCung NVARCHAR(100) NOT NULL, "
                    + "  loaiThuCung NVARCHAR(100) NULL, "
                    + "  tenChu NVARCHAR(100) NOT NULL, "
                    + "  sdt NVARCHAR(20) NOT NULL, "
                    + "  loaiVaccine NVARCHAR(100) NOT NULL, "
                    + "  ngayTiem NVARCHAR(20) NULL, "
                    + "  ngayHenNhac NVARCHAR(20) NULL, "
                    + "  bacSi NVARCHAR(100) NULL, "
                    + "  trangThai NVARCHAR(30) DEFAULT N'Ch\u1edd ti\u00eam', "
                    + "  ghiChu NVARCHAR(500) NULL, "
                    + "  petID NVARCHAR(20) NULL, "
                    + "  maKH NVARCHAR(20) NULL"
                    + ")");
        } catch (SQLException ignored) {
        }
    }

    public List<PhieuTiem> layTatCa() {
        List<PhieuTiem> ds = new ArrayList<>();
        String sql = "SELECT maPhieu, tenThuCung, loaiThuCung, tenChu, sdt, loaiVaccine, "
                + "ngayTiem, ngayHenNhac, bacSi, trangThai, ghiChu, petID, maKH FROM TIEMPHONG ORDER BY maPhieu DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new PhieuTiem(
                        rs.getString("maPhieu"),
                        rs.getString("tenThuCung"),
                        rs.getString("loaiThuCung"),
                        rs.getString("tenChu"),
                        rs.getString("sdt"),
                        rs.getString("loaiVaccine"),
                        rs.getString("ngayTiem"),
                        rs.getString("ngayHenNhac"),
                        rs.getString("bacSi"),
                        rs.getString("trangThai"),
                        rs.getString("ghiChu"),
                        rs.getString("petID"),
                        rs.getString("maKH")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public void them(PhieuTiem pt) throws SQLException {
        String sql = "INSERT INTO TIEMPHONG (maPhieu, tenThuCung, loaiThuCung, tenChu, sdt, loaiVaccine, ngayTiem, ngayHenNhac, bacSi, trangThai, ghiChu, petID, maKH) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pt.getMaPhieu());
            ps.setString(2, pt.getTenThuCung());
            ps.setString(3, pt.getLoaiThuCung());
            ps.setString(4, pt.getTenChu());
            ps.setString(5, pt.getSdt());
            ps.setString(6, pt.getLoaiVaccine());
            ps.setString(7, pt.getNgayTiem());
            ps.setString(8, pt.getNgayHenNhac());
            ps.setString(9, pt.getBacSi());
            ps.setString(10, pt.getTrangThai() != null ? pt.getTrangThai() : "Ch\u1edd ti\u00eam");
            ps.setString(11, pt.getGhiChu());
            ps.setString(12, pt.getPetID());
            ps.setString(13, pt.getMaKH());
            ps.executeUpdate();
        }
    }

    public void capNhat(PhieuTiem pt) throws SQLException {
        String sql = "UPDATE TIEMPHONG SET tenThuCung = ?, loaiThuCung = ?, tenChu = ?, sdt = ?, "
                + "loaiVaccine = ?, ngayTiem = ?, ngayHenNhac = ?, bacSi = ?, trangThai = ?, ghiChu = ?, "
                + "petID = ?, maKH = ? WHERE maPhieu = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pt.getTenThuCung());
            ps.setString(2, pt.getLoaiThuCung());
            ps.setString(3, pt.getTenChu());
            ps.setString(4, pt.getSdt());
            ps.setString(5, pt.getLoaiVaccine());
            ps.setString(6, pt.getNgayTiem());
            ps.setString(7, pt.getNgayHenNhac());
            ps.setString(8, pt.getBacSi());
            ps.setString(9, pt.getTrangThai());
            ps.setString(10, pt.getGhiChu());
            ps.setString(11, pt.getPetID());
            ps.setString(12, pt.getMaKH());
            ps.setString(13, pt.getMaPhieu());
            ps.executeUpdate();
        }
    }

    public void xoa(String maPhieu) throws SQLException {
        String sql = "DELETE FROM TIEMPHONG WHERE maPhieu = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            ps.executeUpdate();
        }
    }

    public String maTiepTheo() {
        int max = 0;
        String sql = "SELECT maPhieu FROM TIEMPHONG WHERE maPhieu LIKE 'TP-%'";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String m = rs.getString(1);
                if (m != null && m.length() > 3) {
                    try {
                        int num = Integer.parseInt(m.substring(3).trim());
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException ignored) {}
        return String.format("TP-%03d", max + 1);
    }
}
