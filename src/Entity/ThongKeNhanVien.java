package Entity;

/**
 * Số liệu thống kê nhân sự cho các thẻ KPI trên giao diện.
 */
public class ThongKeNhanVien {
    public final int tongNhanVien;
    public final int soBanHang;
    public final int soBacSi;
    public final int soKho;
    public final int soQuanLy;
    public final int soDangLamViec;

    public ThongKeNhanVien(int tongNhanVien, int soBanHang, int soBacSi, int soKho, int soQuanLy, int soDangLamViec) {
        this.tongNhanVien = tongNhanVien;
        this.soBanHang = soBanHang;
        this.soBacSi = soBacSi;
        this.soKho = soKho;
        this.soQuanLy = soQuanLy;
        this.soDangLamViec = soDangLamViec;
    }
}
