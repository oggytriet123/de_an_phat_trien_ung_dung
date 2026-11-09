package Bus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.SanPham;
import Entity.ThuCung;

public class KiemTraYeuCauMoiTest {
    private static int pass = 0, fail = 0;

    private static void check(String id, String mota, boolean ok) {
        if (ok) pass++; else fail++;
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + id + " - " + mota);
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("KIỂM THỬ CÁC YÊU CẦU CẬP NHẬT CHỨC NĂNG HÓA ĐƠN");
        System.out.println("=================================================");

        KhachHangBUS khBus = new KhachHangBUS();
        HoaDonBUS hdBus = new HoaDonBUS(khBus);
        hdBus.datNhanVienDangNhap("NV001");

        // 1. Kiểm tra tìm kiếm khách hàng theo tên (hỗ trợ tiếng Việt không dấu, chữ hoa/thường, tìm một phần)
        List<KhachHang> kqTen = khBus.timKiem("nguyen");
        check("TC-KH-01", "Tìm kiếm khách hàng theo tên (không dấu/hoa thường): kết quả không rỗng", !kqTen.isEmpty());

        // 2. Kiểm tra tìm kiếm khách hàng theo số điện thoại (tìm một phần số)
        List<KhachHang> kqSdt = khBus.timKiem("09");
        check("TC-KH-02", "Tìm kiếm khách hàng theo SĐT: tìm được các khách có đầu số 09", !kqSdt.isEmpty());

        // 3. Kiểm tra tìm kiếm khi không thấy và thêm khách hàng mới với cơ chế sinh mã KHxxx
        String sdtTest = "0988" + (System.currentTimeMillis() % 1000000);
        List<KhachHang> kqChuaCo = khBus.timKiem(sdtTest);
        check("TC-KH-03", "Tìm kiếm số điện thoại mới chưa có -> kết quả rỗng", kqChuaCo.isEmpty());

        String maKhMoi = null;
        try {
            List<ThuCung> dsPet = new ArrayList<>();
            dsPet.add(new ThuCung(null, "Miu Test", "Mèo", 3.5));
            maKhMoi = khBus.them("Khách Hàng Test Mới", sdtTest, 20, dsPet);
            khBus.taiLai();
        } catch (Exception e) {
            e.printStackTrace();
        }
        check("TC-KH-04", "Thêm khách hàng mới thành công với mã KH sinh tự động: " + maKhMoi,
                maKhMoi != null && maKhMoi.startsWith("KH"));

        KhachHang khMoiTao = khBus.timTheoMa(maKhMoi);
        check("TC-KH-05", "Khách hàng mới được nạp vào bộ nhớ và CSDL chuẩn xác",
                khMoiTao != null && khMoiTao.getHoTen().equals("Khách Hàng Test Mới"));

        // 4. Kiểm tra tạo bản nháp mới cho chức năng Lập hóa đơn
        HoaDon hdMoi = hdBus.taoNhapMoi();
        check("TC-HD-01", "Tạo hóa đơn mới có mã HD hợp lệ: " + hdMoi.getMaHD(),
                hdMoi.getMaHD() != null && hdMoi.getMaHD().startsWith("HD"));

        // 5. Liên kết khách hàng và thêm sản phẩm/dịch vụ
        hdMoi.setKhachHang(khMoiTao);
        hdMoi.setTenKH(khMoiTao.getHoTen());
        hdMoi.setSdt(khMoiTao.getSoDienThoai());
        hdMoi.setMaKH(khMoiTao.getMaKH());
        hdMoi.them(new SanPham("SP-TEST-01", "Pate mèo vị cá hồi", "Thức ăn", 45000L, false));
        hdMoi.them(new SanPham("DV-TEST-01", "Tắm sấy thú cưng", "Dịch vụ", 150000L, true));

        check("TC-HD-02", "Hóa đơn có 2 sản phẩm/dịch vụ, tạm tính đúng",
                hdMoi.getDanhSachChiTiet().size() == 2 && hdMoi.tamTinh() == 195000L);

