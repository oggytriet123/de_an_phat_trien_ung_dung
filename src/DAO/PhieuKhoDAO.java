package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.PhieuKho;

/**
 * Quản lý lịch sử Nhập / Xuất kho (bảng PHIEUKHO) trong SQL Server:
 * - Nhập kho: người thực hiện là nhân viên (bảng NHANVIEN), nhà cung cấp là bên đối tác (bảng DOITAC).
 * - Xuất kho: xuất cho bên đối tác / chi nhánh (bảng DOITAC).
 */
public class PhieuKhoDAO {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PhieuKhoDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PHIEUKHO') "
                    + "CREATE TABLE PHIEUKHO ("
                    + "  maPhieu NVARCHAR(30) PRIMARY KEY, "
                    + "  loaiPhieu NVARCHAR(50) NOT NULL, "
                    + "  ngayLap DATE NOT NULL, "
                    + "  gioLap NVARCHAR(10) NOT NULL, "
                    + "  nguoiThucHien NVARCHAR(100) NOT NULL, "
                    + "  doiTac NVARCHAR(150) NOT NULL, "
                    + "  tongGiaTri BIGINT NOT NULL, "
                    + "  trangThai NVARCHAR(50) NOT NULL DEFAULT N'Hoàn tất', "
                    + "  ghiChu NVARCHAR(500) NULL, "
                    + "  maNV NVARCHAR(20) NULL, "
                    + "  maDT NVARCHAR(20) NULL"
                    + ")");
        } catch (SQLException ignored) {
        }
    }

    public List<PhieuKho> layTatCa() {
        List<PhieuKho> ds = new ArrayList<>();
        String sql = "SELECT maPhieu, loaiPhieu, ngayLap, gioLap, nguoiThucHien, doiTac, tongGiaTri, trangThai FROM PHIEUKHO ORDER BY ngayLap DESC, gioLap DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ma = rs.getString("maPhieu");
                String loai = rs.getString("loaiPhieu");
                Date ngaySQL = rs.getDate("ngayLap");
                String ngayStr = (ngaySQL != null) ? ngaySQL.toLocalDate().format(DATE_FMT) : "01/01/2026";
                String gio = rs.getString("gioLap");
                if (gio == null) gio = "08:00";
                String nv = rs.getString("nguoiThucHien");
                String dt = rs.getString("doiTac");
                long gia = rs.getLong("tongGiaTri");
                String tt = rs.getString("trangThai");
                ds.add(new PhieuKho(ma, loai, ngayStr, gio, nv, dt, gia, tt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public void them(PhieuKho pk) throws SQLException {
        String sql = "INSERT INTO PHIEUKHO (maPhieu, loaiPhieu, ngayLap, gioLap, nguoiThucHien, doiTac, tongGiaTri, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pk.getMaPhieu());
            ps.setString(2, pk.getLoaiPhieu());

            LocalDate d = LocalDate.now();
            try {
                d = LocalDate.parse(pk.getNgayLap(), DATE_FMT);
            } catch (Exception ignored) {}
            ps.setDate(3, Date.valueOf(d));
            ps.setString(4, pk.getGioLap() != null ? pk.getGioLap() : "09:00");
            ps.setString(5, pk.getNguoiThucHien());
            ps.setString(6, pk.getDoiTac());
            ps.setLong(7, pk.getTongGiaTri());
            ps.setString(8, pk.getTrangThai() != null ? pk.getTrangThai() : "Hoàn tất");
            ps.executeUpdate();
        }
    }

    public void xoa(String maPhieu) throws SQLException {
        String sql = "DELETE FROM PHIEUKHO WHERE maPhieu = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            ps.executeUpdate();
        }
    }

    public String maTiepTheo(boolean isNhap) {
        int year = LocalDate.now().getYear();
        String prefix = (isNhap ? "PN-" : "PX-") + year + "-";
        int max = 0;
        String sql = "SELECT maPhieu FROM PHIEUKHO WHERE maPhieu LIKE '" + prefix + "%'";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String m = rs.getString(1);
                if (m != null && m.length() > prefix.length()) {
                    try {
                        int num = Integer.parseInt(m.substring(prefix.length()).trim());
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException ignored) {}
        return String.format("%s%04d", prefix, max + 1);
    }
}
