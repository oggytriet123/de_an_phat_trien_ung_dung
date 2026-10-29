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
        napDuLieuMau();
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

    private void napDuLieuMau() {
        LocalDate nay = LocalDate.now();

        // 1. Milo - Viêm da dị ứng & ngứa
        DonThuoc d1 = new DonThuoc("DT-001", "TC005", "Milo", "Chó", "Corgi", 10.4,
                "Phạm Gia Hân", "0912 345 678", "BS. Nguyễn Thanh Tùng",
                nay, LocalTime.of(9, 15),
                "Viêm da dị ứng thể tiếp xúc, nhiễm khuẩn ngoài da",
                "Ngứa gãi liên tục, rụng lông vùng cổ và hông, da ửng đỏ",
                DonThuoc.DANG_DIEU_TRI,
                "Đeo loa chống liếm 24/24. Giữ chuồng trại thoáng mát, kiêng tắm nước nóng.",
                nay.plusDays(7), "PD-001");
        d1.themChiTiet(new ChiTietDonThuoc("YT-0018", "Nhỏ gáy Frontline Plus", "hộp", 1, 385000, "Nhỏ 1 tuýp dọc sống lưng cổ sau khi tắm sạch"));
        d1.themChiTiet(new ChiTietDonThuoc("YT-0055", "Xịt trị ve rận Hantox-200", "lọ", 1, 85000, "Xịt nhẹ vùng đệm nằm và xung quanh chuồng"));
        d1.themChiTiet(new ChiTietDonThuoc("", "Kháng sinh Clavamox 125mg", "viên", 14, 18000, "Uống 1 viên/lần, ngày 2 lần sau khi ăn"));
        d1.themChiTiet(new ChiTietDonThuoc("", "Thuốc chống dị ứng Prednisolone 5mg", "viên", 7, 5000, "Uống 1/2 viên vào buổi sáng sau ăn no"));
        d1.ghiLog("Tạo mới đơn thuốc bởi BS. Nguyễn Thanh Tùng");
        d1.ghiLog("Đã xuất kho và cấp phát thuốc cho khách hàng");
        ds.add(d1);

        // 2. Sữa - Viêm kết mạc mắt
        DonThuoc d2 = new DonThuoc("DT-002", "TC009", "Sữa", "Mèo", "Xiêm", 3.5,
                "Hoàng Văn Tuấn", "0909 333 444", "BS. Lê Mai Anh",
                nay.minusDays(1), LocalTime.of(10, 30),
                "Viêm kết mạc mắt cấp tính, chảy ghèn",
                "Mắt phải sưng nhẹ, chảy nước mắt nhiều, nhắm tịt mắt khi ra sáng",
                DonThuoc.CHO_CAP_THUOC,
                "Vệ sinh mắt bằng nước muối sinh lý NaCl 0.9% trước khi nhỏ thuốc 10 phút.",
                nay.plusDays(5), "PD-002");
        d2.themChiTiet(new ChiTietDonThuoc("", "Thuốc nhỏ mắt Tobrex 0.3%", "lọ", 1, 65000, "Nhỏ 1-2 giọt vào mắt phải, ngày 3 lần (sáng, trưa, tối)"));
        d2.themChiTiet(new ChiTietDonThuoc("YT-0031", "Gel dinh dưỡng Nutri-Plus", "tuýp", 1, 245000, "Cho liếm trực tiếp 1 thìa cafe/ngày để tăng đề kháng"));
        d2.themChiTiet(new ChiTietDonThuoc("", "Bột kháng sinh Amoxicillin gói", "gói", 5, 12000, "Pha 1/2 gói với nước ấm cho uống sau ăn"));
        d2.ghiLog("Tạo mới đơn thuốc bởi BS. Lê Mai Anh");
        ds.add(d2);

        // 3. Lu - Theo dõi dị ứng thức ăn
        DonThuoc d3 = new DonThuoc("DT-003", "TC003", "Lu", "Chó", "Golden Retriever", 25.5,
                "Trần Quốc Bảo", "0978 456 789", "BS. Phạm Quốc Việt",
                nay.minusDays(2), LocalTime.of(14, 0),
                "Rối loạn tiêu hóa nhẹ nghi do dị ứng thức ăn",
                "Phân sệt lỏng, hơi đầy bụng, chán ăn",
                DonThuoc.DA_CAP_PHAT,
                "Chuyển sang chế độ ăn cháo loãng thịt nạc, bổ sung nước điện giải Oresol.",
                nay.plusDays(3), "");
        d3.themChiTiet(new ChiTietDonThuoc("", "Men tiêu hóa vi sinh Probio Pet", "hộp", 1, 135000, "Hòa 1 gói vào nước uống, ngày 2 lần"));
        d3.themChiTiet(new ChiTietDonThuoc("", "Gel bao niêm mạc Phosphalugel", "gói", 6, 8000, "Uống 1 gói trước khi ăn 30 phút"));
        d3.themChiTiet(new ChiTietDonThuoc("", "Kháng viêm ruột Smecta", "gói", 4, 6000, "Uống 1/2 gói khi đi ngoài phân lỏng"));
        d3.ghiLog("Tạo mới đơn thuốc bởi BS. Phạm Quốc Việt");
        d3.ghiLog("Đã cấp thuốc");
        ds.add(d3);

        // 4. Mochi - Tẩy giun định kỳ & bổ sung canxi
        DonThuoc d4 = new DonThuoc("DT-004", "TC001", "Mochi", "Chó", "Poodle", 4.2,
                "Nguyễn Minh Anh", "0901 234 456", "BS. Nguyễn Thanh Tùng",
                nay.minusDays(3), LocalTime.of(11, 20),
                "Tẩy giun sán định kỳ & Bổ sung khoáng vi lượng",
                "Thể trạng bình thường, lông hơi khô xơ",
                DonThuoc.HOAN_TAT,
                "Tái tẩy giun sau 3 tháng. Cho phơi nắng sáng sớm 15 phút.",
                null, "");
        d4.themChiTiet(new ChiTietDonThuoc("", "Viên tẩy giun Endogard 10", "viên", 1, 45000, "Uống 1/2 viên vào buổi sáng lúc đói"));
        d4.themChiTiet(new ChiTietDonThuoc("", "Viên nhai Calxi Delice", "hộp", 1, 210000, "Nhai 1/2 viên mỗi ngày cùng bữa ăn"));
        d4.ghiLog("Tạo mới đơn thuốc");
        d4.ghiLog("Đã cấp phát thuốc và hoàn tất điều trị");
        ds.add(d4);

        // 5. Kem - Nấm tai ngoài
        DonThuoc d5 = new DonThuoc("DT-005", "TC004", "Kem", "Mèo", "Munchkin", 3.2,
                "Lê Thị Cẩm Tú", "0933 222 111", "BS. Lê Mai Anh",
                nay.minusDays(4), LocalTime.of(16, 45),
                "Viêm ống tai ngoài do nấm Malassezia",
                "Gãi tai thường xuyên, vành tai có bã nhờn màu nâu đen, có mùi hôi",
                DonThuoc.DANG_DIEU_TRI,
                "Rửa sạch tai bằng dung dịch Epi-Otic trước khi tra thuốc. Không để nước vào tai.",
                nay.plusDays(6), "PD-003");
        d5.themChiTiet(new ChiTietDonThuoc("", "Dung dịch rửa tai Epi-Otic 125ml", "chai", 1, 175000, "Bơm vào ống tai, xoa nhẹ gốc tai rồi lau bằng bông sạch"));
        d5.themChiTiet(new ChiTietDonThuoc("", "Thuốc mỡ tra tai Otomax", "tuýp", 1, 140000, "Nhỏ 4 giọt vào tai bị bệnh, ngày 2 lần trong 7 ngày"));
        d5.ghiLog("Tạo mới đơn thuốc bởi BS. Lê Mai Anh");
        d5.ghiLog("Đã giao thuốc cho chủ nuôi");
        ds.add(d5);

        // 6. Đậu - Viêm dạ dày cấp
        DonThuoc d6 = new DonThuoc("DT-006", "TC008", "Đậu", "Chó", "Phốc sóc", 2.6,
                "Lê Thị Cẩm Tú", "0933 222 111", "BS. Nguyễn Thanh Tùng",
                nay.minusDays(5), LocalTime.of(8, 50),
                "Viêm dạ dày cấp do nuốt dị vật nhỏ mềm",
                "Nôn ọe dịch trắng có bọt, đau vùng bụng khi chạm nhẹ",
                DonThuoc.DA_CAP_PHAT,
                "Nhịn ăn trong 12h đầu, sau đó cho uống từng ngụm nước ấm.",
                nay.plusDays(2), "PD-004");
        d6.themChiTiet(new ChiTietDonThuoc("", "Thuốc giảm tiết Omeprazole 10mg", "viên", 7, 7000, "Uống 1/2 viên trước ăn 1 tiếng"));
        d6.themChiTiet(new ChiTietDonThuoc("", "Thuốc chống co thắt No-spa", "viên", 5, 8000, "Uống 1/4 viên khi thấy bé đau quằn bụng"));
        d6.themChiTiet(new ChiTietDonThuoc("", "Bột bù điện giải Hydrite", "gói", 4, 10000, "Pha 200ml nước ấm uống dần trong ngày"));
        d6.ghiLog("Tạo mới đơn thuốc");
        d6.ghiLog("Đã cấp thuốc");
        ds.add(d6);

        // 7. Bơ - Viêm khớp sưng nhẹ chân sau
        DonThuoc d7 = new DonThuoc("DT-007", "TC010", "Bơ", "Chó", "Shiba Inu", 9.8,
                "Ngô Đức Thắng", "0944 333 555", "BS. Phạm Quốc Việt",
                nay.minusDays(6), LocalTime.of(15, 10),
                "Viêm khớp gối thoái hóa nhẹ chân sau bên trái",
                "Đi khập khiễng sau khi ngủ dậy, ngại vận động chạy nhảy",
                DonThuoc.DANG_DIEU_TRI,
                "Hạn chế leo cầu thang, giảm cân nặng xuống mức 9.0kg trong 2 tháng.",
                nay.plusDays(14), "");
        d7.themChiTiet(new ChiTietDonThuoc("", "Giảm đau kháng viêm Carprofen 25mg", "viên", 10, 22000, "Uống 1 viên sau bữa ăn sáng"));
        d7.themChiTiet(new ChiTietDonThuoc("", "Viên bổ khớp Cosequin DS", "hộp", 1, 480000, "Uống 1 viên/ngày liên tục 30 ngày"));
        d7.ghiLog("Tạo mới đơn thuốc");
        d7.ghiLog("Đã cấp phát thuốc");
        ds.add(d7);

        // 8. Rex - Hồi phục sau mổ vết thương hở
        DonThuoc d8 = new DonThuoc("DT-008", "TC006", "Rex", "Chó", "Husky", 22.0,
                "Võ Hoàng Long", "0988 000 666", "BS. Nguyễn Thanh Tùng",
                nay.minusDays(8), LocalTime.of(9, 40),
                "Nhiễm trùng vết cắn phần mềm sau mổ khâu làm sạch",
                "Vết khâu dài 4cm vùng đùi, hơi sưng phù nhẹ",
                DonThuoc.HOAN_TAT,
                "Rửa vết mổ ngày 2 lần bằng cồn đỏ Povidone Iodine 10%, giữ vết thương khô ráo.",
                null, "");
        d8.themChiTiet(new ChiTietDonThuoc("", "Kháng sinh Cefalexin 250mg", "viên", 16, 15000, "Uống 1 viên sáng, 1 viên tối sau ăn"));
        d8.themChiTiet(new ChiTietDonThuoc("", "Giảm phù nề Alphachymotrypsin", "vỉ", 2, 35000, "Uống 2 viên/lần, ngày 2 lần"));
        d8.themChiTiet(new ChiTietDonThuoc("", "Dung dịch sát trùng Povidine 10%", "chai", 1, 25000, "Thấm gạc chấm sát trùng mép vết mổ"));
        d8.ghiLog("Tạo mới đơn thuốc");
        d8.ghiLog("Cắt chỉ ngày thứ 7, vết thương lành tốt. Hoàn tất đơn thuốc.");
        ds.add(d8);
    }
}
