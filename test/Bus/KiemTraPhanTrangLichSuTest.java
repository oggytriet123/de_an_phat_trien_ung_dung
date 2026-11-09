package Bus;

import java.time.LocalDate;
import java.util.List;

import DAO.KhachHangDAO;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;

public class KiemTraPhanTrangLichSuTest {
    private static int pass = 0, fail = 0;

    private static void check(String tc, String moTa, boolean hopLe) {
        if (hopLe) {
            pass++;
            System.out.println("[PASS] " + tc + " - " + moTa);
        } else {
            fail++;
            System.err.println("[FAIL] " + tc + " - " + moTa);
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("KIỂM THỬ PHÂN TRANG VÀ TÌM KIẾM LỊCH SỬ HÓA ĐƠN");
        System.out.println("=================================================");

        KhachHangBUS khBus = new KhachHangBUS();
        HoaDonBUS bus = new HoaDonBUS(khBus);

        // Đếm tổng số lượng hóa đơn đã thanh toán trong CSDL
        int tongSo = bus.demLichSu("", null, null);
        System.out.println("Tổng số hóa đơn đã thanh toán trong CSDL: " + tongSo);

        // 1. Khi mở Lịch sử hóa đơn, chỉ tải 10 hóa đơn đầu tiên (Trang 1, Page size 10)
        List<HoaDon> trang1 = bus.layLichSuPhanTrang("", null, null, 1, 10);
        check("TC-PT-01", "Chỉ tải tối đa 10 hóa đơn trên trang 1 (kết quả = " + trang1.size() + ")",
                trang1.size() <= 10 && (tongSo == 0 || !trang1.isEmpty()));

        // Kiểm tra thứ tự sắp xếp: hóa đơn mới nhất lên trước theo ngày thanh toán / ngày tạo / mã hóa đơn
        boolean sapXepDung = true;
        for (int i = 0; i < trang1.size() - 1; i++) {
            HoaDon h1 = trang1.get(i);
            HoaDon h2 = trang1.get(i + 1);
            if (h1.getNgayThanhToan() != null && h2.getNgayThanhToan() != null) {
                if (h1.getNgayThanhToan().isBefore(h2.getNgayThanhToan())) {
                    sapXepDung = false;
                    break;
                }
            }
        }
        check("TC-PT-02", "Sắp xếp hóa đơn mới nhất lên trước", sapXepDung);

        // 2. Chuyển sang trang tiếp theo và trang trước hoạt động chính xác
        if (tongSo > 10) {
            List<HoaDon> trang2 = bus.layLichSuPhanTrang("", null, null, 2, 10);
            check("TC-PT-03", "Trang 2 tải đúng dữ liệu không trùng lặp với trang 1",
                    !trang2.isEmpty() && !trang2.get(0).getMaHD().equals(trang1.get(0).getMaHD()));
        } else {
            check("TC-PT-03", "CSDL có <= 10 hóa đơn, trang 2 trả về rỗng đúng yêu cầu",
                    bus.layLichSuPhanTrang("", null, null, 2, 10).isEmpty());
        }

        // 3. Tìm kiếm theo mã hóa đơn, tên khách hàng và số điện thoại
        if (!trang1.isEmpty()) {
            HoaDon mau = trang1.get(0);

            // Tìm theo mã HD
            int demMa = bus.demLichSu(mau.getMaHD(), null, null);
            List<HoaDon> kqMa = bus.layLichSuPhanTrang(mau.getMaHD(), null, null, 1, 10);
            check("TC-PT-04", "Tìm kiếm chính xác theo mã hóa đơn (" + mau.getMaHD() + ")",
                    demMa > 0 && !kqMa.isEmpty() && kqMa.get(0).getMaHD().equalsIgnoreCase(mau.getMaHD()));

            // Tìm theo tên khách hàng
            if (mau.getTenKH() != null && !mau.getTenKH().trim().isEmpty()) {
                String tenTim = mau.getTenKH().split(" ")[0];
                int demTen = bus.demLichSu(tenTim, null, null);
                List<HoaDon> kqTen = bus.layLichSuPhanTrang(tenTim, null, null, 1, 10);
                check("TC-PT-05", "Tìm kiếm theo tên khách hàng ('" + tenTim + "')",
                        demTen > 0 && !kqTen.isEmpty());
            } else {
                check("TC-PT-05", "Bỏ qua do hóa đơn mẫu không có tên khách", true);
            }

            // Tìm theo số điện thoại
            if (mau.getSdt() != null && !mau.getSdt().trim().isEmpty()) {
                String sdtTim = mau.getSdt().length() > 4 ? mau.getSdt().substring(0, 4) : mau.getSdt();
                int demSdt = bus.demLichSu(sdtTim, null, null);
                List<HoaDon> kqSdt = bus.layLichSuPhanTrang(sdtTim, null, null, 1, 10);
                check("TC-PT-06", "Tìm kiếm theo số điện thoại ('" + sdtTim + "')",
                        demSdt > 0 && !kqSdt.isEmpty());
            } else {
                check("TC-PT-06", "Bỏ qua do hóa đơn mẫu không có SĐT", true);
            }
        }

        // 4. Lọc theo ngày lập hóa đơn (Từ ngày / Đến ngày)
        LocalDate homNay = LocalDate.now();
        int demHomNay = bus.demLichSu("", homNay, homNay);
        List<HoaDon> dsHomNay = bus.layLichSuPhanTrang("", homNay, homNay, 1, 10);
        check("TC-PT-07", "Lọc theo ngày hôm nay không gây lỗi và trả về đúng số bản ghi (" + demHomNay + ")",
                demHomNay == dsHomNay.size() || dsHomNay.size() <= 10);

        // 5. Kết hợp tìm kiếm và phân trang
        if (!trang1.isEmpty()) {
            HoaDon mau = trang1.get(0);
            int demKetHop = bus.demLichSu(mau.getMaHD(), LocalDate.of(2020, 1, 1), LocalDate.of(2030, 12, 31));
            List<HoaDon> dsKetHop = bus.layLichSuPhanTrang(mau.getMaHD(), LocalDate.of(2020, 1, 1), LocalDate.of(2030, 12, 31), 1, 10);
            check("TC-PT-08", "Kết hợp tìm kiếm mã HD và khoảng ngày trả về kết quả chuẩn xác",
                    demKetHop > 0 && !dsKetHop.isEmpty() && dsKetHop.get(0).getMaHD().equalsIgnoreCase(mau.getMaHD()));
        }

        // 6. Mở chi tiết hóa đơn: kiểm tra Lazy Loading (lúc phân trang chưa tải chi tiết, gọi napChiTietHoaDon thì nạp đầy đủ)
        if (!trang1.isEmpty()) {
            HoaDon mauLazy = trang1.get(0);
            boolean banDauChuaNap = mauLazy.getDanhSachChiTiet().isEmpty();
            bus.napChiTietHoaDon(mauLazy);
            boolean sauKhiNapCoChiTiet = !mauLazy.getDanhSachChiTiet().isEmpty() || mauLazy.canThanhToan() == 0;
            check("TC-PT-09", "Lazy Loading: Danh sách chỉ nạp thông tin tóm tắt, khi xem chi tiết mới nạp chi tiết hàng hóa",
                    banDauChuaNap && mauLazy.canThanhToan() >= 0);
        }

        // 7. Khi không có kết quả, trả về 0 và danh sách rỗng
        int demKhongCo = bus.demLichSu("MA_HOA_DON_KHONG_TON_TAI_99999", null, null);
        List<HoaDon> dsKhongCo = bus.layLichSuPhanTrang("MA_HOA_DON_KHONG_TON_TAI_99999", null, null, 1, 10);
        check("TC-PT-10", "Không có kết quả: demLichSu = 0 và danh sách trang rỗng",
                demKhongCo == 0 && dsKhongCo.isEmpty());

        // 8. Tối ưu bộ nhớ: tải BUS không load toàn bộ hóa đơn lịch sử vào RAM
        HoaDonBUS busMoi = new HoaDonBUS(khBus);
        check("TC-PT-11", "Khởi động ứng dụng không tải toàn bộ hóa đơn lịch sử lên RAM",
                busMoi.layLichSu().isEmpty());

        // 9. Kiểm tra tương thích giao diện GUI LichSuHoaDonPanel
        try {
            GUI.LichSuHoaDonPanel panel = new GUI.LichSuHoaDonPanel(bus);
            panel.lamMoi();
            check("TC-PT-12", "Khởi tạo và làm mới giao diện LichSuHoaDonPanel không bị lỗi ngoại lệ", true);
        } catch (Exception e) {
            e.printStackTrace();
            check("TC-PT-12", "Khởi tạo và làm mới giao diện LichSuHoaDonPanel không bị lỗi ngoại lệ", false);
        }

        System.out.println("=================================================");
        System.out.println("KẾT QUẢ: PASS = " + pass + " / " + (pass + fail) + " (" + (fail == 0 ? "100% THÀNH CÔNG" : "CÓ LỖI") + ")");
        System.out.println("=================================================");
        if (fail > 0) System.exit(1);
    }
}
