package Entity;

/**
 * Một giai đoạn / bước điều trị trong Phác đồ điều trị.
 */
public class GiaiDoanDieuTri {
    public static final String CHUA_THUC_HIEN = "Chưa thực hiện";
    public static final String DANG_TIEN_HANH = "Đang tiến hành";
    public static final String DA_HOAN_THANH  = "Đã hoàn thành";

    private int thuTu;
    private String tenGiaiDoan;
    private String thoiGian;       // e.g. "3 ngày", "5 ngày", "1 tuần"
    private String mucTieu;        // Mục tiêu cụ thể của giai đoạn
    private String chiDinh;        // Thuốc, liều dùng, thủ thuật chăm sóc
    private String trangThai;

    public GiaiDoanDieuTri() {
        this(1, "", "", "", "", CHUA_THUC_HIEN);
    }

    public GiaiDoanDieuTri(int thuTu, String tenGiaiDoan, String thoiGian, String mucTieu, String chiDinh, String trangThai) {
        this.thuTu = thuTu;
        this.tenGiaiDoan = tenGiaiDoan == null ? "" : tenGiaiDoan;
        this.thoiGian = thoiGian == null ? "" : thoiGian;
        this.mucTieu = mucTieu == null ? "" : mucTieu;
        this.chiDinh = chiDinh == null ? "" : chiDinh;
        this.trangThai = trangThai == null ? CHUA_THUC_HIEN : trangThai;
    }

    public int getThuTu() { return thuTu; }
    public void setThuTu(int thuTu) { this.thuTu = thuTu; }

    public String getTenGiaiDoan() { return tenGiaiDoan; }
    public void setTenGiaiDoan(String tenGiaiDoan) { this.tenGiaiDoan = tenGiaiDoan; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public String getMucTieu() { return mucTieu; }
    public void setMucTieu(String mucTieu) { this.mucTieu = mucTieu; }

    public String getChiDinh() { return chiDinh; }
    public void setChiDinh(String chiDinh) { this.chiDinh = chiDinh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
