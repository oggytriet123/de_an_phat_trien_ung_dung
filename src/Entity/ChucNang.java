package Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Tên các chức năng – phải trùng với cột tenChucNang trong bảng CHUCNANG. */
public final class ChucNang {
    private ChucNang() { }

    public static final String TRANG_CHU = "Trang Chủ";
    public static final String TAI_KHOAN = "Tài Khoản";
    public static final String DANG_XUAT = "Đăng Xuất";
    public static final String THU_CUNG = "Thú Cưng";
    public static final String KHACH_HANG = "Khách Hàng";
    public static final String NHAN_VIEN = "Nhân Viên";
    public static final String SAN_PHAM = "Sản Phẩm";
    public static final String DICH_VU = "Dịch Vụ";
    public static final String DANH_MUC_BENH = "Danh Mục Bệnh";
    public static final String CHUONG = "Chuồng Lưu Trữ";
    public static final String HOA_DON = "Hóa Đơn";
    public static final String LICH_HEN = "Lịch Hẹn";
    public static final String TIM_KIEM = "Tìm Kiếm";
    public static final String TK_DOANH_THU = "Thống Kê Doanh Thu";
    public static final String TK_KHACH_HANG = "Thống Kê Khách Hàng";
    public static final String KHUYEN_MAI = "Khuyến Mãi";
    public static final String TK_THU_CUNG = "Thống Kê Thú Cưng";

    private static Set<String> of(String... s) {
        return Collections.unmodifiableSet(new HashSet<>(java.util.Arrays.asList(s)));
    }

    private static final Set<String> CHUNG = of(TRANG_CHU, TAI_KHOAN, DANG_XUAT);

    /** Quyền mặc định (khớp dữ liệu mẫu PetCare.sql) – chỉ dùng khi chạy thử không có CSDL. */
    public static Set<String> macDinh(String tenQuyen) {
        Set<String> r = new HashSet<>(CHUNG);
        if (tenQuyen == null) return r;
        switch (tenQuyen) {
            case "Quản lý" -> r.addAll(of(THU_CUNG, KHACH_HANG, NHAN_VIEN, SAN_PHAM, DICH_VU, DANH_MUC_BENH,
                    CHUONG, HOA_DON, LICH_HEN, TIM_KIEM, TK_DOANH_THU, TK_KHACH_HANG, KHUYEN_MAI, TK_THU_CUNG));
            case "Bán hàng" -> r.addAll(of(THU_CUNG, KHACH_HANG, HOA_DON, TIM_KIEM));
            case "Kho" -> r.addAll(of(SAN_PHAM, TIM_KIEM));
            case "Bác sĩ" -> r.addAll(of(THU_CUNG, DICH_VU, DANH_MUC_BENH, CHUONG, LICH_HEN, TIM_KIEM));
            default -> { }
        }
        return r;
    }
}
