package Entity;

import java.time.LocalDate;

public class KhachHang {
    private final String maKH;
    private String hoTen;
    private String soDienThoai;
    private String hangThe;          // Thường | Bạc | Vàng | VIP
    private int diemTichLuy;
    private String thuCung;
    private LocalDate giaoDichGanNhat; // null nếu chưa có giao dịch
    private long tongChiTieu;

    public KhachHang(String maKH, String hoTen, String soDienThoai, String hangThe,
                     int diemTichLuy, String thuCung, LocalDate giaoDichGanNhat, long tongChiTieu) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.hangThe = hangThe;
        this.diemTichLuy = diemTichLuy;
        this.thuCung = thuCung;
        this.giaoDichGanNhat = giaoDichGanNhat;
        this.tongChiTieu = tongChiTieu;
    }

    public String getMaKH() { return maKH; }
    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getHangThe() { return hangThe; }
    public int getDiemTichLuy() { return diemTichLuy; }
    public String getThuCung() { return thuCung; }
    public LocalDate getGiaoDichGanNhat() { return giaoDichGanNhat; }
    public long getTongChiTieu() { return tongChiTieu; }

    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public void setHangThe(String hangThe) { this.hangThe = hangThe; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }
    public void setThuCung(String thuCung) { this.thuCung = thuCung; }
    public void setGiaoDichGanNhat(LocalDate d) { this.giaoDichGanNhat = d; }
    public void setTongChiTieu(long tongChiTieu) { this.tongChiTieu = tongChiTieu; }
}
