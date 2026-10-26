package Entity;

import java.time.LocalDateTime;

/**
 * Dịch vụ hoặc thức ăn, thuốc chăm sóc cho bé thú cưng trong thời gian lưu trú tại lồng (tương tự ChiTietHoaDon / Gọi món).
 */
public class ChiTietDichVuChuong {
    private String maDV;
    private String tenDV;
    private String loai; // "Thức ăn & Dinh dưỡng", "Dịch vụ Spa & Grooming", "Y tế & Thuốc"
    private double donGia;
    private int soLuong;
    private LocalDateTime thoiGian;
    private String ghiChu;

    public ChiTietDichVuChuong(String maDV, String tenDV, String loai, double donGia, int soLuong, LocalDateTime thoiGian, String ghiChu) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.loai = loai;
        this.donGia = donGia;
        this.soLuong = soLuong;
        this.thoiGian = thoiGian;
        this.ghiChu = ghiChu;
    }

    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }

    public String getTenDV() { return tenDV; }
    public void setTenDV(String tenDV) { this.tenDV = tenDV; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getThanhTien() { return donGia * soLuong; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
