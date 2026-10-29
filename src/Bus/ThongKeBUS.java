package Bus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import Entity.KhachHang;
import Entity.PhieuKho;
import Entity.SanPham;
import Entity.ThuCung;

/**
 * BUS tổng hợp dữ liệu cho phân hệ Báo cáo & Thống kê.
 * Lấy dữ liệu từ PhieuKhoBUS, KhoHangBUS, KhachHangBUS, ThuCungBUS.
 */
public class ThongKeBUS {
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuKhoBUS phieuKhoBUS;
    private final KhoHangBUS khoHangBUS;
    private final KhachHangBUS khachHangBUS;
    private final ThuCungBUS thuCungBUS;

    public ThongKeBUS() {
        this.phieuKhoBUS = new PhieuKhoBUS();
        this.khoHangBUS = new KhoHangBUS();
        this.khachHangBUS = new KhachHangBUS();
        this.thuCungBUS = new ThuCungBUS();
    }

    public ThongKeBUS(PhieuKhoBUS pkBus, KhoHangBUS khBus, KhachHangBUS khachBus, ThuCungBUS tcBus) {
        this.phieuKhoBUS = pkBus != null ? pkBus : new PhieuKhoBUS();
        this.khoHangBUS = khBus != null ? khBus : new KhoHangBUS();
        this.khachHangBUS = khachBus != null ? khachBus : new KhachHangBUS();
        this.thuCungBUS = tcBus != null ? tcBus : new ThuCungBUS();
    }

    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FMT);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    // ==================== 1. THỐNG KÊ DOANH THU & DÒNG TIỀN ====================

    /** Lấy danh sách phiếu nằm trong khoảng thời gian [tuNgay, denNgay] */
    public List<PhieuKho> layDanhSachPhieu(LocalDate tuNgay, LocalDate denNgay, String loaiPhieu) {
        List<PhieuKho> res = new ArrayList<>();
        for (PhieuKho pk : phieuKhoBUS.layDanhSach()) {
            LocalDate d = parseDate(pk.getNgayLap());
            if (d == null) continue;
            if (tuNgay != null && d.isBefore(tuNgay)) continue;
            if (denNgay != null && d.isAfter(denNgay)) continue;
            if (loaiPhieu != null && !loaiPhieu.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(loaiPhieu.trim()) && !loaiPhieu.trim().equalsIgnoreCase(pk.getLoaiPhieu().trim())) {
                continue;
            }
            res.add(pk);
        }
        return res;
    }

    /** Tính tổng doanh thu xuất bán hoàn tất trong khoảng thời gian */
    public long tinhTongDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        long tong = 0;
        for (PhieuKho pk : phieuKhoBUS.layDanhSach()) {
            if ("Xuất bán".equalsIgnoreCase(pk.getLoaiPhieu()) && "Hoàn tất".equalsIgnoreCase(pk.getTrangThai())) {
                LocalDate d = parseDate(pk.getNgayLap());
                if (d != null && (tuNgay == null || !d.isBefore(tuNgay)) && (denNgay == null || !d.isAfter(denNgay))) {
                    tong += pk.getTongGiaTri();
                }
            }
        }
        return tong;
    }

    /** Tính tổng chi phí nhập kho hoàn tất/đang xử lý trong khoảng thời gian */
    public long tinhTongChiNhap(LocalDate tuNgay, LocalDate denNgay) {
        long tong = 0;
        for (PhieuKho pk : phieuKhoBUS.layDanhSach()) {
            if ("Nhập mới".equalsIgnoreCase(pk.getLoaiPhieu()) && !"Đã hủy".equalsIgnoreCase(pk.getTrangThai())) {
                LocalDate d = parseDate(pk.getNgayLap());
                if (d != null && (tuNgay == null || !d.isBefore(tuNgay)) && (denNgay == null || !d.isAfter(denNgay))) {
                    tong += pk.getTongGiaTri();
                }
            }
        }
        return tong;
    }

    /** Lợi nhuận gộp ước tính = Doanh thu - Chi phí nhập */
    public long tinhLoiNhuan(LocalDate tuNgay, LocalDate denNgay) {
        return tinhTongDoanhThu(tuNgay, denNgay) - tinhTongChiNhap(tuNgay, denNgay);
    }

    /** Đếm số phiếu theo loại */
    public int demSoPhieu(LocalDate tuNgay, LocalDate denNgay, String loaiPhieu) {
        int count = 0;
        for (PhieuKho pk : layDanhSachPhieu(tuNgay, denNgay, loaiPhieu)) {
            count++;
        }
        return count;
    }

    /** Giá trị trung bình của mỗi phiếu xuất bán hoàn tất */
    public long tinhGiaTriTrungBinhDonXuat(LocalDate tuNgay, LocalDate denNgay) {
        int soPhieu = demSoPhieu(tuNgay, denNgay, "Xuất bán");
        if (soPhieu == 0) return 0;
        return tinhTongDoanhThu(tuNgay, denNgay) / soPhieu;
    }

    /**
     * Dữ liệu cho Biểu đồ cột: gom nhóm Doanh thu và Chi phí theo từng ngày.
     * Trả về TreeMap<LocalDate, long[]> trong đó:
     * long[0]: Doanh thu xuất bán
     * long[1]: Chi phí nhập mới
     */
    public Map<LocalDate, long[]> layDoanhThuTheoNgay(LocalDate tuNgay, LocalDate denNgay) {
        Map<LocalDate, long[]> map = new TreeMap<>();
        for (PhieuKho pk : phieuKhoBUS.layDanhSach()) {
            LocalDate d = parseDate(pk.getNgayLap());
            if (d == null) continue;
            if (tuNgay != null && d.isBefore(tuNgay)) continue;
            if (denNgay != null && d.isAfter(denNgay)) continue;

            long[] val = map.computeIfAbsent(d, k -> new long[2]);
            if ("Xuất bán".equalsIgnoreCase(pk.getLoaiPhieu()) && "Hoàn tất".equalsIgnoreCase(pk.getTrangThai())) {
                val[0] += pk.getTongGiaTri();
            } else if ("Nhập mới".equalsIgnoreCase(pk.getLoaiPhieu()) && !"Đã hủy".equalsIgnoreCase(pk.getTrangThai())) {
                val[1] += pk.getTongGiaTri();
            }
        }
        return map;
    }

    /**
     * Tỷ lệ cơ cấu phiếu theo loại (Nhập mới, Xuất bán, Xuất hủy).
     */
    public Map<String, Integer> layCoCauLoaiPhieu(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Xuất bán", 0);
        map.put("Nhập mới", 0);
        map.put("Xuất hủy", 0);

        for (PhieuKho pk : layDanhSachPhieu(tuNgay, denNgay, null)) {
            String loai = pk.getLoaiPhieu();
            map.put(loai, map.getOrDefault(loai, 0) + 1);
        }
        return map;
    }

    // ==================== 2. THỐNG KÊ KHO HÀNG & MẶT HÀNG ====================

    public int getTongSKU() {
        return khoHangBUS.getTongSKU();
    }

    public int getSoSapHet() {
        return khoHangBUS.getSoSapHet();
    }

    public long getTongGiaTriTonKho() {
        long tong = 0;
        for (SanPham sp : khoHangBUS.layDanhSach()) {
            tong += (long) sp.getSoLuongTon() * sp.getDonGia();
        }
        return tong;
    }

    /** Tổng số lượng đơn vị sản phẩm tồn kho (unit count) */
    public int getTongSoLuongSanPhamTon() {
        int tong = 0;
        for (SanPham sp : khoHangBUS.layDanhSach()) {
            tong += sp.getSoLuongTon();
        }
        return tong;
    }

    /** Cơ cấu số lượng SKU theo Danh mục */
    public Map<String, Integer> layCoCauDanhMucSKU() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (SanPham sp : khoHangBUS.layDanhSach()) {
            String dm = sp.getDanhMuc() == null ? "Khác" : sp.getDanhMuc();
            map.put(dm, map.getOrDefault(dm, 0) + 1);
        }
        return map;
    }

    /** Cơ cấu Giá trị tồn kho theo Danh mục */
    public Map<String, Long> layGiaTriTonTheoDanhMuc() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (SanPham sp : khoHangBUS.layDanhSach()) {
            String dm = sp.getDanhMuc() == null ? "Khác" : sp.getDanhMuc();
            long val = (long) sp.getSoLuongTon() * sp.getDonGia();
            map.put(dm, map.getOrDefault(dm, 0L) + val);
        }
        return map;
    }

    /** Danh sách sản phẩm sắp hết hàng (tồn kho <= 5) */
    public List<SanPham> layDanhSachSapHet() {
        List<SanPham> list = new ArrayList<>();
        for (SanPham sp : khoHangBUS.layDanhSach()) {
            if (sp.getSoLuongTon() <= 5) {
                list.add(sp);
            }
        }
        list.sort(Comparator.comparingInt(SanPham::getSoLuongTon));
        return list;
    }

    /** Top sản phẩm có giá trị tồn kho lớn nhất */
    public List<SanPham> layTopGiaTriTon(int limit) {
        List<SanPham> list = new ArrayList<>(khoHangBUS.layDanhSach());
        list.sort((a, b) -> Long.compare((long) b.getSoLuongTon() * b.getDonGia(), (long) a.getSoLuongTon() * a.getDonGia()));
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    // ==================== 3. THỐNG KÊ KHÁCH HÀNG & THÚ CƯNG ====================

    public int getTongSoKhachHang() {
        return khachHangBUS.layDanhSach().size();
    }

    public int getTongSoThuCung() {
        return thuCungBUS.layDanhSach().size();
    }

    public int getSoDangLuuTru() {
        int cnt = 0;
        for (ThuCung tc : thuCungBUS.layDanhSach()) {
            if ("Đang lưu trú".equals(tc.getTrangThai())) cnt++;
        }
        return cnt;
    }

    public int getSoCanTheoDoi() {
        int cnt = 0;
        for (ThuCung tc : thuCungBUS.layDanhSach()) {
            String tt = tc.getTrangThai();
            if ("Cần theo dõi".equals(tt) || "Đang điều trị".equals(tt)) cnt++;
        }
        return cnt;
    }

    /** Cơ cấu khách hàng theo hạng thẻ */
    public Map<String, Integer> layCoCauHangThe() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("VIP", 0);
        map.put("Vàng", 0);
        map.put("Bạc", 0);
        map.put("Thường", 0);

        for (KhachHang kh : khachHangBUS.layDanhSach()) {
            String h = kh.getHangThe() == null ? "Thường" : kh.getHangThe();
            map.put(h, map.getOrDefault(h, 0) + 1);
        }
        return map;
    }

    /** Cơ cấu thú cưng theo loài (Chó vs Mèo) */
    public Map<String, Integer> layCoCauThuCungTheoLoai() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ThuCung tc : thuCungBUS.layDanhSach()) {
            String loai = tc.getLoai() == null ? "Khác" : tc.getLoai();
            map.put(loai, map.getOrDefault(loai, 0) + 1);
        }
        return map;
    }

    /** Thống kê tình trạng sức khỏe thú cưng */
    public Map<String, Integer> layThongKeSucKhoe() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Khỏe mạnh", 0);
        map.put("Cần theo dõi", 0);
        map.put("Đang điều trị", 0);

        for (ThuCung tc : thuCungBUS.layDanhSach()) {
            String sk = tc.getTrangThai() == null ? "Khỏe mạnh" : tc.getTrangThai();
            map.put(sk, map.getOrDefault(sk, 0) + 1);
        }
        return map;
    }

    /** Top khách hàng có tổng chi tiêu cao nhất */
    public List<KhachHang> layTopKhachHangChiTieu(int limit) {
        List<KhachHang> list = new ArrayList<>(khachHangBUS.layDanhSach());
        list.sort((a, b) -> Long.compare(b.getTongChiTieu(), a.getTongChiTieu()));
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    /** Top khách hàng có điểm tích luỹ cao nhất */
    public List<KhachHang> layTopKhachHangDiem(int limit) {
        List<KhachHang> list = new ArrayList<>(khachHangBUS.layDanhSach());
        list.sort((a, b) -> Integer.compare(b.getDiemTichLuy(), a.getDiemTichLuy()));
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }
}
