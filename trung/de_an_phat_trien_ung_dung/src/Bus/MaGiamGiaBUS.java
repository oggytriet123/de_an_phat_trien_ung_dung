package Bus;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import Entity.MaGiamGia;

/** Kiểm tra mã giảm giá (dữ liệu mẫu trong bộ nhớ). */
public class MaGiamGiaBUS {
    private final Map<String, MaGiamGia> ds = new LinkedHashMap<>();

    public MaGiamGiaBUS() {
        them(new MaGiamGia("GIAM10", true, 10, LocalDate.of(2027, 12, 31)));
        them(new MaGiamGia("PET50K", false, 50_000, LocalDate.of(2027, 12, 31)));
        them(new MaGiamGia("TET2026", true, 15, LocalDate.of(2026, 2, 28))); // đã hết hạn
    }

    private void them(MaGiamGia m) {
        ds.put(m.getMa(), m);
    }

    /**
     * @return mã hợp lệ còn hạn.
     * @throws IllegalArgumentException với nội dung lỗi hiển thị cho nhân viên.
     */
    public MaGiamGia kiemTra(String nhap) {
        String ma = nhap == null ? "" : nhap.trim().toUpperCase();
        if (ma.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập mã giảm giá.");
        MaGiamGia m = ds.get(ma);
        if (m == null) throw new IllegalArgumentException("Mã giảm giá không hợp lệ.");
        if (m.getHanDung().isBefore(LocalDate.now())) throw new IllegalArgumentException("Mã giảm giá đã hết hạn.");
        return m;
    }
}
