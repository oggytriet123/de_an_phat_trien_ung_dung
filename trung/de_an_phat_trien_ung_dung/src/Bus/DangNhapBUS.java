package Bus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import DAO.TaiKhoanDAO;
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
        try {
            TaiKhoan tk = dao.timTheoDangNhap(maHoacSdt, bamSha256(matKhau));
            if (tk != null) return tk;
        } catch (SQLException e) {
            System.err.println("[DangNhapBUS] Không truy vấn được CSDL: " + e.getMessage());
        }
        return CHO_PHEP_TAI_KHOAN_MAU ? taiKhoanMau(maHoacSdt, matKhau) : null;
    }

    private TaiKhoan taiKhoanMau(String id, String matKhau) {
        if (!"123456".equals(matKhau)) return null;
        if (id.equalsIgnoreCase("admin") || id.equalsIgnoreCase("QL001") || id.equals("0901234567"))
            return new TaiKhoan("QL001", "0901234567", "Hữu Thịnh", "Quản lý cửa hàng");
        if (id.equalsIgnoreCase("nv01") || id.equals("0902345678"))
            return new TaiKhoan("NV01", "0902345678", "Thu Trang", "Nhân viên");
        return null;
    }

    static String bamSha256(String s) {
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
