package Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hóa đơn: thông tin khách hàng, nhân viên lập hóa đơn, danh sách chi tiết hóa đơn,
 * mã giảm giá và trạng thái thanh toán.
 * Thiết kế chuẩn hướng đối tượng (OOP) và ánh xạ quan hệ cơ sở dữ liệu (HOADON, KHACHHANG, NHANVIEN, CHITIETHOADON).
 */
public class HoaDon {
    public static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    public static final String DA_THANH_TOAN = "Đã thanh toán";
    public static final int SO_LUONG_TOI_DA = 99;

    private final String maHD;
    private int soO;                                    // ô đang giữ hóa đơn (1..20)

    // Các đối tượng thực thể quan hệ theo mô hình OOP & CSDL
    private KhachHang khachHang;                        // Đối tượng Khách hàng
    private NhanVien nhanVien;                          // Đối tượng Nhân viên lập hóa đơn
    private final List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>(); // Danh sách Chi tiết hóa đơn (thay thế DongGioHang)

    // Thuộc tính phụ / lưu vết chuỗi
    private String tenKH = "";
    private String sdt = "";
    private String maKH = "";
    private String maNV = "";

    private MaGiamGia maGiamGia;
    private String trangThai = CHUA_THANH_TOAN;
    private String phuongThuc;
    private LocalDateTime ngayTao = LocalDateTime.now();
    private LocalDateTime ngayThanhToan;
    private boolean preOrder = false;
    private long tienKhachDua = 0;
    private long tienTraLai = 0;
    private Double tongTienLuu;

    public HoaDon(String maHD, int soO) {
        this.maHD = maHD;
        this.soO = soO;
    }

    public HoaDon(String maHD, int soO, KhachHang khachHang, NhanVien nhanVien) {
        this.maHD = maHD;
        this.soO = soO;
        setKhachHang(khachHang);
        setNhanVien(nhanVien);
    }

    /** Bản sao độc lập (kể cả các chi tiết hóa đơn) để chỉnh sửa mà chưa ảnh hưởng bản đã lưu. */
    public HoaDon saoChep() {
        HoaDon h = new HoaDon(maHD, soO);
        h.khachHang = khachHang;
        h.nhanVien = nhanVien;
        h.tenKH = getTenKH();
        h.sdt = getSdt();
        h.maKH = getMaKH();
        h.maNV = getMaNV();
        for (ChiTietHoaDon d : danhSachChiTiet) {
            ChiTietHoaDon copy = d.saoChep();
            copy.setHoaDon(h);
            h.danhSachChiTiet.add(copy);
        }
        h.maGiamGia = maGiamGia;
        h.trangThai = trangThai;
        h.phuongThuc = phuongThuc;
        h.ngayTao = ngayTao;
        h.ngayThanhToan = ngayThanhToan;
        h.preOrder = preOrder;
        h.tienKhachDua = tienKhachDua;
        h.tienTraLai = tienTraLai;
        h.tongTienLuu = tongTienLuu;
        return h;
    }

    // ---- Chi tiết hóa đơn (thay thế cho DongGioHang)
    public List<ChiTietHoaDon> getDanhSachChiTiet() { return danhSachChiTiet; }

    /** Alias tương thích ngược với các module gọi getDong() */
    public List<ChiTietHoaDon> getDong() { return danhSachChiTiet; }

    public boolean trong() { return danhSachChiTiet.isEmpty(); }

    public void them(SanPham sp) {
        if (sp == null) return;
        for (ChiTietHoaDon d : danhSachChiTiet) {
            if (d.getSanPham() != null && d.getSanPham().getSku().equals(sp.getSku())) {
                d.setSoLuong(Math.min(SO_LUONG_TOI_DA, d.getSoLuong() + 1));
                d.tinhThanhTien();
                return;
            }
        }
        ChiTietHoaDon ct = new ChiTietHoaDon(sp, 1);
        ct.setHoaDon(this);
        danhSachChiTiet.add(ct);
    }

    public void themChiTiet(ChiTietHoaDon ct) {
        if (ct == null) return;
        ct.setHoaDon(this);
        for (ChiTietHoaDon d : danhSachChiTiet) {
            if (d.getSanPham() != null && ct.getSanPham() != null
                    && d.getSanPham().getSku().equals(ct.getSanPham().getSku())) {
                d.setSoLuong(Math.min(SO_LUONG_TOI_DA, d.getSoLuong() + ct.getSoLuong()));
                d.tinhThanhTien();
                return;
            }
        }
        danhSachChiTiet.add(ct);
    }

    /** Tăng/giảm số lượng; giảm về 0 thì xóa dòng. Số lượng luôn nằm trong 1..99. */
    public void doiSoLuong(String sku, int delta) {
        for (int i = 0; i < danhSachChiTiet.size(); i++) {
            ChiTietHoaDon d = danhSachChiTiet.get(i);
            if (d.getSanPham() == null || !d.getSanPham().getSku().equals(sku)) continue;
            int sl = Math.min(SO_LUONG_TOI_DA, d.getSoLuong() + delta);
            if (sl <= 0) danhSachChiTiet.remove(i);
            else {
                d.setSoLuong(sl);
                d.tinhThanhTien();
            }
            return;
        }
    }

