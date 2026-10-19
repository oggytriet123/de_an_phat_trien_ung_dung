package Entity;

public class TaiKhoan {
    private final String maTaiKhoan;
    private final String soDienThoai;
    private final String hoTen;
    private final String vaiTro;

    public TaiKhoan(String maTaiKhoan, String soDienThoai, String hoTen, String vaiTro) {
        this.maTaiKhoan = maTaiKhoan;
        this.soDienThoai = soDienThoai;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
    }

    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getHoTen() { return hoTen; }
    public String getVaiTro() { return vaiTro; }
}
