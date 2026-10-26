package Entity;

/**
 * Thực thể Dịch vụ (bảng DICHVU).
 */
public class DichVu {
    private String maDV;
    private String tenDV;
    private String loaiDV;          // Khám & Điều trị | Spa & Grooming | Tiêm phòng & Xét nghiệm | Phẫu thuật | Khách sạn & Lưu trú
    private double giaTien;
    private int thoiGianThucHien;   // Thời lượng ước tính (phút)
    private String moTa;            // Mô tả / quy trình dịch vụ
    private String trangThai;       // Đang cung cấp | Tạm ngưng
    private String doiTuong;        // Tất cả thú cưng | Chó & Mèo | Chỉ dành cho Chó | Chỉ dành cho Mèo

    public DichVu() {
    }

    public DichVu(String maDV, String tenDV, double giaTien) {
        this(maDV, tenDV, "Khám & Điều trị", giaTien, 30, "", "Đang cung cấp", "Tất cả thú cưng");
    }

    public DichVu(String maDV, String tenDV, String loaiDV, double giaTien,
                  int thoiGianThucHien, String moTa, String trangThai, String doiTuong) {
        this.maDV = maDV;
        this.tenDV = tenDV != null ? tenDV.trim() : "";
        this.loaiDV = (loaiDV == null || loaiDV.trim().isEmpty()) ? "Khám & Điều trị" : loaiDV.trim();
        this.giaTien = Math.max(0, giaTien);
        this.thoiGianThucHien = Math.max(0, thoiGianThucHien);
        this.moTa = moTa != null ? moTa.trim() : "";
        this.trangThai = (trangThai == null || trangThai.trim().isEmpty()) ? "Đang cung cấp" : trangThai.trim();
        this.doiTuong = (doiTuong == null || doiTuong.trim().isEmpty()) ? "Tất cả thú cưng" : doiTuong.trim();
    }

    // Getters & Setters
    public String getMaDV() { return maDV; }
    public void setMaDV(String maDV) { this.maDV = maDV; }

    public String getTenDV() { return tenDV; }
    public void setTenDV(String tenDV) { this.tenDV = tenDV; }

    public String getLoaiDV() { return loaiDV; }
    public void setLoaiDV(String loaiDV) { this.loaiDV = loaiDV; }

    public double getGiaTien() { return giaTien; }
    public void setGiaTien(double giaTien) { this.giaTien = giaTien; }

    public int getThoiGianThucHien() { return thoiGianThucHien; }
    public void setThoiGianThucHien(int thoiGianThucHien) { this.thoiGianThucHien = thoiGianThucHien; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }

    @Override
    public String toString() {
        return tenDV;
    }
}