    // ---- Tiền
    public long tamTinh() {
        long t = 0;
        for (ChiTietHoaDon d : danhSachChiTiet) t += d.thanhTien();
        return t;
    }

    public long tienGiam() {
        return maGiamGia == null ? 0 : maGiamGia.tinhGiam(tamTinh());
    }

    public void setTongTien(double tongTien) {
        this.tongTienLuu = (double) Math.round(tongTien);
    }

    /** Cần thanh toán = tổng hàng - tiền giảm giá. Khi chưa tải chi tiết, dùng tổng tiền lưu từ CSDL. */
    public long canThanhToan() {
        if (danhSachChiTiet.isEmpty() && tongTienLuu != null && tongTienLuu >= 0) {
            return tongTienLuu.longValue();
        }
        return tamTinh() - tienGiam();
    }

    // ---- Getter / setter cho đối tượng KhachHang & NhanVien
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang kh) {
        this.khachHang = kh;
        if (kh != null) {
            this.maKH = kh.getMaKH() != null ? kh.getMaKH() : "";
            this.tenKH = kh.getHoTen() != null ? kh.getHoTen() : "";
            this.sdt = kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "";
        }
    }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nv) {
        this.nhanVien = nv;
        if (nv != null && nv.getMaNV() != null) {
            this.maNV = nv.getMaNV();
        }
    }

    // ---- Getter / setter thông thường
    public String getMaHD() { return maHD; }
    public int getSoO() { return soO; }

    public String getTenKH() {
        if (khachHang != null && khachHang.getHoTen() != null && !khachHang.getHoTen().isEmpty()) {
            return khachHang.getHoTen();
        }
        return tenKH;
    }

    public String getSdt() {
        if (khachHang != null && khachHang.getSoDienThoai() != null && !khachHang.getSoDienThoai().isEmpty()) {
            return khachHang.getSoDienThoai();
        }
        return sdt;
    }

    public String getMaKH() {
        if (khachHang != null && khachHang.getMaKH() != null && !khachHang.getMaKH().isEmpty()) {
            return khachHang.getMaKH();
        }
        return maKH;
    }

    public String getMaNV() {
        if (nhanVien != null && nhanVien.getMaNV() != null && !nhanVien.getMaNV().isEmpty()) {
            return nhanVien.getMaNV();
        }
        return maNV;
    }

    public MaGiamGia getMaGiamGia() { return maGiamGia; }
    public String getTrangThai() { return trangThai; }
    public String getPhuongThuc() { return phuongThuc; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public boolean daThanhToan() { return DA_THANH_TOAN.equals(trangThai); }
    public boolean isPreOrder() { return preOrder; }
    public void setPreOrder(boolean preOrder) { this.preOrder = preOrder; }

    /** Tính số phút đơn hàng đã chờ trong hàng đợi. */
    public long getSoPhutCho() {
        if (ngayTao == null) return 0;
        return java.time.Duration.between(ngayTao, LocalDateTime.now()).toMinutes();
    }

    /** Kiểm tra xem đơn hàng có bị quá hạn ngưỡng 15 phút chưa thanh toán hay không. */
    public boolean isQuaHan15Phut() {
        return !daThanhToan() && getSoPhutCho() >= 15;
    }

    public void setSoO(int soO) { this.soO = soO; }
    public void setTenKH(String v) {
        this.tenKH = v == null ? "" : v;
        if (khachHang != null && v != null && !v.isEmpty()) {
            khachHang.setHoTen(v);
        }
    }
    public void setSdt(String v) {
        this.sdt = v == null ? "" : v;
        if (khachHang != null && v != null && !v.isEmpty()) {
            khachHang.setSoDienThoai(v);
        }
    }
    public void setMaKH(String v) {
        this.maKH = v == null ? "" : v;
    }
    public void setMaNV(String v) {
        this.maNV = v == null ? "" : v;
        if (nhanVien != null && v != null && !v.isEmpty()) {
            nhanVien.setMaNV(v);
        }
    }
    public void setMaGiamGia(MaGiamGia m) { this.maGiamGia = m; }
    public void setNgayTao(LocalDateTime t) { this.ngayTao = t; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }
    public void setNgayThanhToan(LocalDateTime ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }

    public long getTienKhachDua() {
        if (tienKhachDua <= 0) return canThanhToan();
        return tienKhachDua;
    }

    public void setTienKhachDua(long tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
        this.tienTraLai = Math.max(0, tienKhachDua - canThanhToan());
    }

    public long getTienTraLai() {
        if (tienKhachDua <= 0) return 0;
        return Math.max(0, tienKhachDua - canThanhToan());
    }

    public void setTienTraLai(long tienTraLai) {
        this.tienTraLai = Math.max(0, tienTraLai);
    }

    public void danhDauDaThanhToan(String phuongThuc, LocalDateTime luc) {
        this.trangThai = DA_THANH_TOAN;
        this.phuongThuc = phuongThuc;
        this.ngayThanhToan = luc;
    }
}
