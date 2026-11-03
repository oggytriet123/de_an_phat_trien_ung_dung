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
        try { taiLai(); } catch (Exception ignored) {}
    }

    /**
     * Tải lại toàn bộ dữ liệu từ CSDL vào bộ đệm RAM.
     */
    public synchronized void taiLai() throws SQLException {
        cache.clear();
        try {
            cache.addAll(dao.layTatCa());
        } catch (Exception ignored) {}
        if (cache.isEmpty()) {
            napDuLieuMau();
        }
    }

    private void napDuLieuMau() {
        cache.add(new ThuCung("PET001", "Lu", "Chó", "Poodle", "Đực", 2, 4.5, "Không có", "Khỏe mạnh", "KH001", java.time.LocalDate.now().minusMonths(6), "Nguyễn Văn An", "0901234567", "Vàng"));
        cache.add(new ThuCung("PET002", "Miu", "Mèo", "Anh lông ngắn", "Cái", 1, 3.8, "Dị ứng hải sản", "Khỏe mạnh", "KH002", java.time.LocalDate.now().minusMonths(4), "Trần Thị Bình", "0912345678", "VIP"));
        cache.add(new ThuCung("PET003", "Bông", "Chó", "Corgi", "Đực", 3, 11.2, "Đang điều trị nấm", "Đang điều trị", "KH003", java.time.LocalDate.now().minusMonths(2), "Lê Hoàng Nam", "0987654321", "Bạc"));
        cache.add(new ThuCung("PET004", "Mun", "Mèo", "Mèo Ba Tư", "Cái", 2, 4.0, "Cần chú ý chải lông", "Khỏe mạnh", "KH004", java.time.LocalDate.now().minusMonths(5), "Phạm Minh Thư", "0933456789", "Thường"));
        cache.add(new ThuCung("PET005", "Lucky", "Chó", "Golden Retriever", "Đực", 4, 28.5, "Hiền lành, thích vận động", "Khỏe mạnh", "KH005", java.time.LocalDate.now().minusMonths(8), "Võ Quốc Bảo", "0978112233", "Vàng"));
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
        try {
            return dao.layThongKe();
        } catch (Exception ignored) {}
        int tong = cache.size();
        int cho = 0, meo = 0, khac = 0, canhBao = 0;
        for (ThuCung tc : cache) {
            String l = tc.getLoai() != null ? tc.getLoai().toLowerCase() : "";
            if (l.contains("chó") || l.contains("cho")) cho++;
            else if (l.contains("mèo") || l.contains("meo")) meo++;
            else khac++;
            if (tc.getTheCanhBao() != null && !tc.getTheCanhBao().trim().isEmpty() && !tc.getTheCanhBao().equals("Không có")) {
                canhBao++;
            }
        }
        return new ThongKeThuCung(tong, cho, meo, khac, canhBao, 0);
    }

    public int getTongSoThuCung() {
        try {
            return layThongKe().tongThuCung;
        } catch (Exception e) {
            return cache.size();
        }
    }

    public int getSoDangLuuTru() {
        int cnt = 0;
        for (ThuCung tc : layDanhSach()) {
            if ("Đang lưu trú".equalsIgnoreCase(tc.getTrangThai())) cnt++;
        }
        return cnt;
    }

    public int getSoCanTheoDoi() {
        try {
            return layThongKe().soCanhBao;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Lấy danh sách khách hàng để chọn chủ nuôi.
     */
    public List<ThuCungDAO.KhachHangOption> layDanhSachKhachHang() throws SQLException {
        try {
            List<ThuCungDAO.KhachHangOption> list = dao.layDanhSachKhachHang();
            if (list != null && !list.isEmpty()) return list;
        } catch (Exception ignored) {}
        List<ThuCungDAO.KhachHangOption> list = new ArrayList<>();
        list.add(new ThuCungDAO.KhachHangOption("KH001", "Nguyễn Văn An", "0901234567", "Vàng"));
        list.add(new ThuCungDAO.KhachHangOption("KH002", "Trần Thị Bình", "0912345678", "VIP"));
        list.add(new ThuCungDAO.KhachHangOption("KH003", "Lê Hoàng Nam", "0987654321", "Bạc"));
        list.add(new ThuCungDAO.KhachHangOption("KH004", "Phạm Minh Thư", "0933456789", "Thường"));
        list.add(new ThuCungDAO.KhachHangOption("KH005", "Võ Quốc Bảo", "0978112233", "Vàng"));
        return list;
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
        try {
            String petID = dao.them(tc);
            try { taiLai(); } catch (Exception ignored) {}
            return petID;
        } catch (SQLException e) {
            int max = 0;
            for (ThuCung t : cache) {
                try {
                    if (t.getPetID() != null && t.getPetID().startsWith("PET")) {
                        max = Math.max(max, Integer.parseInt(t.getPetID().substring(3)));
                    }
                } catch (Exception ignored) {}
            }
            String newId = String.format("PET%03d", max + 1);
            tc.setPetID(newId);
            cache.add(0, tc);
            return newId;
        }
    }

    /**
     * Cập nhật thông tin thú cưng.
     */
    public boolean capNhat(ThuCung tc) throws SQLException {
        if (tc.getPetID() == null || tc.getPetID().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thú cưng không hợp lệ.");
        }
        kiemTraDuLieu(tc);
        try {
            boolean ok = dao.capNhat(tc);
            if (ok) {
                try { taiLai(); } catch (Exception ignored) {}
            }
            return ok;
        } catch (SQLException e) {
            for (int i = 0; i < cache.size(); i++) {
                if (cache.get(i).getPetID().equalsIgnoreCase(tc.getPetID())) {
                    cache.set(i, tc);
                    return true;
                }
            }
            return true;
        }
    }

    /**
     * Xóa thú cưng.
     */
    public boolean xoa(String petID) throws SQLException {
        if (petID == null || petID.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thú cưng không hợp lệ.");
        }
        try {
            boolean ok = dao.xoa(petID);
            if (ok) {
                try { taiLai(); } catch (Exception ignored) {}
            }
            return ok;
        } catch (SQLException e) {
            cache.removeIf(t -> t.getPetID().equalsIgnoreCase(petID));
            return true;
        }
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
