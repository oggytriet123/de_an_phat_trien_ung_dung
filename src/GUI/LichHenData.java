package GUI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dữ liệu lịch hẹn dùng chung cho 5 màn hình của mục "Quản Lý Lịch Hẹn".
 * Lưu trong bộ nhớ (mẫu) để chạy thử giao diện; khi nối CSDL chỉ cần thay các hàm trong lớp này.
 */
public final class LichHenData {
    private LichHenData() {}

    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DA_DOI = "Đã đổi lịch";
    public static final String HOAN_TAT = "Hoàn tất";
    public static final String DA_HUY = "Đã hủy";

    public static final String[] DICH_VU = {
        "Khám tổng quát", "Tiêm phòng", "Spa & Grooming", "Điều trị bệnh", "Phẫu thuật nhỏ", "Lưu trú"
    };
    public static final String[] BAC_SI = {
        "BS. Nguyễn Thanh Tùng", "BS. Lê Mai Anh", "BS. Phạm Quốc Việt"
    };
    public static final String[] GIO = buildGio();

    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LOG_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Một lịch hẹn. */
    public static class LichHen {
        public String ma;
        public String khachHang;
        public String sdt;
        public String thuCung;
        public String dichVu;
        public String bacSi;
        public LocalDate ngay;
        public LocalTime gio;
        public String ghiChu = "";
        public String trangThai = CHO_XAC_NHAN;
        public final List<String> lichSu = new ArrayList<>();

        public LocalDateTime thoiGian() { return LocalDateTime.of(ngay, gio); }
        public boolean conHieuLuc() { return !HOAN_TAT.equals(trangThai) && !DA_HUY.equals(trangThai); }
        public void ghiLog(String noiDung) { lichSu.add(LocalDateTime.now().format(LOG_FMT) + " · " + noiDung); }
    }

    private static final List<LichHen> DS = new ArrayList<>();
    private static final List<Runnable> LISTENERS = new ArrayList<>();
    private static int seq = 0;

    static {
        LocalDate t = LocalDate.now();
        seed("Nguyễn Minh An", "0901234567", "Mimi (Mèo)", "Khám tổng quát", 0, t.plusDays(1), "09:00", "Mèo bỏ ăn 2 ngày", CHO_XAC_NHAN);
        seed("Trần Kim Chi", "0912345678", "Lu (Chó Poodle)", "Tiêm phòng", 1, t.plusDays(1), "10:30", "Mũi dại nhắc lại", CHO_XAC_NHAN);
        seed("Lê Thu Hà", "0933456789", "Bông (Chó Phú Quốc)", "Spa & Grooming", 2, t.plusDays(2), "14:00", "", DA_XAC_NHAN);
        seed("Phạm Quốc Bảo", "0944567890", "Tom (Mèo Anh lông ngắn)", "Điều trị bệnh", 0, t.plusDays(2), "15:30", "Viêm da, ngứa nhiều", DA_XAC_NHAN);
        seed("Võ Minh Khang", "0955678901", "Kiki (Vẹt)", "Khám tổng quát", 1, t.plusDays(3), "08:30", "", CHO_XAC_NHAN);
        seed("Đặng Ngọc Lan", "0966789012", "Sushi (Mèo Ba Tư)", "Phẫu thuật nhỏ", 2, t.plusDays(4), "09:30", "Triệt sản, nhịn ăn từ tối hôm trước", DA_XAC_NHAN);
        seed("Hoàng Gia Huy", "0977890123", "Bin (Chó Corgi)", "Lưu trú", 0, t.plusDays(5), "08:00", "Gửi 3 ngày", DA_DOI);
        seed("Bùi Thanh Mai", "0988901234", "Susu (Thỏ)", "Khám tổng quát", 1, t.minusDays(1), "16:00", "", HOAN_TAT);
        seed("Ngô Đức Thịnh", "0399012345", "Rex (Chó Becgie)", "Tiêm phòng", 2, t.minusDays(2), "11:00", "", HOAN_TAT);
        seed("Phan Thị Yến", "0388123456", "Nu (Mèo)", "Spa & Grooming", 0, t.minusDays(3), "13:30", "Khách báo bận", DA_HUY);
        seed("Đỗ Hữu Phước", "0377234567", "Mon (Chó Husky)", "Điều trị bệnh", 1, t.plusDays(7), "10:00", "Tái khám sau điều trị", CHO_XAC_NHAN);
        seed("Lý Bảo Châu", "0366345678", "Cam (Mèo)", "Tiêm phòng", 2, t.plusDays(8), "15:00", "", DA_XAC_NHAN);
    }

