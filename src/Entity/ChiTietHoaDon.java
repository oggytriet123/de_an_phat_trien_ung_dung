package Entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Thực thể Chi tiết Hóa đơn (tương ứng bảng CHITIETHOADON trong CSDL).
 * Đại diện cho một dòng sản phẩm hoặc dịch vụ trong hóa đơn.
 */
public class ChiTietHoaDon implements Serializable {
    private static final long serialVersionUID = 1L;

    private HoaDon hoaDon;
    private SanPham sanPham;
    private int soLuong = 1;
    private double donGia = 0;
    private String sku;            // Mã SKU nếu là sản phẩm hàng hóa (SANPHAM.sku_maSP)
    private String maDV;           // Mã dịch vụ nếu là dịch vụ (DICHVU.maDV)
    private String danhMuc;        // Tên danh mục
    private boolean dichVu;        // true nếu là dịch vụ, false nếu là sản phẩm hàng hóa

    public ChiTietHoaDon() {
        this.soLuong = 1;
    }

    public ChiTietHoaDon(SanPham sanPham, int soLuong) {
        this.soLuong = Math.max(1, soLuong);
        setSanPham(sanPham);
    }

    public ChiTietHoaDon(SanPham sanPham, int soLuong, double donGia) {
        this.soLuong = Math.max(1, soLuong);
        this.donGia = donGia;
        setSanPham(sanPham);
    }

    public ChiTietHoaDon(HoaDon hoaDon, SanPham sanPham, int soLuong, double donGia) {
        this.hoaDon = hoaDon;
        this.soLuong = Math.max(1, soLuong);
        this.donGia = donGia;
        setSanPham(sanPham);
    }

    public ChiTietHoaDon saoChep() {
        ChiTietHoaDon c = new ChiTietHoaDon();
        c.hoaDon = this.hoaDon;
        c.sanPham = this.sanPham;
        c.soLuong = this.soLuong;
        c.donGia = this.donGia;
        c.sku = this.sku;
        c.maDV = this.maDV;
        c.danhMuc = this.danhMuc;
        c.dichVu = this.dichVu;
        return c;
    }

    // Getters & Setters
    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public String getMaHD() {
        return hoaDon != null ? hoaDon.getMaHD() : null;
    }

    public SanPham getSanPham() {
        if (sanPham == null && (sku != null || maDV != null)) {
            String code = (sku != null && !sku.trim().isEmpty()) ? sku : maDV;
            sanPham = new SanPham(code != null ? code : "SP",
                    "Sản phẩm",
                    danhMuc != null ? danhMuc : "Chung",
                    Math.round(donGia),
                    dichVu);
        }
        return sanPham;
    }

    public void setSanPham(SanPham sp) {
        this.sanPham = sp;
        if (sp != null) {
            this.dichVu = sp.isDichVu();
            if (sp.isDichVu()) {
                this.maDV = sp.getSku();
                this.sku = null;
            } else {
                this.sku = sp.getSku();
                this.maDV = null;
            }
            this.danhMuc = sp.getDanhMuc();
            if (this.donGia <= 0) {
                this.donGia = sp.getDonGia();
            }
        }
    }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) {
        this.soLuong = Math.max(1, soLuong);
    }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) {
        this.donGia = Math.max(0, donGia);
    }

    public String getSku() {
        if (sku != null) return sku;
        if (sanPham != null && !dichVu) return sanPham.getSku();
        return null;
    }
    public void setSku(String sku) { this.sku = sku; }

    public String getMaDV() {
        if (maDV != null) return maDV;
        if (sanPham != null && dichVu) return sanPham.getSku();
        return null;
    }
    public void setMaDV(String maDV) { this.maDV = maDV; }

    public String getTenSP() {
        if (sanPham != null) return sanPham.getTen();
        return "";
    }

    public String getDanhMuc() {
        if (danhMuc != null && !danhMuc.trim().isEmpty()) return danhMuc;
        if (sanPham != null) return sanPham.getDanhMuc();
        return "Chung";
    }
    public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }

    public boolean isDichVu() {
        if (sanPham != null) return sanPham.isDichVu();
        return dichVu;
    }
    public void setDichVu(boolean dichVu) { this.dichVu = dichVu; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietHoaDon that = (ChiTietHoaDon) o;
        return Objects.equals(getSku(), that.getSku()) && Objects.equals(getMaDV(), that.getMaDV());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, maDV);
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon{" +
                "sku='" + getSku() + '\'' +
                ", maDV='" + getMaDV() + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                '}';
    }
}
