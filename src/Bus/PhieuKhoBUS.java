package Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.PhieuKho;

/**
 * Nghiệp vụ quản lý Lịch sử Nhập / Xuất kho (mock 128 phiếu theo thiết kế).
 */
public class PhieuKhoBUS {
    private final List<PhieuKho> ds = new ArrayList<>();

    public PhieuKhoBUS() {
        napDuLieuMau();
    }

    public List<PhieuKho> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public int getTongSoPhieu() {
        return ds.size();
    }

    private void add(String ma, String loai, String ngay, String gio, String nguoi, String doiTac, long gia, String tt) {
        ds.add(new PhieuKho(ma, loai, ngay, gio, nguoi, doiTac, gia, tt));
    }

    private void napDuLieuMau() {
        // 9 dòng đầu tiên trên trang 1 khớp 100% ảnh thiết kế tham chiếu
        add("PN-2026-0045", "Nhập mới", "04/03/2026", "09:45", "Nguyễn Minh An", "Công Ty Green Pet", 12_800_000, "Hoàn tất");
        add("PX-2026-0042", "Xuất bán", "04/03/2026", "09:30", "Trần Kim Chi", "Chi nhánh PetCare Quận 7", 2_450_000, "Hoàn tất");
        add("PN-2026-0044", "Nhập mới", "03/03/2026", "16:20", "Lê Thu Hà", "Dược Thú Y Hoàng Gia", 8_975_000, "Đang xử lý");
        add("PX-2026-0041", "Xuất bán", "03/03/2026", "14:10", "Phạm Quốc Bảo", "Phòng khám thú y An Việt", 2_350_000, "Hoàn tất");
        add("PN-2026-0043", "Nhập mới", "02/03/2026", "10:00", "Nguyễn Minh An", "Công ty CP Royal Canin VN", 21_000_000, "Hoàn tất");
        add("PX-2026-0040", "Xuất hủy", "02/03/2026", "15:45", "Võ Minh Khang", "Chi nhánh PetCare Thủ Đức", 4_750_000, "Đã hủy");
        add("PN-2026-0042", "Nhập mới", "02/03/2026", "09:15", "Lê Thu Hà", "Thuốc thú y Minh Phương", 6_820_000, "Hoàn tất");
        add("PX-2026-0039", "Xuất bán", "01/03/2026", "11:20", "Trần Kim Chi", "Phòng khám thú y Happy Paws", 1_980_000, "Hoàn tất");
        add("PN-2026-0041", "Nhập mới", "01/03/2026", "08:30", "Phạm Quốc Bảo", "Công Ty TNHH Zoetis Việt Nam", 15_240_000, "Hoàn tất");

        // Sinh dữ liệu các phiếu khác đủ 128 phiếu (15 trang x 9 dòng)
        String[] nhanViens = {"Nguyễn Minh An", "Trần Kim Chi", "Lê Thu Hà", "Phạm Quốc Bảo", "Võ Minh Khang", "Đỗ Hải Đăng"};
        String[] doiTacs = {
            "Công Ty CP PetCare VN", "Công Ty Green Pet", "Chi nhánh PetCare Quận 7", "Dược Thú Y Hoàng Gia",
            "Phòng khám thú y An Việt", "Công ty CP Royal Canin VN", "Chi nhánh PetCare Thủ Đức",
            "Thuốc thú y Minh Phương", "Phòng khám thú y Happy Paws", "Công Ty TNHH Zoetis Việt Nam",
            "Đại lý Thức ăn Chăn nuôi Miền Nam", "Công ty TNHH Virbac Việt Nam", "Nhà thuốc thú y Thảo Nguyên"
        };
        long[] giaTiens = {1_250_000, 3_400_000, 5_800_000, 7_650_000, 11_200_000, 16_500_000, 24_000_000};
        String[] loais = {"Nhập mới", "Xuất bán", "Xuất bán", "Nhập mới", "Xuất hủy"};
        String[] trangThais = {"Hoàn tất", "Hoàn tất", "Hoàn tất", "Đang xử lý", "Đã hủy"};

        int idx = 40;
        while (ds.size() < 128) {
            idx--;
            boolean isNhap = (ds.size() % 2 == 0);
            String ma = String.format("%s-2026-%04d", isNhap ? "PN" : "PX", Math.max(1, idx));
            String loai = isNhap ? "Nhập mới" : (ds.size() % 5 == 0 ? "Xuất hủy" : "Xuất bán");
            int day = Math.max(1, 28 - (ds.size() / 5));
            String ngay = String.format("%02d/02/2026", day);
            String gio = String.format("%02d:%02d", 8 + (ds.size() % 10), (ds.size() * 7) % 60);
            String nv = nhanViens[ds.size() % nhanViens.length];
            String dt = doiTacs[ds.size() % doiTacs.length];
            long gia = giaTiens[ds.size() % giaTiens.length];
            String tt = "Xuất hủy".equals(loai) ? "Đã hủy" : (ds.size() % 7 == 0 ? "Đang xử lý" : "Hoàn tất");
            add(ma, loai, ngay, gio, nv, dt, gia, tt);
        }
    }
}
