package Bus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.PhacDoDAO;
import Entity.PhacDoDieuTri;

/**
 * Nghiệp vụ quản lý Phác đồ điều trị chuyên sâu cho thú cưng (đồng bộ trực tiếp CSDL SQL Server).
 */
public class PhacDoBUS {
    private final List<PhacDoDieuTri> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final PhacDoDAO dao = new PhacDoDAO();

    public PhacDoBUS() {
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

    public List<PhacDoDieuTri> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public PhacDoDieuTri timTheoMa(String ma) {
        if (ma == null) return null;
        for (PhacDoDieuTri pd : ds) {
            if (pd.getMaPhacDo().equalsIgnoreCase(ma.trim())) return pd;
        }
        return null;
    }

    public synchronized String taoMaMoi() {
        int max = 0;
        for (PhacDoDieuTri pd : ds) {
            try {
                String s = pd.getMaPhacDo().replaceAll("\\D", "");
                if (!s.isEmpty()) max = Math.max(max, Integer.parseInt(s));
            } catch (Exception ignored) {}
        }
        return String.format("PD-%03d", max + 1);
    }

    public PhacDoDieuTri them(PhacDoDieuTri pd) {
        if (pd.getMaPhacDo() == null || pd.getMaPhacDo().trim().isEmpty()) {
            pd.setMaPhacDo(taoMaMoi());
        }
        pd.ghiLog("Thiết lập phác đồ điều trị bởi " + pd.getBacSi());
        ds.add(0, pd);
        try {
            dao.them(pd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        notifyListeners();
        return pd;
    }

    public boolean capNhat(PhacDoDieuTri pd) {
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getMaPhacDo().equalsIgnoreCase(pd.getMaPhacDo())) {
                pd.ghiLog("Cập nhật thông tin phác đồ điều trị");
                ds.set(i, pd);
                try {
                    dao.capNhat(pd);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public boolean capNhatTienDo(String maPD, int tienDoMoi, String trangThaiMoi) {
        PhacDoDieuTri pd = timTheoMa(maPD);
        if (pd != null) {
            int clamped = Math.min(100, Math.max(0, tienDoMoi));
            pd.setTienDo(clamped);
            if (clamped == 100) {
                pd.setTrangThai(PhacDoDieuTri.HOAN_THANH);
            } else if (trangThaiMoi != null && !trangThaiMoi.isEmpty()) {
                pd.setTrangThai(trangThaiMoi);
            }
            pd.ghiLog("Cập nhật tiến độ: " + clamped + "% (Trạng thái: " + pd.getTrangThai() + ")");
            try {
                dao.capNhat(pd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            notifyListeners();
            return true;
        }
        return false;
    }

    public List<PhacDoDieuTri> layPhacDoCuaThuCung(String maTC) {
        List<PhacDoDieuTri> res = new ArrayList<>();
        if (maTC == null || maTC.trim().isEmpty()) return res;
        for (PhacDoDieuTri pd : ds) {
            if (pd.getMaTC() != null && pd.getMaTC().equalsIgnoreCase(maTC.trim())) {
                res.add(pd);
            }
        }
        return res;
    }

    public boolean xoa(String maPD) {
        if (maPD == null) return false;
        boolean ok = ds.removeIf(pd -> pd.getMaPhacDo().equalsIgnoreCase(maPD));
        try { dao.xoa(maPD); } catch (SQLException e) { e.printStackTrace(); }
        if (ok) notifyListeners();
        return ok;
    }

    public boolean themNhatKy(String maPD, Entity.NhatKyTheoDoi nk) {
        PhacDoDieuTri pd = timTheoMa(maPD);
        if (pd != null && nk != null) {
            pd.themNhatKy(nk);
            try { dao.capNhat(pd); } catch (SQLException e) { e.printStackTrace(); }
            notifyListeners();
            return true;
        }
        return false;
    }

    // --- Thống kê ---
    public int getTongSoPhacDo() {
        return ds.size();
    }

    public int getSoDangDieuTri() {
        int cnt = 0;
        for (PhacDoDieuTri pd : ds) {
            if (PhacDoDieuTri.DANG_DIEU_TRI.equalsIgnoreCase(pd.getTrangThai())) cnt++;
        }
        return cnt;
    }

    public int getSoTheoDoi() {
        int cnt = 0;
        for (PhacDoDieuTri pd : ds) {
            if (PhacDoDieuTri.THEO_DOI_TICH_CUC.equalsIgnoreCase(pd.getTrangThai())) cnt++;
        }
        return cnt;
    }

    public int getTiLeHoanThanh() {
        if (ds.isEmpty()) return 0;
        int h = 0;
        for (PhacDoDieuTri pd : ds) {
            if (PhacDoDieuTri.HOAN_THANH.equalsIgnoreCase(pd.getTrangThai()) || pd.getTienDo() >= 100) h++;
        }
        return (int) Math.round(h * 100.0 / ds.size());
    }
}
