package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import ConnectDB.ConnectDB;
import Entity.TaiKhoan;

public class TaiKhoanDAO {

    /** Tìm tài khoản theo mã NV hoặc tên đăng nhập + mật khẩu đã băm SHA-256 (hex in hoa). */
    public TaiKhoan timTheoDangNhap(String maHoacSdt, String matKhauHash) throws SQLException {
        String sql = "SELECT nv.maNV, nv.tenDangNhap, nv.hoTen, pq.tenQuyen, nv.soDienThoai, nv.ngaySinh, nv.email, "
                + "nv.ngayVaoLam, nv.caLamViec, nv.trangThai "
                + "FROM NHANVIEN nv LEFT JOIN PHANQUYEN pq ON nv.maQuyen = pq.maQuyen "
                + "WHERE (nv.maNV = ? OR nv.tenDangNhap = ?) AND nv.matKhau = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoacSdt);
            ps.setString(2, maHoacSdt);
            ps.setString(3, matKhauHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maNV = rs.getString(1);
                    java.sql.Date ns = rs.getDate(6);
                    java.sql.Date vl = rs.getDate(8);
                    TaiKhoan tk = new TaiKhoan(maNV, rs.getString(2), rs.getString(3), rs.getString(4),
                            layChucNang(con, maNV), rs.getString(5), ns == null ? null : ns.toLocalDate(),
                            rs.getString(7));
                    tk.setThongTinCongViec(vl == null ? null : vl.toLocalDate(), rs.getString(9), rs.getString(10));
                    return tk;
                }
            }
        }
        return null;
    }

    /** Danh sách chức năng của nhân viên, theo QUYEN_CHUCNANG của nhóm quyền. */
    private Set<String> layChucNang(Connection con, String maNV) throws SQLException {
        String sql = "SELECT cn.tenChucNang FROM NHANVIEN nv "
                + "JOIN QUYEN_CHUCNANG qc ON nv.maQuyen = qc.maQuyen "
                + "JOIN CHUCNANG cn ON qc.maChucNang = cn.maChucNang "
                + "WHERE nv.maNV = ?";
        Set<String> r = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) r.add(rs.getString(1));
            }
        }
        return r;
    }

    /** Cập nhật SĐT, ngày sinh, email của nhân viên (họ tên không được sửa). @return số dòng bị ảnh hưởng */
    public int capNhatLienHe(String maNV, String soDienThoai, java.time.LocalDate ngaySinh, String email)
            throws SQLException {
        String sql = "UPDATE NHANVIEN SET soDienThoai = ?, ngaySinh = ?, email = ? WHERE maNV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            if (ngaySinh == null) ps.setNull(2, java.sql.Types.DATE); else ps.setDate(2, java.sql.Date.valueOf(ngaySinh));
            ps.setString(3, email);
            ps.setString(4, maNV);
            return ps.executeUpdate();
        }
    }

    /** Đổi mật khẩu khi mật khẩu cũ (đã băm) khớp. @return số dòng bị ảnh hưởng (0 = sai mật khẩu cũ) */
    public int doiMatKhau(String maNV, String hashCu, String hashMoi) throws SQLException {
        String sql = "UPDATE NHANVIEN SET matKhau = ? WHERE maNV = ? AND matKhau = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashMoi);
            ps.setString(2, maNV);
            ps.setString(3, hashCu);
            return ps.executeUpdate();
        }
    }
}