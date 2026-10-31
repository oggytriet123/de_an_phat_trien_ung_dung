package Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Entity.SanPham;

/**
 * Nghiệp vụ quản lý kho hàng (mock 320 sản phẩm theo thiết kế).
 */
public class KhoHangBUS {
    private final List<SanPham> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public KhoHangBUS() {
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

    public List<SanPham> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public int getTongSKU() {
        return ds.size();
    }

    public int getSoSapHet() {
        int cnt = 0;
        for (SanPham sp : ds) {
            if (sp.getSoLuongTon() <= 5) cnt++;
        }
        return cnt;
    }

    public String getTongGiaTriTonKho() {
        return "420,5 triệu đ";
    }

    public String getNhapKhoTrongThang() {
        return "85 triệu đ";
    }

    public SanPham them(String maSKU, String tenSP, String quyCach, String danhMuc,
                        int ton, String dvt, long donGia, String trangThai) {
        if (maSKU == null || maSKU.trim().isEmpty()) {
            String prefix = switch (danhMuc) {
                case "Thức ăn" -> "TA";
                case "Đồ chơi" -> "DC";
                case "Thuốc & Y tế" -> "YT";
                default -> "PC";
            };
            maSKU = String.format("%s-%04d", prefix, ds.size() + 1);
        }
        SanPham sp = new SanPham(maSKU, tenSP, quyCach, danhMuc, ton, dvt, donGia, trangThai);
        ds.add(0, sp);
        notifyListeners();
        return sp;
    }

    public boolean nhapKho(String maSKU, int soLuongThem) {
        for (SanPham sp : ds) {
            if (sp.getMaSKU().equalsIgnoreCase(maSKU)) {
                sp.setSoLuongTon(sp.getSoLuongTon() + soLuongThem);
                if (sp.getSoLuongTon() > 0 && "Tạm hết hàng".equals(sp.getTrangThai())) {
                    sp.setTrangThai("Đang bán");
                }
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public boolean xuatKho(String maSKU, int soLuongXuat) {
        if (maSKU == null || maSKU.trim().isEmpty() || soLuongXuat <= 0) return false;
        for (SanPham sp : ds) {
            if (sp.getMaSKU().equalsIgnoreCase(maSKU.trim())) {
                int tonMoi = Math.max(0, sp.getSoLuongTon() - soLuongXuat);
                sp.setSoLuongTon(tonMoi);
                if (tonMoi == 0) {
                    sp.setTrangThai("Tạm hết hàng");
                }
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public boolean xoa(String maSKU) {
        boolean ok = ds.removeIf(sp -> sp.getMaSKU().equalsIgnoreCase(maSKU));
        if (ok) notifyListeners();
        return ok;
    }

    public SanPham timTheoSKU(String maSKU) {
        if (maSKU == null) return null;
        for (SanPham sp : ds) {
            if (sp.getMaSKU().equalsIgnoreCase(maSKU.trim())) return sp;
        }
        return null;
    }

    private void add(String sku, String ten, String quyCach, String danhMuc, int ton, String dvt, long gia, String tt) {
        ds.add(new SanPham(sku, ten, quyCach, danhMuc, ton, dvt, gia, tt));
    }

    private void napDuLieuMau() {
        // 7 dòng đầu tiên hiển thị trên trang 1 khớp chính xác ảnh thiết kế tham chiếu
        add("YT-0018", "Nhỏ gáy Frontline Plus", "Cho chó 10–20 kg • Hộp 3 ống", "Thuốc & Y tế", 0, "hộp", 385_000, "Tạm hết hàng");
        add("TA-0042", "Royal Canin Kitten 2 kg", "Thức ăn cho mèo con • Túi 2 kg", "Thức ăn", 1, "túi", 465_000, "Đang bán");
        add("PC-0025", "Dây dắt ngực size M", "Nylon mềm • Xám xanh", "Phụ kiện", 2, "bộ", 145_000, "Đang bán");
        add("DC-0012", "Bóng cao su Kong Classic", "Đồ chơi cho chó • Size S", "Đồ chơi", 3, "cái", 195_000, "Đang bán");
        add("YT-0031", "Gel dinh dưỡng Nutri-Plus", "Bổ sung vitamin • Tuýp 120 g", "Thuốc & Y tế", 4, "tuýp", 245_000, "Đang bán");
        add("TA-0068", "Hạt Me-O vị cá ngừ 1,2 kg", "Thức ăn cho mèo trưởng thành", "Thức ăn", 5, "túi", 125_000, "Đang bán");
        add("PC-0047", "Cát vệ sinh Catsan 5 L", "Cát khoáng không mùi • Túi 5 L", "Phụ kiện", 5, "túi", 115_000, "Đang bán");

        // Thêm 5 sản phẩm sắp hết hàng để tổng "Sản phẩm sắp hết" đúng bằng 12 (ton <= 5)
        add("YT-0055", "Xịt trị ve rận Hantox-200", "Lọ 100 ml xịt ngoài da", "Thuốc & Y tế", 5, "lọ", 85_000, "Đang bán");
        add("TA-0089", "Pate Whiskas vị cá thu 80 g", "Hộp 12 gói x 80 g", "Thức ăn", 5, "hộp", 160_000, "Đang bán");
        add("DC-0034", "Cần câu lông vũ chuông", "Đồ chơi vận động mèo", "Đồ chơi", 5, "cái", 45_000, "Đang bán");
        add("PC-0078", "Vòng cổ chống liếm size 2", "Nhựa mềm có đệm viền", "Phụ kiện", 5, "cái", 75_000, "Đang bán");
        add("TA-0105", "Xương gặm sạch răng Pedigree", "Gói 4 thanh cho chó lớn", "Thức ăn", 5, "túi", 95_000, "Đang bán");

        // Các mẫu sản phẩm khác để sinh đủ 320 SKU (46 trang x 7 = 322)
        String[] prefixes = {"TA", "DC", "YT", "PC"};
        String[] categories = {"Thức ăn", "Đồ chơi", "Thuốc & Y tế", "Phụ kiện"};
        String[] dvts = {"túi", "hộp", "cái", "bộ", "tuýp", "chai", "gói"};
        String[] sampleNames = {
            "Hạt SmartHeart cho chó con", "Pate Ciao Churu vị ức gà", "Sữa tắm SOS cho thú cưng 530ml",
            "Lược chải lông rụng tự làm sạch", "Bát ăn đôi inox chống lật", "Khay vệ sinh thành cao cho mèo",
            "Balo phi thuyền vận chuyển thú cưng", "Xương sữa canxi cho chó", "Chuột đồ chơi gắn lò xo",
            "Bột canxi Nano PetPro", "Kìm cắt móng thú cưng chuyên dụng", "Đệm nằm mùa đông lông cừu",
            "Men tiêu hóa Biotic Pet", "Trụ cào móng cây thông", "Tã bỉm cho chó đực size S"
        };
        long[] samplePrices = {85_000, 120_000, 165_000, 210_000, 290_000, 350_000, 420_000, 550_000};

        while (ds.size() < 320) {
            int i = ds.size();
            int catIdx = i % 4;
            String sku = String.format("%s-%04d", prefixes[catIdx], i + 1);
            String name = sampleNames[i % sampleNames.length] + " #" + (i + 1);
            String cat = categories[catIdx];
            String dvt = dvts[i % dvts.length];
            int ton = 10 + (i * 7) % 85; // Tồn kho dồi dào > 5
            long gia = samplePrices[i % samplePrices.length];
            add(sku, name, "Quy cách chuẩn PetCare", cat, ton, dvt, gia, "Đang bán");
        }
    }
}
