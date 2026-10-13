package Entity;

/** Một dòng trong giỏ hàng: sản phẩm + số lượng. */
public class DongGioHang {
    private final SanPham sanPham;
    private int soLuong;

    public DongGioHang(SanPham sanPham, int soLuong) {
        this.sanPham = sanPham;
        this.soLuong = soLuong;
    }

    public SanPham getSanPham() { return sanPham; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public long thanhTien() { return sanPham.getDonGia() * soLuong; }
}
