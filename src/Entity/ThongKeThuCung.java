package Entity;

/**
 * Số liệu thống kê hồ sơ thú cưng hiển thị trên các thẻ KPI.
 */
public class ThongKeThuCung {
    public final int tongThuCung;
    public final int soCho;
    public final int soMeo;
    public final int soKhac;
    public final int soCanhBao;
    public final int soTiepNhanThang;

    public ThongKeThuCung(int tongThuCung, int soCho, int soMeo, int soKhac, int soCanhBao, int soTiepNhanThang) {
        this.tongThuCung = tongThuCung;
        this.soCho = soCho;
        this.soMeo = soMeo;
        this.soKhac = soKhac;
        this.soCanhBao = soCanhBao;
        this.soTiepNhanThang = soTiepNhanThang;
    }
}
