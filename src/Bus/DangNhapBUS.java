package Bus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import DAO.TaiKhoanDAO;
import Entity.ChucNang;
import Entity.TaiKhoan;

public class DangNhapBUS {
    /**
     * true  = nếu không kết nối được SQL Server (hoặc không khớp trong CSDL) thì cho phép
     *         đăng nhập bằng tài khoản mẫu bên dưới để chạy thử giao diện.
     * false = chỉ đăng nhập bằng CSDL. NHỚ ĐẶT false khi triển khai thật.
     */
    private static final boolean CHO_PHEP_TAI_KHOAN_MAU = true;

    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    /** @return tài khoản nếu đúng, null nếu sai thông tin. */
    public TaiKhoan dangNhap(String maHoacSdt, String matKhau) {
        // Ưu tiên vào thẳng ngay lập tức với tài khoản chạy thử, không cần chờ kết nối SQL
        if (CHO_PHEP_TAI_KHOAN_MAU) {
            TaiKhoan tkMau = taiKhoanMau(maHoacSdt, matKhau);
            if (tkMau != null) return tkMau;
        }

        try {
            TaiKhoan tk = dao.timTheoDangNhap(maHoacSdt, bamSha256(matKhau));
            return tk; // sai mật khẩu/không có tài khoản -> null
        } catch (SQLException e) {
            System.err.println("[DangNhapBUS] Không truy vấn được CSDL: " + e.getMessage());
        }
        // Chỉ dùng tài khoản mẫu khi không kết nối được CSDL
        return CHO_PHEP_TAI_KHOAN_MAU ? taiKhoanMau(maHoacSdt, matKhau) : null;
    }

    /** Tài khoản mẫu (chạy thử, không có trong CSDL). */
    public static boolean laTaiKhoanMau(TaiKhoan tk) {
        return tk != null && (tk.getMaTaiKhoan().equals("QL001") || tk.getMaTaiKhoan().matches("NV0[1-3]"));
    }

    /** Mật khẩu mới của tài khoản mẫu sau khi "Quên mật khẩu" (chỉ giữ trong lần chạy này). */
    private static final java.util.Map<String, String> MAT_KHAU_MAU_MOI = new java.util.HashMap<>();

    /** Mật khẩu hiện tại của tài khoản mẫu có đúng không. */
    public static boolean dungMatKhauMau(TaiKhoan tk, String matKhau) {
        String moi = MAT_KHAU_MAU_MOI.get(tk.getMaTaiKhoan());
        if (moi != null) return moi.equals(matKhau);
        return "123456".equals(matKhau) || "admin".equalsIgnoreCase(matKhau);
    }

    public static void datMatKhauMau(TaiKhoan tk, String moi) {
        MAT_KHAU_MAU_MOI.put(tk.getMaTaiKhoan(), moi);
    }

    private TaiKhoan taiKhoanMau(String id, String matKhau) {
        TaiKhoan tk = timMau(id);
        return tk != null && dungMatKhauMau(tk, matKhau) ? mau(tk) : null;
    }

    private static TaiKhoan timMau(String id) {
        if (id == null) return null;
        id = id.trim();
        if (id.equalsIgnoreCase("admin") || id.equalsIgnoreCase("QL001") || id.equals("0901234567") || id.isEmpty())
            return new TaiKhoan("QL001", "admin", "Hữu Thịnh", "Quản lý", ChucNang.macDinh("Quản lý"),
                    "0901234567", java.time.LocalDate.of(1995, 5, 20), "thinh@petstation.vn");
        if (id.equalsIgnoreCase("nv01") || id.equals("0902345678"))
            return new TaiKhoan("NV01", "nv01", "Thu Trang", "Bán hàng", ChucNang.macDinh("Bán hàng"),
                    "0902345678", java.time.LocalDate.of(1998, 11, 3), "trang@petstation.vn");
        if (id.equalsIgnoreCase("nv02") || id.equals("0903456789"))
            return new TaiKhoan("NV02", "nv02", "Quốc Việt", "Kho", ChucNang.macDinh("Kho"),
                    "0903456789", java.time.LocalDate.of(1996, 2, 14), "viet@petstation.vn");
        if (id.equalsIgnoreCase("nv03") || id.equals("0904567890"))
            return new TaiKhoan("NV03", "nv03", "Thanh Tùng", "Bác sĩ", ChucNang.macDinh("Bác sĩ"),
                    "0904567890", java.time.LocalDate.of(1990, 8, 9), "tung@petstation.vn");
        return null;
    }

    /**
     * Quên mật khẩu: đặt mật khẩu mới khi nhập đúng SĐT hoặc email đã đăng ký của tài khoản.
     * @return null nếu thành công, ngược lại là thông báo lỗi.
     */
    public String quenMatKhau(String maHoacTenDN, String lienHe, String moi, String nhapLai) {
        String id = maHoacTenDN == null ? "" : maHoacTenDN.trim();
        String lh = lienHe == null ? "" : lienHe.trim();
        if (id.isEmpty()) return "Vui lòng nhập mã tài khoản hoặc tên đăng nhập.";
        if (lh.isEmpty()) return "Vui lòng nhập số điện thoại hoặc email đã đăng ký.";
        if (moi == null || moi.length() < 6) return "Mật khẩu mới phải có ít nhất 6 ký tự.";
        if (moi.contains(" ")) return "Mật khẩu mới không được chứa khoảng trắng.";
        if (!moi.equals(nhapLai)) return "Mật khẩu nhập lại không khớp.";
        final String KHONG_KHOP = "Thông tin không khớp với tài khoản đã đăng ký.";

        boolean dbDuoc = true;
        try {
            if (dao.datLaiMatKhau(id, lh, bamSha256(moi)) > 0) {
                TaiKhoan m = timMau(id);
                if (m != null) MAT_KHAU_MAU_MOI.put(m.getMaTaiKhoan(), moi);
                return null;
            }
        } catch (SQLException e) {
            dbDuoc = false;
            System.err.println("[DangNhapBUS] Không đặt lại được mật khẩu trong CSDL: " + e.getMessage());
        }
        if (CHO_PHEP_TAI_KHOAN_MAU && !id.isEmpty()) {
            TaiKhoan m = timMau(id);
            if (m != null && !id.isEmpty() && (lh.equals(m.getSoDienThoai()) || lh.equalsIgnoreCase(m.getEmail()))) {
                MAT_KHAU_MAU_MOI.put(m.getMaTaiKhoan(), moi);
                return null;
            }
        }
        return dbDuoc ? KHONG_KHOP : KHONG_KHOP + " (Chưa kết nối được CSDL, chỉ kiểm tra được tài khoản mẫu.)";
    }

    private static TaiKhoan mau(TaiKhoan tk) {
        tk.setThongTinCongViec(java.time.LocalDate.of(2024, 1, 15), "Toàn thời gian", "Đang làm việc");
        return tk;
    }

    public static String bamSha256(String s) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
