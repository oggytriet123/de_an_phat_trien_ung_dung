package DTO;

import java.io.Serializable;

/**
 * Model DTO chứa dữ liệu tổng hợp thống kê sản phẩm bán chạy nhất.
 */
public class ThongKeSanPhamBanChay implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rank;
    private String maSKU;
    private String tenSP;
    private String quyCach;
    private String danhMuc;
    private String donViTinh;
    private long donGia;
    private int soLuongBan;
    private long tongDoanhThu;
    private int soLuongTon;
    private double tyTrong;
    private String danhGia;

    public ThongKeSanPhamBanChay() {}

    public ThongKeSanPhamBanChay(int rank, String maSKU, String tenSP, String quyCach, String danhMuc,
                                 String donViTinh, long donGia, int soLuongBan, long tongDoanhThu,
                                 int soLuongTon, double tyTrong, String danhGia) {
        this.rank = rank;
        this.maSKU = maSKU;
        this.tenSP = tenSP;
        this.quyCach = quyCach;
        this.danhMuc = danhMuc;
        this.donViTinh = donViTinh;
        this.donGia = donGia;
        this.soLuongBan = soLuongBan;
        this.tongDoanhThu = tongDoanhThu;
        this.soLuongTon = soLuongTon;
        this.tyTrong = tyTrong;
        this.danhGia = danhGia;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getMaSKU() { return maSKU; }
    public void setMaSKU(String maSKU) { this.maSKU = maSKU; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public String getQuyCach() { return quyCach; }
    public void setQuyCach(String quyCach) { this.quyCach = quyCach; }

    public String getDanhMuc() { return danhMuc; }
    public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) { this.donGia = donGia; }

    public int getSoLuongBan() { return soLuongBan; }
    public void setSoLuongBan(int soLuongBan) { this.soLuongBan = soLuongBan; }

    public long getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(long tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public double getTyTrong() { return tyTrong; }
    public void setTyTrong(double tyTrong) { this.tyTrong = tyTrong; }

    public String getDanhGia() { return danhGia; }
    public void setDanhGia(String danhGia) { this.danhGia = danhGia; }
}
