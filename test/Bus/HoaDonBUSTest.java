package Bus;

import java.util.List;

import Entity.HoaDon;
import Entity.SanPham;

/**
 * Kiểm thử logic nghiệp vụ HoaDonBUS (không cần JUnit, chạy bằng main).
 * Chạy được cả khi chưa có SQL Server: KhachHangBUS/HoaDonBUS tự rơi về bộ nhớ, lỗi ghi CSDL chỉ được in ra.
 *
 * Biên dịch + chạy: xem docs/HoaDonBUS_TestCases.md
 */
public class HoaDonBUSTest {
    private static int pass = 0, fail = 0;

    private static void check(String id, String mota, boolean ok) {
        if (ok) pass++; else fail++;
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + id + " - " + mota);
    }

    private static HoaDon hoaDon(HoaDonBUS bus, int o, String ten, String sdt, boolean coHang) {
        HoaDon h = bus.taoNhap(o);
        h.setTenKH(ten);
        h.setSdt(sdt);
        if (coHang) h.them(new SanPham("SP-TEST", "Hạt cho chó", "Thức ăn", 100000L, false));
        return h;
    }

    private static Class<?> loaiNgoaiLe(Runnable r) {
        try { r.run(); return null; } catch (RuntimeException e) { return e.getClass(); }
    }

    public static void main(String[] args) {
        KhachHangBUS kh = new KhachHangBUS();
        kh.them("Nguyễn Văn An", "0901234567", "Thường", "");      // khách đã đăng ký duy nhất
        int soKhachBanDau = kh.layDanhSach().size();

        HoaDonBUS bus = new HoaDonBUS(kh);
        bus.datNhanVienDangNhap("NV002");

        // ---- Nhóm 1: khách hàng phải đã đăng ký, không tự tạo mới
        HoaDon a = hoaDon(bus, 0, "Trần Thị Bình", "0912345678", true);
        String loi = bus.kiemTra(a);
        check("TC01", "SĐT chưa đăng ký -> kiemTra báo 'chưa đăng ký'", loi != null && loi.contains("chưa đăng ký"));
        check("TC02", "SĐT chưa đăng ký -> luu() ném IllegalArgumentException",
                loaiNgoaiLe(() -> bus.luu(a)) == IllegalArgumentException.class);
        check("TC03", "Lưu hóa đơn khách lạ KHÔNG tạo thêm khách hàng", kh.layDanhSach().size() == soKhachBanDau);
        check("TC04", "Lưu thất bại -> ô hóa đơn vẫn trống", bus.layO(0) == null);

        HoaDon b = hoaDon(bus, 1, "Lê Văn Cường", "0901234567", true);
        loi = bus.kiemTra(b);
        check("TC05", "SĐT đã đăng ký cho khách khác tên -> báo lỗi", loi != null && loi.contains("đã thuộc khách hàng"));

        HoaDon c = hoaDon(bus, 2, "nguyen van an", "0901 234 567", true);
        check("TC06", "Đúng khách đã đăng ký (khác hoa/thường, dấu, định dạng SĐT) -> hợp lệ", bus.kiemTra(c) == null);

        // ---- Nhóm 2: gắn khách hàng + nhân viên đăng nhập
        bus.luu(c);
        HoaDon daLuu = bus.layO(2);
        check("TC07", "Lưu thành công -> ô được giữ hóa đơn", daLuu != null);
        check("TC08", "Hóa đơn gắn mã khách hàng của khách đã đăng ký",
                daLuu != null && daLuu.getMaKH().equals(kh.timTheoSdtAnToan("0901234567").getMaKH()));
        check("TC09", "Hóa đơn gắn nhân viên từ phiên đăng nhập (NV002)", daLuu != null && "NV002".equals(daLuu.getMaNV()));
        check("TC10", "Lưu hóa đơn hợp lệ vẫn không tạo thêm khách hàng", kh.layDanhSach().size() == soKhachBanDau);
        check("TC11", "Bản sao (moNhap) giữ nguyên nhân viên lập", "NV002".equals(bus.moNhap(2).getMaNV()));

        HoaDonBUS chuaDangNhap = new HoaDonBUS(kh);
        HoaDon d = hoaDon(chuaDangNhap, 3, "Nguyễn Văn An", "0901234567", true);
        check("TC12", "Chưa có nhân viên đăng nhập -> luu() ném IllegalStateException",
                loaiNgoaiLe(() -> chuaDangNhap.luu(d)) == IllegalStateException.class);

        bus.datNhanVienDangNhap("NV005");
        HoaDon e = hoaDon(bus, 4, "Nguyễn Văn An", "0901234567", true);
        bus.luu(e);
        check("TC13", "Đổi người đăng nhập -> hóa đơn mới gắn nhân viên mới (NV005)", "NV005".equals(bus.layO(4).getMaNV()));
        check("TC14", "Hóa đơn cũ giữ nguyên nhân viên lập (NV002)", "NV002".equals(bus.layO(2).getMaNV()));

        // ---- Nhóm 3: dữ liệu đầu vào khác
        check("TC15", "Giỏ hàng trống -> báo lỗi", bus.kiemTra(hoaDon(bus, 5, "Nguyễn Văn An", "0901234567", false)) != null);
        check("TC16", "Tên rỗng -> báo lỗi", bus.kiemTra(hoaDon(bus, 5, "", "0901234567", true)) != null);
        check("TC17", "SĐT sai định dạng -> báo lỗi", bus.kiemTra(hoaDon(bus, 5, "Nguyễn Văn An", "123", true)) != null);

        // ---- Nhóm 4: thanh toán
        HoaDon f = bus.moNhap(2);
        check("TC18", "Phương thức thanh toán không hợp lệ -> IllegalArgumentException",
                loaiNgoaiLe(() -> bus.thanhToan(f, "Bitcoin")) == IllegalArgumentException.class);
        bus.thanhToan(f, "Tiền mặt");
        check("TC19", "Thanh toán -> ô trống lại, hóa đơn vào lịch sử", bus.layO(2) == null && !bus.layLichSu().isEmpty());
        List<HoaDon> ls = bus.layLichSu();
        bus.datNhanVienDangNhap("NV009");
        HoaDon g = hoaDon(bus, 6, "Nguyễn Văn An", "0901234567", true);
        bus.datNhanVienDangNhap("NV002");
        bus.luu(g);
        bus.datNhanVienDangNhap("NV009");
        HoaDon g2 = bus.moNhap(6);
        bus.luu(g2);
        check("TC20", "Nhân viên khác sửa hóa đơn đã lưu -> vẫn giữ người lập ban đầu (NV002)", "NV002".equals(bus.layO(6).getMaNV()));
        check("TC21", "Hóa đơn đã thanh toán giữ khách hàng và nhân viên lập",
                ls.get(0).daThanhToan() && "NV002".equals(ls.get(0).getMaNV()) && !ls.get(0).getMaKH().isEmpty());

        System.out.println("\nKết quả: " + pass + " đạt, " + fail + " lỗi / " + (pass + fail) + " test case");
        System.exit(fail == 0 ? 0 : 1);
    }
}
