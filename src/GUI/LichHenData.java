package GUI;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Bus.LichHenBUS;
import Entity.LichHen;

/**
 * Dữ liệu lịch hẹn dùng chung cho các màn hình của mục "Quản Lý Lịch Hẹn".
 * Đồng bộ thông qua LichHenBUS (chuẩn mô hình 3 lớp).
 */
public final class LichHenData {
    private LichHenData() {}

    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN  = "Đã xác nhận";
    public static final String DA_DOI       = "Đã đổi lịch";
    public static final String HOAN_TAT     = "Hoàn thành";
    public static final String DA_HUY       = "Đã hủy";

    public static final String[] DICH_VU = {
        "Khám tổng quát", "Tiêm phòng vaccine", "Spa & Grooming", "Xét nghiệm máu tổng quát", "Phẫu thuật lấy sỏi bàng quang", "Lưu trú thú cưng (theo dõi)"
    };
    public static final String[] BAC_SI = {
        "BS. Nguyễn Hoài Anh", "BS. Võ Thanh Hùng", "BS. Ngô Gia Bảo"
    };
    public static final String[] GIO = buildGio();

    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final LichHenBUS bus = new LichHenBUS();

    static {
        taiTuCSDL();
    }

    public static synchronized void taiTuCSDL() {
        bus.taiLai();
    }

    private static String[] buildGio() {
        List<String> r = new ArrayList<>();
        for (int h = 8; h <= 17; h++) {
            r.add(String.format("%02d:00", h));
            r.add(String.format("%02d:30", h));
        }
        return r.toArray(new String[0]);
    }

    // ---------------- Truy vấn / thao tác ----------------
    public static synchronized String nextMa() {
        return bus.maTiepTheo();
    }

    /** Danh sách (mới nhất trước). */
    public static synchronized List<LichHen> layDanhSach() {
        List<LichHen> r = new ArrayList<>(bus.layDanhSach());
        r.sort(Comparator.comparing(LichHen::thoiGian).reversed());
        return r;
    }

    /** Có lịch khác của cùng bác sĩ trùng ngày + giờ (còn hiệu lực) không? */
    public static synchronized boolean biTrung(String bacSi, LocalDate ngay, LocalTime gio, String boQuaMa) {
        return bus.biTrung(bacSi, ngay, gio, boQuaMa);
    }

    public static synchronized void them(LichHen l) {
        if (l.ma == null || l.ma.trim().isEmpty()) l.ma = nextMa();
        l.ghiLog("Tạo lịch hẹn");
        bus.them(l);
        fire();
    }

    public static synchronized void capNhat(LichHen l) {
        l.ghiLog("Cập nhật thông tin lịch hẹn");
        bus.capNhat(l);
        fire();
    }

    public static synchronized void xoa(LichHen l) {
        bus.xoa(l);
        fire();
    }

    public static synchronized void xacNhan(LichHen l) {
        l.trangThai = DA_XAC_NHAN;
        l.ghiLog("Xác nhận lịch hẹn");
        bus.capNhat(l);
        fire();
    }

    public static synchronized void doiLich(LichHen l, LocalDate ngayMoi, LocalTime gioMoi, String lyDo) {
        String cu = l.gio.format(TIME_FMT) + " " + l.ngay.format(CalendarPicker.DATE_FMT);
        l.ngay = ngayMoi;
        l.gio  = gioMoi;
        l.trangThai = DA_DOI;
        l.ghiLog("Đổi lịch hẹn: " + cu + " -> " + gioMoi.format(TIME_FMT) + " " + ngayMoi.format(CalendarPicker.DATE_FMT)
                + (lyDo.isEmpty() ? "" : ". Lý do: " + lyDo));
        bus.capNhat(l);
        fire();
    }

    public static synchronized void huy(LichHen l, String lyDo) {
        l.trangThai = DA_HUY;
        l.ghiLog("Hủy lịch hẹn. Lý do: " + lyDo);
        bus.capNhat(l);
        fire();
    }

    public static synchronized void addListener(Runnable r) { bus.addListener(r); }

    private static void fire() {
        bus.notifyListeners();
    }
}

