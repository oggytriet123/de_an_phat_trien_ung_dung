package Entity;

import java.time.LocalDate;

public class ThuCung {
    private final String maTC;
    private String ten;
    private String giong;
    private String loai;          // Chó | Mèo
    private String tenChu;
    private String sdtChu;
    private double canNang;       // Đơn vị kg
    private String sucKhoe;       // Khỏe mạnh | Cần theo dõi | Đang điều trị
    private boolean dangLuuTru;
    private LocalDate ngayTiepNhan;
    private String ghiChu;

    public ThuCung(String maTC, String ten, String giong, String loai, String tenChu, String sdtChu,
                   double canNang, String sucKhoe, boolean dangLuuTru, LocalDate ngayTiepNhan, String ghiChu) {
        this.maTC = maTC;
        this.ten = ten;
        this.giong = giong;
        this.loai = loai;
        this.tenChu = tenChu;
        this.sdtChu = sdtChu;
        this.canNang = canNang;
        this.sucKhoe = sucKhoe;
        this.dangLuuTru = dangLuuTru;
        this.ngayTiepNhan = ngayTiepNhan;
        this.ghiChu = ghiChu == null ? "" : ghiChu;
    }

    public String getMaTC() { return maTC; }
    public String getTen() { return ten; }
    public String getGiong() { return giong; }
    public String getLoai() { return loai; }
    public String getTenChu() { return tenChu; }
    public String getSdtChu() { return sdtChu; }
    public double getCanNang() { return canNang; }
    public String getSucKhoe() { return sucKhoe; }
    public boolean isDangLuuTru() { return dangLuuTru; }
    public LocalDate getNgayTiepNhan() { return ngayTiepNhan; }
    public String getGhiChu() { return ghiChu; }

    public void setTen(String ten) { this.ten = ten; }
    public void setGiong(String giong) { this.giong = giong; }
    public void setLoai(String loai) { this.loai = loai; }
    public void setTenChu(String tenChu) { this.tenChu = tenChu; }
    public void setSdtChu(String sdtChu) { this.sdtChu = sdtChu; }
    public void setCanNang(double canNang) { this.canNang = canNang; }
    public void setSucKhoe(String sucKhoe) { this.sucKhoe = sucKhoe; }
    public void setDangLuuTru(boolean dangLuuTru) { this.dangLuuTru = dangLuuTru; }
    public void setNgayTiepNhan(LocalDate ngayTiepNhan) { this.ngayTiepNhan = ngayTiepNhan; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
