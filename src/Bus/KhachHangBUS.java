package Bus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DAO.KhachHangDAO;
import Entity.KhachHang;
import Entity.ThongKeKhachHang;
import Entity.ThuCung;

/** Nghiệp vụ khách hàng, dữ liệu lấy từ SQL Server qua KhachHangDAO. */
public class KhachHangBUS {
    /** Mã lỗi SQL Server khi vi phạm UNIQUE (2601: unique index, 2627: unique constraint). */
    public static boolean laTrungKhoa(SQLException e) {
        return e.getErrorCode() == 2601 || e.getErrorCode() == 2627;
    }

    private final KhachHangDAO dao = new KhachHangDAO();
    private List<KhachHang> ds = new ArrayList<>();

    /** Đọc lại danh sách từ CSDL. Nếu lỗi thì giữ nguyên danh sách đang có. */
    public void taiLai() throws SQLException {
        List<KhachHang> moi = dao.layTatCa();
        for (KhachHang k : moi) k.setSoDienThoai(dinhDangSdt(k.getSoDienThoai()));
        ds = moi;
    }

    public List<KhachHang> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public ThongKeKhachHang layThongKe() throws SQLException {
        return dao.layThongKe();
    }

    public boolean tonTaiSoDienThoai(String sdt) throws SQLException {
        return dao.tonTaiSoDienThoai(chiSo(sdt));
    }

    public boolean tonTaiSoDienThoai(String sdt, String boQuaMaKH) throws SQLException {
        return dao.tonTaiSoDienThoai(chiSo(sdt), boQuaMaKH);
    }

    /** Tìm khách theo SĐT trực tiếp trong CSDL (kèm thú cưng); null nếu chưa có. */
    public KhachHang timTheoSdt(String sdt) throws SQLException {
        KhachHang k = dao.timTheoSdt(chiSo(sdt));
        if (k != null) k.setSoDienThoai(dinhDangSdt(k.getSoDienThoai()));
        return k;
    }

    /** Thêm khách hàng cùng thú cưng; hạng thẻ tự tính theo điểm. @return mã KH mới */
    public String them(String hoTen, String sdt, int diem, List<ThuCung> thuCung) throws SQLException {
        return dao.them(hoTen, chiSo(sdt), diem, thuCung == null ? new ArrayList<>() : thuCung);
    }

    /** Cập nhật thông tin khách hàng (họ tên, SĐT, điểm). */
    public boolean capNhat(String maKH, String hoTen, String sdt, int diem) throws SQLException {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        }
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống.");
        }
        String so = chiSo(sdt);
        if (so.length() < 9 || so.length() > 11) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 9–11 chữ số.");
        }
        if (dao.tonTaiSoDienThoai(so, maKH)) {
            throw new IllegalArgumentException("Số điện thoại này đã được dùng bởi khách hàng khác.");
        }
        boolean ok = dao.capNhat(maKH, hoTen.trim(), so, Math.max(0, diem));
        if (ok) taiLai();
        return ok;
    }

    /** Xóa khách hàng có kiểm tra ràng buộc. */
    public boolean xoa(String maKH) throws SQLException {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        }
        boolean ok = dao.xoa(maKH);
        if (ok) taiLai();
        return ok;
    }

    /** Hạng thẻ theo điểm: Thường (<100) · Bạc (100) · Vàng (200) · VIP (500). */
    public static String tinhHang(int diem) {
        return KhachHangDAO.tinhHang(diem);
    }

    /** Số điểm nhận được khi thanh toán số tiền này (1 điểm / 10.000₫). */
    public static long diemTuSoTien(long soTien) {
        return Math.max(0, soTien / KhachHangDAO.SO_TIEN_MOI_DIEM);
    }

    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    public static String dinhDangSdt(String s) {
        String d = chiSo(s);
        return d.length() == 10 ? d.substring(0, 4) + " " + d.substring(4, 7) + " " + d.substring(7) : d;
    }
}
