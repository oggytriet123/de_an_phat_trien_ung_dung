package Entity;

public class SanPham {
    private final String maSKU;
    private String tenSP;
    private String quyCach;
    private String danhMuc;       // Thức ăn | Đồ chơi | Thuốc & Y tế | Phụ kiện
    private int soLuongTon;
    private String donViTinh;     // hộp | túi | bộ | cái | tuýp
    private long donGia;
    private String trangThai;     // Đang bán | Tạm hết hàng | Ngừng kinh doanh

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
    }

    public String getMaSKU() { return maSKU; }
    public String getTenSP() { return tenSP; }
    public String getQuyCach() { return quyCach; }
    public String getDanhMuc() { return danhMuc; }
    public int getSoLuongTon() { return soLuongTon; }
    public String getDonViTinh() { return donViTinh; }
    public long getDonGia() { return donGia; }
    public String getTrangThai() { return trangThai; }

    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public void setQuyCach(String quyCach) { this.quyCach = quyCach; }
    public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public void setDonGia(long donGia) { this.donGia = donGia; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
