package Entity;

/** Ca phẫu thuật thú cưng: đăng ký, bác sĩ chính, chi phí và tiến độ. */
public class CaPhauThuat {
    private String maCa;
    private String tenThuCung;
    private String loaiThuCung;
    private String tenChu;
    private String sdt;
    private String loaiPhauThuat;
    private String ngayMo;
    private String gioMo;
    private String bacSiChinh;
    private long chiPhi;
    private String trangThai; // "Chờ phẫu thuật", "Đang phẫu thuật", "Theo dõi hậu phẫu", "Hoàn thành"
    private String ghiChu;
    private String petID;
    private String maKH;

    public CaPhauThuat(String maCa, String tenThuCung, String loaiThuCung, String tenChu, String sdt,
                       String loaiPhauThuat, String ngayMo, String gioMo, String bacSiChinh,
                       long chiPhi, String trangThai, String ghiChu) {
        this(maCa, tenThuCung, loaiThuCung, tenChu, sdt, loaiPhauThuat, ngayMo, gioMo, bacSiChinh, chiPhi, trangThai, ghiChu, null, null);
    }

    public CaPhauThuat(String maCa, String tenThuCung, String loaiThuCung, String tenChu, String sdt,
                       String loaiPhauThuat, String ngayMo, String gioMo, String bacSiChinh,
                       long chiPhi, String trangThai, String ghiChu, String petID, String maKH) {
        this.maCa = maCa;
        this.tenThuCung = tenThuCung;
        this.loaiThuCung = loaiThuCung;
        this.tenChu = tenChu;
        this.sdt = sdt;
        this.loaiPhauThuat = loaiPhauThuat;
        this.ngayMo = ngayMo;
        this.gioMo = gioMo;
        this.bacSiChinh = bacSiChinh;
        this.chiPhi = chiPhi;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.petID = petID;
        this.maKH = maKH;
    }

    public String getMaCa() { return maCa; }
    public String getTenThuCung() { return tenThuCung; }
    public String getLoaiThuCung() { return loaiThuCung; }
    public String getTenChu() { return tenChu; }
    public String getSdt() { return sdt; }
    public String getLoaiPhauThuat() { return loaiPhauThuat; }
    public String getNgayMo() { return ngayMo; }
    public String getGioMo() { return gioMo; }
    public String getBacSiChinh() { return bacSiChinh; }
    public long getChiPhi() { return chiPhi; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
    public String getPetID() { return petID; }
    public String getMaKH() { return maKH; }

    public void setMaCa(String maCa) { this.maCa = maCa; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }
    public void setLoaiThuCung(String loaiThuCung) { this.loaiThuCung = loaiThuCung; }
    public void setTenChu(String tenChu) { this.tenChu = tenChu; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public void setLoaiPhauThuat(String loaiPhauThuat) { this.loaiPhauThuat = loaiPhauThuat; }
    public void setNgayMo(String ngayMo) { this.ngayMo = ngayMo; }
    public void setGioMo(String gioMo) { this.gioMo = gioMo; }
    public void setBacSiChinh(String bacSiChinh) { this.bacSiChinh = bacSiChinh; }
    public void setChiPhi(long chiPhi) { this.chiPhi = chiPhi; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public void setPetID(String petID) { this.petID = petID; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
}
