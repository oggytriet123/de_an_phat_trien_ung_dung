package Bus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import DAO.HoaDonDAO;
import Entity.HoaDon;
import Entity.KhachHang;

/**
 * Quản lý 20 ô hóa đơn đang hoạt động và lịch sử hóa đơn đã thanh toán.
 * Kết nối trực tiếp cơ sở dữ liệu SQL Server (bảng HOADON và CHITIETHOADON).
 */
public class HoaDonBUS {
    public static final int SO_O = 20;
    public static final String[] PHUONG_THUC = {"Tiền mặt", "Quẹt thẻ", "MoMo", "ZaloPay"};

    private final HoaDon[] cacO = new HoaDon[SO_O];       // null = ô trống
    private final List<HoaDon> lichSu = new ArrayList<>(); // mới nhất ở đầu
    private final KhachHangBUS khachHang;
    private final HoaDonDAO dao = new HoaDonDAO();
    private int soHoaDonTiepTheo = 1;
    private String maNVDangNhap = "";                      // nhân viên của phiên đăng nhập hiện tại

    /** Mở lên tải các ô đang mở và lịch sử đã thanh toán từ CSDL SQL Server. */
    public HoaDonBUS(KhachHangBUS khachHang) {
        this.khachHang = khachHang;
        taiTuCSDL();
    }

    /** Gắn nhân viên đang đăng nhập; mọi hóa đơn lưu sau đó đều ghi nhận nhân viên này là người lập. */
    public synchronized void datNhanVienDangNhap(String maNV) {
        this.maNVDangNhap = maNV == null ? "" : maNV.trim();
    }

    public String getNhanVienDangNhap() {
        return maNVDangNhap;
    }

    public synchronized void taiTuCSDL() {
        for (int i = 0; i < SO_O; i++) cacO[i] = null;
        lichSu.clear();

        Map<Integer, HoaDon> oDangMo = dao.layCacODangMo();
        for (Map.Entry<Integer, HoaDon> entry : oDangMo.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < SO_O) {
                cacO[entry.getKey()] = entry.getValue();
            }
        }

