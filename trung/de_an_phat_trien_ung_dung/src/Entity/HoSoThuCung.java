package Entity;

import java.time.LocalDate;

/** Hồ sơ thú cưng: thông tin chung, chủ nuôi và tình trạng sức khỏe. */
public class HoSoThuCung {
    private final String maHoSo;
    private String tenThuCung;
    private String loai;      // Chó | Mèo | Khác
    private String giong;
    private String chuNuoi;
    private String sdtChu;
    private String sucKhoe;   // Tốt | Cần theo dõi | Dị ứng thuốc | Đang điều trị
    private LocalDate capNhat;

    public HoSoThuCung(String maHoSo, String tenThuCung, String loai, String giong,
                       String chuNuoi, String sdtChu, String sucKhoe, LocalDate capNhat) {
        this.maHoSo = maHoSo;
        this.tenThuCung = tenThuCung;
        this.loai = loai;
        this.giong = giong;
        this.chuNuoi = chuNuoi;
        this.sdtChu = sdtChu;
        this.sucKhoe = sucKhoe;
        this.capNhat = capNhat;
    }

    public String getMaHoSo() { return maHoSo; }
    public String getTenThuCung() { return tenThuCung; }
    public String getLoai() { return loai; }
    public String getGiong() { return giong; }
    public String getChuNuoi() { return chuNuoi; }
    public String getSdtChu() { return sdtChu; }
    public String getSucKhoe() { return sucKhoe; }
    public LocalDate getCapNhat() { return capNhat; }

    public void setTenThuCung(String v) { this.tenThuCung = v; }
    public void setLoai(String v) { this.loai = v; }
    public void setGiong(String v) { this.giong = v; }
    public void setChuNuoi(String v) { this.chuNuoi = v; }
    public void setSdtChu(String v) { this.sdtChu = v; }
    public void setSucKhoe(String v) { this.sucKhoe = v; }
    public void setCapNhat(LocalDate v) { this.capNhat = v; }
}
