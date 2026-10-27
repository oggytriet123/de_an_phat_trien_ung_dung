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
        // Ưu tiên vào thẳng ngay lập tức với tài khoản chạy thử, không cần chờ kết nối SQL
        if (CHO_PHEP_TAI_KHOAN_MAU) {
            TaiKhoan tkMau = taiKhoanMau(maHoacSdt, matKhau);
            if (tkMau != null) return tkMau;
        }

        try {
            TaiKhoan tk = dao.timTheoDangNhap(maHoacSdt, bamSha256(matKhau));
            if (tk != null) return tk;
        } catch (SQLException ignored) {
        }
        return null;
    }

    private TaiKhoan taiKhoanMau(String id, String matKhau) {
        if ("123456".equals(matKhau) || "admin".equalsIgnoreCase(matKhau)) {
            if ("nv01".equalsIgnoreCase(id) || "0902345678".equals(id)) {
                return new TaiKhoan("NV01", "0902345678", "Thu Trang", "Nhân viên");
            }
            if ("admin".equalsIgnoreCase(id) || "QL001".equalsIgnoreCase(id) || "0901234567".equals(id) || id.isEmpty()) {
                return new TaiKhoan("QL001", "0901234567", "Hữu Thịnh", "Quản lý cửa hàng");
            }
            return new TaiKhoan("QL001", "0901234567", id, "Quản lý cửa hàng");
        }
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
