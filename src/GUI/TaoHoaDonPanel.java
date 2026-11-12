package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.HoaDonBUS;
import Bus.KhachHangBUS;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.MaGiamGia;
import Entity.SanPham;

/**
 * Giao diện Lập hóa đơn và Chỉnh sửa hóa đơn chờ.
 * Kế thừa toàn bộ giao diện, nút bấm, logic giỏ hàng, mã giảm giá và thanh toán.
 *
 * Chế độ hoạt động:
 * - TAO_MOI: Lập hóa đơn mới (vào thẳng từ menu Lập Hóa Đơn, không qua 20 ô).
 * - HOA_DON_CHO: Xem và chỉnh sửa hóa đơn chờ (có nút Lưu, Thoát, Xóa, Hủy, Thanh toán).
 */
public class TaoHoaDonPanel extends JPanel {
    public enum CheDo {
        TAO_MOI,
        HOA_DON_CHO
    }

    private final HoaDonBUS bus;
    private final SanPhamBUS spBus;
    private final MaGiamGiaBUS mgBus;
    private final KhachHangBUS khBus;
    private final Runnable onThoat;

    private CheDo cheDo = CheDo.TAO_MOI;
    private HoaDon hd;               // Bản nháp đang thao tác
    private HoaDon banGoc;            // Snapshot khi mở/lưu hóa đơn chờ để kiểm tra thay đổi chưa lưu
    private KhachHang khachHangChon; // Khách hàng hiện đang liên kết với hóa đơn
    private String danhMuc = SanPhamBUS.TAT_CA;

    // Tiêu đề & Breadcrumb
    private final JLabel lblCrumbBack = new JLabel();
    private final JLabel lblCrumbSep = new JLabel("   /   ");
    private final JLabel lblCrumbCurrent = new JLabel();
    private final JLabel lblTitle = new JLabel();
    private final JLabel lblSub = new JLabel();

    // Khối khách hàng (Tìm kiếm duy nhất / Thẻ khách hàng đã chọn)
    private final JPanel pnlKhachHangContainer = new JPanel(new CardLayout());
    private static final String VIEW_SEARCH = "SEARCH";
    private static final String VIEW_INFO = "INFO";

