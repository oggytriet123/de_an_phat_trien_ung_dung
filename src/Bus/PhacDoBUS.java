package Bus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.GiaiDoanDieuTri;
import Entity.NhatKyTheoDoi;
import Entity.PhacDoDieuTri;

/**
 * Nghiệp vụ quản lý Phác đồ điều trị chuyên sâu cho thú cưng.
 */
public class PhacDoBUS {
    private final List<PhacDoDieuTri> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public PhacDoBUS() {
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

    public List<PhacDoDieuTri> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public PhacDoDieuTri timTheoMa(String ma) {
        if (ma == null) return null;
        for (PhacDoDieuTri pd : ds) {
            if (pd.getMaPhacDo().equalsIgnoreCase(ma.trim())) return pd;
        }
        return null;
    }

    public synchronized String taoMaMoi() {
        int max = 0;
        for (PhacDoDieuTri pd : ds) {
            try {
                String s = pd.getMaPhacDo().replaceAll("\\D", "");
                if (!s.isEmpty()) max = Math.max(max, Integer.parseInt(s));
            } catch (Exception ignored) {}
        }
        return String.format("PD-%03d", max + 1);
    }

    public PhacDoDieuTri them(PhacDoDieuTri pd) {
        if (pd.getMaPhacDo() == null || pd.getMaPhacDo().trim().isEmpty()) {
            pd.setMaPhacDo(taoMaMoi());
        }
        pd.ghiLog("Thiết lập phác đồ điều trị bởi " + pd.getBacSi());
        ds.add(0, pd);
        notifyListeners();
        return pd;
    }

    public boolean capNhat(PhacDoDieuTri pd) {
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getMaPhacDo().equalsIgnoreCase(pd.getMaPhacDo())) {
                pd.ghiLog("Cập nhật thông tin phác đồ điều trị");
                ds.set(i, pd);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public boolean capNhatTienDo(String maPD, int tienDoMoi, String trangThaiMoi) {
        PhacDoDieuTri pd = timTheoMa(maPD);
        if (pd != null) {
            int clamped = Math.min(100, Math.max(0, tienDoMoi));
            pd.setTienDo(clamped);
            if (clamped == 100) {
                pd.setTrangThai(PhacDoDieuTri.HOAN_THANH);
            } else if (trangThaiMoi != null && !trangThaiMoi.isEmpty()) {
                pd.setTrangThai(trangThaiMoi);
            }
            pd.ghiLog("Cập nhật tiến độ: " + clamped + "% (Trạng thái: " + pd.getTrangThai() + ")");
            notifyListeners();
            return true;
        }
        return false;
    }

    public List<PhacDoDieuTri> layPhacDoCuaThuCung(String maTC) {
        List<PhacDoDieuTri> res = new ArrayList<>();
        if (maTC == null || maTC.trim().isEmpty()) return res;
        for (PhacDoDieuTri pd : ds) {
            if (maTC.trim().equalsIgnoreCase(pd.getMaTC())) {
                res.add(pd);
            }
        }
        return res;
    }

    public boolean themNhatKy(String maPD, NhatKyTheoDoi nk) {
        PhacDoDieuTri pd = timTheoMa(maPD);
        if (pd != null && nk != null) {
            pd.themNhatKy(nk);
            pd.ghiLog("Ghi nhận nhật ký khám: " + nk.getTinhTrang() + " (Thân nhiệt: " + nk.getThanNhiet() + "°C)");
            notifyListeners();
            return true;
        }
        return false;
    }

    public boolean hoanThanh(String maPD) {
        PhacDoDieuTri pd = timTheoMa(maPD);
        if (pd != null) {
            pd.setTienDo(100);
            pd.setTrangThai(PhacDoDieuTri.HOAN_THANH);
            pd.ghiLog("Bác sĩ kết luận hoàn thành liệu trình điều trị");
            notifyListeners();
            return true;
        }
        return false;
    }

    public boolean tamNgung(String maPD, String lyDo) {
        PhacDoDieuTri pd = timTheoMa(maPD);
        if (pd != null) {
            pd.setTrangThai(PhacDoDieuTri.TAM_NGUNG);
            pd.ghiLog("Tạm ngưng phác đồ. Lý do: " + (lyDo == null || lyDo.isEmpty() ? "Khách yêu cầu" : lyDo));
            notifyListeners();
            return true;
        }
        return false;
    }

    public boolean xoa(String maPD) {
        boolean ok = ds.removeIf(p -> p.getMaPhacDo().equalsIgnoreCase(maPD));
        if (ok) notifyListeners();
        return ok;
    }

    // --- Thống kê ---
    public int getTongSoPhacDo() { return ds.size(); }

    public int getSoDangDieuTri() {
        int cnt = 0;
        for (PhacDoDieuTri p : ds) if (PhacDoDieuTri.DANG_DIEU_TRI.equals(p.getTrangThai())) cnt++;
        return cnt;
    }

    public int getSoTheoDoi() {
        int cnt = 0;
        for (PhacDoDieuTri p : ds) if (PhacDoDieuTri.THEO_DOI_TICH_CUC.equals(p.getTrangThai())) cnt++;
        return cnt;
    }

    public int getSoHoanThanh() {
        int cnt = 0;
        for (PhacDoDieuTri p : ds) if (PhacDoDieuTri.HOAN_THANH.equals(p.getTrangThai())) cnt++;
        return cnt;
    }

    public int getTiLeHoanThanh() {
        if (ds.isEmpty()) return 0;
        return (int) Math.round((double) getSoHoanThanh() / ds.size() * 100.0);
    }

    private void napDuLieuMau() {
        LocalDate nay = LocalDate.now();

        // 1. Milo - Viêm da dị ứng & Nhiễm khuẩn
        PhacDoDieuTri p1 = new PhacDoDieuTri("PD-001",
                "Phác đồ điều trị Viêm da dị ứng & Nhiễm khuẩn ngoài da",
                "TC005", "Milo", "Chó", "Corgi", 10.4,
                "Phạm Gia Hân", "0912 345 678", "BS. Nguyễn Thanh Tùng",
                nay.minusDays(4), nay.plusDays(10),
                PhacDoDieuTri.MUC_DO_TRUNG_BINH,
                "Viêm da tiếp xúc bội nhiễm vi khuẩn Gram (+) thể bán cấp",
                "Cắt cơn ngứa trong 48h, diệt khuẩn bề mặt da, kích thích mọc lông sau 14 ngày",
                "Chế độ ăn kiêng đạm lạ (bò, hải sản). Bổ sung dầu cá Omega 3-6 để tái tạo biểu bì da.",
                65, PhacDoDieuTri.DANG_DIEU_TRI, "DT-001");
        p1.themGiaiDoan(new GiaiDoanDieuTri(1, "Giai đoạn 1: Tấn công & Giảm ngứa khẩn cấp", "3 ngày",
                "Cắt cơn gãi, chống tổn thương loét thêm da",
                "Kháng sinh Clavamox 125mg x 2 lần/ngày, Prednisolone 5mg sáng sau ăn", GiaiDoanDieuTri.DA_HOAN_THANH));
        p1.themGiaiDoan(new GiaiDoanDieuTri(2, "Giai đoạn 2: Khử khuẩn & Làm lành tổn thương", "7 ngày",
                "Tiêu diệt vi khuẩn trú ngụ, se khô các nốt đỏ",
                "Tắm sữa tắm thảo dược Dermaleen 2 ngày/lần, tiếp tục kháng sinh đủ 10 ngày", GiaiDoanDieuTri.DANG_TIEN_HANH));
        p1.themGiaiDoan(new GiaiDoanDieuTri(3, "Giai đoạn 3: Phục hồi biểu bì lông & Phòng tái phát", "4 ngày",
                "Kích thích nang lông phát triển, dưỡng ẩm da",
                "Bổ sung viên kẽm & Omega 3, nhỏ phòng ngừa ve rận Frontline", GiaiDoanDieuTri.CHUA_THUC_HIEN));
        p1.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(nay.minusDays(1), java.time.LocalTime.of(9, 30)),
                "BS. Nguyễn Thanh Tùng", 10.3, 38.6, "Tiến triển tốt",
                "Các nốt đỏ đã se khô, bé đỡ ngứa nhiều, không còn cọ xát vào tường. Tiếp tục giai đoạn 2."));
        p1.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(nay.minusDays(4), java.time.LocalTime.of(9, 0)),
                "BS. Nguyễn Thanh Tùng", 10.4, 38.8, "Cần theo dõi sát",
                "Khám lần đầu: Vùng bụng và cổ ửng đỏ, có mủ nhẹ. Bắt đầu giai đoạn 1 và kê đơn thuốc DT-001."));
        p1.ghiLog("Khởi tạo phác đồ điều trị");
        ds.add(p1);

        // 2. Sữa - Viêm kết mạc mắt
        PhacDoDieuTri p2 = new PhacDoDieuTri("PD-002",
                "Phác đồ điều trị Viêm kết mạc cấp tính ở mèo",
                "TC009", "Sữa", "Mèo", "Xiêm", 3.5,
                "Hoàng Văn Tuấn", "0909 333 444", "BS. Lê Mai Anh",
                nay.minusDays(1), nay.plusDays(6),
                PhacDoDieuTri.MUC_DO_NHE,
                "Viêm kết mạc mắt phải do bụi bẩn nhiễm khuẩn",
                "Làm sạch ghèn mắt trong 24h, hết đỏ mắt sau 5 ngày",
                "Ăn pate dinh dưỡng mềm, để chậu cát xa chỗ ngủ để tránh bụi bám vào mắt.",
                35, PhacDoDieuTri.DANG_DIEU_TRI, "DT-002");
        p2.themGiaiDoan(new GiaiDoanDieuTri(1, "Giai đoạn 1: Kháng khuẩn tại chỗ", "4 ngày",
                "Khống chế vi khuẩn tại màng kết mạc",
                "Nhỏ mắt Tobrex 3 lần/ngày sau khi rửa NaCl 0.9%, uống Amoxicillin", GiaiDoanDieuTri.DANG_TIEN_HANH));
        p2.themGiaiDoan(new GiaiDoanDieuTri(2, "Giai đoạn 2: Củng cố & Tăng đề kháng", "3 ngày",
                "Chống tái nhiễm, bổ sung vitamin A",
                "Nhỏ dung dịch nước mắt nhân tạo làm ẩm giác mạc, ăn gel Nutri-Plus", GiaiDoanDieuTri.CHUA_THUC_HIEN));
        p2.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(nay, java.time.LocalTime.of(10, 0)),
                "BS. Lê Mai Anh", 3.5, 38.4, "Tiến triển tốt",
                "Mắt phải đã mở to hơn, bớt chảy nước mắt. Nhắc chủ tiếp tục tra thuốc đều đặn."));
        p2.ghiLog("Khởi tạo phác đồ điều trị");
        ds.add(p2);

        // 3. Kem - Nấm tai Malassezia
        PhacDoDieuTri p3 = new PhacDoDieuTri("PD-003",
                "Phác đồ kiểm soát & Tiêu trừ Nấm tai Malassezia",
                "TC004", "Kem", "Mèo", "Munchkin", 3.2,
                "Lê Thị Cẩm Tú", "0933 222 111", "BS. Lê Mai Anh",
                nay.minusDays(3), nay.plusDays(7),
                PhacDoDieuTri.MUC_DO_TRUNG_BINH,
                "Viêm ống tai ngoài tăng tiết bã nhờn bội nhiễm nấm Malassezia pachydermatis",
                "Làm sạch ráy tai sau 3 ngày, diệt sạch bào tử nấm sau 10 ngày",
                "Chế độ ăn hạt kiểm soát dị ứng. Giữ khô vành tai sau khi rửa.",
                50, PhacDoDieuTri.THEO_DOI_TICH_CUC, "DT-005");
        p3.themGiaiDoan(new GiaiDoanDieuTri(1, "Giai đoạn 1: Làm sạch & Kháng nấm trực tiếp", "5 ngày",
                "Loại bỏ mảng bám ráy tai, diệt nấm",
                "Rửa tai Epi-Otic hàng ngày, tra Otomax 2 lần/ngày", GiaiDoanDieuTri.DA_HOAN_THANH));
        p3.themGiaiDoan(new GiaiDoanDieuTri(2, "Giai đoạn 2: Duy trì & Cân bằng pH tai", "5 ngày",
                "Ngăn chặn nấm tái phát sinh",
                "Tra tai 1 lần/ngày vào buổi tối, soi kiểm tra vi nấm", GiaiDoanDieuTri.DANG_TIEN_HANH));
        p3.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(nay.minusDays(1), java.time.LocalTime.of(16, 0)),
                "BS. Lê Mai Anh", 3.2, 38.5, "Ổn định",
                "Lượng ráy tai giảm rõ rệt, tai bớt mùi hôi. Bé bớt lắc đầu gãi tai."));
        p3.ghiLog("Khởi tạo phác đồ điều trị");
        ds.add(p3);

        // 4. Đậu - Viêm dạ dày cấp
        PhacDoDieuTri p4 = new PhacDoDieuTri("PD-004",
                "Phác đồ cấp cứu & Phục hồi niêm mạc dạ dày cấp",
                "TC008", "Đậu", "Chó", "Phốc sóc", 2.6,
                "Lê Thị Cẩm Tú", "0933 222 111", "BS. Nguyễn Thanh Tùng",
                nay.minusDays(5), nay.plusDays(2),
                PhacDoDieuTri.MUC_DO_NANG,
                "Viêm loét trợt niêm mạc dạ dày cấp nghi do dị vật",
                "Cắt cơn nôn trong 12h, chống mất nước, làm lành vết loét",
                "Nhịn ăn 12h đầu, sau đó ăn súp gà loãng chia nhỏ 4 bữa/ngày.",
                80, PhacDoDieuTri.THEO_DOI_TICH_CUC, "DT-006");
        p4.themGiaiDoan(new GiaiDoanDieuTri(1, "Giai đoạn 1: Cầm nôn & Bù dịch điện giải", "2 ngày",
                "Ổn định tuần hoàn, hết nôn mửa",
                "Tiêm kháng nôn Ondansetron, truyền dung dịch Ringer Lactate 80ml, uống Omeprazole", GiaiDoanDieuTri.DA_HOAN_THANH));
        p4.themGiaiDoan(new GiaiDoanDieuTri(2, "Giai đoạn 2: Tái tạo niêm mạc dạ dày", "3 ngày",
                "Bảo vệ thành dạ dày, giảm co thắt",
                "Phosphalugel trước bữa ăn, No-spa khi đau", GiaiDoanDieuTri.DA_HOAN_THANH));
        p4.themGiaiDoan(new GiaiDoanDieuTri(3, "Giai đoạn 3: Phục hồi chức năng tiêu hóa", "2 ngày",
                "Hệ tiêu hóa thích nghi trở lại với thức ăn hạt ngâm",
                "Bổ sung men vi sinh sống Probio, thức ăn tiêu hóa cao Gastrointestinal", GiaiDoanDieuTri.DANG_TIEN_HANH));
        p4.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(nay.minusDays(1), java.time.LocalTime.of(14, 0)),
                "BS. Nguyễn Thanh Tùng", 2.55, 38.3, "Đang hồi phục",
                "Bé đã bắt đầu đòi ăn, không nôn lại suốt 48h qua. Thể trạng tươi tỉnh."));
        p4.ghiLog("Khởi tạo phác đồ điều trị");
        ds.add(p4);

        // 5. Rex - Kháng sinh & Liền sẹo sau phẫu thuật
        PhacDoDieuTri p5 = new PhacDoDieuTri("PD-005",
                "Phác đồ kháng sinh & Chăm sóc liền sẹo sau mổ",
                "TC006", "Rex", "Chó", "Husky", 22.0,
                "Võ Hoàng Long", "0988 000 666", "BS. Nguyễn Thanh Tùng",
                nay.minusDays(9), nay.minusDays(1),
                PhacDoDieuTri.MUC_DO_TRUNG_BINH,
                "Khâu tạo hình vết thương hở vùng đùi sau phẫu thuật",
                "Liền mép sẹo vết mổ sau 7 ngày, cắt chỉ an toàn không nhiễm trùng",
                "Khẩu phần giàu đạm và vitamin C. Hạn chế chạy nhảy mạnh trong 2 tuần.",
                100, PhacDoDieuTri.HOAN_THANH, "DT-008");
        p5.themGiaiDoan(new GiaiDoanDieuTri(1, "Giai đoạn 1: Kháng khuẩn & Chống phù nề", "4 ngày",
                "Ngăn chặn nhiễm khuẩn vết mổ",
                "Cefalexin 250mg, Alphachymotrypsin, sát khuẩn vết mổ hàng ngày", GiaiDoanDieuTri.DA_HOAN_THANH));
        p5.themGiaiDoan(new GiaiDoanDieuTri(2, "Giai đoạn 2: Lên da non & Cắt chỉ", "4 ngày",
                "Mép vết mổ dính chắc, loại bỏ chỉ khâu",
                "Tháo chỉ ngày thứ 7, bôi thuốc liền sẹo Dermatix", GiaiDoanDieuTri.DA_HOAN_THANH));
        p5.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(nay.minusDays(2), java.time.LocalTime.of(15, 30)),
                "BS. Nguyễn Thanh Tùng", 22.1, 38.5, "Hồi phục hoàn toàn",
                "Đã cắt chỉ an toàn. Vết mổ khô ráo, biểu mô hóa hoàn toàn. Kết thúc phác đồ."));
        p5.ghiLog("Khởi tạo phác đồ điều trị");
        p5.ghiLog("Đã hoàn thành phác đồ điều trị");
        ds.add(p5);
    }
}
