package Bus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.KhachHang;

/**
 * Nghiệp vụ khách hàng. Hiện dùng dữ liệu mẫu trong bộ nhớ;
 * khi có KhachHangDAO chỉ cần thay phần khởi tạo / them() bằng lệnh gọi DAO.
 */
public class KhachHangBUS {
    private final List<KhachHang> ds = new ArrayList<>();

    public KhachHangBUS() {
        napDuLieuMau();
    }

    public List<KhachHang> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public boolean tonTaiSoDienThoai(String sdt) {
        String s = chiSo(sdt);
        for (KhachHang k : ds) if (chiSo(k.getSoDienThoai()).equals(s)) return true;
        return false;
    }

    public KhachHang them(String hoTen, String sdt, String hangThe, String thuCung) {
        int max = 0;
        for (KhachHang k : ds) max = Math.max(max, Integer.parseInt(k.getMaKH().substring(2)));
        KhachHang kh = new KhachHang(String.format("KH%03d", max + 1), hoTen, dinhDangSdt(sdt), hangThe,
                0, thuCung.isEmpty() ? "—" : thuCung, null, 0);
        ds.add(kh);
        return kh;
    }

    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    public static String dinhDangSdt(String s) {
        String d = chiSo(s);
        return d.length() == 10 ? d.substring(0, 4) + " " + d.substring(4, 7) + " " + d.substring(7) : d;
    }

    private void add(String ten, String sdt, String hang, int diem, String pet, int d, int m, int y, long chi) {
        ds.add(new KhachHang(String.format("KH%03d", ds.size() + 1), ten, sdt, hang, diem, pet,
                LocalDate.of(y, m, d), chi));
    }

    private void napDuLieuMau() {
        add("Nguyễn Minh Anh", "0903 123 456", "VIP", 2480, "Mochi, Bông", 3, 10, 2026, 18_450_000);
        add("Trần Quốc Bảo", "0912 456 789", "Vàng", 1320, "Lu", 1, 10, 2026, 9_820_000);
        add("Lê Thị Cẩm Tú", "0938 222 111", "Vàng", 1105, "Kem, Đậu", 28, 9, 2026, 8_140_000);
        add("Phạm Gia Hân", "0977 654 321", "Bạc", 640, "Milo", 25, 9, 2026, 4_560_000);
        add("Võ Hoàng Long", "0386 909 808", "Bạc", 520, "Rex", 22, 9, 2026, 3_790_000);
        add("Đặng Thu Hà", "0905 777 333", "Thường", 210, "Nâu", 15, 9, 2026, 1_650_000);
        add("Bùi Anh Khoa", "0919 888 000", "Thường", 95, "Tép", 9, 9, 2026, 720_000);
        add("Hoàng Thị Mai", "0932 111 222", "Thường", 88, "Mun", 5, 9, 2026, 610_000);
        add("Ngô Đức Thắng", "0944 333 555", "Thường", 80, "Bơ", 30, 8, 2026, 540_000);
        add("Đỗ Khánh Linh", "0966 222 888", "Thường", 72, "Susu", 27, 8, 2026, 480_000);
        add("Vũ Minh Quân", "0987 123 321", "Thường", 65, "Cốm", 21, 8, 2026, 410_000);
        add("Lý Thanh Trúc", "0356 789 123", "Thường", 58, "Bin", 18, 8, 2026, 360_000);
        add("Trương Quốc Huy", "0908 456 654", "Thường", 50, "Lu Lu", 12, 8, 2026, 310_000);
        add("Phan Bảo Ngọc", "0913 999 111", "Thường", 44, "Xoài", 7, 8, 2026, 270_000);
        add("Dương Gia Bảo", "0979 345 678", "Thường", 38, "Gấu", 2, 8, 2026, 230_000);
        add("Mai Thị Hồng", "0935 678 901", "Thường", 30, "Nấm", 25, 7, 2026, 190_000);
        add("Cao Văn Tài", "0369 258 147", "Thường", 24, "Sushi", 19, 7, 2026, 150_000);
        add("Tô Ngọc Diệp", "0909 147 258", "Thường", 18, "Béo", 11, 7, 2026, 120_000);
        add("Lâm Hoàng Nam", "0918 753 951", "Thường", 12, "Cún", 3, 7, 2026, 90_000);
        add("Hồ Thị Thanh", "0971 852 963", "Thường", 6, "Mướp", 26, 6, 2026, 50_000);
        add("Đinh Quang Vinh", "0383 147 369", "Thường", 0, "—", 15, 6, 2026, 0);
    }
}