    private static void seed(String kh, String sdt, String tc, String dv, int bs, LocalDate ngay, String gio,
                             String ghiChu, String tt) {
        LichHen l = new LichHen();
        l.ma = nextMa();
        l.khachHang = kh;
        l.sdt = sdt;
        l.thuCung = tc;
        l.dichVu = dv;
        l.bacSi = BAC_SI[bs];
        l.ngay = ngay;
        l.gio = LocalTime.parse(gio);
        l.ghiChu = ghiChu;
        l.trangThai = tt;
        l.lichSu.add(LocalDateTime.of(ngay.minusDays(2), LocalTime.of(8, 30)).format(LOG_FMT) + " · Tạo lịch hẹn");
        if (DA_XAC_NHAN.equals(tt) || HOAN_TAT.equals(tt))
            l.lichSu.add(LocalDateTime.of(ngay.minusDays(1), LocalTime.of(9, 0)).format(LOG_FMT) + " · Xác nhận lịch hẹn");
        if (DA_DOI.equals(tt))
            l.lichSu.add(LocalDateTime.of(ngay.minusDays(1), LocalTime.of(9, 0)).format(LOG_FMT) + " · Đổi lịch hẹn (khách yêu cầu)");
        if (DA_HUY.equals(tt))
            l.lichSu.add(LocalDateTime.of(ngay.minusDays(1), LocalTime.of(9, 0)).format(LOG_FMT) + " · Hủy lịch hẹn. Lý do: Khách báo bận");
        DS.add(l);
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
        seq++;
        return String.format("LH-%d-%04d", LocalDate.now().getYear(), seq);
    }

    /** Danh sách (mới nhất trước). */
    public static synchronized List<LichHen> layDanhSach() {
        List<LichHen> r = new ArrayList<>(DS);
        r.sort(Comparator.comparing(LichHen::thoiGian).reversed());
        return r;
    }

    /** Có lịch khác của cùng bác sĩ trùng ngày + giờ (còn hiệu lực) không? */
    public static synchronized boolean biTrung(String bacSi, LocalDate ngay, LocalTime gio, String boQuaMa) {
        for (LichHen l : DS) {
            if (l.ma.equals(boQuaMa) || !l.conHieuLuc()) continue;
            if (l.bacSi.equals(bacSi) && l.ngay.equals(ngay) && l.gio.equals(gio)) return true;
        }
        return false;
    }

    public static synchronized void them(LichHen l) {
        l.ma = nextMa();
        l.ghiLog("Tạo lịch hẹn");
        DS.add(l);
        fire();
    }

    public static synchronized void capNhat(LichHen l) {
        l.ghiLog("Cập nhật thông tin lịch hẹn");
        fire();
    }

    public static synchronized void xoa(LichHen l) {
        DS.remove(l);
        fire();
    }

    public static synchronized void xacNhan(LichHen l) {
        l.trangThai = DA_XAC_NHAN;
        l.ghiLog("Xác nhận lịch hẹn");
        fire();
    }

    public static synchronized void doiLich(LichHen l, LocalDate ngayMoi, LocalTime gioMoi, String lyDo) {
        String cu = l.gio.format(TIME_FMT) + " " + l.ngay.format(CalendarPicker.DATE_FMT);
        l.ngay = ngayMoi;
        l.gio = gioMoi;
        l.trangThai = DA_DOI;
        l.ghiLog("Đổi lịch hẹn: " + cu + " → " + gioMoi.format(TIME_FMT) + " " + ngayMoi.format(CalendarPicker.DATE_FMT)
                + (lyDo.isEmpty() ? "" : ". Lý do: " + lyDo));
        fire();
    }

    public static synchronized void huy(LichHen l, String lyDo) {
        l.trangThai = DA_HUY;
        l.ghiLog("Hủy lịch hẹn. Lý do: " + lyDo);
        fire();
    }

    // ---------------- Thông báo thay đổi để các màn hình tự làm mới ----------------
    public static synchronized void addListener(Runnable r) { LISTENERS.add(r); }

    private static void fire() {
        for (Runnable r : new ArrayList<>(LISTENERS)) r.run();
    }
}
