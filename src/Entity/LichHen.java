package Entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Lịch hẹn — ánh xạ bảng LICHHEN trong CSDL.
 */
public class LichHen {
    private static final DateTimeFormatter LOG_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String ma;
    public String khachHang;
    public String sdt;
    public String thuCung;
    public String dichVu;
    public String bacSi;
    public LocalDate ngay;
    public LocalTime gio;
    public String ghiChu = "";
    public String trangThai = "Chờ xác nhận";
    public final List<String> lichSu = new ArrayList<>();

    public LocalDateTime thoiGian() {
        return LocalDateTime.of(ngay, gio);
    }

    public boolean conHieuLuc() {
        return !"Hoàn thành".equalsIgnoreCase(trangThai)
            && !"Hoàn tất".equalsIgnoreCase(trangThai)
            && !"Đã hủy".equalsIgnoreCase(trangThai);
    }

    public void ghiLog(String noiDung) {
        lichSu.add(LocalDateTime.now().format(LOG_FMT) + " - " + noiDung);
    }
}
