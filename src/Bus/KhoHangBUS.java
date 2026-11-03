package Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.DichVuDAO;
import Entity.SanPham;

/**
 * Nghiệp vụ quản lý kho hàng: nạp và đồng bộ trực tiếp với CSDL (bảng SANPHAM).
 */
public class KhoHangBUS {
    private final List<SanPham> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final DichVuDAO dao = new DichVuDAO();

    public KhoHangBUS() {
        taiTuCSDL();
    }

    public synchronized void taiLai() {
        taiTuCSDL();
    }

    public synchronized void taiTuCSDL() {
        ds.clear();
        ds.addAll(dao.laySanPhamKho());
        if (ds.isEmpty()) {
            napDuLieuMauKho();
        }
        notifyListeners();
    }

    private void napDuLieuMauKho() {
        String[][] spData = {
            {"SP001", "Thức ăn hạt Royal Canin Mini Adult 2kg", "Túi 2kg", "Thức ăn", "50", "bao", "280000"},
            {"SP002", "Pate Whiskas cá thu & cá hồi 85g", "Gói 85g", "Thức ăn", "120", "gói", "25000"},
            {"SP003", "Cát vệ sinh Cature đậu nành tự nhiên 6L", "Túi 6L", "Phụ kiện", "35", "túi", "145000"},
            {"SP004", "Sữa tắm trị ve rận Bio Derma 250ml", "Chai 250ml", "Thuốc & Y tế", "25", "chai", "95000"},
            {"SP005", "Vòng cổ trị ve rận Seresto cho chó", "Hộp 1 cái", "Phụ kiện", "18", "cái", "320000"},
            {"SP006", "Xương gặm sạch răng Pedigree DentaStix 110g", "Gói 110g", "Thức ăn", "60", "gói", "45000"},
            {"SP007", "Dung dịch nhỏ mắt Bioline 50ml", "Lọ 50ml", "Thuốc & Y tế", "40", "chai", "65000"},
            {"SP008", "Gel dinh dưỡng Nourse 120g", "Tuýp 120g", "Thuốc & Y tế", "22", "tuýp", "120000"},
            {"SP009", "Bánh thưởng vị thịt gà SmartHeart 100g", "Gói 100g", "Thức ăn", "75", "gói", "38000"},
            {"SP010", "Cần câu mèo lông vũ gắn chuông", "Cây 50cm", "Đồ chơi", "45", "cái", "35000"},
            {"SP011", "Bóng cao su đồ chơi gặm sạch răng cho chó", "Quả đk 7cm", "Đồ chơi", "30", "cái", "45000"},
            {"SP012", "Thuốc tẩy giun Sanpet cho chó mèo", "Vỉ 10 viên", "Thuốc & Y tế", "85", "viên", "35000"},
            {"SP013", "Xịt khử mùi chuồng trại & khay cát Forcans 300ml", "Chai 300ml", "Phụ kiện", "20", "chai", "110000"},
            {"SP014", "Bát ăn chống trượt inox 2 ngăn cho thú cưng", "Cái 2 ngăn", "Phụ kiện", "32", "cái", "75000"},
            {"SP015", "Lược chải lông tự làm sạch Pet Brush", "Cây cầm tay", "Phụ kiện", "28", "cái", "85000"},
            {"SP016", "Dây dắt kèm yếm ngực phản quang cho chó", "Bộ dây + yếm", "Phụ kiện", "24", "bộ", "125000"},
            {"SP017", "Hạt Me-O vị cá ngừ cho mèo lớn 1.2kg", "Túi 1.2kg", "Thức ăn", "40", "bao", "135000"},
            {"SP018", "Cỏ tươi Catnip sấy khô cho mèo", "Hũ 45g", "Đồ chơi", "50", "hũ", "50000"},
            {"SP019", "Tã bỉm thú cưng Dono size M (12 miếng)", "Bịch 12 miếng", "Phụ kiện", "35", "gói", "90000"},
            {"SP020", "Bột điện giải và men vi sinh Biotic 5g", "Gói 5g", "Thuốc & Y tế", "100", "gói", "15000"}
        };
        for (String[] r : spData) {
            ds.add(new SanPham(r[0], r[1], r[2], r[3], Integer.parseInt(r[4]), r[5], Long.parseLong(r[6]), "Đang bán"));
        }
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
        long tong = 0;
        for (SanPham sp : ds) {
            tong += (sp.getDonGia() * Math.max(0, sp.getSoLuongTon()));
        }
        if (tong >= 1_000_000_000) {
            return String.format("%.1f tỷ đ", tong / 1_000_000_000.0);
        } else if (tong >= 1_000_000) {
            return String.format("%.1f triệu đ", tong / 1_000_000.0);
        }
        return String.format("%,d đ", tong);
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
}