        lichSu.addAll(dao.layLichSu());
        soHoaDonTiepTheo = dao.laySoHoaDonTiepTheo();
    }

    // ------------------------------------------------------------ 20 ô
    /** @param viTri 0..19 */
    public HoaDon layO(int viTri) {
        return cacO[viTri];
    }

    public int oTrongDauTien() {
        for (int i = 0; i < SO_O; i++) if (cacO[i] == null) return i;
        return -1;
    }

    public int demOTrong() {
        int n = 0;
        for (HoaDon h : cacO) if (h == null) n++;
        return n;
    }

    /** Mã hóa đơn sẽ được cấp cho hóa đơn mới tiếp theo (chưa tiêu thụ cho đến khi lưu). */
    public String maTiepTheo() {
        return String.format("HD%04d", soHoaDonTiepTheo);
    }

    /** Bản nháp cho một ô trống. Chưa nằm trong ô cho đến khi bấm Lưu hoặc Thanh toán. */
    public HoaDon taoNhap(int viTri) {
        return new HoaDon(maTiepTheo(), viTri + 1);
    }

    /** Bản sao của hóa đơn đang giữ tại ô để chỉnh sửa. Hủy chỉnh sửa không ảnh hưởng bản đã lưu. */
    public HoaDon moNhap(int viTri) {
        return cacO[viTri].saoChep();
    }

    // ------------------------------------------------------------ kiểm tra dữ liệu
    public static String loiTen(String ten) {
        return KhachHangBUS.loiTen(ten);
    }

    public static String loiSdt(String sdt) {
        return KhachHangBUS.loiSdt(sdt);
    }

    public static String loiGioHang(HoaDon h) {
        return h.trong() ? "Hóa đơn chưa có sản phẩm hoặc dịch vụ nào." : null;
    }

    /**
     * Hóa đơn phải gắn với một khách hàng đã đăng ký: số điện thoại phải có trong danh sách khách hàng
     * (hệ thống không tự tạo khách mới khi lưu hóa đơn) và tên phải khớp với khách đó.
     * Tên được so khớp không phân biệt hoa thường và dấu.
     */
    public String loiTrungSdt(String ten, String sdt) {
        KhachHang k = khachHang.timTheoSdtAnToan(KhachHangBUS.chuanHoaSdt(sdt));
        if (k == null) {
            return "Khách hàng chưa đăng ký. Hãy đăng ký khách hàng trước (Danh Mục > Khách Hàng) "
                    + "rồi lập hóa đơn.";
        }
        String a = ChuoiUtil.chuanHoa(KhachHangBUS.chuanHoaTen(ten));
        String b = ChuoiUtil.chuanHoa(KhachHangBUS.chuanHoaTen(k.getHoTen()));
        if (a.equals(b)) return null;
        return "Số điện thoại này đã thuộc khách hàng " + k.getHoTen() + " (" + k.getMaKH()
                + "). Hãy nhập đúng tên hoặc đổi số điện thoại.";
    }

    /** Lỗi đầu tiên của hóa đơn hoặc null nếu hợp lệ. */
    public String kiemTra(HoaDon h) {
        String e = loiTen(h.getTenKH());
        if (e == null) e = loiSdt(h.getSdt());
        if (e == null) e = loiTrungSdt(h.getTenKH(), h.getSdt());
        if (e == null) e = loiGioHang(h);
        return e;
    }

    // ------------------------------------------------------------ lưu / thanh toán
    /** @return true nếu hóa đơn này đã được lưu và đang nằm ở ô được gán. */
    public boolean daLuu(HoaDon h) {
        HoaDon g = cacO[h.getSoO() - 1];
        return g != null && g.getMaHD().equals(h.getMaHD());
    }

    /**
     * Lưu hóa đơn vào ô được gán (Chưa thanh toán) và lưu vào SQL Server.
     */
    public void luu(HoaDon nhap) {
        String loi = kiemTra(nhap);
        if (loi != null) throw new IllegalArgumentException(loi);
        if (nhap.daThanhToan()) throw new IllegalStateException("Hóa đơn đã thanh toán, không thể sửa.");
        if (maNVDangNhap.isEmpty()) {
            throw new IllegalStateException("Chưa xác định nhân viên đăng nhập, không thể lập hóa đơn.");
        }
        int i = nhap.getSoO() - 1;
        HoaDon cu = cacO[i];
        if (cu != null && !cu.getMaHD().equals(nhap.getMaHD())) {
            throw new IllegalStateException("Ô " + nhap.getSoO() + " đang chứa hóa đơn khác.");
        }

        // Gắn hóa đơn với khách hàng đã đăng ký (không tự tạo khách mới) và nhân viên đang đăng nhập
        String ten = KhachHangBUS.chuanHoaTen(nhap.getTenKH());
        String sdt = KhachHangBUS.chuanHoaSdt(nhap.getSdt());
        KhachHang k = khachHang.timTheoSdtAnToan(sdt);
        if (k == null) throw new IllegalArgumentException(loiTrungSdt(ten, sdt));
        nhap.setTenKH(ten);
        nhap.setSdt(KhachHangBUS.dinhDangSdt(sdt));
        nhap.setMaKH(k.getMaKH());
        if (nhap.getMaNV().isEmpty()) nhap.setMaNV(maNVDangNhap);   // giữ nguyên người lập khi người khác sửa/thanh toán

        if (cu == null) {
            nhap.setNgayTao(LocalDateTime.now());
            soHoaDonTiepTheo++;                       // chỉ cấp số khi lưu lần đầu
        }
        cacO[i] = nhap.saoChep();

        try {
            dao.luuHoaDon(cacO[i]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa hóa đơn đã lưu (chưa thanh toán) khỏi ô và xóa khỏi SQL Server.
     */
    public HoaDon xoa(int viTri) {
        HoaDon h = cacO[viTri];
        if (h == null) throw new IllegalStateException("Ô " + (viTri + 1) + " đang trống.");
        cacO[viTri] = null;
        try {
            dao.xoa(h.getMaHD());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return h;
    }

    /**
     * Ghi nhận thanh toán đã được xác nhận thành công: hóa đơn rời ô, vào lịch sử và cập nhật vào SQL Server.
     */
    public void thanhToan(HoaDon nhap, String phuongThuc) {
        if (phuongThuc == null || !List.of(PHUONG_THUC).contains(phuongThuc)) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }
        luu(nhap);
        int i = nhap.getSoO() - 1;
        HoaDon h = cacO[i];
        h.danhDauDaThanhToan(phuongThuc, LocalDateTime.now());
        cacO[i] = null;
        lichSu.add(0, h);

        try {
            dao.thanhToan(h);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // đồng bộ lại bản nháp đang mở để giao diện biết hóa đơn đã thanh toán
        nhap.danhDauDaThanhToan(phuongThuc, h.getNgayThanhToan());
        khachHang.ghiNhanThanhToan(h.getMaKH(), h.canThanhToan());   // cộng vào tổng chi tiêu của khách
    }

    // ------------------------------------------------------------ lịch sử
    public List<HoaDon> layLichSu() {
        return Collections.unmodifiableList(lichSu);
    }

    /** Tìm theo mã hóa đơn, tên khách, mã khách hàng hoặc số điện thoại; lọc theo ngày thanh toán (null = bỏ qua). */
    public List<HoaDon> timLichSu(String tuKhoa, LocalDate tu, LocalDate den) {
        String kw = ChuoiUtil.chuanHoa(tuKhoa == null ? "" : tuKhoa.trim());
        String kwSo = kw.replaceAll("[\\s.+-]", "").matches("\\d+") ? ChuoiUtil.chiSo(kw) : "";
        List<HoaDon> kq = new ArrayList<>();
        for (HoaDon h : lichSu) {
            LocalDate ngay = h.getNgayThanhToan() != null ? h.getNgayThanhToan().toLocalDate() : h.getNgayTao().toLocalDate();
            if (tu != null && ngay.isBefore(tu)) continue;
            if (den != null && ngay.isAfter(den)) continue;
            if (!kw.isEmpty()) {
                String hay = ChuoiUtil.chuanHoa(h.getMaHD()) + "|" + ChuoiUtil.chuanHoa(h.getTenKH())
                        + "|" + ChuoiUtil.chuanHoa(h.getMaKH());
                boolean khop = hay.contains(kw)
                        || (!kwSo.isEmpty() && ChuoiUtil.chiSo(h.getSdt()).contains(kwSo));
                if (!khop) continue;
            }
            kq.add(h);
        }
        return kq;
    }
}
