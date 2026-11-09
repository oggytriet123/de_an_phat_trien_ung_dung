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

    private final HoaDon[] cacO = new HoaDon[SO_O];       // null = ô trống (hỗ trợ tương thích ngược)
    private final List<HoaDon> dsHoaDonCho = new ArrayList<>(); // Toàn bộ hóa đơn chờ (không giới hạn 20 ô)
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
        dsHoaDonCho.clear();
        lichSu.clear();

        // 1. Tải toàn bộ danh sách hóa đơn chờ từ CSDL
        List<HoaDon> choTuDb = dao.layDanhSachHoaDonCho();
        dsHoaDonCho.addAll(choTuDb);

        // Map vào các ô 1..20 nếu có
        for (HoaDon h : choTuDb) {
            if (h.getSoO() >= 1 && h.getSoO() <= SO_O) {
                cacO[h.getSoO() - 1] = h;
            }
        }

        // Không tải toàn bộ lịch sử hóa đơn lên RAM lúc khởi động (tối ưu hóa theo Server-side Pagination)
        soHoaDonTiepTheo = dao.laySoHoaDonTiepTheo();
    }

    // ------------------------------------------------------------ Hóa đơn chờ động
    public synchronized List<HoaDon> layDanhSachHoaDonCho() {
        return Collections.unmodifiableList(dsHoaDonCho);
    }

    public synchronized HoaDon timHoaDonCho(String maHD) {
        if (maHD == null) return null;
        for (HoaDon h : dsHoaDonCho) {
            if (maHD.equalsIgnoreCase(h.getMaHD())) return h;
        }
        return null;
    }

    /** Tạo bản nháp mới tinh cho chức năng Lập hóa đơn */
    public HoaDon taoNhapMoi() {
        return new HoaDon(maTiepTheo(), 0);
    }

    /** Mở lại bản sao độc lập của một hóa đơn chờ theo mã để chỉnh sửa */
    public HoaDon moNhap(String maHD) {
        HoaDon h = timHoaDonCho(maHD);
        return h != null ? h.saoChep() : null;
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
        if (h.getKhachHang() == null && (h.getMaKH() == null || h.getMaKH().trim().isEmpty())) {
            String eTen = loiTen(h.getTenKH());
            if (eTen != null) return eTen;
            String eSdt = loiSdt(h.getSdt());
            if (eSdt != null) return eSdt;
            String eTrung = loiTrungSdt(h.getTenKH(), h.getSdt());
            if (eTrung != null) return eTrung;
        }
        if (h.trong()) return "Hóa đơn chưa có sản phẩm hoặc dịch vụ nào.";
        return null;
    }

    // ------------------------------------------------------------ lưu / thanh toán
    /** @return true nếu hóa đơn này đã được lưu và đang nằm trong danh sách chờ. */
    public boolean daLuu(HoaDon h) {
        if (h == null || h.getMaHD() == null) return false;
        return timHoaDonCho(h.getMaHD()) != null;
    }

    /**
     * Lưu hóa đơn (Chưa thanh toán) vào danh sách hóa đơn chờ và lưu vào SQL Server.
     */
    public synchronized void luu(HoaDon nhap) {
        String loi = kiemTra(nhap);
        if (loi != null) throw new IllegalArgumentException(loi);
        if (nhap.daThanhToan()) throw new IllegalStateException("Hóa đơn đã thanh toán, không thể sửa.");
        if (maNVDangNhap.isEmpty()) {
            throw new IllegalStateException("Chưa xác định nhân viên đăng nhập, không thể lập hóa đơn.");
        }

        // Gắn hóa đơn với khách hàng đã đăng ký và nhân viên đang đăng nhập
        KhachHang k = nhap.getKhachHang();
        if (k == null && nhap.getMaKH() != null && !nhap.getMaKH().trim().isEmpty()) {
            k = khachHang.timTheoMa(nhap.getMaKH());
        }
        if (k == null) {
            String sdt = KhachHangBUS.chuanHoaSdt(nhap.getSdt());
            k = khachHang.timTheoSdtAnToan(sdt);
        }
        if (k == null) {
            throw new IllegalArgumentException("Vui lòng chọn hoặc thêm khách hàng cho hóa đơn.");
        }

        nhap.setKhachHang(k);
        nhap.setTenKH(k.getHoTen());
        nhap.setSdt(KhachHangBUS.dinhDangSdt(k.getSoDienThoai()));
        nhap.setMaKH(k.getMaKH());

        if (nhap.getMaNV().isEmpty()) nhap.setMaNV(maNVDangNhap);
        if (nhap.getNhanVien() == null && !nhap.getMaNV().isEmpty()) {
            Entity.NhanVien nv = new Entity.NhanVien();
            nv.setMaNV(nhap.getMaNV());
            nhap.setNhanVien(nv);
        }

        HoaDon cu = timHoaDonCho(nhap.getMaHD());
        if (cu == null) {
            nhap.setNgayTao(LocalDateTime.now());
            soHoaDonTiepTheo++;                       // chỉ cấp số khi lưu lần đầu
            dsHoaDonCho.add(0, nhap.saoChep());
        } else {
            int idx = dsHoaDonCho.indexOf(cu);
            if (idx >= 0) {
                dsHoaDonCho.set(idx, nhap.saoChep());
            }
        }

        if (nhap.getSoO() >= 1 && nhap.getSoO() <= SO_O) {
            cacO[nhap.getSoO() - 1] = nhap.saoChep();
        }

        try {
            dao.luuHoaDon(nhap);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi lưu hóa đơn vào CSDL: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa hóa đơn đã lưu (chưa thanh toán) theo mã và xóa khỏi SQL Server.
     */
    public synchronized HoaDon xoaHoaDon(String maHD) {
        if (maHD == null) return null;
        HoaDon h = timHoaDonCho(maHD);
        if (h != null) {
            dsHoaDonCho.remove(h);
            if (h.getSoO() >= 1 && h.getSoO() <= SO_O) {
                cacO[h.getSoO() - 1] = null;
            }
        }
        try {
            dao.xoa(maHD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi xóa hóa đơn: " + e.getMessage(), e);
        }
        return h;
    }

    /**
     * Xóa hóa đơn theo vị trí ô (tương thích ngược với các module cũ).
     */
    public HoaDon xoa(int viTri) {
        if (viTri >= 0 && viTri < SO_O && cacO[viTri] != null) {
            return xoaHoaDon(cacO[viTri].getMaHD());
        }
        if (viTri >= 0 && viTri < dsHoaDonCho.size()) {
            return xoaHoaDon(dsHoaDonCho.get(viTri).getMaHD());
        }
        throw new IllegalStateException("Không tìm thấy hóa đơn cần xóa.");
    }

    /**
     * Ghi nhận thanh toán đã được xác nhận thành công: rời danh sách chờ, vào lịch sử và cập nhật vào SQL Server.
     */
    public synchronized void thanhToan(HoaDon nhap, String phuongThuc) {
        if (phuongThuc == null || !List.of(PHUONG_THUC).contains(phuongThuc)) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }
        luu(nhap);
        HoaDon h = timHoaDonCho(nhap.getMaHD());
        if (h == null) h = nhap;
        LocalDateTime luc = LocalDateTime.now();
        h.danhDauDaThanhToan(phuongThuc, luc);
        dsHoaDonCho.remove(h);
        if (h.getSoO() >= 1 && h.getSoO() <= SO_O) {
            cacO[h.getSoO() - 1] = null;
        }
        lichSu.add(0, h);

        try {
            dao.thanhToan(h);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi thanh toán: " + e.getMessage(), e);
        }

        // đồng bộ lại bản nháp đang mở để giao diện biết hóa đơn đã thanh toán
        nhap.danhDauDaThanhToan(phuongThuc, luc);
        khachHang.ghiNhanThanhToan(h.getMaKH(), h.canThanhToan());   // cộng vào tổng chi tiêu của khách
    }

    // ------------------------------------------------------------ lịch sử
    public List<HoaDon> layLichSu() {
        return Collections.unmodifiableList(lichSu);
    }

    /** Đếm tổng số hóa đơn đã thanh toán theo điều kiện tìm kiếm và ngày trực tiếp trên CSDL. */
    public int demLichSu(String tuKhoa, LocalDate tu, LocalDate den) {
        return dao.demLichSu(tuKhoa, tu, den);
    }

    /** Lấy danh sách hóa đơn theo từng trang trực tiếp từ SQL Server (Server-side Pagination). */
    public List<HoaDon> layLichSuPhanTrang(String tuKhoa, LocalDate tu, LocalDate den, int trang, int soLuongMoiTrang) {
        return dao.layLichSuPhanTrang(tuKhoa, tu, den, trang, soLuongMoiTrang);
    }

    /** Tải chi tiết sản phẩm / dịch vụ khi người dùng xem chi tiết hoặc in hóa đơn. */
    public void napChiTietHoaDon(HoaDon h) {
        dao.napChiTietChoHoaDon(h);
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
