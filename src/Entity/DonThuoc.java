package Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Đơn thuốc khám chữa bệnh cho thú cưng.
 */
public class DonThuoc {
    public static final String CHO_CAP_THUOC = "Chờ cấp thuốc";
    public static final String DA_CAP_PHAT   = "Đã cấp phát";
    public static final String DANG_DIEU_TRI = "Đang điều trị";
    public static final String HOAN_TAT      = "Hoàn tất";
    public static final String DA_HUY        = "Đã hủy";

    private static final DateTimeFormatter LOG_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String maDonThuoc;
    private String maTC;
    private String tenThuCung;
    private String loai;
    private String giong;
    private double canNang;
    private String tenChu;
    private String sdtChu;
    private String bacSi;
    private LocalDate ngayKe;
    private LocalTime gioKe;
    private String chanDoan;
    private String trieuChung;
    private final List<ChiTietDonThuoc> danhSachThuoc = new ArrayList<>();
    private String trangThai;
    private String loiDan;
    private LocalDate ngayTaiKham;
    private String maPhacDo;
    private final List<String> lichSu = new ArrayList<>();

    public DonThuoc() {
        this("", "", "", "Chó", "", 0, "", "", "", LocalDate.now(), LocalTime.now(), "", "", CHO_CAP_THUOC, "", null, "");
    }

    public DonThuoc(String maDonThuoc, String maTC, String tenThuCung, String loai, String giong,
                    double canNang, String tenChu, String sdtChu, String bacSi,
                    LocalDate ngayKe, LocalTime gioKe, String chanDoan, String trieuChung,
                    String trangThai, String loiDan, LocalDate ngayTaiKham, String maPhacDo) {
        this.maDonThuoc = maDonThuoc;
        this.maTC = maTC;
        this.tenThuCung = tenThuCung;
        this.loai = loai == null ? "Chó" : loai;
        this.giong = giong == null ? "" : giong;
        this.canNang = canNang;
        this.tenChu = tenChu == null ? "" : tenChu;
        this.sdtChu = sdtChu == null ? "" : sdtChu;
        this.bacSi = bacSi == null ? "" : bacSi;
        this.ngayKe = ngayKe == null ? LocalDate.now() : ngayKe;
        this.gioKe = gioKe == null ? LocalTime.now() : gioKe;
        this.chanDoan = chanDoan == null ? "" : chanDoan;
        this.trieuChung = trieuChung == null ? "" : trieuChung;
        this.trangThai = trangThai == null ? CHO_CAP_THUOC : trangThai;
        this.loiDan = loiDan == null ? "" : loiDan;
        this.ngayTaiKham = ngayTaiKham;
        this.maPhacDo = maPhacDo == null ? "" : maPhacDo;
    }

    public long tinhTongTien() {
        long tong = 0;
        for (ChiTietDonThuoc ct : danhSachThuoc) {
            tong += ct.getThanhTien();
        }
        return tong;
    }

    public int getSoLoaiThuoc() {
        return danhSachThuoc.size();
    }

    public void themChiTiet(ChiTietDonThuoc ct) {
        if (ct != null) danhSachThuoc.add(ct);
    }

    public void ghiLog(String noiDung) {
        lichSu.add(LocalDateTime.now().format(LOG_FMT) + " · " + noiDung);
    }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaTC() { return maTC; }
    public void setMaTC(String maTC) { this.maTC = maTC; }

    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getGiong() { return giong; }
    public void setGiong(String giong) { this.giong = giong; }

    public double getCanNang() { return canNang; }
    public void setCanNang(double canNang) { this.canNang = canNang; }

    public String getTenChu() { return tenChu; }
    public void setTenChu(String tenChu) { this.tenChu = tenChu; }

    public String getSdtChu() { return sdtChu; }
    public void setSdtChu(String sdtChu) { this.sdtChu = sdtChu; }

    public String getBacSi() { return bacSi; }
    public void setBacSi(String bacSi) { this.bacSi = bacSi; }

    public LocalDate getNgayKe() { return ngayKe; }
    public void setNgayKe(LocalDate ngayKe) { this.ngayKe = ngayKe; }

    public LocalTime getGioKe() { return gioKe; }
    public void setGioKe(LocalTime gioKe) { this.gioKe = gioKe; }

    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }

    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }

    public List<ChiTietDonThuoc> getDanhSachThuoc() { return danhSachThuoc; }
    public void setDanhSachThuoc(List<ChiTietDonThuoc> list) {
        this.danhSachThuoc.clear();
        if (list != null) this.danhSachThuoc.addAll(list);
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getLoiDan() { return loiDan; }
    public void setLoiDan(String loiDan) { this.loiDan = loiDan; }

    public LocalDate getNgayTaiKham() { return ngayTaiKham; }
    public void setNgayTaiKham(LocalDate ngayTaiKham) { this.ngayTaiKham = ngayTaiKham; }

    public String getMaPhacDo() { return maPhacDo; }
    public void setMaPhacDo(String maPhacDo) { this.maPhacDo = maPhacDo; }

    public List<String> getLichSu() { return Collections.unmodifiableList(lichSu); }
}
