package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ConnectDB.ConnectDB;
import Entity.TaiKhoan;

public class TaiKhoanDAO {

    /** Tìm tài khoản theo mã hoặc số điện thoại + mật khẩu đã băm SHA-256 (hex in hoa). */
    public TaiKhoan timTheoDangNhap(String maHoacSdt, String matKhauHash) throws SQLException {
        String sql = "SELECT MaTaiKhoan, SoDienThoai, HoTen, VaiTro FROM TaiKhoan "
                + "WHERE (MaTaiKhoan = ? OR SoDienThoai = ?) AND MatKhau = ? AND HoatDong = 1";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoacSdt);
            ps.setString(2, maHoacSdt);
            ps.setString(3, matKhauHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TaiKhoan(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                }
            }
        }
        return null;
    }
}
