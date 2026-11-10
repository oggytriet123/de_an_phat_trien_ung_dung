package Bus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.DichVuDAO;
import Entity.SanPham;

/** Danh sách sản phẩm / dịch vụ bán tại quầy, đồng bộ trực tiếp với CSDL SQL Server (DICHVU, SANPHAM). */
public class SanPhamBUS {
    public static final String TAT_CA = "Tất cả";
    public static final int DANH_MUC_TOI_DA = 16;
    public static final String[] DOI_TUONG = {"Tất cả thú cưng", "Chó & Mèo", "Chó", "Mèo"};

    /** Một loại hàng: tên, tiền tố mã SKU (ví dụ SPA) và có phải dịch vụ không. */
    private record LoaiHang(String ten, String tienTo, boolean dichVu) { }

    private final java.util.LinkedHashMap<String, LoaiHang> loai = new java.util.LinkedHashMap<>();
    private final List<SanPham> ds = new ArrayList<>();
    private final java.util.Map<String, Integer> daCap = new java.util.HashMap<>();   // số SKU lớn nhất đã cấp theo tiền tố
    private int demStt;
    private final DichVuDAO dao = new DichVuDAO();

    public SanPhamBUS() {
        macDinh("Thức ăn", "TA", false);
        macDinh("Đồ chơi", "TY", false);
        macDinh("Thuốc", "MED", false);
        macDinh("Dịch vụ khám", "VET", true);
        macDinh("Dịch vụ spa", "SPA", true);
        macDinh("Tiêm phòng", "VAC", true);
        macDinh("Phẫu thuật", "SUR", true);
        macDinh("Lưu chuồng", "LC", true);
        taiTuCSDL();
    }

    public synchronized void taiTuCSDL() {
        ds.clear();
        daCap.clear();
        demStt = 0;

        List<SanPham> dichVuList = dao.layTatCaDichVu();
        for (SanPham dv : dichVuList) {
            String dm = dv.getDanhMuc();
            if (dm != null && !loai.containsKey(dm)) {
                String prefix = dm.length() >= 3 ? dm.substring(0, 3).toUpperCase() : "DV";
                macDinh(dm, prefix, true);
            }
            if (dv.getSku() != null && dv.getSku().matches("^DV0*\\d+$")) {
                String skuMoi = SkuGenerator.generateService(dv.getDanhMuc(), dv.getTen(), dv.getDoiTuong());
                dv.setMaSKU(skuMoi);
            }
            dangKy(dv);
        }

        List<SanPham> spList = dao.layTatCaSanPham();
        for (SanPham sp : spList) {
            String dm = sp.getDanhMuc();
            if (dm != null && !dm.isBlank() && !loai.containsKey(dm)) {
                String prefix = tienToChoLoaiMoi(dm);
                macDinh(dm, prefix, false);
            }
            dangKy(sp);
        }
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

    public SanPham timTheoSku(String sku) {
        for (SanPham sp : ds) if (sp.getSku().equals(sku)) return sp;
        return null;
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

    // ------------------------------------------------------------ thêm / sửa / xóa mục
    private String kiemTraTen(String tenNhap) {
        String ten = tenNhap == null ? "" : tenNhap.trim();
        if (ten.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên.");
        if (ten.length() > 100) throw new IllegalArgumentException("Tên tối đa 100 ký tự.");
        return ten;
    }

    private LoaiHang kiemTraLoai(String danhMuc) {
        LoaiHang l = loai.get(danhMuc);
        if (l == null) throw new IllegalArgumentException("Nhóm \"" + danhMuc + "\" không hợp lệ.");
        return l;
    }

    private void kiemTraGia(long donGia) {
        if (donGia < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm.");
        }
        if (donGia > 500_000_000) {
            throw new IllegalArgumentException("Đơn giá phải từ 0 đến 500.000.000₫.");
        }
    }

    private String kiemTraThoiLuong(boolean laDichVu, String thoiLuong) {
        if (!laDichVu) return "";
        String tl = thoiLuong == null ? "" : thoiLuong.trim();
        if (tl.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn hoặc nhập thời lượng.");
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

        try {
            if (l.dichVu()) dao.themDichVu(sp);
            else dao.themSanPham(sp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sp;
    }

    public SanPham tao(String tenNhap, String danhMuc, long donGia, String thoiLuong, String doiTuong, boolean dangMo) {
        SanPham sp = themMoi(tenNhap, danhMuc, donGia, thoiLuong, doiTuong);
        sp.setDangMo(dangMo);
        try {
            if (sp.isDichVu()) dao.capNhatDichVu(sp);
            else dao.capNhatSanPham(sp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sp;
    }

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

        try {
            if (sp.isDichVu()) dao.capNhatDichVu(sp);
            else dao.capNhatSanPham(sp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sp;
    }

    public void xoa(String sku) {
        SanPham sp = timTheoSku(sku);
        if (sp == null) throw new IllegalArgumentException("Không tìm thấy mục có mã " + sku + ".");
        ds.remove(sp);
        try {
            if (sp.isDichVu()) dao.xoaDichVu(sku);
            else dao.xoaSanPham(sku);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
