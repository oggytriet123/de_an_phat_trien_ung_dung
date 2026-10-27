package Entity;

/**
 * Chi tiết một loại thuốc trong đơn thuốc.
 */
public class ChiTietDonThuoc {
    private String maSKU;          // Mã SKU trong kho nếu có (e.g. YT-0018), có thể để trống
    private String tenThuoc;       // Tên thuốc, hoạt chất, hàm lượng
    private String donViTinh;      // viên, gói, tuýp, lọ, ống, vỉ, ml, chai
    private int soLuong;           // Số lượng kê
    private long donGia;           // Đơn giá bán (VNĐ)
    private String cachDung;       // Liều dùng và hướng dẫn chi tiết

    public ChiTietDonThuoc() {
        this("", "", "viên", 1, 0, "");
    }

    public ChiTietDonThuoc(String maSKU, String tenThuoc, String donViTinh, int soLuong, long donGia, String cachDung) {
        this.maSKU = maSKU == null ? "" : maSKU;
        this.tenThuoc = tenThuoc == null ? "" : tenThuoc;
        this.donViTinh = donViTinh == null ? "viên" : donViTinh;
        this.soLuong = Math.max(1, soLuong);
        this.donGia = Math.max(0, donGia);
        this.cachDung = cachDung == null ? "" : cachDung;
    }

    public long getThanhTien() {
        return (long) soLuong * donGia;
    }

    public String getMaSKU() { return maSKU; }
    public void setMaSKU(String maSKU) { this.maSKU = maSKU; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = Math.max(1, soLuong); }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) { this.donGia = Math.max(0, donGia); }

    public String getCachDung() { return cachDung; }
    public void setCachDung(String cachDung) { this.cachDung = cachDung; }
}
