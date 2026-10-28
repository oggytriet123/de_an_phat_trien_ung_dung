package Entity;

import java.time.LocalDate;

/**
 * Thực thể Nhân viên (bảng NHANVIEN và các bảng vai trò mở rộng).
 */
public class NhanVien {
    private String maNV;
    private String hoTen;
    private String tenDangNhap;
    private String matKhau;        // Chuỗi băm SHA-256
    private int maQuyen;           // 1: Quản lý, 2: Bán hàng, 3: Kho, 4: Bác sĩ
    private String tenQuyen;       // Quản lý, Bán hàng, Kho, Bác sĩ
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String email;
    private String chuyenMon;      // Chuyên môn y tế (nếu là Bác sĩ)
    private String trangThai;      // Đang làm việc | Tạm nghỉ | Đã nghỉ việc
    private String caLamViec;      // Toàn thời gian | Ca sáng | Ca chiều | Ca tối
    private LocalDate ngayVaoLam;

    public NhanVien() {
    }

    public NhanVien(String maNV, String hoTen, String tenDangNhap, String matKhau,
                    int maQuyen, String tenQuyen, String soDienThoai, LocalDate ngaySinh,
                    String email, String chuyenMon, String trangThai, String caLamViec, LocalDate ngayVaoLam) {
        this.maNV = maNV;
        this.hoTen = hoTen != null ? hoTen.trim() : "";
        this.tenDangNhap = tenDangNhap != null ? tenDangNhap.trim() : "";
        this.matKhau = matKhau;
        this.maQuyen = maQuyen;
        this.tenQuyen = tenQuyen != null ? tenQuyen.trim() : "Nhân viên";
        this.soDienThoai = soDienThoai != null ? soDienThoai.trim() : "";
        this.ngaySinh = ngaySinh;
        this.email = email != null ? email.trim() : "";
        this.chuyenMon = chuyenMon != null ? chuyenMon.trim() : "";
        this.trangThai = (trangThai == null || trangThai.trim().isEmpty()) ? "Đang làm việc" : trangThai.trim();
        this.caLamViec = (caLamViec == null || caLamViec.trim().isEmpty()) ? "Toàn thời gian" : caLamViec.trim();
        this.ngayVaoLam = ngayVaoLam != null ? ngayVaoLam : LocalDate.now();
    }

    // Getters & Setters
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public int getMaQuyen() { return maQuyen; }
    public void setMaQuyen(int maQuyen) { this.maQuyen = maQuyen; }

    public String getTenQuyen() { return tenQuyen; }
    public void setTenQuyen(String tenQuyen) { this.tenQuyen = tenQuyen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getChuyenMon() { return chuyenMon; }
    public void setChuyenMon(String chuyenMon) { this.chuyenMon = chuyenMon; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getCaLamViec() { return caLamViec; }
    public void setCaLamViec(String caLamViec) { this.caLamViec = caLamViec; }

    public LocalDate getNgayVaoLam() { return ngayVaoLam; }
    public void setNgayVaoLam(LocalDate ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    @Override
    public String toString() {
        return hoTen + " (" + maNV + ")";
    }
}
