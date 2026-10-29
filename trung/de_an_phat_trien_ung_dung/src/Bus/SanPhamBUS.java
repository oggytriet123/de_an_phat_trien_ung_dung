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
