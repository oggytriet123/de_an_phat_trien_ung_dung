package Bus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.DonThuocDAO;
import Entity.ChiTietDonThuoc;
import Entity.DonThuoc;

/**
 * Nghiệp vụ quản lý Đơn thuốc phòng khám thú y (đồng bộ trực tiếp CSDL SQL Server).
 */
public class DonThuocBUS {
    private final List<DonThuoc> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final DonThuocDAO dao = new DonThuocDAO();

    public DonThuocBUS() {
        taiTuCSDL();
    }

    public synchronized void taiLai() {
        taiTuCSDL();
    }

    public synchronized void taiTuCSDL() {
        ds.clear();
        ds.addAll(dao.layTatCa());
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

    public List<DonThuoc> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public DonThuoc timTheoMa(String ma) {
        if (ma == null) return null;
        for (DonThuoc dt : ds) {
            if (dt.getMaDonThuoc().equalsIgnoreCase(ma.trim())) return dt;
        }
        return null;
    }

    public synchronized String taoMaMoi() {
        int max = 0;
        for (DonThuoc dt : ds) {
            try {
                String s = dt.getMaDonThuoc().replaceAll("\\D", "");
                if (!s.isEmpty()) max = Math.max(max, Integer.parseInt(s));
            } catch (Exception ignored) {}
        }
        return String.format("DT-%03d", max + 1);
    }

    public DonThuoc them(DonThuoc dt) {
        if (dt.getMaDonThuoc() == null || dt.getMaDonThuoc().trim().isEmpty()) {
            dt.setMaDonThuoc(taoMaMoi());
        }
        dt.ghiLog("Tạo mới đơn thuốc bởi " + dt.getBacSi());
        ds.add(0, dt);
        try {
            dao.them(dt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        notifyListeners();
        return dt;
    }

    public boolean capNhat(DonThuoc dt) {
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getMaDonThuoc().equalsIgnoreCase(dt.getMaDonThuoc())) {
                dt.ghiLog("Cập nhật thông tin đơn thuốc");
                ds.set(i, dt);
                try {
                    dao.capNhat(dt);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public boolean capPhatThuoc(String maDT) {
        return capPhatThuoc(maDT, null);
    }

    public boolean capPhatThuoc(String maDT, KhoHangBUS khoHangBus) {
        DonThuoc dt = timTheoMa(maDT);
        if (dt != null) {
            dt.setTrangThai(DonThuoc.DA_CAP_PHAT);
            dt.ghiLog("Đã xuất kho và cấp phát thuốc cho chủ nuôi");

            if (khoHangBus != null && dt.getDanhSachThuoc() != null) {
                for (ChiTietDonThuoc ct : dt.getDanhSachThuoc()) {
                    if (ct.getMaSKU() != null && !ct.getMaSKU().trim().isEmpty()) {
                        khoHangBus.xuatKho(ct.getMaSKU(), ct.getSoLuong());
                    }
                }
            }

            notifyListeners();
            return true;
        }
        return false;
    }

    public List<DonThuoc> layDonThuocCuaThuCung(String maTC) {
        List<DonThuoc> res = new ArrayList<>();
        if (maTC == null || maTC.trim().isEmpty()) return res;
        for (DonThuoc dt : ds) {
            if (maTC.trim().equalsIgnoreCase(dt.getMaTC())) {
                res.add(dt);
            }
        }
        return res;
    }

    public DonThuoc timTheoPhacDo(String maPD) {
        if (maPD == null || maPD.trim().isEmpty()) return null;
        for (DonThuoc dt : ds) {
            if (maPD.trim().equalsIgnoreCase(dt.getMaPhacDo())) return dt;
        }
        return null;
    }

    public boolean huyDon(String maDT, String lyDo) {
        DonThuoc dt = timTheoMa(maDT);
        if (dt != null) {
            dt.setTrangThai(DonThuoc.DA_HUY);
            dt.ghiLog("Hủy đơn thuốc. Lý do: " + (lyDo == null || lyDo.isEmpty() ? "Không có" : lyDo));
            notifyListeners();
            return true;
        }
        return false;
    }

    public boolean xoa(String maDT) {
        if (maDT == null) return false;
        boolean ok = ds.removeIf(d -> d.getMaDonThuoc().equalsIgnoreCase(maDT));
        try { dao.xoa(maDT); } catch (SQLException e) { e.printStackTrace(); }
        if (ok) notifyListeners();
        return ok;
    }

    // --- Thống kê ---
    public int getTongSoDon() { return ds.size(); }

    public int getSoChoCapThuoc() {
        int cnt = 0;
        for (DonThuoc d : ds) if (DonThuoc.CHO_CAP_THUOC.equals(d.getTrangThai())) cnt++;
        return cnt;
    }

    public int getSoDangDieuTri() {
        int cnt = 0;
        for (DonThuoc d : ds) {
            if (DonThuoc.DANG_DIEU_TRI.equals(d.getTrangThai()) || DonThuoc.DA_CAP_PHAT.equals(d.getTrangThai())) cnt++;
        }
        return cnt;
    }

    public int getSoHoanTat() {
        int cnt = 0;
        for (DonThuoc d : ds) if (DonThuoc.HOAN_TAT.equals(d.getTrangThai())) cnt++;
        return cnt;
    }

    public long getTongDoanhThu() {
        long sum = 0;
        for (DonThuoc d : ds) {
            if (!DonThuoc.DA_HUY.equals(d.getTrangThai())) sum += d.tinhTongTien();
        }
        return sum;
    }
}
