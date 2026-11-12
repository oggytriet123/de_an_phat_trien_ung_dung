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

import java.time.format.DateTimeFormatter;

import ConnectDB.ConnectDB;
import Entity.LichHen;

/**
 * Quản lý lịch hẹn (bảng LICHHEN) trong SQL Server:
 * Tự động đồng bộ với KHACHHANG, THUCUNG, DICHVU, NHANVIEN_BACSI.
 */
public class LichHenDAO {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public LichHenDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'tenKH') IS NULL ALTER TABLE LICHHEN ADD tenKH NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'soDienThoai') IS NULL ALTER TABLE LICHHEN ADD soDienThoai NVARCHAR(20) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'tenPet') IS NULL ALTER TABLE LICHHEN ADD tenPet NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'tenDV') IS NULL ALTER TABLE LICHHEN ADD tenDV NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'tenBacSi') IS NULL ALTER TABLE LICHHEN ADD tenBacSi NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'gioHen') IS NULL ALTER TABLE LICHHEN ADD gioHen NVARCHAR(10) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'ghiChu') IS NULL ALTER TABLE LICHHEN ADD ghiChu NVARCHAR(500) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'nhatKy') IS NULL ALTER TABLE LICHHEN ADD nhatKy NVARCHAR(MAX) NULL");
            st.executeUpdate("IF COL_LENGTH('LICHHEN', 'maKH') IS NULL ALTER TABLE LICHHEN ADD maKH NVARCHAR(20) NULL");
        } catch (SQLException ignored) {
        }
    }

    public List<LichHen> layTatCa() {
        List<LichHen> ds = new ArrayList<>();
        String sql = "SELECT lh.maLH, lh.ngayHen, lh.khungGio, lh.gioHen, lh.trangThai, "
                + "       ISNULL(lh.tenKH, k.hoTen) AS khachHang, "
                + "       ISNULL(lh.soDienThoai, k.soDienThoai) AS sdt, "
                + "       ISNULL(lh.tenPet, t.ten) AS thuCung, "
                + "       ISNULL(lh.tenDV, d.tenDV) AS dichVu, "
                + "       ISNULL(lh.tenBacSi, nv.hoTen) AS bacSi, "
                + "       lh.ghiChu, lh.nhatKy "
                + "FROM LICHHEN lh "
                + "LEFT JOIN THUCUNG t ON lh.petID = t.petID "
                + "LEFT JOIN KHACHHANG k ON (lh.maKH = k.maKH OR t.maKH = k.maKH) "
                + "LEFT JOIN DICHVU d ON lh.maDV = d.maDV "
                + "LEFT JOIN NHANVIEN nv ON lh.maNV_BS = nv.maNV "
                + "ORDER BY lh.ngayHen DESC, lh.maLH DESC";

        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LichHen l = new LichHen();
                l.ma = rs.getString("maLH");
                Date ngaySQL = rs.getDate("ngayHen");
                l.ngay = (ngaySQL != null) ? ngaySQL.toLocalDate() : LocalDate.now();

                String gioStr = rs.getString("gioHen");
                if (gioStr == null || gioStr.trim().isEmpty()) {
                    String kg = rs.getString("khungGio");
                    if (kg != null && kg.contains("-")) gioStr = kg.split("-")[0].trim();
                }
                try {
                    l.gio = (gioStr != null && !gioStr.trim().isEmpty()) ? LocalTime.parse(gioStr.trim().substring(0, 5)) : LocalTime.of(9, 0);
                } catch (Exception e) {
                    l.gio = LocalTime.of(9, 0);
                }

                l.trangThai = rs.getString("trangThai");
                if (l.trangThai == null || l.trangThai.trim().isEmpty()) l.trangThai = "Chờ xác nhận";

                l.khachHang = rs.getString("khachHang") != null ? rs.getString("khachHang") : "Khách vãng lai";
                l.sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                l.thuCung = rs.getString("thuCung") != null ? rs.getString("thuCung") : "Thú cưng";
                l.dichVu = rs.getString("dichVu") != null ? rs.getString("dichVu") : "Khám tổng quát";
                l.bacSi = rs.getString("bacSi") != null ? rs.getString("bacSi") : "BS. Nguyễn Hoài Anh";
                l.ghiChu = rs.getString("ghiChu") != null ? rs.getString("ghiChu") : "";

                String nk = rs.getString("nhatKy");
                if (nk != null && !nk.trim().isEmpty()) {
                    for (String line : nk.split(";;")) {
                        if (!line.trim().isEmpty()) l.lichSu.add(line.trim());
                    }
                }
                if (l.lichSu.isEmpty()) {
                    l.ghiLog("Tiếp nhận lịch hẹn");
                }
                ds.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public void them(LichHen l) throws SQLException {
        String sql = "INSERT INTO LICHHEN (maLH, ngayHen, khungGio, gioHen, trangThai, tenKH, soDienThoai, tenPet, tenDV, tenBacSi, ghiChu, nhatKy) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.ma);
            ps.setDate(2, Date.valueOf(l.ngay));
            String g = l.gio.format(TIME_FMT);
            ps.setString(3, g + " - " + l.gio.plusHours(1).format(TIME_FMT));
            ps.setString(4, g);
            ps.setString(5, l.trangThai);
            ps.setString(6, l.khachHang);
            ps.setString(7, l.sdt);
            ps.setString(8, l.thuCung);
            ps.setString(9, l.dichVu);
            ps.setString(10, l.bacSi);
            ps.setString(11, l.ghiChu);
            ps.setString(12, String.join(";;", l.lichSu));
            ps.executeUpdate();
        }
    }

    public void capNhat(LichHen l) throws SQLException {
        String sql = "UPDATE LICHHEN SET ngayHen = ?, khungGio = ?, gioHen = ?, trangThai = ?, "
                + "tenKH = ?, soDienThoai = ?, tenPet = ?, tenDV = ?, tenBacSi = ?, ghiChu = ?, nhatKy = ? "
                + "WHERE maLH = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(l.ngay));
            String g = l.gio.format(TIME_FMT);
            ps.setString(2, g + " - " + l.gio.plusHours(1).format(TIME_FMT));
            ps.setString(3, g);
            ps.setString(4, l.trangThai);
            ps.setString(5, l.khachHang);
            ps.setString(6, l.sdt);
            ps.setString(7, l.thuCung);
            ps.setString(8, l.dichVu);
            ps.setString(9, l.bacSi);
            ps.setString(10, l.ghiChu);
            ps.setString(11, String.join(";;", l.lichSu));
            ps.setString(12, l.ma);
            ps.executeUpdate();
        }
    }

    public void xoa(String maLH) throws SQLException {
        String sql = "DELETE FROM LICHHEN WHERE maLH = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLH);
            ps.executeUpdate();
        }
    }

    public String maTiepTheo() {
        int max = 0;
        String sql = "SELECT maLH FROM LICHHEN WHERE maLH LIKE 'LH%'";
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
        return String.format("LH%03d", max + 1);
    }
}
