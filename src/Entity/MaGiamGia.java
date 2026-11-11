package Entity;

import java.time.LocalDate;
import Bus.ChuoiUtil;

/**
 * Thực thể Mã Khuyến Mãi / Giảm Giá.
 * 
 * Hỗ trợ 3 hình thức khuyến mãi:
 * 1. Giảm theo % (áp dụng trên giỏ hàng hoặc nhóm sản phẩm).
 * 2. Giảm theo số tiền cố định (VNĐ).
 * 3. Mua X tặng Y (kèm % giảm giá cho phần sản phẩm tặng thêm).
 * 
 * Hỗ trợ các điều kiện:
 * - Giới hạn số lượt dùng tối đa (soLuotToiDa, daSuDung).
 * - Phạm vi áp dụng: Tất cả hoặc đích danh 1 sản phẩm / 1 nhóm sản phẩm.
 * - Khoảng thời gian hiệu lực (ngayBatDau -> hanDung).
 */
public class MaGiamGia {
    public static final String TAT_CA = "Tất cả";

    // --- Thông tin định danh & giá trị ---
    private final String ma;
    private final boolean phanTram;
    private final long giaTri;          // % giảm hoặc số tiền cố định (VNĐ)
    private final LocalDate ngayBatDau;
    private final LocalDate hanDung;
    private final String ghiChu;

    // --- Quy tắc số lượt & phạm vi sản phẩm ---
    private final int soLuotToiDa;      // 0 = Không giới hạn lượt
    private int daSuDung;               // Số lượt đã thanh toán thành công
    private final String danhMucApDung; // "Tất cả" hoặc "SKU - Tên SP" hoặc tên nhóm hàng

    // --- Quy tắc Mua X tặng Y ---
    private final int muaX;             // Số lượng cần mua để được tặng (> 0)
    private final int tangY;             // Số lượng được tặng thêm (> 0)

