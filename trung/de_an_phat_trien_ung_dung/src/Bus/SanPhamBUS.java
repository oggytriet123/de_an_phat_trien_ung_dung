package Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.SanPham;

/** Danh sách sản phẩm / dịch vụ bán tại quầy (dữ liệu mẫu trong bộ nhớ). */
public class SanPhamBUS {
    public static final String TAT_CA = "Tất cả";
    public static final String[] DANH_MUC = {
        TAT_CA, "Thức ăn", "Đồ chơi", "Thuốc", "Dịch vụ khám", "Dịch vụ spa", "Lưu chuồng"
    };

    private final List<SanPham> ds = new ArrayList<>();

    public SanPhamBUS() {
        napDuLieuMau();
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

    public SanPham timTheoSku(String sku) {
        if (sku == null) return null;
        String s = sku.trim();
        for (SanPham sp : ds) if (sp.getSku().equalsIgnoreCase(s)) return sp;
        return null;
    }

    private void add(String sku, String ten, String dm, long gia) {
        boolean dv = dm.startsWith("Dịch vụ") || dm.equals("Lưu chuồng");
        ds.add(new SanPham(sku, ten, dm, gia, dv));
    }

    private void napDuLieuMau() {
        add("RC-002", "Royal Canin 2kg", "Thức ăn", 350_000);
        add("PG-015", "Pedigree trưởng thành 1,5kg", "Thức ăn", 185_000);
        add("WK-012", "Whiskas cá thu 1,2kg", "Thức ăn", 120_000);
        add("SH-030", "SmartHeart Gold 3kg", "Thức ăn", 290_000);
        add("MO-080", "Pate Me-O 80g", "Thức ăn", 18_000);
        add("TY-001", "Bóng cao su có chuông", "Đồ chơi", 45_000);
        add("TY-002", "Xương gặm cho chó", "Đồ chơi", 60_000);
        add("TY-003", "Cần câu mèo lông vũ", "Đồ chơi", 55_000);
        add("TY-004", "Chuột bông catnip", "Đồ chơi", 35_000);
        add("TH-DR1", "Thuốc xổ giun Drontal", "Thuốc", 95_000);
        add("TH-FL1", "Xịt trị ve rận Frontline", "Thuốc", 280_000);
        add("TH-NP1", "Gel vitamin tổng hợp", "Thuốc", 150_000);
        add("SV-KH1", "Khám tổng quát", "Dịch vụ khám", 150_000);
        add("SV-KH2", "Tiêm phòng 5 bệnh", "Dịch vụ khám", 250_000);
        add("SV-KH3", "Siêu âm", "Dịch vụ khám", 200_000);
        add("SV-SPA1", "Dịch vụ spa", "Dịch vụ spa", 400_000);
        add("SV-SPA2", "Cắt tỉa lông", "Dịch vụ spa", 250_000);
        add("SV-SPA3", "Vệ sinh tai - cắt móng", "Dịch vụ spa", 80_000);
        add("SV-LC1", "Lưu chuồng nhỏ (ngày)", "Lưu chuồng", 100_000);
        add("SV-LC2", "Lưu chuồng lớn (ngày)", "Lưu chuồng", 150_000);
        add("SV-LC3", "Lưu chuồng VIP (ngày)", "Lưu chuồng", 250_000);
    }
}
