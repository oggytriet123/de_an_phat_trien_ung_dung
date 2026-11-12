package Entity;

/**
 * Lựa chọn khách hàng trong combobox — dùng cho form thêm/sửa thú cưng.
 */
public class KhachHangOption {
    public final String maKH;
    public final String hoTen;
    public final String soDienThoai;
    public final String hangThe;

    public KhachHangOption(String maKH, String hoTen, String soDienThoai, String hangThe) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.hangThe = hangThe;
    }

    @Override
    public String toString() {
        String sdtStr = (soDienThoai == null || soDienThoai.isEmpty()) ? "" : " - " + soDienThoai;
        return hoTen + sdtStr + " (" + maKH + ")";
    }
}
