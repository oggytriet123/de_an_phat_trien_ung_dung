package Bus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.ThuCung;

/**
 * Nghiệp vụ quản lý thú cưng. Dùng dữ liệu mẫu trong bộ nhớ (450 thú cưng);
 * khi có ThuCungDAO chỉ cần thay phần khởi tạo / thao tác bằng DAO.
 */
public class ThuCungBUS {
    private final List<ThuCung> ds = new ArrayList<>();

    public ThuCungBUS() {
        napDuLieuMau();
    }

    public List<ThuCung> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public int getTongSoThuCung() {
        return ds.size();
    }

    public int getSoDangLuuTru() {
        int cnt = 0;
        for (ThuCung tc : ds) if (tc.isDangLuuTru()) cnt++;
        return cnt;
    }

    public int getSoCanTheoDoi() {
        int cnt = 0;
        for (ThuCung tc : ds) {
            if ("Cần theo dõi".equals(tc.getSucKhoe()) || "Đang điều trị".equals(tc.getSucKhoe())) cnt++;
        }
        return cnt;
    }

    public ThuCung them(String ten, String giong, String loai, String tenChu, String sdtChu,
                        double canNang, String sucKhoe, boolean dangLuuTru, String ghiChu) {
        int max = 0;
        for (ThuCung t : ds) {
            try {
                max = Math.max(max, Integer.parseInt(t.getMaTC().substring(2)));
            } catch (Exception ignored) {}
        }
        ThuCung tc = new ThuCung(String.format("TC%03d", max + 1), ten, giong, loai, tenChu,
                dinhDangSdt(sdtChu), canNang, sucKhoe, dangLuuTru, LocalDate.now(), ghiChu);
        ds.add(0, tc); // Thêm lên đầu danh sách
        return tc;
    }

    public boolean capNhat(ThuCung tc) {
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getMaTC().equals(tc.getMaTC())) {
                ds.set(i, tc);
                return true;
            }
        }
        return false;
    }

    public boolean xoa(String maTC) {
        return ds.removeIf(tc -> tc.getMaTC().equals(maTC));
    }

    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    public static String dinhDangSdt(String s) {
        String d = chiSo(s);
        return d.length() == 10 ? d.substring(0, 4) + " " + d.substring(4, 7) + " " + d.substring(7) : d;
    }

    private void add(String ten, String giong, String loai, String tenChu, String sdtChu,
                     double canNang, String sucKhoe, boolean dangLuuTru, String ghiChu) {
        ds.add(new ThuCung(String.format("TC%03d", ds.size() + 1), ten, giong, loai, tenChu,
                sdtChu, canNang, sucKhoe, dangLuuTru, LocalDate.now().minusDays(ds.size()), ghiChu));
    }

    private void napDuLieuMau() {
        // 7 dòng đầu tiên hiển thị trên trang 1 khớp chính xác ảnh thiết kế tham chiếu
        add("Mochi", "Poodle", "Chó", "Nguyễn Minh Anh", "0901 234 456", 4.2, "Khỏe mạnh", false, "");
        add("Bông", "Anh lông ngắn", "Mèo", "Nguyễn Minh Anh", "0901 234 456", 3.8, "Khỏe mạnh", false, "");
        add("Lu", "Golden Retriever", "Chó", "Trần Quốc Bảo", "0978 456 789", 25.5, "Cần theo dõi", false, "Theo dõi dị ứng");
        add("Kem", "Munchkin", "Mèo", "Lê Thị Cẩm Tú", "0933 222 111", 3.2, "Khỏe mạnh", false, "");
        add("Milo", "Corgi", "Chó", "Phạm Gia Hân", "0912 345 678", 10.4, "Đang điều trị", false, "Điều trị viêm da");
        add("Rex", "Husky", "Chó", "Võ Hoàng Long", "0988 000 666", 22.0, "Khỏe mạnh", false, "");
        add("Nâu", "Mèo ta", "Mèo", "Đặng Thu Hà", "0905 777 333", 4.0, "Khỏe mạnh", false, "");

        // Thêm 3 dòng cần theo dõi / điều trị để tổng số "Cần theo dõi sức khỏe" đúng bằng 5
        add("Đậu", "Phốc sóc", "Chó", "Lê Thị Cẩm Tú", "0933 222 111", 2.6, "Cần theo dõi", false, "Theo dõi tiêu hóa");
        add("Sữa", "Xiêm", "Mèo", "Hoàng Văn Tuấn", "0909 333 444", 3.5, "Đang điều trị", false, "Điều trị mắt");
        add("Bơ", "Shiba Inu", "Chó", "Ngô Đức Thắng", "0944 333 555", 9.8, "Cần theo dõi", false, "Cần kiểm tra cân nặng");

        // Các tên mẫu để sinh tổng cộng đúng 450 thú cưng (65 trang x 7)
        String[] tenCho = {"Lucky", "Bông Lan", "Kiki", "Choco", "Max", "Bắp", "Cacao", "Gấu", "Tôm", "Sam"};
        String[] giongCho = {"Poodle", "Corgi", "Pug", "Chihuahua", "Bulldog", "Samoyed", "Husky", "Golden"};
        String[] tenMeo = {"Miu", "Mimi", "Mun", "Xoài", "Bơ", "Sushi", "Simba", "Mây", "Bánh Bao", "Neko"};
        String[] giongMeo = {"Anh lông ngắn", "Anh lông dài", "Mèo Ba Tư", "Mèo ta", "Munchkin", "Scottish Fold"};
        String[] chu = {"Trần Mai Anh", "Lê Hoàng Nam", "Nguyễn Văn Đức", "Vũ Phương Linh", "Bùi Quốc Huy",
                        "Phan Ngọc Trâm", "Đỗ Hải Đăng", "Hồ Minh Châu", "Dương Tấn Phát", "Lý Gia Hân"};

        // Sinh thêm cho đủ 450 thú cưng, trong đó phân bổ 28 bé đang lưu trú
        int luuTruConLai = 28;
        while (ds.size() < 450) {
            int i = ds.size();
            boolean isDog = (i % 2 == 0);
            String t = isDog ? tenCho[i % tenCho.length] : tenMeo[i % tenMeo.length];
            String g = isDog ? giongCho[i % giongCho.length] : giongMeo[i % giongMeo.length];
            String loai = isDog ? "Chó" : "Mèo";
            String c = chu[i % chu.length];
            String sdt = String.format("09%02d %03d %03d", (i * 7) % 90 + 10, (i * 37) % 900 + 100, (i * 53) % 900 + 100);
            double w = isDog ? Math.round((3.0 + (i % 25) * 1.1) * 10.0) / 10.0 : Math.round((2.5 + (i % 6) * 0.5) * 10.0) / 10.0;
            boolean lt = false;
            if (luuTruConLai > 0 && i % 15 == 0) {
                lt = true;
                luuTruConLai--;
            }
            add(t + (i > 27 ? " #" + i : ""), g, loai, c, sdt, w, "Khỏe mạnh", lt, "");
        }
    }
}
