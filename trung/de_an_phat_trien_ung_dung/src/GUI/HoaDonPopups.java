package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.HoaDonBUS;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.DongGioHang;
import Entity.HoaDon;
import Entity.MaGiamGia;
import Entity.SanPham;

/** Các popup của nhóm màn hình hóa đơn. Mỗi popup phủ lên trang hiện tại (xem {@link Overlay}). */
final class HoaDonPopups {
    private HoaDonPopups() {}

    private static final int PAD = 36;
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =====================================================================
    // Nhập mã giảm giá
    // =====================================================================
    static void maGiamGia(Component host, MaGiamGia hienTai, MaGiamGiaBUS bus, Consumer<MaGiamGia> onApply) {
        RoundedPanel card = card(580, 370, "Nhập mã giảm giá", "Nhập mã khuyến mãi để áp dụng cho hóa đơn.");

        PlaceholderField tf = new PlaceholderField("Nhập mã giảm giá");
        if (hienTai != null) tf.setText(hienTai.getMa());
        FieldBox box = new FieldBox(tf, Color.WHITE, 42);
        JLabel err = errorLabel();
        tf.getDocument().addDocumentListener(clearError(err, box));

        card.add(body(label("Mã giảm giá"), box, err), BorderLayout.CENTER);

        RoundedButton boQua = secondary("Bỏ qua", 150);
        RoundedButton apDung = primary("Áp dụng", 172);
        Runnable dong = () -> Overlay.dong(host);
        Runnable ap = () -> {
            try {
                MaGiamGia m = bus.kiemTra(tf.getText());
                Overlay.dong(host);
                onApply.accept(m);
            } catch (IllegalArgumentException ex) {
                err.setText(ex.getMessage());
                box.setError(true);
                tf.requestFocusInWindow();
            }
        };
        boQua.addActionListener(e -> dong.run());
        apDung.addActionListener(e -> ap.run());
        tf.addActionListener(e -> ap.run());
        card.add(buttons(boQua, apDung), BorderLayout.SOUTH);

        Overlay.hien(host, card, dong);
        SwingUtilities.invokeLater(tf::requestFocusInWindow);
    }

