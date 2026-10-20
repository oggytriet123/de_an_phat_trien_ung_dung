package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ConnectDB.ConnectDB;
import Entity.KhachHang;
import Entity.ThongKeKhachHang;
import Entity.ThuCung;

/** Truy cập bảng KHACHHANG (kèm THUCUNG, HOADON để tính thú cưng / giao dịch / chi tiêu). */
public class KhachHangDAO {

    /** Toàn bộ khách hàng. Số điện thoại trả về dạng chỉ gồm chữ số (BUS sẽ định dạng lại để hiển thị). */
    public List<KhachHang> layTatCa() throws SQLException {
        String sqlKh = "SELECT k.maKH, k.hoTen, k.soDienThoai, k.hangThe, ISNULL(k.diemTichLuy, 0), "
                + "       hd.ngayGanNhat, ISNULL(hd.tong, 0) "
                + "FROM KHACHHANG k "
                + "OUTER APPLY (SELECT MAX(h.ngayLap) AS ngayGanNhat, SUM(h.tongTien) AS tong "
                + "             FROM HOADON h WHERE h.maKH = k.maKH) hd "
                + "ORDER BY k.maKH";
        String sqlPet = "SELECT maKH, ten FROM THUCUNG WHERE maKH IS NOT NULL ORDER BY LEN(petID), petID";

        try (Connection con = ConnectDB.getConnection()) {
            Map<String, List<String>> pets = new LinkedHashMap<>();
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlPet)) {
                while (rs.next()) pets.computeIfAbsent(rs.getString(1), x -> new ArrayList<>()).add(rs.getString(2));
            }

