package Bus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import DAO.LichHenDAO;
import Entity.LichHen;

/**
 * Nghiệp vụ quản lý lịch hẹn — trung gian giữa GUI và LichHenDAO.
 */
public class LichHenBUS {
    private final LichHenDAO dao = new LichHenDAO();
    private final List<LichHen> ds = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public LichHenBUS() {
        taiLai();
    }

    public void taiLai() {
        ds.clear();
        ds.addAll(dao.layTatCa());
    }

    public List<LichHen> layDanhSach() {
        return new ArrayList<>(ds);
    }

    public String maTiepTheo() {
        return dao.maTiepTheo();
    }

    public boolean biTrung(String bacSi, LocalDate ngay, LocalTime gio, String boQuaMa) {
        for (LichHen l : ds) {
            if (l.ma.equals(boQuaMa) || !l.conHieuLuc()) continue;
            if (l.bacSi.equals(bacSi) && l.ngay.equals(ngay) && l.gio.equals(gio)) return true;
        }
        return false;
    }

    public void them(LichHen l) {
        ds.add(l);
        try {
            dao.them(l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void capNhat(LichHen l) {
        try {
            dao.capNhat(l);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void xoa(LichHen l) {
        ds.remove(l);
        try {
            dao.xoa(l.ma);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addListener(Runnable r) {
        if (r != null && !listeners.contains(r)) listeners.add(r);
    }

    public void notifyListeners() {
        for (Runnable r : new ArrayList<>(listeners)) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }
}
