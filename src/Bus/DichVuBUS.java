package Bus;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import DAO.DichVuDAO;
import Entity.DichVu;
import Entity.ThongKeDichVu;

/**
 * Xử lý nghiệp vụ cho danh mục Dịch vụ (Validation, Caching, Tìm kiếm, Thống kê).
 */
public class DichVuBUS {
    private final DichVuDAO dao = new DichVuDAO();
    private final List<DichVu> cache = new ArrayList<>();

    public DichVuBUS() {
    }

    public synchronized void taiLai() throws SQLException {
        cache.clear();
        cache.addAll(dao.layTatCa());
    }

    public synchronized List<DichVu> layDanhSach() {
        return Collections.unmodifiableList(new ArrayList<>(cache));
    }

    public ThongKeDichVu layThongKe() throws SQLException {
        return dao.layThongKe();
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu dịch vụ.
     */
    public void kiemTraDuLieu(DichVu dv) {
        if (dv == null) {
            throw new IllegalArgumentException("Dữ liệu dịch vụ không được rỗng.");
        }
        if (dv.getTenDV() == null || dv.getTenDV().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (dv.getTenDV().trim().length() > 100) {
            throw new IllegalArgumentException("Tên dịch vụ không được vượt quá 100 ký tự.");
        }
        if (dv.getGiaTien() < 0) {
            throw new IllegalArgumentException("Giá dịch vụ không được âm.");
        }
        if (dv.getGiaTien() > 100_000_000) {
            throw new IllegalArgumentException("Giá dịch vụ không được vượt quá 100.000.000₫.");
        }
        if (dv.getThoiGianThucHien() <= 0) {
            throw new IllegalArgumentException("Thời gian thực hiện dịch vụ phải lớn hơn 0 phút.");
        }
        if (dv.getThoiGianThucHien() > 10080) { // 7 ngày
            throw new IllegalArgumentException("Thời gian thực hiện không được vượt quá 7 ngày (10.080 phút).");
        }
        if (dv.getMoTa() != null && dv.getMoTa().trim().length() > 500) {
            throw new IllegalArgumentException("Mô tả dịch vụ không được vượt quá 500 ký tự.");
        }
    }

    public String them(DichVu dv) throws SQLException {
        kiemTraDuLieu(dv);
        String maDV = dao.them(dv);
        taiLai();
        return maDV;
    }

    public boolean capNhat(DichVu dv) throws SQLException {
        if (dv.getMaDV() == null || dv.getMaDV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
        }
        kiemTraDuLieu(dv);
        boolean ok = dao.capNhat(dv);
        if (ok) {
            taiLai();
        }
        return ok;
    }

    public boolean xoa(String maDV) throws SQLException {
        if (maDV == null || maDV.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
        }
        boolean ok = dao.xoa(maDV);
        if (ok) {
            taiLai();
        }
        return ok;
    }

    /**
     * Tìm kiếm và lọc đa tiêu chí.
     */
    public List<DichVu> timKiemVaLoc(String tuKhoa, String loaiLoc, String trangThaiLoc, String doiTuongLoc) {
        String kw = norm(tuKhoa != null ? tuKhoa.trim() : "");

        List<DichVu> ketQua = new ArrayList<>();
        for (DichVu dv : cache) {
            // Lọc theo Nhóm loại
            if (loaiLoc != null && !loaiLoc.equals("Tất cả")) {
                if (!dv.getLoaiDV().equalsIgnoreCase(loaiLoc)) {
                    continue;
                }
            }

            // Lọc theo Trạng thái
            if (trangThaiLoc != null && !trangThaiLoc.equals("Tất cả")) {
                if (!dv.getTrangThai().equalsIgnoreCase(trangThaiLoc)) {
                    continue;
                }
            }

            // Lọc theo Đối tượng
            if (doiTuongLoc != null && !doiTuongLoc.equals("Tất cả")) {
                if (!dv.getDoiTuong().equalsIgnoreCase(doiTuongLoc)) {
                    continue;
                }
            }

            // Tìm kiếm theo từ khóa (Tên dịch vụ, Mã DV, Mô tả, Đối tượng)
            if (!kw.isEmpty()) {
                String searchHay = norm(dv.getTenDV()) + " "
                        + norm(dv.getMaDV()) + " "
                        + norm(dv.getLoaiDV()) + " "
                        + norm(dv.getMoTa()) + " "
                        + norm(dv.getDoiTuong());

                if (!searchHay.contains(kw)) {
                    continue;
                }
            }

            ketQua.add(dv);
        }
        return ketQua;
    }

    public static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }
}
