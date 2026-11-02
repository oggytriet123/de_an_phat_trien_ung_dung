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
        khoiTaoDuLieuMau();
    }

    private void khoiTaoDuLieuMau() {
        if (!ds.isEmpty()) return;

        // PD-001: Milu (PET002) - Hậu phẫu sỏi bàng quang PT-001
        PhacDoDieuTri pd1 = new PhacDoDieuTri("PD-001", "Phác đồ phục hồi chức năng tiết niệu & hậu phẫu sỏi bàng quang",
                "PET002", "Milu", "Chó", "Golden Retriever", 28.5,
                "Lê Thị Cẩm Tú", "0938222111", "BS. Võ Thanh Hùng",
                LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 30),
                PhacDoDieuTri.MUC_DO_NANG,
                "Sỏi bàng quang Struvite kích thước 15mm đã phẫu thuật lấy sỏi (PT-001)",
                "Kiểm soát nhiễm trùng đường tiểu, hồi phục niêm mạc bàng quang, ngăn ngừa tái phát sỏi",
                "Hạt Royal Canin Urinary S/O, uống đủ 1.5 lít nước lọc/ngày, kiêng hoàn toàn hải sản",
                60, PhacDoDieuTri.DANG_DIEU_TRI, "DT-001");
        pd1.themGiaiDoan(new GiaiDoanDieuTri(1, "Chăm sóc vết mổ & kiểm soát đau cấp", "3 ngày",
                "Giảm đau, kháng viêm, giữ vết mổ khô ráo không nhiễm trùng",
                "Meloxicam 0.5mg uống sau ăn no, Betadine Vet xịt vệ sinh vết khâu 2 lần/ngày",
                GiaiDoanDieuTri.DA_HOAN_THANH));
        pd1.themGiaiDoan(new GiaiDoanDieuTri(2, "Kháng sinh điều trị nhiễm trùng bàng quang", "7 ngày",
                "Diệt khuẩn đường niệu, kiểm soát pH nước tiểu về mức 6.5",
                "Amoxicillin + Clavulanic 250mg, nước điện giải, bổ sung Gel Nourse",
                GiaiDoanDieuTri.DANG_TIEN_HANH));
        pd1.themGiaiDoan(new GiaiDoanDieuTri(3, "Phục hồi niêm mạc & siêu âm kiểm tra", "5 ngày",
                "Khảo sát hình ảnh siêu âm bàng quang sạch cặn, cắt chỉ vết mổ",
                "Siêu âm màu 4D kiểm tra hệ tiết niệu, xét nghiệm nước tiểu 10 thông số",
                GiaiDoanDieuTri.CHUA_THUC_HIEN));
        pd1.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(2026, 10, 18, 9, 0), "BS. Võ Thanh Hùng", 28.3, 38.6,
                "Tiến triển tốt", "Vết mổ khô ráo, đi tiểu thông suốt không còn rặn buốt hay lẫn máu. Tinh thần hoạt bát"));
        pd1.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(2026, 10, 16, 10, 30), "BS. Võ Thanh Hùng", 28.5, 38.8,
                "Ổn định", "Hậu phẫu ngày đầu, đã tỉnh mê hoàn toàn, uống được nước đường ấm và ăn nhẹ cháo dinh dưỡng"));
        pd1.ghiLog("Thiết lập phác đồ điều trị bởi BS. Võ Thanh Hùng");
        pd1.ghiLog("Liên kết với ca phẫu thuật PT-001 và đơn thuốc DT-001");
        ds.add(pd1);

        // PD-002: Buddy (PET007) - Viêm da cơ địa, đang lưu trú C-101
        PhacDoDieuTri pd2 = new PhacDoDieuTri("PD-002", "Phác đồ điều trị viêm da cơ địa dị ứng & nấm da mãn tính",
                "PET007", "Buddy", "Chó", "Poodle Toy", 4.5,
                "Hoàng Minh Tuấn", "0945678901", "BS. Nguyễn Hoài Anh",
                LocalDate.of(2026, 10, 18), LocalDate.of(2026, 11, 5),
                PhacDoDieuTri.MUC_DO_TRUNG_BINH,
                "Viêm da Atopic kết hợp nấm kẽ móng và vành tai (đang lưu trú tại lồng C-101)",
                "Tiêu diệt vi nấm gây bệnh, dứt cơn ngứa ngáy, phục hồi nang lông và hàng rào biểu bì",
                "Thức ăn Hypoallergenic kiêng đạm bò/gà, tăng cường Omega-3 và dầu cá hồi",
                45, PhacDoDieuTri.THEO_DOI_TICH_CUC, "DT-002");
        pd2.themGiaiDoan(new GiaiDoanDieuTri(1, "Tấn công chống nấm & giảm ngứa cấp", "7 ngày",
                "Cắt cơn ngứa, ức chế bào tử nấm lây lan toàn thân",
                "Bôi kem Dermaleen 2 lần/ngày, tắm Bio Derma ủ bọt 10 phút xả sạch 2 lần/tuần",
                GiaiDoanDieuTri.DANG_TIEN_HANH));
        pd2.themGiaiDoan(new GiaiDoanDieuTri(2, "Dưỡng ẩm phục hồi & tái tạo biểu bì da", "10 ngày",
                "Lông tơ mọc lại đều màu, da hết bong vảy gàu đỏ",
                "Xịt dưỡng ẩm da chuyên dụng, viên uống kẽm Chelate và Vitamin E",
                GiaiDoanDieuTri.CHUA_THUC_HIEN));
        pd2.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(2026, 10, 21, 15, 30), "BS. Nguyễn Hoài Anh", 4.5, 38.4,
                "Tiến triển tốt", "Các mảng đỏ ở kẽ tai se khít, giảm gãi rõ rệt. Chăm sóc tại lồng C-101 rất chu đáo"));
        pd2.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(2026, 10, 18, 16, 0), "BS. Nguyễn Hoài Anh", 4.4, 38.7,
                "Cần theo dõi sát", "Tiếp nhận lưu trú lồng C-101. Da vùng sống lưng đỏ tấy, ngứa nhiều, đã đeo loa chống cắn"));
        pd2.ghiLog("Thiết lập phác đồ trị liệu da liễu bởi BS. Nguyễn Hoài Anh");
        pd2.ghiLog("Đã giao biên bản chăm sóc cho bộ phận quản lý lồng chuồng");
        ds.add(pd2);

        // PD-003: Rocky (PET004) - Hậu phẫu gãy xương PT-003
        PhacDoDieuTri pd3 = new PhacDoDieuTri("PD-003", "Phác đồ cố định & phục hồi chức năng gãy kín 1/3 xương đùi",
                "PET004", "Rocky", "Chó", "Rottweiler", 35.0,
                "Võ Hoàng Long", "0386909808", "BS. Võ Thanh Hùng",
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 20),
                PhacDoDieuTri.MUC_DO_NGUY_KICH,
                "Gãy chéo 1/3 giữa thân xương đùi phải do tai nạn, đã phẫu thuật nẹp vít kết hợp xương (PT-003)",
                "Kiểm soát đau và sốc, bảo vệ kết cấu nẹp xương vững chắc, tái tạo cầu can xương hoàn chỉnh",
                "Tăng cường Protein cao cấp, bổ sung Canxi Nano Pet Cal Plus và sụn khớp Glucosamine",
                20, PhacDoDieuTri.DANG_DIEU_TRI, "DT-003");
        pd3.themGiaiDoan(new GiaiDoanDieuTri(1, "Chống phù nề & bất động chi tuyệt đối", "5 ngày",
                "Giảm sưng nề mô mềm, ngăn ngừa nhiễm trùng huyết và sốt hậu phẫu",
                "Kháng sinh Cefalexin 500mg, Meloxicam liều cao, nhốt lồng hạn chế vận động",
                GiaiDoanDieuTri.DANG_TIEN_HANH));
        pd3.themGiaiDoan(new GiaiDoanDieuTri(2, "Kích thích liền can xương sớm", "14 ngày",
                "Hình thành cầu can xương canxi hóa, không xô lệch vít nẹp",
                "Canxi nano sinh học 2 viên/ngày, chiếu đèn hồng ngoại giảm đau",
                GiaiDoanDieuTri.CHUA_THUC_HIEN));
        pd3.themGiaiDoan(new GiaiDoanDieuTri(3, "Vật lý trị liệu phục hồi dáng đi", "12 ngày",
                "Chân bước tì đè chạm đất bình thường, chụp X-quang nghiệm thu liền xương",
                "Bài tập co duỗi thụ động, massage cơ đùi, đi dạo nhẹ nhàng 10 phút/ngày",
                GiaiDoanDieuTri.CHUA_THUC_HIEN));
        pd3.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(2026, 10, 21, 11, 0), "BS. Võ Thanh Hùng", 34.8, 38.9,
                "Cần theo dõi sát", "Hậu phẫu ngày thứ 1, nẹp xương cố định tốt, đã chịu ăn nhẹ pate thịt nạc, đang theo dõi thân nhiệt"));
        pd3.ghiLog("Thiết lập phác đồ chỉnh hình xương bởi BS. Võ Thanh Hùng");
        pd3.ghiLog("Liên kết với ca mổ PT-003 và đơn thuốc DT-003");
        ds.add(pd3);

        // PD-004: Bằng Xù (PET012) - Viêm phế quản cấp
        PhacDoDieuTri pd4 = new PhacDoDieuTri("PD-004", "Phác đồ điều trị viêm phế quản cấp thể nhẹ",
                "PET012", "Bằng Xù", "Mèo", "Mèo Ragdoll", 5.1,
                "Bùi Anh Khoa", "0919888000", "BS. Ngô Gia Bảo",
                LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 14),
                PhacDoDieuTri.MUC_DO_NHE,
                "Viêm đường hô hấp trên do thay đổi thời tiết chuyển lạnh, ho khan có đờm dãi",
                "Long đờm, dứt cơn ho đêm, khôi phục đường thở thông thoáng và đề kháng",
                "Pate hâm ấm, nước canh thịt gà ninh, giữ ấm phòng ngủ 26°C tránh gió lùa",
                100, PhacDoDieuTri.HOAN_THANH, "");
        pd4.themGiaiDoan(new GiaiDoanDieuTri(1, "Khí dung & siro giảm ho long đờm", "4 ngày",
                "Loãng đờm, làm dịu niêm mạc họng, chấm dứt ho cơn",
                "Xông khí dung NaCl 0.9% 15 phút/ngày, siro ho thảo dược",
                GiaiDoanDieuTri.DA_HOAN_THANH));
        pd4.themGiaiDoan(new GiaiDoanDieuTri(2, "Bổ sung đề kháng & phục hồi thể trạng", "5 ngày",
                "Hết đờm hoàn toàn, phổi trong, ăn uống tăng cân",
                "Lysine tăng miễn dịch mèo, men vi sinh tiêu hóa",
                GiaiDoanDieuTri.DA_HOAN_THANH));
        pd4.themNhatKy(new NhatKyTheoDoi(LocalDateTime.of(2026, 10, 14, 10, 0), "BS. Ngô Gia Bảo", 5.1, 38.3,
                "Đang hồi phục", "Đã khỏi bệnh hoàn toàn. Phổi êm, không còn tiếng ran ẩm, mèo vui vẻ linh hoạt. Đóng phác đồ"));
        pd4.ghiLog("Thiết lập phác đồ hô hấp bởi BS. Ngô Gia Bảo");
        pd4.ghiLog("Bác sĩ kết luận hoàn thành liệu trình điều trị và xuất viện");
        ds.add(pd4);
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
}
