package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.CaPhauThuat;

/**
 * Quan ly ca phau thuat thu cung (bang PHAUTHUAT) trong SQL Server.
 */
public class PhauThuatDAO {

    public PhauThuatDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PHAUTHUAT') "
                    + "CREATE TABLE PHAUTHUAT ("
                    + "  maCa NVARCHAR(20) PRIMARY KEY, "
                    + "  tenThuCung NVARCHAR(100) NOT NULL, "
                    + "  loaiThuCung NVARCHAR(100) NULL, "
                    + "  tenChu NVARCHAR(100) NOT NULL, "
                    + "  sdt NVARCHAR(20) NOT NULL, "
                    + "  loaiPhauThuat NVARCHAR(100) NOT NULL, "
                    + "  ngayMo NVARCHAR(20) NULL, "
                    + "  gioMo NVARCHAR(20) NULL, "
                    + "  bacSiChinh NVARCHAR(100) NULL, "
                    + "  chiPhi BIGINT DEFAULT 0, "
                    + "  trangThai NVARCHAR(50) DEFAULT N'Ch\u1edd ph\u1eabu thu\u1eadt', "
                    + "  ghiChu NVARCHAR(500) NULL, "
                    + "  petID NVARCHAR(20) NULL, "
                    + "  maKH NVARCHAR(20) NULL"
                    + ")");
        } catch (SQLException ignored) {
        }
    }

    public List<CaPhauThuat> layTatCa() {
        List<CaPhauThuat> ds = new ArrayList<>();
        String sql = "SELECT maCa, tenThuCung, loaiThuCung, tenChu, sdt, loaiPhauThuat, "
                + "ngayMo, gioMo, bacSiChinh, chiPhi, trangThai, ghiChu, petID, maKH FROM PHAUTHUAT ORDER BY maCa DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new CaPhauThuat(
                        rs.getString("maCa"),
                        rs.getString("tenThuCung"),
                        rs.getString("loaiThuCung"),
                        rs.getString("tenChu"),
                        rs.getString("sdt"),
                        rs.getString("loaiPhauThuat"),
                        rs.getString("ngayMo"),
                        rs.getString("gioMo"),
                        rs.getString("bacSiChinh"),
                        rs.getLong("chiPhi"),
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

    public void them(CaPhauThuat cpt) throws SQLException {
        String sql = "INSERT INTO PHAUTHUAT (maCa, tenThuCung, loaiThuCung, tenChu, sdt, loaiPhauThuat, ngayMo, gioMo, bacSiChinh, chiPhi, trangThai, ghiChu, petID, maKH) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cpt.getMaCa());
            ps.setString(2, cpt.getTenThuCung());
            ps.setString(3, cpt.getLoaiThuCung());
            ps.setString(4, cpt.getTenChu());
            ps.setString(5, cpt.getSdt());
            ps.setString(6, cpt.getLoaiPhauThuat());
            ps.setString(7, cpt.getNgayMo());
            ps.setString(8, cpt.getGioMo());
            ps.setString(9, cpt.getBacSiChinh());
            ps.setLong(10, cpt.getChiPhi());
            ps.setString(11, cpt.getTrangThai() != null ? cpt.getTrangThai() : "Ch\u1edd ph\u1eabu thu\u1eadt");
            ps.setString(12, cpt.getGhiChu());
            ps.setString(13, cpt.getPetID());
            ps.setString(14, cpt.getMaKH());
            ps.executeUpdate();
        }
    }

    public void capNhat(CaPhauThuat cpt) throws SQLException {
        String sql = "UPDATE PHAUTHUAT SET tenThuCung = ?, loaiThuCung = ?, tenChu = ?, sdt = ?, "
                + "loaiPhauThuat = ?, ngayMo = ?, gioMo = ?, bacSiChinh = ?, chiPhi = ?, trangThai = ?, ghiChu = ?, "
                + "petID = ?, maKH = ? WHERE maCa = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cpt.getTenThuCung());
            ps.setString(2, cpt.getLoaiThuCung());
            ps.setString(3, cpt.getTenChu());
            ps.setString(4, cpt.getSdt());
            ps.setString(5, cpt.getLoaiPhauThuat());
            ps.setString(6, cpt.getNgayMo());
            ps.setString(7, cpt.getGioMo());
            ps.setString(8, cpt.getBacSiChinh());
            ps.setLong(9, cpt.getChiPhi());
            ps.setString(10, cpt.getTrangThai());
            ps.setString(11, cpt.getGhiChu());
            ps.setString(12, cpt.getPetID());
            ps.setString(13, cpt.getMaKH());
            ps.setString(14, cpt.getMaCa());
            ps.executeUpdate();
        }
    }

    public void xoa(String maCa) throws SQLException {
        String sql = "DELETE FROM PHAUTHUAT WHERE maCa = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCa);
            ps.executeUpdate();
        }
    }

    public String maTiepTheo() {
        int max = 0;
        String sql = "SELECT maCa FROM PHAUTHUAT WHERE maCa LIKE 'PT-%'";
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
        return String.format("PT-%03d", max + 1);
    }
}
