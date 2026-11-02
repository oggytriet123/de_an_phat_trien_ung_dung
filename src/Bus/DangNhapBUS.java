package Bus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;

public class DangNhapBUS {
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    /** @return tài khoản nếu đúng, null nếu sai thông tin hoặc không kết nối được CSDL. */
    public TaiKhoan dangNhap(String maHoacSdt, String matKhau) {
        if (maHoacSdt == null || maHoacSdt.trim().isEmpty() || matKhau == null || matKhau.isEmpty()) return null;
        try {
            return dao.timTheoDangNhap(maHoacSdt.trim(), bamSha256(matKhau)); // sai mật khẩu/không có tài khoản -> null
        } catch (SQLException e) {
            System.err.println("[DangNhapBUS] Không truy vấn được CSDL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Quên mật khẩu: đặt mật khẩu mới khi nhập đúng SĐT hoặc email đã đăng ký của tài khoản (kiểm tra trong CSDL).
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
        try {
            if (dao.datLaiMatKhau(id, lh, bamSha256(moi)) > 0) return null;
            return "Thông tin không khớp với tài khoản đã đăng ký.";
        } catch (SQLException e) {
            System.err.println("[DangNhapBUS] Không đặt lại được mật khẩu: " + e.getMessage());
            return "Không kết nối được cơ sở dữ liệu. Vui lòng thử lại sau.";
        }
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
