package Entity;

/** Phiếu tiếp nhận và theo dõi tiêm phòng vaccine thú cưng. */
public class PhieuTiem {
    private String maPhieu;
    private String tenThuCung;
    private String loaiThuCung;
    private String tenChu;
    private String sdt;
    private String loaiVaccine;
    private String ngayTiem;
    private String ngayHenNhac;
    private String bacSi;
    private String trangThai; // "Đã tiêm", "Chờ tiêm", "Quá hạn"
    private String ghiChu;
    private String petID;
    private String maKH;

    public PhieuTiem(String maPhieu, String tenThuCung, String loaiThuCung, String tenChu, String sdt,
                     String loaiVaccine, String ngayTiem, String ngayHenNhac, String bacSi,
                     String trangThai, String ghiChu) {
        this(maPhieu, tenThuCung, loaiThuCung, tenChu, sdt, loaiVaccine, ngayTiem, ngayHenNhac, bacSi, trangThai, ghiChu, null, null);
    }

    public PhieuTiem(String maPhieu, String tenThuCung, String loaiThuCung, String tenChu, String sdt,
                     String loaiVaccine, String ngayTiem, String ngayHenNhac, String bacSi,
                     String trangThai, String ghiChu, String petID, String maKH) {
        this.maPhieu = maPhieu;
        this.tenThuCung = tenThuCung;
        this.loaiThuCung = loaiThuCung;
        this.tenChu = tenChu;
        this.sdt = sdt;
        this.loaiVaccine = loaiVaccine;
        this.ngayTiem = ngayTiem;
        this.ngayHenNhac = ngayHenNhac;
        this.bacSi = bacSi;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.petID = petID;
        this.maKH = maKH;
    }

    public String getMaPhieu() { return maPhieu; }
    public String getTenThuCung() { return tenThuCung; }
    public String getLoaiThuCung() { return loaiThuCung; }
    public String getTenChu() { return tenChu; }
    public String getSdt() { return sdt; }
    public String getLoaiVaccine() { return loaiVaccine; }
    public String getNgayTiem() { return ngayTiem; }
    public String getNgayHenNhac() { return ngayHenNhac; }
    public String getBacSi() { return bacSi; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
    public String getPetID() { return petID; }
    public String getMaKH() { return maKH; }

    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }
    public void setLoaiThuCung(String loaiThuCung) { this.loaiThuCung = loaiThuCung; }
    public void setTenChu(String tenChu) { this.tenChu = tenChu; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public void setLoaiVaccine(String loaiVaccine) { this.loaiVaccine = loaiVaccine; }
    public void setNgayTiem(String ngayTiem) { this.ngayTiem = ngayTiem; }
    public void setNgayHenNhac(String ngayHenNhac) { this.ngayHenNhac = ngayHenNhac; }
    public void setBacSi(String bacSi) { this.bacSi = bacSi; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public void setPetID(String petID) { this.petID = petID; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
}
