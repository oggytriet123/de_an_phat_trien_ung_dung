package Bus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.PhieuKhoDAO;
import Entity.PhieuKho;

/**
 * Nghiệp vụ quản lý Lịch sử Nhập / Xuất kho:
 * Đồng bộ trực tiếp với CSDL SQL Server (bảng PHIEUKHO, NHANVIEN, DOITAC).
 * - Nhập kho: người thực hiện là nhân viên.
 * - Xuất kho: bên nhận / đối tác là bên đối tác.
 */
public class PhieuKhoBUS {
    private final List<PhieuKho> ds = new ArrayList<>();
    private final PhieuKhoDAO dao = new PhieuKhoDAO();

    public PhieuKhoBUS() {
        taiTuCSDL();
    }

    public synchronized void taiTuCSDL() {
        ds.clear();
        ds.addAll(dao.layTatCa());
    }

    public synchronized void taiLai() {
        taiTuCSDL();
    }

    public List<PhieuKho> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public int getTongSoPhieu() {
        return ds.size();
    }

    public void them(PhieuKho pk) {
        ds.add(0, pk);
        try {
            dao.them(pk);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void xoa(String maPhieu) {
        ds.removeIf(p -> p.getMaPhieu().equalsIgnoreCase(maPhieu));
        try {
            dao.xoa(maPhieu);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String maTiepTheo(boolean isNhap) {
        return dao.maTiepTheo(isNhap);
    }
}
