package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.DichVu;
import Entity.ThongKeDichVu;

/**
 * Lớp truy xuất cơ sở dữ liệu cho module Quản lý Dịch vụ (bảng DICHVU).
 */
public class DichVuDAO {

    public DichVuDAO() {
        damBaoCauTrucBang();
    }

    /**
     * Tự động bổ sung các cột mới nếu CSDL chưa có (Auto DDL Migration).
     */
    private void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'loaiDV') IS NULL ALTER TABLE DICHVU ADD loaiDV NVARCHAR(50) NULL");
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'thoiGianThucHien') IS NULL ALTER TABLE DICHVU ADD thoiGianThucHien INT NULL");
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'moTa') IS NULL ALTER TABLE DICHVU ADD moTa NVARCHAR(500) NULL");
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'trangThai') IS NULL ALTER TABLE DICHVU ADD trangThai NVARCHAR(30) NULL");
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'doiTuong') IS NULL ALTER TABLE DICHVU ADD doiTuong NVARCHAR(100) NULL");
        } catch (SQLException ignored) {
        }
    }

    /**
     * Lấy toàn bộ danh sách dịch vụ trong hệ thống.
     */
    public List<DichVu> layTatCa() throws SQLException {
        List<DichVu> ds = new ArrayList<>();
        String sql = "SELECT maDV, tenDV, giaTien, "
                + "       ISNULL(loaiDV, N'Khám & Điều trị') AS loaiDV, "
                + "       ISNULL(thoiGianThucHien, 30) AS thoiGianThucHien, "
                + "       ISNULL(moTa, N'') AS moTa, "
                + "       ISNULL(trangThai, N'Đang cung cấp') AS trangThai, "
                + "       ISNULL(doiTuong, N'Tất cả thú cưng') AS doiTuong "
                + "FROM DICHVU "
                + "ORDER BY LEN(maDV), maDV";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maDV = rs.getString("maDV");
                String tenDV = rs.getString("tenDV");
                double giaTien = rs.getDouble("giaTien");
                String loaiDV = rs.getString("loaiDV");
                int thoiGian = rs.getInt("thoiGianThucHien");
                String moTa = rs.getString("moTa");
                String trangThai = rs.getString("trangThai");
                String doiTuong = rs.getString("doiTuong");

                ds.add(new DichVu(maDV, tenDV, loaiDV, giaTien, thoiGian, moTa, trangThai, doiTuong));
            }
        }
        return ds;
    }

    /**
     * Thêm mới một dịch vụ vào hệ thống. Tự động sinh mã DVxxx tăng dần.
     * @return mã dịch vụ vừa tạo (ví dụ: DV013)
     */
    public String them(DichVu dv) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int nextId = maxSoDV(con) + 1;
                String maDV = String.format("DV%03d", nextId);

                String sql = "INSERT INTO DICHVU (maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maDV);
                    ps.setString(2, dv.getTenDV());
                    ps.setDouble(3, dv.getGiaTien());
                    ps.setString(4, dv.getLoaiDV());
                    ps.setInt(5, dv.getThoiGianThucHien());
                    ps.setString(6, dv.getMoTa());
                    ps.setString(7, dv.getTrangThai());
                    ps.setString(8, dv.getDoiTuong());
                    ps.executeUpdate();
                }

                con.commit();
                return maDV;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Cập nhật thông tin dịch vụ.
     */
    public boolean capNhat(DichVu dv) throws SQLException {
        String sql = "UPDATE DICHVU SET tenDV = ?, giaTien = ?, loaiDV = ?, thoiGianThucHien = ?, "
                + "moTa = ?, trangThai = ?, doiTuong = ? WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dv.getTenDV());
            ps.setDouble(2, dv.getGiaTien());
            ps.setString(3, dv.getLoaiDV());
            ps.setInt(4, dv.getThoiGianThucHien());
            ps.setString(5, dv.getMoTa());
            ps.setString(6, dv.getTrangThai());
            ps.setString(7, dv.getDoiTuong());
            ps.setString(8, dv.getMaDV());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Kiểm tra tính toàn vẹn dữ liệu trước khi xóa dịch vụ.
     * @return chuỗi thông báo nếu có ràng buộc, null nếu an toàn để xóa.
     */
    public String kiemTraRangBuocTruocKhiXoa(String maDV) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            // 1. Kiểm tra hóa đơn thanh toán đã sử dụng dịch vụ này
            if (bangTonTai(con, "CHITIETHOADON")) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM CHITIETHOADON WHERE maDV = ?")) {
                    ps.setString(1, maDV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return "Dịch vụ đã từng được xuất trong " + rs.getInt(1) + " hóa đơn thanh toán.\n"
                                    + "Không thể xóa để bảo toàn dữ liệu sổ sách kế toán và doanh thu.\n"
                                    + "Gợi ý: Bạn hãy chuyển trạng thái sang \"Tạm ngưng\" thay vì xóa.";
                        }
                    }
                }
            }

            // 2. Kiểm tra lịch hẹn dịch vụ
            if (bangTonTai(con, "LICHHEN")) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM LICHHEN WHERE maDV = ? AND trangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đang thực hiện')")) {
                    ps.setString(1, maDV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return "Dịch vụ đang có " + rs.getInt(1) + " lịch hẹn sắp tới của khách hàng chưa hoàn tất.\n"
                                    + "Không thể xóa khi lịch hẹn còn hiệu lực!";
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Xóa dịch vụ khỏi hệ thống.
     */
    public boolean xoa(String maDV) throws SQLException {
        String err = kiemTraRangBuocTruocKhiXoa(maDV);
        if (err != null) {
            throw new SQLException(err);
        }

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM DICHVU WHERE maDV = ?")) {
            ps.setString(1, maDV);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Thống kê số lượng dịch vụ và đơn giá trung bình.
     */
    public ThongKeDichVu layThongKe() throws SQLException {
        String sql = "SELECT "
                + " (SELECT COUNT(*) FROM DICHVU) AS tong, "
                + " (SELECT COUNT(*) FROM DICHVU WHERE loaiDV = N'Spa & Grooming') AS soSpa, "
                + " (SELECT COUNT(*) FROM DICHVU WHERE loaiDV = N'Khám & Điều trị' OR loaiDV = N'Phẫu thuật') AS soKhamChua, "
                + " (SELECT COUNT(*) FROM DICHVU WHERE loaiDV = N'Tiêm phòng & Xét nghiệm') AS soTiemPhong, "
                + " (SELECT ISNULL(AVG(CAST(giaTien AS BIGINT)), 0) FROM DICHVU) AS giaTB, "
                + " (SELECT COUNT(*) FROM DICHVU WHERE trangThai = N'Đang cung cấp') AS soDangMo";

        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return new ThongKeDichVu(
                        rs.getInt("tong"),
                        rs.getInt("soSpa"),
                        rs.getInt("soKhamChua"),
                        rs.getInt("soTiemPhong"),
                        rs.getLong("giaTB"),
                        rs.getInt("soDangMo")
                );
            }
        }
        return new ThongKeDichVu(0, 0, 0, 0, 0, 0);
    }

    private static int maxSoDV(Connection con) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maDV, 3, 18) AS INT)), 0) "
                + "FROM DICHVU WITH (UPDLOCK, HOLDLOCK) WHERE maDV LIKE 'DV%'";
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
