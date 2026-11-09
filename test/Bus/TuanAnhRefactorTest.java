package Bus;

import java.time.LocalDate;
import java.util.List;

import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.NhanVien;
import Entity.SanPham;

/**
 * Kiểm tra tái cấu trúc OOP và DB theo yêu cầu của Tuấn Anh:
 * 1. ChiTietHoaDon tương ứng bảng CHITIETHOADON.
 * 2. HoaDon chứa KhachHang, NhanVien và List<ChiTietHoaDon> (thay thế DongGioHang).
 * 3. Tính toán thành tiền, sao chép sâu và getter/setter đồng bộ.
 */
public class TuanAnhRefactorTest {
    private static int pass = 0, fail = 0;

    private static void check(String id, String mota, boolean ok) {
        if (ok) pass++; else fail++;
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + id + " - " + mota);
    }

    public static void main(String[] args) {
        System.out.println("=== KIỂM THỬ TÁI CẤU TRÚC ENTITY & CSDL (TUẤN ANH) ===");

        // 1. Kiểm tra thực thể ChiTietHoaDon
        SanPham sp = new SanPham("SP001", "Thức ăn hạt Royal Canin", "Thức ăn", 350000L, false);
        ChiTietHoaDon ct1 = new ChiTietHoaDon(sp, 2);
        check("TC_CT01", "ChiTietHoaDon khởi tạo từ SanPham", ct1.getSanPham() != null && "SP001".equals(ct1.getSku()));
        check("TC_CT02", "Tính thành tiền tự động: 350.000 * 2 = 700.000", ct1.thanhTien() == 700000L);
        check("TC_CT03", "Đơn giá khớp đơn giá sản phẩm", ct1.getDonGia() == 350000.0);
        check("TC_CT04", "Không phải dịch vụ", !ct1.isDichVu());

        SanPham dv = new SanPham("DV001", "Khám tổng quát", "Khám & Điều trị", 200000L, true);
        ChiTietHoaDon ct2 = new ChiTietHoaDon("CT002", dv, 1, 200000.0);
        check("TC_CT05", "ChiTietHoaDon dịch vụ nhận biết maDV và isDichVu", ct2.isDichVu() && "DV001".equals(ct2.getMaDV()));

        // 2. Kiểm tra thực thể HoaDon chứa KhachHang, NhanVien, List<ChiTietHoaDon>
        KhachHang kh = new KhachHang("KH001", "Nguyễn Văn An", "0901234567", "Vàng", 250, "Mèo Miu", LocalDate.now(), 5000000L);
        NhanVien nv = new NhanVien();
        nv.setMaNV("NV002");
        nv.setHoTen("Lê Bán Hàng");

        HoaDon hd = new HoaDon("HD20260309001", 1, kh, nv);
        check("TC_HD01", "HoaDon chứa đối tượng KhachHang", hd.getKhachHang() != null && "KH001".equals(hd.getKhachHang().getMaKH()));
        check("TC_HD02", "HoaDon chứa đối tượng NhanVien", hd.getNhanVien() != null && "NV002".equals(hd.getNhanVien().getMaNV()));
        check("TC_HD03", "Getter maKH, tenKH, sdt đồng bộ từ KhachHang",
                "KH001".equals(hd.getMaKH()) && "Nguyễn Văn An".equals(hd.getTenKH()) && "0901234567".equals(hd.getSdt()));
        check("TC_HD04", "Getter maNV đồng bộ từ NhanVien", "NV002".equals(hd.getMaNV()));

        // Thêm chi tiết hóa đơn
        hd.them(sp);
        hd.themChiTiet(ct2);
        List<ChiTietHoaDon> dsChiTiet = hd.getDanhSachChiTiet();
        check("TC_HD05", "Danh sách chi tiết hóa đơn gồm 2 dòng", dsChiTiet.size() == 2);
        check("TC_HD06", "ChiTietHoaDon gắn ngược liên kết tới HoaDon", dsChiTiet.get(0).getHoaDon() == hd);
        check("TC_HD07", "Tạm tính tổng tiền: 350.000 + 200.000 = 550.000", hd.tamTinh() == 550000L);

        // Tăng số lượng sản phẩm SP001
        hd.doiSoLuong("SP001", 1);
        check("TC_HD08", "Đổi số lượng: 1 + 1 = 2 -> Tạm tính 350k*2 + 200k = 900.000", hd.tamTinh() == 900000L);

        // Sao chép hóa đơn độc lập
        HoaDon banSao = hd.saoChep();
        check("TC_HD09", "Sao chép giữ nguyên KhachHang & NhanVien",
                banSao.getKhachHang() == kh && banSao.getNhanVien() == nv);
        check("TC_HD10", "Sao chép danh sách ChiTietHoaDon độc lập",
                banSao.getDanhSachChiTiet().size() == 2 && banSao.getDanhSachChiTiet() != hd.getDanhSachChiTiet());
        check("TC_HD11", "Chi tiết của bản sao liên kết tới bản sao",
                banSao.getDanhSachChiTiet().get(0).getHoaDon() == banSao);

        System.out.println("\nTổng kết: " + pass + " đạt, " + fail + " lỗi.");
        System.exit(fail == 0 ? 0 : 1);
    }
}
