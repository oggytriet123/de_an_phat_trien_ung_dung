package Bus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.ChiTietDonThuoc;
import Entity.DonThuoc;

/**
 * Nghiệp vụ quản lý Đơn thuốc phòng khám thú y.
 */
public class DonThuocBUS {
    private final List<DonThuoc> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public DonThuocBUS() {
        khoiTaoDuLieuMau();
    }

    private void khoiTaoDuLieuMau() {
        if (!ds.isEmpty()) return;

        // DT-001: Milu (PET002) - Hậu phẫu sỏi bàng quang PT-001
        DonThuoc dt1 = new DonThuoc("DT-001", "PET002", "Milu", "Chó", "Golden Retriever",
                28.5, "Lê Thị Cẩm Tú", "0938222111", "BS. Võ Thanh Hùng",
                LocalDate.of(2026, 10, 16), LocalTime.of(8, 30),
                "Hậu phẫu lấy sỏi bàng quang (PT-001) & nhiễm khuẩn tiết niệu",
                "Tiểu buốt, tiểu rắt, nước tiểu đục sau phẫu thuật sỏi",
                DonThuoc.DANG_DIEU_TRI,
                "Uống thuốc sau ăn no, uống nhiều nước lọc (1.5L/ngày), kiêng thức ăn mặn và hải sản",
                LocalDate.of(2026, 10, 23), "PD-001");
        dt1.themChiTiet(new ChiTietDonThuoc("YT-001", "Amoxicillin + Clavulanic 250mg", "viên", 14, 15000, "Uống 1 viên/lần, ngày 2 lần sáng - chiều sau ăn 30 phút"));
        dt1.themChiTiet(new ChiTietDonThuoc("YT-002", "Meloxicam kháng viêm giảm đau 0.5mg", "viên", 7, 12000, "Uống 1 viên/ngày vào buổi sáng sau ăn"));
        dt1.themChiTiet(new ChiTietDonThuoc("YT-004", "Dung dịch xịt sát trùng Betadine Vet 100ml", "chai", 1, 75000, "Xịt vệ sinh mép vết mổ 2 lần/ngày, giữ vết mổ khô ráo"));
        dt1.themChiTiet(new ChiTietDonThuoc("SP008", "Gel dinh dưỡng cho chó mèo Nourse 120g", "tuýp", 1, 180000, "Ăn 5cm gel/ngày giúp bồi bổ thể lực hậu phẫu"));
        dt1.ghiLog("Đã tạo đơn thuốc hậu phẫu liên kết với phác đồ PD-001 bởi BS. Võ Thanh Hùng");
        dt1.ghiLog("Đã xuất kho và cấp phát thuốc cho chủ nuôi Lê Thị Cẩm Tú");
        ds.add(dt1);

        // DT-002: Buddy (PET007) - Viêm da cơ địa, đang lưu trú C-101
        DonThuoc dt2 = new DonThuoc("DT-002", "PET007", "Buddy", "Chó", "Poodle Toy",
                4.5, "Hoàng Minh Tuấn", "0945678901", "BS. Nguyễn Hoài Anh",
                LocalDate.of(2026, 10, 20), LocalTime.of(9, 15),
                "Viêm da cơ địa dị ứng, nhiễm nấm Microsporum canis",
                "Ngứa dữ dội, gãi rụng lông từng mảng tròn ở lưng và vành tai",
                DonThuoc.DANG_DIEU_TRI,
                "Đeo loa chống liếm 24/24, tắm bằng sữa tắm thuốc 2 lần/tuần theo phác đồ",
                LocalDate.of(2026, 10, 27), "PD-002");
        dt2.themChiTiet(new ChiTietDonThuoc("SP004", "Sữa tắm trị ve rận Bio Derma 250ml", "chai", 1, 95000, "Tắm 2 lần/tuần, xoa kỹ bọt ủ 10 phút rồi xả sạch nước ấm"));
        dt2.themChiTiet(new ChiTietDonThuoc("YT-005", "Kem bôi da trị nấm Dermaleen 20g", "tuýp", 2, 85000, "Bôi lớp mỏng lên vùng da tổn thương 2 lần/ngày sáng - tối"));
        dt2.themChiTiet(new ChiTietDonThuoc("YT-007", "Kháng Histamin chống dị ứng Cetirizine 5mg", "viên", 10, 6000, "Uống 1/2 viên/ngày vào buổi tối giúp giảm ngứa"));
        dt2.ghiLog("Tạo đơn thuốc điều trị viêm da liên kết với phác đồ PD-002");
        dt2.ghiLog("Đã cấp phát thuốc và chuyển giao quy trình cho khu lưu trú C-101");
        ds.add(dt2);

        // DT-003: Rocky (PET004) - Hậu phẫu kết hợp xương đùi gãy PT-003
        DonThuoc dt3 = new DonThuoc("DT-003", "PET004", "Rocky", "Chó", "Rottweiler",
                35.0, "Võ Hoàng Long", "0386909808", "BS. Võ Thanh Hùng",
                LocalDate.of(2026, 10, 21), LocalTime.of(10, 0),
                "Hậu phẫu kết hợp xương đùi gãy (PT-003), phòng ngừa nhiễm trùng huyết",
                "Sưng nề vùng đùi phải, hạn chế vận động, đau khi cử động chi",
                DonThuoc.CHO_CAP_THUOC,
                "Bất động tuyệt đối, nhốt chuồng lưu trú tĩnh dưỡng, thay gạc 2 ngày/lần",
                LocalDate.of(2026, 10, 28), "PD-003");
        dt3.themChiTiet(new ChiTietDonThuoc("YT-008", "Cefalexin 500mg kháng sinh phổ rộng", "viên", 20, 18000, "Uống 1 viên/lần, ngày 2 lần sáng - tối sau ăn no"));
        dt3.themChiTiet(new ChiTietDonThuoc("YT-002", "Meloxicam kháng viêm giảm đau 0.5mg", "viên", 14, 12000, "Uống 2 viên/ngày vào buổi sáng sau ăn no"));
        dt3.themChiTiet(new ChiTietDonThuoc("YT-009", "Canxi nano sinh học Pet Cal Plus", "lọ", 1, 220000, "Uống 2 viên/ngày kích thích liền can xương sớm"));
        dt3.ghiLog("Tạo đơn thuốc kháng sinh & giảm đau hậu phẫu kết hợp xương bởi BS. Võ Thanh Hùng");
        ds.add(dt3);

        // DT-004: Kitty (PET001) - Rối loạn tiêu hóa cấp
        DonThuoc dt4 = new DonThuoc("DT-004", "PET001", "Kitty", "Mèo", "Anh lông ngắn",
                4.2, "Nguyễn Minh Anh", "0903123456", "BS. Ngô Gia Bảo",
                LocalDate.of(2026, 10, 15), LocalTime.of(14, 0),
                "Viêm ruột cấp tính, rối loạn tiêu hóa nhẹ do đổi thức ăn",
                "Nôn dịch bọt trắng 1 lần, phân lỏng nhẹ, ăn kém",
                DonThuoc.HOAN_TAT,
                "Cho ăn thức ăn mềm dễ tiêu, chia nhỏ 4 bữa/ngày, uống đủ nước",
                LocalDate.of(2026, 10, 19), "");
        dt4.themChiTiet(new ChiTietDonThuoc("YT-003", "Men tiêu hóa vi sinh sống Probiotic Bio-Scour", "gói", 10, 20000, "Hòa 1 gói vào 10ml nước ấm cho uống ngày 2 lần"));
        dt4.themChiTiet(new ChiTietDonThuoc("SP007", "Dung dịch nhỏ mắt cho thú cưng Bioline 50ml", "lọ", 1, 65000, "Nhỏ 2 giọt/mắt/lần, ngày 2 lần vệ sinh gỉ mắt"));
        dt4.ghiLog("Đã tạo đơn thuốc khám bệnh ngoại trú");
        dt4.ghiLog("Đã cấp phát đầy đủ thuốc cho chủ nuôi Nguyễn Minh Anh");
        dt4.ghiLog("Tái khám ngày 19/10: Bé Kitty đã hoàn toàn khỏe mạnh, phân khuôn đẹp");
        ds.add(dt4);

        // DT-005: Mimi (PET005) - Hậu phẫu triệt sản nội soi PT-002
        DonThuoc dt5 = new DonThuoc("DT-005", "PET005", "Mimi", "Mèo", "Xiêm (Siamese)",
                3.2, "Võ Hoàng Long", "0386909808", "BS. Ngô Gia Bảo",
                LocalDate.of(2026, 10, 19), LocalTime.of(11, 30),
                "Chăm sóc và giảm đau hậu phẫu triệt sản nội soi (PT-002)",
                "Vết mổ nội soi 1cm khô ráo, thân nhiệt ổn định 38.4°C",
                DonThuoc.DA_CAP_PHAT,
                "Kiểm tra mép vết mổ hàng ngày, không để liếm vết thương, cắt chỉ sau 7 ngày",
                LocalDate.of(2026, 10, 26), "");
        dt5.themChiTiet(new ChiTietDonThuoc("YT-001", "Amoxicillin + Clavulanic 125mg", "viên", 8, 10000, "Uống 1/2 viên/lần, ngày 2 lần sau ăn no"));
        dt5.themChiTiet(new ChiTietDonThuoc("YT-004", "Dung dịch xịt sát trùng Betadine Vet 100ml", "chai", 1, 75000, "Xịt sát khuẩn vết mổ 1 lần/ngày vào buổi tối"));
        dt5.ghiLog("Kê đơn thuốc hậu phẫu triệt sản nội soi cho bé Mimi");
        dt5.ghiLog("Đã xuất kho và cấp phát thuốc cho chủ nuôi");
        ds.add(dt5);
    }

