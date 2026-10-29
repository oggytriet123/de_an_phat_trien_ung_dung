package Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.SanPham;

/** Danh sách sản phẩm / dịch vụ bán tại quầy (dữ liệu mẫu trong bộ nhớ). */
public class SanPhamBUS {
    public static final String TAT_CA = "Tất cả";
    public static final int DANH_MUC_TOI_DA = 16;
    public static final String[] DOI_TUONG = {"Tất cả thú cưng", "Chó & Mèo", "Chó", "Mèo"};

    /** Một loại hàng: tên, tiền tố mã SKU (ví dụ SPA) và có phải dịch vụ không. */
    private record LoaiHang(String ten, String tienTo, boolean dichVu) { }

    private final java.util.LinkedHashMap<String, LoaiHang> loai = new java.util.LinkedHashMap<>();
    private final List<SanPham> ds = new ArrayList<>();
    private final java.util.Map<String, Integer> daCap = new java.util.HashMap<>();   // số SKU lớn nhất đã cấp theo tiền tố (không cấp lại sau khi xóa)
    private int demStt;

    public SanPhamBUS() {
        macDinh("Thức ăn", "TA", false);
        macDinh("Đồ chơi", "TY", false);
        macDinh("Thuốc", "MED", false);
        macDinh("Dịch vụ khám", "VET", true);
        macDinh("Dịch vụ spa", "SPA", true);
        macDinh("Tiêm phòng", "VAC", true);
        macDinh("Phẫu thuật", "SUR", true);
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
            if (!sp.isDangMo()) continue;       // mục tạm ngưng không hiện khi lập hóa đơn
            if (danhMuc != null && !TAT_CA.equals(danhMuc) && !sp.getDanhMuc().equals(danhMuc)) continue;
            if (!kw.isEmpty()
                    && !ChuoiUtil.chuanHoa(sp.getTen()).contains(kw)
                    && !ChuoiUtil.chuanHoa(sp.getSku()).contains(kw)
                    && !ChuoiUtil.chuanHoa(sp.getDanhMuc()).contains(kw)) continue;
            kq.add(sp);
        }
        return kq;
    }

    /**
     * Danh sách cho trang quản lý (kể cả mục tạm ngưng): chữ dịch vụ hoặc chữ sản phẩm,
     * lọc theo nhóm và từ khóa (tên, mã, nhóm, đối tượng; không phân biệt dấu).
     */
    public List<SanPham> locQuanLy(boolean dichVu, String nhom, String tuKhoa) {
        String kw = ChuoiUtil.chuanHoa(tuKhoa == null ? "" : tuKhoa.trim());
        List<SanPham> kq = new ArrayList<>();
        for (SanPham sp : ds) {
            if (sp.isDichVu() != dichVu) continue;
            if (nhom != null && !TAT_CA.equals(nhom) && !sp.getDanhMuc().equals(nhom)) continue;
            if (!kw.isEmpty()) {
                String hay = ChuoiUtil.chuanHoa(sp.getTen()) + "|" + ChuoiUtil.chuanHoa(sp.getSku()) + "|"
                        + ChuoiUtil.chuanHoa(sp.getDanhMuc()) + "|" + ChuoiUtil.chuanHoa(sp.getDoiTuong());
                if (!hay.contains(kw)) continue;
            }
            kq.add(sp);
        }
        return kq;
    }

    /** Các nhóm thuộc dịch vụ (hoặc sản phẩm), kể cả nhóm đang chứa mục thuộc loại đó. */
    public List<String> layNhom(boolean dichVu) {
        java.util.Set<String> co = new java.util.HashSet<>();
        for (String ten : loai.keySet()) if (laDichVu(ten) == dichVu) co.add(ten);
        for (SanPham sp : ds) if (sp.isDichVu() == dichVu) co.add(sp.getDanhMuc());
        List<String> r = new ArrayList<>();
        for (String ten : loai.keySet()) if (co.contains(ten)) r.add(ten);
        return r;
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
            String w = tu[0].toUpperCase(java.util.Locale.ROOT);
            sb.append(w.length() <= 3 ? w : w.substring(0, 3));
        }
        String goc = sb.length() == 0 ? "DM" : sb.toString();
        String candidate = goc;
        int dem = 2;
        while (tienToDaDung(candidate)) {
            candidate = goc + dem++;
        }
        return candidate;
    }

    private boolean tienToDaDung(String tt) {
        for (LoaiHang l : loai.values()) if (l.tienTo().equalsIgnoreCase(tt)) return true;
        for (SanPham sp : ds) {
            int gach = sp.getSku().lastIndexOf('-');
            if (gach > 0 && sp.getSku().substring(0, gach).equalsIgnoreCase(tt)) return true;
        }
        return false;
    }

    /**
     * Thêm một loại hàng mới (nhóm dịch vụ hoặc nhóm sản phẩm).
     * @return tiền tố SKU tự sinh cho loại này
     * @throws IllegalArgumentException nếu tên trống, quá dài, đã tồn tại hoặc đã đạt số lượng tối đa
     */
    public String themLoaiHang(String tenNhap, boolean dichVu) {
        if (loai.size() >= DANH_MUC_TOI_DA) {
            throw new IllegalArgumentException("Hệ thống chỉ hỗ trợ tối đa " + DANH_MUC_TOI_DA + " nhóm.");
        }
        String ten = tenNhap == null ? "" : tenNhap.trim();
        if (ten.isEmpty()) throw new IllegalArgumentException("Tên nhóm không được để trống.");
        if (ten.length() > 30) throw new IllegalArgumentException("Tên nhóm tối đa 30 ký tự.");
        for (String k : loai.keySet()) {
            if (k.equalsIgnoreCase(ten)) throw new IllegalArgumentException("Nhóm \"" + k + "\" đã tồn tại.");
        }
        String tt = tienToChoLoaiMoi(ten);
        loai.put(ten, new LoaiHang(ten, tt, dichVu));
        return tt;
    }

    public String themDanhMuc(String tenNhap, boolean dichVu) {
        return themLoaiHang(tenNhap, dichVu);
    }

    /**
     * Cấp mã SKU mới cho mục sắp thêm vào danh mục này (dạng PRE-001, tăng dần và không dùng lại số đã xóa).
     */
    public String sinhSku(String danhMuc) {
        LoaiHang l = loai.get(danhMuc);
        String tienTo = l != null ? l.tienTo() : tienToChoLoaiMoi(danhMuc);
        int so = daCap.getOrDefault(tienTo, 0) + 1;
        daCap.put(tienTo, so);
        return String.format("%s-%03d", tienTo, so);
    }

    public String maTiepTheo(String danhMuc) {
        LoaiHang l = loai.get(danhMuc);
        String tienTo = l != null ? l.tienTo() : tienToChoLoaiMoi(danhMuc);
        int so = daCap.getOrDefault(tienTo, 0) + 1;
        return String.format("%s-%03d", tienTo, so);
    }

    private void kiemTraGia(long gia) {
        if (gia < 0) throw new IllegalArgumentException("Đơn giá không được âm.");
        if (gia > 1_000_000_000L) throw new IllegalArgumentException("Đơn giá tối đa 1.000.000.000đ.");
    }

    private String kiemTraTen(String tenNhap) {
        String t = tenNhap == null ? "" : tenNhap.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Tên mục không được để trống.");
        if (t.length() > 60) throw new IllegalArgumentException("Tên mục tối đa 60 ký tự.");
        return t;
    }

    private LoaiHang kiemTraLoai(String danhMuc) {
        LoaiHang l = loai.get(danhMuc);
        if (l == null) throw new IllegalArgumentException("Nhóm \"" + danhMuc + "\" không tồn tại.");
        return l;
    }

    private String kiemTraThoiLuong(boolean laDichVu, String thoiLuong) {
        if (!laDichVu) return "";
        String tl = thoiLuong == null ? "" : thoiLuong.trim();
        if (tl.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn thời lượng dự kiến.");
        return tl;
    }

    private String kiemTraDoiTuong(boolean laDichVu, String doiTuong) {
        if (!laDichVu) return "";
        String dt = doiTuong == null ? "" : doiTuong.trim();
        if (dt.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn đối tượng áp dụng.");
        return dt;
    }

    private void kiemTraTrungTen(String ten, String danhMuc, SanPham boQua) {
        for (SanPham sp : ds) {
            if (sp == boQua) continue;
            if (sp.getDanhMuc().equalsIgnoreCase(danhMuc) && sp.getTen().equalsIgnoreCase(ten)) {
                throw new IllegalArgumentException("Trong nhóm \"" + danhMuc + "\" đã có mục tên \"" + sp.getTen() + "\".");
            }
        }
    }

    /**
     * Thêm một dịch vụ hoặc sản phẩm mới vào danh mục. Mã SKU tự sinh theo nhóm.
     * @return đối tượng vừa thêm
     */
    public SanPham themMoi(String tenNhap, String danhMuc, long donGia, String thoiLuong, String doiTuong) {
        String ten = kiemTraTen(tenNhap);
        LoaiHang l = kiemTraLoai(danhMuc);
        kiemTraGia(donGia);
        String tl = kiemTraThoiLuong(l.dichVu(), thoiLuong);
        String dt = kiemTraDoiTuong(l.dichVu(), doiTuong);
        kiemTraTrungTen(ten, danhMuc, null);

        String sku = sinhSku(danhMuc);
        SanPham sp = new SanPham(sku, ten, danhMuc, donGia, l.dichVu());
        sp.setThoiLuong(tl);
        sp.setDoiTuong(dt);
        dangKy(sp);
        return sp;
    }

    public SanPham tao(String tenNhap, String danhMuc, long donGia, String thoiLuong, String doiTuong, boolean dangMo) {
        SanPham sp = themMoi(tenNhap, danhMuc, donGia, thoiLuong, doiTuong);
        sp.setDangMo(dangMo);
        return sp;
    }

    /**
     * Cập nhật thông tin dịch vụ hoặc sản phẩm. Nhóm mới phải cùng loại (dịch vụ hoặc sản phẩm) với nhóm cũ.
     */
    public SanPham capNhat(String sku, String tenNhap, String danhMuc, long donGia, String thoiLuong, String doiTuong, boolean dangMo) {
        SanPham sp = timTheoSku(sku);
        if (sp == null) throw new IllegalArgumentException("Không tìm thấy mục có mã " + sku + ".");
        String ten = kiemTraTen(tenNhap);
        LoaiHang l = kiemTraLoai(danhMuc);
        if (!danhMuc.equals(sp.getDanhMuc()) && l.dichVu() != sp.isDichVu()) {
            throw new IllegalArgumentException("Không thể chuyển giữa nhóm dịch vụ và nhóm sản phẩm.");
        }
        kiemTraGia(donGia);
        String tl = kiemTraThoiLuong(sp.isDichVu(), thoiLuong);
        String dt = kiemTraDoiTuong(sp.isDichVu(), doiTuong);
        kiemTraTrungTen(ten, danhMuc, sp);

        sp.setTen(ten);
        sp.setDanhMuc(danhMuc);
        sp.setDonGia(donGia);
        sp.setThoiLuong(tl);
        sp.setDoiTuong(dt);
        sp.setDangMo(dangMo);
        return sp;
    }

    /**
     * Xóa một mục khỏi danh mục. Hóa đơn đã lưu hoặc đã thanh toán có mục này vẫn giữ nguyên dòng hàng.
     * Mã SKU đã dùng không được cấp lại.
     * @throws IllegalArgumentException nếu không có mục đó.
     */
    public void xoa(String sku) {
        SanPham sp = timTheoSku(sku);
        if (sp == null) throw new IllegalArgumentException("Không tìm thấy mục có mã " + sku + ".");
        ds.remove(sp);
    }

    /** Thêm vào danh sách, đánh số thứ tự và ghi nhận số SKU đã cấp. */
    private void dangKy(SanPham sp) {
        sp.setStt(++demStt);
        ds.add(sp);
        int gach = sp.getSku().lastIndexOf('-');
        if (gach > 0) {
            try {
                daCap.merge(sp.getSku().substring(0, gach), Integer.parseInt(sp.getSku().substring(gach + 1)), Math::max);
            } catch (NumberFormatException ignored) { }
        }
    }

    public SanPham timTheoSku(String sku) {
        if (sku == null) return null;
        String s = sku.trim();
        for (SanPham sp : ds) if (sp.getSku().equalsIgnoreCase(s)) return sp;
        return null;
    }

    private void add(String sku, String ten, String dm, long gia) {
        dangKy(new SanPham(sku, ten, dm, gia, laDichVu(dm)));
    }

    /** Sản phẩm (không phải dịch vụ) nhưng xếp trong nhóm dịch vụ, ví dụ sữa tắm trong nhóm spa. */
    private void addSp(String sku, String ten, String dm, long gia) {
        dangKy(new SanPham(sku, ten, dm, gia, false));
    }

    private void napDuLieuMau() {
        // Thức ăn & Đồ chơi & Thuốc
        add("RC-002", "Royal Canin 2kg", "Thức ăn", 350_000);
        add("CAT-014", "Thức ăn hạt cho mèo", "Thức ăn", 185_000);
        add("PG-015", "Pedigree trưởng thành 1,5kg", "Thức ăn", 185_000);
        add("WK-012", "Whiskas cá thu 1,2kg", "Thức ăn", 120_000);
        add("SH-030", "SmartHeart Gold 3kg", "Thức ăn", 290_000);
        add("MO-080", "Pate Me-O 80g", "Thức ăn", 18_000);
        add("TY-001", "Bóng cao su có chuông", "Đồ chơi", 45_000);
        add("TY-002", "Xương gặm cho chó", "Đồ chơi", 60_000);
        add("TY-003", "Cần câu mèo lông vũ", "Đồ chơi", 55_000);
        add("MED-010", "Vitamin tổng hợp thú cưng", "Thuốc", 95_000);
        add("MED-011", "Thuốc xổ giun Drontal", "Thuốc", 95_000);
        add("MED-012", "Xịt trị ve rận Frontline", "Thuốc", 280_000);

        // Dịch vụ Khám & Spa & Lưu chuồng
        add("VET-001", "Khám tổng quát", "Dịch vụ khám", 250_000);
        add("VET-002", "Khám chuyên sâu nội khoa", "Dịch vụ khám", 300_000);
        add("VET-003", "Siêu âm màu bụng", "Dịch vụ khám", 200_000);
        addSp("SPA-008", "Sữa tắm thú cưng thảo dược", "Dịch vụ spa", 120_000);
        add("SPA-001", "Tắm sấy vệ sinh toàn diện", "Dịch vụ spa", 200_000);
        add("SPA-002", "Cắt tỉa tạo kiểu lông", "Dịch vụ spa", 300_000);
        add("SPA-003", "Vệ sinh tai - cắt móng", "Dịch vụ spa", 80_000);
        add("LC-001", "Lưu chuồng tiêu chuẩn (ngày)", "Lưu chuồng", 100_000);
        add("LC-002", "Lưu chuồng lớn chó cảnh (ngày)", "Lưu chuồng", 150_000);
        add("LC-003", "Lưu chuồng phòng VIP (ngày)", "Lưu chuồng", 250_000);

        // Dịch vụ Tiêm phòng
        add("VAC-001", "Tiêm vaccine phòng Dại (Rabies)", "Tiêm phòng", 150_000);
        add("VAC-002", "Tiêm vaccine 5 bệnh chó", "Tiêm phòng", 250_000);
        add("VAC-003", "Tiêm vaccine 7 bệnh chó", "Tiêm phòng", 350_000);
        add("VAC-004", "Tiêm vaccine 4 bệnh mèo", "Tiêm phòng", 320_000);
        add("VAC-005", "Tiêm phòng ngừa xoắn khuẩn Lepto", "Tiêm phòng", 200_000);
        add("VAC-006", "Tẩy giun sán định kỳ thú cưng", "Tiêm phòng", 80_000);

        // Dịch vụ Phẫu thuật
        add("SUR-001", "Triệt sản mèo đực", "Phẫu thuật", 350_000);
        add("SUR-002", "Triệt sản mèo cái nội soi", "Phẫu thuật", 650_000);
        add("SUR-003", "Triệt sản chó đực (< 10kg)", "Phẫu thuật", 500_000);
        add("SUR-004", "Triệt sản chó cái (< 10kg)", "Phẫu thuật", 800_000);
        add("SUR-005", "Phẫu thuật lấy sỏi bàng quang", "Phẫu thuật", 1_500_000);
        add("SUR-006", "Mổ đẻ / Lấy thai cấp cứu", "Phẫu thuật", 1_800_000);
        add("SUR-007", "Phẫu thuật may phục hồi vết thương", "Phẫu thuật", 400_000);
        add("SUR-008", "Phẫu thuật kết hợp xương gãy", "Phẫu thuật", 2_500_000);

        // Thời lượng và đối tượng
        datDv("VET-001", "30 phút", "Tất cả thú cưng");
        datDv("VET-002", "45 phút", "Chó & Mèo");
        datDv("VET-003", "25 phút", "Chó & Mèo");
        datDv("SPA-001", "60 phút", "Chó & Mèo");
        datDv("SPA-002", "60 phút", "Chó & Mèo");
        datDv("SPA-003", "20 phút", "Chó & Mèo");
        datDv("LC-001", "1 ngày", "Tất cả thú cưng");
        datDv("LC-002", "1 ngày", "Chó");
        datDv("LC-003", "1 ngày", "Tất cả thú cưng");

        datDv("VAC-001", "15 phút", "Chó & Mèo");
        datDv("VAC-002", "20 phút", "Chó");
        datDv("VAC-003", "20 phút", "Chó");
        datDv("VAC-004", "20 phút", "Mèo");
        datDv("VAC-005", "15 phút", "Chó");
        datDv("VAC-006", "15 phút", "Tất cả thú cưng");

        datDv("SUR-001", "45 phút", "Mèo");
        datDv("SUR-002", "60 phút", "Mèo");
        datDv("SUR-003", "60 phút", "Chó");
        datDv("SUR-004", "75 phút", "Chó");
        datDv("SUR-005", "90 phút", "Chó & Mèo");
        datDv("SUR-006", "90 phút", "Chó & Mèo");
        datDv("SUR-007", "45 phút", "Tất cả thú cưng");
        datDv("SUR-008", "120 phút", "Chó & Mèo");
    }

    private void datDv(String sku, String thoiLuong, String doiTuong) {
        SanPham sp = timTheoSku(sku);
        if (sp != null) {
            sp.setThoiLuong(thoiLuong);
            sp.setDoiTuong(doiTuong);
        }
    }
}
