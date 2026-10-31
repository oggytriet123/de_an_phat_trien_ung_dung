package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.HoaDonBUS;
import Bus.KhachHangBUS;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.DongGioHang;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.SanPham;

/**
 * Trang "Tạo hóa đơn" (trang riêng, không phải popup): thông tin khách, chọn sản phẩm/dịch vụ,
 * chi tiết hóa đơn, mã giảm giá, Lưu / Hủy / Thanh toán.
 * Mọi chỉnh sửa diễn ra trên một bản nháp; chỉ khi bấm Lưu hoặc Thanh toán mới ghi vào {@link HoaDonBUS}.
 */
public class TaoHoaDonPanel extends JPanel {
    private final HoaDonBUS bus;
    private final SanPhamBUS spBus;
    private final MaGiamGiaBUS mgBus;
    private final KhachHangBUS khBus;
    private final Consumer<String> veQuanLy;

    private HoaDon hd;               // bản nháp đang chỉnh
    private boolean taoMoi = true;   // true: mở từ ô trống
    private boolean dangNap;         // đang nạp dữ liệu vào ô nhập (bỏ qua listener)
    private String danhMuc = SanPhamBUS.TAT_CA;

    // tiêu đề
    private final JLabel lblCrumb = new JLabel();
    private final JLabel lblTitle = new JLabel();
    private final JLabel lblSub = new JLabel();
    // khách hàng
    private final PlaceholderField tfTen = new PlaceholderField("Nhập tên khách hàng");
    private final PlaceholderField tfSdt = new PlaceholderField("Nhập số điện thoại");
    private final PlaceholderField tfMaKH = new PlaceholderField("Tự động nhận diện nếu khách hàng đã tồn tại");
    private final FieldBox boxTen = new FieldBox(tfTen, Color.WHITE, 38);
    private final FieldBox boxSdt = new FieldBox(tfSdt, Color.WHITE, 38);
    private final FieldBox boxMa = new FieldBox(tfMaKH, Theme.FIELD_BG, 38);
    // chọn sản phẩm
    private final PlaceholderField tfTim = new PlaceholderField("Tìm tên sản phẩm, dịch vụ hoặc mã SKU...");
    private final List<Pill> pills = new ArrayList<>();
    private final WrapPanel pillRow = new WrapPanel(8, 8, 640);
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
    private final ScrollPanel gridView = new ScrollPanel(new BorderLayout());
    private final JLabel lblKhongCo = new JLabel("Không tìm thấy sản phẩm hoặc dịch vụ phù hợp", SwingConstants.CENTER);
    // chi tiết hóa đơn
    private final JLabel lblMaHD = new JLabel(" ", SwingConstants.RIGHT);
    private final JPanel pnlDong = new JPanel();
    private JScrollPane dongScroll;
    private final JLabel lblTam = new JLabel();
    private final JLabel lblGiam = new JLabel();
    private final JLabel lblTong = new JLabel();
    private final JLabel lblKhach = new JLabel(" ");
    private final JLabel lblLoi = new JLabel("");
    private final RoundedButton btnMa = new RoundedButton("+  Nhập mã giảm giá", Theme.PRIMARY_SOFT,
            new Color(0xDCE8FA), Theme.PRIMARY, Theme.PRIMARY_LINE, 8);
    private final RoundedButton btnXoaHd = new RoundedButton("Xóa hóa đơn này", Color.WHITE, new Color(0xFDECEC),
            Theme.DANGER, new Color(0xF1B5B5), 8);
    private final RoundedButton btnGoMa = new RoundedButton("Gỡ mã", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);

