package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.DoiTac;

/**
 * Quản lý danh sách Đối tác & Nhà cung cấp (bảng DOITAC) trong SQL Server.
 */
public class DoiTacDAO {

    public DoiTacDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DOITAC') "
                    + "CREATE TABLE DOITAC ("
                    + "  maDT NVARCHAR(20) PRIMARY KEY, "
                    + "  tenDT NVARCHAR(150) NOT NULL, "
                    + "  loaiDT NVARCHAR(50) DEFAULT N'Nhà cung cấp', "
                    + "  soDienThoai NVARCHAR(20) NULL, "
                    + "  diaChi NVARCHAR(200) NULL, "
                    + "  email NVARCHAR(100) NULL, "
                    + "  trangThai NVARCHAR(30) DEFAULT N'Đang hợp tác'"
                    + ")");
        } catch (SQLException ignored) {
        }
    }

    public List<DoiTac> layTatCa() {
        List<DoiTac> ds = new ArrayList<>();
        String sql = "SELECT maDT, tenDT, loaiDT, soDienThoai, diaChi, email, trangThai FROM DOITAC ORDER BY maDT";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new DoiTac(
                        rs.getString("maDT"),
                        rs.getString("tenDT"),
                        rs.getString("loaiDT"),
                        rs.getString("soDienThoai"),
                        rs.getString("diaChi"),
                        rs.getString("email"),
                        rs.getString("trangThai")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public void them(DoiTac dt) throws SQLException {
        String sql = "INSERT INTO DOITAC (maDT, tenDT, loaiDT, soDienThoai, diaChi, email, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dt.getMaDT());
            ps.setString(2, dt.getTenDT());
            ps.setString(3, dt.getLoaiDT());
            ps.setString(4, dt.getSoDienThoai());
            ps.setString(5, dt.getDiaChi());
            ps.setString(6, dt.getEmail());
            ps.setString(7, dt.getTrangThai() != null ? dt.getTrangThai() : "Đang hợp tác");
            ps.executeUpdate();
        }
    }

    public void capNhat(DoiTac dt) throws SQLException {
        String sql = "UPDATE DOITAC SET tenDT = ?, loaiDT = ?, soDienThoai = ?, diaChi = ?, email = ?, trangThai = ? WHERE maDT = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dt.getTenDT());
            ps.setString(2, dt.getLoaiDT());
            ps.setString(3, dt.getSoDienThoai());
            ps.setString(4, dt.getDiaChi());
            ps.setString(5, dt.getEmail());
            ps.setString(6, dt.getTrangThai());
            ps.setString(7, dt.getMaDT());
            ps.executeUpdate();
        }
    }

    public void xoa(String maDT) throws SQLException {
        String sql = "DELETE FROM DOITAC WHERE maDT = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDT);
            ps.executeUpdate();
        }
    }

    public String maTiepTheo() {
        int max = 0;
        String sql = "SELECT maDT FROM DOITAC WHERE maDT LIKE 'DT%'";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String m = rs.getString(1);
                if (m != null && m.length() > 2) {
                    try {
                        int num = Integer.parseInt(m.substring(2).trim());
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException ignored) {}
        return String.format("DT%03d", max + 1);
    }
}
