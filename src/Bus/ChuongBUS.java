package Bus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import DAO.ChuongDAO;
import Entity.ChiTietDichVuChuong;
import Entity.Chuong;
import Entity.PhieuDatChuong;

/**
 * Lớp xử lý nghiệp vụ quản lý lồng chuồng lưu trú thú cưng (Pet Hotel / Boarding BUS).
 */
public class ChuongBUS {
    private final ChuongDAO dao;

    public ChuongBUS() {
        this.dao = new ChuongDAO();
    }

    public List<Chuong> layDanhSach() {
        return dao.getAllChuong();
    }

    public Chuong timTheoMa(String maChuong) {
        return dao.getChuong(maChuong);
    }

    /**
     * Lọc danh sách lồng theo trạng thái, kích cỡ, khu vực và từ khóa tìm kiếm.
     */
    public List<Chuong> locChuong(String filterTrangThai, String filterSize, String filterKhuVuc, String tuKhoa) {
        List<Chuong> all = dao.getAllChuong();
        List<Chuong> result = new ArrayList<>();

        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();

        for (Chuong c : all) {
            // Lọc trạng thái
            boolean matchTT = "Tất cả".equalsIgnoreCase(filterTrangThai) ||
                    (c.getTrangThai() != null && c.getTrangThai().equalsIgnoreCase(filterTrangThai));

            // Lọc kích cỡ / size
            boolean matchSize = "Tất cả".equalsIgnoreCase(filterSize) ||
                    (c.getSize() != null && c.getSize().toLowerCase().contains(filterSize.toLowerCase()));

            // Lọc khu vực
            boolean matchKV = "Tất cả".equalsIgnoreCase(filterKhuVuc) ||
                    (c.getKhuVuc() != null && c.getKhuVuc().equalsIgnoreCase(filterKhuVuc));

            // Lọc từ khóa: mã lồng, tên lồng, tên pet, tên khách, số điện thoại
            boolean matchKW = kw.isEmpty() ||
                    (c.getMaChuong() != null && c.getMaChuong().toLowerCase().contains(kw)) ||
                    (c.getTenChuong() != null && c.getTenChuong().toLowerCase().contains(kw)) ||
                    (c.getTenPet() != null && c.getTenPet().toLowerCase().contains(kw)) ||
                    (c.getLoaiPet() != null && c.getLoaiPet().toLowerCase().contains(kw)) ||
                    (c.getTenKhach() != null && c.getTenKhach().toLowerCase().contains(kw)) ||
                    (c.getSdtKhach() != null && c.getSdtKhach().contains(kw));

            if (matchTT && matchSize && matchKV && matchKW) {
                result.add(c);
            }
        }
        return result;
    }

    public int demTongSoChuong() {
        return dao.getAllChuong().size();
    }

    public int demChuongTheoTrangThai(String trangThai) {
        int cnt = 0;
        for (Chuong c : dao.getAllChuong()) {
            if ("Đang lưu trú".equalsIgnoreCase(trangThai)) {
                if (c.isDangLuuTru()) cnt++;
            } else if ("Trống".equalsIgnoreCase(trangThai)) {
                if (c.isTrong()) cnt++;
            } else if ("Đã đặt".equalsIgnoreCase(trangThai)) {
                if (c.isDaDat()) cnt++;
            } else if ("Bảo trì".equalsIgnoreCase(trangThai)) {
                if (c.isBaoTri()) cnt++;
            } else {
                if (trangThai.equalsIgnoreCase(c.getTrangThai())) cnt++;
            }
        }
        return cnt;
    }

    public boolean datChuong(PhieuDatChuong pdc) {
        return dao.datChuong(pdc);
    }

    public boolean nhanChuong(String maChuong, String tenPet, String loaiPet, double canNang,
                             String tenKhach, String sdtKhach, String ghiChu, LocalDateTime ngayRaDuKien) {
        return dao.nhanChuong(maChuong, tenPet, loaiPet, canNang, tenKhach, sdtKhach, ghiChu, ngayRaDuKien);
    }

    public boolean huyDat(String maChuong) {
        return dao.huyDat(maChuong);
    }

    public boolean chuyenChuong(String maChuongCu, String maChuongMoi) {
        return dao.chuyenChuong(maChuongCu, maChuongMoi);
    }

    /** Trả lồng + thanh toán: lập hóa đơn, cộng điểm cho chủ nuôi rồi đưa lồng về Trống. */
    public boolean donChuong(String maChuong) {
        double tong = tinhTongChiPhi(timTheoMa(maChuong));
        return dao.donChuong(maChuong, tong);
    }

    public List<PhieuDatChuong> layDanhSachPhieuDat() {
        return dao.getAllPhieuDat();
    }

    public List<ChiTietDichVuChuong> layDichVuCuaChuong(String maChuong) {
        return dao.getDichVuCuaChuong(maChuong);
    }

    public void themDichVu(String maChuong, ChiTietDichVuChuong dv) {
        dao.themDichVu(maChuong, dv);
    }

    public void xoaDichVu(String maChuong, int index) {
        dao.xoaDichVu(maChuong, index);
    }

    /**
     * Tính tổng chi phí lưu trú tạm tính (tiền phòng theo số ngày + tiền dịch vụ).
     */
    public double tinhTongChiPhi(Chuong c) {
        if (c == null) return 0;
        double tienPhong = 0;
        if (c.getNgayVao() != null) {
            long soGio = Math.max(1, Duration.between(c.getNgayVao(), LocalDateTime.now()).toHours());
            long soNgay = (soGio + 23) / 24; // Làm tròn lên 1 ngày
            if (soNgay <= 0) soNgay = 1;
            tienPhong = soNgay * c.getGiaTheoNgay();
        } else {
            tienPhong = c.getGiaTheoNgay();
        }

        double tienDV = 0;
        List<ChiTietDichVuChuong> listDV = dao.getDichVuCuaChuong(c.getMaChuong());
        for (ChiTietDichVuChuong dv : listDV) {
            tienDV += dv.getThanhTien();
        }

        return tienPhong + tienDV;
    }

    public long tinhSoNgayLuuTru(Chuong c) {
        if (c == null || c.getNgayVao() == null) return 1;
        long soGio = Math.max(1, Duration.between(c.getNgayVao(), LocalDateTime.now()).toHours());
        long soNgay = (soGio + 23) / 24;
        return Math.max(1, soNgay);
    }
}
