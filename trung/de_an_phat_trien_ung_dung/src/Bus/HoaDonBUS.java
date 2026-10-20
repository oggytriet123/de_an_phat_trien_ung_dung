package Bus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.HoaDon;
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
    private int soHoaDonTiepTheo = 1;

    public HoaDonBUS(SanPhamBUS sanPham) {
        napDuLieuMau(sanPham);
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
        return ten == null || ten.trim().isEmpty() ? "Vui lòng nhập tên khách hàng." : null;
    }

    public static String loiSdt(String sdt) {
        String d = ChuoiUtil.chiSo(sdt);
        if (d.isEmpty()) return "Vui lòng nhập số điện thoại.";
        if (d.length() < 9 || d.length() > 11) return "Số điện thoại phải gồm 9–11 chữ số.";
        return null;
    }

    public static String loiGioHang(HoaDon h) {
        return h.trong() ? "Hóa đơn chưa có sản phẩm hoặc dịch vụ nào." : null;
    }

    /** Lỗi đầu tiên của hóa đơn hoặc null nếu hợp lệ. */
    public static String kiemTra(HoaDon h) {
        String e = loiTen(h.getTenKH());
        if (e == null) e = loiSdt(h.getSdt());
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
        if (cu == null) {
            nhap.setNgayTao(LocalDateTime.now());
            soHoaDonTiepTheo++;                       // chỉ cấp số khi lưu lần đầu
        } else if (!cu.getMaHD().equals(nhap.getMaHD())) {
            throw new IllegalStateException("Ô " + nhap.getSoO() + " đang chứa hóa đơn khác.");
        }
        cacO[i] = nhap.saoChep();
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
    }

    // ------------------------------------------------------------ lịch sử
    public List<HoaDon> layLichSu() {
        return Collections.unmodifiableList(lichSu);
    }

    /** Tìm theo mã hóa đơn, tên khách hoặc số điện thoại; lọc theo ngày thanh toán (null = bỏ qua). */
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
                String hay = ChuoiUtil.chuanHoa(h.getMaHD()) + "|" + ChuoiUtil.chuanHoa(h.getTenKH());
                boolean khop = hay.contains(kw)
                        || (!kwSo.isEmpty() && ChuoiUtil.chiSo(h.getSdt()).contains(kwSo));
                if (!khop) continue;
            }
            kq.add(h);
        }
        return kq;
    }

    // ------------------------------------------------------------ dữ liệu mẫu
    private void napDuLieuMau(SanPhamBUS sanPham) {
        // Ô 02: HD0001 - Nguyễn Văn A - 1.000.000₫ ; Ô 07: HD0002 - Khách lẻ - 485.000₫ (như thiết kế)
        HoaDon a = new HoaDon(maTiepTheo(), 2);
        soHoaDonTiepTheo++;
        a.setTenKH("Nguyễn Văn A");
        a.setSdt("0901 111 222");
        a.them(sanPham.timTheoSku("RC-002"));
        a.doiSoLuong("RC-002", 1);
        a.them(sanPham.timTheoSku("SPA-002"));
        cacO[1] = a;

        HoaDon b = new HoaDon(maTiepTheo(), 7);
        soHoaDonTiepTheo++;
        b.setTenKH("Khách lẻ");
        b.setSdt("0900 000 001");
        b.them(sanPham.timTheoSku("CAT-014"));
        b.them(sanPham.timTheoSku("SPA-002"));
        cacO[6] = b;
    }
}