            List<KhachHang> ds = new ArrayList<>();
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlKh)) {
                while (rs.next()) {
                    String ma = rs.getString(1);
                    String hang = rs.getString(4);
                    Date ngay = rs.getDate(6);
                    List<String> p = pets.get(ma);
                    ds.add(new KhachHang(ma, rs.getString(2),
                            rs.getString(3) == null ? "" : rs.getString(3),
                            hang == null ? "Thường" : hang,
                            rs.getInt(5),
                            p == null || p.isEmpty() ? "—" : String.join(", ", p),
                            ngay == null ? null : ngay.toLocalDate(),
                            Math.round(rs.getDouble(7))));
                }
            }
            return ds;
        }
    }

    public boolean tonTaiSoDienThoai(String sdtChiSo) throws SQLException {
        return tonTaiSoDienThoai(sdtChiSo, null);
    }

    public boolean tonTaiSoDienThoai(String sdtChiSo, String boQuaMaKH) throws SQLException {
        String sql = "SELECT COUNT(*) FROM KHACHHANG WHERE REPLACE(soDienThoai, N' ', N'') = ? AND (maKH <> ? OR ? IS NULL)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdtChiSo);
            ps.setString(2, boQuaMaKH != null ? boQuaMaKH : "");
            ps.setString(3, boQuaMaKH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** 1 điểm cho mỗi 10.000₫ thanh toán. */
    public static final long SO_TIEN_MOI_DIEM = 10_000;

    /** Hạng thẻ theo điểm tích lũy: Thường (<100) · Bạc (từ 100) · Vàng (từ 200) · VIP (từ 500). */
    public static String tinhHang(int diem) {
        if (diem >= 500) return "VIP";
        if (diem >= 200) return "Vàng";
        if (diem >= 100) return "Bạc";
        return "Thường";
    }

    /** Loài chính ("Chó Poodle" -> "Chó") để lưu vào THUCUNG.loai; giữ nguyên nếu không nhận ra. */
    public static String loaiChinh(String loai) {
        if (loai == null) return null;
        String l = loai.trim();
        if (l.isEmpty()) return null;
        for (String g : new String[] {"Chó", "Mèo", "Thỏ"}) {
            if (l.regionMatches(true, 0, g, 0, g.length())) return g;
        }
        return l.length() > 50 ? l.substring(0, 50) : l;
    }

    private static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    /** Tìm khách theo số điện thoại (chỉ gồm chữ số), kèm danh sách thú cưng; null nếu chưa có. */
    public KhachHang timTheoSdt(String sdtChiSo) throws SQLException {
        String sql = "SELECT maKH, hoTen, soDienThoai, hangThe, ISNULL(diemTichLuy, 0) FROM KHACHHANG "
                + "WHERE REPLACE(soDienThoai, N' ', N'') = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, chiSo(sdtChiSo));
            String ma, ten, sdt, hang;
            int diem;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ma = rs.getString(1);
                ten = rs.getString(2);
                sdt = rs.getString(3) == null ? "" : rs.getString(3);
                hang = rs.getString(4) == null ? "Thường" : rs.getString(4);
                diem = rs.getInt(5);
            }
            List<ThuCung> pets = new ArrayList<>();
            try (PreparedStatement pp = con.prepareStatement(
                    "SELECT petID, ten, loai, canNang FROM THUCUNG WHERE maKH = ? ORDER BY LEN(petID), petID")) {
                pp.setString(1, ma);
                try (ResultSet rs = pp.executeQuery()) {
                    while (rs.next()) {
                        pets.add(new ThuCung(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4)));
                    }
                }
            }
            List<String> tenPet = new ArrayList<>();
            for (ThuCung t : pets) tenPet.add(t.getTen());
            KhachHang k = new KhachHang(ma, ten, sdt, hang, diem,
                    tenPet.isEmpty() ? "—" : String.join(", ", tenPet), null, 0);
            k.setDanhSachThuCung(pets);
            return k;
        }
    }

    /**
     * Thêm khách hàng mới cùng các thú cưng trong một giao dịch. Hạng thẻ tự tính theo điểm.
     * @return mã khách hàng vừa tạo (KH001, KH002, ...)
     */
    public String them(String hoTen, String sdtChiSo, int diem, List<ThuCung> thuCung) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maKH = taoKhach(con, hoTen, sdtChiSo, Math.max(0, diem));
                for (ThuCung t : thuCung) taoThuCung(con, maKH, t.getTen(), t.getLoai(), t.getCanNang());
                con.commit();
                return maKH;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Cập nhật thông tin khách hàng (họ tên, SĐT, điểm tích lũy, tự tính hạng thẻ). */
    public boolean capNhat(String maKH, String hoTen, String sdtChiSo, int diem) throws SQLException {
        String sql = "UPDATE KHACHHANG SET hoTen = ?, soDienThoai = ?, diemTichLuy = ?, hangThe = ? WHERE maKH = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, sdtChiSo);
            ps.setInt(3, diem);
            ps.setString(4, tinhHang(diem));
            ps.setString(5, maKH);
            return ps.executeUpdate() > 0;
        }
    }

    /** Kiểm tra ràng buộc toàn vẹn trước khi xóa khách hàng. */
    public String kiemTraRangBuocTruocKhiXoa(String maKH) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM HOADON WHERE maKH = ?")) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Khách hàng đang có " + rs.getInt(1) + " hóa đơn trong lịch sử. Không thể xóa để bảo toàn doanh thu!";
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM PHIEU_LUUTRU WHERE maKH = ? AND trangThai IN (N'Đang lưu trú', N'Đã đặt', N'Chưa nhận')")) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Khách hàng đang có phiếu lưu trú lồng chuồng đang hoạt động. Không thể xóa!";
                    }
                }
            }
        }
        return null;
    }

    /** Xóa khách hàng an toàn. */
    public boolean xoa(String maKH) throws SQLException {
        String err = kiemTraRangBuocTruocKhiXoa(maKH);
        if (err != null) throw new SQLException(err);
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE PHIEU_LUUTRU SET maKH = NULL WHERE maKH = ?")) {
                    ps.setString(1, maKH);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM THUCUNG WHERE maKH = ?")) {
                    ps.setString(1, maKH);
                    ps.executeUpdate();
                }
                int count;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM KHACHHANG WHERE maKH = ?")) {
                    ps.setString(1, maKH);
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

    private static String taoKhach(Connection con, String hoTen, String sdtChiSo, int diem) throws SQLException {
        String maKH = String.format("KH%03d", maxSo(con, "KHACHHANG", "maKH", 2, "KH") + 1);
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO KHACHHANG (maKH, hoTen, soDienThoai, diemTichLuy, hangThe) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, maKH);
            ps.setString(2, hoTen);
            ps.setString(3, sdtChiSo);
            ps.setInt(4, diem);
            ps.setString(5, tinhHang(diem));
            ps.executeUpdate();
        }
        return maKH;
    }

    private static String taoThuCung(Connection con, String maKH, String ten, String loai, double canNang) throws SQLException {
        String petID = String.format("PET%03d", maxSo(con, "THUCUNG", "petID", 3, "PET") + 1);
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO THUCUNG (petID, ten, loai, canNang, maKH) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, petID);
            ps.setString(2, ten);
            String l = loaiChinh(loai);
            if (l == null) ps.setNull(3, java.sql.Types.NVARCHAR); else ps.setString(3, l);
            if (canNang > 0) ps.setDouble(4, canNang); else ps.setNull(4, java.sql.Types.FLOAT);
            ps.setString(5, maKH);
            ps.executeUpdate();
        }
        return petID;
    }

    /**
     * Gắn phiếu lưu trú với khách hàng + thú cưng thật trong CSDL:
     * khách chưa có (theo SĐT) thì tạo mới (Thường, 0 điểm); thú cưng chưa có (theo tên, cùng chủ) thì tạo mới.
     * @return {maKH, petID}; petID = null nếu tenPet trống
     */
    public String[] xacDinhKhachVaThuCung(String hoTen, String sdt, String tenPet, String loai, double canNang) throws SQLException {
        String so = chiSo(sdt);
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maKH = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT maKH FROM KHACHHANG WITH (UPDLOCK, HOLDLOCK) WHERE REPLACE(soDienThoai, N' ', N'') = ?")) {
                    ps.setString(1, so);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) maKH = rs.getString(1);
                    }
                }
                if (maKH == null) maKH = taoKhach(con, hoTen == null ? so : hoTen.trim(), so, 0);

                String petID = null;
                String ten = tenPet == null ? "" : tenPet.trim();
                if (!ten.isEmpty()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT TOP 1 petID FROM THUCUNG WHERE maKH = ? AND ten = ? ORDER BY LEN(petID), petID")) {
                        ps.setString(1, maKH);
                        ps.setString(2, ten);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) petID = rs.getString(1);
                        }
                    }
                    if (petID == null) petID = taoThuCung(con, maKH, ten, loai, canNang);
                }
                con.commit();
                return new String[] {maKH, petID};
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Cộng điểm khi thanh toán (1 điểm / 10.000₫) và nâng hạng thẻ nếu đủ điểm. Dùng chung giao dịch của người gọi. @return số điểm đã cộng */
    static int tichDiem(Connection con, String maKH, long soTien) throws SQLException {
        long cong = soTien / SO_TIEN_MOI_DIEM;
        if (maKH == null || cong <= 0) return 0;
        int cu = 0;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ISNULL(diemTichLuy, 0) FROM KHACHHANG WITH (UPDLOCK) WHERE maKH = ?")) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                cu = rs.getInt(1);
            }
        }
        int moi = (int) Math.min(Integer.MAX_VALUE, cu + cong);
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE KHACHHANG SET diemTichLuy = ?, hangThe = ? WHERE maKH = ?")) {
            ps.setInt(1, moi);
            ps.setString(2, tinhHang(moi));
            ps.setString(3, maKH);
            ps.executeUpdate();
        }
        return (int) cong;
    }

    /** Số lớn nhất trong mã dạng tiền tố + số (KH012 -> 12); 0 nếu bảng chưa có dòng nào. Khóa bảng đến hết giao dịch. */
    static int maxSo(Connection con, String bang, String cot, int doDaiTienTo, String tienTo) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(" + cot + ", " + (doDaiTienTo + 1) + ", 18) AS INT)), 0) "
                + "FROM " + bang + " WITH (UPDLOCK, HOLDLOCK) WHERE " + cot + " LIKE '" + tienTo + "%'";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public ThongKeKhachHang layThongKe() throws SQLException {
        String sql = "SET NOCOUNT ON; DECLARE @dau DATE = DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1); "
                + "SELECT "
                + " (SELECT COUNT(*) FROM KHACHHANG), "
                + " (SELECT COUNT(*) FROM KHACHHANG WHERE ngayTao >= @dau), "
                + " (SELECT COUNT(*) FROM KHACHHANG WHERE hangThe = N'VIP'), "
                + " (SELECT ISNULL(SUM(CAST(diemTichLuy AS BIGINT)), 0) FROM KHACHHANG), "
                + " (SELECT ISNULL(SUM(tongTien), 0) FROM HOADON WHERE maKH IS NOT NULL "
                + "     AND ngayLap >= @dau AND ngayLap < DATEADD(MONTH, 1, @dau)), "
                + " (SELECT ISNULL(SUM(tongTien), 0) FROM HOADON WHERE maKH IS NOT NULL "
                + "     AND ngayLap >= DATEADD(MONTH, -1, @dau) AND ngayLap < @dau)";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return new ThongKeKhachHang(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getLong(4),
                    Math.round(rs.getDouble(5)), Math.round(rs.getDouble(6)));
        }
    }
}