    // =========================================================================
    // CONSTRUCTORS (Tương thích ngược đầy đủ)
    // =========================================================================

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate hanDung) {
        this(ma, phanTram, giaTri, LocalDate.now(), hanDung, "", 0, 0, TAT_CA, 0, 0);
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate hanDung, String ghiChu) {
        this(ma, phanTram, giaTri, LocalDate.now(), hanDung, ghiChu, 0, 0, TAT_CA, 0, 0);
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung) {
        this(ma, phanTram, giaTri, ngayBatDau, hanDung, "", 0, 0, TAT_CA, 0, 0);
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung, String ghiChu) {
        this(ma, phanTram, giaTri, ngayBatDau, hanDung, ghiChu, 0, 0, TAT_CA, 0, 0);
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung,
                     String ghiChu, int soLuotToiDa, int daSuDung, String danhMucApDung, int muaX, int tangY) {
        this.ma = ma;
        this.phanTram = phanTram || (muaX > 0 && tangY > 0);
        this.giaTri = giaTri;
        this.ngayBatDau = ngayBatDau != null ? ngayBatDau : LocalDate.now();
        this.hanDung = hanDung;
        this.ghiChu = ghiChu != null ? ghiChu.trim() : "";
        this.soLuotToiDa = Math.max(0, soLuotToiDa);
        this.daSuDung = Math.max(0, daSuDung);
        this.danhMucApDung = (danhMucApDung == null || danhMucApDung.trim().isEmpty()) ? TAT_CA : danhMucApDung.trim();
        this.muaX = Math.max(0, muaX);
        this.tangY = Math.max(0, tangY);
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================

    public String getMa() { return ma; }
    public boolean isPhanTram() { return phanTram; }
    public long getGiaTri() { return giaTri; }
    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public LocalDate getHanDung() { return hanDung; }
    public String getGhiChu() { return ghiChu; }

    public int getSoLuotToiDa() { return soLuotToiDa; }
    public int getDaSuDung() { return daSuDung; }
    public void setDaSuDung(int daSuDung) { this.daSuDung = Math.max(0, daSuDung); }
    public void tangDaSuDung() { this.daSuDung++; }

    public String getDanhMucApDung() { return danhMucApDung; }
    public int getMuaX() { return muaX; }
    public int getTangY() { return tangY; }

    // =========================================================================
    // KIỂM TRA TRẠNG THÁI & ĐIỀU KIỆN
    // =========================================================================

    public boolean coGioiHanLuot() { 
        return soLuotToiDa > 0; 
    }

    public boolean hetLuot() { 
        return soLuotToiDa > 0 && daSuDung >= soLuotToiDa; 
    }

    public int getSoLuotConLai() { 
        return soLuotToiDa > 0 ? Math.max(0, soLuotToiDa - daSuDung) : Integer.MAX_VALUE; 
    }

    public boolean laMuaXTangY() { 
        return muaX > 0 && tangY > 0; 
    }

    public boolean apDungChoTatCa() {
        return TAT_CA.equalsIgnoreCase(danhMucApDung) || ChuoiUtil.chuanHoa(danhMucApDung).equals("tat ca");
    }

    /** 
     * Kiểm tra sản phẩm trong giỏ có khớp với điều kiện áp dụng của mã hay không.
     */
    public boolean khopSanPham(SanPham sp) {
        if (sp == null) return false;
        if (apDungChoTatCa()) return true;

        String raw = danhMucApDung == null ? "" : danhMucApDung.trim();
        int idx = raw.indexOf(" - ");
        if (idx > 0) {
            String skuTarget = raw.substring(0, idx).trim();
            String tenTarget = ChuoiUtil.chuanHoa(raw.substring(idx + 3));
            return (sp.getSku() != null && sp.getSku().equalsIgnoreCase(skuTarget))
                    || ChuoiUtil.chuanHoa(sp.getTen()).equals(tenTarget);
        }

        String target = ChuoiUtil.chuanHoa(raw);
        String dmSp = ChuoiUtil.chuanHoa(sp.getDanhMuc());
        String tenSp = ChuoiUtil.chuanHoa(sp.getTen());
        String skuSp = ChuoiUtil.chuanHoa(sp.getSku());
        return dmSp.equals(target) || skuSp.equals(target) || tenSp.equals(target);
    }

    // =========================================================================
    // TÍNH TOÁN GIẢM GIÁ & ƯU ĐÃI
    // =========================================================================

    /** 
     * Số tiền được giảm trên tổng tiền hàng thông thường.
     */
    public long tinhGiam(long tongHang) {
        if (tongHang <= 0) return 0;
        long g = phanTram ? Math.round(tongHang * giaTri / 100.0) : giaTri;
        return Math.max(0, Math.min(g, tongHang));
    }

    /**
     * Tính số tiền giảm giá trên toàn bộ Hóa đơn:
     * - Nếu mã áp dụng theo nhóm hoặc sản phẩm cụ thể: chỉ tính trên các dòng khớp.
     * - Nếu là Mua X tặng Y: tính chiết khấu cho số lượng hàng tặng theo % cấu hình.
     */
    public long tinhGiam(HoaDon hd) {
        if (hd == null || hd.getDanhSachChiTiet().isEmpty()) return 0;
        if (apDungChoTatCa() && !laMuaXTangY()) {
            return tinhGiam(hd.tamTinh());
        }

        long tongGiam = 0;
        long tongTienNhom = 0;
        for (ChiTietHoaDon d : hd.getDanhSachChiTiet()) {
            SanPham sp = d.getSanPham();
            if (!khopSanPham(sp)) continue;
            long thanhTienDong = d.thanhTien();
            tongTienNhom += thanhTienDong;

            if (laMuaXTangY()) {
                int sl = d.getSoLuong();
                if (sl >= muaX) {
                    int soSuat = sl / muaX;
                    int soLuongTang = soSuat * tangY;
                    long giamDong = Math.round(d.getDonGia() * soLuongTang * (giaTri / 100.0));
                    tongGiam += Math.min(thanhTienDong, Math.max(0, giamDong));
                }
            }
        }

        if (laMuaXTangY()) {
            return Math.min(tongGiam, hd.tamTinh());
        }
        return tinhGiam(tongTienNhom);
    }

    /** 
     * Tổng số lượng sản phẩm được tặng thêm trên hóa đơn (khi dùng hình thức Mua X tặng Y).
     */
    public int tinhSoLuongTang(HoaDon hd) {
        if (!laMuaXTangY() || hd == null) return 0;
        int tongTang = 0;
        for (ChiTietHoaDon d : hd.getDanhSachChiTiet()) {
            if (khopSanPham(d.getSanPham()) && d.getSoLuong() >= muaX) {
                tongTang += (d.getSoLuong() / muaX) * tangY;
            }
        }
        return tongTang;
    }

    // =========================================================================
    // ĐỊNH DẠNG HIỂN THỊ
    // =========================================================================

    public String moTaLoaiGiam() {
        if (laMuaXTangY()) return "Mua " + muaX + " tặng " + tangY;
        return phanTram ? "Giảm theo %" : "Giảm theo số tiền";
    }

    public String moTaLuotDung() {
        if (!coGioiHanLuot()) return "Không giới hạn";
        return daSuDung + "/" + soLuotToiDa + " lượt";
    }

    public String moTa() {
        String base;
        if (laMuaXTangY()) {
            String pt = giaTri >= 100 ? "miễn phí" : ("giảm " + giaTri + "%");
            base = "Mua " + muaX + " tặng " + tangY + " (" + pt + ")";
        } else if (phanTram) {
            base = "Giảm " + giaTri + "%";
        } else {
            base = "Giảm " + String.format("%,d", giaTri).replace(',', '.') + "₫";
        }
        if (!apDungChoTatCa()) {
            return base + " [" + danhMucApDung + "]";
        }
        return base;
    }
}
