package Bus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Entity.HoSoThuCung;

/**
 * Nghiệp vụ hồ sơ thú cưng. Hiện dùng dữ liệu mẫu trong bộ nhớ;
 * khi có HoSoThuCungDAO chỉ cần thay phần khởi tạo / them() / capNhat() bằng lệnh gọi DAO.
 */
public class HoSoThuCungBUS {
    public static final String[] LOAI = {"Chó", "Mèo", "Khác"};
    public static final String[] SUC_KHOE = {"Tốt", "Cần theo dõi", "Dị ứng thuốc", "Đang điều trị"};

    private final List<HoSoThuCung> ds = new ArrayList<>();

    public HoSoThuCungBUS() {
        napDuLieuMau();
    }

    /** Danh sách đã sắp xếp: cập nhật gần nhất lên đầu. */
    public List<HoSoThuCung> layDanhSach() {
        List<HoSoThuCung> r = new ArrayList<>(ds);
        r.sort(Comparator.comparing(HoSoThuCung::getCapNhat).reversed()
                .thenComparing(HoSoThuCung::getMaHoSo));
        return r;
    }

    /** Tìm theo tên thú cưng, mã hồ sơ, tên chủ hoặc số điện thoại chủ. Từ khóa rỗng = tất cả. */
    public List<HoSoThuCung> tim(String tuKhoa) {
        String kw = ChuoiUtil.chuanHoa(tuKhoa == null ? "" : tuKhoa.trim());
        // chỉ so khớp số điện thoại khi từ khóa toàn là chữ số (tránh "hs003" khớp nhầm SĐT chứa 003)
        String kwSo = kw.replaceAll("[\\s.+-]", "").matches("\\d+") ? ChuoiUtil.chiSo(kw) : "";
        List<HoSoThuCung> kq = new ArrayList<>();
        for (HoSoThuCung h : layDanhSach()) {
            if (kw.isEmpty()) { kq.add(h); continue; }
            String hay = ChuoiUtil.chuanHoa(h.getTenThuCung()) + "|" + ChuoiUtil.chuanHoa(h.getMaHoSo())
                    + "|" + ChuoiUtil.chuanHoa(h.getChuNuoi());
            if (hay.contains(kw) || (!kwSo.isEmpty() && ChuoiUtil.chiSo(h.getSdtChu()).contains(kwSo))) kq.add(h);
        }
        return kq;
    }

    public HoSoThuCung them(String ten, String loai, String giong, String chuNuoi, String sdt, String sucKhoe) {
        int max = 0;
        for (HoSoThuCung h : ds) max = Math.max(max, Integer.parseInt(h.getMaHoSo().substring(2)));
        HoSoThuCung h = new HoSoThuCung(String.format("HS%03d", max + 1), ten, loai, giong, chuNuoi,
                dinhDangSdt(sdt), sucKhoe, LocalDate.now());
        ds.add(h);
        return h;
    }

    public void capNhat(HoSoThuCung h, String ten, String loai, String giong, String chuNuoi, String sdt, String sucKhoe) {
        h.setTenThuCung(ten);
        h.setLoai(loai);
        h.setGiong(giong);
        h.setChuNuoi(chuNuoi);
        h.setSdtChu(dinhDangSdt(sdt));
        h.setSucKhoe(sucKhoe);
        h.setCapNhat(LocalDate.now());
    }

    public static String dinhDangSdt(String s) {
        String d = ChuoiUtil.chiSo(s);
        return d.length() == 10 ? d.substring(0, 4) + " " + d.substring(4, 7) + " " + d.substring(7) : d;
    }

    private void add(String ten, String loai, String giong, String chu, String sdt, String sk, int d, int m, int y) {
        ds.add(new HoSoThuCung(String.format("HS%03d", ds.size() + 1), ten, loai, giong, chu, sdt, sk,
                LocalDate.of(y, m, d)));
    }

    private void napDuLieuMau() {
        // 4 hồ sơ đầu giống bản thiết kế
        add("Milo", "Chó", "Poodle", "Nguyễn Văn A", "0903 123 456", "Tốt", 6, 10, 2026);
        add("Mimi", "Mèo", "Anh lông ngắn", "Trần Thị B", "0912 456 789", "Cần theo dõi", 5, 10, 2026);
        add("Bông", "Chó", "Pug", "Lê Văn C", "0938 222 111", "Dị ứng thuốc", 3, 10, 2026);
        add("Lucky", "Mèo", "Ba Tư", "Phạm Thị D", "0977 654 321", "Tốt", 1, 10, 2026);

        String[] ten = {"Mochi", "Kem", "Đậu", "Lu", "Mít", "Nấm", "Bơ", "Xoài", "Gấu", "Sushi",
                "Susu", "Cốm", "Bin", "Mun", "Tép", "Rex", "Nâu", "Béo", "Cún", "Mướp"};
        String[][] giongCho = {{"Poodle"}, {"Corgi"}, {"Husky"}, {"Golden Retriever"}, {"Phốc sóc"}, {"Chihuahua"}};
        String[] giongMeo = {"Anh lông ngắn", "Ba Tư", "Munchkin", "Mướp ta", "Scottish Fold"};
        String[] ho = {"Võ Hoàng", "Đặng Thu", "Bùi Anh", "Hoàng Thị", "Ngô Đức", "Đỗ Khánh", "Vũ Minh", "Lý Thanh",
                "Trương Quốc", "Phan Bảo", "Dương Gia", "Mai Thị"};
        String[] tenChu = {"Long", "Hà", "Khoa", "Mai", "Thắng", "Linh", "Quân", "Trúc", "Huy", "Ngọc", "Bảo", "Hồng"};
        LocalDate d = LocalDate.of(2026, 9, 30);
        for (int i = 0; i < 36; i++) {
            boolean cho = i % 3 != 1;
            String giong = cho ? giongCho[i % giongCho.length][0] : giongMeo[i % giongMeo.length];
            String sk = switch (i % 9) {
                case 3 -> "Cần theo dõi";
                case 6 -> "Đang điều trị";
                case 8 -> "Dị ứng thuốc";
                default -> "Tốt";
            };
            String sdt = String.format("09%02d %03d %03d", 10 + i, 100 + i * 7, 200 + i * 11);
            LocalDate ngay = d.minusDays(i * 2L);
            add(ten[i % ten.length], cho ? "Chó" : "Mèo", giong, ho[i % ho.length] + " " + tenChu[(i * 5) % tenChu.length],
                    sdt, sk, ngay.getDayOfMonth(), ngay.getMonthValue(), ngay.getYear());
        }
    }
}
