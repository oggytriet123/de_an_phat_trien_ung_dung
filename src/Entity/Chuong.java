package Entity;

import java.time.LocalDateTime;

/**
 * Thực thể Lồng Chuồng lưu trữ thú cưng (tương tự Bàn trong phòng bàn).
 */
public class Chuong {
    private String maChuong;      // Ví dụ: C-01, C-02, VIP-01
    private int soChuong;         // Số thứ tự 1, 2, 3...
    private String tenChuong;     // Tên hiển thị
    private String size;          // "Nhỏ (<5kg)", "Vừa (5-15kg)", "Lớn (>15kg)", "VIP Suite"
    private String khuVuc;        // "Khu Mèo", "Khu Chó Nhỏ", "Khu Chó Lớn", "Khu VIP"
    private String trangThai;     // "Trống", "Đã đặt", "Đang lưu trú", "Bảo trì"
    private double giaTheoNgay;   // Đơn giá lưu trú 1 ngày (VNĐ)
    private boolean isVip;        // Lồng VIP có điều hòa, camera 24/7

    // Thông tin thú cưng và khách hàng đang lưu trú hoặc đặt trước (null nếu trống)
    private String tenPet;
    private String loaiPet;
    private double canNang;
    private String tenKhach;
    private String sdtKhach;
    private LocalDateTime ngayVao;
    private LocalDateTime ngayRaDuKien;
    private String ghiChu;

    public Chuong(String maChuong, int soChuong, String tenChuong, String size,
                  String khuVuc, String trangThai, double giaTheoNgay, boolean isVip) {
        this.maChuong = maChuong;
        this.soChuong = soChuong;
        this.tenChuong = tenChuong;
        this.size = size;
        this.khuVuc = khuVuc;
        this.trangThai = trangThai;
        this.giaTheoNgay = giaTheoNgay;
        this.isVip = isVip;
    }

    public Chuong(String maChuong, int soChuong, String tenChuong, String size,
                  String khuVuc, String trangThai, double giaTheoNgay, boolean isVip,
                  String tenPet, String loaiPet, double canNang,
                  String tenKhach, String sdtKhach,
                  LocalDateTime ngayVao, LocalDateTime ngayRaDuKien, String ghiChu) {
        this(maChuong, soChuong, tenChuong, size, khuVuc, trangThai, giaTheoNgay, isVip);
        this.tenPet = tenPet;
        this.loaiPet = loaiPet;
        this.canNang = canNang;
        this.tenKhach = tenKhach;
        this.sdtKhach = sdtKhach;
        this.ngayVao = ngayVao;
        this.ngayRaDuKien = ngayRaDuKien;
        this.ghiChu = ghiChu;
    }

    // Getters và Setters
    public String getMaChuong() { return maChuong; }
    public void setMaChuong(String maChuong) { this.maChuong = maChuong; }

    public int getSoChuong() { return soChuong; }
    public void setSoChuong(int soChuong) { this.soChuong = soChuong; }

    public String getTenChuong() { return tenChuong; }
    public void setTenChuong(String tenChuong) { this.tenChuong = tenChuong; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String khuVuc) { this.khuVuc = khuVuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public double getGiaTheoNgay() { return giaTheoNgay; }
    public void setGiaTheoNgay(double giaTheoNgay) { this.giaTheoNgay = giaTheoNgay; }

    public boolean isVip() { return isVip; }
    public void setVip(boolean vip) { isVip = vip; }

    public String getTenPet() { return tenPet; }
    public void setTenPet(String tenPet) { this.tenPet = tenPet; }

    public String getLoaiPet() { return loaiPet; }
    public void setLoaiPet(String loaiPet) { this.loaiPet = loaiPet; }

    public double getCanNang() { return canNang; }
    public void setCanNang(double canNang) { this.canNang = canNang; }

    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }

    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }

    public LocalDateTime getNgayVao() { return ngayVao; }
    public void setNgayVao(LocalDateTime ngayVao) { this.ngayVao = ngayVao; }

    public LocalDateTime getNgayRaDuKien() { return ngayRaDuKien; }
    public void setNgayRaDuKien(LocalDateTime ngayRaDuKien) { this.ngayRaDuKien = ngayRaDuKien; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public boolean isTrong() {
        return "Trống".equalsIgnoreCase(trangThai);
    }

    public boolean isDaDat() {
        return "Đã đặt".equalsIgnoreCase(trangThai);
    }

    public boolean isDangLuuTru() {
        return "Đang lưu trú".equalsIgnoreCase(trangThai) || "Đang sử dụng".equalsIgnoreCase(trangThai);
    }

    public boolean isBaoTri() {
        return "Bảo trì".equalsIgnoreCase(trangThai) || "Vệ sinh".equalsIgnoreCase(trangThai);
    }
}
