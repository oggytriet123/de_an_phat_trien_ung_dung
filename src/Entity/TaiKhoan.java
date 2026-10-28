package Entity;

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
    // Thông tin liên hệ: người dùng được tự cập nhật
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String email;

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

    /** Cập nhật bản sao trong bộ nhớ sau khi đã lưu CSDL thành công. */
    public void capNhatLienHe(String soDienThoai, LocalDate ngaySinh, String email) {
        this.soDienThoai = soDienThoai;
        this.ngaySinh = ngaySinh;
        this.email = email;
    }

    /** Tài khoản có được dùng chức năng này không? (null = chức năng ai cũng dùng được) */
    public boolean coQuyen(String tenChucNang) {
        return tenChucNang == null || chucNang.contains(tenChucNang);
    }
}
