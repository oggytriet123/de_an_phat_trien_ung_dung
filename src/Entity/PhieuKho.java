package Entity;

import java.io.Serializable;

/**
 * Thực thể Phiếu Kho (Nhập / Xuất kho).
 */
public class PhieuKho implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maPhieu;
    private String loaiPhieu;    // "Nhập mới", "Xuất bán", "Xuất hủy"
    private String ngayLap;      // "04/03/2026"
    private String gioLap;       // "09:45"
    private String nguoiThucHien;// "Nguyễn Minh An"
    private String doiTac;       // "Công Ty Green Pet"
    private long tongGiaTri;     // 12800000
    private String trangThai;    // "Hoàn tất", "Đang xử lý", "Đã hủy"

    public PhieuKho() {}

    public PhieuKho(String maPhieu, String loaiPhieu, String ngayLap, String gioLap,
                    String nguoiThucHien, String doiTac, long tongGiaTri, String trangThai) {
        this.maPhieu = maPhieu;
        this.loaiPhieu = loaiPhieu;
        this.ngayLap = ngayLap;
        this.gioLap = gioLap;
        this.nguoiThucHien = nguoiThucHien;
        this.doiTac = doiTac;
        this.tongGiaTri = tongGiaTri;
        this.trangThai = trangThai;
    }

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public String getLoaiPhieu() { return loaiPhieu; }
    public void setLoaiPhieu(String loaiPhieu) { this.loaiPhieu = loaiPhieu; }

    public String getNgayLap() { return ngayLap; }
    public void setNgayLap(String ngayLap) { this.ngayLap = ngayLap; }

    public String getGioLap() { return gioLap; }
    public void setGioLap(String gioLap) { this.gioLap = gioLap; }

    public String getNguoiThucHien() { return nguoiThucHien; }
    public void setNguoiThucHien(String nguoiThucHien) { this.nguoiThucHien = nguoiThucHien; }

    public String getDoiTac() { return doiTac; }
    public void setDoiTac(String doiTac) { this.doiTac = doiTac; }

    public long getTongGiaTri() { return tongGiaTri; }
    public void setTongGiaTri(long tongGiaTri) { this.tongGiaTri = tongGiaTri; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
