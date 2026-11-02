package Bus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import DAO.MaGiamGiaDAO;
import Entity.MaGiamGia;

/** Quản lý mã giảm giá, đồng bộ trực tiếp với CSDL SQL Server (bảng MAGIAMGIA). */
public class MaGiamGiaBUS {
    private final Map<String, MaGiamGia> ds = new LinkedHashMap<>();
    private final MaGiamGiaDAO dao = new MaGiamGiaDAO();

    public MaGiamGiaBUS() {
        taiTuCSDL();
    }

    public synchronized void taiTuCSDL() {
        ds.clear();
        List<MaGiamGia> list = dao.layTatCa();
        for (MaGiamGia m : list) {
            ds.put(m.getMa(), m);
        }
        if (ds.isEmpty()) {
            them(new MaGiamGia("GIAM10", true, 10, LocalDate.of(2027, 12, 31), "Giảm 10% cho khách hàng thành viên"));
            them(new MaGiamGia("PET50K", false, 50_000, LocalDate.of(2027, 12, 31), "Giảm 50.000₫ cho hóa đơn dịch vụ"));
            them(new MaGiamGia("VIP100K", false, 100_000, LocalDate.of(2027, 11, 30), "Ưu đãi khách hàng VIP thân thiết"));
            them(new MaGiamGia("PETCARE20", true, 20, LocalDate.of(2027, 12, 31), "Chương trình tri ân khách hàng Pet Care"));
            them(new MaGiamGia("TRIAN15", true, 15, LocalDate.of(2027, 6, 30), "Giảm 15% gói combo tiêm phòng và spa"));
            them(new MaGiamGia("TET2026", true, 15, LocalDate.of(2026, 2, 28), "Khuyến mãi dịp Tết Nguyên Đán (Đã hết hạn)"));
            them(new MaGiamGia("HE2025", false, 30_000, LocalDate.of(2025, 8, 31), "Khuyến mãi chào hè 2025 (Đã hết hạn)"));
        }
    }

    private void them(MaGiamGia m) {
        ds.put(m.getMa(), m);
    }

    public List<MaGiamGia> layDanhSach() {
        return new ArrayList<>(ds.values());
    }

    public MaGiamGia lay(String ma) {
        if (ma == null) return null;
        return ds.get(ma.trim().toUpperCase());
    }

    public boolean tonTai(String ma) {
        if (ma == null) return false;
        return ds.containsKey(ma.trim().toUpperCase());
    }

    /**
     * Xóa một mã giảm giá khỏi danh sách và CSDL.
     * @throws IllegalArgumentException nếu không có mã đó.
     */
    public void xoa(String ma) {
        if (ma == null || ds.remove(ma.trim().toUpperCase()) == null) {
            throw new IllegalArgumentException("Không tìm thấy mã giảm giá " + ma + ".");
        }
        try {
            dao.xoa(ma.trim().toUpperCase());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean daHetHan(MaGiamGia m) {
        return m.getHanDung().isBefore(LocalDate.now());
    }

    /**
     * Tạo mã giảm giá mới (không có ghi chú).
     */
    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate hanDung) {
        return tao(maNhap, phanTram, giaTri, hanDung, "");
    }

    /**
     * Tạo mã giảm giá mới có ghi chú/mô tả và lưu vào CSDL.
     * @throws IllegalArgumentException với nội dung lỗi hiển thị cho nhân viên.
     */
    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate hanDung, String ghiChu) {
        String ma = maNhap == null ? "" : maNhap.trim().toUpperCase();
        if (ma.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập mã giảm giá.");
        if (!ma.matches("[A-Z0-9_-]{3,20}")) {
            throw new IllegalArgumentException("Mã gồm 3–20 ký tự: chữ cái, chữ số, gạch ngang hoặc gạch dưới (không dấu, không khoảng trắng).");
        }
        if (ds.containsKey(ma)) throw new IllegalArgumentException("Mã giảm giá \"" + ma + "\" đã tồn tại trong hệ thống.");
        if (phanTram) {
            if (giaTri < 1 || giaTri > 100) throw new IllegalArgumentException("Phần trăm giảm phải từ 1% đến 100%.");
        } else if (giaTri < 1_000 || giaTri > 100_000_000) {
            throw new IllegalArgumentException("Số tiền giảm phải từ 1.000₫ đến 100.000.000₫.");
        }
        if (hanDung == null) throw new IllegalArgumentException("Vui lòng nhập hạn dùng (dd/MM/yyyy).");
        if (hanDung.isBefore(LocalDate.now())) throw new IllegalArgumentException("Hạn dùng không được ở quá khứ.");
        MaGiamGia m = new MaGiamGia(ma, phanTram, giaTri, hanDung, ghiChu);
        them(m);
        try {
            dao.them(m);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return m;
    }

    /**
     * Chỉnh sửa thông tin mã giảm giá đã có (mức giảm, hạn dùng, ghi chú).
     */
    public MaGiamGia sua(String maNhap, boolean phanTram, long giaTri, LocalDate hanDung, String ghiChu) {
        String ma = maNhap == null ? "" : maNhap.trim().toUpperCase();
        if (ma.isEmpty() || !ds.containsKey(ma)) {
            throw new IllegalArgumentException("Không tìm thấy mã giảm giá \"" + ma + "\" để cập nhật.");
        }
        if (phanTram) {
            if (giaTri < 1 || giaTri > 100) throw new IllegalArgumentException("Phần trăm giảm phải từ 1% đến 100%.");
        } else if (giaTri < 1_000 || giaTri > 100_000_000) {
            throw new IllegalArgumentException("Số tiền giảm phải từ 1.000₫ đến 100.000.000₫.");
        }
        if (hanDung == null) throw new IllegalArgumentException("Vui lòng nhập hạn dùng (dd/MM/yyyy).");
        MaGiamGia m = new MaGiamGia(ma, phanTram, giaTri, hanDung, ghiChu);
        ds.put(ma, m);
        return m;
    }

    /**
     * @return mã hợp lệ còn hạn.
     */
    public MaGiamGia kiemTra(String nhap) {
        String ma = nhap == null ? "" : nhap.trim().toUpperCase();
        if (ma.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập mã giảm giá.");
        MaGiamGia m = ds.get(ma);
        if (m == null) throw new IllegalArgumentException("Mã giảm giá không tồn tại.");
        if (m.getHanDung().isBefore(LocalDate.now())) throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng.");
        return m;
    }
}
