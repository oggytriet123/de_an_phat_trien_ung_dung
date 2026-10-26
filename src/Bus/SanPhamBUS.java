package Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.SanPham;

/** Danh sách sản phẩm / dịch vụ bán tại quầy (dữ liệu mẫu trong bộ nhớ). */
public class SanPhamBUS {
    public static final String TAT_CA = "Tất cả";
    public static final int DANH_MUC_TOI_DA = 16;

    /** Một loại hàng: tên, tiền tố mã SKU (ví dụ SPA) và có phải dịch vụ không. */
    private record LoaiHang(String ten, String tienTo, boolean dichVu) { }

    private final java.util.LinkedHashMap<String, LoaiHang> loai = new java.util.LinkedHashMap<>();
    private final List<SanPham> ds = new ArrayList<>();

    public SanPhamBUS() {
        macDinh("Thức ăn", "TA", false);
        macDinh("Đồ chơi", "TY", false);
        macDinh("Thuốc", "MED", false);
        macDinh("Dịch vụ khám", "VET", true);
        macDinh("Dịch vụ spa", "SPA", true);
        macDinh("Lưu chuồng", "LC", true);
        napDuLieuMau();
    }

    private void macDinh(String ten, String tienTo, boolean dichVu) {
        loai.put(ten, new LoaiHang(ten, tienTo, dichVu));
    }

    public List<SanPham> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    /** Lọc theo danh mục và từ khóa (tên, SKU hoặc tên danh mục, không phân biệt dấu). */
    public List<SanPham> loc(String danhMuc, String tuKhoa) {
        String kw = ChuoiUtil.chuanHoa(tuKhoa == null ? "" : tuKhoa.trim());
        List<SanPham> kq = new ArrayList<>();
        for (SanPham sp : ds) {
            if (danhMuc != null && !TAT_CA.equals(danhMuc) && !sp.getDanhMuc().equals(danhMuc)) continue;
            if (!kw.isEmpty()
                    && !ChuoiUtil.chuanHoa(sp.getTen()).contains(kw)
                    && !ChuoiUtil.chuanHoa(sp.getSku()).contains(kw)
                    && !ChuoiUtil.chuanHoa(sp.getDanhMuc()).contains(kw)) continue;
            kq.add(sp);
        }
        return kq;
    }

    /** Danh sách loại hàng để lọc: "Tất cả" đứng đầu rồi tới các loại theo thứ tự tạo. */
    public List<String> layDanhMuc() {
        List<String> r = new ArrayList<>();
        r.add(TAT_CA);
        r.addAll(loai.keySet());
        return r;
    }

    /** Loại dịch vụ (hiển thị "DV:" và "/ lần" thay vì "SKU:" và "/ sp"). */
    public boolean laDichVu(String danhMuc) {
        LoaiHang l = loai.get(danhMuc);
        return l != null && l.dichVu();
    }

    /** Tiền tố SKU sẽ cấp cho một loại hàng mới có tên này (viết tắt chữ cái đầu, tránh trùng loại khác). */
    public String tienToChoLoaiMoi(String ten) {
        String khongDau = ChuoiUtil.chuanHoa(ten == null ? "" : ten).replaceAll("[^a-z0-9 ]", " ").trim();
        String[] tu = khongDau.isEmpty() ? new String[0] : khongDau.split("\\s+");
        StringBuilder sb = new StringBuilder();
        if (tu.length >= 2) {
            for (String t : tu) {
                sb.append(Character.toUpperCase(t.charAt(0)));
                if (sb.length() == 4) break;
            }
        } else if (tu.length == 1) {
            sb.append(tu[0].toUpperCase(), 0, Math.min(3, tu[0].length()));
        }
        String goc = sb.length() == 0 ? "X" : sb.toString();
        String tt = goc;
        for (int n = 2; tienToDaDung(tt); n++) tt = goc + n;
        return tt;
    }

    private boolean tienToDaDung(String tt) {
        for (LoaiHang l : loai.values()) if (l.tienTo().equals(tt)) return true;
        return false;
    }

