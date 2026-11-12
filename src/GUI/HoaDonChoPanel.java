package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.HoaDonBUS;
import Bus.KhachHangBUS;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.HoaDon;

/**
 * Màn hình "Hóa đơn chờ" (Mục mới trong menu Xử Lý).
 * Thay thế giới hạn cứng 20 ô cũ bằng danh sách cuộn dọc không giới hạn các hóa đơn chưa thanh toán.
 *
 * Chức năng:
 * - Hiển thị danh sách các hóa đơn đã lập nhưng chưa thanh toán.
 * - Mỗi hóa đơn hiển thị thành một ô riêng biệt có Tên khách hàng, Số điện thoại, Mã HD, Tổng tiền, Thời gian chờ.
 * - Kéo dài xuống dưới, cuộn dọc khi số lượng hóa đơn tăng lên.
 * - Khi nhấn vào một ô, mở lại đúng nội dung hóa đơn đã lưu.
 * - Kế thừa toàn bộ logic, nút bấm (Lưu, Thoát, Xóa, Hủy, Thanh toán) của chức năng lập hóa đơn.
 */
public class HoaDonChoPanel extends JPanel implements Refreshable {
    private static final String VIEW_LIST = "DANH_SACH";
    private static final String VIEW_DETAIL = "CHI_TIET";
    private static final DateTimeFormatter FORMAT_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CardLayout cards = new CardLayout();
    private final HoaDonBUS bus;
    private final TaoHoaDonPanel panelChiTiet;

