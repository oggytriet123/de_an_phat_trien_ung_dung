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

    // [CN-TK-29] Cập nhật SĐT/ngày sinh/email trong đối tượng đang đăng nhập
    public void capNhatLienHe(String soDienThoai, LocalDate ngaySinh, String email) {
        this.soDienThoai = soDienThoai;
        this.ngaySinh = ngaySinh;
        this.email = email;
    }

    private static String norm(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim();
    }

    // [CN-PQ-04] HÀM KIỂM TRA QUYỀN: tài khoản có được dùng chức năng này không
    /** Kiểm tra xem tài khoản có quyền dùng chức năng này không. */
    public boolean coQuyen(String tenChucNang) {
        if (tenChucNang == null || tenChucNang.trim().isEmpty()) return true;

        String target = norm(tenChucNang);

        // Các chức năng dùng chung: mọi tài khoản đều có quyền
        // [CN-PQ-05] Chức năng dùng chung: ai cũng được vào
        if (target.equals("trang chu") || target.equals("tai khoan") || target.equals("dang xuat") || target.equals("tro giup")) {
            return true;
        }

        // Quản lý hoặc admin có toàn quyền 100% với tất cả chức năng
        String v = norm(vaiTro);
        // [CN-PQ-09] Chi bypass theo vai tro, KHONG bypass theo username
        if (v.contains("quan ly") || v.contains("admin")) {
            return true;
        }

        // Tìm kiếm chung: cho phép mở menu tìm kiếm nếu có quyền tra cứu
        if (target.equals("tim kiem")) {
            // [CN-PQ-05b] Chi hien menu Tim Kiem neu tai khoan co quyen
            return !chucNang.isEmpty();
        }

        // Duyệt qua các quyền thực tế trong database của tài khoản
        for (String cn : chucNang) {
            String c = norm(cn);
            if (c.equals(target)) return true;

            // Ánh xạ Lồng Chuồng / Chuồng Lưu Trữ -> Lưu Trú
            if ((target.contains("chuong") || target.contains("long chuong") || target.contains("luu tru"))
                    && (c.contains("luu tru") || c.contains("chuong"))) {
                return true;
            }

            // Ánh xạ Thống kê
            if (target.contains("thong ke")) {
                // Chi cho phep "Thong Ke Kho" neu co quyen kho
                if (target.equals("thong ke kho") && (c.equals("thong ke kho") || c.equals("kho hang") || c.contains("san pham"))) return true;
                if (!target.equals("thong ke kho") && c.equals("thong ke")) return true;
                if (c.equals("thong ke kho")) return false; // Thong Ke Kho khong duoc tinh la Thong Ke chung
                if (target.contains("ton kho") && (c.equals("kho hang") || c.contains("san pham") || c.equals("thong ke kho"))) return true;
                if (target.contains("thu cung") && (c.contains("bac si") || c.contains("thu cung"))) return true;
                if (target.contains("khach hang") && (c.contains("ban hang") || c.contains("khach hang"))) return true;
            }

            // Ánh xạ Hóa đơn / Bán hàng
            if ((target.contains("hoa don") || target.contains("ban hang"))
                    && (c.contains("hoa don") || c.contains("ban hang"))) {
                return true;
            }

            // Ánh xạ Bác sĩ, Tiêm phòng, Phẫu thuật, Đơn thuốc, Phác đồ, Bệnh lý
            if ((target.contains("tiem phong") || target.contains("phau thuat") || target.contains("don thuoc")
                    || target.contains("phac do") || target.contains("dieu tri") || target.contains("benh") || target.contains("bac si"))
                    && (c.contains("bac si") || c.contains("kham") || c.contains("dieu tri") || c.contains("don thuoc") || c.contains("benh"))) {
                return true;
            }

            // Ánh xạ Kho hàng / Sản phẩm / Nhập xuất
            if ((target.contains("kho hang") || target.contains("nhap xuat") || target.contains("san pham"))
                    && (c.equals("kho hang") || c.contains("nhap") || c.contains("san pham"))) {
                return true;
            }

            // Ánh xạ Dịch vụ / Khuyến mãi
            if ((target.contains("dich vu") || target.contains("khuyen mai"))
                    && (c.contains("dich vu") || c.contains("khuyen mai"))) {
                return true;
            }

            // Ánh xạ Tìm kiếm chi tiết theo thực thể
            if (target.startsWith("tim ") || target.equals("phieu dat lich")) {
                if (c.contains("tim kiem")) return true;
                if (target.contains("nhan vien") && c.contains("nhan vien")) return true;
                if (target.contains("khach hang") && c.contains("khach hang")) return true;
                if (target.contains("thu cung") && c.contains("thu cung")) return true;
                if (target.contains("hoa don") && (c.contains("ban hang") || c.contains("hoa don"))) return true;
                if ((target.contains("lich") || target.contains("dat lich")) && c.contains("lich hen")) return true;
            }
        }

        return false;
    }
}
