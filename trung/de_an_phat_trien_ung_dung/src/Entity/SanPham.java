package Entity;

/** Sản phẩm hoặc dịch vụ bán tại quầy. */
public class SanPham {
    private final String sku;
    private final String ten;
    private final String danhMuc;
    private final long donGia;
    private final boolean dichVu;

    public SanPham(String sku, String ten, String danhMuc, long donGia, boolean dichVu) {
        this.sku = sku;
        this.ten = ten;
        this.danhMuc = danhMuc;
        this.donGia = donGia;
        this.dichVu = dichVu;
    }

    public String getSku() { return sku; }
    public String getTen() { return ten; }
    public String getDanhMuc() { return danhMuc; }
    public long getDonGia() { return donGia; }
    public boolean isDichVu() { return dichVu; }
}
