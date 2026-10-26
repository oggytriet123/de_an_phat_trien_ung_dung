package Bus;

import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.KhachHangDAO;
import Entity.KhachHang;
import Entity.ThongKeKhachHang;
import Entity.ThuCung;

/** Nghiệp vụ khách hàng, dữ liệu lấy từ SQL Server qua KhachHangDAO. */
public class KhachHangBUS {
    /** Mã lỗi SQL Server khi vi phạm UNIQUE (2601: unique index, 2627: unique constraint). */
    public static boolean laTrungKhoa(SQLException e) {
        return e.getErrorCode() == 2601 || e.getErrorCode() == 2627;
    }

    private final KhachHangDAO dao = new KhachHangDAO();
    private List<KhachHang> ds = new ArrayList<>();

    public KhachHangBUS() {
        napDuLieuMau();
    }

    /** Đọc lại danh sách từ CSDL. Nếu lỗi thì giữ nguyên danh sách đang có. */
    public void taiLai() throws SQLException {
        List<KhachHang> moi = dao.layTatCa();
        for (KhachHang k : moi) k.setSoDienThoai(dinhDangSdt(k.getSoDienThoai()));
        ds = moi;
    }

    public List<KhachHang> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public ThongKeKhachHang layThongKe() throws SQLException {
        return dao.layThongKe();
    }

    public boolean tonTaiSoDienThoai(String sdt) throws SQLException {
        return dao.tonTaiSoDienThoai(chiSo(sdt));
    }

    public boolean tonTaiSoDienThoai(String sdt, String boQuaMaKH) throws SQLException {
        return dao.tonTaiSoDienThoai(chiSo(sdt), boQuaMaKH);
    }

    /**
     * Tìm khách theo SĐT — thử CSDL trước, nếu lỗi kết nối thì tìm trong bộ nhớ.
     * Trả về null nếu chưa có.
     */
    public KhachHang timTheoSdt(String sdt) throws SQLException {
        String so = chiSo(sdt);
        if (so.length() < 9) return null;
        try {
            KhachHang k = dao.timTheoSdt(so);
            if (k != null) {
                k.setSoDienThoai(dinhDangSdt(k.getSoDienThoai()));
                return k;
            }
        } catch (SQLException e) {
            // fallback: nếu lỗi CSDL thì tìm trong danh sách bộ nhớ
            for (KhachHang k : ds) {
                if (chiSo(k.getSoDienThoai()).equals(so)) return k;
            }
            throw e;
        }
        for (KhachHang k : ds) {
            if (chiSo(k.getSoDienThoai()).equals(so)) return k;
        }
        return null;
    }

    /** Bản an toàn không ném SQLException (bắt exception và fallback bộ nhớ), dùng cho hóa đơn. */
    public KhachHang timTheoSdtAnToan(String sdt) {
        try {
            return timTheoSdt(sdt);
        } catch (Exception e) {
            String so = chiSo(sdt);
            for (KhachHang k : ds) {
                if (chiSo(k.getSoDienThoai()).equals(so)) return k;
            }
            return null;
        }
    }

    /** Tìm khách theo mã KH trong bộ nhớ. */
    public KhachHang timTheoMa(String maKH) {
        if (maKH == null) return null;
        for (KhachHang k : ds) {
            if (k.getMaKH() != null && k.getMaKH().equalsIgnoreCase(maKH.trim())) return k;
        }
        return null;
    }

    /** Thêm khách hàng cùng thú cưng vào CSDL; hạng thẻ tự tính theo điểm. @return mã KH mới */
    public String them(String hoTen, String sdt, int diem, List<ThuCung> thuCung) throws SQLException {
        return dao.them(hoTen, chiSo(sdt), diem, thuCung == null ? new ArrayList<>() : thuCung);
    }

