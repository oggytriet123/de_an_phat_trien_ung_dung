package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.MaGiamGia;

/**
 * Quản lý mã giảm giá (bảng MAGIAMGIA) trong SQL Server.
 */
public class MaGiamGiaDAO {

    public MaGiamGiaDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'MAGIAMGIA') "
                    + "CREATE TABLE MAGIAMGIA ("
                    + "  maGiamGia NVARCHAR(20) PRIMARY KEY, "
                    + "  phanTram BIT DEFAULT 1, "
                    + "  giaTri BIGINT NOT NULL, "
                    + "  ngayBatDau DATE NULL, "
                    + "  hanDung DATE NOT NULL, "
                    + "  trangThai NVARCHAR(30) DEFAULT N'Hoạt động'"
                    + ")");
            st.executeUpdate("IF COL_LENGTH('MAGIAMGIA', 'ngayBatDau') IS NULL ALTER TABLE MAGIAMGIA ADD ngayBatDau DATE NULL");
        } catch (SQLException ignored) {
        }
    }

    public List<MaGiamGia> layTatCa() {
        List<MaGiamGia> ds = new ArrayList<>();
        String sql = "SELECT maGiamGia, phanTram, giaTri, ngayBatDau, hanDung, trangThai FROM MAGIAMGIA ORDER BY hanDung DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ma = rs.getString("maGiamGia");
                boolean phanTram = rs.getBoolean("phanTram");
                long giaTri = rs.getLong("giaTri");
                Date bd = rs.getDate("ngayBatDau");
                Date hd = rs.getDate("hanDung");
                LocalDate hanDung = hd != null ? hd.toLocalDate() : LocalDate.now().plusMonths(1);
                LocalDate ngayBatDau = bd != null ? bd.toLocalDate() : LocalDate.now();
                if (ngayBatDau.isAfter(hanDung)) ngayBatDau = hanDung.minusMonths(1);
                ds.add(new MaGiamGia(ma, phanTram, giaTri, ngayBatDau, hanDung));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public void them(MaGiamGia m) throws SQLException {
        String sql = "INSERT INTO MAGIAMGIA (maGiamGia, phanTram, giaTri, ngayBatDau, hanDung, trangThai) VALUES (?, ?, ?, ?, ?, N'Hoạt động')";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getMa());
            ps.setBoolean(2, m.isPhanTram());
            ps.setLong(3, m.getGiaTri());
            ps.setDate(4, Date.valueOf(m.getNgayBatDau() != null ? m.getNgayBatDau() : LocalDate.now()));
            ps.setDate(5, Date.valueOf(m.getHanDung()));
            ps.executeUpdate();
        }
    }

    public void xoa(String ma) throws SQLException {
        String sql = "DELETE FROM MAGIAMGIA WHERE maGiamGia = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.executeUpdate();
        }
    }
}
