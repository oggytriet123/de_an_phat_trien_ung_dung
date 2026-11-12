package Bus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import DAO.DichVuDAO;
import DAO.KhachHangDAO;
import DAO.ThuCungDAO;
import DAO.TiemPhongDAO;
import Entity.KhachHang;
import Entity.PhieuTiem;
import Entity.SanPham;
import Entity.ThuCung;

/**
 * Lớp nghiệp vụ quản lý Tiêm Phòng (chuẩn mô hình 3 lớp).
 */
public class TiemPhongBUS {
    private final TiemPhongDAO tiemPhongDAO = new TiemPhongDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ThuCungDAO thuCungDAO = new ThuCungDAO();

    public List<PhieuTiem> layTatCa() {
        return tiemPhongDAO.layTatCa();
    }

    public List<KhachHang> layDanhSachKhachHang() throws SQLException {
        return khachHangDAO.layTatCa();
    }

    public List<ThuCung> layDanhSachThuCung() throws SQLException {
        return thuCungDAO.layTatCa();
    }

    public List<SanPham> layDichVuTiemPhong() {
        return dichVuDAO.layDichVuTiemPhong();
    }

    public String maTiepTheo() {
        return tiemPhongDAO.maTiepTheo();
    }

    public void them(PhieuTiem pt) throws SQLException {
        tiemPhongDAO.them(pt);
    }

    public void capNhat(PhieuTiem pt) throws SQLException {
        tiemPhongDAO.capNhat(pt);
    }

    public void xoa(String maPhieu) throws SQLException {
        tiemPhongDAO.xoa(maPhieu);
    }
}
