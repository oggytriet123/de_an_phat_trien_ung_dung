package Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tên các chức năng phân quyền trong hệ thống Pet Station.
 * Đồng bộ chuẩn xác giữa CSDL (bảng CHUCNANG, QUYEN_CHUCNANG) và giao diện Java Swing.
 */
public final class ChucNang {
    private ChucNang() { }

    // Hệ thống & dùng chung
    public static final String TRANG_CHU        = "Trang Chủ";
    public static final String TAI_KHOAN        = "Tài Khoản";
    public static final String DANG_XUAT        = "Đăng Xuất";
    public static final String TIM_KIEM         = "Tìm Kiếm";

    // Phân hệ Danh mục
    public static final String NHAN_VIEN        = "Nhân Viên";
    public static final String KHACH_HANG       = "Khách Hàng";
    public static final String THU_CUNG         = "Thú Cưng";
    public static final String KHO_HANG         = "Kho Hàng";
    public static final String DANH_MUC_BENH    = "Danh Mục Bệnh";

    // Phân hệ Xử lý & Bán hàng POS
    public static final String HOA_DON          = "Bán Hàng";
    public static final String QUAN_LY_DICH_VU  = "Dịch Vụ";
    public static final String TIEM_PHONG       = "Tiêm Phòng";
    public static final String PHAU_THUAT       = "Phẫu Thuật";
    public static final String CHUONG           = "Lưu Trú";
    public static final String LICH_HEN         = "Lịch Hẹn";
    public static final String LICH_SU_KHO      = "Lịch Sử Kho";

    // Phân hệ Y tế, Bác sĩ & Điều trị
    public static final String DON_THUOC        = "Đơn Thuốc";
    public static final String PHAC_DO          = "Phác Đồ";

    // Phân hệ Thống kê
    public static final String THONG_KE         = "Thống Kê";
    public static final String TK_DOANH_THU     = "Thống Kê Doanh Thu";
    public static final String TK_BAN_CHAY      = "Thống Kê Bán Chạy";
    public static final String TK_TON_KHO       = "Thống Kê Tồn Kho";

    private static Set<String> of(String... s) {
        return Collections.unmodifiableSet(new HashSet<>(java.util.Arrays.asList(s)));
    }

    private static final Set<String> CHUNG = of(TRANG_CHU, TAI_KHOAN, DANG_XUAT, TIM_KIEM);

    private static String boDau(String s) {
        if (s == null) return "";
        String n = java.text.Normalizer.normalize(s.toLowerCase(), java.text.Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").replace("đ", "d");
    }

    /**
     * Quyền mặc định dự phòng theo vai trò (khi mất kết nối CSDL hoặc khởi tạo offline).
     */
    public static Set<String> macDinh(String tenQuyen) {
        Set<String> r = new HashSet<>(CHUNG);
        if (tenQuyen == null) return r;
        String q = boDau(tenQuyen);

        // 1. Quản lý / Admin: Toàn quyền 100% tất cả chức năng
        if (q.contains("quan ly") || q.contains("admin")) {
            r.addAll(of(
                TRANG_CHU, TAI_KHOAN, NHAN_VIEN, KHACH_HANG, THU_CUNG,
                KHO_HANG, DANH_MUC_BENH, HOA_DON, QUAN_LY_DICH_VU, TIEM_PHONG,
                PHAU_THUAT, CHUONG, LICH_HEN, LICH_SU_KHO, DON_THUOC, PHAC_DO,
                THONG_KE, TK_DOANH_THU, TK_BAN_CHAY, TK_TON_KHO, TIM_KIEM
            ));
        }
        // 2. Nhân viên Bán hàng: Tiếp đón khách, tính tiền POS, quản lý dịch vụ, lồng chuồng, đặt lịch, xem top bán chạy
        else if (q.contains("ban hang")) {
            r.addAll(of(
                KHACH_HANG, THU_CUNG, HOA_DON, QUAN_LY_DICH_VU, CHUONG, LICH_HEN,
                TK_BAN_CHAY, TIM_KIEM
            ));
        }
        // 3. Nhân viên Kho: Quản lý kho hàng, phiếu nhập/xuất kho, thống kê tồn kho, top bán chạy
        else if (q.contains("kho")) {
            r.addAll(of(
                KHO_HANG, LICH_SU_KHO, TK_TON_KHO, TK_BAN_CHAY, TIM_KIEM
            ));
        }
        // 4. Bác sĩ Thú y: Khám chữa bệnh, tiêm phòng, phẫu thuật, đơn thuốc, phác đồ, danh mục bệnh, lịch hẹn, lồng chuồng
        else if (q.contains("bac si")) {
            r.addAll(of(
                KHACH_HANG, THU_CUNG, DANH_MUC_BENH, TIEM_PHONG, PHAU_THUAT,
                CHUONG, LICH_HEN, DON_THUOC, PHAC_DO, TIM_KIEM
            ));
        }
        return r;
    }
}
