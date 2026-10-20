package Bus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import DAO.NhanVienDAO;
import Entity.NhanVien;
import Entity.ThongKeNhanVien;

/**
 * Xử lý nghiệp vụ cho hồ sơ Nhân viên (Validation, Mã hóa SHA-256, Bộ lọc, Thống kê).
 */
public class NhanVienBUS {
    private final NhanVienDAO dao = new NhanVienDAO();
    private final List<NhanVien> cache = new ArrayList<>();

    public NhanVienBUS() {
    }

    public synchronized void taiLai() throws SQLException {
        cache.clear();
        cache.addAll(dao.layTatCa());
    }

    public synchronized List<NhanVien> layDanhSach() {
        return Collections.unmodifiableList(new ArrayList<>(cache));
    }

    public List<NhanVienDAO.QuyenOption> layDanhSachQuyen() throws SQLException {
        return dao.layDanhSachQuyen();
    }

    public ThongKeNhanVien layThongKe() throws SQLException {
        return dao.layThongKe();
    }

    /**
     * Băm chuỗi bằng thuật toán SHA-256, trả về chuỗi Hex in hoa.
     */
    public static String bamSha256(String s) {
        if (s == null) return "";
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu nhân viên.
     */
    public void kiemTraDuLieu(NhanVien nv, boolean laThemMoi, String matKhauTho) throws SQLException {
        if (nv == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không được rỗng.");
        }
        if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên nhân viên không được để trống.");
        }
        if (nv.getHoTen().trim().length() > 100) {
            throw new IllegalArgumentException("Họ và tên không được vượt quá 100 ký tự.");
        }

        String username = nv.getTenDangNhap() != null ? nv.getTenDangNhap().trim() : "";
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (!username.matches("^[a-zA-Z0-9_]{3,30}$")) {
            throw new IllegalArgumentException("Tên đăng nhập chỉ gồm chữ cái, số, dấu gạch dưới (3–30 ký tự, không dấu).");
        }
        if (dao.tonTaiTenDangNhap(username, laThemMoi ? null : nv.getMaNV())) {
            throw new IllegalArgumentException("Tên đăng nhập \"" + username + "\" đã có người sử dụng.");
        }

        if (laThemMoi) {
            if (matKhauTho == null || matKhauTho.trim().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu ban đầu phải có ít nhất 6 ký tự.");
            }
        } else if (matKhauTho != null && !matKhauTho.trim().isEmpty()) {
            if (matKhauTho.trim().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
            }
        }

        String sdt = chiSo(nv.getSoDienThoai());
        if (!sdt.isEmpty() && (sdt.length() < 9 || sdt.length() > 11)) {
            throw new IllegalArgumentException("Số điện thoại phải gồm từ 9 đến 11 chữ số.");
        }

        String email = nv.getEmail() != null ? nv.getEmail().trim() : "";
        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Địa chỉ email không đúng định dạng (ví dụ: ten@petcare.vn).");
        }

        if (nv.getNgaySinh() != null) {
            int tuoi = Period.between(nv.getNgaySinh(), LocalDate.now()).getYears();
            if (tuoi < 18) {
                throw new IllegalArgumentException("Nhân viên phải từ đủ 18 tuổi trở lên (hiện tại: " + tuoi + " tuổi).");
            }
            if (tuoi > 75) {
                throw new IllegalArgumentException("Năm sinh không hợp lệ.");
            }
        }
    }

    public String them(NhanVien nv, String matKhauTho) throws SQLException {
        kiemTraDuLieu(nv, true, matKhauTho);
        String hash = bamSha256(matKhauTho.trim());
        String maNV = dao.them(nv, hash);
        taiLai();
        return maNV;
    }

    public boolean capNhat(NhanVien nv, String matKhauMoiTho) throws SQLException {
        kiemTraDuLieu(nv, false, matKhauMoiTho);
        String hash = (matKhauMoiTho != null && !matKhauMoiTho.trim().isEmpty()) ? bamSha256(matKhauMoiTho.trim()) : null;
        boolean ok = dao.capNhat(nv, hash);
        if (ok) {
            taiLai();
        }
        return ok;
    }

    public boolean xoa(String maNV, String maNVDangNhap) throws SQLException {
        String err = dao.kiemTraRangBuocTruocKhiXoa(maNV, maNVDangNhap);
        if (err != null) {
            throw new SQLException(err);
        }
        boolean ok = dao.xoa(maNV);
        if (ok) {
            taiLai();
        }
        return ok;
    }

    /**
     * Tìm kiếm và lọc đa tiêu chí.
     */
    public List<NhanVien> timKiemVaLoc(String tuKhoa, String vaiTroLoc, String trangThaiLoc, String caLamLoc) {
        String kw = norm(tuKhoa != null ? tuKhoa.trim() : "");
        String sdtKw = chiSo(tuKhoa);

        List<NhanVien> ketQua = new ArrayList<>();
        for (NhanVien nv : cache) {
            // Lọc theo Vai trò
            if (vaiTroLoc != null && !vaiTroLoc.equals("Tất cả")) {
                if (!nv.getTenQuyen().equalsIgnoreCase(vaiTroLoc)) {
                    continue;
                }
            }

            // Lọc theo Trạng thái
            if (trangThaiLoc != null && !trangThaiLoc.equals("Tất cả")) {
                if (!nv.getTrangThai().equalsIgnoreCase(trangThaiLoc)) {
                    continue;
                }
            }

            // Lọc theo Ca làm việc
            if (caLamLoc != null && !caLamLoc.equals("Tất cả")) {
                if (!nv.getCaLamViec().equalsIgnoreCase(caLamLoc)) {
                    continue;
                }
            }

            // Tìm kiếm theo từ khóa (Họ tên, Mã NV, Tên đăng nhập, SĐT, Email, Chuyên môn)
            if (!kw.isEmpty()) {
                String searchHay = norm(nv.getHoTen()) + " "
                        + norm(nv.getMaNV()) + " "
                        + norm(nv.getTenDangNhap()) + " "
                        + norm(nv.getEmail()) + " "
                        + norm(nv.getChuyenMon()) + " "
                        + norm(nv.getTenQuyen());

                boolean matchText = searchHay.contains(kw);
                boolean matchPhone = (!sdtKw.isEmpty() && chiSo(nv.getSoDienThoai()).contains(sdtKw));

                if (!matchText && !matchPhone) {
                    continue;
                }
            }

            ketQua.add(nv);
        }
        return ketQua;
    }

    public static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
