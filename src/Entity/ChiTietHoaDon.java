package Entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Thực thể Chi tiết Hóa đơn (tương ứng bảng CHITIETHOADON trong CSDL).
 * Đại diện cho một dòng sản phẩm hoặc dịch vụ trong hóa đơn.
 */
public class ChiTietHoaDon implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maCTHD;
    private HoaDon hoaDon;
    private SanPham sanPham;
    private int soLuong = 1;
    private double donGia = 0;
    private double thanhTien = 0;
    private String sku;            // Mã SKU nếu là sản phẩm hàng hóa (SANPHAM.sku_maSP)
    private String maDV;           // Mã dịch vụ nếu là dịch vụ (DICHVU.maDV)
    private String tenSP;          // Tên sản phẩm / dịch vụ lưu vết
    private String danhMuc;        // Tên danh mục lưu vết
    private boolean dichVu;        // true nếu là dịch vụ, false nếu là sản phẩm hàng hóa

    public ChiTietHoaDon() {
        this.soLuong = 1;
    }

    public ChiTietHoaDon(SanPham sanPham, int soLuong) {
        this.soLuong = Math.max(1, soLuong);
        setSanPham(sanPham);
    }

    public ChiTietHoaDon(String maCTHD, SanPham sanPham, int soLuong, double donGia) {
        this.maCTHD = maCTHD;
        this.soLuong = Math.max(1, soLuong);
        this.donGia = donGia;
        setSanPham(sanPham);
        tinhThanhTien();
    }

    public ChiTietHoaDon(String maCTHD, HoaDon hoaDon, SanPham sanPham, int soLuong, double donGia) {
        this.maCTHD = maCTHD;
        this.hoaDon = hoaDon;
        this.soLuong = Math.max(1, soLuong);
        this.donGia = donGia;
        setSanPham(sanPham);
        tinhThanhTien();
    }

    public ChiTietHoaDon(String maCTHD, HoaDon hoaDon, SanPham sanPham, int soLuong, double donGia, double thanhTien) {
        this.maCTHD = maCTHD;
        this.hoaDon = hoaDon;
        this.soLuong = Math.max(1, soLuong);
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        setSanPham(sanPham);
        if (this.thanhTien <= 0) {
            tinhThanhTien();
        }
    }

    public ChiTietHoaDon saoChep() {
        ChiTietHoaDon c = new ChiTietHoaDon();
        c.maCTHD = this.maCTHD;
        c.hoaDon = this.hoaDon;
        c.sanPham = this.sanPham;
        c.soLuong = this.soLuong;
        c.donGia = this.donGia;
        c.thanhTien = this.thanhTien;
        c.sku = this.sku;
        c.maDV = this.maDV;
        c.tenSP = this.tenSP;
        c.danhMuc = this.danhMuc;
        c.dichVu = this.dichVu;
        return c;
    }

    public double tinhThanhTien() {
        this.thanhTien = this.donGia * this.soLuong;
        return this.thanhTien;
    }

    /**
     * Phương thức trả về thành tiền kiểu long phục vụ hiển thị tiền tệ và tương thích với nghiệp vụ cũ.
     */
    public long thanhTien() {
        return Math.round(thanhTien > 0 ? thanhTien : (donGia * soLuong));
    }

    // Getters & Setters
    public String getMaCTHD() { return maCTHD; }
    public void setMaCTHD(String maCTHD) { this.maCTHD = maCTHD; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public String getMaHD() {
        return hoaDon != null ? hoaDon.getMaHD() : null;
    }

    public SanPham getSanPham() {
        if (sanPham == null && (sku != null || maDV != null || tenSP != null)) {
            String code = (sku != null && !sku.trim().isEmpty()) ? sku : maDV;
            sanPham = new SanPham(code != null ? code : "SP",
                    tenSP != null ? tenSP : "Sản phẩm",
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
            this.tenSP = sp.getTen();
            this.danhMuc = sp.getDanhMuc();
            if (this.donGia <= 0) {
                this.donGia = sp.getDonGia();
            }
        }
        tinhThanhTien();
    }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) {
        this.soLuong = Math.max(1, soLuong);
        tinhThanhTien();
    }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) {
        this.donGia = Math.max(0, donGia);
        tinhThanhTien();
    }

    public double getThanhTien() {
        return thanhTien > 0 ? thanhTien : tinhThanhTien();
    }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

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
        if (tenSP != null && !tenSP.trim().isEmpty()) return tenSP;
        if (sanPham != null) return sanPham.getTen();
        return "";
    }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

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
        if (maCTHD != null && that.maCTHD != null) {
            return Objects.equals(maCTHD, that.maCTHD);
        }
        return Objects.equals(getSku(), that.getSku()) && Objects.equals(getMaDV(), that.getMaDV());
    }

    @Override
    public int hashCode() {
        return Objects.hash(maCTHD, sku, maDV);
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon{" +
                "maCTHD='" + maCTHD + '\'' +
                ", tenSP='" + getTenSP() + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                ", thanhTien=" + getThanhTien() +
                '}';
    }
}
