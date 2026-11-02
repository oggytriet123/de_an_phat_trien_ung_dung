package Bus;



import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;

import java.sql.SQLException;

import java.time.LocalDate;

import java.util.HashMap;

import java.util.Map;



import DAO.TaiKhoanDAO;

import Entity.ChucNang;

import Entity.TaiKhoan;



public class DangNhapBUS {

    private final TaiKhoanDAO dao = new TaiKhoanDAO();



    // Dữ liệu tài khoản chuẩn dự phòng (khi CSDL offline hoặc tài khoản mẫu chưa có trong CSDL)

    private static final Map<String, TaiKhoanMock> MOCK_ACCOUNTS = new HashMap<>();

    private static final Map<String, String> CUSTOM_PASSWORDS = new HashMap<>(); // key: username -> password



    private static class TaiKhoanMock {

        final String maNV;

        final String tenDangNhap;

        final String hoTen;

        final String vaiTro;

        final String soDienThoai;

        final String email;

        final LocalDate ngaySinh;

        final String defaultPass;



        TaiKhoanMock(String maNV, String tenDangNhap, String hoTen, String vaiTro, String sdt, String email, LocalDate ngaySinh, String defaultPass) {

            this.maNV = maNV;

            this.tenDangNhap = tenDangNhap;

            this.hoTen = hoTen;

            this.vaiTro = vaiTro;

            this.soDienThoai = sdt;

            this.email = email;

            this.ngaySinh = ngaySinh;

            this.defaultPass = defaultPass;

        }



        TaiKhoan toTaiKhoan() {

            TaiKhoan tk = new TaiKhoan(maNV, tenDangNhap, hoTen, vaiTro,

                    ChucNang.macDinh(vaiTro), soDienThoai, ngaySinh, email);

            tk.setThongTinCongViec(LocalDate.of(2023, 1, 1), "Toàn thời gian", "Đang làm việc");

            return tk;

        }

    }



    static {

        themTaiKhoan(new TaiKhoanMock("NV001", "admin", "Hứa Hữu Thịnh", "Quản lý", "0901000001", "thinh.hh@petcare.vn", LocalDate.of(1995, 3, 15), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV002", "trung.ht", "Hà Trọng Trung", "Bán hàng", "0901000002", "trung.ht@petcare.vn", LocalDate.of(1998, 7, 20), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV003", "tuananh.lt", "Lê Tuấn Anh", "Kho", "0901000003", "tuananh.lt@petcare.vn", LocalDate.of(1997, 11, 5), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV004", "hoaianh.nh", "Nguyễn Hoài Anh", "Bác sĩ", "0901000004", "hoaianh.nh@petcare.vn", LocalDate.of(1992, 9, 12), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV005", "toan.nn", "Nguyễn Ngọc Toàn", "Bán hàng", "0901000005", "toan.nn@petcare.vn", LocalDate.of(1999, 1, 25), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV006", "hung06", "Võ Thanh Hùng", "Bác sĩ", "0901000006", "hung.vt@petcare.vn", LocalDate.of(1990, 12, 8), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV007", "mai07", "Đặng Thị Mai", "Kho", "0901000007", "mai.dt@petcare.vn", LocalDate.of(1996, 4, 18), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV008", "bao08", "Ngô Gia Bảo", "Bác sĩ", "0901000008", "bao.ng@petcare.vn", LocalDate.of(1994, 6, 30), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV009", "thao09", "Đỗ Phương Thảo", "Bán hàng", "0901000009", "thao.dp@petcare.vn", LocalDate.of(2000, 8, 14), "123456"));

        themTaiKhoan(new TaiKhoanMock("NV010", "thinh10", "Bùi Hữu Thịnh", "Quản lý", "0901000010", "thinh.bh@petcare.vn", LocalDate.of(1993, 2, 28), "123456"));

    }



    private static void themTaiKhoan(TaiKhoanMock mock) {

        MOCK_ACCOUNTS.put(mock.maNV.toLowerCase(), mock);

        MOCK_ACCOUNTS.put(mock.tenDangNhap.toLowerCase(), mock);

        if (mock.soDienThoai != null && !mock.soDienThoai.isEmpty()) {

            MOCK_ACCOUNTS.put(mock.soDienThoai.replaceAll("[^0-9]", ""), mock);

        }

    }



    /**

     * Xác thực đăng nhập:

     * 1. Ưu tiên tra cứu CSDL SQL Server (hỗ trợ mã NV, tên đăng nhập hoặc SĐT, mật khẩu hash hoặc thô).

     * 2. Nếu CSDL offline hoặc không tìm thấy, đối chiếu với danh sách tài khoản tiêu chuẩn dự phòng.

     * @return TaiKhoan nếu thông tin chính xác, null nếu sai.

     */

    public TaiKhoan dangNhap(String maHoacSdt, String matKhau) {

        if (maHoacSdt == null || maHoacSdt.trim().isEmpty() || matKhau == null || matKhau.isEmpty()) return null;

        String id = maHoacSdt.trim();

        String hash = bamSha256(matKhau);



        // 1. Thử xác thực trực tiếp qua CSDL SQL Server

        try {

            TaiKhoan tk = dao.timTheoDangNhap(id, matKhau, hash);

            if (tk != null) {

                if (tk.getChucNang() == null || tk.getChucNang().isEmpty()) {

                    tk = new TaiKhoan(tk.getMaTaiKhoan(), tk.getTenDangNhap(), tk.getHoTen(), tk.getVaiTro(),

                            ChucNang.macDinh(tk.getVaiTro()), tk.getSoDienThoai(), tk.getNgaySinh(), tk.getEmail());

                }

                return tk;

            }

        } catch (SQLException e) {

            System.err.println("[DangNhapBUS] Không truy vấn được CSDL: " + e.getMessage() + ". Sử dụng xác thực nội bộ.");

        }



        // 2. Fallback tài khoản mặc định dự phòng

        String key = id.toLowerCase();

        TaiKhoanMock mock = MOCK_ACCOUNTS.get(key);

        if (mock == null && key.matches("^[0-9]+$")) {

            mock = MOCK_ACCOUNTS.get(key.replaceAll("[^0-9]", ""));

        }



        if (mock != null) {

            String expected = CUSTOM_PASSWORDS.getOrDefault(mock.tenDangNhap.toLowerCase(), mock.defaultPass);

            if (expected.equals(matKhau) || bamSha256(expected).equalsIgnoreCase(hash)) {

                return mock.toTaiKhoan();

            }

        }



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



        // 1. Thử cập nhật vào CSDL

        try {

            if (dao.datLaiMatKhau(id, lh, bamSha256(moi)) > 0) return null;

        } catch (SQLException e) {

            System.err.println("[DangNhapBUS] Không đặt lại được trên CSDL: " + e.getMessage());

        }



        // 2. Fallback kiểm tra tài khoản nội bộ

        TaiKhoanMock mock = MOCK_ACCOUNTS.get(id.toLowerCase());

        if (mock != null) {

            String lhClean = lh.replaceAll("[^0-9]", "");

            boolean khopSdt = !lhClean.isEmpty() && mock.soDienThoai.replaceAll("[^0-9]", "").equals(lhClean);

            boolean khopEmail = mock.email != null && mock.email.equalsIgnoreCase(lh);

            if (khopSdt || khopEmail) {

                CUSTOM_PASSWORDS.put(mock.tenDangNhap.toLowerCase(), moi);

                return null;

            }

        }



        return "Thông tin không khớp với tài khoản đã đăng ký.";

    }



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

}

