package Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Phác đồ điều trị y tế cho thú cưng (Treatment Protocol).
 */
public class PhacDoDieuTri {
    public static final String DANG_DIEU_TRI   = "Đang điều trị";
    public static final String THEO_DOI_TICH_CUC = "Theo dõi tích cực";
    public static final String HOAN_THANH     = "Hoàn thành";
    public static final String TAM_NGUNG      = "Tạm ngưng";
    public static final String DA_HUY         = "Đã hủy";

    public static final String MUC_DO_NHE        = "Nhẹ";
    public static final String MUC_DO_TRUNG_BINH = "Trung bình";
    public static final String MUC_DO_NANG       = "Nặng";
    public static final String MUC_DO_NGUY_KICH  = "Nguy kịch";

    private static final DateTimeFormatter LOG_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String maPhacDo;
    private String tenPhacDo;
    private String maTC;
    private String tenThuCung;
    private String loai;
    private String giong;
    private double canNang;
    private String tenChu;
    private String sdtChu;
    private String bacSi;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String mucDo;
    private String chanDoan;
    private String mucTieuDieuTri;
    private String cheDoDinhDuong;
    private final List<GiaiDoanDieuTri> cacGiaiDoan = new ArrayList<>();
    private int tienDo;             // 0 - 100%
    private String trangThai;
    private String maDonThuoc;      // Mã đơn thuốc liên kết
    private final List<NhatKyTheoDoi> nhatKy = new ArrayList<>();
    private final List<String> lichSu = new ArrayList<>();

    public PhacDoDieuTri() {
        this("", "", "", "", "Chó", "", 0, "", "", "",
                LocalDate.now(), LocalDate.now().plusDays(7),
                MUC_DO_TRUNG_BINH, "", "", "", 0, DANG_DIEU_TRI, "");
    }

    public PhacDoDieuTri(String maPhacDo, String tenPhacDo, String maTC, String tenThuCung,
                         String loai, String giong, double canNang, String tenChu, String sdtChu,
                         String bacSi, LocalDate ngayBatDau, LocalDate ngayKetThuc, String mucDo,
                         String chanDoan, String mucTieuDieuTri, String cheDoDinhDuong,
                         int tienDo, String trangThai, String maDonThuoc) {
        this.maPhacDo = maPhacDo;
        this.tenPhacDo = tenPhacDo == null ? "" : tenPhacDo;
        this.maTC = maTC == null ? "" : maTC;
        this.tenThuCung = tenThuCung == null ? "" : tenThuCung;
        this.loai = loai == null ? "Chó" : loai;
        this.giong = giong == null ? "" : giong;
        this.canNang = canNang;
        this.tenChu = tenChu == null ? "" : tenChu;
        this.sdtChu = sdtChu == null ? "" : sdtChu;
        this.bacSi = bacSi == null ? "" : bacSi;
        this.ngayBatDau = ngayBatDau == null ? LocalDate.now() : ngayBatDau;
        this.ngayKetThuc = ngayKetThuc == null ? LocalDate.now().plusDays(7) : ngayKetThuc;
        this.mucDo = mucDo == null ? MUC_DO_TRUNG_BINH : mucDo;
        this.chanDoan = chanDoan == null ? "" : chanDoan;
        this.mucTieuDieuTri = mucTieuDieuTri == null ? "" : mucTieuDieuTri;
        this.cheDoDinhDuong = cheDoDinhDuong == null ? "" : cheDoDinhDuong;
        this.tienDo = Math.min(100, Math.max(0, tienDo));
        this.trangThai = trangThai == null ? DANG_DIEU_TRI : trangThai;
        this.maDonThuoc = maDonThuoc == null ? "" : maDonThuoc;
    }

    public void themGiaiDoan(GiaiDoanDieuTri gd) {
        if (gd != null) cacGiaiDoan.add(gd);
    }

    public void themNhatKy(NhatKyTheoDoi nk) {
        if (nk != null) nhatKy.add(0, nk);
    }

    public void ghiLog(String noiDung) {
        lichSu.add(LocalDateTime.now().format(LOG_FMT) + " · " + noiDung);
    }

    public String getMaPhacDo() { return maPhacDo; }
    public void setMaPhacDo(String maPhacDo) { this.maPhacDo = maPhacDo; }

    public String getTenPhacDo() { return tenPhacDo; }
    public void setTenPhacDo(String tenPhacDo) { this.tenPhacDo = tenPhacDo; }

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

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getMucDo() { return mucDo; }
    public void setMucDo(String mucDo) { this.mucDo = mucDo; }

    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }

    public String getMucTieuDieuTri() { return mucTieuDieuTri; }
    public void setMucTieuDieuTri(String mucTieuDieuTri) { this.mucTieuDieuTri = mucTieuDieuTri; }

    public String getCheDoDinhDuong() { return cheDoDinhDuong; }
    public void setCheDoDinhDuong(String cheDoDinhDuong) { this.cheDoDinhDuong = cheDoDinhDuong; }

    public List<GiaiDoanDieuTri> getCacGiaiDoan() { return cacGiaiDoan; }
    public void setCacGiaiDoan(List<GiaiDoanDieuTri> list) {
        this.cacGiaiDoan.clear();
        if (list != null) this.cacGiaiDoan.addAll(list);
    }

    public int getTienDo() { return tienDo; }
    public void setTienDo(int tienDo) {
        this.tienDo = Math.min(100, Math.max(0, tienDo));
        if (this.tienDo == 100 && !HOAN_THANH.equals(trangThai)) {
            this.trangThai = HOAN_THANH;
        }
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public List<NhatKyTheoDoi> getNhatKy() { return Collections.unmodifiableList(nhatKy); }
    public void setNhatKy(List<NhatKyTheoDoi> list) {
        this.nhatKy.clear();
        if (list != null) this.nhatKy.addAll(list);
    }

    public List<String> getLichSu() { return Collections.unmodifiableList(lichSu); }
}
