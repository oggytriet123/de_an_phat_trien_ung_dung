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
import Entity.NhanVien;
import Entity.ThongKeNhanVien;

/**
 * Lớp truy xuất cơ sở dữ liệu cho module Quản lý Nhân viên (bảng NHANVIEN & các vai trò phụ thuộc).
 */
public class NhanVienDAO {

    public NhanVienDAO() {
        damBaoCauTrucBang();
    }

    /**
     * Tự động bổ sung các cột mới nếu CSDL chưa có (Auto DDL Migration).
     */
    private void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF COL_LENGTH('NHANVIEN', 'trangThai') IS NULL ALTER TABLE NHANVIEN ADD trangThai NVARCHAR(30) NULL");
            st.executeUpdate("IF COL_LENGTH('NHANVIEN', 'caLamViec') IS NULL ALTER TABLE NHANVIEN ADD caLamViec NVARCHAR(30) NULL");
            st.executeUpdate("IF COL_LENGTH('NHANVIEN', 'ngayVaoLam') IS NULL ALTER TABLE NHANVIEN ADD ngayVaoLam DATE NULL");
        } catch (SQLException ignored) {
        }
    }

    /**
     * Nhóm quyền từ bảng PHANQUYEN để hiển thị trong ComboBox.
     */
    public static class QuyenOption {
        public final int maQuyen;
        public final String tenQuyen;

        public QuyenOption(int maQuyen, String tenQuyen) {
            this.maQuyen = maQuyen;
            this.tenQuyen = tenQuyen;
        }

        @Override
        public String toString() {
            return tenQuyen;
        }
    }

    /**
     * Lấy danh sách toàn bộ nhân viên trong hệ thống kèm vai trò và chuyên môn bác sĩ.
     */
    public List<NhanVien> layTatCa() throws SQLException {
        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT nv.maNV, nv.hoTen, nv.tenDangNhap, nv.matKhau, nv.maQuyen, "
                + "       ISNULL(pq.tenQuyen, N'Chưa phân quyền') AS tenQuyen, "
                + "       ISNULL(nv.soDienThoai, N'') AS soDienThoai, "
                + "       nv.ngaySinh, "
                + "       ISNULL(nv.email, N'') AS email, "
                + "       ISNULL(bs.chuyenMon, N'') AS chuyenMon, "
                + "       ISNULL(nv.trangThai, N'Đang làm việc') AS trangThai, "
                + "       ISNULL(nv.caLamViec, N'Toàn thời gian') AS caLamViec, "
                + "       nv.ngayVaoLam "
                + "FROM NHANVIEN nv "
                + "LEFT JOIN PHANQUYEN pq ON nv.maQuyen = pq.maQuyen "
                + "LEFT JOIN NHANVIEN_BACSI bs ON nv.maNV = bs.maNV "
                + "ORDER BY LEN(nv.maNV), nv.maNV";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String hoTen = rs.getString("hoTen");
                String tenDangNhap = rs.getString("tenDangNhap");
                String matKhau = rs.getString("matKhau");
                int maQuyen = rs.getInt("maQuyen");
                String tenQuyen = rs.getString("tenQuyen");
                String sdt = rs.getString("soDienThoai");
                Date nsSQL = rs.getDate("ngaySinh");
                LocalDate ngaySinh = (nsSQL != null) ? nsSQL.toLocalDate() : null;
                String email = rs.getString("email");
                String chuyenMon = rs.getString("chuyenMon");
                String trangThai = rs.getString("trangThai");
                String caLamViec = rs.getString("caLamViec");
                Date nvlSQL = rs.getDate("ngayVaoLam");
                LocalDate ngayVaoLam = (nvlSQL != null) ? nvlSQL.toLocalDate() : LocalDate.now();

                ds.add(new NhanVien(maNV, hoTen, tenDangNhap, matKhau, maQuyen, tenQuyen,
                        sdt, ngaySinh, email, chuyenMon, trangThai, caLamViec, ngayVaoLam));
            }
        }
        return ds;
    }

    /**
     * Lấy danh sách nhóm quyền từ bảng PHANQUYEN.
     */
    public List<QuyenOption> layDanhSachQuyen() throws SQLException {
        List<QuyenOption> list = new ArrayList<>();
        String sql = "SELECT maQuyen, tenQuyen FROM PHANQUYEN ORDER BY maQuyen";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new QuyenOption(rs.getInt("maQuyen"), rs.getString("tenQuyen")));
            }
        }
        return list;
    }

    /**
     * Kiểm tra tên đăng nhập đã tồn tại trong CSDL hay chưa.
     */
    public boolean tonTaiTenDangNhap(String tenDangNhap, String boQuaMaNV) throws SQLException {
        String sql = "SELECT COUNT(*) FROM NHANVIEN WHERE tenDangNhap = ? AND (maNV <> ? OR ? IS NULL)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, boQuaMaNV != null ? boQuaMaNV : "");
            ps.setString(3, boQuaMaNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Thêm mới một nhân viên cùng bảng vai trò phụ thuộc. Tự động sinh mã NVxxx.
     * @return mã nhân viên vừa tạo (ví dụ: NV011)
     */
    public String them(NhanVien nv, String matKhauHash) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                int nextId = maxSoNV(con) + 1;
                String maNV = String.format("NV%03d", nextId);

                String sql = "INSERT INTO NHANVIEN (maNV, hoTen, tenDangNhap, matKhau, maQuyen, "
                        + "                   soDienThoai, ngaySinh, email, trangThai, caLamViec, ngayVaoLam) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maNV);
                    ps.setString(2, nv.getHoTen());
                    ps.setString(3, nv.getTenDangNhap());
                    ps.setString(4, matKhauHash);
                    ps.setInt(5, nv.getMaQuyen());
                    ps.setString(6, nv.getSoDienThoai());
                    if (nv.getNgaySinh() != null) ps.setDate(7, Date.valueOf(nv.getNgaySinh()));
                    else ps.setNull(7, java.sql.Types.DATE);
                    ps.setString(8, nv.getEmail());
                    ps.setString(9, nv.getTrangThai());
                    ps.setString(10, nv.getCaLamViec());
                    LocalDate nvl = nv.getNgayVaoLam() != null ? nv.getNgayVaoLam() : LocalDate.now();
                    ps.setDate(11, Date.valueOf(nvl));
                    ps.executeUpdate();
                }

                // Cập nhật vào bảng phân cấp chức vụ tương ứng
                capNhatBangVaiTro(con, maNV, nv.getMaQuyen(), nv.getChuyenMon());

                con.commit();
                return maNV;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    /**
     * Cập nhật thông tin hồ sơ nhân viên.
     * @param matKhauMoiHash nếu null hoặc rỗng thì giữ nguyên mật khẩu cũ.
     */
    public boolean capNhat(NhanVien nv, String matKhauMoiHash) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                StringBuilder sql = new StringBuilder("UPDATE NHANVIEN SET hoTen = ?, tenDangNhap = ?, maQuyen = ?, "
                        + "soDienThoai = ?, ngaySinh = ?, email = ?, trangThai = ?, caLamViec = ? ");
                if (matKhauMoiHash != null && !matKhauMoiHash.isEmpty()) {
                    sql.append(", matKhau = ? ");
                }
                sql.append("WHERE maNV = ?");

                try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                    int idx = 1;
                    ps.setString(idx++, nv.getHoTen());
                    ps.setString(idx++, nv.getTenDangNhap());
                    ps.setInt(idx++, nv.getMaQuyen());
                    ps.setString(idx++, nv.getSoDienThoai());
                    if (nv.getNgaySinh() != null) ps.setDate(idx++, Date.valueOf(nv.getNgaySinh()));
                    else ps.setNull(idx++, java.sql.Types.DATE);
                    ps.setString(idx++, nv.getEmail());
                    ps.setString(idx++, nv.getTrangThai());
                    ps.setString(idx++, nv.getCaLamViec());
                    if (matKhauMoiHash != null && !matKhauMoiHash.isEmpty()) {
                        ps.setString(idx++, matKhauMoiHash);
                    }
                    ps.setString(idx, nv.getMaNV());
                    ps.executeUpdate();
                }

                // Cập nhật lại bảng vai trò phụ thuộc
                capNhatBangVaiTro(con, nv.getMaNV(), nv.getMaQuyen(), nv.getChuyenMon());

                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private void capNhatBangVaiTro(Connection con, String maNV, int maQuyen, String chuyenMon) throws SQLException {
        // Gỡ khỏi các bảng con cũ
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM QUANLY WHERE maNV = '" + maNV + "'");
            st.executeUpdate("DELETE FROM NHANVIEN_BANHANG WHERE maNV = '" + maNV + "'");
            st.executeUpdate("DELETE FROM NHANVIEN_KHO WHERE maNV = '" + maNV + "'");
            st.executeUpdate("DELETE FROM NHANVIEN_BACSI WHERE maNV = '" + maNV + "'");
        }

        // Thêm vào bảng con tương ứng với maQuyen
        switch (maQuyen) {
            case 1: // Quản lý
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO QUANLY (maNV) VALUES (?)")) {
                    ps.setString(1, maNV);
                    ps.executeUpdate();
                }
                break;
            case 2: // Bán hàng
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO NHANVIEN_BANHANG (maNV) VALUES (?)")) {
                    ps.setString(1, maNV);
                    ps.executeUpdate();
                }
                break;
            case 3: // Kho
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO NHANVIEN_KHO (maNV) VALUES (?)")) {
                    ps.setString(1, maNV);
                    ps.executeUpdate();
                }
                break;
            case 4: // Bác sĩ
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO NHANVIEN_BACSI (maNV, chuyenMon) VALUES (?, ?)")) {
                    ps.setString(1, maNV);
                    ps.setString(2, (chuyenMon != null && !chuyenMon.trim().isEmpty()) ? chuyenMon : "Bác sĩ thú y");
                    ps.executeUpdate();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Kiểm tra tính toàn vẹn dữ liệu trước khi xóa nhân viên.
     * @return chuỗi thông báo nếu có ràng buộc, null nếu có thể xóa an toàn.
     */
    public String kiemTraRangBuocTruocKhiXoa(String maNV, String maNVDangNhap) throws SQLException {
        if (maNV.equalsIgnoreCase(maNVDangNhap)) {
            return "Không thể xóa tài khoản của chính bạn đang đăng nhập!";
        }

        try (Connection con = ConnectDB.getConnection()) {
            // 1. Kiểm tra khách hàng do nhân viên phụ trách
            if (bangTonTai(con, "KHACHHANG")) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM KHACHHANG WHERE maNV_BH = ?")) {
                    ps.setString(1, maNV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return "Nhân viên đang phụ trách quản lý " + rs.getInt(1) + " khách hàng.\n"
                                    + "Gợi ý: Hãy chuyển trạng thái nhân viên sang \"Đã nghỉ việc\" để bảo tồn dữ liệu.";
                        }
                    }
                }
            }

            // 2. Kiểm tra sản phẩm do nhân viên kho quản lý
            if (bangTonTai(con, "SANPHAM")) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM SANPHAM WHERE maNV_Kho = ?")) {
                    ps.setString(1, maNV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return "Nhân viên đang là thủ kho phụ trách " + rs.getInt(1) + " sản phẩm.\n"
                                    + "Không thể xóa trực tiếp để tránh mất dấu kiểm kê kho.";
                        }
                    }
                }
            }

            // 3. Kiểm tra lịch hẹn khám do bác sĩ phụ trách
            if (bangTonTai(con, "LICHHEN")) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM LICHHEN WHERE maNV_BS = ?")) {
                    ps.setString(1, maNV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return "Bác sĩ đang có " + rs.getInt(1) + " lịch hẹn khám bệnh / tiêm phòng liên kết.\n"
                                    + "Hãy chuyển trạng thái nhân viên sang \"Đã nghỉ việc\".";
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Xóa hồ sơ nhân viên.
     */
    public boolean xoa(String maNV) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Xóa ở các bảng vai trò con trước
                try (Statement st = con.createStatement()) {
                    st.executeUpdate("DELETE FROM QUANLY WHERE maNV = '" + maNV + "'");
                    st.executeUpdate("DELETE FROM NHANVIEN_BANHANG WHERE maNV = '" + maNV + "'");
                    st.executeUpdate("DELETE FROM NHANVIEN_KHO WHERE maNV = '" + maNV + "'");
                    st.executeUpdate("DELETE FROM NHANVIEN_BACSI WHERE maNV = '" + maNV + "'");
                }

                // Xóa bảng chính NHANVIEN
                int count;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM NHANVIEN WHERE maNV = ?")) {
                    ps.setString(1, maNV);
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
     * Thống kê số lượng nhân sự theo từng chức danh và trạng thái.
     */
    public ThongKeNhanVien layThongKe() throws SQLException {
        String sql = "SELECT "
                + " (SELECT COUNT(*) FROM NHANVIEN) AS tong, "
                + " (SELECT COUNT(*) FROM NHANVIEN WHERE maQuyen = 2) AS soBanHang, "
                + " (SELECT COUNT(*) FROM NHANVIEN WHERE maQuyen = 4) AS soBacSi, "
                + " (SELECT COUNT(*) FROM NHANVIEN WHERE maQuyen = 3) AS soKho, "
                + " (SELECT COUNT(*) FROM NHANVIEN WHERE maQuyen = 1) AS soQuanLy, "
                + " (SELECT COUNT(*) FROM NHANVIEN WHERE trangThai = N'Đang làm việc') AS soDangLamViec";

        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return new ThongKeNhanVien(
                        rs.getInt("tong"),
                        rs.getInt("soBanHang"),
                        rs.getInt("soBacSi"),
                        rs.getInt("soKho"),
                        rs.getInt("soQuanLy"),
                        rs.getInt("soDangLamViec")
                );
            }
        }
        return new ThongKeNhanVien(0, 0, 0, 0, 0, 0);
    }

    private static int maxSoNV(Connection con) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(maNV, 3, 18) AS INT)), 0) "
                + "FROM NHANVIEN WITH (UPDLOCK, HOLDLOCK) WHERE maNV LIKE 'NV%'";
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