    // View Danh sách
    private final JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 16));
    private final WrapPanel pnlGridHoaDon = new WrapPanel(16, 16, 800);
    private final PlaceholderField tfTimKiem = new PlaceholderField("Tìm theo mã hóa đơn, tên khách hàng hoặc số điện thoại...");
    private final JLabel lblTongSo = new JLabel("0 hóa đơn chờ");
    private final JLabel lblKhongCo = new JLabel("Hiện không có hóa đơn chờ nào trong hệ thống.", SwingConstants.CENTER);

    private String boLoc = "TẤT CẢ";
    private final List<RoundedButton> filterChips = new ArrayList<>();
    private Timer timerTuDongCapNhat;

    public HoaDonChoPanel(HoaDonBUS bus, SanPhamBUS spBus, MaGiamGiaBUS mgBus, KhachHangBUS khBus) {
        super(null);
        setLayout(cards);
        this.bus = bus;

        // 1. Khởi tạo màn hình chi tiết (tái sử dụng TaoHoaDonPanel ở chế độ HOA_DON_CHO)
        this.panelChiTiet = new TaoHoaDonPanel(bus, spBus, mgBus, khBus, this::veDanhSach);

        // 2. Xây dựng màn hình danh sách cuộn dọc
        initViewDanhSach();

        add(pnlDanhSach, VIEW_LIST);
        add(panelChiTiet, VIEW_DETAIL);

        cards.show(this, VIEW_LIST);

        // Timer cập nhật số phút chờ định kỳ mỗi 15 giây
        timerTuDongCapNhat = new Timer(15000, e -> {
            if (isShowing()) {
                pnlGridHoaDon.repaint();
            }
        });
        timerTuDongCapNhat.start();
    }

    private void initViewDanhSach() {
        pnlDanhSach.setBackground(Theme.BG);
        pnlDanhSach.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Header
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Hóa đơn chờ");
        title.setFont(Theme.font(Font.BOLD, 26));
        title.setForeground(Theme.TEXT);

        JLabel sub = new JLabel("Danh sách các hóa đơn đã lập nhưng chưa thanh toán. Nhấn vào một hóa đơn để tiếp tục xử lý.");
        sub.setFont(Theme.font(Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED);

        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JPanel pnlLeftTitle = new JPanel();
        pnlLeftTitle.setOpaque(false);
        pnlLeftTitle.setLayout(new BoxLayout(pnlLeftTitle, BoxLayout.Y_AXIS));
        pnlLeftTitle.add(title);
        pnlLeftTitle.add(Box.createVerticalStrut(4));
        pnlLeftTitle.add(sub);

        RoundedButton btnLamMoi = new RoundedButton("Làm mới", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8).pad(14, 8);
        btnLamMoi.addActionListener(e -> lamMoi());

        pnlTitle.add(pnlLeftTitle, BorderLayout.WEST);
        pnlTitle.add(btnLamMoi, BorderLayout.EAST);
        top.add(pnlTitle);
        top.add(Box.createVerticalStrut(14));

        // Thanh công cụ: Tìm kiếm + Lọc chips
        JPanel pnlToolbar = new JPanel(new BorderLayout(14, 0));
        pnlToolbar.setOpaque(false);

        FieldBox searchBox = new FieldBox(tfTimKiem, Theme.FIELD_BG, 38);
        pnlToolbar.add(searchBox, BorderLayout.CENTER);

        JPanel pnlChips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlChips.setOpaque(false);
        pnlChips.add(taoFilterChip("TẤT CẢ"));
        pnlChips.add(taoFilterChip("QUÁ HẠN >15P"));
        pnlChips.add(taoFilterChip("PRE-ORDER"));
        pnlToolbar.add(pnlChips, BorderLayout.EAST);

        top.add(pnlToolbar);
        top.add(Box.createVerticalStrut(10));

        lblTongSo.setFont(Theme.font(Font.BOLD, 12));
        lblTongSo.setForeground(Theme.PRIMARY);
        top.add(lblTongSo);

        pnlDanhSach.add(top, BorderLayout.NORTH);

        // Body: Danh sách hóa đơn dạng ô cuộn dọc không giới hạn
        pnlGridHoaDon.setOpaque(false);

        ScrollPanel viewScroll = new ScrollPanel(new BorderLayout()) {
            @Override public boolean getScrollableTracksViewportHeight() {
                return getParent() instanceof JViewport vp && vp.getHeight() >= getPreferredSize().height;
            }
        };
        viewScroll.setOpaque(false);
        viewScroll.add(pnlGridHoaDon, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(viewScroll, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(scrollPane);

        pnlDanhSach.add(scrollPane, BorderLayout.CENTER);

        // Listener tìm kiếm
        tfTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { capNhatDanhSach(); }
            @Override public void removeUpdate(DocumentEvent e) { capNhatDanhSach(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });
    }

    private RoundedButton taoFilterChip(String label) {
        boolean active = label.equals(boLoc);
        RoundedButton b = active
                ? new RoundedButton(label, Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8)
                : new RoundedButton(label, Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        b.setFont(Theme.font(Font.PLAIN, 12));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 12, 32));
        b.addActionListener(e -> {
            boLoc = label;
            for (RoundedButton chip : filterChips) {
                boolean on = chip.getText().equals(boLoc);
                if (on) chip.style(Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null);
                else chip.style(Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER);
            }
            capNhatDanhSach();
        });
        filterChips.add(b);
        return b;
    }

    /** Mở một hóa đơn chờ để xử lý tiếp */
    private void moHoaDon(HoaDon hd) {
        if (hd == null) return;
        panelChiTiet.batDauHoaDonCho(hd);
        cards.show(this, VIEW_DETAIL);
    }

    /** Quay về màn hình danh sách hóa đơn chờ */
    private void veDanhSach() {
        lamMoi();
        cards.show(this, VIEW_LIST);
    }

    /** Tải lại toàn bộ dữ liệu từ CSDL và vẽ lại các ô */
    @Override
    public void lamMoi() {
        bus.taiTuCSDL();
        capNhatDanhSach();
    }

    private void capNhatDanhSach() {
        pnlGridHoaDon.removeAll();

        List<HoaDon> tatCaCho = bus.layDanhSachHoaDonCho();
        String tuKhoa = tfTimKiem.getText().trim().toLowerCase();
        List<HoaDon> loc = new ArrayList<>();

        for (HoaDon h : tatCaCho) {
            // Lọc theo chip
            if ("⚠️ QUÁ HẠN >15P".equals(boLoc) && !h.isQuaHan15Phut()) continue;
            if ("📦 PRE-ORDER".equals(boLoc) && !h.isPreOrder()) continue;

            // Lọc theo từ khóa
            if (!tuKhoa.isEmpty()) {
                String ma = h.getMaHD() != null ? h.getMaHD().toLowerCase() : "";
                String ten = h.getTenKH() != null ? h.getTenKH().toLowerCase() : "";
                String sdt = h.getSdt() != null ? h.getSdt().toLowerCase() : "";
                if (!ma.contains(tuKhoa) && !ten.contains(tuKhoa) && !sdt.contains(tuKhoa)) {
                    continue;
                }
            }
            loc.add(h);
        }

        lblTongSo.setText("Hiển thị " + loc.size() + " / " + tatCaCho.size() + " hóa đơn chờ");

        if (loc.isEmpty()) {
            lblKhongCo.setFont(Theme.font(Font.PLAIN, 14));
            lblKhongCo.setForeground(Theme.MUTED);
            lblKhongCo.setBorder(new EmptyBorder(60, 0, 0, 0));
            pnlGridHoaDon.add(lblKhongCo);
        } else {
            for (HoaDon h : loc) {
                pnlGridHoaDon.add(new OHoaDonCho(h));
            }
        }

        pnlGridHoaDon.revalidate();
        pnlGridHoaDon.repaint();
    }

    /**
     * Thành phần ô riêng biệt hiển thị thông tin hóa đơn chờ.
     * Bắt buộc hiển thị tên khách hàng và số điện thoại theo Yêu cầu 4.
     */
    private class OHoaDonCho extends JPanel {
        private final HoaDon hd;
        private boolean hover;

        OHoaDonCho(HoaDon hd) {
            this.hd = hd;
            setOpaque(false);
            setPreferredSize(new Dimension(280, 150));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) {
                        moHoaDon(hd);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int pad = 14;

            boolean quaHan = hd.isQuaHan15Phut();
            boolean preOrder = hd.isPreOrder();
            long phutCho = hd.getSoPhutCho();

            Color nen = hover ? new Color(0xF8FAFC) : (quaHan ? Theme.RED_BG : (preOrder ? Theme.BLUE_BG : Color.WHITE));
            Color vien = hover ? Theme.PRIMARY : (quaHan ? Theme.RED : (preOrder ? Theme.ACCENT : Theme.BORDER));

            // Nền thẻ
            g2.setColor(nen);
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            // Viền
            g2.setColor(vien);
            g2.setStroke(new BasicStroke(hover || quaHan ? 1.8f : 1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            // 1. Mã hóa đơn
            g2.setFont(Theme.font(Font.BOLD, 15));
            g2.setColor(quaHan ? Theme.DANGER : Theme.TEXT);
            g2.drawString(hd.getMaHD(), pad, 26);

            // Badge thời gian chờ ở góc trên phải
            String strWait = (phutCho >= 15 ? "⚠️ " : "⏱ ") + phutCho + "p";
            g2.setFont(Theme.font(Font.BOLD, 10));
            FontMetrics fmWait = g2.getFontMetrics();
            int ww = fmWait.stringWidth(strWait) + 12;
            int wx = w - pad - ww, wy = 12;
            g2.setColor(quaHan ? Theme.RED : (phutCho > 8 ? Theme.ORANGE : Theme.GREEN));
            g2.fillRoundRect(wx, wy, ww, 18, 9, 9);
            g2.setColor(Color.WHITE);
            g2.drawString(strWait, wx + 6, wy + 13);

            // 2. Tên khách hàng (Bắt buộc theo Yêu cầu 4)
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            String tenKhach = hd.getTenKH() != null && !hd.getTenKH().isBlank() ? hd.getTenKH() : "(Khách lẻ)";
            g2.drawString(cat(tenKhach, fm, w - pad * 2), pad, 50);

            // 3. Số điện thoại khách hàng (Bắt buộc theo Yêu cầu 4)
            g2.setFont(Theme.font(Font.PLAIN, 12));
            g2.setColor(Theme.MUTED);
            String sdtKhach = hd.getSdt() != null && !hd.getSdt().isBlank() ? hd.getSdt() : "Chưa có SĐT";
            g2.drawString("SĐT: " + sdtKhach, pad, 70);

            // Đường phân cách mờ
            g2.setColor(new Color(0xE2E8F0));
            g2.drawLine(pad, 82, w - pad, 82);

            // 4. Số lượng món và Tổng tiền
            g2.setFont(Theme.font(Font.PLAIN, 12));
            g2.setColor(Theme.MUTED);
            int soMon = hd.getDanhSachChiTiet().size();
            g2.drawString(soMon + " mục", pad, 104);

            g2.setFont(Theme.font(Font.BOLD, 16));
            g2.setColor(Theme.PRIMARY);
            String strTien = Theme.money(hd.canThanhToan());
            int wTien = g2.getFontMetrics().stringWidth(strTien);
            g2.drawString(strTien, w - pad - wTien, 104);

            // 5. Badges góc dưới: Trạng thái & Pre-order
            int by = h - 26;
            g2.setFont(Theme.font(Font.BOLD, 10));
            FontMetrics fmBadge = g2.getFontMetrics();

            String txtStatus = hd.getTrangThai();
            int bw = fmBadge.stringWidth(txtStatus) + 14;
            g2.setColor(Theme.AMBER_PILL);
            g2.fillRoundRect(pad, by, bw, 18, 9, 9);
            g2.setColor(Theme.AMBER_TEXT);
            g2.drawString(txtStatus, pad + 7, by + 13);

            if (preOrder) {
                int pw = fmBadge.stringWidth("Pre-order") + 14;
                int px = pad + bw + 6;
                g2.setColor(Theme.BLUE_BG);
                g2.fillRoundRect(px, by, pw, 18, 9, 9);
                g2.setColor(Theme.BLUE_TEXT);
                g2.drawString("Pre-order", px + 7, by + 13);
            }

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
