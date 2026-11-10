package Entity;

import java.text.Normalizer;
import java.time.LocalDate;

/**
 * Mã khuyến mãi:
 * - Hỗ trợ 3 hình thức: Giảm theo %, Giảm theo số tiền cố định, hoặc Mua X tặng Y (kèm giảm % cho phần tặng).
 * - Hỗ trợ giới hạn số lượng người/lượt sử dụng (soLuotToiDa, daSuDung).
 * - Hỗ trợ khuyến mãi riêng theo từng loại sản phẩm/dịch vụ (danhMucApDung).
 */
public class MaGiamGia {
    public static final String TAT_CA = "Tất cả";

    private final String ma;
    private final boolean phanTram;
    private final long giaTri;          // % (nếu phanTram hoặc Mua X tặng Y) hoặc số tiền (VNĐ)
    private final LocalDate ngayBatDau;
    private final LocalDate hanDung;
    private final String ghiChu;
    private final int soLuotToiDa;      // 0 = Không giới hạn
    private int daSuDung;               // Số lượt đã sử dụng
    private final String danhMucApDung; // "Tất cả" hoặc tên loại sản phẩm / dịch vụ
    private final int muaX;             // > 0 nếu là khuyến mãi Mua X tặng Y
    private final int tangY;            // > 0 nếu là khuyến mãi Mua X tặng Y

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
    public boolean coGioiHanLuot() { return soLuotToiDa > 0; }
    public boolean hetLuot() { return soLuotToiDa > 0 && daSuDung >= soLuotToiDa; }
    public int getSoLuotConLai() { return soLuotToiDa > 0 ? Math.max(0, soLuotToiDa - daSuDung) : Integer.MAX_VALUE; }

    public String getDanhMucApDung() { return danhMucApDung; }
    public boolean apDungChoTatCa() {
        return TAT_CA.equalsIgnoreCase(danhMucApDung) || boDau(danhMucApDung).equals("tat ca");
    }

    public int getMuaX() { return muaX; }
    public int getTangY() { return tangY; }
    public boolean laMuaXTangY() { return muaX > 0 && tangY > 0; }

    private static String boDau(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").replace('đ', 'd');
    }

    /** Kiểm tra sản phẩm/dịch vụ có khớp với sản phẩm hoặc phân loại được khuyến mãi không. */
    public boolean khopSanPham(SanPham sp) {
        if (sp == null) return false;
        if (apDungChoTatCa()) return true;
        String raw = danhMucApDung == null ? "" : danhMucApDung.trim();
        int idx = raw.indexOf(" - ");
        if (idx > 0) {
            String skuTarget = raw.substring(0, idx).trim();
            String tenTarget = boDau(raw.substring(idx + 3));
            return (sp.getSku() != null && sp.getSku().equalsIgnoreCase(skuTarget))
                    || boDau(sp.getTen()).equals(tenTarget);
        }
        String target = boDau(raw);
        String dmSp = boDau(sp.getDanhMuc());
        String tenSp = boDau(sp.getTen());
        String skuSp = boDau(sp.getSku());
        return dmSp.equals(target) || skuSp.equals(target) || tenSp.equals(target);
    }

    /** Số tiền được giảm trên tổng hàng đơn giản; không bao giờ vượt quá tổng hàng. */
    public long tinhGiam(long tongHang) {
        if (tongHang <= 0) return 0;
        long g = phanTram ? Math.round(tongHang * giaTri / 100.0) : giaTri;
        return Math.max(0, Math.min(g, tongHang));
    }

    /**
     * Tính số tiền giảm giá trên toàn bộ Hóa đơn (có xét loại sản phẩm áp dụng và Mua X tặng Y).
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

    /** Tổng số lượng sản phẩm được tặng thêm trên hóa đơn (khi dùng chế độ Mua X tặng Y). */
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
