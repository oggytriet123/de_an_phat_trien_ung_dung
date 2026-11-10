package Bus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import DAO.MaGiamGiaDAO;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.MaGiamGia;

/** Quản lý mã giảm giá, đồng bộ trực tiếp với CSDL SQL Server (bảng MAGIAMGIA). */
public class MaGiamGiaBUS {
    private static final DateTimeFormatter NGAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
            them(new MaGiamGia("GIAM10", true, 10, LocalDate.now(), LocalDate.of(2027, 12, 31),
                    "Giảm 10% cho khách hàng thành viên", 100, 0, MaGiamGia.TAT_CA, 0, 0));
            them(new MaGiamGia("PET50K", false, 50_000, LocalDate.now(), LocalDate.of(2027, 12, 31),
                    "Giảm 50.000₫ cho hóa đơn dịch vụ", 50, 0, MaGiamGia.TAT_CA, 0, 0));
            them(new MaGiamGia("VIP100K", false, 100_000, LocalDate.now(), LocalDate.of(2027, 11, 30),
                    "Ưu đãi khách hàng VIP thân thiết", 30, 0, MaGiamGia.TAT_CA, 0, 0));
            them(new MaGiamGia("MUA2TANG1", true, 100, LocalDate.now(), LocalDate.of(2027, 12, 31),
                    "Mua 2 tặng 1 thức ăn cho thú cưng", 50, 0, "Thức ăn", 2, 1));
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

    public boolean chuaDenHan(MaGiamGia m) {
        return m.getNgayBatDau() != null && m.getNgayBatDau().isAfter(LocalDate.now());
    }

    public boolean daHetLuot(MaGiamGia m) {
        return m.hetLuot();
    }

