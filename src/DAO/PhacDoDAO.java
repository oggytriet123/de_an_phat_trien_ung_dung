package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.GiaiDoanDieuTri;
import Entity.NhatKyTheoDoi;
import Entity.PhacDoDieuTri;

/**
 * Quản lý dữ liệu Phác đồ điều trị trong SQL Server (PHACDO_DIEUTRI, GIAIDOAN_DIEUTRI, NHATKY_THEODOI).
 */
public class PhacDoDAO {

    public List<PhacDoDieuTri> layTatCa() {
        List<PhacDoDieuTri> ds = new ArrayList<>();
        String sql = "SELECT maPhacDo, tenPhacDo, maThuCung, tenThuCung, loaiThuCung, giongLoai, canNang, "
                + "       tenChuNuoi, soDienThoai, bacSi, ngayBatDau, ngayKetThuc, mucDo, chanDoan, "
                + "       mucTieu, cheDoDinhDuong, tienDo, trangThai, maDonThuoc, nhatKyLog "
                + "FROM PHACDO_DIEUTRI ORDER BY ngayBatDau DESC, maPhacDo DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String maPD = rs.getString("maPhacDo");
                String tenPD = rs.getString("tenPhacDo");
                String maTC = rs.getString("maThuCung");
                String tenTC = rs.getString("tenThuCung");
                String loai = rs.getString("loaiThuCung");
                String giong = rs.getString("giongLoai");
                double canNang = rs.getDouble("canNang");
                String tenChu = rs.getString("tenChuNuoi");
                String sdt = rs.getString("soDienThoai");
                String bacSi = rs.getString("bacSi");
                Date ngayBdSql = rs.getDate("ngayBatDau");
                LocalDate ngayBd = ngayBdSql != null ? ngayBdSql.toLocalDate() : LocalDate.now();
                Date ngayKtSql = rs.getDate("ngayKetThuc");
                LocalDate ngayKt = ngayKtSql != null ? ngayKtSql.toLocalDate() : LocalDate.now().plusDays(7);
                String mucDo = rs.getString("mucDo");
                String chanDoan = rs.getString("chanDoan");
                String mucTieu = rs.getString("mucTieu");
                String dinhDuong = rs.getString("cheDoDinhDuong");
                int tienDo = rs.getInt("tienDo");
                String trangThai = rs.getString("trangThai");
                String maDT = rs.getString("maDonThuoc");

                PhacDoDieuTri pd = new PhacDoDieuTri(maPD, tenPD, maTC, tenTC, loai, giong, canNang,
                        tenChu, sdt, bacSi, ngayBd, ngayKt, mucDo, chanDoan, mucTieu, dinhDuong, tienDo, trangThai, maDT);

                String log = rs.getString("nhatKyLog");
                if (log != null && !log.trim().isEmpty()) {
                    String[] lines = log.split("\\|");
                    for (String l : lines) if (!l.trim().isEmpty()) pd.addLichSuRaw(l.trim());
                }
                ds.add(pd);
            }
        } catch (SQLException e) {
            // Ngắt kết nối SQL thì trả về danh sách rỗng, không tự tạo data ảo
            return ds;
        }

        // Đọc giai đoạn điều trị
        String sqlGD = "SELECT maPhacDo, soThuTu, tenGiaiDoan, thoiGianUocTinh, mucTieuGiaiDoan, phuongPhap, trangThai "
                + "FROM GIAIDOAN_DIEUTRI ORDER BY soThuTu ASC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sqlGD)) {
            while (rs.next()) {
                String maPD = rs.getString("maPhacDo");
                for (PhacDoDieuTri pd : ds) {
                    if (pd.getMaPhacDo().equalsIgnoreCase(maPD)) {
                        pd.themGiaiDoan(new GiaiDoanDieuTri(
                                rs.getInt("soThuTu"),
                                rs.getString("tenGiaiDoan"),
                                rs.getString("thoiGianUocTinh"),
                                rs.getString("mucTieuGiaiDoan"),
                                rs.getString("phuongPhap"),
                                rs.getString("trangThai")
                        ));
                        break;
                    }
                }
            }
        } catch (SQLException ignored) {}

        // Đọc nhật ký theo dõi
        String sqlNK = "SELECT maPhacDo, thoiGian, bacSi, canNang, nhietDo, tinhTrang, ghiChu "
                + "FROM NHATKY_THEODOI ORDER BY thoiGian DESC";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sqlNK)) {
            while (rs.next()) {
                String maPD = rs.getString("maPhacDo");
                for (PhacDoDieuTri pd : ds) {
                    if (pd.getMaPhacDo().equalsIgnoreCase(maPD)) {
                        Timestamp ts = rs.getTimestamp("thoiGian");
                        LocalDateTime tg = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                        pd.themNhatKy(new NhatKyTheoDoi(
                                tg,
                                rs.getString("bacSi"),
                                rs.getDouble("canNang"),
                                rs.getDouble("nhietDo"),
                                rs.getString("tinhTrang"),
                                rs.getString("ghiChu")
                        ));
                        break;
                    }
                }
            }
        } catch (SQLException ignored) {}

        return ds;
    }

    public void them(PhacDoDieuTri pd) throws SQLException {
        String sql = "INSERT INTO PHACDO_DIEUTRI (maPhacDo, tenPhacDo, maThuCung, tenThuCung, loaiThuCung, giongLoai, "
                + "  canNang, tenChuNuoi, soDienThoai, bacSi, ngayBatDau, ngayKetThuc, mucDo, chanDoan, mucTieu, "
                + "  cheDoDinhDuong, tienDo, trangThai, maDonThuoc, nhatKyLog) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pd.getMaPhacDo());
            ps.setString(2, pd.getTenPhacDo());
            ps.setString(3, pd.getMaTC());
            ps.setString(4, pd.getTenThuCung());
            ps.setString(5, pd.getLoai());
            ps.setString(6, pd.getGiong());
            ps.setDouble(7, pd.getCanNang());
            ps.setString(8, pd.getTenChu());
            ps.setString(9, pd.getSdtChu());
            ps.setString(10, pd.getBacSi());
            ps.setDate(11, pd.getNgayBatDau() != null ? Date.valueOf(pd.getNgayBatDau()) : Date.valueOf(LocalDate.now()));
            ps.setDate(12, pd.getNgayKetThuc() != null ? Date.valueOf(pd.getNgayKetThuc()) : Date.valueOf(LocalDate.now().plusDays(7)));
            ps.setString(13, pd.getMucDo());
            ps.setString(14, pd.getChanDoan());
            ps.setString(15, pd.getMucTieuDieuTri());
            ps.setString(16, pd.getCheDoDinhDuong());
            ps.setInt(17, pd.getTienDo());
            ps.setString(18, pd.getTrangThai());
            ps.setString(19, pd.getMaDonThuoc());
            ps.setString(20, String.join("|", pd.getLichSu()));
            ps.executeUpdate();
        }
        luuGiaiDoanVaNhatKy(pd);
    }

    public void capNhat(PhacDoDieuTri pd) throws SQLException {
        String sql = "UPDATE PHACDO_DIEUTRI SET tenPhacDo = ?, maThuCung = ?, tenThuCung = ?, loaiThuCung = ?, giongLoai = ?, "
                + "  canNang = ?, tenChuNuoi = ?, soDienThoai = ?, bacSi = ?, ngayBatDau = ?, ngayKetThuc = ?, mucDo = ?, "
                + "  chanDoan = ?, mucTieu = ?, cheDoDinhDuong = ?, tienDo = ?, trangThai = ?, maDonThuoc = ?, nhatKyLog = ? "
                + "WHERE maPhacDo = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pd.getTenPhacDo());
            ps.setString(2, pd.getMaTC());
            ps.setString(3, pd.getTenThuCung());
            ps.setString(4, pd.getLoai());
            ps.setString(5, pd.getGiong());
            ps.setDouble(6, pd.getCanNang());
            ps.setString(7, pd.getTenChu());
            ps.setString(8, pd.getSdtChu());
            ps.setString(9, pd.getBacSi());
            ps.setDate(10, pd.getNgayBatDau() != null ? Date.valueOf(pd.getNgayBatDau()) : Date.valueOf(LocalDate.now()));
            ps.setDate(11, pd.getNgayKetThuc() != null ? Date.valueOf(pd.getNgayKetThuc()) : Date.valueOf(LocalDate.now().plusDays(7)));
            ps.setString(12, pd.getMucDo());
            ps.setString(13, pd.getChanDoan());
            ps.setString(14, pd.getMucTieuDieuTri());
            ps.setString(15, pd.getCheDoDinhDuong());
            ps.setInt(16, pd.getTienDo());
            ps.setString(17, pd.getTrangThai());
            ps.setString(18, pd.getMaDonThuoc());
            ps.setString(19, String.join("|", pd.getLichSu()));
            ps.setString(20, pd.getMaPhacDo());
            ps.executeUpdate();
        }
        luuGiaiDoanVaNhatKy(pd);
    }

    public void xoa(String maPD) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM GIAIDOAN_DIEUTRI WHERE maPhacDo = ?")) {
                ps.setString(1, maPD);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM NHATKY_THEODOI WHERE maPhacDo = ?")) {
                ps.setString(1, maPD);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PHACDO_DIEUTRI WHERE maPhacDo = ?")) {
                ps.setString(1, maPD);
                ps.executeUpdate();
            }
        }
    }

    private void luuGiaiDoanVaNhatKy(PhacDoDieuTri pd) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            try (PreparedStatement del = con.prepareStatement("DELETE FROM GIAIDOAN_DIEUTRI WHERE maPhacDo = ?")) {
                del.setString(1, pd.getMaPhacDo());
                del.executeUpdate();
            }
            if (pd.getCacGiaiDoan() != null && !pd.getCacGiaiDoan().isEmpty()) {
                String ins = "INSERT INTO GIAIDOAN_DIEUTRI (maPhacDo, soThuTu, tenGiaiDoan, thoiGianUocTinh, mucTieuGiaiDoan, phuongPhap, trangThai) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(ins)) {
                    for (GiaiDoanDieuTri gd : pd.getCacGiaiDoan()) {
                        ps.setString(1, pd.getMaPhacDo());
                        ps.setInt(2, gd.getThuTu());
                        ps.setString(3, gd.getTenGiaiDoan());
                        ps.setString(4, gd.getThoiGian());
                        ps.setString(5, gd.getMucTieu());
                        ps.setString(6, gd.getChiDinh());
                        ps.setString(7, gd.getTrangThai());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            try (PreparedStatement del = con.prepareStatement("DELETE FROM NHATKY_THEODOI WHERE maPhacDo = ?")) {
                del.setString(1, pd.getMaPhacDo());
                del.executeUpdate();
            }
            if (pd.getNhatKy() != null && !pd.getNhatKy().isEmpty()) {
                String ins = "INSERT INTO NHATKY_THEODOI (maPhacDo, thoiGian, bacSi, canNang, nhietDo, tinhTrang, ghiChu) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(ins)) {
                    for (NhatKyTheoDoi nk : pd.getNhatKy()) {
                        ps.setString(1, pd.getMaPhacDo());
                        ps.setTimestamp(2, nk.getThoiGian() != null ? Timestamp.valueOf(nk.getThoiGian()) : Timestamp.valueOf(LocalDateTime.now()));
                        ps.setString(3, nk.getBacSi());
                        ps.setDouble(4, nk.getCanNang());
                        ps.setDouble(5, nk.getThanNhiet());
                        ps.setString(6, nk.getTinhTrang());
                        ps.setString(7, nk.getGhiChu());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }
}
