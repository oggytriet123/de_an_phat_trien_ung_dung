package Bus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    public List<MaGiamGia> layDanhSach() {
        return new ArrayList<>(ds.values());
    }

    /**
     * Xóa một mã giảm giá khỏi danh sách. Hóa đơn đã áp dụng mã này vẫn giữ mức giảm đã áp dụng.
     * @throws IllegalArgumentException nếu không có mã đó.
     */
    public void xoa(String ma) {
        if (ma == null || ds.remove(ma.trim().toUpperCase()) == null) {
            throw new IllegalArgumentException("Không tìm thấy mã giảm giá " + ma + ".");
        }
    }

    public boolean daHetHan(MaGiamGia m) {
        return m.getHanDung().isBefore(LocalDate.now());
    }

    /**
     * Tạo mã giảm giá mới (xuất hiện ngay trong danh sách mã có sẵn).
     * @throws IllegalArgumentException với nội dung lỗi hiển thị cho nhân viên.
     */
    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate hanDung) {
        String ma = maNhap == null ? "" : maNhap.trim().toUpperCase();
        if (ma.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập mã giảm giá.");
        if (!ma.matches("[A-Z0-9_-]{3,20}")) {
            throw new IllegalArgumentException("Mã gồm 3–20 ký tự: chữ, số, gạch ngang hoặc gạch dưới (không dấu, không khoảng trắng).");
        }
        if (ds.containsKey(ma)) throw new IllegalArgumentException("Mã giảm giá " + ma + " đã tồn tại.");
        if (phanTram) {
            if (giaTri < 1 || giaTri > 100) throw new IllegalArgumentException("Phần trăm giảm phải từ 1 đến 100.");
        } else if (giaTri < 1_000 || giaTri > 100_000_000) {
            throw new IllegalArgumentException("Số tiền giảm phải từ 1.000₫ đến 100.000.000₫.");
        }
        if (hanDung == null) throw new IllegalArgumentException("Vui lòng nhập hạn dùng (dd/MM/yyyy).");
        if (hanDung.isBefore(LocalDate.now())) throw new IllegalArgumentException("Hạn dùng không được ở quá khứ.");
        MaGiamGia m = new MaGiamGia(ma, phanTram, giaTri, hanDung);
        them(m);
        return m;
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
