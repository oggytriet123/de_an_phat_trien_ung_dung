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
import Entity.ThongKeThuCung;
import Entity.ThuCung;

/**
 * Lớp truy xuất cơ sở dữ liệu cho module Quản lý Thú cưng (bảng THUCUNG).
 */
public class ThuCungDAO {

    public ThuCungDAO() {
        // Tự động kiểm tra và nâng cấp bảng THUCUNG nếu cơ sở dữ liệu chưa có các cột mới
        damBaoCauTrucBang();
    }

    /**
     * Tự động bổ sung các cột mới vào bảng THUCUNG nếu CSDL chưa có,
     * giúp chương trình chạy mượt mà ngay cả khi chưa chạy file script nâng cấp SQL.
     */
    private void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF COL_LENGTH('THUCUNG', 'giong') IS NULL ALTER TABLE THUCUNG ADD giong NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('THUCUNG', 'gioiTinh') IS NULL ALTER TABLE THUCUNG ADD gioiTinh NVARCHAR(10) NULL");
            st.executeUpdate("IF COL_LENGTH('THUCUNG', 'tuoi') IS NULL ALTER TABLE THUCUNG ADD tuoi INT NULL");
            st.executeUpdate("IF COL_LENGTH('THUCUNG', 'trangThai') IS NULL ALTER TABLE THUCUNG ADD trangThai NVARCHAR(50) NULL");
            st.executeUpdate("IF COL_LENGTH('THUCUNG', 'ngayTiepNhan') IS NULL ALTER TABLE THUCUNG ADD ngayTiepNhan DATE NULL");
        } catch (SQLException ignored) {
            // Không ngắt ứng dụng nếu quyền tài khoản CSDL không cho phép DDL
        }
    }

    /**
     * Lấy toàn bộ danh sách thú cưng kèm thông tin chủ nuôi từ bảng KHACHHANG.
     */
    public List<ThuCung> layTatCa() throws SQLException {
        List<ThuCung> ds = new ArrayList<>();
        String sql = "SELECT t.petID, t.ten, t.loai, "
                + "       ISNULL(t.giong, N'') AS giong, "
                + "       ISNULL(t.gioiTinh, N'Đực') AS gioiTinh, "
                + "       ISNULL(t.tuoi, 1) AS tuoi, "
                + "       ISNULL(t.canNang, 0) AS canNang, "
                + "       ISNULL(t.theCanhBao, N'') AS theCanhBao, "
                + "       ISNULL(t.trangThai, N'Khỏe mạnh') AS trangThai, "
                + "       t.maKH, t.ngayTiepNhan, "
                + "       k.hoTen, k.soDienThoai, k.hangThe "
                + "FROM THUCUNG t "
                + "LEFT JOIN KHACHHANG k ON t.maKH = k.maKH "
                + "ORDER BY LEN(t.petID), t.petID";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String petID = rs.getString("petID");
                String ten = rs.getString("ten");
                String loai = rs.getString("loai");
                String giong = rs.getString("giong");
                String gioiTinh = rs.getString("gioiTinh");
                int tuoi = rs.getInt("tuoi");
                double canNang = rs.getDouble("canNang");
                String theCanhBao = rs.getString("theCanhBao");
                String trangThai = rs.getString("trangThai");
                String maKH = rs.getString("maKH");
                Date ngaySQL = rs.getDate("ngayTiepNhan");
                LocalDate ngayTN = (ngaySQL != null) ? ngaySQL.toLocalDate() : LocalDate.now();
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("soDienThoai");
                String hangThe = rs.getString("hangThe");

                ds.add(new ThuCung(petID, ten, loai, giong, gioiTinh, tuoi, canNang,
                        theCanhBao, trangThai, maKH, ngayTN, hoTen, sdt, hangThe));
            }
        }
        return ds;
    }

    /**
     * Thông tin cơ bản khách hàng để đổ vào ComboBox chọn chủ nuôi.
     */
    public static class KhachHangOption {
        public final String maKH;
        public final String hoTen;
        public final String soDienThoai;
        public final String hangThe;

        public KhachHangOption(String maKH, String hoTen, String soDienThoai, String hangThe) {
            this.maKH = maKH;
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
            this.hangThe = hangThe;
        }

        @Override
        public String toString() {
            String sdtStr = (soDienThoai == null || soDienThoai.isEmpty()) ? "" : " - " + soDienThoai;
            return hoTen + sdtStr + " (" + maKH + ")";
        }
    }

    /**
     * Lấy danh sách khách hàng phục vụ việc chọn chủ nuôi cho thú cưng.
     */
    public List<KhachHangOption> layDanhSachKhachHang() throws SQLException {
        List<KhachHangOption> list = new ArrayList<>();
        String sql = "SELECT maKH, hoTen, soDienThoai, hangThe FROM KHACHHANG ORDER BY hoTen";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhachHangOption(
                        rs.getString("maKH"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"),
                        rs.getString("hangThe")
                ));
            }
        }
        return list;
    }

    /**
     * Thêm mới một thú cưng vào hệ thống. Tự động sinh mã PETxxx tăng dần.
     * @return mã thú cưng mới vừa tạo (ví dụ: PET013)
     */
    public String them(ThuCung tc) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int nextId = maxSoPet(con) + 1;
                String petID = String.format("PET%03d", nextId);

                String sql = "INSERT INTO THUCUNG (petID, ten, loai, giong, gioiTinh, tuoi, canNang, "
                        + "                   theCanhBao, trangThai, maKH, ngayTiepNhan) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, petID);
                    ps.setString(2, tc.getTen());
                    ps.setString(3, tc.getLoai());
                    ps.setString(4, tc.getGiong());
                    ps.setString(5, tc.getGioiTinh());
                    ps.setInt(6, tc.getTuoi());
                    ps.setDouble(7, tc.getCanNang());
                    ps.setString(8, tc.getTheCanhBao());
                    ps.setString(9, tc.getTrangThai());
                    if (tc.getMaKH() != null && !tc.getMaKH().trim().isEmpty()) {
                        ps.setString(10, tc.getMaKH());
                    } else {
                        ps.setNull(10, java.sql.Types.NVARCHAR);
                    }
                    LocalDate ngayTN = (tc.getNgayTiepNhan() != null) ? tc.getNgayTiepNhan() : LocalDate.now();
                    ps.setDate(11, Date.valueOf(ngayTN));
                    ps.executeUpdate();
                }

                con.commit();
                return petID;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Cập nhật thông tin hồ sơ thú cưng đã có trong CSDL.
     */
    public boolean capNhat(ThuCung tc) throws SQLException {
        String sql = "UPDATE THUCUNG SET "
                + "ten = ?, loai = ?, giong = ?, gioiTinh = ?, tuoi = ?, "
                + "canNang = ?, theCanhBao = ?, trangThai = ?, maKH = ? "
                + "WHERE petID = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tc.getTen());
            ps.setString(2, tc.getLoai());
            ps.setString(3, tc.getGiong());
            ps.setString(4, tc.getGioiTinh());
            ps.setInt(5, tc.getTuoi());
            ps.setDouble(6, tc.getCanNang());
            ps.setString(7, tc.getTheCanhBao());
            ps.setString(8, tc.getTrangThai());
            if (tc.getMaKH() != null && !tc.getMaKH().trim().isEmpty()) {
                ps.setString(9, tc.getMaKH());
            } else {
                ps.setNull(9, java.sql.Types.NVARCHAR);
            }
            ps.setString(10, tc.getPetID());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Kiểm tra tính toàn vẹn dữ liệu trước khi xóa.
     * @return chuỗi thông báo nếu có ràng buộc không được xóa, null nếu an toàn để xóa.
     */
    public String kiemTraRangBuocTruocKhiXoa(String petID) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            // 1. Kiểm tra lồng chuồng lưu trữ đang sử dụng thú cưng này
            if (bangTonTai(con, "PHIEU_LUUTRU")) {
                String sqlCage = "SELECT COUNT(*) FROM PHIEU_LUUTRU WHERE petID = ? AND trangThai IN (N'Đang lưu trú', N'Đã đặt', N'Chưa nhận')";
                try (PreparedStatement ps = con.prepareStatement(sqlCage)) {
                    ps.setString(1, petID);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return "Thú cưng đang có phiếu lưu trú tại lồng chuồng đang hoạt động. Không thể xóa!";
                        }
                    }
                }
            }

            // 2. Kiểm tra lịch hẹn dịch vụ/bác sĩ chưa hoàn tất
            if (bangTonTai(con, "LICHHEN")) {
                String sqlHen = "SELECT COUNT(*) FROM LICHHEN WHERE petID = ? AND trangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang thực hiện')";
                try (PreparedStatement ps = con.prepareStatement(sqlHen)) {
                    ps.setString(1, petID);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return "Thú cưng đang có lịch hẹn dịch vụ hoặc khám bệnh chưa hoàn tất. Không thể xóa!";
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Xóa hồ sơ thú cưng khỏi CSDL.
     */
    public boolean xoa(String petID) throws SQLException {
        String err = kiemTraRangBuocTruocKhiXoa(petID);
        if (err != null) {
            throw new SQLException(err);
        }

        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Gỡ tham chiếu mềm ở các bảng lịch sử (nếu có) để tránh lỗi Foreign Key
                if (bangTonTai(con, "PHIEU_LUUTRU")) {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE PHIEU_LUUTRU SET petID = NULL WHERE petID = ?")) {
                        ps.setString(1, petID);
                        ps.executeUpdate();
                    }
                }
                if (bangTonTai(con, "LICHHEN")) {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE LICHHEN SET petID = NULL WHERE petID = ?")) {
                        ps.setString(1, petID);
                        ps.executeUpdate();
                    }
                }

                // Thực hiện xóa dòng thú cưng
                int count;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM THUCUNG WHERE petID = ?")) {
                    ps.setString(1, petID);
                    count = ps.executeUpdate();
                }

                con.commit();
                return count > 0;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Thống kê số liệu phục vụ 4 thẻ KPI trên giao diện.
     */
    public ThongKeThuCung layThongKe() throws SQLException {
        String sql = "SET NOCOUNT ON; "
                + "DECLARE @dauThang DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1); "
                + "SELECT "
                + " (SELECT COUNT(*) FROM THUCUNG) AS tong, "
                + " (SELECT COUNT(*) FROM THUCUNG WHERE loai = N'Chó') AS soCho, "
                + " (SELECT COUNT(*) FROM THUCUNG WHERE loai = N'Mèo') AS soMeo, "
                + " (SELECT COUNT(*) FROM THUCUNG WHERE loai NOT IN (N'Chó', N'Mèo')) AS soKhac, "
                + " (SELECT COUNT(*) FROM THUCUNG WHERE (theCanhBao IS NOT NULL AND LEN(theCanhBao) > 0) "
                + "                                    OR trangThai IN (N'Cần chú ý', N'Đang theo dõi', N'Đang điều trị')) AS soCanhBao, "
                + " (SELECT COUNT(*) FROM THUCUNG WHERE ngayTiepNhan >= @dauThang) AS tiepNhanThang";

        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return new ThongKeThuCung(
                        rs.getInt("tong"),
                        rs.getInt("soCho"),
                        rs.getInt("soMeo"),
                        rs.getInt("soKhac"),
                        rs.getInt("soCanhBao"),
                        rs.getInt("tiepNhanThang")
                );
            }
        }
        return new ThongKeThuCung(0, 0, 0, 0, 0, 0);
    }

    private static int maxSoPet(Connection con) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(petID, 4, 18) AS INT)), 0) "
                + "FROM THUCUNG WITH (UPDLOCK, HOLDLOCK) WHERE petID LIKE 'PET%'";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static boolean bangTonTai(Connection con, String tableName) throws SQLException {
        String sql = "SELECT 1 FROM sys.tables WHERE name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