    /** Thêm khách hàng mới vào bộ nhớ (dùng khi lập hóa đơn không qua CSDL). */
    public KhachHang them(String hoTen, String sdt, String hangThe, String thuCung) {
        int max = 0;
        for (KhachHang k : ds) {
            try {
                if (k.getMaKH() != null && k.getMaKH().startsWith("KH"))
                    max = Math.max(max, Integer.parseInt(k.getMaKH().substring(2)));
            } catch (Exception ignored) {}
        }
        KhachHang kh = new KhachHang(String.format("KH%03d", max + 1), hoTen,
                dinhDangSdt(sdt), hangThe == null ? "Thường" : hangThe,
                0, (thuCung == null || thuCung.isEmpty()) ? "—" : thuCung, null, 0);
        ds.add(kh);
        return kh;
    }

    /** Cộng số tiền vừa thanh toán vào tổng chi tiêu và cập nhật ngày giao dịch gần nhất. */
    public void ghiNhanThanhToan(String maKH, long soTien) {
        KhachHang k = timTheoMa(maKH);
        if (k == null) return;
        k.setTongChiTieu(k.getTongChiTieu() + soTien);
        k.setGiaoDichGanNhat(LocalDate.now());
    }

    /** Cập nhật thông tin khách hàng (họ tên, SĐT, điểm). */
    public boolean capNhat(String maKH, String hoTen, String sdt, int diem) throws SQLException {
        if (maKH == null || maKH.trim().isEmpty())
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        if (hoTen == null || hoTen.trim().isEmpty())
            throw new IllegalArgumentException("Họ và tên không được để trống.");
        String so = chiSo(sdt);
        if (so.length() < 9 || so.length() > 11)
            throw new IllegalArgumentException("Số điện thoại phải gồm 9–11 chữ số.");
        if (dao.tonTaiSoDienThoai(so, maKH))
            throw new IllegalArgumentException("Số điện thoại này đã được dùng bởi khách hàng khác.");
        boolean ok = dao.capNhat(maKH, hoTen.trim(), so, Math.max(0, diem));
        if (ok) taiLai();
        return ok;
    }

    /** Xóa khách hàng có kiểm tra ràng buộc. */
    public boolean xoa(String maKH) throws SQLException {
        if (maKH == null || maKH.trim().isEmpty())
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        boolean ok = dao.xoa(maKH);
        if (ok) taiLai();
        return ok;
    }

    /** Hạng thẻ theo điểm: Thường (<100) · Bạc (100) · Vàng (200) · VIP (500). */
    public static String tinhHang(int diem) {
        return KhachHangDAO.tinhHang(diem);
    }

    /** Số điểm nhận được khi thanh toán số tiền này (1 điểm / 10.000₫). */
    public static long diemTuSoTien(long soTien) {
        return Math.max(0, soTien / KhachHangDAO.SO_TIEN_MOI_DIEM);
    }

