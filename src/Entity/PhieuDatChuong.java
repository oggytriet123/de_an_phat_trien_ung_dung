package Entity;

import java.time.LocalDateTime;

/**
 * Phiếu đặt lịch trước lồng chuồng lưu trữ cho thú cưng (tương tự PhieuDatBan).
 */
public class PhieuDatChuong {
    private String maPhieu;
    private String maChuong;
    private String tenKhach;
    private String sdtKhach;
    private String tenPet;
    private String loaiPet;
    private double canNang;
    private LocalDateTime ngayDat;
    private LocalDateTime ngayDen;
    private LocalDateTime ngayTraDuKien;
    private double tienCoc;
    private String trangThai; // "Đã đặt", "Đã nhận", "Đã hủy"
    private String ghiChu;

    public PhieuDatChuong(String maPhieu, String maChuong, String tenKhach, String sdtKhach,
                          String tenPet, String loaiPet, double canNang,
                          LocalDateTime ngayDat, LocalDateTime ngayDen, LocalDateTime ngayTraDuKien,
                          double tienCoc, String trangThai, String ghiChu) {
        this.maPhieu = maPhieu;
        this.maChuong = maChuong;
        this.tenKhach = tenKhach;
        this.sdtKhach = sdtKhach;
        this.tenPet = tenPet;
        this.loaiPet = loaiPet;
        this.canNang = canNang;
        this.ngayDat = ngayDat;
        this.ngayDen = ngayDen;
        this.ngayTraDuKien = ngayTraDuKien;
        this.tienCoc = tienCoc;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public String getMaChuong() { return maChuong; }
    public void setMaChuong(String maChuong) { this.maChuong = maChuong; }

    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }

    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }

    public String getTenPet() { return tenPet; }
    public void setTenPet(String tenPet) { this.tenPet = tenPet; }

    public String getLoaiPet() { return loaiPet; }
    public void setLoaiPet(String loaiPet) { this.loaiPet = loaiPet; }

    public double getCanNang() { return canNang; }
    public void setCanNang(double canNang) { this.canNang = canNang; }

    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }

    public LocalDateTime getNgayDen() { return ngayDen; }
    public void setNgayDen(LocalDateTime ngayDen) { this.ngayDen = ngayDen; }

    public LocalDateTime getNgayTraDuKien() { return ngayTraDuKien; }
    public void setNgayTraDuKien(LocalDateTime ngayTraDuKien) { this.ngayTraDuKien = ngayTraDuKien; }

    public double getTienCoc() { return tienCoc; }
    public void setTienCoc(double tienCoc) { this.tienCoc = tienCoc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