    public void addListener(Runnable r) {
        if (r != null && !listeners.contains(r)) listeners.add(r);
    }

    public void notifyListeners() {
        for (Runnable r : listeners) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }

    public List<DonThuoc> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public DonThuoc timTheoMa(String ma) {
        if (ma == null) return null;
        for (DonThuoc dt : ds) {
            if (dt.getMaDonThuoc().equalsIgnoreCase(ma.trim())) return dt;
        }
        return null;
    }

    public synchronized String taoMaMoi() {
        int max = 0;
        for (DonThuoc dt : ds) {
            try {
                String s = dt.getMaDonThuoc().replaceAll("\\D", "");
                if (!s.isEmpty()) max = Math.max(max, Integer.parseInt(s));
            } catch (Exception ignored) {}
        }
        return String.format("DT-%03d", max + 1);
    }

    public DonThuoc them(DonThuoc dt) {
        if (dt.getMaDonThuoc() == null || dt.getMaDonThuoc().trim().isEmpty()) {
            dt.setMaDonThuoc(taoMaMoi());
        }
        dt.ghiLog("Tạo mới đơn thuốc bởi " + dt.getBacSi());
        ds.add(0, dt);
        notifyListeners();
        return dt;
    }

    public boolean capNhat(DonThuoc dt) {
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getMaDonThuoc().equalsIgnoreCase(dt.getMaDonThuoc())) {
                dt.ghiLog("Cập nhật thông tin đơn thuốc");
                ds.set(i, dt);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public boolean capPhatThuoc(String maDT) {
        return capPhatThuoc(maDT, null);
    }

    public boolean capPhatThuoc(String maDT, KhoHangBUS khoHangBus) {
        DonThuoc dt = timTheoMa(maDT);
        if (dt != null) {
            dt.setTrangThai(DonThuoc.DA_CAP_PHAT);
            dt.ghiLog("Đã xuất kho và cấp phát thuốc cho chủ nuôi");

            if (khoHangBus != null && dt.getDanhSachThuoc() != null) {
                for (ChiTietDonThuoc ct : dt.getDanhSachThuoc()) {
                    if (ct.getMaSKU() != null && !ct.getMaSKU().trim().isEmpty()) {
                        khoHangBus.xuatKho(ct.getMaSKU(), ct.getSoLuong());
                    }
                }
            }

            notifyListeners();
            return true;
        }
        return false;
    }

    public List<DonThuoc> layDonThuocCuaThuCung(String maTC) {
        List<DonThuoc> res = new ArrayList<>();
        if (maTC == null || maTC.trim().isEmpty()) return res;
        for (DonThuoc dt : ds) {
            if (maTC.trim().equalsIgnoreCase(dt.getMaTC())) {
                res.add(dt);
            }
        }
        return res;
    }

    public DonThuoc timTheoPhacDo(String maPD) {
        if (maPD == null || maPD.trim().isEmpty()) return null;
        for (DonThuoc dt : ds) {
            if (maPD.trim().equalsIgnoreCase(dt.getMaPhacDo())) return dt;
        }
        return null;
    }

    public boolean huyDon(String maDT, String lyDo) {
        DonThuoc dt = timTheoMa(maDT);
        if (dt != null) {
            dt.setTrangThai(DonThuoc.DA_HUY);
            dt.ghiLog("Hủy đơn thuốc. Lý do: " + (lyDo == null || lyDo.isEmpty() ? "Không có" : lyDo));
            notifyListeners();
            return true;
        }
        return false;
    }

    public boolean xoa(String maDT) {
        boolean ok = ds.removeIf(d -> d.getMaDonThuoc().equalsIgnoreCase(maDT));
        if (ok) notifyListeners();
        return ok;
    }

    // --- Thống kê ---
    public int getTongSoDon() { return ds.size(); }

    public int getSoChoCapThuoc() {
        int cnt = 0;
        for (DonThuoc d : ds) if (DonThuoc.CHO_CAP_THUOC.equals(d.getTrangThai())) cnt++;
        return cnt;
    }

    public int getSoDangDieuTri() {
        int cnt = 0;
        for (DonThuoc d : ds) {
            if (DonThuoc.DANG_DIEU_TRI.equals(d.getTrangThai()) || DonThuoc.DA_CAP_PHAT.equals(d.getTrangThai())) cnt++;
        }
        return cnt;
    }

    public int getSoHoanTat() {
        int cnt = 0;
        for (DonThuoc d : ds) if (DonThuoc.HOAN_TAT.equals(d.getTrangThai())) cnt++;
        return cnt;
    }

    public long getTongDoanhThu() {
        long sum = 0;
        for (DonThuoc d : ds) {
            if (!DonThuoc.DA_HUY.equals(d.getTrangThai())) sum += d.tinhTongTien();
        }
        return sum;
    }
}
