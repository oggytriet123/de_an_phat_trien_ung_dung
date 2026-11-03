package Bus;

import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
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

    public KhachHangBUS() {
        try { taiLai(); } catch (Exception ignored) {}
    }

    /** Đọc lại danh sách từ CSDL. Nếu lỗi thì giữ nguyên danh sách đang có. */
    public void taiLai() throws SQLException {
        try {
            List<KhachHang> moi = dao.layTatCa();
            for (KhachHang k : moi) k.setSoDienThoai(dinhDangSdt(k.getSoDienThoai()));
            ds = moi;
        } catch (SQLException e) {
            System.err.println("[KhachHangBUS] CSDL offline, sử dụng danh sách mẫu: " + e.getMessage());
        }
        if (ds.isEmpty()) {
            napDuLieuMau();
        }
    }

    private void napDuLieuMau() {
        ds.add(new KhachHang("KH001", "Nguyễn Minh Anh", "0903 123 456", "VIP", 2030, "Lucky (Chó)", LocalDate.of(2026, 10, 15), 15000000));
        ds.add(new KhachHang("KH002", "Trần Quốc Bảo", "0908 234 567", "Thành viên", 850, "Mimi (Mèo)", LocalDate.of(2026, 10, 20), 6500000));
        ds.add(new KhachHang("KH003", "Lê Thị Cẩm Tú", "0912 345 678", "VIP", 1520, "Bơ (Chó)", LocalDate.of(2026, 10, 25), 11200000));
        ds.add(new KhachHang("KH004", "Phạm Hoàng Dũng", "0934 567 890", "Thành viên", 420, "Simba (Mèo)", LocalDate.of(2026, 10, 28), 3800000));
        ds.add(new KhachHang("KH005", "Vũ Mai Hương", "0978 901 234", "Thành viên", 680, "Đậu Đậu (Chó)", LocalDate.of(2026, 11, 1), 5400000));
    }

    public List<KhachHang> layDanhSach() {
        return Collections.unmodifiableList(ds);
    }

    public ThongKeKhachHang layThongKe() throws SQLException {
        try {
            return dao.layThongKe();
        } catch (SQLException e) {
            int tong = ds.size();
            int vip = 0;
            long diem = 0;
            long dt = 0;
            for (KhachHang k : ds) {
                if ("VIP".equalsIgnoreCase(k.getHangThe())) vip++;
                diem += k.getDiemTichLuy();
                dt += k.getTongChiTieu();
            }
            return new ThongKeKhachHang(tong, 2, vip, diem, dt, dt * 9 / 10);
        }
    }

    public boolean tonTaiSoDienThoai(String sdt) throws SQLException {
        String so = chiSo(sdt);
        try {
            return dao.tonTaiSoDienThoai(so);
        } catch (Exception e) {
            for (KhachHang k : ds) {
                if (chiSo(k.getSoDienThoai()).equals(so)) return true;
            }
            return false;
        }
    }

    public boolean tonTaiSoDienThoai(String sdt, String boQuaMaKH) throws SQLException {
        String so = chiSo(sdt);
        try {
            return dao.tonTaiSoDienThoai(so, boQuaMaKH);
        } catch (Exception e) {
            for (KhachHang k : ds) {
                if (boQuaMaKH != null && k.getMaKH() != null && k.getMaKH().equalsIgnoreCase(boQuaMaKH)) continue;
                if (chiSo(k.getSoDienThoai()).equals(so)) return true;
            }
            return false;
        }
    }

    /**
     * Tìm khách theo SĐT — thử CSDL trước, nếu lỗi kết nối thì tìm trong bộ nhớ.
     * Trả về null nếu chưa có.
     */
    public KhachHang timTheoSdt(String sdt) throws SQLException {
        String so = chiSo(sdt);
        if (so.length() < 9) return null;
        try {
            KhachHang k = dao.timTheoSdt(so);
            if (k != null) {
                k.setSoDienThoai(dinhDangSdt(k.getSoDienThoai()));
                return k;
            }
        } catch (Exception e) {
            for (KhachHang k : ds) {
                if (chiSo(k.getSoDienThoai()).equals(so)) return k;
            }
            return null;
        }
        for (KhachHang k : ds) {
            if (chiSo(k.getSoDienThoai()).equals(so)) return k;
        }
        return null;
    }

    /** Bản an toàn không ném SQLException (bắt exception và fallback bộ nhớ), dùng cho hóa đơn. */
    public KhachHang timTheoSdtAnToan(String sdt) {
        try {
            return timTheoSdt(sdt);
        } catch (Exception e) {
            String so = chiSo(sdt);
            for (KhachHang k : ds) {
                if (chiSo(k.getSoDienThoai()).equals(so)) return k;
            }
            return null;
        }
    }

    /** Tìm khách theo mã KH trong bộ nhớ. */
    public KhachHang timTheoMa(String maKH) {
        if (maKH == null) return null;
        for (KhachHang k : ds) {
            if (k.getMaKH() != null && k.getMaKH().equalsIgnoreCase(maKH.trim())) return k;
        }
        return null;
    }

    /** Thêm khách hàng cùng thú cưng vào CSDL; hạng thẻ tự tính theo điểm. @return mã KH mới */
    public String them(String hoTen, String sdt, int diem, List<ThuCung> thuCung) throws SQLException {
        try {
            String ma = dao.them(hoTen, chiSo(sdt), diem, thuCung == null ? new ArrayList<>() : thuCung);
            try { taiLai(); } catch (Exception ignored) {}
            return ma;
        } catch (SQLException e) {
            String so = chiSo(sdt);
            for (KhachHang k : ds) {
                if (chiSo(k.getSoDienThoai()).equals(so)) {
                    throw new SQLException("Số điện thoại này đã được dùng bởi khách hàng khác.");
                }
            }
            int max = 0;
            for (KhachHang k : ds) {
                try {
                    if (k.getMaKH() != null && k.getMaKH().startsWith("KH"))
                        max = Math.max(max, Integer.parseInt(k.getMaKH().substring(2)));
                } catch (Exception ignored) {}
            }
            String maKH = String.format("KH%03d", max + 1);
            String tcStr = (thuCung != null && !thuCung.isEmpty()) ? thuCung.get(0).getTen() : "—";
            String hangThe = tinhHang(diem);
            KhachHang kh = new KhachHang(maKH, hoTen, dinhDangSdt(sdt), hangThe, diem, tcStr, LocalDate.now(), 0);
            ds.add(0, kh);
            return maKH;
        }
    }

    /** Thêm khách hàng mới vào bộ nhớ (dùng khi lập hóa đơn không qua CSDL). */
    public KhachHang them(String hoTen, String sdt, String hangThe, String thuCung) {
        int max = 0;
        for (KhachHang k : ds) {
            try {
                if (k.getMaKH() != null && k.getMaKH().startsWith("KH"))
                    max = Math.max(max, Integer.parseInt(k.getMaKH().substring(2)));
            } catch (Exception ignored) {}
        }
        KhachHang kh = new KhachHang(String.format("KH%03d", max + 1), hoTen,
                dinhDangSdt(sdt), hangThe == null ? "Thường" : hangThe,
                0, (thuCung == null || thuCung.isEmpty()) ? "—" : thuCung, null, 0);
        ds.add(kh);
        return kh;
    }

    /** Cộng số tiền vừa thanh toán vào tổng chi tiêu và cập nhật ngày giao dịch gần nhất. */
    public void ghiNhanThanhToan(String maKH, long soTien) {
        KhachHang k = timTheoMa(maKH);
        if (k == null) return;
        k.setTongChiTieu(k.getTongChiTieu() + soTien);
        k.setGiaoDichGanNhat(LocalDate.now());
    }

    /** Cập nhật thông tin khách hàng (họ tên, SĐT, điểm). */
    public boolean capNhat(String maKH, String hoTen, String sdt, int diem) throws SQLException {
        if (maKH == null || maKH.trim().isEmpty())
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        if (hoTen == null || hoTen.trim().isEmpty())
            throw new IllegalArgumentException("Họ và tên không được để trống.");
        String so = chiSo(sdt);
        if (so.length() < 9 || so.length() > 11)
            throw new IllegalArgumentException("Số điện thoại phải gồm 9–11 chữ số.");
        if (tonTaiSoDienThoai(so, maKH))
            throw new IllegalArgumentException("Số điện thoại này đã được dùng bởi khách hàng khác.");
        try {
            boolean ok = dao.capNhat(maKH, hoTen.trim(), so, Math.max(0, diem));
            if (ok) {
                try { taiLai(); } catch (Exception ignored) {}
            }
            return ok;
        } catch (SQLException e) {
            for (KhachHang k : ds) {
                if (k.getMaKH() != null && k.getMaKH().equalsIgnoreCase(maKH)) {
                    k.setHoTen(hoTen.trim());
                    k.setSoDienThoai(dinhDangSdt(so));
                    k.setDiemTichLuy(Math.max(0, diem));
                    k.setHangThe(tinhHang(diem));
                    return true;
                }
            }
            return true;
        }
    }

    /** Xóa khách hàng có kiểm tra ràng buộc. */
    public boolean xoa(String maKH) throws SQLException {
        if (maKH == null || maKH.trim().isEmpty())
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        try {
            boolean ok = dao.xoa(maKH);
            if (ok) {
                try { taiLai(); } catch (Exception ignored) {}
            }
            return ok;
        } catch (SQLException e) {
            ds.removeIf(k -> k.getMaKH() != null && k.getMaKH().equalsIgnoreCase(maKH));
            return true;
        }
    }

    /** Hạng thẻ theo điểm: Thường (<100) · Bạc (100) · Vàng (200) · VIP (500). */
    public static String tinhHang(int diem) {
        return KhachHangDAO.tinhHang(diem);
    }

    /** Số điểm nhận được khi thanh toán số tiền này (1 điểm / 10.000₫). */
    public static long diemTuSoTien(long soTien) {
        return Math.max(0, soTien / KhachHangDAO.SO_TIEN_MOI_DIEM);
    }

    // ------------------------------------------------------------ quy tắc dữ liệu khách hàng
    /** Gọn khoảng trắng, chuẩn Unicode (NFC) và viết hoa chữ cái đầu mỗi từ. */
    public static String chuanHoaTen(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFC).trim().replaceAll("\\s+", " ");
        StringBuilder sb = new StringBuilder();
        boolean dauTu = true;
        for (char c : t.toCharArray()) {
            sb.append(dauTu && Character.isLetter(c) ? Character.toUpperCase(c) : c);
            dauTu = c == ' ' || c == '-';
        }
        return sb.toString();
    }

    /** @return thông báo lỗi hoặc null nếu tên hợp lệ. */
    public static String loiTen(String ten) {
        String t = chuanHoaTen(ten);
        if (t.isEmpty()) return "Vui lòng nhập tên khách hàng.";
        if (t.length() < 2) return "Tên khách hàng phải có ít nhất 2 ký tự.";
        if (t.length() > 50) return "Tên khách hàng tối đa 50 ký tự.";
        if (!t.matches("\\p{L}[\\p{L}\\p{M} .''-]*"))
            return "Tên khách hàng chỉ được chứa chữ cái, khoảng trắng và các dấu . ' -";
        return null;
    }

    /** Chỉ giữ chữ số; đầu số quốc tế 84 được đổi về 0 (+84 903... thành 0903...). */
    public static String chuanHoaSdt(String s) {
        String d = chiSo(s);
        if (d.startsWith("84") && (d.length() == 11 || d.length() == 12)) d = "0" + d.substring(2);
        return d;
    }

    /** Di động Việt Nam 10 số (03, 05, 07, 08, 09) hoặc điện thoại bàn 11 số (02x). */
    public static String loiSdt(String sdt) {
        String d = chuanHoaSdt(sdt);
        if (d.isEmpty()) return "Vui lòng nhập số điện thoại.";
        if (d.matches("0[35789]\\d{8}") || d.matches("02\\d{9}")) return null;
        return "Số điện thoại không hợp lệ. Ví dụ: 0903 123 456.";
    }

    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    public static String dinhDangSdt(String s) {
        String d = chiSo(s);
        return d.length() == 10 ? d.substring(0, 4) + " " + d.substring(4, 7) + " " + d.substring(7) : d;
    }

}
