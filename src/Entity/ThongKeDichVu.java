package Entity;

/**
 * Số liệu thống kê dịch vụ cho các thẻ KPI trên giao diện.
 */
public class ThongKeDichVu {
    public final int tongDichVu;
    public final int soSpaGrooming;
    public final int soKhamChua;
    public final int soTiemPhong;
    public final long giaTrungBinh;
    public final int soDangCungCap;

    public ThongKeDichVu(int tongDichVu, int soSpaGrooming, int soKhamChua,
                         int soTiemPhong, long giaTrungBinh, int soDangCungCap) {
        this.tongDichVu = tongDichVu;
        this.soSpaGrooming = soSpaGrooming;
        this.soKhamChua = soKhamChua;
        this.soTiemPhong = soTiemPhong;
        this.giaTrungBinh = giaTrungBinh;
        this.soDangCungCap = soDangCungCap;
    }
}
