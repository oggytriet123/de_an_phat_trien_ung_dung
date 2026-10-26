package Bus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.HoaDon;
import Entity.KhachHang;

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

    /** Mở lên là 20 ô trống và lịch sử rỗng: hóa đơn chỉ xuất hiện khi nhân viên lưu hoặc thanh toán. */
    public HoaDonBUS(KhachHangBUS khachHang) {
        this.khachHang = khachHang;
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
