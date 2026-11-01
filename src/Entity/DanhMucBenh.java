package Entity;

/** Một bệnh trong từ điển bệnh lý thú cưng (bảng DANHMUCBENH). */
public class DanhMucBenh {
    private String maBenh;
    private String tenBenh;
    private String trieuChung;

    public DanhMucBenh(String maBenh, String tenBenh, String trieuChung) {
        this.maBenh = maBenh;
        this.tenBenh = tenBenh;
        this.trieuChung = trieuChung;
    }

    public String getMaBenh() { return maBenh; }
    public void setMaBenh(String maBenh) { this.maBenh = maBenh; }
    public String getTenBenh() { return tenBenh; }
    public void setTenBenh(String tenBenh) { this.tenBenh = tenBenh; }
    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }

    @Override
    public String toString() {
        return maBenh + " - " + tenBenh;
    }
}
