package Bus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Entity.DongGioHang;
import Entity.SanPham;

/** Giỏ hàng + hóa đơn đang lập tại quầy. */
public class GioHangBUS {
    public static final double TY_LE_VAT = 0.10;
    public static final int SO_LUONG_TOI_DA = 99;

    private final Map<String, DongGioHang> dong = new LinkedHashMap<>();
    private long giamTien;      // giảm theo số tiền
    private int giamPhanTram;   // giảm theo % (ưu tiên nếu > 0)
    private int soHoaDon = 1;

    public String maHoaDon() {
        return String.format("HD%04d", soHoaDon);
    }

    public List<DongGioHang> layDong() {
        return new ArrayList<>(dong.values());
    }

    public boolean trong() {
        return dong.isEmpty();
    }

    public void them(SanPham sp) {
        DongGioHang d = dong.get(sp.getSku());
        if (d == null) dong.put(sp.getSku(), new DongGioHang(sp, 1));
        else d.setSoLuong(Math.min(SO_LUONG_TOI_DA, d.getSoLuong() + 1));
    }

    /** Tăng/giảm số lượng; giảm về 0 thì bỏ dòng. */
    public void doiSoLuong(String sku, int delta) {
        DongGioHang d = dong.get(sku);
        if (d == null) return;
        int sl = Math.min(SO_LUONG_TOI_DA, d.getSoLuong() + delta);
        if (sl <= 0) dong.remove(sku);
        else d.setSoLuong(sl);
        if (dong.isEmpty()) datLaiGiamGia();
    }

    public long tamTinh() {
        long t = 0;
        for (DongGioHang d : dong.values()) t += d.thanhTien();
        return t;
    }

    public long giamGia() {
        long t = tamTinh();
        if (giamPhanTram > 0) return Math.round(t * giamPhanTram / 100.0);
        return Math.min(giamTien, t);
    }

    /**
     * Nhập giảm giá: "50000", "50.000" (số tiền) hoặc "10%" (phần trăm).
     * @return false nếu không hợp lệ (giữ nguyên giảm giá cũ).
     */
    public boolean datGiamGia(String nhap) {
        if (nhap == null) return false;
        String s = nhap.trim();
        if (s.isEmpty()) { datLaiGiamGia(); return true; }
        if (s.endsWith("%")) {
            String so = s.substring(0, s.length() - 1).trim();
            if (!so.matches("\\d{1,3}")) return false;
            int p = Integer.parseInt(so);
            if (p > 100) return false;
            giamPhanTram = p;
            giamTien = 0;
            return true;
        }
        String so = ChuoiUtil.chiSo(s);
        if (so.isEmpty() || so.length() > 12 || !s.matches("[\\d.,\\s]+(₫|đ|d)?")) return false;
        long v = Long.parseLong(so);
        if (v > tamTinh()) return false;
        giamTien = v;
        giamPhanTram = 0;
        return true;
    }

    public long vat() {
        return Math.round((tamTinh() - giamGia()) * TY_LE_VAT);
    }

    public long tong() {
        return tamTinh() - giamGia() + vat();
    }

    /** Chốt hóa đơn: trả về mã hóa đơn vừa thanh toán và mở hóa đơn mới. */
    public String thanhToan() {
        String ma = maHoaDon();
        soHoaDon++;
        dong.clear();
        datLaiGiamGia();
        return ma;
    }

    private void datLaiGiamGia() {
        giamTien = 0;
        giamPhanTram = 0;
    }
}
