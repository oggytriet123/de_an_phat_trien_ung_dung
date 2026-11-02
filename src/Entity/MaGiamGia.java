package Entity;

import java.time.LocalDate;

/** Mã khuyến mãi: giảm theo phần trăm hoặc theo số tiền cố định, có hạn dùng. */
public class MaGiamGia {
    private final String ma;
    private final boolean phanTram;
    private final long giaTri;       // % (nếu phanTram) hoặc số tiền
    private final LocalDate hanDung;
    private final String ghiChu;

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate hanDung) {
        this(ma, phanTram, giaTri, hanDung, "");
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate hanDung, String ghiChu) {
        this.ma = ma;
        this.phanTram = phanTram;
        this.giaTri = giaTri;
        this.hanDung = hanDung;
        this.ghiChu = ghiChu != null ? ghiChu.trim() : "";
    }

    public String getMa() { return ma; }
    public boolean isPhanTram() { return phanTram; }
    public long getGiaTri() { return giaTri; }
    public LocalDate getHanDung() { return hanDung; }
    public String getGhiChu() { return ghiChu; }

    /** Số tiền được giảm trên tổng hàng; không bao giờ vượt quá tổng hàng. */
    public long tinhGiam(long tongHang) {
        long g = phanTram ? Math.round(tongHang * giaTri / 100.0) : giaTri;
        return Math.max(0, Math.min(g, tongHang));
    }

    public String moTa() {
        if (phanTram) return "Giảm " + giaTri + "%";
        return "Giảm " + String.format("%,d", giaTri).replace(',', '.') + "₫";
    }
}
