package Bus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.HoaDon;
import Entity.KhachHang;
import Entity.SanPham;

/**
 * Quản lý 20 ô hóa đơn đang hoạt động và lịch sử hóa đơn đã thanh toán.
 * <ul>
 *   <li>Hóa đơn đã lưu nhưng chưa thanh toán luôn nằm tại ô được gán.</li>
 *   <li>Chỉ khi thanh toán thành công hóa đơn mới rời ô (ô trở thành trống) và vào lịch sử.</li>
 * </ul>
 * Hiện dùng dữ liệu trong bộ nhớ; khi có HoaDonDAO chỉ cần thay phần lưu / đọc.
 */
public class HoaDonBUS {
    public static final int SO_O = 20;
    public static final String[] PHUONG_THUC = {"Tiền mặt", "Quẹt thẻ", "MoMo", "ZaloPay"};

    private final HoaDon[] cacO = new HoaDon[SO_O];       // null = ô trống
    private final List<HoaDon> lichSu = new ArrayList<>(); // mới nhất ở đầu
    private final KhachHangBUS khachHang;
    private int soHoaDonTiepTheo = 1;

    /** Khởi tạo BUS: tải hóa đơn từ CSDL (hoặc nạp dữ liệu mẫu ban đầu). */
    public HoaDonBUS(KhachHangBUS khachHang) {
        this.khachHang = khachHang;
        taiDuLieuTuCSDL();
    }

    private void taiDuLieuTuCSDL() {
        try (java.sql.Connection conn = ConnectDB.ConnectDB.getConnection()) {
            if (conn != null) {
                String sql = "SELECT h.maHD, h.ngayLap, h.tongTien, h.maKH, k.hoTen, k.soDienThoai "
                           + "FROM HOADON h LEFT JOIN KHACHHANG k ON h.maKH = k.maKH "
                           + "ORDER BY h.ngayLap DESC, h.maHD DESC";
                try (java.sql.Statement st = conn.createStatement();
                     java.sql.ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        String maHD = rs.getString("maHD");
                        java.sql.Date nl = rs.getDate("ngayLap");
                        double tongTien = rs.getDouble("tongTien");
                        String maKH = rs.getString("maKH");
                        String tenKH = rs.getString("hoTen");
                        String sdt = rs.getString("soDienThoai");

                        HoaDon hd = new HoaDon(maHD, 0);
                        hd.setMaKH(maKH != null ? maKH : "");
                        hd.setTenKH(tenKH != null ? tenKH : "Khách vãng lai");
                        hd.setSdt(sdt != null ? sdt : "");
                        LocalDateTime ldt = nl != null ? nl.toLocalDate().atTime(9, 0) : LocalDateTime.now();
                        hd.setNgayTao(ldt);
                        hd.danhDauDaThanhToan("Tiền mặt", ldt);
                        taiChiTietHoaDon(conn, hd);
                        if (hd.canThanhToan() == 0 && tongTien > 0) {
                            hd.them(new SanPham(maHD, "Dịch vụ & Sản phẩm tổng hợp", "Dịch vụ", (long) tongTien, true));
                        }
                        lichSu.add(hd);
                    }
                }
            }
        } catch (Exception ignored) { }

        if (lichSu.isEmpty()) {
            napDuLieuMau();
        }

