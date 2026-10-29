package Entity;

import java.time.LocalDate;

/**
 * Thú cưng của khách hàng (bảng THUCUNG).
 * petID = null khi chưa lưu vào CSDL (tự sinh mã PETxxx).
 */
public class ThuCung {
    private String petID;
    private String ten;
    private String loai;           // Chó | Mèo | Thỏ | Khác
    private String giong;          // Poodle, Corgi, Anh lông ngắn,...
    private String gioiTinh;       // Đực | Cái | Không rõ
    private int tuoi;              // Tuổi (năm hoặc tháng)
    private double canNang;        // kg (0 nếu chưa biết)
    private String theCanhBao;     // Cảnh báo y tế / dị ứng / tính cách
    private String trangThai;      // Khỏe mạnh | Đang theo dõi | Cần chú ý | Đang điều trị
    private String maKH;           // Mã khách hàng chủ nuôi
    private LocalDate ngayTiepNhan;

    // Các trường hỗ trợ hiển thị từ JOIN bảng KHACHHANG
    private String tenChuNuoi;
    private String sdtChuNuoi;
    private String hangTheChuNuoi;

    /** Constructor tương thích ngược cho các module cũ (KhachHangDAO, v.v.). */
    public ThuCung(String petID, String ten, String loai, double canNang) {
        this(petID, ten, loai, "", "Đực", 1, canNang, "", "Khỏe mạnh", null, LocalDate.now(), null, null, null);
    }

    /** Constructor cơ bản khi nhập liệu trong module quản lý thú cưng. */
    public ThuCung(String petID, String ten, String loai, String giong, String gioiTinh,
                   int tuoi, double canNang, String theCanhBao, String trangThai, String maKH) {
        this(petID, ten, loai, giong, gioiTinh, tuoi, canNang, theCanhBao, trangThai, maKH, LocalDate.now(), null, null, null);
    }

    /** Constructor đầy đủ bao gồm dữ liệu chủ nuôi từ JOIN. */
    public ThuCung(String petID, String ten, String loai, String giong, String gioiTinh,
                   int tuoi, double canNang, String theCanhBao, String trangThai, String maKH,
                   LocalDate ngayTiepNhan, String tenChuNuoi, String sdtChuNuoi, String hangTheChuNuoi) {
        this.petID = petID;
        this.ten = ten;
        this.loai = (loai == null || loai.trim().isEmpty()) ? "Khác" : loai.trim();
        this.giong = (giong == null) ? "" : giong.trim();
        this.gioiTinh = (gioiTinh == null || gioiTinh.trim().isEmpty()) ? "Đực" : gioiTinh.trim();
        this.tuoi = Math.max(0, tuoi);
        this.canNang = Math.max(0, canNang);
        this.theCanhBao = (theCanhBao == null) ? "" : theCanhBao.trim();
        this.trangThai = (trangThai == null || trangThai.trim().isEmpty()) ? "Khỏe mạnh" : trangThai.trim();
        this.maKH = maKH;
        this.ngayTiepNhan = ngayTiepNhan;
        this.tenChuNuoi = tenChuNuoi;
        this.sdtChuNuoi = sdtChuNuoi;
        this.hangTheChuNuoi = hangTheChuNuoi;
    }

    // Getters & Setters
    public String getPetID() { return petID; }
    public void setPetID(String petID) { this.petID = petID; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getGiong() { return giong; }
    public void setGiong(String giong) { this.giong = giong; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public int getTuoi() { return tuoi; }
    public void setTuoi(int tuoi) { this.tuoi = tuoi; }

    public double getCanNang() { return canNang; }
    public void setCanNang(double canNang) { this.canNang = canNang; }

    public String getTheCanhBao() { return theCanhBao; }
    public void setTheCanhBao(String theCanhBao) { this.theCanhBao = theCanhBao; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public LocalDate getNgayTiepNhan() { return ngayTiepNhan; }
    public void setNgayTiepNhan(LocalDate ngayTiepNhan) { this.ngayTiepNhan = ngayTiepNhan; }

    public String getTenChuNuoi() { return tenChuNuoi != null ? tenChuNuoi : "—"; }
    public void setTenChuNuoi(String tenChuNuoi) { this.tenChuNuoi = tenChuNuoi; }

    public String getSdtChuNuoi() { return sdtChuNuoi != null ? sdtChuNuoi : ""; }
    public void setSdtChuNuoi(String sdtChuNuoi) { this.sdtChuNuoi = sdtChuNuoi; }

    public String getHangTheChuNuoi() { return hangTheChuNuoi != null ? hangTheChuNuoi : "Thường"; }
    public void setHangTheChuNuoi(String hangTheChuNuoi) { this.hangTheChuNuoi = hangTheChuNuoi; }


    // Compatibility aliases cho DonThuocPanel & PhacDoPanel
    public String getMaTC() { return petID; }
    public String getTenChu() { return getTenChuNuoi(); }
    public String getSdtChu() { return getSdtChuNuoi(); }
    public String getSucKhoe() { return trangThai != null ? trangThai : "Khỏe mạnh"; }
    public boolean isDangLuuTru() { return "Đang lưu trú".equalsIgnoreCase(trangThai); }
    public String getGhiChu() { return theCanhBao != null ? theCanhBao : ""; }
    @Override
    public String toString() {
        return ten;
    }
}
