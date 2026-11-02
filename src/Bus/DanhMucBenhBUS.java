package Bus;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import DAO.DanhMucBenhDAO;
import Entity.DanhMucBenh;

/**
 * Nghiệp vụ Danh mục bệnh: kiểm tra dữ liệu, tìm kiếm, thêm/sửa/xóa.
 * Dữ liệu nạp từ SQL Server (data/PetCare.sql); mất kết nối thì danh sách rỗng, thay đổi không được lưu.
 */
public class DanhMucBenhBUS {
    private final DanhMucBenhDAO dao = new DanhMucBenhDAO();
    private final List<DanhMucBenh> ds = new ArrayList<>();
    private boolean ngoaiTuyen;

    public boolean laNgoaiTuyen() { return ngoaiTuyen; }

    public synchronized void taiLai() {
        ds.clear();
        try {
            ds.addAll(dao.layTatCa());
            ngoaiTuyen = false;
        } catch (SQLException ex) {
            ngoaiTuyen = true;
        }
    }

    public synchronized List<DanhMucBenh> layDanhSach() { return new ArrayList<>(ds); }

    /** Tìm theo mã, tên bệnh hoặc triệu chứng (không phân biệt hoa thường và dấu). */
    public synchronized List<DanhMucBenh> timKiem(String tuKhoa) {
        String k = boDau(tuKhoa);
        List<DanhMucBenh> kq = new ArrayList<>();
        for (DanhMucBenh b : ds) {
            if (k.isEmpty() || boDau(b.getMaBenh()).contains(k) || boDau(b.getTenBenh()).contains(k)
                    || boDau(b.getTrieuChung()).contains(k)) kq.add(b);
        }
        return kq;
    }

    private static String boDau(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").replace('đ', 'd');
    }

    /** @throws IllegalArgumentException nếu dữ liệu không hợp lệ */
    public synchronized void kiemTra(String maBenhDangSua, String ten, String trieuChung) {
        if (ten == null || ten.trim().isEmpty()) throw new IllegalArgumentException("Tên bệnh không được để trống.");
        if (ten.trim().length() > 100) throw new IllegalArgumentException("Tên bệnh tối đa 100 ký tự.");
        if (trieuChung != null && trieuChung.trim().length() > 500)
            throw new IllegalArgumentException("Triệu chứng tối đa 500 ký tự.");
        for (DanhMucBenh b : ds) {
            if (!b.getMaBenh().equals(maBenhDangSua) && boDau(b.getTenBenh()).equals(boDau(ten)))
                throw new IllegalArgumentException("Bệnh \"" + ten.trim() + "\" đã có trong danh mục.");
        }
    }

    private String phatSinhMa() {
        int max = 0;
        for (DanhMucBenh b : ds) {
            try { max = Math.max(max, Integer.parseInt(b.getMaBenh().replaceAll("\\D", ""))); }
            catch (NumberFormatException ignored) { }
        }
        return String.format("BNH%03d", max + 1);
    }

    public synchronized DanhMucBenh them(String ten, String trieuChung) throws SQLException {
        kiemTra(null, ten, trieuChung);
        DanhMucBenh b = new DanhMucBenh(phatSinhMa(), ten.trim(), trieuChung == null ? "" : trieuChung.trim());
        if (!ngoaiTuyen) dao.them(b);
        ds.add(b);
        return b;
    }

    public synchronized void sua(String maBenh, String ten, String trieuChung) throws SQLException {
        kiemTra(maBenh, ten, trieuChung);
        for (DanhMucBenh b : ds) {
            if (b.getMaBenh().equals(maBenh)) {
                b.setTenBenh(ten.trim());
                b.setTrieuChung(trieuChung == null ? "" : trieuChung.trim());
                if (!ngoaiTuyen) dao.capNhat(b);
                return;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy bệnh " + maBenh + ".");
    }

    public synchronized void xoa(String maBenh) throws SQLException {
        if (!ngoaiTuyen) dao.xoa(maBenh);
        ds.removeIf(b -> b.getMaBenh().equals(maBenh));
    }
}
