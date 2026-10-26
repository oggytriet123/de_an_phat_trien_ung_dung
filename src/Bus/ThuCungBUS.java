package Bus;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import DAO.ThuCungDAO;
import Entity.ThongKeThuCung;
import Entity.ThuCung;

/**
 * Xử lý nghiệp vụ cho hồ sơ Thú cưng (Validation, Caching, Tìm kiếm, Thống kê).
 */
public class ThuCungBUS {
    private final ThuCungDAO dao = new ThuCungDAO();
    private final List<ThuCung> cache = new ArrayList<>();

    public ThuCungBUS() {
    }

    /**
     * Tải lại toàn bộ dữ liệu từ CSDL vào bộ đệm RAM.
     */
    public synchronized void taiLai() throws SQLException {
        cache.clear();
        cache.addAll(dao.layTatCa());
    }

    /**
     * Lấy danh sách thú cưng đang có trong bộ đệm.
     */
    public synchronized List<ThuCung> layDanhSach() {
        return Collections.unmodifiableList(new ArrayList<>(cache));
    }

    /**
     * Lấy số liệu thống kê.
     */
    public ThongKeThuCung layThongKe() throws SQLException {
        return dao.layThongKe();
    }

    /**
     * Lấy danh sách khách hàng để chọn chủ nuôi.
     */
    public List<ThuCungDAO.KhachHangOption> layDanhSachKhachHang() throws SQLException {
        return dao.layDanhSachKhachHang();
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu trước khi lưu vào CSDL (Business Validation).
     * @throws IllegalArgumentException nếu dữ liệu không thỏa mãn
     */
    public void kiemTraDuLieu(ThuCung tc) {
        if (tc == null) {
            throw new IllegalArgumentException("Dữ liệu thú cưng không được rỗng.");
        }
        if (tc.getTen() == null || tc.getTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thú cưng không được để trống.");
        }
        if (tc.getTen().trim().length() > 100) {
            throw new IllegalArgumentException("Tên thú cưng không vượt quá 100 ký tự.");
        }
        if (tc.getLoai() == null || tc.getLoai().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn hoặc nhập loài thú cưng.");
        }
        if (tc.getLoai().trim().length() > 50) {
            throw new IllegalArgumentException("Tên loài không vượt quá 50 ký tự.");
        }
        if (tc.getGiong() != null && tc.getGiong().trim().length() > 100) {
            throw new IllegalArgumentException("Tên giống loài không vượt quá 100 ký tự.");
        }
        if (tc.getTuoi() < 0 || tc.getTuoi() > 50) {
            throw new IllegalArgumentException("Tuổi thú cưng phải trong khoảng từ 0 đến 50.");
        }
        if (tc.getCanNang() < 0 || tc.getCanNang() > 150) {
            throw new IllegalArgumentException("Cân nặng phải trong khoảng từ 0 đến 150 kg.");
        }
        if (tc.getTheCanhBao() != null && tc.getTheCanhBao().trim().length() > 200) {
            throw new IllegalArgumentException("Nội dung thẻ cảnh báo / ghi chú không vượt quá 200 ký tự.");
        }
    }

    /**
     * Thêm mới thú cưng.
     * @return mã thú cưng mới tạo
     */
    public String them(ThuCung tc) throws SQLException {
        kiemTraDuLieu(tc);
        String petID = dao.them(tc);
        taiLai();
        return petID;
    }

    /**
     * Cập nhật thông tin thú cưng.
     */
    public boolean capNhat(ThuCung tc) throws SQLException {
        if (tc.getPetID() == null || tc.getPetID().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thú cưng không hợp lệ.");
        }
        kiemTraDuLieu(tc);
        boolean ok = dao.capNhat(tc);
        if (ok) {
            taiLai();
        }
        return ok;
    }

    /**
     * Xóa thú cưng.
     */
    public boolean xoa(String petID) throws SQLException {
        if (petID == null || petID.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thú cưng không hợp lệ.");
        }
        boolean ok = dao.xoa(petID);
        if (ok) {
            taiLai();
        }
        return ok;
    }

    /**
     * Lọc danh sách thú cưng theo từ khóa và bộ lọc.
     */
    public List<ThuCung> timKiemVaLoc(String tuKhoa, String loaiLoc, String gioiTinhLoc, String trangThaiLoc) {
        String kw = norm(tuKhoa != null ? tuKhoa.trim() : "");
        String sdtKw = chiSo(tuKhoa);

        List<ThuCung> ketQua = new ArrayList<>();
        for (ThuCung t : cache) {
            // Lọc theo Loài
            if (loaiLoc != null && !loaiLoc.equals("Tất cả")) {
                if (loaiLoc.equals("Khác")) {
                    if (t.getLoai().equalsIgnoreCase("Chó") || t.getLoai().equalsIgnoreCase("Mèo") || t.getLoai().equalsIgnoreCase("Thỏ")) {
                        continue;
                    }
                } else if (!t.getLoai().equalsIgnoreCase(loaiLoc)) {
                    continue;
                }
            }

            // Lọc theo Giới tính
            if (gioiTinhLoc != null && !gioiTinhLoc.equals("Tất cả")) {
                if (!t.getGioiTinh().equalsIgnoreCase(gioiTinhLoc)) {
                    continue;
                }
            }

            // Lọc theo Trạng thái
            if (trangThaiLoc != null && !trangThaiLoc.equals("Tất cả")) {
                if (trangThaiLoc.equals("Cần chú ý / Theo dõi")) {
                    boolean coCanhBao = (t.getTheCanhBao() != null && !t.getTheCanhBao().isEmpty())
                            || !t.getTrangThai().equalsIgnoreCase("Khỏe mạnh");
                    if (!coCanhBao) continue;
                } else if (!t.getTrangThai().equalsIgnoreCase(trangThaiLoc)) {
                    continue;
                }
            }

            // Lọc theo từ khóa tìm kiếm (Tên thú cưng, Mã PET, Giống, Tên chủ, SĐT chủ)
            if (!kw.isEmpty()) {
                String searchHay = norm(t.getTen()) + " "
                        + norm(t.getPetID()) + " "
                        + norm(t.getGiong()) + " "
                        + norm(t.getTenChuNuoi()) + " "
                        + norm(t.getTheCanhBao());

                boolean matchText = searchHay.contains(kw);
                boolean matchPhone = (!sdtKw.isEmpty() && chiSo(t.getSdtChuNuoi()).contains(sdtKw));

                if (!matchText && !matchPhone) {
                    continue;
                }
            }

            ketQua.add(t);
        }
        return ketQua;
    }

    /**
     * Chuẩn hóa chuỗi tiếng Việt không dấu, chữ thường để tìm kiếm linh hoạt.
     */
    public static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    /**
     * Trích xuất các ký tự số (dùng cho số điện thoại).
     */
    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
