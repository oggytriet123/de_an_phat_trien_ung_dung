package Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.DichVuDAO;
import DAO.PhieuKhoDAO;
import Entity.SanPham;

/**
 * Nghiệp vụ quản lý kho hàng: nạp và đồng bộ trực tiếp với CSDL (bảng SANPHAM).
 */
public class KhoHangBUS {
    private final List<SanPham> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final DichVuDAO dao = new DichVuDAO();
    private final PhieuKhoDAO phieuKhoDAO = new PhieuKhoDAO();

    public KhoHangBUS() {
        taiTuCSDL();
    }

    public synchronized void taiLai() {
        taiTuCSDL();
    }

    public synchronized void taiTuCSDL() {
        ds.clear();
        ds.addAll(dao.laySanPhamKho());
        notifyListeners();
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
        long tongNhap = phieuKhoDAO.getTongNhapKhoTrongThang();
        if (tongNhap >= 1_000_000_000) {
            return String.format("%.1f tỷ đ", tongNhap / 1_000_000_000.0);
        } else if (tongNhap >= 1_000_000) {
            return String.format("%.1f triệu đ", tongNhap / 1_000_000.0);
        }
        return String.format("%,d đ", tongNhap);
    }

    public SanPham them(String maSKU, String tenSP, String quyCach, String danhMuc,
                        int ton, String dvt, long donGia, String trangThai) {
        if (maSKU == null || maSKU.trim().isEmpty()) {
            String prefix = switch (danhMuc) {
                case "Thức ăn" -> "FOOD";
                case "Đồ chơi" -> "TOY";
                case "Thuốc" -> "MED";
                case "Chăm sóc và vệ sinh" -> "CARE";
                case "Phụ kiện" -> "ACC";
                case "Đồ dùng và sinh hoạt" -> "LIV";
                default -> "SP";
            };
            maSKU = String.format("%s%03d", prefix, ds.size() + 1);
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

    public List<String> layDanhSachNhaCungCap() {
        List<String> list = new ArrayList<>();
        try {
            for (Entity.DoiTac dt : new DAO.DoiTacDAO().layTatCa()) {
                list.add(dt.getTenDT());
            }
        } catch (Exception ignored) {}
        return list;
    }
}
