package Bus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import DAO.DichVuDAO;
import DAO.KhachHangDAO;
import DAO.PhauThuatDAO;
import DAO.ThuCungDAO;
import Entity.CaPhauThuat;
import Entity.KhachHang;
import Entity.SanPham;
import Entity.ThuCung;

/**
 * Lớp nghiệp vụ quản lý Phẫu Thuật (chuẩn mô hình 3 lớp).
 */
public class PhauThuatBUS {
    private final PhauThuatDAO phauThuatDAO = new PhauThuatDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ThuCungDAO thuCungDAO = new ThuCungDAO();

    public List<CaPhauThuat> layTatCa() {
        return phauThuatDAO.layTatCa();
    }

    public List<KhachHang> layDanhSachKhachHang() throws SQLException {
        return khachHangDAO.layTatCa();
    }

    public List<ThuCung> layDanhSachThuCung() throws SQLException {
        return thuCungDAO.layTatCa();
    }

    public List<SanPham> layDichVuPhauThuat() {
        return dichVuDAO.layDichVuPhauThuat();
    }

    public String maTiepTheo() {
        return phauThuatDAO.maTiepTheo();
    }

    public void them(CaPhauThuat c) throws SQLException {
        phauThuatDAO.them(c);
    }

    public void capNhat(CaPhauThuat c) throws SQLException {
        phauThuatDAO.capNhat(c);
    }

    public void xoa(String maCa) throws SQLException {
        phauThuatDAO.xoa(maCa);
    }
}