    // ------------------------------------------------------------ quy tắc dữ liệu khách hàng
    /** Gọn khoảng trắng, chuẩn Unicode (NFC) và viết hoa chữ cái đầu mỗi từ. */
    public static String chuanHoaTen(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFC).trim().replaceAll("\\s+", " ");
        StringBuilder sb = new StringBuilder();
        boolean dauTu = true;
        for (char c : t.toCharArray()) {
            sb.append(dauTu && Character.isLetter(c) ? Character.toUpperCase(c) : c);
            dauTu = c == ' ' || c == '-';
        }
        return sb.toString();
    }

    /** @return thông báo lỗi hoặc null nếu tên hợp lệ. */
    public static String loiTen(String ten) {
        String t = chuanHoaTen(ten);
        if (t.isEmpty()) return "Vui lòng nhập tên khách hàng.";
        if (t.length() < 2) return "Tên khách hàng phải có ít nhất 2 ký tự.";
        if (t.length() > 50) return "Tên khách hàng tối đa 50 ký tự.";
        if (!t.matches("\\p{L}[\\p{L}\\p{M} .''-]*"))
            return "Tên khách hàng chỉ được chứa chữ cái, khoảng trắng và các dấu . ' -";
        return null;
    }

    /** Chỉ giữ chữ số; đầu số quốc tế 84 được đổi về 0 (+84 903... thành 0903...). */
    public static String chuanHoaSdt(String s) {
        String d = chiSo(s);
        if (d.startsWith("84") && (d.length() == 11 || d.length() == 12)) d = "0" + d.substring(2);
        return d;
    }

    /** Di động Việt Nam 10 số (03, 05, 07, 08, 09) hoặc điện thoại bàn 11 số (02x). */
    public static String loiSdt(String sdt) {
        String d = chuanHoaSdt(sdt);
        if (d.isEmpty()) return "Vui lòng nhập số điện thoại.";
        if (d.matches("0[35789]\\d{8}") || d.matches("02\\d{9}")) return null;
        return "Số điện thoại không hợp lệ. Ví dụ: 0903 123 456.";
    }

    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    public static String dinhDangSdt(String s) {
        String d = chiSo(s);
        return d.length() == 10 ? d.substring(0, 4) + " " + d.substring(4, 7) + " " + d.substring(7) : d;
    }

    // ---- dữ liệu mẫu (dùng khi không kết nối CSDL)
    private void add(String ten, String sdt, String hang, int diem, String pet, int d, int m, int y, long chi) {
        ds.add(new KhachHang(String.format("KH%03d", ds.size() + 1), ten, sdt, hang, diem, pet,
                LocalDate.of(y, m, d), chi));
    }

    private void napDuLieuMau() {
        add("Nguyễn Minh Anh",  "0903 123 456", "VIP",    2480, "Mochi, Bông", 3,  10, 2026, 18_450_000);
        add("Trần Quốc Bảo",    "0912 456 789", "Vàng",   1320, "Lu",          1,  10, 2026,  9_820_000);
        add("Lê Thị Cẩm Tú",   "0938 222 111", "Vàng",   1105, "Kem, Đậu",   28,   9, 2026,  8_140_000);
        add("Phạm Gia Hân",     "0977 654 321", "Bạc",     640, "Milo",        25,   9, 2026,  4_560_000);
        add("Võ Hoàng Long",    "0386 909 808", "Bạc",     520, "Rex",         22,   9, 2026,  3_790_000);
        add("Đặng Thu Hà",      "0905 777 333", "Thường",  210, "Nâu",         15,   9, 2026,  1_650_000);
        add("Bùi Anh Khoa",     "0919 888 000", "Thường",   95, "Tép",          9,   9, 2026,    720_000);
        add("Hoàng Thị Mai",    "0932 111 222", "Thường",   88, "Mun",          5,   9, 2026,    610_000);
        add("Ngô Đức Thắng",   "0944 333 555", "Thường",   80, "Bơ",          30,   8, 2026,    540_000);
        add("Đỗ Khánh Linh",   "0966 222 888", "Thường",   72, "Susu",        27,   8, 2026,    480_000);
        add("Vũ Minh Quân",    "0987 123 321", "Thường",   65, "Cốm",         21,   8, 2026,    410_000);
        add("Lý Thanh Trúc",   "0356 789 123", "Thường",   58, "Bin",         18,   8, 2026,    360_000);
        add("Trương Quốc Huy", "0908 456 654", "Thường",   50, "Lu Lu",       12,   8, 2026,    310_000);
        add("Phan Bảo Ngọc",   "0913 999 111", "Thường",   44, "Xoài",         7,   8, 2026,    270_000);
        add("Dương Gia Bảo",   "0979 345 678", "Thường",   38, "Gấu",          2,   8, 2026,    230_000);
        add("Mai Thị Hồng",    "0935 678 901", "Thường",   30, "Nấm",         25,   7, 2026,    190_000);
        add("Cao Văn Tài",     "0369 258 147", "Thường",   24, "Sushi",       19,   7, 2026,    150_000);
        add("Tô Ngọc Diệp",   "0909 147 258", "Thường",   18, "Béo",         11,   7, 2026,    120_000);
        add("Lâm Hoàng Nam",  "0918 753 951", "Thường",   12, "Cún",           3,   7, 2026,     90_000);
        add("Hồ Thị Thanh",   "0971 852 963", "Thường",    6, "Mướp",         26,   6, 2026,     50_000);
        add("Đinh Quang Vinh","0383 147 369", "Thường",    0, "—",            15,   6, 2026,          0);
    }
}
