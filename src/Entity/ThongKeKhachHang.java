package Entity;

/** Số liệu tổng hợp hiển thị ở 4 thẻ thống kê của màn hình Khách hàng. */
public class ThongKeKhachHang {
    public final int tongKhach;
    public final int khachMoiThang;
    public final int soVip;
    public final long diemLuuHanh;
    public final long doanhThuThang;      // doanh thu từ hóa đơn của khách hàng trong tháng hiện tại
    public final long doanhThuThangTruoc; // cùng chỉ số của tháng trước

    public ThongKeKhachHang(int tongKhach, int khachMoiThang, int soVip, long diemLuuHanh,
                            long doanhThuThang, long doanhThuThangTruoc) {
        this.tongKhach = tongKhach;
        this.khachMoiThang = khachMoiThang;
        this.soVip = soVip;
        this.diemLuuHanh = diemLuuHanh;
        this.doanhThuThang = doanhThuThang;
        this.doanhThuThangTruoc = doanhThuThangTruoc;
    }
}