    // =====================================================================
    // Thanh toán
    // =====================================================================
    static void thanhToan(Component host, String maHD, long tong, Consumer<String> onConfirm) {
        RoundedPanel card = card(580, 450, "Thanh toán hóa đơn", "Chọn phương thức thanh toán cho hóa đơn " + maHD + ".");

        JLabel tl = new JLabel("Tổng thanh toán");
        tl.setFont(Theme.font(Font.PLAIN, 12));
        tl.setForeground(Theme.MUTED);
        JLabel amount = new JLabel(Theme.money(tong));
        amount.setFont(Theme.font(Font.BOLD, 26));
        amount.setForeground(Theme.TEXT);

        String[] huongDan = {
            "Xác nhận sau khi đã nhận đủ tiền mặt từ khách.",
            "Xác nhận sau khi giao dịch quẹt thẻ thành công.",
            "Chỉ xác nhận sau khi nhận được thông báo thanh toán MoMo thành công.",
            "Chỉ xác nhận sau khi nhận được thông báo thanh toán ZaloPay thành công.",
        };
        JLabel hint = new JLabel(huongDan[0]);
        hint.setFont(Theme.font(Font.PLAIN, 12));
        hint.setForeground(Theme.MUTED);

        String[] chon = {HoaDonBUS.PHUONG_THUC[0]};
        List<MethodCard> cards = new ArrayList<>();
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);
        for (int i = 0; i < HoaDonBUS.PHUONG_THUC.length; i++) {
            MethodCard mc = new MethodCard(HoaDonBUS.PHUONG_THUC[i], i == 0);
            int idx = i;
            mc.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e) || !mc.contains(e.getPoint())) return;
                    for (MethodCard c : cards) c.setChon(c == mc);
                    chon[0] = mc.ten;
                    hint.setText(huongDan[idx]);
                }
            });
            cards.add(mc);
            grid.add(mc);
        }
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 0, 0, 0));
        for (JComponent c : new JComponent[] {tl, amount, grid, hint}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(tl);
        body.add(Box.createVerticalStrut(4));
        body.add(amount);
        body.add(Box.createVerticalStrut(14));
        body.add(grid);
        body.add(Box.createVerticalStrut(10));
        body.add(hint);
        card.add(body, BorderLayout.CENTER);

        RoundedButton dong = secondary("Đóng", 150);
        RoundedButton xacNhan = primary("Xác nhận thanh toán", 210);
        Runnable dongPopup = () -> Overlay.dong(host);
        dong.addActionListener(e -> dongPopup.run());
        xacNhan.addActionListener(e -> {
            Overlay.dong(host);
            onConfirm.accept(chon[0]);
        });
        card.add(buttons(dong, xacNhan), BorderLayout.SOUTH);
        Overlay.hien(host, card, dongPopup);
    }

    /** Ô chọn phương thức thanh toán; ô đang chọn nền xanh nhạt, viền xanh và dấu ✓. */
    private static final class MethodCard extends JPanel {
        final String ten;
        private boolean chon;

        MethodCard(String ten, boolean chon) {
            this.ten = ten;
            this.chon = chon;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 50));
        }

        void setChon(boolean c) { chon = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(chon ? Theme.PRIMARY_SOFT : Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(chon ? Theme.PRIMARY : Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ten, 14, (h - fm.getHeight()) / 2 + fm.getAscent());
            if (chon) {
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = w - 24, cy = h / 2;
                g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
                g2.drawLine(cx - 1, cy + 4, cx + 6, cy - 4);
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // Xác nhận hủy
    // =====================================================================
    static void xacNhanHuy(Component host, Runnable onYes) {
        RoundedPanel card = card(580, 340, "Bạn có muốn hủy hóa đơn này không?",
                "Thông tin chưa lưu sẽ bị mất nếu bạn xác nhận hủy.");

        JComponent icon = new JComponent() {
            { setPreferredSize(new Dimension(46, 46)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0xFEEBCB));
                g2.fillOval(0, 0, 46, 46);
                g2.setColor(new Color(0xD9822B));
                g2.setFont(Theme.font(Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("!", (46 - fm.stringWidth("!")) / 2, (46 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        JLabel msg = new JLabel("Thao tác này không thể hoàn tác.");
        msg.setFont(Theme.font(Font.PLAIN, 13));
        msg.setForeground(Theme.TEXT);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(22, 0, 0, 0));
        row.add(icon);
        row.add(msg);
        card.add(row, BorderLayout.CENTER);

        RoundedButton khong = secondary("Không", 150);
        RoundedButton co = new RoundedButton("Có, hủy", Theme.DANGER, Theme.DANGER_HOVER, Color.WHITE, null, 8);
        co.setPreferredSize(new Dimension(172, 42));
        Runnable dong = () -> Overlay.dong(host);
        khong.addActionListener(e -> dong.run());
        co.addActionListener(e -> {
            Overlay.dong(host);
            onYes.run();
        });
        card.add(buttons(khong, co), BorderLayout.SOUTH);
        Overlay.hien(host, card, dong);
    }

    // =====================================================================
    // Quét mã
    // =====================================================================
    static void quetMa(Component host, SanPhamBUS bus, Consumer<SanPham> onFound) {
        RoundedPanel card = card(580, 350, "Quét mã sản phẩm", "Quét mã vạch hoặc nhập mã SKU của sản phẩm / dịch vụ.");
        PlaceholderField tf = new PlaceholderField("Nhập hoặc quét mã SKU");
        FieldBox box = new FieldBox(tf, Color.WHITE, 42);
        JLabel err = errorLabel();
        tf.getDocument().addDocumentListener(clearError(err, box));
        card.add(body(label("Mã SKU"), box, err), BorderLayout.CENTER);

        RoundedButton dong = secondary("Đóng", 150);
        RoundedButton them = primary("Thêm vào hóa đơn", 200);
        Runnable dongPopup = () -> Overlay.dong(host);
        Runnable xong = () -> {
            SanPham sp = bus.timTheoSku(tf.getText());
            if (sp == null) {
                err.setText(tf.getText().trim().isEmpty() ? "Vui lòng nhập mã SKU."
                        : "Không tìm thấy sản phẩm có mã \"" + tf.getText().trim() + "\".");
                box.setError(true);
                tf.requestFocusInWindow();
                return;
            }
            Overlay.dong(host);
            onFound.accept(sp);
        };
        dong.addActionListener(e -> dongPopup.run());
        them.addActionListener(e -> xong.run());
        tf.addActionListener(e -> xong.run());
        card.add(buttons(dong, them), BorderLayout.SOUTH);
        Overlay.hien(host, card, dongPopup);
        SwingUtilities.invokeLater(tf::requestFocusInWindow);
    }

    // =====================================================================
    // Chi tiết hóa đơn (dùng ở Lịch sử hóa đơn)
    // =====================================================================
    static void chiTiet(Component host, HoaDon hd) {
        String phuLuc = hd.daThanhToan()
                ? hd.getTrangThai() + " · " + NGAY_GIO.format(hd.getNgayThanhToan()) + " · " + hd.getPhuongThuc()
                : hd.getTrangThai();
        int cao = 420 + 40 * Math.min(hd.getDong().size(), 6);
        RoundedPanel card = card(640, cao, "Hóa đơn " + hd.getMaHD(), phuLuc);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(18, 0, 14, 0));

        body.add(infoRow("Khách hàng", hd.getTenKH() + (hd.getMaKH().isEmpty() ? "" : " (" + hd.getMaKH() + ")")));
        body.add(Box.createVerticalStrut(6));
        body.add(infoRow("Số điện thoại", hd.getSdt()));
        body.add(Box.createVerticalStrut(6));
        body.add(infoRow("Ngày tạo", NGAY_GIO.format(hd.getNgayTao())));
        body.add(Box.createVerticalStrut(16));

        JPanel head = lineRow("Sản phẩm / dịch vụ", "SL", "Thành tiền", true);
        body.add(head);

        JPanel lines = new JPanel();
        lines.setOpaque(false);
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
        for (DongGioHang d : hd.getDong()) {
            String ten = d.getSanPham().getTen() + "  ·  " + Theme.money(d.getSanPham().getDonGia());
            JPanel r = lineRow(ten, String.valueOf(d.getSoLuong()), Theme.money(d.thanhTien()), false);
            lines.add(r);
        }
        ScrollPanel view = new ScrollPanel(new BorderLayout());
        view.setOpaque(false);
        view.add(lines, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        SlimScrollBarUI.apply(sp);
        body.add(sp);
        body.add(Box.createVerticalStrut(12));

        body.add(sumRow("Tạm tính", Theme.money(hd.tamTinh()), false));
        body.add(Box.createVerticalStrut(6));
        String giamTxt = hd.getMaGiamGia() == null ? "Giảm giá" : "Giảm giá (" + hd.getMaGiamGia().getMa() + ")";
        body.add(sumRow(giamTxt, hd.tienGiam() > 0 ? "-" + Theme.money(hd.tienGiam()) : Theme.money(0), false));
        body.add(Box.createVerticalStrut(10));
        body.add(sumRow("Tổng thanh toán", Theme.money(hd.canThanhToan()), true));
        card.add(body, BorderLayout.CENTER);

        RoundedButton dong = primary("Đóng", 130);
        Runnable dongPopup = () -> Overlay.dong(host);
        dong.addActionListener(e -> dongPopup.run());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.add(dong);
        card.add(south, BorderLayout.SOUTH);
        Overlay.hien(host, card, dongPopup);
    }

    private static JPanel infoRow(String k, String v) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel a = new JLabel(k);
        a.setFont(Theme.font(Font.PLAIN, 13));
        a.setForeground(Theme.MUTED);
        a.setPreferredSize(new Dimension(130, 20));
        JLabel b = new JLabel(v);
        b.setFont(Theme.font(Font.BOLD, 13));
        b.setForeground(Theme.TEXT);
        p.add(a, BorderLayout.WEST);
        p.add(b, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return p;
    }

    private static JPanel lineRow(String ten, String sl, String tien, boolean header) {
        int pad = header ? 6 : 9;
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new javax.swing.border.CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                new EmptyBorder(pad, 0, pad, 0)));
        Font f = Theme.font(header ? Font.BOLD : Font.PLAIN, header ? 11 : 13);
        Color c = header ? Theme.MUTED : Theme.TEXT;
        JLabel a = new JLabel(ten);
        JLabel b = new JLabel(sl, SwingConstants.CENTER);
        JLabel d = new JLabel(tien, SwingConstants.RIGHT);
        for (JLabel l : new JLabel[] {a, b, d}) { l.setFont(f); l.setForeground(c); }
        d.setFont(Theme.font(Font.BOLD, header ? 11 : 13));
        b.setPreferredSize(new Dimension(40, 18));
        d.setPreferredSize(new Dimension(110, 18));
        p.add(a, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(b, BorderLayout.WEST);
        right.add(d, BorderLayout.EAST);
        p.add(right, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private static JPanel sumRow(String k, String v, boolean big) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel a = new JLabel(k);
        a.setFont(Theme.font(big ? Font.BOLD : Font.PLAIN, big ? 14 : 13));
        a.setForeground(big ? Theme.TEXT : Theme.MUTED);
        JLabel b = new JLabel(v, SwingConstants.RIGHT);
        b.setFont(Theme.font(big ? Font.BOLD : Font.PLAIN, big ? 20 : 13));
        b.setForeground(Theme.TEXT);
        p.add(a, BorderLayout.WEST);
        p.add(b, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    // =====================================================================
    // Khung chung
    // =====================================================================
    private static RoundedPanel card(int w, int h, String title, String subtitle) {
        RoundedPanel c = new RoundedPanel(16, Color.WHITE, null);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(32, PAD, 30, PAD));
        c.setPreferredSize(new Dimension(w, h));

        JLabel t = new JLabel(title);
        t.setFont(Theme.font(Font.BOLD, 22));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(t);
        head.add(Box.createVerticalStrut(8));
        head.add(s);
        c.add(head, BorderLayout.NORTH);
        return c;
    }

    private static JPanel body(JLabel label, FieldBox box, JLabel err) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(26, 0, 0, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        err.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(label);
        p.add(Box.createVerticalStrut(8));
        p.add(box);
        p.add(Box.createVerticalStrut(8));
        p.add(err);
        return p;
    }

    private static JPanel buttons(RoundedButton left, RoundedButton right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.TEXT);
        return l;
    }

    private static JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.DANGER);
        return l;
    }

    private static DocumentListener clearError(JLabel err, FieldBox box) {
        return new DocumentListener() {
            private void c() { err.setText(" "); box.setError(false); }
            @Override public void insertUpdate(DocumentEvent e) { c(); }
            @Override public void removeUpdate(DocumentEvent e) { c(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        };
    }

    private static RoundedButton primary(String text, int w) {
        RoundedButton b = new RoundedButton(text, Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        b.setPreferredSize(new Dimension(w, 42));
        return b;
    }

    private static RoundedButton secondary(String text, int w) {
        RoundedButton b = new RoundedButton(text, Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        b.setPreferredSize(new Dimension(w, 42));
        return b;
    }
}
