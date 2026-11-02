package Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tên các chức năng phân quyền trong hệ thống PetCare.
 * Đồng bộ chuẩn xác giữa CSDL (bảng CHUCNANG, QUYEN_CHUCNANG) và giao diện Java Swing.
 */
public final class ChucNang {
    private ChucNang() { }

    // Các chức năng hệ thống & dùng chung
    public static final String TRANG_CHU     = "Trang Chủ";
    public static final String TAI_KHOAN     = "Tài Khoản";
    public static final String DANG_XUAT     = "Đăng Xuất";
    public static final String TIM_KIEM      = "Tìm Kiếm";

    // Phân hệ Danh mục & Nghiệp vụ cơ bản
    public static final String NHAN_VIEN     = "Nhân Viên";
    public static final String KHACH_HANG    = "Khách Hàng";
    public static final String THU_CUNG      = "Thú Cưng";
    public static final String SAN_PHAM      = "Sản Phẩm";
    public static final String DICH_VU       = "Dịch Vụ";
    public static final String KHUYEN_MAI    = "Khuyến Mãi";
    public static final String DANH_MUC_BENH = "Danh Mục Bệnh";

    // Phân hệ Xử lý & Bán hàng POS
    public static final String HOA_DON       = "Bán Hàng";
    public static final String KHO_HANG      = "Kho Hàng";
    public static final String LICH_HEN      = "Lịch Hẹn";
    public static final String CHUONG        = "Lưu Trú";

    // Phân hệ Y tế, Bác sĩ & Điều trị
    public static final String BAC_SI        = "Bác Sĩ";
    public static final String TIEM_PHONG    = "Bác Sĩ";
    public static final String PHAU_THUAT    = "Bác Sĩ";
    public static final String DON_THUOC     = "Đơn Thuốc & Phác Đồ";
    public static final String PHAC_DO       = "Đơn Thuốc & Phác Đồ";
    public static final String DIEU_TRI      = "Đơn Thuốc & Phác Đồ";
    public static final String QUAN_LY_DICH_VU = "Dịch Vụ";

    // Phân hệ Báo cáo & Thống kê
    public static final String THONG_KE      = "Thống Kê";
    public static final String TK_DOANH_THU  = "Thống Kê";
    public static final String TK_KHACH_HANG = "Thống Kê";
    public static final String TK_THU_CUNG   = "Thống Kê";

    private static Set<String> of(String... s) {
        return Collections.unmodifiableSet(new HashSet<>(java.util.Arrays.asList(s)));
    }

    private static final Set<String> CHUNG = of(TRANG_CHU, TAI_KHOAN, DANG_XUAT, TIM_KIEM);

    /**
     * Quyền mặc định dự phòng theo vai trò (khi mất kết nối CSDL hoặc khởi tạo offline).
     */
    public static Set<String> macDinh(String tenQuyen) {
        Set<String> r = new HashSet<>(CHUNG);
        if (tenQuyen == null) return r;
        String q = tenQuyen.toLowerCase();

        // 1. Quản lý / Admin: Toàn quyền 100% tất cả các chức năng
        if (q.contains("quan ly") || q.contains("admin")) {
            r.addAll(of(
                TRANG_CHU, TAI_KHOAN, NHAN_VIEN, KHACH_HANG, THU_CUNG,
                DICH_VU, SAN_PHAM, HOA_DON, KHO_HANG, LICH_HEN, CHUONG,
                BAC_SI, DON_THUOC, PHAC_DO, DIEU_TRI, DANH_MUC_BENH, KHUYEN_MAI,
                THONG_KE, TK_DOANH_THU, TK_KHACH_HANG, TK_THU_CUNG,
                TIEM_PHONG, PHAU_THUAT, QUAN_LY_DICH_VU, TIM_KIEM,
                "Lồng Chuồng", "Chuồng Lưu Trữ", "Lịch Sử Nhập / Xuất Kho"
            ));
        }
        // 2. Nhân viên Bán hàng: Tiếp đón khách, tính tiền POS, đặt lịch, đặt chuồng, voucher
        else if (q.contains("ban hang")) {
            r.addAll(of(
                KHACH_HANG, THU_CUNG, HOA_DON, LICH_HEN, CHUONG, SAN_PHAM, DICH_VU,
                KHUYEN_MAI, TIM_KIEM, QUAN_LY_DICH_VU, "Lồng Chuồng"
            ));
        }
        // 3. Nhân viên Kho: Quản lý sản phẩm, nhập/xuất kho, kiểm kê, thống kê tồn
        else if (q.contains("kho")) {
            r.addAll(of(
                SAN_PHAM, KHO_HANG, "Lịch Sử Nhập / Xuất Kho", THONG_KE, TIM_KIEM
            ));
        }
        // 4. Bác sĩ Thú y: Khám chữa bệnh, tiêm phòng, phẫu thuật, đơn thuốc, phác đồ, theo dõi nội trú
        else if (q.contains("bac si")) {
            r.addAll(of(
                THU_CUNG, KHACH_HANG, BAC_SI, TIEM_PHONG, PHAU_THUAT, DON_THUOC, PHAC_DO, DIEU_TRI,
                DANH_MUC_BENH, LICH_HEN, CHUONG, DICH_VU, THONG_KE, TIM_KIEM, "Lồng Chuồng"
            ));
        }
        return r;
    }
}
