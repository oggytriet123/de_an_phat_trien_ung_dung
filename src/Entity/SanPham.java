package Entity;

/**
 * Sản phẩm hoặc dịch vụ bán tại quầy, quản lý kho, thống kê và dịch vụ chăm sóc.
 */
public class SanPham {
    private final String sku;
    private String ten;
    private String danhMuc;
    private long donGia;
    private final boolean dichVu;

    // Các thuộc tính mở rộng cho Dịch vụ (từ alo)
    private String thoiLuong = "";                 // ví dụ "30 phút", "1 ngày"
    private String doiTuong = "Tất cả thú cưng";   // đối tượng áp dụng
    private boolean dangMo = true;                 // false = tạm ngưng
    private int stt;                               // thứ tự tạo

    // Các thuộc tính mở rộng cho Kho hàng (từ de_an_phat_trien_ung_dung)
    private String quyCach = "";
    private String donViTinh = "";
    private int soLuongTon = 0;
    private String trangThai = "Đang bán";

    // Constructor chuẩn từ alo & bán hàng
    public SanPham(String sku, String ten, String danhMuc, long donGia, boolean dichVu) {
        this.sku = sku;
        this.ten = ten;
        this.danhMuc = danhMuc;
        this.donGia = donGia;
        this.dichVu = dichVu;
    }

    // Constructor cho quản lý kho hàng
    public SanPham(String maSKU, String tenSP, String quyCach, String danhMuc,
                   int ton, String dvt, long donGia, String trangThai) {
        this.sku = maSKU;
        this.ten = tenSP;
        this.quyCach = quyCach;
        this.danhMuc = danhMuc;
        this.soLuongTon = ton;
        this.donViTinh = dvt;
        this.donGia = donGia;
        this.trangThai = trangThai;
        this.dichVu = false;
    }

    public String getSku() { return sku; }
    public String getMaSKU() { return sku; }

    public String getTen() { return ten; }
    public String getTenSP() { return ten; }
    public void setTen(String v) { this.ten = v; }
    public void setTenSP(String v) { this.ten = v; }

    public String getDanhMuc() { return danhMuc; }
    public void setDanhMuc(String v) { this.danhMuc = v; }

    public long getDonGia() { return donGia; }
    public void setDonGia(long v) { this.donGia = v; }

    public boolean isDichVu() { return dichVu; }

    public String getThoiLuong() { return thoiLuong; }
    public void setThoiLuong(String v) { this.thoiLuong = v == null ? "" : v; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String v) { this.doiTuong = v == null ? "" : v; }

    public boolean isDangMo() { return dangMo; }
    public void setDangMo(boolean v) { this.dangMo = v; }

    public int getStt() { return stt; }
    public void setStt(int v) { this.stt = v; }

    public String getQuyCach() { return quyCach; }
    public void setQuyCach(String quyCach) { this.quyCach = quyCach; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
