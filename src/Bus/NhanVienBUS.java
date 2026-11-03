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
        try { taiLai(); } catch (Exception ignored) {}
    }

    public synchronized void taiLai() throws SQLException {
        cache.clear();
        try {
            cache.addAll(dao.layTatCa());
        } catch (SQLException e) {
            System.err.println("[NhanVienBUS] CSDL offline, sử dụng danh sách mẫu: " + e.getMessage());
        }
        if (cache.isEmpty()) {
            napDuLieuMau();
        }
    }

    private void napDuLieuMau() {
        cache.add(new NhanVien("NV001", "Hứa Hữu Thịnh", "admin", "123456", 1, "Quản lý", "0901000001", LocalDate.of(1995, 3, 15), "thinh.hh@petcare.vn", "", "Đang làm việc", "Toàn thời gian", LocalDate.of(2023, 1, 1)));
        cache.add(new NhanVien("NV002", "Hà Trọng Trung", "trung.ht", "123456", 2, "Bán hàng", "0901000002", LocalDate.of(1998, 7, 20), "trung.ht@petcare.vn", "", "Đang làm việc", "Ca sáng (07:30 - 15:30)", LocalDate.of(2023, 3, 15)));
        cache.add(new NhanVien("NV003", "Lê Tuấn Anh", "tuananh.lt", "123456", 3, "Kho", "0901000003", LocalDate.of(1997, 11, 5), "tuananh.lt@petcare.vn", "", "Đang làm việc", "Ca sáng (07:30 - 15:30)", LocalDate.of(2023, 2, 1)));
        cache.add(new NhanVien("NV004", "Nguyễn Hoài Anh", "hoaianh.nh", "123456", 4, "Bác sĩ", "0901000004", LocalDate.of(1992, 9, 12), "hoaianh.nh@petcare.vn", "Ngoại khoa thú y", "Đang làm việc", "Toàn thời gian", LocalDate.of(2022, 10, 1)));
        cache.add(new NhanVien("NV005", "Nguyễn Ngọc Toàn", "toan.nn", "123456", 2, "Bán hàng", "0901000005", LocalDate.of(1999, 1, 25), "toan.nn@petcare.vn", "", "Đang làm việc", "Ca chiều (14:30 - 21:30)", LocalDate.of(2023, 5, 10)));
        cache.add(new NhanVien("NV006", "Võ Thanh Hùng", "hung06", "123456", 4, "Bác sĩ", "0901000006", LocalDate.of(1990, 12, 8), "hung.vt@petcare.vn", "Chẩn đoán hình ảnh", "Đang làm việc", "Toàn thời gian", LocalDate.of(2022, 8, 15)));
        cache.add(new NhanVien("NV007", "Đặng Thị Mai", "mai07", "123456", 3, "Kho", "0901000007", LocalDate.of(1996, 4, 18), "mai.dt@petcare.vn", "", "Đang làm việc", "Ca chiều (14:30 - 21:30)", LocalDate.of(2023, 4, 1)));
        cache.add(new NhanVien("NV008", "Ngô Gia Bảo", "bao08", "123456", 4, "Bác sĩ", "0901000008", LocalDate.of(1994, 6, 30), "bao.ng@petcare.vn", "Nội khoa & Tiêm chủng", "Đang làm việc", "Ca xoay", LocalDate.of(2023, 2, 15)));
        cache.add(new NhanVien("NV009", "Đỗ Phương Thảo", "thao09", "123456", 2, "Bán hàng", "0901000009", LocalDate.of(2000, 8, 14), "thao.dp@petcare.vn", "", "Đang làm việc", "Ca sáng (07:30 - 15:30)", LocalDate.of(2023, 7, 1)));
        cache.add(new NhanVien("NV010", "Bùi Hữu Thịnh", "thinh10", "123456", 1, "Quản lý", "0901000010", LocalDate.of(1993, 2, 28), "thinh.bh@petcare.vn", "", "Đang làm việc", "Toàn thời gian", LocalDate.of(2022, 5, 1)));
    }

    public synchronized List<NhanVien> layDanhSach() {
        return Collections.unmodifiableList(new ArrayList<>(cache));
    }

    public List<NhanVienDAO.QuyenOption> layDanhSachQuyen() throws SQLException {
        try {
            List<NhanVienDAO.QuyenOption> list = dao.layDanhSachQuyen();
            if (list != null && !list.isEmpty()) return list;
        } catch (Exception ignored) {}
        List<NhanVienDAO.QuyenOption> list = new ArrayList<>();
        list.add(new NhanVienDAO.QuyenOption(1, "Quản lý"));
        list.add(new NhanVienDAO.QuyenOption(2, "Bán hàng"));
        list.add(new NhanVienDAO.QuyenOption(3, "Kho"));
        list.add(new NhanVienDAO.QuyenOption(4, "Bác sĩ"));
        return list;
    }

    public ThongKeNhanVien layThongKe() throws SQLException {
        try {
            return dao.layThongKe();
        } catch (Exception ignored) {}
        int tong = cache.size();
        int bs = 0, bh = 0, kho = 0, ql = 0, dangLam = 0;
        for (NhanVien n : cache) {
            String v = n.getTenQuyen() == null ? "" : n.getTenQuyen().toLowerCase();
            if (v.contains("bác sĩ") || v.contains("bac si")) bs++;
            else if (v.contains("bán hàng") || v.contains("ban hang")) bh++;
            else if (v.contains("kho")) kho++;
            else ql++;
            if (!"Đã nghỉ việc".equalsIgnoreCase(n.getTrangThai())) dangLam++;
        }
        return new ThongKeNhanVien(tong, bh, bs, kho, ql, dangLam);
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
        boolean tonTai = false;
        try {
            tonTai = dao.tonTaiTenDangNhap(username, laThemMoi ? null : nv.getMaNV());
        } catch (Exception ignored) {
            for (NhanVien n : cache) {
                if (n.getTenDangNhap() != null && n.getTenDangNhap().equalsIgnoreCase(username)) {
                    if (laThemMoi || !n.getMaNV().equalsIgnoreCase(nv.getMaNV())) {
                        tonTai = true;
                        break;
                    }
                }
            }
        }
        if (tonTai) {
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
        try {
            String maNV = dao.them(nv, hash);
            try { taiLai(); } catch (Exception ignored) {}
            return maNV;
        } catch (SQLException e) {
            int maxId = 0;
            for (NhanVien n : cache) {
                try {
                    if (n.getMaNV() != null && n.getMaNV().startsWith("NV")) {
                        maxId = Math.max(maxId, Integer.parseInt(n.getMaNV().substring(2)));
                    }
                } catch (Exception ignored) {}
            }
            String mockMa = String.format("NV%03d", maxId + 1);
            nv.setMaNV(mockMa);
            cache.add(nv);
            return mockMa;
        }
    }

    public boolean capNhat(NhanVien nv, String matKhauMoiTho) throws SQLException {
        kiemTraDuLieu(nv, false, matKhauMoiTho);
        String hash = (matKhauMoiTho != null && !matKhauMoiTho.trim().isEmpty()) ? bamSha256(matKhauMoiTho.trim()) : null;
        try {
            boolean ok = dao.capNhat(nv, hash);
            if (ok) {
                try { taiLai(); } catch (Exception ignored) {}
            }
            return ok;
        } catch (SQLException e) {
            for (int i = 0; i < cache.size(); i++) {
                if (cache.get(i).getMaNV().equalsIgnoreCase(nv.getMaNV())) {
                    cache.set(i, nv);
                    return true;
                }
            }
            return true;
        }
    }

    public boolean xoa(String maNV, String maNVDangNhap) throws SQLException {
        try {
            String err = dao.kiemTraRangBuocTruocKhiXoa(maNV, maNVDangNhap);
            if (err != null) {
                throw new SQLException(err);
            }
            boolean ok = dao.xoa(maNV);
            if (ok) {
                try { taiLai(); } catch (Exception ignored) {}
            }
            return ok;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("không được phép")) throw e;
            cache.removeIf(n -> n.getMaNV().equalsIgnoreCase(maNV));
            return true;
        }
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
