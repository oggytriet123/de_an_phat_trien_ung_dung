package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ConnectDB.ConnectDB;
import Entity.ChiTietDichVuChuong;
import Entity.Chuong;
import Entity.PhieuDatChuong;

/**
 * Xử lý truy xuất và lưu trữ trực tiếp vào CSDL SQL Server:
 * - Bảng CHUONGLUUTRU: quản lý trạng thái, kích cỡ, giá các lồng chuồng
 * - Bảng PHIEU_LUUTRU: lưu thông tin thú cưng, chủ nuôi, ngày vào, ngày ra, cọc, ghi chú
 * - Bảng CHITIET_DICHVU_CHUONG: lưu dịch vụ và thức ăn sử dụng tại lồng
 */
public class ChuongDAO {

    public ChuongDAO() {
        // Đảm bảo dữ liệu bảng được cập nhật
    }

    /**
     * Lấy danh sách lồng chuồng từ SQL Server kèm thông tin thú cưng đang lưu trú / đã đặt.
     */
    public List<Chuong> getAllChuong() {
        List<Chuong> list = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                // 1. Lấy tất cả lồng từ CHUONGLUUTRU
                String sql = "SELECT * FROM CHUONGLUUTRU ORDER BY cageID";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(sql)) {
                    int stt = 1;
                    while (rs.next()) {
                        String id = rs.getString("cageID");
                        String sz = rs.getString("size");
                        String trangThai = rs.getString("trangThai");
                        double gia = rs.getDouble("giaTheoDem");

                        boolean isVip = (sz != null && sz.contains("VIP")) || id.contains("VIP");
                        String khuVuc = isVip ? "Khu VIP" :
                                (sz != null && sz.contains("Nhỏ") ? "Khu Mèo" :
                                (sz != null && sz.contains("Vừa") ? "Khu Chó Nhỏ" : "Khu Chó Lớn"));

                        Chuong c = new Chuong(id, stt++, "Lồng " + id, sz, khuVuc, trangThai, gia > 0 ? gia : 100000, isVip);
                        list.add(c);
                    }
                }

