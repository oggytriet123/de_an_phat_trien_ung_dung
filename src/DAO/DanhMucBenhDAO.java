package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.DanhMucBenh;

/** Truy xuất bảng DANHMUCBENH. */
public class DanhMucBenhDAO {

    public List<DanhMucBenh> layTatCa() throws SQLException {
        List<DanhMucBenh> ds = new ArrayList<>();
        String sql = "SELECT maBenh, tenBenh, ISNULL(trieuChung, N'') AS trieuChung "
                + "FROM DANHMUCBENH ORDER BY LEN(maBenh), maBenh";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new DanhMucBenh(rs.getString("maBenh"), rs.getString("tenBenh"), rs.getString("trieuChung")));
            }
        }
        return ds;
    }

    public void them(DanhMucBenh b) throws SQLException {
        String sql = "INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung) VALUES (?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getMaBenh());
            ps.setString(2, b.getTenBenh());
            ps.setString(3, b.getTrieuChung());
            ps.executeUpdate();
        }
    }

    public void capNhat(DanhMucBenh b) throws SQLException {
        String sql = "UPDATE DANHMUCBENH SET tenBenh = ?, trieuChung = ? WHERE maBenh = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getTenBenh());
            ps.setString(2, b.getTrieuChung());
            ps.setString(3, b.getMaBenh());
            ps.executeUpdate();
        }
    }

    public void xoa(String maBenh) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM DANHMUCBENH WHERE maBenh = ?")) {
            ps.setString(1, maBenh);
            ps.executeUpdate();
        }
    }
}