    /**
     * @param veQuanLy quay về trang 20 ô; tham số là thông báo ngắn (null nếu không có)
     */
    public TaoHoaDonPanel(HoaDonBUS bus, SanPhamBUS spBus, MaGiamGiaBUS mgBus, KhachHangBUS khBus,
                          Consumer<String> veQuanLy) {
        super(new BorderLayout(0, 14));
        this.bus = bus;
        this.spBus = spBus;
        this.mgBus = mgBus;
        this.khBus = khBus;
        this.veQuanLy = veQuanLy;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 32, 22, 32));

        tfMaKH.setEditable(false);
        tfMaKH.setFocusable(false);
        GioiHanNhap.ap(tfTen, 50, null);                       // tên tối đa 50 ký tự
        GioiHanNhap.ap(tfSdt, 16, "[0-9 +.()\\-]");             // số điện thoại: chỉ chữ số và + . ( ) - khoảng trắng

        add(buildHeader(), BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.add(buildKhachHang(), BorderLayout.NORTH);
        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setOpaque(false);
        main.add(buildChonSanPham(), BorderLayout.CENTER);
        main.add(buildChiTiet(), BorderLayout.EAST);
        content.add(main, BorderLayout.CENTER);

        // Đủ chỗ thì nội dung giãn đầy cửa sổ; cửa sổ thấp thì cả trang cuộn dọc,
        // không để danh sách dịch vụ trong hóa đơn bị ép mất.
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

        dangKyListener();
    }

    // =====================================================================
    // Bắt đầu một phiên tạo / chỉnh sửa hóa đơn
    // =====================================================================
    /** @param nhap bản nháp từ {@link HoaDonBUS#taoNhap} (ô trống) hoặc {@link HoaDonBUS#moNhap} (ô có hóa đơn). */
    public void batDau(HoaDon nhap) {
        this.hd = nhap;
        this.taoMoi = !bus.daLuu(nhap);
        dangNap = true;
        tfTen.setText(nhap.getTenKH());
        tfSdt.setText(nhap.getSdt());
        tfMaKH.setText(nhap.getMaKH());
        tfTim.setText("");
        dangNap = false;
        danhMuc = SanPhamBUS.TAT_CA;
        boxTen.setError(false);
        boxSdt.setError(false);
        datLoi(" ");
        capNhatTieuDe();
        dungPills();
        btnXoaHd.setVisible(!taoMoi);
        capNhatThongTinKhach();
        taiSanPham();
        renderChiTiet(false);
        SwingUtilities.invokeLater(() -> { if (taoMoi) tfTen.requestFocusInWindow(); });
    }

    public HoaDon getNhap() { return hd; }

    // =====================================================================
    // Tiêu đề + breadcrumb
    // =====================================================================
    private JComponent buildHeader() {
        JLabel back = new JLabel("Quản lý hóa đơn");
        back.setFont(Theme.font(Font.PLAIN, 12));
        back.setForeground(Theme.MUTED);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { back.setForeground(Theme.PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { back.setForeground(Theme.MUTED); }
            @Override public void mouseClicked(MouseEvent e) { huy(); }
        });
        JLabel sep = new JLabel("   /   ");
        sep.setFont(Theme.font(Font.PLAIN, 12));
        sep.setForeground(Theme.MUTED);
        lblCrumb.setFont(Theme.font(Font.PLAIN, 12));
        lblCrumb.setForeground(Theme.MUTED);
        JPanel crumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        crumb.setOpaque(false);
        crumb.add(back);
        crumb.add(sep);
        crumb.add(lblCrumb);

        lblTitle.setFont(Theme.font(Font.BOLD, 26));
        lblTitle.setForeground(Theme.TEXT);
        lblSub.setFont(Theme.font(Font.PLAIN, 13));
        lblSub.setForeground(Theme.MUTED);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[] {crumb, lblTitle, lblSub}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(crumb);
        head.add(Box.createVerticalStrut(8));
        head.add(lblTitle);
        head.add(Box.createVerticalStrut(4));
        head.add(lblSub);
        return head;
    }

    private void capNhatTieuDe() {
        if (hd == null) return;
        lblCrumb.setText(taoMoi ? "Tạo hóa đơn mới" : "Chi tiết " + hd.getMaHD());
        lblTitle.setText(taoMoi ? "Tạo hóa đơn" : "Chi tiết hóa đơn");
        lblSub.setText(taoMoi
                ? "Nhập thông tin khách hàng, chọn sản phẩm hoặc dịch vụ rồi lưu hóa đơn."
                : "Tiếp tục chỉnh sửa hoặc thanh toán hóa đơn đang chờ xử lý.");
        lblMaHD.setText(hd.getMaHD() + "  •  Ô " + String.format("%02d", hd.getSoO()));
    }

    // =====================================================================
    // Thông tin khách hàng
    // =====================================================================
    private JComponent buildKhachHang() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 20, 18, 20));
        card.add(sectionLabel("THÔNG TIN KHÁCH HÀNG", 12, Theme.MUTED), BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 3, 26, 0));
        row.setOpaque(false);
        row.add(labeled("Tên khách hàng *", boxTen));
        row.add(labeled("Số điện thoại *", boxSdt));
        row.add(labeled("Mã khách hàng", boxMa));
        card.add(row, BorderLayout.CENTER);

        lblKhach.setFont(Theme.font(Font.PLAIN, 12));
        lblKhach.setForeground(Theme.MUTED);
        card.add(lblKhach, BorderLayout.SOUTH);
        return card;
    }

    /** Cho biết khách đã có (hạng, điểm, tổng chi tiêu) hoặc khách mới sẽ được tạo mã khi lưu. */
    private void capNhatThongTinKhach() {
        String sdt = tfSdt.getText();
        KhachHang k = khBus.timTheoSdtAnToan(KhachHangBUS.chuanHoaSdt(sdt));
        if (k != null) {
            String diem = String.format("%,d", k.getDiemTichLuy()).replace(',', '.');
            lblKhach.setText("Khách hàng đã có  •  hạng " + k.getHangThe() + "  •  " + diem + " điểm  •  tổng chi tiêu "
                    + Theme.money(k.getTongChiTieu()));
            lblKhach.setForeground(Theme.SUCCESS);
        } else if (KhachHangBUS.loiSdt(sdt) == null) {
            lblKhach.setText("Khách hàng mới  •  hệ thống sẽ tạo mã khách hàng khi lưu hóa đơn.");
            lblKhach.setForeground(Theme.PRIMARY);
        } else {
            lblKhach.setText("Nhập số điện thoại để tự nhận diện khách hàng đã có.");
            lblKhach.setForeground(Theme.MUTED);
        }
    }

    private static JComponent labeled(String text, JComponent field) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.TEXT);
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void dangKyListener() {
        tfTen.getDocument().addDocumentListener(onChange(() -> {
            if (dangNap) return;
            boxTen.setError(false);
            datLoi(" ");
        }));
        tfSdt.getDocument().addDocumentListener(onChange(() -> {
            if (dangNap) return;
            boxSdt.setError(false);
            datLoi(" ");
            // Số điện thoại trùng khách cũ: tự nhận diện mã khách và điền đúng tên đã đăng ký
            KhachHang k = khBus.timTheoSdtAnToan(KhachHangBUS.chuanHoaSdt(tfSdt.getText()));
            tfMaKH.setText(k == null ? "" : k.getMaKH());
            if (k != null && !k.getHoTen().equals(tfTen.getText())) tfTen.setText(k.getHoTen());
            capNhatThongTinKhach();
        }));
        tfTim.getDocument().addDocumentListener(onChange(this::taiSanPham));
        // Máy quét mã vạch gõ mã rồi Enter: trùng SKU thì thêm thẳng vào hóa đơn
        tfTim.addActionListener(e -> {
            SanPham sp = spBus.timTheoSku(tfTim.getText());
            if (sp != null) {
                tfTim.setText("");
                them(sp);
            }
        });
    }

    private static DocumentListener onChange(Runnable r) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { r.run(); }
            @Override public void removeUpdate(DocumentEvent e) { r.run(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        };
    }

    // =====================================================================
    // Chọn sản phẩm / dịch vụ
    // =====================================================================
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

        FieldBox tim = new FieldBox(tfTim, Theme.FIELD_BG, 40);
        RoundedButton quet = new RoundedButton("Quét mã", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        quet.setPreferredSize(new Dimension(126, 40));
        quet.addActionListener(e -> HoaDonPopups.quetMa(this, spBus, this::them));
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

        grid.setOpaque(false);
        gridView.setOpaque(false);
        lblKhongCo.setFont(Theme.font(Font.PLAIN, 13));
        lblKhongCo.setForeground(Theme.MUTED);
        lblKhongCo.setBorder(new EmptyBorder(40, 0, 0, 0));
        JScrollPane sp = new JScrollPane(gridView, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
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

    /** Dựng lại các nút lọc theo loại hàng hiện có (gọi lại khi vừa tạo loại mới). */
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
        List<SanPham> ds = spBus.loc(danhMuc, tfTim.getText());
        grid.removeAll();
        for (SanPham sp : ds) grid.add(new ProductItem(sp));
        gridView.removeAll();
        gridView.add(ds.isEmpty() ? lblKhongCo : grid, BorderLayout.NORTH);
        gridView.revalidate();
        gridView.repaint();
    }

    private void them(SanPham sp) {
        if (hd == null) return;
        hd.them(sp);
        datLoi(" ");
        renderChiTiet(true);
    }

    // =====================================================================
    // Chi tiết hóa đơn + tính tiền + nút
    // =====================================================================
    private JComponent buildChiTiet() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(450, super.getPreferredSize().height);
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
        btnXoaHd.setPreferredSize(new Dimension(124, 28));
        btnXoaHd.setVisible(false);                     // chỉ hiện khi đang mở một hóa đơn đã lưu
        btnXoaHd.addActionListener(e -> xoaHoaDon());
        JPanel dauPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dauPhai.setOpaque(false);
        dauPhai.add(lblMaHD);
        dauPhai.add(btnXoaHd);
        dauPhai.setBorder(new EmptyBorder(0, 0, 0, -10));
        head.add(dauPhai, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        pnlDong.setOpaque(false);
        pnlDong.setLayout(new BoxLayout(pnlDong, BoxLayout.Y_AXIS));
        ScrollPanel view = new ScrollPanel(new BorderLayout());
        view.setOpaque(false);
        view.setBorder(new EmptyBorder(0, 20, 0, 14));
        view.add(pnlDong, BorderLayout.NORTH);
        dongScroll = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dongScroll.setBorder(BorderFactory.createEmptyBorder());
        dongScroll.setViewportBorder(null);
        dongScroll.setOpaque(false);
        dongScroll.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(dongScroll);
        dongScroll.setPreferredSize(new Dimension(0, 130));
        card.add(dongScroll, BorderLayout.CENTER);

        // ---- chân: tổng tiền + nút
        JPanel foot = new JPanel();
        foot.setOpaque(false);
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        foot.setBorder(new EmptyBorder(6, 20, 16, 20));

        lblTam.setFont(Theme.font(Font.PLAIN, 13));
        lblGiam.setFont(Theme.font(Font.PLAIN, 13));
        lblTam.setForeground(Theme.TEXT);
        lblGiam.setForeground(Theme.TEXT);
        lblTong.setFont(Theme.font(Font.BOLD, 26));
        lblTong.setForeground(Theme.TEXT);

        foot.add(sumRow("Tạm tính", lblTam, 13, false));
        foot.add(Box.createVerticalStrut(6));
        foot.add(sumRow("Giảm giá", lblGiam, 13, false));
        foot.add(Box.createVerticalStrut(8));

        btnMa.setPreferredSize(new Dimension(0, 40));
        btnMa.addActionListener(e -> HoaDonPopups.maGiamGia(this, hd.getMaGiamGia(), mgBus, m -> {
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
        maRow.add(btnMa, BorderLayout.CENTER);
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
        foot.add(sumRow("TỔNG CỘNG", lblTong, 13, true));
        foot.add(Box.createVerticalStrut(10));

        JLabel hint = new JLabel("Hóa đơn sẽ được giữ lại trong ô sau khi lưu.");
        hint.setFont(Theme.font(Font.PLAIN, 12));
        hint.setForeground(Theme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(hint);
        foot.add(Box.createVerticalStrut(6));
        lblLoi.setFont(Theme.font(Font.PLAIN, 12));
        lblLoi.setForeground(Theme.DANGER);
        lblLoi.setVerticalAlignment(SwingConstants.TOP);
        lblLoi.setBorder(new EmptyBorder(0, 0, 0, 16));
        lblLoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(lblLoi);
        foot.add(Box.createVerticalStrut(8));

        RoundedButton luu = new RoundedButton("Lưu", Theme.SUCCESS, Theme.SUCCESS_HOVER, Color.WHITE, null, 8);
        RoundedButton huy = new RoundedButton("Hủy", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        RoundedButton thanhToan = new RoundedButton("Thanh toán", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        luu.addActionListener(e -> {
            if (luu()) veQuanLy.accept(null);
        });
        huy.addActionListener(e -> huy());
        thanhToan.addActionListener(e -> thanhToan());
        JPanel btns = new JPanel(new GridLayout(1, 3, 12, 0));
        btns.setOpaque(false);
        btns.add(luu);
        btns.add(huy);
        btns.add(thanhToan);
        btns.setPreferredSize(new Dimension(0, 44));
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(btns);

        card.add(foot, BorderLayout.SOUTH);
        return card;
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

    private void renderChiTiet(boolean cuonXuongCuoi) {
        if (hd == null) return;
        pnlDong.removeAll();
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
            pnlDong.add(empty);
        } else {
            for (DongGioHang d : hd.getDong()) pnlDong.add(dongHang(d));
        }

        long giam = hd.tienGiam();
        lblTam.setText(Theme.money(hd.tamTinh()));
        lblGiam.setText(giam > 0 ? "-" + Theme.money(giam) : Theme.money(0));
        lblTong.setText(Theme.money(hd.canThanhToan()));
        if (hd.getMaGiamGia() == null) {
            btnMa.setText("+  Nhập mã giảm giá");
            btnGoMa.setVisible(false);
        } else {
            btnMa.setText(hd.getMaGiamGia().getMa() + " • " + hd.getMaGiamGia().moTa() + "  (đổi mã)");
            btnGoMa.setVisible(true);
        }
        pnlDong.revalidate();
        pnlDong.repaint();
        if (cuonXuongCuoi) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = dongScroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        }
    }

    private JComponent dongHang(DongGioHang d) {
        SanPham sp = d.getSanPham();
        RoundedPanel box = new RoundedPanel(10, Theme.FIELD_BG, null);
        box.setLayout(new BorderLayout(10, 0));
        box.setBorder(new EmptyBorder(10, 14, 10, 12));

        JLabel name = new JLabel(sp.getTen());
        name.setFont(Theme.font(Font.BOLD, 13));
        name.setForeground(Theme.TEXT);
        JLabel unit = new JLabel(Theme.money(sp.getDonGia()) + (sp.isDichVu() ? " / lần" : " / sp"));
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

        JLabel tien = new JLabel(Theme.money(d.thanhTien()), SwingConstants.RIGHT);
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

    // =====================================================================
    // Lưu / Hủy / Thanh toán
    // =====================================================================
    /** Hiện / xóa dòng lỗi; lỗi dài tự xuống dòng thay vì bị cắt chữ. */
    private void datLoi(String text) {
        boolean trong = text == null || text.isBlank();
        lblLoi.setText(trong ? "" : "<html><body style='width:340px'>" + text + "</body></html>");
    }

    /** Đưa nội dung ô nhập vào bản nháp. */
    private void dongBoNhap() {
        hd.setTenKH(KhachHangBUS.chuanHoaTen(tfTen.getText()));
        hd.setSdt(KhachHangBUS.dinhDangSdt(KhachHangBUS.chuanHoaSdt(tfSdt.getText())));
        hd.setMaKH(tfMaKH.getText().trim());
    }

    /** Kiểm tra dữ liệu bắt buộc; tô viền đỏ ô lỗi và hiện dòng lỗi (không dùng popup). */
    private boolean kiemTra() {
        String eTen = KhachHangBUS.loiTen(hd.getTenKH());
        String eSdt = KhachHangBUS.loiSdt(hd.getSdt());
        String eKhach = eTen == null && eSdt == null ? bus.loiTrungSdt(hd.getTenKH(), hd.getSdt()) : null;
        String eGio = HoaDonBUS.loiGioHang(hd);
        boxTen.setError(eTen != null || eKhach != null);
        boxSdt.setError(eSdt != null || eKhach != null);
        String dau = eTen != null ? eTen : eSdt != null ? eSdt : eKhach != null ? eKhach : eGio;
        datLoi(dau);
        if (eTen != null) tfTen.requestFocusInWindow();
        else if (eSdt != null || eKhach != null) tfSdt.requestFocusInWindow();
        return dau == null;
    }

    /** Lưu: không hiện popup hay thông báo thành công. Trở về true nếu đã lưu (khi đó trang quay về 20 ô). */
    private boolean luu() {
        dongBoNhap();
        if (!kiemTra()) return false;
        try {
            bus.luu(hd);
        } catch (RuntimeException ex) {
            datLoi(ex.getMessage());
            return false;
        }
        return true;
    }

    /** Yêu cầu rời trang (hỏi xác nhận như nút Hủy) - dùng cho menu điều hướng. */
    public void yeuCauHuy() {
        huy();
    }

    /** Xóa hóa đơn đã lưu: hỏi xác nhận, xóa khỏi ô rồi quay về trang 20 ô. */
    private void xoaHoaDon() {
        if (hd == null || !bus.daLuu(hd)) return;
        String ma = hd.getMaHD();
        int viTri = hd.getSoO() - 1;
        HoaDonPopups.xacNhanXoa(this, "Xóa hóa đơn " + ma + "?",
                "Hóa đơn đang nằm ở ô " + String.format("%02d", hd.getSoO()) + " sẽ bị xóa khỏi danh sách.",
                "Hóa đơn bị xóa khỏi ô và không thể khôi phục.", () -> {
                    try {
                        bus.xoa(viTri);
                    } catch (RuntimeException ex) {
                        datLoi(ex.getMessage());
                        return;
                    }
                    veQuanLy.accept("Đã xóa hóa đơn " + ma + " - Ô " + String.format("%02d", viTri + 1) + " đã trống");
                });
    }

    private void huy() {
        HoaDonPopups.xacNhanHuy(this, () -> veQuanLy.accept(null));
    }

    private void thanhToan() {
        dongBoNhap();
        if (!kiemTra()) return;
        String ma = hd.getMaHD();
        long tong = hd.canThanhToan();
        HoaDonPopups.thanhToan(this, ma, tong, phuongThuc -> {
            try {
                bus.thanhToan(hd, phuongThuc);
            } catch (RuntimeException ex) {
                datLoi(ex.getMessage());
                return;
            }
            veQuanLy.accept("Đã thanh toán " + ma + " (" + Theme.money(tong) + ", " + phuongThuc
                    + ") - xem tại Lịch sử hóa đơn");
        });
    }

    // =====================================================================
    // Thành phần giao diện nhỏ
    // =====================================================================
    private static JLabel sectionLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    private static String cat(String s, FontMetrics fm, int maxW) {
        if (fm.stringWidth(s) <= maxW) return s;
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n).trim() + ".") > maxW) n--;
        return s.substring(0, n).trim() + ".";
    }

    /** Thẻ sản phẩm / dịch vụ: biểu tượng, tên, mã, giá và nút "+". */
    private class ProductItem extends JPanel {
        private final SanPham sp;
        private boolean hover;

        ProductItem(SanPham sp) {
            this.sp = sp;
            setOpaque(false);
            setPreferredSize(new Dimension(180, 124));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(sp.getTen());
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) them(ProductItem.this.sp);
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 12;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 10, 10);
            g2.setColor(hover ? Theme.PRIMARY : Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Ô ảnh tạm + ngôi sao 4 cánh
            g2.setColor(new Color(0xE9EEF2));
            g2.fillRoundRect(pad, pad, 50, 50, 8, 8);
            double cx = pad + 25, cy = pad + 25, R = 9, r = 3.2;
            veSao(g2, cx, cy, R, r, new Color(0x5B7B8C));

            int tx = pad + 50 + 12, tw = w - tx - pad;
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(cat(sp.getTen(), fm, tw), tx, pad + 22);
            g2.setFont(Theme.font(Font.PLAIN, 11));
            g2.setColor(Theme.MUTED);
            g2.drawString((sp.isDichVu() ? "DV: " : "SKU: ") + sp.getSku(), tx, pad + 42);

            g2.setFont(Theme.font(Font.BOLD, 14));
            g2.setColor(Theme.TEXT);
            g2.drawString(Theme.money(sp.getDonGia()), pad, h - pad - 8);

            int d = 34, bx = w - pad - d, by = h - pad - d;
            g2.setColor(hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY);
            g2.fillRoundRect(bx, by, d, d, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(bx + 10, by + d / 2, bx + d - 10, by + d / 2);
            g2.drawLine(bx + d / 2, by + 10, bx + d / 2, by + d - 10);
            g2.dispose();
        }

        private void veSao(Graphics2D g2, double cx, double cy, double R, double r, Color c) {
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
            for (int i = 0; i < 8; i++) {
                double ang = Math.PI / 4 * i - Math.PI / 2;
                double rad = i % 2 == 0 ? R : r;
                double x = cx + Math.cos(ang) * rad, y = cy + Math.sin(ang) * rad;
                if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
            }
            p.closePath();
            g2.setColor(c);
            g2.fill(p);
        }
    }
}
