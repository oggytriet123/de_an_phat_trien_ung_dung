package Entity;

/** Đối tác / Nhà cung cấp liên kết với cửa hàng. */
public class DoiTac {
    private String maDT;
    private String tenDT;
    private String loaiDT;
    private String soDienThoai;
    private String diaChi;
    private String email;
    private String trangThai;

    public DoiTac(String maDT, String tenDT, String loaiDT, String soDienThoai, String diaChi, String email, String trangThai) {
        this.maDT = maDT;
        this.tenDT = tenDT;
        this.loaiDT = loaiDT;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.email = email;
        this.trangThai = trangThai;
    }

    public String getMaDT() { return maDT; }
    public String getTenDT() { return tenDT; }
    public String getLoaiDT() { return loaiDT; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public String getEmail() { return email; }
    public String getTrangThai() { return trangThai; }

    public void setTenDT(String tenDT) { this.tenDT = tenDT; }
    public void setLoaiDT(String loaiDT) { this.loaiDT = loaiDT; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public void setEmail(String email) { this.email = email; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return tenDT;
    }
}
