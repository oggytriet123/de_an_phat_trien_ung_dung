package Entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Nhật ký theo dõi diễn tiến điều trị hàng ngày / từng lần tái khám.
 */
public class NhatKyTheoDoi {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private LocalDateTime thoiGian;
    private String bacSi;
    private double canNang;       // kg
    private double thanNhiet;     // °C
    private String tinhTrang;     // Tiến triển tốt | Ổn định | Cần theo dõi sát | Đang hồi phục
    private String ghiChu;

    public NhatKyTheoDoi() {
        this(LocalDateTime.now(), "", 0, 38.5, "Ổn định", "");
    }

    public NhatKyTheoDoi(LocalDateTime thoiGian, String bacSi, double canNang, double thanNhiet, String tinhTrang, String ghiChu) {
        this.thoiGian = thoiGian == null ? LocalDateTime.now() : thoiGian;
        this.bacSi = bacSi == null ? "" : bacSi;
        this.canNang = canNang;
        this.thanNhiet = thanNhiet;
        this.tinhTrang = tinhTrang == null ? "Ổn định" : tinhTrang;
        this.ghiChu = ghiChu == null ? "" : ghiChu;
    }

    public String getThoiGianStr() {
        return thoiGian.format(FMT);
    }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getBacSi() { return bacSi; }
    public void setBacSi(String bacSi) { this.bacSi = bacSi; }

    public double getCanNang() { return canNang; }
    public void setCanNang(double canNang) { this.canNang = canNang; }

    public double getThanNhiet() { return thanNhiet; }
    public void setThanNhiet(double thanNhiet) { this.thanNhiet = thanNhiet; }

    public String getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(String tinhTrang) { this.tinhTrang = tinhTrang; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