    private final PlaceholderField tfTimKhach = new PlaceholderField("Nhập tên hoặc số điện thoại khách hàng để tìm kiếm...");
    private final JPopupMenu popupGoiYKhach = new JPopupMenu();
    private final RoundedButton btnThemKhachMoi = new RoundedButton("+ Thêm khách hàng mới", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(14, 8);

    private final JLabel lblTenKhachInfo = new JLabel();
    private final JLabel lblSdtKhachInfo = new JLabel();
    private final JLabel lblMaKhachInfo = new JLabel();
    private final JLabel lblHangThePill = new JLabel();
    private final JLabel lblDiemChiTieu = new JLabel();
    private final RoundedButton btnThayDoiKhach = new RoundedButton("Thay đổi", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8).pad(12, 6);
    private final JCheckBox chkPreOrder = new JCheckBox("Đơn đặt trước (Pre-order)");

    // Chọn sản phẩm / dịch vụ
    private final PlaceholderField tfTimSp = new PlaceholderField("Tìm tên sản phẩm, dịch vụ hoặc mã SKU...");
    private final List<Pill> pills = new ArrayList<>();
    private final WrapPanel pillRow = new WrapPanel(8, 8, 640);
    private final JPanel gridSp = new JPanel(new GridLayout(0, 3, 12, 12));
    private final ScrollPanel gridViewSp = new ScrollPanel(new BorderLayout());
    private final JLabel lblKhongCoSp = new JLabel("Không tìm thấy sản phẩm hoặc dịch vụ phù hợp", SwingConstants.CENTER);

    // Chi tiết hóa đơn
    private final JLabel lblMaHD = new JLabel(" ", SwingConstants.RIGHT);
    private final RoundedButton btnXoaHd = new RoundedButton("Xóa hóa đơn này", Color.WHITE, new Color(0xFDECEC), Theme.DANGER, new Color(0xF1B5B5), 8);
    private final JPanel pnlDongHang = new JPanel();
    private JScrollPane dongScroll;
    private final JLabel lblTamTinh = new JLabel();
    private final JLabel lblGiamGia = new JLabel();
    private final JLabel lblTongCong = new JLabel();
    private final JLabel lblLoi = new JLabel("");
    private final JLabel lblHuongDanLuu = new JLabel("Hóa đơn đã lưu sẽ được giữ an toàn trong Hóa đơn chờ.");

    private final RoundedButton btnMaGiamGia = new RoundedButton("+  Chọn mã giảm giá", Theme.PRIMARY_SOFT, new Color(0xDCE8FA), Theme.PRIMARY, Theme.PRIMARY_LINE, 8);
    private final RoundedButton btnGoMa = new RoundedButton("Gỡ mã", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);

    // Các nút hành động chính
    private final RoundedButton btnLuu = new RoundedButton("Lưu", Theme.SUCCESS, Theme.SUCCESS_HOVER, Color.WHITE, null, 8);
    private final RoundedButton btnThoat = new RoundedButton("Thoát", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
    private final RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
    private final RoundedButton btnThanhToan = new RoundedButton("Thanh toán", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);

    public TaoHoaDonPanel(HoaDonBUS bus, SanPhamBUS spBus, MaGiamGiaBUS mgBus, KhachHangBUS khBus, Runnable onThoat) {
        super(new BorderLayout(0, 14));
        this.bus = bus;
        this.spBus = spBus;
        this.mgBus = mgBus;
        this.khBus = khBus;
        this.onThoat = onThoat;

        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 30, 22, 30));

        chkPreOrder.setOpaque(false);
        chkPreOrder.setFont(Theme.font(Font.BOLD, 12));
        chkPreOrder.setForeground(Theme.PRIMARY);
        chkPreOrder.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chkPreOrder.addActionListener(e -> {
            if (hd != null) hd.setPreOrder(chkPreOrder.isSelected());
        });

        add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.add(buildKhachHangSection(), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(18, 0));
        main.setOpaque(false);
        main.add(buildChonSanPham(), BorderLayout.CENTER);
        main.add(buildChiTietHoaDon(), BorderLayout.EAST);
        content.add(main, BorderLayout.CENTER);

        ScrollPanel trangCuon = new ScrollPanel(new BorderLayout()) {
            @Override public boolean getScrollableTracksViewportHeight() {
                return getParent() instanceof JViewport vp && vp.getHeight() >= getPreferredSize().height;
            }
        };
        trangCuon.setOpaque(false);
        trangCuon.add(content, BorderLayout.CENTER);

        JScrollPane khung = new JScrollPane(trangCuon, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        khung.setBorder(BorderFactory.createEmptyBorder());
        khung.setViewportBorder(null);
        khung.setOpaque(false);
        khung.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(khung);
        add(khung, BorderLayout.CENTER);

        dangKyListeners();
    }

    // =========================================================================
    // KHỞI ĐỘNG CÁC PHIÊN LÀM VIỆC (LẬP MỚI HOẶC MỞ HÓA ĐƠN CHỜ)
    // =========================================================================

    /** Phương thức tương thích ngược */
    public void batDau(HoaDon nhap) {
        if (nhap == null || !bus.daLuu(nhap)) {
            batDauTaoMoi();
        } else {
            batDauHoaDonCho(nhap);
        }
    }

    public void yeuCauHuy() {
        xuLyHuy();
    }

    /** Mở chế độ Lập Hóa Đơn mới */
    public void batDauTaoMoi() {
        this.cheDo = CheDo.TAO_MOI;
        this.hd = bus.taoNhapMoi();
        this.banGoc = null;
        this.khachHangChon = null;
        hienThiTrangThaiKhach();
        datLoi(" ");
        danhMuc = SanPhamBUS.TAT_CA;
        tfTimSp.setText("");
        chkPreOrder.setSelected(false);

        capNhatGiaoDienTheoCheDo();
        dungPills();
        taiSanPham();
        renderChiTiet(false);
        SwingUtilities.invokeLater(tfTimKhach::requestFocusInWindow);
    }

    /** Mở chế độ xem/chỉnh sửa một hóa đơn chờ đã lưu */
    public void batDauHoaDonCho(HoaDon hoaDonCho) {
        this.cheDo = CheDo.HOA_DON_CHO;
        this.hd = hoaDonCho.saoChep();
        this.banGoc = hoaDonCho.saoChep(); // Lưu snapshot để kiểm tra thay đổi chưa lưu
        this.khachHangChon = null;

        if (hd.getKhachHang() != null) {
            this.khachHangChon = hd.getKhachHang();
        } else if (hd.getMaKH() != null && !hd.getMaKH().trim().isEmpty()) {
            this.khachHangChon = khBus.timTheoMa(hd.getMaKH());
        }
        if (this.khachHangChon == null && hd.getSdt() != null && !hd.getSdt().trim().isEmpty()) {
            this.khachHangChon = khBus.timTheoSdtAnToan(hd.getSdt());
        }

        hienThiTrangThaiKhach();
        datLoi(" ");
        danhMuc = SanPhamBUS.TAT_CA;
        tfTimSp.setText("");
        chkPreOrder.setSelected(hd.isPreOrder());

        capNhatGiaoDienTheoCheDo();
        dungPills();
        taiSanPham();
        renderChiTiet(false);
    }

    /** Kiểm tra xem hóa đơn chờ hiện tại có thay đổi nào chưa lưu hay không */
    public boolean coThayDoi() {
        if (cheDo != CheDo.HOA_DON_CHO || hd == null || banGoc == null) return false;

        String maKhA = hd.getMaKH() != null ? hd.getMaKH().trim() : "";
        String maKhB = banGoc.getMaKH() != null ? banGoc.getMaKH().trim() : "";
        if (!maKhA.equals(maKhB)) return true;

        String tenA = hd.getTenKH() != null ? hd.getTenKH().trim() : "";
        String tenB = banGoc.getTenKH() != null ? banGoc.getTenKH().trim() : "";
        if (!tenA.equals(tenB)) return true;

        String sdtA = hd.getSdt() != null ? hd.getSdt().trim() : "";
        String sdtB = banGoc.getSdt() != null ? banGoc.getSdt().trim() : "";
        if (!sdtA.equals(sdtB)) return true;

        if (hd.isPreOrder() != banGoc.isPreOrder()) return true;

        String mgA = hd.getMaGiamGia() != null ? hd.getMaGiamGia().getMa() : "";
        String mgB = banGoc.getMaGiamGia() != null ? banGoc.getMaGiamGia().getMa() : "";
        if (!mgA.equalsIgnoreCase(mgB)) return true;

        List<ChiTietHoaDon> dsA = hd.getDanhSachChiTiet();
        List<ChiTietHoaDon> dsB = banGoc.getDanhSachChiTiet();
        if (dsA.size() != dsB.size()) return true;

        for (int i = 0; i < dsA.size(); i++) {
            ChiTietHoaDon a = dsA.get(i);
            ChiTietHoaDon b = dsB.get(i);
            String skuA = a.getSanPham() != null ? a.getSanPham().getSku() : "";
            String skuB = b.getSanPham() != null ? b.getSanPham().getSku() : "";
            if (!skuA.equals(skuB)) return true;
            if (a.getSoLuong() != b.getSoLuong()) return true;
            if (Double.compare(a.getDonGia(), b.getDonGia()) != 0) return true;
        }

        return false;
    }

    private void capNhatGiaoDienTheoCheDo() {
        if (cheDo == CheDo.TAO_MOI) {
            lblCrumbBack.setText("Xử lý");
            lblCrumbCurrent.setText("Lập hóa đơn mới");
            lblTitle.setText("Lập hóa đơn");
            lblSub.setText("Tìm kiếm khách hàng, thêm sản phẩm / dịch vụ và thanh toán hoặc lưu chờ.");
            lblMaHD.setText(hd.getMaHD() + "  •  Mới");
            btnXoaHd.setVisible(false);
            btnThoat.setVisible(false);
            btnHuy.setVisible(true);
            btnHuy.setText("Hủy");
            lblHuongDanLuu.setText("Hóa đơn lưu tại đây sẽ được đưa vào danh sách Hóa đơn chờ.");
        } else {
            lblCrumbBack.setText("Hóa đơn chờ");
            lblCrumbCurrent.setText("Chi tiết " + hd.getMaHD());
            lblTitle.setText("Chi tiết hóa đơn chờ");
            lblSub.setText("Tiếp tục chỉnh sửa hoặc thanh toán hóa đơn đang lưu trong hệ thống.");
            lblMaHD.setText(hd.getMaHD() + (hd.getSoO() > 0 ? "  •  Ô " + String.format("%02d", hd.getSoO()) : "  •  Chờ"));
            btnXoaHd.setVisible(true);
            btnThoat.setVisible(true);
            btnHuy.setVisible(true);
            btnHuy.setText("Hủy thay đổi");
            lblHuongDanLuu.setText("Nhấn Lưu để cập nhật thay đổi vào cơ sở dữ liệu.");
        }
    }

    // =========================================================================
    // 1. HEADER & BREADCRUMB
    // =========================================================================
    private JComponent buildHeader() {
        lblCrumbBack.setFont(Theme.font(Font.PLAIN, 12));
        lblCrumbBack.setForeground(Theme.MUTED);
        lblCrumbBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblCrumbBack.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { lblCrumbBack.setForeground(Theme.PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { lblCrumbBack.setForeground(Theme.MUTED); }
            @Override public void mouseClicked(MouseEvent e) { xuLyThoatHoacTroVe(); }
        });

        lblCrumbSep.setFont(Theme.font(Font.PLAIN, 12));
        lblCrumbSep.setForeground(Theme.MUTED);
        lblCrumbCurrent.setFont(Theme.font(Font.PLAIN, 12));
        lblCrumbCurrent.setForeground(Theme.MUTED);

        JPanel crumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        crumb.setOpaque(false);
        crumb.add(lblCrumbBack);
        crumb.add(lblCrumbSep);
        crumb.add(lblCrumbCurrent);

        lblTitle.setFont(Theme.font(Font.BOLD, 26));
        lblTitle.setForeground(Theme.TEXT);
        lblSub.setFont(Theme.font(Font.PLAIN, 13));
        lblSub.setForeground(Theme.MUTED);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[] {crumb, lblTitle, lblSub}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(crumb);
        head.add(Box.createVerticalStrut(6));
        head.add(lblTitle);
        head.add(Box.createVerticalStrut(4));
        head.add(lblSub);
        return head;
    }

    // =========================================================================
    // 2. KHỐI TÌM KIẾM VÀ LIÊN KẾT KHÁCH HÀNG (YÊU CẦU 2 & 3)
    // =========================================================================
    private JComponent buildKhachHangSection() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 20, 16, 20));

        JLabel title = new JLabel("KHÁCH HÀNG LIÊN KẾT");
        title.setFont(Theme.font(Font.BOLD, 12));
        title.setForeground(Theme.MUTED);
        card.add(title, BorderLayout.NORTH);

        // View 1: Ô tìm kiếm duy nhất + Nút + Thêm khách hàng mới
        JPanel pnlSearch = new JPanel(new BorderLayout(14, 0));
        pnlSearch.setOpaque(false);
        FieldBox searchBox = new FieldBox(tfTimKhach, Theme.FIELD_BG, 40);
        btnThemKhachMoi.addActionListener(e -> moPopupThemKhachMoi(tfTimKhach.getText()));
        pnlSearch.add(searchBox, BorderLayout.CENTER);
        pnlSearch.add(btnThemKhachMoi, BorderLayout.EAST);

        // View 2: Thẻ hiển thị thông tin khách hàng đã chọn
        RoundedPanel pnlInfo = new RoundedPanel(10, Theme.FIELD_BG, new Color(0xCBD5E1));
        pnlInfo.setLayout(new BorderLayout(14, 0));
        pnlInfo.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel pnlLeftInfo = new JPanel();
        pnlLeftInfo.setOpaque(false);
        pnlLeftInfo.setLayout(new BoxLayout(pnlLeftInfo, BoxLayout.Y_AXIS));

        JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        line1.setOpaque(false);
        lblTenKhachInfo.setFont(Theme.font(Font.BOLD, 15));
        lblTenKhachInfo.setForeground(Theme.TEXT);
        lblSdtKhachInfo.setFont(Theme.font(Font.BOLD, 13));
        lblSdtKhachInfo.setForeground(Theme.PRIMARY);
        lblMaKhachInfo.setFont(Theme.font(Font.PLAIN, 12));
        lblMaKhachInfo.setForeground(Theme.MUTED);

        line1.add(lblTenKhachInfo);
        line1.add(new JLabel("•"));
        line1.add(lblSdtKhachInfo);
        line1.add(new JLabel("•"));
        line1.add(lblMaKhachInfo);

        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        line2.setOpaque(false);
        lblHangThePill.setFont(Theme.font(Font.BOLD, 11));
        lblHangThePill.setOpaque(true);
        lblHangThePill.setBorder(new EmptyBorder(2, 8, 2, 8));

        lblDiemChiTieu.setFont(Theme.font(Font.PLAIN, 12));
        lblDiemChiTieu.setForeground(Theme.TEXT);

        line2.add(lblHangThePill);
        line2.add(lblDiemChiTieu);

        pnlLeftInfo.add(line1);
        pnlLeftInfo.add(Box.createVerticalStrut(4));
        pnlLeftInfo.add(line2);

        JPanel pnlRightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        pnlRightActions.setOpaque(false);
        btnThayDoiKhach.addActionListener(e -> {
            giaiPhongKhachHang();
            tfTimKhach.requestFocusInWindow();
        });
        pnlRightActions.add(chkPreOrder);
        pnlRightActions.add(btnThayDoiKhach);

        pnlInfo.add(pnlLeftInfo, BorderLayout.CENTER);
        pnlInfo.add(pnlRightActions, BorderLayout.EAST);

        pnlKhachHangContainer.setOpaque(false);
        pnlKhachHangContainer.add(pnlSearch, VIEW_SEARCH);
        pnlKhachHangContainer.add(pnlInfo, VIEW_INFO);

        card.add(pnlKhachHangContainer, BorderLayout.CENTER);
        return card;
    }

    private void hienThiTrangThaiKhach() {
        CardLayout cl = (CardLayout) pnlKhachHangContainer.getLayout();
        if (khachHangChon != null) {
            lblTenKhachInfo.setText(khachHangChon.getHoTen());
            lblSdtKhachInfo.setText(khachHangChon.getSoDienThoai());
            lblMaKhachInfo.setText("(" + khachHangChon.getMaKH() + ")");

            String hang = khachHangChon.getHangThe() != null ? khachHangChon.getHangThe() : "Thường";
            lblHangThePill.setText("Hạng " + hang);
            if ("VIP".equalsIgnoreCase(hang)) {
                lblHangThePill.setBackground(new Color(0xFEF3C7));
                lblHangThePill.setForeground(new Color(0xB45309));
            } else if ("Thân thiết".equalsIgnoreCase(hang) || "Bạc".equalsIgnoreCase(hang) || "Vàng".equalsIgnoreCase(hang)) {
                lblHangThePill.setBackground(new Color(0xE0F2FE));
                lblHangThePill.setForeground(new Color(0x0369A1));
            } else {
                lblHangThePill.setBackground(new Color(0xF1F5F9));
                lblHangThePill.setForeground(new Color(0x475569));
            }

            String diem = String.format("%,d", khachHangChon.getDiemTichLuy()).replace(',', '.');
            lblDiemChiTieu.setText("Điểm tích lũy: " + diem + "  |  Tổng chi tiêu: " + Theme.money(khachHangChon.getTongChiTieu()));
            cl.show(pnlKhachHangContainer, VIEW_INFO);
        } else {
            tfTimKhach.setText("");
            cl.show(pnlKhachHangContainer, VIEW_SEARCH);
        }
    }

    private void chonKhachHang(KhachHang kh) {
        if (kh == null || hd == null) return;
        this.khachHangChon = kh;
        hd.setKhachHang(kh);
        hd.setTenKH(kh.getHoTen());
        hd.setSdt(kh.getSoDienThoai());
        hd.setMaKH(kh.getMaKH());
        datLoi(" ");
        hienThiTrangThaiKhach();
    }

    private void giaiPhongKhachHang() {
        this.khachHangChon = null;
        if (hd != null) {
            hd.setKhachHang(null);
            hd.setTenKH("");
            hd.setSdt("");
            hd.setMaKH("");
        }
        hienThiTrangThaiKhach();
    }

    private void timKiemVaHienThiGoiY() {
        String kw = tfTimKhach.getText().trim();
        popupGoiYKhach.setVisible(false);
        popupGoiYKhach.removeAll();

        if (kw.isEmpty()) return;

        List<KhachHang> ketQua = khBus.timKiem(kw);
        int toiDa = Math.min(ketQua.size(), 7);

        if (ketQua.isEmpty()) {
            JPanel pnlKhongCo = new JPanel(new BorderLayout(8, 0));
            pnlKhongCo.setBackground(Color.WHITE);
            pnlKhongCo.setBorder(new EmptyBorder(10, 14, 10, 14));

            JLabel lblNone = new JLabel("Không tìm thấy khách hàng phù hợp");
            lblNone.setFont(Theme.font(Font.PLAIN, 13));
            lblNone.setForeground(Theme.MUTED);

            RoundedButton btnAdd = new RoundedButton("+ Thêm khách hàng mới", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 6).pad(10, 4);
            btnAdd.setFont(Theme.font(Font.PLAIN, 12));
            btnAdd.addActionListener(e -> {
                popupGoiYKhach.setVisible(false);
                moPopupThemKhachMoi(kw);
            });

            pnlKhongCo.add(lblNone, BorderLayout.CENTER);
            pnlKhongCo.add(btnAdd, BorderLayout.EAST);
            popupGoiYKhach.add(pnlKhongCo);
        } else {
            for (int i = 0; i < toiDa; i++) {
                KhachHang k = ketQua.get(i);
                JMenuItem item = new JMenuItem();
                item.setLayout(new BorderLayout());
                item.setBackground(Color.WHITE);
                item.setBorder(new EmptyBorder(8, 14, 8, 14));
                item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JLabel lblTen = new JLabel(k.getHoTen() + "  -  " + k.getSoDienThoai() + " (" + k.getMaKH() + ")");
                lblTen.setFont(Theme.font(Font.BOLD, 13));
                lblTen.setForeground(Theme.TEXT);

                JLabel lblSub = new JLabel("Hạng: " + k.getHangThe() + " • " + k.getDiemTichLuy() + " điểm • Chi tiêu: " + Theme.money(k.getTongChiTieu()));
                lblSub.setFont(Theme.font(Font.PLAIN, 11));
                lblSub.setForeground(Theme.MUTED);

                JPanel box = new JPanel();
                box.setOpaque(false);
                box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
                box.add(lblTen);
                box.add(Box.createVerticalStrut(2));
                box.add(lblSub);

                item.add(box, BorderLayout.CENTER);
                item.addActionListener(e -> chonKhachHang(k));
                popupGoiYKhach.add(item);
            }

            popupGoiYKhach.addSeparator();
            JMenuItem itemThem = new JMenuItem("+ Thêm khách hàng mới");
            itemThem.setFont(Theme.font(Font.BOLD, 12));
            itemThem.setForeground(Theme.ACCENT);
            itemThem.setBackground(Color.WHITE);
            itemThem.setBorder(new EmptyBorder(8, 14, 8, 14));
            itemThem.addActionListener(e -> {
                popupGoiYKhach.setVisible(false);
                moPopupThemKhachMoi(kw);
            });
            popupGoiYKhach.add(itemThem);
        }

        popupGoiYKhach.setFocusable(false);
        if (tfTimKhach.isShowing()) {
            popupGoiYKhach.show(tfTimKhach, 0, tfTimKhach.getHeight() + 4);
            tfTimKhach.requestFocusInWindow();
        }
    }

    private void moPopupThemKhachMoi(String tuKhoa) {
        String sdtGoiY = null;
        String tenGoiY = null;
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            String chiSo = KhachHangBUS.chiSo(tuKhoa);
            if (chiSo.length() >= 3) {
                sdtGoiY = chiSo;
            } else {
                tenGoiY = tuKhoa.trim();
            }
        }

        KhachHang khMoi = DialogThemKhachHang.hienThi(this, khBus, sdtGoiY, tenGoiY);
        if (khMoi != null) {
            chonKhachHang(khMoi);
        }
    }

    // =========================================================================
    // 3. CHỌN SẢN PHẨM & DỊCH VỤ
    // =========================================================================
    private JComponent buildChonSanPham() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 20, 0, 20));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JComponent title = sectionLabel("CHỌN SẢN PHẨM / DỊCH VỤ", 14, Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title);
        top.add(Box.createVerticalStrut(14));

        FieldBox tim = new FieldBox(tfTimSp, Theme.FIELD_BG, 40);
        RoundedButton quet = new RoundedButton("Quét mã", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        quet.setPreferredSize(new Dimension(126, 40));
        quet.addActionListener(e -> HoaDonPopups.quetMa(this, spBus, this::themSanPham));

        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.add(tim, BorderLayout.CENTER);
        row.add(quet, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        top.add(row);
        top.add(Box.createVerticalStrut(14));

        dungPills();
        pillRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(pillRow);
        card.add(top, BorderLayout.NORTH);

        gridSp.setOpaque(false);
        gridViewSp.setOpaque(false);
        lblKhongCoSp.setFont(Theme.font(Font.PLAIN, 13));
        lblKhongCoSp.setForeground(Theme.MUTED);
        lblKhongCoSp.setBorder(new EmptyBorder(40, 0, 0, 0));

        JScrollPane sp = new JScrollPane(gridViewSp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(sp);
        sp.setPreferredSize(new Dimension(0, 240));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 18, 0));
        wrap.add(sp, BorderLayout.CENTER);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private void dungPills() {
        pillRow.removeAll();
        pills.clear();
        for (String name : spBus.layDanhMuc()) {
            Pill p = new Pill(name);
            p.setSelected(name.equals(danhMuc));
            p.addActionListener(e -> { danhMuc = name; taiSanPham(); });
            pills.add(p);
            pillRow.add(p);
        }
        pillRow.revalidate();
        pillRow.repaint();
    }

    private void taiSanPham() {
        for (Pill p : pills) p.setSelected(p.getText().equals(danhMuc));
        List<SanPham> ds = spBus.loc(danhMuc, tfTimSp.getText());
        gridSp.removeAll();
        for (SanPham sp : ds) gridSp.add(new ProductItem(sp));
        gridViewSp.removeAll();
        gridViewSp.add(ds.isEmpty() ? lblKhongCoSp : gridSp, BorderLayout.NORTH);
        gridViewSp.revalidate();
        gridViewSp.repaint();
    }

    private void themSanPham(SanPham sp) {
        if (hd == null) return;
        hd.them(sp);
        datLoi(" ");
        renderChiTiet(true);
    }

    // =========================================================================
    // 4. CHI TIẾT HÓA ĐƠN & THANH TOÁN
    // =========================================================================
    private JComponent buildChiTietHoaDon() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(460, super.getPreferredSize().height);
            }
        };
        card.setLayout(new BorderLayout());

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(18, 20, 12, 20));
        head.add(sectionLabel("CHI TIẾT HÓA ĐƠN", 14, Theme.TEXT), BorderLayout.WEST);

        lblMaHD.setFont(Theme.font(Font.PLAIN, 12));
        lblMaHD.setForeground(Theme.MUTED);

        btnXoaHd.setFont(Theme.font(Font.PLAIN, 12));
        btnXoaHd.setPreferredSize(new Dimension(130, 28));
        btnXoaHd.addActionListener(e -> xuLyXoaHoaDon());

        JPanel dauPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dauPhai.setOpaque(false);
        dauPhai.add(lblMaHD);
        dauPhai.add(btnXoaHd);
        dauPhai.setBorder(new EmptyBorder(0, 0, 0, -10));
        head.add(dauPhai, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        pnlDongHang.setOpaque(false);
        pnlDongHang.setLayout(new BoxLayout(pnlDongHang, BoxLayout.Y_AXIS));

        ScrollPanel view = new ScrollPanel(new BorderLayout());
        view.setOpaque(false);
        view.setBorder(new EmptyBorder(0, 20, 0, 14));
        view.add(pnlDongHang, BorderLayout.NORTH);

        dongScroll = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dongScroll.setBorder(BorderFactory.createEmptyBorder());
        dongScroll.setViewportBorder(null);
        dongScroll.setOpaque(false);
        dongScroll.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(dongScroll);
        dongScroll.setPreferredSize(new Dimension(0, 130));
        card.add(dongScroll, BorderLayout.CENTER);

        // Chân: tổng tiền + nút
        JPanel foot = new JPanel();
        foot.setOpaque(false);
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        foot.setBorder(new EmptyBorder(6, 20, 16, 20));

        lblTamTinh.setFont(Theme.font(Font.PLAIN, 13));
        lblGiamGia.setFont(Theme.font(Font.PLAIN, 13));
        lblTamTinh.setForeground(Theme.TEXT);
        lblGiamGia.setForeground(Theme.TEXT);
        lblTongCong.setFont(Theme.font(Font.BOLD, 26));
        lblTongCong.setForeground(Theme.TEXT);

        foot.add(sumRow("Tạm tính", lblTamTinh, 13, false));
        foot.add(Box.createVerticalStrut(6));
        foot.add(sumRow("Giảm giá", lblGiamGia, 13, false));
        foot.add(Box.createVerticalStrut(8));

        btnMaGiamGia.setPreferredSize(new Dimension(0, 40));
        btnMaGiamGia.addActionListener(e -> HoaDonPopups.maGiamGia(this, hd, mgBus, m -> {
            hd.setMaGiamGia(m);
            renderChiTiet(false);
        }));

        btnGoMa.setFont(Theme.font(Font.PLAIN, 12));
        btnGoMa.setPreferredSize(new Dimension(72, 40));
        btnGoMa.addActionListener(e -> {
            hd.setMaGiamGia(null);
            renderChiTiet(false);
        });

        JPanel maRow = new JPanel(new BorderLayout(8, 0));
        maRow.setOpaque(false);
        maRow.add(btnMaGiamGia, BorderLayout.CENTER);
        maRow.add(btnGoMa, BorderLayout.EAST);
        maRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        maRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        foot.add(maRow);
        foot.add(Box.createVerticalStrut(14));

        JPanel line = new JPanel();
        line.setOpaque(false);
        line.setBorder(new MatteBorder(1, 0, 0, 0, Theme.ROW_LINE));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));
        foot.add(line);
        foot.add(Box.createVerticalStrut(12));
        foot.add(sumRow("TỔNG CỘNG", lblTongCong, 13, true));
        foot.add(Box.createVerticalStrut(10));

        lblHuongDanLuu.setFont(Theme.font(Font.PLAIN, 12));
        lblHuongDanLuu.setForeground(Theme.MUTED);
        lblHuongDanLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(lblHuongDanLuu);
        foot.add(Box.createVerticalStrut(6));

        lblLoi.setFont(Theme.font(Font.PLAIN, 12));
        lblLoi.setForeground(Theme.DANGER);
        lblLoi.setVerticalAlignment(SwingConstants.TOP);
        lblLoi.setBorder(new EmptyBorder(0, 0, 0, 16));
        lblLoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(lblLoi);
        foot.add(Box.createVerticalStrut(8));

        btnLuu.addActionListener(e -> xuLyLuu());
        btnThoat.addActionListener(e -> xuLyThoatHoacTroVe());
        btnHuy.addActionListener(e -> xuLyHuy());
        btnThanhToan.addActionListener(e -> xuLyThanhToan());

        JPanel btns = new JPanel(new GridLayout(1, 4, 10, 0));
        btns.setOpaque(false);
        btns.add(btnLuu);
        btns.add(btnThoat);
        btns.add(btnHuy);
        btns.add(btnThanhToan);
        btns.setPreferredSize(new Dimension(0, 44));
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(btns);

        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private void renderChiTiet(boolean cuonXuongCuoi) {
        if (hd == null) return;
        pnlDongHang.removeAll();

        if (hd.trong()) {
            JLabel a = new JLabel("Chưa có sản phẩm hoặc dịch vụ nào");
            a.setFont(Theme.font(Font.PLAIN, 13));
            a.setForeground(Theme.MUTED);
            a.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel b = new JLabel("Chọn từ danh sách bên trái để thêm vào hóa đơn");
            b.setFont(Theme.font(Font.PLAIN, 11));
            b.setForeground(new Color(0x9AA6B0));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setBorder(new EmptyBorder(36, 0, 0, 0));
            empty.add(a);
            empty.add(Box.createVerticalStrut(4));
            empty.add(b);
            pnlDongHang.add(empty);
        } else {
            for (ChiTietHoaDon d : hd.getDanhSachChiTiet()) {
                pnlDongHang.add(taoDongHang(d));
            }
        }

        long giam = hd.tienGiam();
        lblTamTinh.setText(Theme.money(hd.tamTinh()));
        lblGiamGia.setText(giam > 0 ? "-" + Theme.money(giam) : Theme.money(0));
        lblTongCong.setText(Theme.money(hd.canThanhToan()));

        if (hd.getMaGiamGia() == null) {
            btnMaGiamGia.setText("+  Chọn mã giảm giá");
            btnGoMa.setVisible(false);
        } else {
            btnMaGiamGia.setText(hd.getMaGiamGia().getMa() + " • " + hd.getMaGiamGia().moTa() + " (đổi mã)");
            btnGoMa.setVisible(true);
        }

        pnlDongHang.revalidate();
        pnlDongHang.repaint();

        if (cuonXuongCuoi) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = dongScroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        }
    }

    private JComponent taoDongHang(ChiTietHoaDon d) {
        SanPham sp = d.getSanPham();
        RoundedPanel box = new RoundedPanel(10, Theme.FIELD_BG, null);
        box.setLayout(new BorderLayout(10, 0));
        box.setBorder(new EmptyBorder(10, 14, 10, 12));

        JLabel name = new JLabel(sp.getTen());
        name.setFont(Theme.font(Font.BOLD, 13));
        name.setForeground(Theme.TEXT);
        String donViTxt = Theme.money(sp.getDonGia()) + (sp.isDichVu() ? " / lần" : " / sp");
        MaGiamGia mg = hd == null ? null : hd.getMaGiamGia();
        if (mg != null && mg.laMuaXTangY() && mg.khopSanPham(sp) && d.getSoLuong() >= mg.getMuaX()) {
            int slTang = (d.getSoLuong() / mg.getMuaX()) * mg.getTangY();
            String ptTxt = mg.getGiaTri() >= 100 ? "miễn phí" : ("-" + mg.getGiaTri() + "%");
            donViTxt += "  •  Tặng +" + slTang + " (" + ptTxt + ")";
        }
        JLabel unit = new JLabel(donViTxt);
        unit.setFont(Theme.font(Font.PLAIN, 12));
        unit.setForeground(Theme.MUTED);
        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);
        left.add(name, BorderLayout.NORTH);
        left.add(unit, BorderLayout.CENTER);

        JLabel qty = new JLabel(String.valueOf(d.getSoLuong()), SwingConstants.CENTER);
        qty.setFont(Theme.font(Font.PLAIN, 13));
        qty.setForeground(Theme.TEXT);
        qty.setPreferredSize(new Dimension(30, 28));

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        ctrl.setOpaque(false);
        ctrl.add(qtyButton("-", () -> { hd.doiSoLuong(sp.getSku(), -1); renderChiTiet(false); }));
        ctrl.add(qty);
        ctrl.add(qtyButton("+", () -> { hd.doiSoLuong(sp.getSku(), 1); renderChiTiet(false); }));

        JLabel tien = new JLabel(Theme.money(Math.round(d.getDonGia() * d.getSoLuong())), SwingConstants.RIGHT);
        tien.setFont(Theme.font(Font.BOLD, 12));
        tien.setForeground(Theme.TEXT);
        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);
        right.add(ctrl, BorderLayout.NORTH);
        right.add(tien, BorderLayout.CENTER);

        box.add(left, BorderLayout.CENTER);
        box.add(right, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrap.add(box);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrap.getPreferredSize().height));
        return wrap;
    }

    private RoundedButton qtyButton(String text, Runnable action) {
        RoundedButton b = new RoundedButton(text, Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 6);
        b.setFont(Theme.font(Font.PLAIN, 14));
        b.setPreferredSize(new Dimension(28, 28));
        b.addActionListener(e -> action.run());
        return b;
    }

    private static JComponent sumRow(String label, JLabel value, int size, boolean bold) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(bold ? Theme.TEXT : Theme.MUTED);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private static JLabel sectionLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    private void datLoi(String text) {
        boolean trong = text == null || text.isBlank();
        lblLoi.setText(trong ? "" : "<html><body style='width:360px'>" + text + "</body></html>");
    }

    // =========================================================================
    // 5. CÁC HÀNH ĐỘNG NÚT BẤM (LƯU, THOÁT, XÓA, HỦY, THANH TOÁN - YÊU CẦU 5 & 6)
    // =========================================================================

    /** Kiểm tra dữ liệu bắt buộc trước khi lưu hoặc thanh toán */
    private boolean kiemTraHopLe() {
        if (hd == null) return false;
        if (khachHangChon == null && (hd.getMaKH() == null || hd.getMaKH().trim().isEmpty())) {
            datLoi("Vui lòng tìm kiếm và chọn khách hàng liên kết với hóa đơn.");
            tfTimKhach.requestFocusInWindow();
            return false;
        }
        if (hd.trong()) {
            datLoi("Hóa đơn chưa có sản phẩm hoặc dịch vụ nào.");
            return false;
        }
        datLoi(" ");
        return true;
    }

    /** Yêu cầu 5A: Nút Lưu */
    private void xuLyLuu() {
        if (!kiemTraHopLe()) return;
        try {
            bus.luu(hd);
            if (cheDo == CheDo.HOA_DON_CHO) {
                this.banGoc = hd.saoChep(); // Cập nhật lại snapshot sau khi lưu thành công
                JOptionPane.showMessageDialog(this,
                        "Đã lưu các thay đổi cho hóa đơn " + hd.getMaHD() + " vào cơ sở dữ liệu.",
                        "Lưu thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String ma = hd.getMaHD();
                JOptionPane.showMessageDialog(this,
                        "Hóa đơn " + ma + " đã được lưu vào mục Hóa đơn chờ thành công!",
                        "Lưu hóa đơn", JOptionPane.INFORMATION_MESSAGE);
                batDauTaoMoi(); // Chuẩn bị cho hóa đơn tiếp theo
            }
        } catch (Exception ex) {
            datLoi("Lỗi khi lưu: " + ex.getMessage());
        }
    }

    /** Yêu cầu 5B: Nút Thoát (có kiểm tra dirty check và popup xác nhận) */
    private void xuLyThoatHoacTroVe() {
        if (cheDo != CheDo.HOA_DON_CHO) {
            // Đang ở Lập hóa đơn: hỏi hủy nếu có giỏ hàng
            xuLyHuy();
            return;
        }

        if (!coThayDoi()) {
            // Không có thay đổi nào chưa lưu: thoát trực tiếp
            if (onThoat != null) onThoat.run();
            return;
        }

        // Có thay đổi chưa lưu: hiển thị popup xác nhận theo đúng Yêu cầu 5B
        RoundedPanel card = HoaDonPopups.card(560, 320, "Xác nhận thoát",
                "Hóa đơn đang có các thay đổi chưa được lưu vào cơ sở dữ liệu.");
        JLabel msg = new JLabel("<html><b>Bạn có chắc chắn muốn thoát mà không lưu các thay đổi không?</b><br><br>"
                + "• <b>Tiếp tục chỉnh sửa:</b> Quay lại hóa đơn để tiếp tục làm việc.<br>"
                + "• <b>Thoát không lưu:</b> Bỏ qua các thay đổi vừa chỉnh sửa. Hóa đơn đã lưu trước đó trong cơ sở dữ liệu vẫn được giữ nguyên vẹn.</html>");
        msg.setFont(Theme.font(Font.PLAIN, 13));
        msg.setForeground(Theme.TEXT);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 0, 16, 0));
        body.add(msg, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        RoundedButton btnTiepTuc = new RoundedButton("Tiếp tục chỉnh sửa", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        btnTiepTuc.setPreferredSize(new Dimension(170, 40));
        RoundedButton btnThoatKhongLuu = new RoundedButton("Thoát không lưu", Theme.DANGER, Theme.DANGER_HOVER, Color.WHITE, null, 8);
        btnThoatKhongLuu.setPreferredSize(new Dimension(160, 40));

        btnTiepTuc.addActionListener(e -> Overlay.dong(this));
        btnThoatKhongLuu.addActionListener(e -> {
            Overlay.dong(this);
            // Revert về snapshot banGoc để không bị ô nhiễm dữ liệu
            if (banGoc != null) {
                hd = banGoc.saoChep();
            }
            if (onThoat != null) onThoat.run();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(btnTiepTuc);
        btns.add(btnThoatKhongLuu);
        card.add(btns, BorderLayout.SOUTH);

        Overlay.hien(this, card, () -> Overlay.dong(this));
    }

    /** Yêu cầu 5C: Nút Xóa hóa đơn (chức năng riêng biệt) */
    private void xuLyXoaHoaDon() {
        if (hd == null) return;
        String ma = hd.getMaHD();
        String ten = hd.getTenKH();
        HoaDonPopups.xacNhanXoa(this, "Xóa hóa đơn " + ma + "?",
                "Hóa đơn của " + (ten.isEmpty() ? "khách hàng" : ten) + " sẽ bị xóa hoàn toàn khỏi cơ sở dữ liệu.",
                "Hóa đơn bị xóa khỏi hệ thống và không thể khôi phục.", () -> {
                    try {
                        bus.xoaHoaDon(ma);
                    } catch (Exception ex) {
                        datLoi("Lỗi khi xóa: " + ex.getMessage());
                        return;
                    }
                    if (onThoat != null) onThoat.run();
                });
    }

    /** Yêu cầu 5D: Nút Hủy hóa đơn (logic hủy riêng, không gộp với Thoát/Xóa) */
    private void xuLyHuy() {
        if (cheDo == CheDo.HOA_DON_CHO) {
            HoaDonPopups.xacNhan(this, "Hủy các thay đổi?",
                    "Bạn có muốn hủy các thay đổi chưa lưu của hóa đơn " + hd.getMaHD() + " không?",
                    "Mọi chỉnh sửa trong phiên làm việc này sẽ trở về trạng thái lưu gần nhất trong CSDL.",
                    "Không", "Có, hủy thay đổi", () -> {
                        if (banGoc != null) {
                            batDauHoaDonCho(banGoc.saoChep());
                        }
                    });
        } else {
            if (hd.trong() && khachHangChon == null) {
                batDauTaoMoi();
                return;
            }
            HoaDonPopups.xacNhanHuy(this, this::batDauTaoMoi);
        }
    }

    /** Yêu cầu 6: Xử lý thanh toán */
    private void xuLyThanhToan() {
        if (!kiemTraHopLe()) return;
        String ma = hd.getMaHD();
        long tong = hd.canThanhToan();

        HoaDonPopups.thanhToan(this, ma, tong, (phuongThuc, tienKhachDua) -> {
            try {
                bus.thanhToan(hd, phuongThuc);
                if (hd.getMaGiamGia() != null) {
                    mgBus.ghiNhanSuDung(hd.getMaGiamGia().getMa());
                }
            } catch (Exception ex) {
                datLoi("Lỗi khi thanh toán: " + ex.getMessage());
                return;
            }

            hd.setTienKhachDua(tienKhachDua);
            HoaDon hdDaThanhToan = hd.saoChep();

            Window w = SwingUtilities.getWindowAncestor(this);
            DialogXuatHoaDon.hienThi(w, hdDaThanhToan, null);

            if (cheDo == CheDo.HOA_DON_CHO) {
                if (onThoat != null) onThoat.run();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thành công hóa đơn " + ma + " (" + Theme.money(tong) + ", " + phuongThuc + ").\nHóa đơn đã được lưu vào Lịch sử hóa đơn.",
                        "Thanh toán thành công", JOptionPane.INFORMATION_MESSAGE);
                batDauTaoMoi();
            }
        });
    }

    // =========================================================================
    // 6. LISTENERS
    // =========================================================================
    private void dangKyListeners() {
        // Tìm kiếm khách hàng động
        tfTimKhach.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { timKiemVaHienThiGoiY(); }
            @Override public void removeUpdate(DocumentEvent e) { timKiemVaHienThiGoiY(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        // Tìm kiếm sản phẩm động
        tfTimSp.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { taiSanPham(); }
            @Override public void removeUpdate(DocumentEvent e) { taiSanPham(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        // Quét mã vạch SKU Enter
        tfTimSp.addActionListener(e -> {
            SanPham sp = spBus.timTheoSku(tfTimSp.getText());
            if (sp != null) {
                tfTimSp.setText("");
                themSanPham(sp);
            }
        });
    }

    public HoaDon getHoaDonHienTai() {
        return hd;
    }

    /** Thẻ hiển thị sản phẩm / dịch vụ trong lưới */
    private class ProductItem extends JPanel {
        private final SanPham sp;
        private boolean hover;

        ProductItem(SanPham sp) {
            this.sp = sp;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 110));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) {
                        themSanPham(sp);
                    }
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(hover ? new Color(0xF1F5F9) : Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 10, 10);

            g2.setColor(hover ? Theme.PRIMARY : Theme.BORDER);
            g2.setStroke(new BasicStroke(hover ? 1.5f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(cat(sp.getTen(), fm, w - 24), 12, 28);

            g2.setFont(Theme.font(Font.PLAIN, 11));
            g2.setColor(Theme.MUTED);
            g2.drawString(sp.getSku(), 12, 48);

            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.PRIMARY);
            g2.drawString(Theme.money(sp.getDonGia()), 12, 74);

            if (sp.isDichVu()) {
                g2.setFont(Theme.font(Font.BOLD, 10));
                g2.setColor(Theme.ACCENT);
                g2.drawString("Dịch vụ", 12, 92);
            }

            // Nút cộng tròn
            g2.setColor(hover ? Theme.PRIMARY : new Color(0xE2E8F0));
            g2.fillOval(w - 32, h - 32, 22, 22);
            g2.setColor(hover ? Color.WHITE : Theme.TEXT);
            g2.setFont(Theme.font(Font.BOLD, 14));
            g2.drawString("+", w - 25, h - 16);

            g2.dispose();
        }
    }

    private static String cat(String s, FontMetrics fm, int maxW) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= maxW) return s;
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n).trim() + "...") > maxW) n--;
        return s.substring(0, n).trim() + "...";
    }
}
