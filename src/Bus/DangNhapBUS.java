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
<<<<<<< HEAD
            if (tk != null) return tk;
        } catch (SQLException ignored) {
        }
        return null;
=======
            return tk; // sai mật khẩu/không có tài khoản -> null, KHÔNG rơi xuống tài khoản mẫu
        } catch (SQLException e) {
            System.err.println("[DangNhapBUS] Không truy vấn được CSDL: " + e.getMessage());
        }
        // Chỉ dùng tài khoản mẫu khi không kết nối được CSDL
        return CHO_PHEP_TAI_KHOAN_MAU ? taiKhoanMau(maHoacSdt, matKhau) : null;
>>>>>>> origin/main
    }

    /** Tài khoản mẫu (chạy thử, không có trong CSDL). */
    public static boolean laTaiKhoanMau(TaiKhoan tk) {
        return tk != null && (tk.getMaTaiKhoan().equals("QL001") || tk.getMaTaiKhoan().equals("NV01"));
    }

    private TaiKhoan taiKhoanMau(String id, String matKhau) {
<<<<<<< HEAD
        if ("123456".equals(matKhau) || "admin".equalsIgnoreCase(matKhau)) {
            if ("nv01".equalsIgnoreCase(id) || "0902345678".equals(id)) {
                return new TaiKhoan("NV01", "0902345678", "Thu Trang", "Nhân viên");
            }
            if ("admin".equalsIgnoreCase(id) || "QL001".equalsIgnoreCase(id) || "0901234567".equals(id) || id.isEmpty()) {
                return new TaiKhoan("QL001", "0901234567", "Hữu Thịnh", "Quản lý cửa hàng");
            }
            return new TaiKhoan("QL001", "0901234567", id, "Quản lý cửa hàng");
        }
=======
        if (!"123456".equals(matKhau)) return null;
        if (id.equalsIgnoreCase("admin") || id.equalsIgnoreCase("QL001") || id.equals("0901234567"))
            return mau(new TaiKhoan("QL001", "admin", "Hữu Thịnh", "Quản lý", ChucNang.macDinh("Quản lý"),
                    "0901234567", java.time.LocalDate.of(1995, 5, 20), "thinh@petstation.vn"));
        if (id.equalsIgnoreCase("nv01") || id.equals("0902345678"))
            return mau(new TaiKhoan("NV01", "nv01", "Thu Trang", "Bán hàng", ChucNang.macDinh("Bán hàng"),
                    "0902345678", java.time.LocalDate.of(1998, 11, 3), "trang@petstation.vn"));
>>>>>>> origin/main
        return null;
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