        capNhatSoHoaDonTiepTheo();
    }

    private void taiChiTietHoaDon(java.sql.Connection conn, HoaDon hd) {
        String sql = "SELECT ct.soLuong, ct.donGia, ct.thanhTien, ct.sku, ct.maDV, "
                   + "ISNULL(sp.tenSP, dv.tenDV) AS tenItem, "
                   + "CASE WHEN ct.maDV IS NOT NULL THEN 1 ELSE 0 END AS laDV "
                   + "FROM CHITIETHOADON ct "
                   + "LEFT JOIN SANPHAM sp ON ct.sku = sp.sku_maSP "
                   + "LEFT JOIN DICHVU dv ON ct.maDV = dv.maDV "
                   + "WHERE ct.maHD = ?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hd.getMaHD());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sl = rs.getInt("soLuong");
                    long gia = (long) rs.getDouble("donGia");
                    String sku = rs.getString("sku");
                    String maDV = rs.getString("maDV");
                    String ten = rs.getString("tenItem");
                    boolean laDV = rs.getInt("laDV") == 1;
                    String code = laDV ? (maDV != null ? maDV : "DV") : (sku != null ? sku : "SP");
                    if (ten == null) ten = code;
                    SanPham item = new SanPham(code, ten, laDV ? "Dịch vụ" : "Sản phẩm", gia, laDV);
                    for (int i = 0; i < sl; i++) {
                        hd.them(item);
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    private void napDuLieuMau() {
        taoMau("HD001", "Nguyễn Minh Anh", "0903123456", "KH001", LocalDate.now().minusDays(3),
               new SanPham("SP001", "Thức ăn hạt Royal Canin 2kg", "Thức ăn", 350000, false),
               new SanPham("SP002", "Thức ăn hạt Whiskas 1.5kg", "Thức ăn", 180000, false));
        taoMau("HD002", "Lê Thị Cẩm Tú", "0938222111", "KH002", LocalDate.now().minusDays(2),
               new SanPham("DV002", "Tiêm phòng vaccine", "Tiêm phòng & Xét nghiệm", 350000, true));
        taoMau("HD003", "Phạm Gia Hân", "0977654321", "KH003", LocalDate.now().minusDays(1),
               new SanPham("SP004", "Vòng cổ chống ve rận", "Phụ kiện", 95000, false),
               new SanPham("SP002", "Thức ăn hạt Whiskas 1.5kg", "Thức ăn", 180000, false),
               new SanPham("DV003", "Tắm và cắt tỉa lông", "Spa & Grooming", 150000, true));
        taoMau("HD004", "Võ Hoàng Long", "0386909808", "KH004", LocalDate.now(),
               new SanPham("DV001", "Khám tổng quát", "Khám & Điều trị", 200000, true));
    }

    private void taoMau(String ma, String ten, String sdt, String maKH, LocalDate ngay, SanPham... sps) {
        HoaDon h = new HoaDon(ma, 0);
        h.setTenKH(ten);
        h.setSdt(sdt);
        h.setMaKH(maKH);
        h.setNgayTao(ngay.atTime(10, 0));
        h.danhDauDaThanhToan("Tiền mặt", ngay.atTime(10, 15));
        for (SanPham s : sps) h.them(s);
        lichSu.add(h);
    }

    private void capNhatSoHoaDonTiepTheo() {
        int max = 0;
        for (HoaDon h : lichSu) {
            String m = h.getMaHD();
            if (m != null && m.startsWith("HD")) {
                try {
                    int num = Integer.parseInt(m.substring(2).trim());
                    if (num > max) max = num;
                } catch (Exception ignored) { }
            }
        }
        soHoaDonTiepTheo = Math.max(soHoaDonTiepTheo, max + 1);
    }

    // ------------------------------------------------------------ 20 ô
    /** @param viTri 0..19 */
    public HoaDon layO(int viTri) {
        return cacO[viTri];
    }

    public int oTrongDauTien() {
        for (int i = 0; i < SO_O; i++) if (cacO[i] == null) return i;
        return -1;
    }

    public int demOTrong() {
        int n = 0;
        for (HoaDon h : cacO) if (h == null) n++;
        return n;
    }

    /** Mã hóa đơn sẽ được cấp cho hóa đơn mới tiếp theo (chưa tiêu thụ cho đến khi lưu). */
    public String maTiepTheo() {
        return String.format("HD%04d", soHoaDonTiepTheo);
    }

    /** Bản nháp cho một ô trống. Chưa nằm trong ô cho đến khi bấm Lưu hoặc Thanh toán. */
    public HoaDon taoNhap(int viTri) {
        return new HoaDon(maTiepTheo(), viTri + 1);
    }

    /** Bản sao của hóa đơn đang giữ tại ô để chỉnh sửa. Hủy chỉnh sửa không ảnh hưởng bản đã lưu. */
    public HoaDon moNhap(int viTri) {
        return cacO[viTri].saoChep();
    }

    // ------------------------------------------------------------ kiểm tra dữ liệu
    public static String loiTen(String ten) {
        return KhachHangBUS.loiTen(ten);
    }

    public static String loiSdt(String sdt) {
        return KhachHangBUS.loiSdt(sdt);
    }

    public static String loiGioHang(HoaDon h) {
        return h.trong() ? "Hóa đơn chưa có sản phẩm hoặc dịch vụ nào." : null;
    }

    /**
     * Một số điện thoại chỉ thuộc về một khách hàng: nếu số đã đăng ký cho khách khác tên thì báo lỗi.
     * Tên được so khớp không phân biệt hoa thường và dấu.
     */
    public String loiTrungSdt(String ten, String sdt) {
        KhachHang k = khachHang.timTheoSdtAnToan(KhachHangBUS.chuanHoaSdt(sdt));
        if (k == null) return null;
        String a = ChuoiUtil.chuanHoa(KhachHangBUS.chuanHoaTen(ten));
        String b = ChuoiUtil.chuanHoa(KhachHangBUS.chuanHoaTen(k.getHoTen()));
        if (a.equals(b)) return null;
        return "Số điện thoại này đã thuộc khách hàng " + k.getHoTen() + " (" + k.getMaKH()
                + "). Hãy nhập đúng tên hoặc đổi số điện thoại.";
    }

    /** Lỗi đầu tiên của hóa đơn hoặc null nếu hợp lệ. */
    public String kiemTra(HoaDon h) {
        String e = loiTen(h.getTenKH());
        if (e == null) e = loiSdt(h.getSdt());
        if (e == null) e = loiTrungSdt(h.getTenKH(), h.getSdt());
        if (e == null) e = loiGioHang(h);
        return e;
    }

    // ------------------------------------------------------------ lưu / thanh toán
    /** @return true nếu hóa đơn này đã được lưu và đang nằm ở ô được gán. */
    public boolean daLuu(HoaDon h) {
        HoaDon g = cacO[h.getSoO() - 1];
        return g != null && g.getMaHD().equals(h.getMaHD());
    }

    /**
     * Lưu hóa đơn vào ô được gán (Chưa thanh toán). Lưu lần nữa thì cập nhật bản ghi cũ,
     * không tạo bản trùng.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public void luu(HoaDon nhap) {
        String loi = kiemTra(nhap);
        if (loi != null) throw new IllegalArgumentException(loi);
        if (nhap.daThanhToan()) throw new IllegalStateException("Hóa đơn đã thanh toán, không thể sửa.");
        int i = nhap.getSoO() - 1;
        HoaDon cu = cacO[i];
        if (cu != null && !cu.getMaHD().equals(nhap.getMaHD())) {
            throw new IllegalStateException("Ô " + nhap.getSoO() + " đang chứa hóa đơn khác.");
        }

        // Chuẩn hóa thông tin khách và gắn mã khách hàng (khách chưa có thì tự tạo mới)
        String ten = KhachHangBUS.chuanHoaTen(nhap.getTenKH());
        String sdt = KhachHangBUS.chuanHoaSdt(nhap.getSdt());
        KhachHang k = khachHang.timTheoSdtAnToan(sdt);
        if (k == null) k = khachHang.them(ten, sdt, "Thường", "");
        nhap.setTenKH(ten);
        nhap.setSdt(KhachHangBUS.dinhDangSdt(sdt));
        nhap.setMaKH(k.getMaKH());

        if (cu == null) {
            nhap.setNgayTao(LocalDateTime.now());
            soHoaDonTiepTheo++;                       // chỉ cấp số khi lưu lần đầu
        }
        cacO[i] = nhap.saoChep();
    }

    /**
     * Xóa hóa đơn đã lưu (chưa thanh toán) khỏi ô: ô trở thành trống để tạo hóa đơn mới.
     * Hóa đơn đã thanh toán nằm trong lịch sử nên không xóa được. Mã hóa đơn đã dùng không được cấp lại.
     * @param viTri 0..19
     * @return hóa đơn vừa bị xóa
     */
    public HoaDon xoa(int viTri) {
        HoaDon h = cacO[viTri];
        if (h == null) throw new IllegalStateException("Ô " + (viTri + 1) + " đang trống.");
        cacO[viTri] = null;
        return h;
    }

    /**
     * Ghi nhận thanh toán đã được xác nhận thành công: hóa đơn rời ô, vào lịch sử.
     * Nếu chưa lưu thì lưu trước.
     */
    public void thanhToan(HoaDon nhap, String phuongThuc) {
        if (phuongThuc == null || !List.of(PHUONG_THUC).contains(phuongThuc)) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }
        luu(nhap);
        int i = nhap.getSoO() - 1;
        HoaDon h = cacO[i];
        h.danhDauDaThanhToan(phuongThuc, LocalDateTime.now());
        cacO[i] = null;
        lichSu.add(0, h);
        // đồng bộ lại bản nháp đang mở để giao diện biết hóa đơn đã thanh toán
        nhap.danhDauDaThanhToan(phuongThuc, h.getNgayThanhToan());
        khachHang.ghiNhanThanhToan(h.getMaKH(), h.canThanhToan());   // cộng vào tổng chi tiêu của khách
        luuVaoCSDL(h);
    }

    private void luuVaoCSDL(HoaDon h) {
        try (java.sql.Connection conn = ConnectDB.ConnectDB.getConnection()) {
            if (conn == null) return;
            String sqlHD = "INSERT INTO HOADON (maHD, ngayLap, tongTien, maKH) VALUES (?, ?, ?, ?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlHD)) {
                ps.setString(1, h.getMaHD());
                ps.setDate(2, java.sql.Date.valueOf(h.getNgayThanhToan().toLocalDate()));
                ps.setDouble(3, h.canThanhToan());
                ps.setString(4, h.getMaKH().isEmpty() ? null : h.getMaKH());
                ps.executeUpdate();
            }
            String sqlCT = "INSERT INTO CHITIETHOADON (maCTHD, soLuong, donGia, thanhTien, maHD, sku, maDV) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int ctIdx = 1;
            for (Entity.DongGioHang d : h.getDong()) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCT)) {
                    String maCT = String.format("CT%s_%02d", h.getMaHD(), ctIdx++);
                    ps.setString(1, maCT);
                    ps.setInt(2, d.getSoLuong());
                    ps.setDouble(3, d.getSanPham().getDonGia());
                    ps.setDouble(4, d.thanhTien());
                    ps.setString(5, h.getMaHD());
                    if (d.getSanPham().isDichVu()) {
                        ps.setNull(6, java.sql.Types.NVARCHAR);
                        ps.setString(7, d.getSanPham().getSku());
                    } else {
                        ps.setString(6, d.getSanPham().getSku());
                        ps.setNull(7, java.sql.Types.NVARCHAR);
                    }
                    ps.executeUpdate();
                }
            }
        } catch (Exception ignored) { }
    }

    // ------------------------------------------------------------ lịch sử
    public List<HoaDon> layLichSu() {
        return Collections.unmodifiableList(lichSu);
    }

    /** Tìm theo mã hóa đơn, tên khách, mã khách hàng hoặc số điện thoại; lọc theo ngày thanh toán (null = bỏ qua). */
    public List<HoaDon> timLichSu(String tuKhoa, LocalDate tu, LocalDate den) {
        String kw = ChuoiUtil.chuanHoa(tuKhoa == null ? "" : tuKhoa.trim());
        // chỉ so khớp số điện thoại khi từ khóa toàn là chữ số (tránh "hd0001" khớp nhầm SĐT chứa 0001)
        String kwSo = kw.replaceAll("[\\s.+-]", "").matches("\\d+") ? ChuoiUtil.chiSo(kw) : "";
        List<HoaDon> kq = new ArrayList<>();
        for (HoaDon h : lichSu) {
            LocalDate ngay = h.getNgayThanhToan().toLocalDate();
            if (tu != null && ngay.isBefore(tu)) continue;
            if (den != null && ngay.isAfter(den)) continue;
            if (!kw.isEmpty()) {
                String hay = ChuoiUtil.chuanHoa(h.getMaHD()) + "|" + ChuoiUtil.chuanHoa(h.getTenKH())
                        + "|" + ChuoiUtil.chuanHoa(h.getMaKH());
                boolean khop = hay.contains(kw)
                        || (!kwSo.isEmpty() && ChuoiUtil.chiSo(h.getSdt()).contains(kwSo));
                if (!khop) continue;
            }
            kq.add(h);
        }
        return kq;
    }
}
