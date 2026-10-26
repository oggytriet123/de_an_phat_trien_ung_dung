package Entity;

import java.io.Serializable;

/** Sản phẩm hoặc dịch vụ bán tại quầy / quản lý kho hàng. */
public class SanPham implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maSKU;
    private String tenSP;
    private String quyCach;
    private String danhMuc;       // Thức ăn | Đồ chơi | Thuốc & Y tế | Phụ kiện
    private int soLuongTon;
    private String donViTinh;     // hộp | túi | bộ | cái | tuýp
    private long donGia;
    private String trangThai;     // Đang bán | Tạm hết hàng | Ngừng kinh doanh
    private boolean dichVu;

    public SanPham(String sku, String ten, String danhMuc, long donGia, boolean dichVu) {
        this.maSKU = sku;
        this.tenSP = ten;
        this.quyCach = "";
        this.danhMuc = danhMuc;
        this.soLuongTon = dichVu ? 0 : 50;
        this.donViTinh = dichVu ? "lượt" : "cái";
        this.donGia = donGia;
        this.trangThai = "Đang bán";
        this.dichVu = dichVu;
    }

    public SanPham(String maSKU, String tenSP, String quyCach, String danhMuc,
                   int soLuongTon, String donViTinh, long donGia, String trangThai) {
        this.maSKU = maSKU;
        this.tenSP = tenSP;
        this.quyCach = quyCach;
        this.danhMuc = danhMuc;
        this.soLuongTon = soLuongTon;
        this.donViTinh = donViTinh;
        this.donGia = donGia;
        this.trangThai = trangThai;
        this.dichVu = false;
    }

    public String getSku() { return maSKU; }
    public String getTen() { return tenSP; }
    public String getMaSKU() { return maSKU; }
    public String getTenSP() { return tenSP; }
    public String getQuyCach() { return quyCach == null ? "" : quyCach; }
    public String getDanhMuc() { return danhMuc; }
    public int getSoLuongTon() { return soLuongTon; }
    public String getDonViTinh() { return donViTinh == null ? "cái" : donViTinh; }
    public long getDonGia() { return donGia; }
    public String getTrangThai() { return trangThai == null ? "Đang bán" : trangThai; }
    public boolean isDichVu() { return dichVu; }

    public void setMaSKU(String maSKU) { this.maSKU = maSKU; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public void setQuyCach(String quyCach) { this.quyCach = quyCach; }
    public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public void setDonGia(long donGia) { this.donGia = donGia; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public void setDichVu(boolean dichVu) { this.dichVu = dichVu; }
}
