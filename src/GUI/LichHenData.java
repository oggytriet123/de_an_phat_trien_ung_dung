package GUI;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import DAO.LichHenDAO;

/**
 * Dữ liệu lịch hẹn dùng chung cho các màn hình của mục "Quản Lý Lịch Hẹn".
 * Đồng bộ trực tiếp với CSDL SQL Server (bảng LICHHEN).
 */
public final class LichHenData {
    private LichHenData() {}

    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DA_DOI = "Đã đổi lịch";
    public static final String HOAN_TAT = "Hoàn thành";
    public static final String DA_HUY = "Đã hủy";

    public static final String[] DICH_VU = {
        "Khám tổng quát", "Tiêm phòng vaccine", "Spa & Grooming", "Xét nghiệm máu tổng quát", "Phẫu thuật lấy sỏi bàng quang", "Lưu trú thú cưng (theo dõi)"
    };
    public static final String[] BAC_SI = {
        "BS. Nguyễn Hoài Anh", "BS. Võ Thanh Hùng", "BS. Ngô Gia Bảo"
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
        public boolean conHieuLuc() { 
            return !HOAN_TAT.equalsIgnoreCase(trangThai) 
                && !"Hoàn tất".equalsIgnoreCase(trangThai) 
                && !DA_HUY.equalsIgnoreCase(trangThai); 
        }
        public void ghiLog(String noiDung) { lichSu.add(LocalDateTime.now().format(LOG_FMT) + " - " + noiDung); }
    }

    private static final List<LichHen> DS = new ArrayList<>();
    private static final List<Runnable> LISTENERS = new ArrayList<>();
    private static final LichHenDAO dao = new LichHenDAO();

    static {
        taiTuCSDL();
    }

    public static synchronized void taiTuCSDL() {
        DS.clear();
        DS.addAll(dao.layTatCa());
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
        return dao.maTiepTheo();
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
        if (l.ma == null || l.ma.trim().isEmpty()) {
            l.ma = nextMa();
        }
        l.ghiLog("Tạo lịch hẹn");
        DS.add(l);
        try {
            dao.them(l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fire();
    }

    public static synchronized void capNhat(LichHen l) {
        l.ghiLog("Cập nhật thông tin lịch hẹn");
        try {
            dao.capNhat(l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fire();
    }

    public static synchronized void xoa(LichHen l) {
        DS.remove(l);
        try {
            dao.xoa(l.ma);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fire();
    }

    public static synchronized void xacNhan(LichHen l) {
        l.trangThai = DA_XAC_NHAN;
        l.ghiLog("Xác nhận lịch hẹn");
        try {
            dao.capNhat(l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fire();
    }

    public static synchronized void doiLich(LichHen l, LocalDate ngayMoi, LocalTime gioMoi, String lyDo) {
        String cu = l.gio.format(TIME_FMT) + " " + l.ngay.format(CalendarPicker.DATE_FMT);
        l.ngay = ngayMoi;
        l.gio = gioMoi;
        l.trangThai = DA_DOI;
        l.ghiLog("Đổi lịch hẹn: " + cu + " -> " + gioMoi.format(TIME_FMT) + " " + ngayMoi.format(CalendarPicker.DATE_FMT)
                + (lyDo.isEmpty() ? "" : ". Lý do: " + lyDo));
        try {
            dao.capNhat(l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fire();
    }

    public static synchronized void huy(LichHen l, String lyDo) {
        l.trangThai = DA_HUY;
        l.ghiLog("Hủy lịch hẹn. Lý do: " + lyDo);
        try {
            dao.capNhat(l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fire();
    }

    public static synchronized void addListener(Runnable r) { LISTENERS.add(r); }

    private static void fire() {
        for (Runnable r : new ArrayList<>(LISTENERS)) r.run();
    }
}