                // 2. Nạp thông tin phiếu lưu trú / đặt trước đang có hiệu lực
                String sqlPLT = "SELECT * FROM PHIEU_LUUTRU WHERE trangThai IN (N'Đang lưu trú', N'Đang sử dụng', N'Đã đặt') ORDER BY ngayVao DESC";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(sqlPLT)) {
                    while (rs.next()) {
                        String cageID = rs.getString("cageID");
                        for (Chuong c : list) {
                            if (c.getMaChuong().equalsIgnoreCase(cageID) && c.getTenPet() == null) {
                                c.setTenPet(rs.getString("tenPet"));
                                c.setLoaiPet(rs.getString("loaiPet"));
                                c.setTenKhach(rs.getString("tenKhach"));
                                c.setSdtKhach(rs.getString("sdtKhach"));
                                c.setGhiChu(rs.getString("ghiChu"));

                                Timestamp nv = rs.getTimestamp("ngayVao");
                                if (nv != null) c.setNgayVao(nv.toLocalDateTime());

                                Timestamp nr = rs.getTimestamp("ngayRaDuKien");
                                if (nr != null) c.setNgayRaDuKien(nr.toLocalDateTime());

                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Chuong getChuong(String maChuong) {
        if (maChuong == null) return null;
        for (Chuong c : getAllChuong()) {
            if (c.getMaChuong().equalsIgnoreCase(maChuong.trim())) {
                return c;
            }
        }
        return null;
    }

    public boolean capNhatTrangThai(String maChuong, String trangThai) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                String sql = "UPDATE CHUONGLUUTRU SET trangThai = ? WHERE cageID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, trangThai);
                    ps.setString(2, maChuong);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean nhanChuong(String maChuong, String tenPet, String loaiPet, double canNang,
                             String tenKhach, String sdtKhach, String ghiChu, LocalDateTime ngayRaDuKien) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                // Gắn phiếu với khách hàng + thú cưng thật (tạo mới nếu chưa có)
                String[] kp = new KhachHangDAO().xacDinhKhachVaThuCung(tenKhach, sdtKhach, tenPet, loaiPet, canNang);
                // Kiểm tra xem đã có phiếu đặt trước của lồng này chưa
                String sqlCheck = "SELECT maPhieu FROM PHIEU_LUUTRU WHERE cageID = ? AND trangThai = N'Đã đặt'";
                String maPhieu = null;
                try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                    ps.setString(1, maChuong);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maPhieu = rs.getString("maPhieu");
                        }
                    }
                }

                if (maPhieu != null) {
                    // Cập nhật phiếu đặt thành Đang lưu trú
                    String sqlUp = "UPDATE PHIEU_LUUTRU SET trangThai = N'Đang lưu trú', ngayVao = GETDATE(), maKH = ?, petID = ?, tenPet = ?, loaiPet = ?, tenKhach = ?, sdtKhach = ?, ghiChu = ? WHERE maPhieu = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUp)) {
                        ps.setString(1, kp[0]);
                        ps.setString(2, kp[1]);
                        ps.setString(3, tenPet);
                        ps.setString(4, loaiPet);
                        ps.setString(5, tenKhach);
                        ps.setString(6, sdtKhach);
                        ps.setString(7, ghiChu);
                        ps.setString(8, maPhieu);
                        ps.executeUpdate();
                    }
                } else {
                    // Tạo phiếu lưu trú mới
                    maPhieu = "PLT-" + System.currentTimeMillis() % 100000;
                    String sqlIns = "INSERT INTO PHIEU_LUUTRU (maPhieu, cageID, maKH, petID, tenPet, loaiPet, tenKhach, sdtKhach, ngayVao, ngayRaDuKien, trangThai, ghiChu) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, N'Đang lưu trú', ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                        ps.setString(1, maPhieu);
                        ps.setString(2, maChuong);
                        ps.setString(3, kp[0]);
                        ps.setString(4, kp[1]);
                        ps.setString(5, tenPet);
                        ps.setString(6, loaiPet);
                        ps.setString(7, tenKhach);
                        ps.setString(8, sdtKhach);
                        ps.setTimestamp(9, ngayRaDuKien != null ? Timestamp.valueOf(ngayRaDuKien) : Timestamp.valueOf(LocalDateTime.now().plusDays(2)));
                        ps.setString(10, ghiChu);
                        ps.executeUpdate();
                    }
                }

                capNhatTrangThai(maChuong, "Đang lưu trú");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean datChuong(PhieuDatChuong pdc) {
        if (pdc == null || pdc.getMaChuong() == null) return false;
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                // Gắn phiếu với khách hàng + thú cưng thật (tạo mới nếu chưa có)
                String[] kp = new KhachHangDAO().xacDinhKhachVaThuCung(
                        pdc.getTenKhach(), pdc.getSdtKhach(), pdc.getTenPet(), pdc.getLoaiPet(), pdc.getCanNang());
                String sql = "INSERT INTO PHIEU_LUUTRU (maPhieu, cageID, maKH, petID, tenPet, loaiPet, tenKhach, sdtKhach, ngayVao, ngayRaDuKien, tienCoc, trangThai, ghiChu) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, N'Đã đặt', ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, pdc.getMaPhieu());
                    ps.setString(2, pdc.getMaChuong());
                    ps.setString(3, kp[0]);
                    ps.setString(4, kp[1]);
                    ps.setString(5, pdc.getTenPet());
                    ps.setString(6, pdc.getLoaiPet());
                    ps.setString(7, pdc.getTenKhach());
                    ps.setString(8, pdc.getSdtKhach());
                    ps.setTimestamp(9, pdc.getNgayDen() != null ? Timestamp.valueOf(pdc.getNgayDen()) : null);
                    ps.setTimestamp(10, pdc.getNgayTraDuKien() != null ? Timestamp.valueOf(pdc.getNgayTraDuKien()) : null);
                    ps.setDouble(11, pdc.getTienCoc());
                    ps.setString(12, pdc.getGhiChu());
                    ps.executeUpdate();
                }

                capNhatTrangThai(pdc.getMaChuong(), "Đã đặt");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean huyDat(String maChuong) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                String sql = "UPDATE PHIEU_LUUTRU SET trangThai = N'Đã hủy' WHERE cageID = ? AND trangThai = N'Đã đặt'";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maChuong);
                    ps.executeUpdate();
                }
                capNhatTrangThai(maChuong, "Trống");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean chuyenChuong(String maChuongCu, String maChuongMoi) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                // Cập nhật lồng trong PHIEU_LUUTRU
                String sql = "UPDATE PHIEU_LUUTRU SET cageID = ? WHERE cageID = ? AND trangThai IN (N'Đang lưu trú', N'Đang sử dụng', N'Đã đặt')";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maChuongMoi);
                    ps.setString(2, maChuongCu);
                    ps.executeUpdate();
                }

                capNhatTrangThai(maChuongCu, "Trống");
                capNhatTrangThai(maChuongMoi, "Đang lưu trú");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Trả lồng + thanh toán: đóng phiếu, lập hóa đơn cho khách, cộng điểm tích lũy (1 điểm / 10.000₫)
     * và nâng hạng thẻ; sau đó đưa lồng về trạng thái Trống.
     */
    public boolean donChuong(String maChuong, double tongTien) {
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                conn.setAutoCommit(false);
                try {
                    String maKH = null;
                    String sqlFind = "SELECT TOP 1 maKH FROM PHIEU_LUUTRU WHERE cageID = ? AND trangThai IN (N'Đang lưu trú', N'Đang sử dụng') ORDER BY ngayVao DESC";
                    try (PreparedStatement ps = conn.prepareStatement(sqlFind)) {
                        ps.setString(1, maChuong);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) maKH = rs.getString(1);
                        }
                    }

                    // Cập nhật phiếu sang Đã trả phòng
                    String sql = "UPDATE PHIEU_LUUTRU SET trangThai = N'Đã trả phòng', ngayRaThucTe = GETDATE() WHERE cageID = ? AND trangThai IN (N'Đang lưu trú', N'Đang sử dụng')";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, maChuong);
                        ps.executeUpdate();
                    }

                    if (maKH != null && tongTien > 0) {
                        String maHD = String.format("HD%03d", KhachHangDAO.maxSo(conn, "HOADON", "maHD", 2, "HD") + 1);
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO HOADON (maHD, ngayLap, tongTien, maKH) VALUES (?, CAST(GETDATE() AS DATE), ?, ?)")) {
                            ps.setString(1, maHD);
                            ps.setDouble(2, tongTien);
                            ps.setString(3, maKH);
                            ps.executeUpdate();
                        }
                        String maCT = String.format("CT%03d", KhachHangDAO.maxSo(conn, "CHITIETHOADON", "maCTHD", 2, "CT") + 1);
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV) VALUES (?, 1, ?, ?, ?, NULL, N'DV005')")) {
                            ps.setString(1, maCT);
                            ps.setDouble(2, tongTien);
                            ps.setDouble(3, tongTien);
                            ps.setString(4, maHD);
                            ps.executeUpdate();
                        }
                        KhachHangDAO.tichDiem(conn, maKH, Math.round(tongTien));
                    }
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }

                capNhatTrangThai(maChuong, "Trống");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<PhieuDatChuong> getAllPhieuDat() {
        List<PhieuDatChuong> list = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                String sql = "SELECT * FROM PHIEU_LUUTRU ORDER BY ngayVao DESC";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        Timestamp nv = rs.getTimestamp("ngayVao");
                        Timestamp nr = rs.getTimestamp("ngayRaDuKien");
                        list.add(new PhieuDatChuong(
                                rs.getString("maPhieu"),
                                rs.getString("cageID"),
                                rs.getString("tenKhach"),
                                rs.getString("sdtKhach"),
                                rs.getString("tenPet"),
                                rs.getString("loaiPet"),
                                4.0,
                                nv != null ? nv.toLocalDateTime() : LocalDateTime.now(),
                                nv != null ? nv.toLocalDateTime() : null,
                                nr != null ? nr.toLocalDateTime() : null,
                                rs.getDouble("tienCoc"),
                                rs.getString("trangThai"),
                                rs.getString("ghiChu")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ChiTietDichVuChuong> getDichVuCuaChuong(String maChuong) {
        List<ChiTietDichVuChuong> list = new ArrayList<>();
        if (maChuong == null) return list;
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                String sql = "SELECT d.* FROM CHITIET_DICHVU_CHUONG d "
                           + "JOIN PHIEU_LUUTRU p ON d.maPhieu = p.maPhieu "
                           + "WHERE p.cageID = ? AND p.trangThai IN (N'Đang lưu trú', N'Đang sử dụng')";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maChuong);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Timestamp tg = rs.getTimestamp("thoiGian");
                            list.add(new ChiTietDichVuChuong(
                                    rs.getString("maDV"),
                                    rs.getString("tenDV"),
                                    rs.getString("loai"),
                                    rs.getDouble("donGia"),
                                    rs.getInt("soLuong"),
                                    tg != null ? tg.toLocalDateTime() : LocalDateTime.now(),
                                    ""
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void themDichVu(String maChuong, ChiTietDichVuChuong dv) {
        if (maChuong == null || dv == null) return;
        try (Connection conn = ConnectDB.getConnection()) {
            if (conn != null) {
                // Tìm mã phiếu đang lưu trú của lồng này
                String sqlFind = "SELECT TOP 1 maPhieu FROM PHIEU_LUUTRU WHERE cageID = ? AND trangThai IN (N'Đang lưu trú', N'Đang sử dụng') ORDER BY ngayVao DESC";
                String maPhieu = null;
                try (PreparedStatement ps = conn.prepareStatement(sqlFind)) {
                    ps.setString(1, maChuong);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maPhieu = rs.getString("maPhieu");
                        }
                    }
                }

                if (maPhieu != null) {
                    String sqlIns = "INSERT INTO CHITIET_DICHVU_CHUONG (maPhieu, maDV, tenDV, loai, donGia, soLuong, thoiGian) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
                    try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                        ps.setString(1, maPhieu);
                        ps.setString(2, dv.getMaDV());
                        ps.setString(3, dv.getTenDV());
                        ps.setString(4, dv.getLoai());
                        ps.setDouble(5, dv.getDonGia());
                        ps.setInt(6, dv.getSoLuong());
                        ps.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xoaDichVu(String maChuong, int index) {
        // Có thể mở rộng xóa theo ID dịch vụ
    }
}
