package Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Hóa đơn: thông tin khách, các dòng hàng, mã giảm giá và trạng thái thanh toán. */
public class HoaDon {
    public static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    public static final String DA_THANH_TOAN = "Đã thanh toán";
    public static final int SO_LUONG_TOI_DA = 99;

    private final String maHD;
    private int soO;                    // ô đang giữ hóa đơn (1..20)
    private String tenKH = "";
    private String sdt = "";
    private String maKH = "";
    private final List<DongGioHang> dong = new ArrayList<>();
    private MaGiamGia maGiamGia;
    private String trangThai = CHUA_THANH_TOAN;
    private String phuongThuc;
    private LocalDateTime ngayTao = LocalDateTime.now();
    private LocalDateTime ngayThanhToan;

    public HoaDon(String maHD, int soO) {
        this.maHD = maHD;
        this.soO = soO;
    }

    /** Bản sao độc lập (kể cả các dòng hàng) để chỉnh sửa mà chưa ảnh hưởng bản đã lưu. */
    public HoaDon saoChep() {
        HoaDon h = new HoaDon(maHD, soO);
        h.tenKH = tenKH;
        h.sdt = sdt;
        h.maKH = maKH;
        for (DongGioHang d : dong) h.dong.add(new DongGioHang(d.getSanPham(), d.getSoLuong()));
        h.maGiamGia = maGiamGia;
        h.trangThai = trangThai;
        h.phuongThuc = phuongThuc;
        h.ngayTao = ngayTao;
        h.ngayThanhToan = ngayThanhToan;
        return h;
    }

    // ---- Dòng hàng
    public List<DongGioHang> getDong() { return dong; }
    public boolean trong() { return dong.isEmpty(); }

    public void them(SanPham sp) {
        for (DongGioHang d : dong) {
            if (d.getSanPham().getSku().equals(sp.getSku())) {
                d.setSoLuong(Math.min(SO_LUONG_TOI_DA, d.getSoLuong() + 1));
                return;
            }
        }
        dong.add(new DongGioHang(sp, 1));
    }

    /** Tăng/giảm số lượng; giảm về 0 thì xóa dòng. Số lượng luôn nằm trong 1..99. */
    public void doiSoLuong(String sku, int delta) {
        for (int i = 0; i < dong.size(); i++) {
            DongGioHang d = dong.get(i);
            if (!d.getSanPham().getSku().equals(sku)) continue;
            int sl = Math.min(SO_LUONG_TOI_DA, d.getSoLuong() + delta);
            if (sl <= 0) dong.remove(i);
            else d.setSoLuong(sl);
            return;
        }
    }

    // ---- Tiền
    public long tamTinh() {
        long t = 0;
        for (DongGioHang d : dong) t += d.thanhTien();
        return t;
    }

    public long tienGiam() {
        return maGiamGia == null ? 0 : maGiamGia.tinhGiam(tamTinh());
    }

    /** Cần thanh toán = tổng hàng - tiền giảm giá. */
    public long canThanhToan() {
        return tamTinh() - tienGiam();
    }

    // ---- Getter / setter
    public String getMaHD() { return maHD; }
    public int getSoO() { return soO; }
    public String getTenKH() { return tenKH; }
    public String getSdt() { return sdt; }
    public String getMaKH() { return maKH; }
    public MaGiamGia getMaGiamGia() { return maGiamGia; }
    public String getTrangThai() { return trangThai; }
    public String getPhuongThuc() { return phuongThuc; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public boolean daThanhToan() { return DA_THANH_TOAN.equals(trangThai); }

    public void setSoO(int soO) { this.soO = soO; }
    public void setTenKH(String v) { this.tenKH = v == null ? "" : v; }
    public void setSdt(String v) { this.sdt = v == null ? "" : v; }
    public void setMaKH(String v) { this.maKH = v == null ? "" : v; }
    public void setMaGiamGia(MaGiamGia m) { this.maGiamGia = m; }
    public void setNgayTao(LocalDateTime t) { this.ngayTao = t; }

    public void danhDauDaThanhToan(String phuongThuc, LocalDateTime luc) {
        this.trangThai = DA_THANH_TOAN;
        this.phuongThuc = phuongThuc;
        this.ngayThanhToan = luc;
    }
}
