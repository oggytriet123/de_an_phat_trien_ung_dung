package GUI;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Entity.NhanVien;

/**
 * Dữ liệu xếp ca làm và trạng thái ca trực của nhân viên.
 * Lưu trong bộ nhớ (mẫu) để chạy thử giao diện; khi nối CSDL chỉ cần thay các hàm trong lớp này.
 * Mỗi nhân viên có lịch tuần cố định: 7 ngày (T2..CN) x 3 ca (Sáng / Chiều / Tối).
 */
public final class CaLamData {
    private CaLamData() {}

    public static final String[] TEN_CA = {"Ca sáng", "Ca chiều", "Ca tối"};
    public static final LocalTime[] GIO_BD = {LocalTime.of(7, 0), LocalTime.of(12, 0), LocalTime.of(17, 0)};
    public static final LocalTime[] GIO_KT = {LocalTime.of(12, 0), LocalTime.of(17, 0), LocalTime.of(22, 0)};
    public static final String[] TEN_THU = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    public static final String DANG_TRUC = "Đang trực";
    public static final String SAP_VAO_CA = "Sắp vào ca";
    public static final String HET_CA = "Đã hết ca";
    public static final String NGHI_HOM_NAY = "Nghỉ hôm nay";
    public static final String NGHI_PHEP = "Nghỉ phép";
    public static final String VANG_MAT = "Vắng mặt";
    public static final String CHUA_XEP = "Chưa xếp ca";
    public static final String TAM_NGHI = "Tạm nghỉ";
    public static final String DA_NGHI_VIEC = "Đã nghỉ việc";

    private static final Map<String, boolean[][]> LICH = new HashMap<>();
    /** Ghi chú hôm nay (nghỉ phép / vắng mặt): maNV -> [ngày, trạng thái]. */
    private static final Map<String, Object[]> NGOAI_LE = new HashMap<>();
    private static final List<Runnable> LISTENERS = new ArrayList<>();

    /** Lịch tuần của nhân viên (nếu chưa xếp thì suy ra từ cột "Ca làm việc" trong hồ sơ). */
    public static synchronized boolean[][] layLich(NhanVien nv) {
        boolean[][] l = LICH.get(nv.getMaNV());
        if (l == null) {
            l = macDinh(nv.getCaLamViec());
            LICH.put(nv.getMaNV(), l);
        }
        return l;
    }

    private static boolean[][] macDinh(String ca) {
        boolean[][] l = new boolean[7][3];
        for (int d = 0; d < 6; d++) {          // T2..T7
            if ("Ca sáng".equals(ca)) l[d][0] = true;
            else if ("Ca chiều".equals(ca)) l[d][1] = true;
            else if ("Ca tối".equals(ca)) l[d][2] = true;
            else { l[d][0] = true; l[d][1] = true; }   // Toàn thời gian
        }
        return l;
    }

    public static synchronized void datCa(NhanVien nv, int thu, int ca, boolean co) {
        layLich(nv)[thu][ca] = co;
        fire();
    }

    public static synchronized void datTatCa(NhanVien nv, boolean[][] mau) {
        boolean[][] l = layLich(nv);
        for (int d = 0; d < 7; d++) for (int c = 0; c < 3; c++) l[d][c] = mau[d][c];
        fire();
    }

    public static synchronized int tongCaTuan(NhanVien nv) {
        int n = 0;
        for (boolean[] d : layLich(nv)) for (boolean c : d) if (c) n++;
        return n;
    }

    /** Các ca của nhân viên trong ngày (chỉ số 0..2). */
    public static synchronized List<Integer> caTrongNgay(NhanVien nv, LocalDate ngay) {
        List<Integer> r = new ArrayList<>();
        boolean[] d = layLich(nv)[ngay.getDayOfWeek().getValue() - 1];
        for (int c = 0; c < 3; c++) if (d[c]) r.add(c);
        return r;
    }

    /** Mô tả ca hôm nay, ví dụ "Sáng + Chiều" hoặc "—". */
    public static String moTaCaHomNay(NhanVien nv) {
        List<Integer> cs = caTrongNgay(nv, LocalDate.now());
        if (cs.isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        for (int c : cs) {
            if (sb.length() > 0) sb.append(" + ");
            sb.append(Character.toUpperCase(TEN_CA[c].charAt(3))).append(TEN_CA[c].substring(4));
        }
        return sb.toString();
    }

    /** Khung giờ hôm nay, ví dụ "07:00–17:00". */
    public static String khungGioHomNay(NhanVien nv) {
        List<Integer> cs = caTrongNgay(nv, LocalDate.now());
        if (cs.isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        int bd = cs.get(0), kt = cs.get(0);
        for (int i = 1; i <= cs.size(); i++) {
            if (i < cs.size() && cs.get(i) == kt + 1) { kt = cs.get(i); continue; }
            if (sb.length() > 0) sb.append(", ");
            sb.append(GIO_BD[bd].format(LichHenData.TIME_FMT)).append("–").append(GIO_KT[kt].format(LichHenData.TIME_FMT));
            if (i < cs.size()) { bd = cs.get(i); kt = bd; }
        }
        return sb.toString();
    }

    // ---------------- Trạng thái ca trực ----------------
    public static String trangThaiCaTruc(NhanVien nv) {
        return trangThaiCaTruc(nv, LocalDate.now(), LocalTime.now());
    }

    public static synchronized String trangThaiCaTruc(NhanVien nv, LocalDate ngay, LocalTime gio) {
        if ("Đã nghỉ việc".equals(nv.getTrangThai())) return DA_NGHI_VIEC;
        if ("Tạm nghỉ".equals(nv.getTrangThai())) return TAM_NGHI;
        Object[] nl = NGOAI_LE.get(nv.getMaNV());
        if (nl != null && ngay.equals(nl[0])) return (String) nl[1];
        if (tongCaTuan(nv) == 0) return CHUA_XEP;
        List<Integer> cs = caTrongNgay(nv, ngay);
        if (cs.isEmpty()) return NGHI_HOM_NAY;
        boolean sau = false;
        for (int c : cs) {
            if (!gio.isBefore(GIO_BD[c]) && gio.isBefore(GIO_KT[c])) return DANG_TRUC;
            if (gio.isBefore(GIO_BD[c])) sau = true;
        }
        return sau ? SAP_VAO_CA : HET_CA;
    }

    /** Đánh dấu nghỉ phép / vắng mặt hôm nay; truyền null để bỏ đánh dấu. */
    public static synchronized void danhDauHomNay(NhanVien nv, String trangThai) {
        if (trangThai == null) NGOAI_LE.remove(nv.getMaNV());
        else NGOAI_LE.put(nv.getMaNV(), new Object[] {LocalDate.now(), trangThai});
        fire();
    }

    public static synchronized boolean coDanhDau(NhanVien nv) {
        Object[] nl = NGOAI_LE.get(nv.getMaNV());
        return nl != null && LocalDate.now().equals(nl[0]);
    }

    public static boolean laThu(LocalDate d, DayOfWeek w) { return d.getDayOfWeek() == w; }

    // ---------------- Thông báo thay đổi ----------------
    public static synchronized void addListener(Runnable r) { LISTENERS.add(r); }

    private static void fire() {
        for (Runnable r : new ArrayList<>(LISTENERS)) r.run();
    }
}