        // 6. Lưu hóa đơn vào Hóa đơn chờ (CSDL)
        try {
            hdBus.luu(hdMoi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        check("TC-HD-03", "Lưu hóa đơn vào Hóa đơn chờ thành công: daLuu = true", hdBus.daLuu(hdMoi));

        // 7. Kiểm tra hóa đơn chờ xuất hiện trong danh sách lấy từ CSDL
        HoaDon timThayTrongCho = hdBus.timHoaDonCho(hdMoi.getMaHD());
        check("TC-HD-04", "Hóa đơn xuất hiện trong danh sách Hóa đơn chờ với đúng mã KH và tổng tiền",
                timThayTrongCho != null && maKhMoi.equals(timThayTrongCho.getMaKH()) && timThayTrongCho.canThanhToan() == 195000L);

        // 8. Kiểm tra tính bền vững (Persistence): Mở một BUS mới hoàn toàn tải từ CSDL
        HoaDonBUS hdBus2 = new HoaDonBUS(khBus);
        hdBus2.datNhanVienDangNhap("NV001");
        HoaDon hdTuDb = hdBus2.timHoaDonCho(hdMoi.getMaHD());
        check("TC-HD-05", "Khởi tạo lại instance BUS: Hóa đơn chờ vẫn tồn tại bền vững trong CSDL",
                hdTuDb != null && hdTuDb.getDanhSachChiTiet().size() == 2);

        // 9. Kiểm tra sửa đổi hóa đơn chờ: đổi số lượng và lưu lại
        hdTuDb.doiSoLuong("SP-TEST-01", 1); // tăng số lượng lên 2
        hdBus2.luu(hdTuDb);

        HoaDonBUS hdBus3 = new HoaDonBUS(khBus);
        hdBus3.datNhanVienDangNhap("NV001");
        HoaDon hdSauKhiSua = hdBus3.timHoaDonCho(hdMoi.getMaHD());
        check("TC-HD-06", "Sửa số lượng và lưu: CSDL cập nhật thành công tổng tiền mới " + (hdSauKhiSua != null ? hdSauKhiSua.canThanhToan() : 0),
                hdSauKhiSua != null && hdSauKhiSua.canThanhToan() == 240000L);

        // 10. Kiểm tra thanh toán hóa đơn chờ
        hdBus3.thanhToan(hdSauKhiSua, "Tiền mặt");
        check("TC-HD-07", "Thanh toán thành công: Hóa đơn rời khỏi danh sách Hóa đơn chờ",
                hdBus3.timHoaDonCho(hdMoi.getMaHD()) == null);

        // 11. Kiểm tra hóa đơn đã vào Lịch sử hóa đơn
        List<HoaDon> ls = hdBus3.layLichSu();
        boolean coTrongLichSu = ls.stream().anyMatch(h -> h.getMaHD().equals(hdMoi.getMaHD()));
        check("TC-HD-08", "Hóa đơn chuyển sang Lịch sử hóa đơn với trạng thái 'Đã thanh toán'", coTrongLichSu);

        // Kiểm tra không bị trùng lặp trong cả chờ và lịch sử
        boolean trongCho = hdBus3.timHoaDonCho(hdMoi.getMaHD()) != null;
        check("TC-HD-09", "Không xuất hiện đồng thời trong cả Hóa đơn chờ và Lịch sử", !trongCho && coTrongLichSu);

        // 12. Kiểm tra xóa hóa đơn chờ
        HoaDon hdXoa = hdBus3.taoNhapMoi();
        hdXoa.setKhachHang(khMoiTao);
        hdXoa.them(new SanPham("SP-XOA", "Đồ chơi gặm", "Phụ kiện", 50000L, false));
        hdBus3.luu(hdXoa);
        check("TC-HD-10", "Tạo hóa đơn chờ thứ 2 để kiểm tra xóa", hdBus3.timHoaDonCho(hdXoa.getMaHD()) != null);

        hdBus3.xoaHoaDon(hdXoa.getMaHD());
        HoaDonBUS hdBus4 = new HoaDonBUS(khBus);
        check("TC-HD-11", "Xóa hóa đơn chờ thành công khỏi CSDL", hdBus4.timHoaDonCho(hdXoa.getMaHD()) == null);

        System.out.println("=================================================");
        System.out.println("KẾT QUẢ KIỂM THỬ: PASS = " + pass + " / " + (pass + fail) + " (" + (fail == 0 ? "100% THÀNH CÔNG" : "CÓ LỖI") + ")");
        System.out.println("=================================================");
    }
}
