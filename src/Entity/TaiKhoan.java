package Entity;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TaiKhoan {
    private final String maTaiKhoan;
    private final String tenDangNhap;
    private final String hoTen;
    private final String vaiTro;
    private final Set<String> chucNang;
    // Thông tin liên hệ
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String email;
    // Thông tin công việc
    private LocalDate ngayVaoLam;
    private String caLamViec;
    private String trangThai;

    public TaiKhoan(String maTaiKhoan, String tenDangNhap, String hoTen, String vaiTro, Set<String> chucNang,
                    String soDienThoai, LocalDate ngaySinh, String email) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro == null ? "Chưa phân quyền" : vaiTro;
        this.chucNang = Collections.unmodifiableSet(chucNang == null ? new HashSet<>() : new HashSet<>(chucNang));
        this.soDienThoai = soDienThoai;
        this.ngaySinh = ngaySinh;
        this.email = email;
    }

    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getHoTen() { return hoTen; }
    public String getVaiTro() { return vaiTro; }
    public Set<String> getChucNang() { return chucNang; }
    public String getSoDienThoai() { return soDienThoai; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getEmail() { return email; }
    public LocalDate getNgayVaoLam() { return ngayVaoLam; }
    public String getCaLamViec() { return caLamViec; }
    public String getTrangThai() { return trangThai; }

    public void setThongTinCongViec(LocalDate ngayVaoLam, String caLamViec, String trangThai) {
        this.ngayVaoLam = ngayVaoLam;
        this.caLamViec = caLamViec;
        this.trangThai = trangThai;
    }

    public void capNhatLienHe(String soDienThoai, LocalDate ngaySinh, String email) {
        this.soDienThoai = soDienThoai;
        this.ngaySinh = ngaySinh;
        this.email = email;
    }

    private static String norm(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("đ", "d")
                .trim();
    }

    /** Kiểm tra xem tài khoản có quyền dùng chức năng này không. */
    public boolean coQuyen(String tenChucNang) {
        if (tenChucNang == null || tenChucNang.trim().isEmpty()) return true;

        String target = norm(tenChucNang);

        // 1. Chức năng hệ thống dùng chung: mọi nhân viên đều được truy cập
        if (target.equals("trang chu") || target.equals("tai khoan") || target.equals("dang xuat") || target.equals("tro giup")) {
            return true;
        }

        // 2. Quản lý / Admin có toàn quyền 100% tất cả chức năng
        String v = norm(vaiTro);
        if (v.contains("quan ly") || v.contains("admin")) {
            return true;
        }

        // 3. Tìm kiếm chung: cho phép mở menu nếu có ít nhất 1 quyền
        if (target.equals("tim kiem")) {
            return !chucNang.isEmpty();
        }

        // 4. Đối chiếu trực tiếp và theo nghiệp vụ với tập quyền thực tế trong CSDL của tài khoản
        for (String cn : chucNang) {
            String c = norm(cn);
            if (c.equals(target)) return true;

            // Nhân Viên: CHỈ tài khoản có quyền Nhân Viên (hoặc Quản lý)
            if (target.contains("nhan vien") && c.contains("nhan vien")) {
                return true;
            }

            // Khách Hàng
            if (target.contains("khach hang") && c.contains("khach hang")) {
                return true;
            }

            // Thú Cưng
            if (target.contains("thu cung") && c.contains("thu cung")) {
                return true;
            }

            // Bán Hàng / Thu Ngân / Hóa Đơn
            if ((target.contains("hoa don") || target.contains("ban hang"))
                    && (c.contains("ban hang") || c.contains("hoa don") || c.contains("thu ngan"))) {
                return true;
            }

            // Kho Hàng
            if (target.equals("kho hang") && (c.equals("kho hang") || c.equals("quan ly kho hang") || c.equals("kho"))) {
                return true;
            }

            // Lịch Sử Nhập / Xuất Kho
            if ((target.contains("lich su kho") || target.contains("nhap xuat") || target.contains("lich su nhap"))
                    && (c.contains("nhap xuat") || c.contains("kho hang") || c.equals("kho"))) {
                return true;
            }

            // Danh Mục Bệnh
            if (target.contains("benh") && (c.contains("benh") || c.contains("bac si") || c.contains("kham"))) {
                return true;
            }

            // Tiêm Phòng
            if (target.contains("tiem") && (c.contains("tiem") || c.contains("bac si") || c.contains("kham"))) {
                return true;
            }

            // Phẫu Thuật
            if (target.contains("phau thuat") && (c.contains("phau thuat") || c.contains("bac si"))) {
                return true;
            }

            // Đơn Thuốc
            if (target.contains("don thuoc") && (c.contains("don thuoc") || c.contains("bac si"))) {
                return true;
            }

            // Phác Đồ
            if (target.contains("phac do") && (c.contains("phac do") || c.contains("benh an") || c.contains("bac si"))) {
                return true;
            }

            // Lồng Chuồng / Lưu Trú
            if ((target.contains("chuong") || target.contains("luu tru"))
                    && (c.contains("luu tru") || c.contains("chuong"))) {
                return true;
            }

            // Lịch Hẹn
            if (target.contains("lich hen") && (c.contains("lich hen") || c.contains("dat lich"))) {
                return true;
            }

            // Dịch Vụ / Khuyến Mãi
            if ((target.contains("dich vu") || target.contains("khuyen mai"))
                    && (c.contains("dich vu") || c.contains("khuyen mai"))) {
                return true;
            }

            // Thống Kê
            if (target.contains("thong ke")) {
                // Doanh thu tài chính: chỉ Quản lý
                if (target.contains("doanh thu")) {
                    return false;
                }
                // Tồn kho: Kho hàng hoặc Quản lý
                if (target.contains("ton kho")) {
                    if (c.contains("kho") || c.contains("san pham")) return true;
                }
                // Bán chạy: Bán hàng hoặc Kho
                if (target.contains("ban chay")) {
                    if (c.contains("ban hang") || c.contains("kho") || c.contains("san pham")) return true;
                }
            }
        }

        return false;
    }
}
