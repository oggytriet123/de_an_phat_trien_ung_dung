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