    /**
     * Thêm một loại hàng mới (ví dụ Phụ kiện, Quần áo).
     * @return tên đã chuẩn hóa của loại vừa tạo.
     * @throws IllegalArgumentException với nội dung lỗi hiển thị cho nhân viên.
     */
    public String themDanhMuc(String tenNhap, boolean dichVu) {
        String ten = tenNhap == null ? "" : tenNhap.trim().replaceAll("\\s+", " ");
        if (ten.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên loại hàng.");
        if (ten.length() < 2) throw new IllegalArgumentException("Tên loại hàng phải có ít nhất 2 ký tự.");
        if (ten.length() > 30) throw new IllegalArgumentException("Tên loại hàng tối đa 30 ký tự.");
        if (!ten.matches("[\\p{L}\\p{M}\\p{N} &/-]+")) {
            throw new IllegalArgumentException("Tên loại hàng chỉ được chứa chữ, số, khoảng trắng và các dấu & / -");
        }
        if (loai.size() >= DANH_MUC_TOI_DA) {
            throw new IllegalArgumentException("Tối đa " + DANH_MUC_TOI_DA + " loại hàng.");
        }
        ten = ten.substring(0, 1).toUpperCase() + ten.substring(1);
        String key = ChuoiUtil.chuanHoa(ten);
        if (key.equals(ChuoiUtil.chuanHoa(TAT_CA))) throw new IllegalArgumentException("Tên này dành riêng cho bộ lọc \"" + TAT_CA + "\".");
        for (LoaiHang l : loai.values()) {
            if (ChuoiUtil.chuanHoa(l.ten()).equals(key)) {
                throw new IllegalArgumentException("Loại hàng \"" + l.ten() + "\" đã tồn tại.");
            }
        }
        loai.put(ten, new LoaiHang(ten, tienToChoLoaiMoi(ten), dichVu));
        return ten;
    }

    /** Mã SKU sẽ được cấp cho mục mới của loại hàng (ví dụ SPA-009). */
    public String maTiepTheo(String danhMuc) {
        LoaiHang l = loai.get(danhMuc);
        if (l == null) return "";
        String tt = l.tienTo();
        int max = 0;
        for (SanPham s : ds) {
            if (!s.getSku().startsWith(tt + "-")) continue;
            try {
                max = Math.max(max, Integer.parseInt(s.getSku().substring(tt.length() + 1)));
            } catch (NumberFormatException ignored) { }
        }
        return String.format("%s-%03d", tt, max + 1);
    }

    /**
     * Tạo sản phẩm / dịch vụ mới với mã SKU tự sinh.
     * @throws IllegalArgumentException với nội dung lỗi hiển thị cho nhân viên.
     */
    public SanPham tao(String tenNhap, String danhMuc, long donGia) {
        String ten = tenNhap == null ? "" : tenNhap.trim().replaceAll("\\s+", " ");
        if (ten.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm / dịch vụ.");
        if (ten.length() < 2) throw new IllegalArgumentException("Tên phải có ít nhất 2 ký tự.");
        if (ten.length() > 60) throw new IllegalArgumentException("Tên tối đa 60 ký tự.");
        if (!ten.matches("[\\p{L}\\p{M}\\p{N} .,'’()/+%-]+")) {
            throw new IllegalArgumentException("Tên chỉ được chứa chữ, số, khoảng trắng và các dấu . , ' ( ) / + % -");
        }
        if (danhMuc == null || !loai.containsKey(danhMuc)) throw new IllegalArgumentException("Vui lòng chọn loại hàng.");
        if (donGia < 1_000 || donGia > 100_000_000) {
            throw new IllegalArgumentException("Đơn giá phải từ 1.000₫ đến 100.000.000₫.");
        }
        ten = ten.substring(0, 1).toUpperCase() + ten.substring(1);
        String key = ChuoiUtil.chuanHoa(ten);
        for (SanPham s : ds) {
            if (s.getDanhMuc().equals(danhMuc) && ChuoiUtil.chuanHoa(s.getTen()).equals(key)) {
                throw new IllegalArgumentException("\"" + s.getTen() + "\" đã có trong loại " + danhMuc + " (" + s.getSku() + ").");
            }
        }
        SanPham sp = new SanPham(maTiepTheo(danhMuc), ten, danhMuc, donGia, laDichVu(danhMuc));
        ds.add(sp);
        return sp;
    }

    public SanPham timTheoSku(String sku) {
        if (sku == null) return null;
        String s = sku.trim();
        for (SanPham sp : ds) if (sp.getSku().equalsIgnoreCase(s)) return sp;
        return null;
    }

    private void add(String sku, String ten, String dm, long gia) {
        ds.add(new SanPham(sku, ten, dm, gia, laDichVu(dm)));
    }

    /** Sản phẩm (không phải dịch vụ) nhưng xếp trong nhóm dịch vụ, ví dụ sữa tắm trong nhóm spa. */
    private void addSp(String sku, String ten, String dm, long gia) {
        ds.add(new SanPham(sku, ten, dm, gia, false));
    }

    private void napDuLieuMau() {
        // 6 mục đầu giống thứ tự trên thiết kế
        add("RC-002", "Royal Canin 2kg", "Thức ăn", 350_000);
        add("CAT-014", "Thức ăn hạt cho mèo", "Thức ăn", 185_000);
        addSp("SPA-008", "Sữa tắm thú cưng", "Dịch vụ spa", 120_000);
        add("VET-001", "Khám tổng quát", "Dịch vụ khám", 250_000);
        add("SPA-002", "Cắt tỉa lông", "Dịch vụ spa", 300_000);
        add("MED-010", "Vitamin thú cưng", "Thuốc", 95_000);

        add("PG-015", "Pedigree trưởng thành 1,5kg", "Thức ăn", 185_000);
        add("WK-012", "Whiskas cá thu 1,2kg", "Thức ăn", 120_000);
        add("SH-030", "SmartHeart Gold 3kg", "Thức ăn", 290_000);
        add("MO-080", "Pate Me-O 80g", "Thức ăn", 18_000);
        add("TY-001", "Bóng cao su có chuông", "Đồ chơi", 45_000);
        add("TY-002", "Xương gặm cho chó", "Đồ chơi", 60_000);
        add("TY-003", "Cần câu mèo lông vũ", "Đồ chơi", 55_000);
        add("MED-011", "Thuốc xổ giun Drontal", "Thuốc", 95_000);
        add("MED-012", "Xịt trị ve rận Frontline", "Thuốc", 280_000);
        add("VET-002", "Tiêm phòng 5 bệnh", "Dịch vụ khám", 250_000);
        add("VET-003", "Siêu âm", "Dịch vụ khám", 200_000);
        add("SPA-001", "Tắm sấy thú cưng", "Dịch vụ spa", 200_000);
        add("SPA-003", "Vệ sinh tai - cắt móng", "Dịch vụ spa", 80_000);
        add("LC-001", "Lưu chuồng nhỏ (ngày)", "Lưu chuồng", 100_000);
        add("LC-002", "Lưu chuồng lớn (ngày)", "Lưu chuồng", 150_000);
        add("LC-003", "Lưu chuồng VIP (ngày)", "Lưu chuồng", 250_000);
    }
}