    /**
     * Ghi nhận 1 lượt sử dụng mã giảm giá sau khi thanh toán hóa đơn thành công.
     */
    public void ghiNhanSuDung(String ma) {
        if (ma == null || ma.isBlank()) return;
        String key = ma.trim().toUpperCase();
        MaGiamGia m = ds.get(key);
        if (m != null) {
            m.tangDaSuDung();
        }
        try {
            dao.tangLuotSuDung(key);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate hanDung) {
        return tao(maNhap, phanTram, giaTri, LocalDate.now(), hanDung, "", 0, MaGiamGia.TAT_CA, 0, 0);
    }

    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung) {
        return tao(maNhap, phanTram, giaTri, ngayBatDau, hanDung, "", 0, MaGiamGia.TAT_CA, 0, 0);
    }

    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate hanDung, String ghiChu) {
        return tao(maNhap, phanTram, giaTri, LocalDate.now(), hanDung, ghiChu, 0, MaGiamGia.TAT_CA, 0, 0);
    }

    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung, String ghiChu) {
        return tao(maNhap, phanTram, giaTri, ngayBatDau, hanDung, ghiChu, 0, MaGiamGia.TAT_CA, 0, 0);
    }

    /**
     * Tạo mã giảm giá đầy đủ thông số (số lượt người dùng, loại sản phẩm áp dụng, Mua X tặng Y + giảm %).
     */
    public MaGiamGia tao(String maNhap, boolean phanTram, long giaTri, LocalDate ngayBatDau, LocalDate hanDung,
                         String ghiChu, int soLuotToiDa, String danhMucApDung, int muaX, int tangY) {
        String ma = maNhap == null ? "" : maNhap.trim().toUpperCase();
        if (ma.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập mã giảm giá.");
        if (!ma.matches("[A-Z0-9_-]{3,20}")) {
            throw new IllegalArgumentException("Mã gồm 3–20 ký tự: chữ cái, chữ số, gạch ngang hoặc gạch dưới (không dấu, không khoảng trắng).");
        }
        if (ds.containsKey(ma)) throw new IllegalArgumentException("Mã giảm giá \"" + ma + "\" đã tồn tại trong hệ thống.");
        if (soLuotToiDa < 0 || soLuotToiDa > 1_000_000) {
            throw new IllegalArgumentException("Số lượng lượt dùng không hợp lệ (0 = Không giới hạn).");
        }
        boolean laMuaTang = (muaX > 0 || tangY > 0);
        if (laMuaTang) {
            if (muaX < 1 || muaX > 100) throw new IllegalArgumentException("Số lượng Mua (X) phải từ 1 đến 100.");
            if (tangY < 1 || tangY > 100) throw new IllegalArgumentException("Số lượng Tặng (Y) phải từ 1 đến 100.");
            if (giaTri < 1 || giaTri > 100) throw new IllegalArgumentException("Phần trăm giảm cho phần tặng phải từ 1% đến 100%.");
        } else if (phanTram) {
            if (giaTri < 1 || giaTri > 100) throw new IllegalArgumentException("Phần trăm giảm phải từ 1% đến 100%.");
        } else if (giaTri < 1_000 || giaTri > 100_000_000) {
            throw new IllegalArgumentException("Số tiền giảm phải từ 1.000₫ đến 100.000.000₫.");
        }
        if (ngayBatDau == null) throw new IllegalArgumentException("Vui lòng nhập ngày bắt đầu (dd/MM/yyyy).");
        if (hanDung == null) throw new IllegalArgumentException("Vui lòng nhập ngày kết thúc (dd/MM/yyyy).");
        if (hanDung.isBefore(ngayBatDau)) throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        if (hanDung.isBefore(LocalDate.now())) throw new IllegalArgumentException("Ngày kết thúc không được ở quá khứ.");

        MaGiamGia m = new MaGiamGia(ma, phanTram || laMuaTang, giaTri, ngayBatDau, hanDung,
                ghiChu, soLuotToiDa, 0, danhMucApDung, muaX, tangY);
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
        MaGiamGia cu = ds.get(ma);
        MaGiamGia m = new MaGiamGia(ma, phanTram, giaTri, cu.getNgayBatDau(), hanDung,
                ghiChu, cu.getSoLuotToiDa(), cu.getDaSuDung(), cu.getDanhMucApDung(), cu.getMuaX(), cu.getTangY());
        ds.put(ma, m);
        return m;
    }

    /**
     * Kiểm tra mã hợp lệ (còn hạn, đã đến ngày, còn lượt dùng).
     */
    public MaGiamGia kiemTra(String nhap) {
        String ma = nhap == null ? "" : nhap.trim().toUpperCase();
        if (ma.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập mã giảm giá.");
        MaGiamGia m = ds.get(ma);
        if (m == null) throw new IllegalArgumentException("Mã giảm giá không tồn tại.");
        if (chuaDenHan(m)) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến ngày áp dụng (từ "
                    + m.getNgayBatDau().format(NGAY_FMT) + ").");
        }
        if (m.getHanDung().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng.");
        }
        if (m.hetLuot()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng (" + m.getDaSuDung() + "/" + m.getSoLuotToiDa() + " lượt).");
        }
        return m;
    }

    /**
     * Kiểm tra mã giảm giá trên một Hóa đơn cụ thể (bao gồm kiểm tra loại sản phẩm & điều kiện Mua X tặng Y).
     */
    public MaGiamGia kiemTraChoHoaDon(String nhap, HoaDon hd) {
        MaGiamGia m = kiemTra(nhap);
        if (hd == null || hd.getDanhSachChiTiet().isEmpty()) {
            return m;
        }
        if (!m.apDungChoTatCa()) {
            boolean coSpKhop = false;
            int maxSlKhop = 0;
            for (ChiTietHoaDon d : hd.getDanhSachChiTiet()) {
                if (m.khopSanPham(d.getSanPham())) {
                    coSpKhop = true;
                    maxSlKhop = Math.max(maxSlKhop, d.getSoLuong());
                }
            }
            if (!coSpKhop) {
                throw new IllegalArgumentException("Giỏ hàng chưa có mặt hàng \"" + m.getDanhMucApDung() + "\" để áp dụng mã này.");
            }
            if (m.laMuaXTangY() && maxSlKhop < m.getMuaX()) {
                throw new IllegalArgumentException("Cần mua tối thiểu " + m.getMuaX() + " mặt hàng \""
                        + m.getDanhMucApDung() + "\" để hưởng ưu đãi Mua " + m.getMuaX() + " tặng " + m.getTangY() + ".");
            }
        } else if (m.laMuaXTangY()) {
            int maxSl = 0;
            for (ChiTietHoaDon d : hd.getDanhSachChiTiet()) {
                maxSl = Math.max(maxSl, d.getSoLuong());
            }
            if (maxSl < m.getMuaX()) {
                throw new IllegalArgumentException("Cần mua tối thiểu " + m.getMuaX() + " sản phẩm cùng loại để hưởng ưu đãi Mua "
                        + m.getMuaX() + " tặng " + m.getTangY() + ".");
            }
        }
        return m;
    }
}
