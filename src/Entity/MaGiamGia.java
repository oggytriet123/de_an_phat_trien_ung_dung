package Entity;

import java.time.LocalDate;

/** Mã khuyến mãi: giảm theo phần trăm hoặc theo số tiền cố định, có ngày bắt đầu và ngày kết thúc (hạn dùng). */
public class MaGiamGia {
    private final String ma;
    private final boolean phanTram;
    private final long giaTri;       // % (nếu phanTram) hoặc số tiền
    private final LocalDate ngayBatDau;
    private final LocalDate hanDung;
    private final String ghiChu;

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate hanDung) {
        this(ma, phanTram, giaTri, LocalDate.now(), hanDung, "");
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate hanDung, String ghiChu) {
        this(ma, phanTram, giaTri, LocalDate.now(), hanDung, ghiChu);
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung) {
        this(ma, phanTram, giaTri, ngayBatDau, hanDung, "");
    }

    public MaGiamGia(String ma, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung, String ghiChu) {
        this.ma = ma;
        this.phanTram = phanTram;
        this.giaTri = giaTri;
        this.ngayBatDau = ngayBatDau != null ? ngayBatDau : LocalDate.now();
        this.hanDung = hanDung;
        this.ghiChu = ghiChu != null ? ghiChu.trim() : "";
    }

    public String getMa() { return ma; }
    public boolean isPhanTram() { return phanTram; }
    public long getGiaTri() { return giaTri; }
    public LocalDate getNgayBatDau() { return ngayBatDau; }
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
