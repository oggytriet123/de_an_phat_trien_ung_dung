package Bus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;

/** Nghiệp vụ tài khoản đang đăng nhập: kiểm tra và cập nhật thông tin liên hệ. */
public class TaiKhoanBUS {
    public static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern SDT = Pattern.compile("^0\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    /**
     * Kiểm tra rồi lưu SĐT / ngày sinh / email của chính tài khoản đăng nhập. Họ tên KHÔNG được sửa.
     * @return null nếu thành công, ngược lại là thông báo lỗi.
     */
    public String capNhatThongTin(TaiKhoan tk, String sdtNhap, String ngaySinhNhap, String emailNhap) {
        String sdt = sdtNhap == null ? "" : sdtNhap.replaceAll("[\\s.\\-]", "");
        if (sdt.isEmpty()) return "Vui lòng nhập số điện thoại.";
        if (!SDT.matcher(sdt).matches()) return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.";

        LocalDate ns = null;
        String nsText = ngaySinhNhap == null ? "" : ngaySinhNhap.trim();
        if (!nsText.isEmpty()) {
            try {
                ns = LocalDate.parse(nsText, NGAY);
            } catch (DateTimeParseException e) {
                return "Ngày sinh không hợp lệ. Nhập theo dạng dd/MM/yyyy, ví dụ 20/05/1995.";
            }
            if (ns.isAfter(LocalDate.now())) return "Ngày sinh không được lớn hơn ngày hiện tại.";
            if (ns.getYear() < 1900) return "Ngày sinh không hợp lệ.";
        }

        String email = emailNhap == null ? "" : emailNhap.trim();
        if (!email.isEmpty() && (email.length() > 100 || !EMAIL.matcher(email).matches()))
            return "Email không hợp lệ, ví dụ: ten@gmail.com.";

        String emailLuu = email.isEmpty() ? null : email;
        if (!DangNhapBUS.laTaiKhoanMau(tk)) {
            try {
                if (dao.capNhatLienHe(tk.getMaTaiKhoan(), sdt, ns, emailLuu) == 0)
                    return "Không tìm thấy tài khoản trong CSDL.";
            } catch (SQLException e) {
                return "Không lưu được vào CSDL: " + e.getMessage();
            }
        }
        tk.capNhatLienHe(sdt, ns, emailLuu);
        return null;
    }
}
