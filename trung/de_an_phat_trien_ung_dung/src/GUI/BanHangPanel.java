package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.GioHangBUS;
import Bus.SanPhamBUS;
import Entity.DongGioHang;
import Entity.SanPham;

/** Màn hình "Bán hàng & Tiếp nhận tại quầy": danh mục - sản phẩm/dịch vụ - giỏ hàng. */
public class BanHangPanel extends JPanel {
    private static final Color HOVER_BG = new Color(0xF3F6F8);
    private static final Color IMG_BG = new Color(0xE3EBEC);

    private final SanPhamBUS spBus = new SanPhamBUS();
    private final GioHangBUS gioBus = new GioHangBUS();
    private String danhMuc = SanPhamBUS.TAT_CA;

    private final List<CategoryItem> catItems = new ArrayList<>();
    private final PlaceholderField txtTim = new PlaceholderField("Tìm sản phẩm hoặc dịch vụ...");
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
    private final ScrollPanel gridView = new ScrollPanel(new BorderLayout());
    private final JLabel lblKhongCo = new JLabel("Không tìm thấy sản phẩm hoặc dịch vụ phù hợp", SwingConstants.CENTER);

    private final JPanel pnlDong = new JPanel();
    private JScrollPane cartScroll;
    private final JLabel lblHoaDon = new JLabel();
    private final JLabel lblTam = new JLabel();
    private final JLabel lblGiam = new JLabel();
    private final JLabel lblVat = new JLabel();
    private final JLabel lblTong = new JLabel();
    private final RoundedButton btnThanhToan =
            new RoundedButton("Thanh toán", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10);

    public BanHangPanel() {
        super(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(buildCategories(), BorderLayout.WEST);
        body.add(buildProducts(), BorderLayout.CENTER);
        body.add(buildCart(), BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        loadProducts();
        renderCart(false);
    }

    // ---------------- Tiêu đề ----------------
    private JComponent buildHeader() {
        JLabel t = new JLabel("Bán hàng & Tiếp nhận tại quầy");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Chọn sản phẩm, dịch vụ và lập hóa đơn cho khách hàng");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(t);
        head.add(Box.createVerticalStrut(4));
        head.add(s);
        return head;
    }

    // ---------------- Cột danh mục ----------------
    private JComponent buildCategories() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 14, 18, 14));
        card.setPreferredSize(new Dimension(190, 0));

        JLabel l = sectionLabel("DANH MỤC");
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 4, 0, 0));
        card.add(l);
        card.add(Box.createVerticalStrut(12));
        for (String name : SanPhamBUS.DANH_MUC) {
            CategoryItem it = new CategoryItem(name);
            it.addActionListener(e -> { danhMuc = name; loadProducts(); });
            catItems.add(it);
            card.add(it);
            card.add(Box.createVerticalStrut(6));
        }
        card.add(Box.createVerticalGlue());
        return card;
    }

    // ---------------- Cột sản phẩm / dịch vụ ----------------
    private JComponent buildProducts() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 18, 0, 18));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(sectionLabel("SẢN PHẨM / DỊCH VỤ"), BorderLayout.NORTH);

        RoundedPanel search = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        search.setLayout(new BorderLayout());
        search.setPreferredSize(new Dimension(0, 40));
        txtTim.setBorder(new EmptyBorder(0, 12, 0, 12));
        txtTim.setOpaque(false);
        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        search.add(txtTim, BorderLayout.CENTER);
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadProducts(); }
            @Override public void removeUpdate(DocumentEvent e) { loadProducts(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });
        // Máy quét mã vạch gõ mã rồi nhấn Enter: nếu trùng SKU thì thêm thẳng vào giỏ
        txtTim.addActionListener(e -> {
            SanPham sp = spBus.timTheoSku(txtTim.getText());
            if (sp != null) {
                txtTim.setText("");
                themVaoGio(sp);
            }
        });

        RoundedButton btnQuet = new RoundedButton("Quét mã", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(20, 0);
        btnQuet.setPreferredSize(new Dimension(btnQuet.getPreferredSize().width, 40));
        btnQuet.addActionListener(e -> quetMa());

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(search, BorderLayout.CENTER);
        row.add(btnQuet, BorderLayout.EAST);
        top.add(row, BorderLayout.CENTER);
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
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 18, 0));
        wrap.add(sp, BorderLayout.CENTER);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private void loadProducts() {
        for (CategoryItem it : catItems) it.setSelected(it.getText().equals(danhMuc));

        List<SanPham> ds = spBus.loc(danhMuc, txtTim.getText());
        grid.removeAll();
        for (SanPham sp : ds) grid.add(new ProductCard(sp));
        gridView.removeAll();
        gridView.add(ds.isEmpty() ? lblKhongCo : grid, BorderLayout.NORTH);
        gridView.revalidate();
        gridView.repaint();
    }

    private void quetMa() {
        String in = JOptionPane.showInputDialog(this, "Nhập hoặc quét mã SKU của sản phẩm:", "Quét mã",
                JOptionPane.PLAIN_MESSAGE);
        if (in == null || in.trim().isEmpty()) return;
        SanPham sp = spBus.timTheoSku(in);
        if (sp == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm có mã \"" + in.trim() + "\".",
                    "Quét mã", JOptionPane.WARNING_MESSAGE);
            return;
        }
        themVaoGio(sp);
    }

    // ---------------- Cột giỏ hàng ----------------
    private JComponent buildCart() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(330, 0));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBorder(new EmptyBorder(18, 18, 10, 18));
        JLabel gh = sectionLabel("GIỎ HÀNG");
        gh.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblHoaDon.setFont(Theme.font(Font.BOLD, 12));
        lblHoaDon.setForeground(Theme.TEXT);
        lblHoaDon.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(gh);
        head.add(Box.createVerticalStrut(8));
        head.add(lblHoaDon);
        card.add(head, BorderLayout.NORTH);

        pnlDong.setOpaque(false);
        pnlDong.setLayout(new BoxLayout(pnlDong, BoxLayout.Y_AXIS));
        ScrollPanel view = new ScrollPanel(new BorderLayout());
        view.setOpaque(false);
        view.add(pnlDong, BorderLayout.NORTH);
        cartScroll = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.setViewportBorder(null);
        cartScroll.setOpaque(false);
        cartScroll.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(cartScroll);
        card.add(cartScroll, BorderLayout.CENTER);

        // Tổng tiền
        JPanel foot = new JPanel();
        foot.setOpaque(false);
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        foot.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.ROW_LINE), new EmptyBorder(14, 18, 18, 18)));

        for (JLabel v : new JLabel[] {lblTam, lblGiam, lblVat}) {
            v.setFont(Theme.font(Font.PLAIN, 13));
            v.setForeground(Theme.TEXT);
        }
        lblGiam.setForeground(Theme.ACCENT);
        lblGiam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblGiam.setToolTipText("Nhấn để nhập giảm giá (số tiền hoặc %)");
        lblGiam.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { nhapGiamGia(); }
        });
        lblTong.setFont(Theme.font(Font.BOLD, 20));
        lblTong.setForeground(Theme.TEXT);

        foot.add(totalRow("Tạm tính", lblTam, false));
        foot.add(Box.createVerticalStrut(8));
        foot.add(totalRow("Giảm giá", lblGiam, false));
        foot.add(Box.createVerticalStrut(8));
        foot.add(totalRow("VAT (" + Math.round(GioHangBUS.TY_LE_VAT * 100) + "%)", lblVat, false));
        foot.add(Box.createVerticalStrut(14));
        foot.add(totalRow("Tổng cộng", lblTong, true));
        foot.add(Box.createVerticalStrut(14));

        btnThanhToan.setPreferredSize(new Dimension(0, 42));
        btnThanhToan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnThanhToan.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnThanhToan.addActionListener(e -> thanhToan());
        foot.add(btnThanhToan);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private JComponent totalRow(String label, JLabel value, boolean big) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(big ? Font.BOLD : Font.PLAIN, big ? 14 : 13));
        l.setForeground(big ? Theme.TEXT : Theme.MUTED);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private void themVaoGio(SanPham sp) {
        gioBus.them(sp);
        renderCart(true);
    }

    private void renderCart(boolean cuonXuongCuoi) {
        lblHoaDon.setText("Hóa đơn #" + gioBus.maHoaDon());

        pnlDong.removeAll();
        List<DongGioHang> ds = gioBus.layDong();
        if (ds.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setBorder(new EmptyBorder(48, 18, 0, 18));
            JLabel a = new JLabel("Chưa có sản phẩm nào trong giỏ");
            a.setFont(Theme.font(Font.PLAIN, 13));
            a.setForeground(Theme.MUTED);
            a.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel b = new JLabel("Chọn sản phẩm hoặc quét mã để thêm");
            b.setFont(Theme.font(Font.PLAIN, 11));
            b.setForeground(new Color(0x9AA6B0));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(a);
            empty.add(Box.createVerticalStrut(4));
            empty.add(b);
            pnlDong.add(empty);
        } else {
            for (DongGioHang d : ds) pnlDong.add(cartLine(d));
        }

        lblTam.setText(Theme.money(gioBus.tamTinh()));
        long giam = gioBus.giamGia();
        lblGiam.setText(giam > 0 ? "-" + Theme.money(giam) : Theme.money(0));
        lblVat.setText(Theme.money(gioBus.vat()));
        lblTong.setText(Theme.money(gioBus.tong()));
        btnThanhToan.setEnabled(!ds.isEmpty());

        pnlDong.revalidate();
        pnlDong.repaint();
        if (cuonXuongCuoi) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = cartScroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        }
    }

    private JComponent cartLine(DongGioHang d) {
        SanPham sp = d.getSanPham();
        JPanel line = new JPanel(new BorderLayout(10, 0));
        line.setOpaque(false);
        line.setBorder(new EmptyBorder(8, 18, 8, 18));

        JLabel name = new JLabel(sp.getTen());
        name.setFont(Theme.font(Font.BOLD, 13));
        name.setForeground(Theme.TEXT);

        JLabel qty = new JLabel(String.valueOf(d.getSoLuong()), SwingConstants.CENTER);
        qty.setFont(Theme.font(Font.BOLD, 13));
        qty.setForeground(Theme.TEXT);
        qty.setPreferredSize(new Dimension(28, 24));

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ctrl.setOpaque(false);
        ctrl.add(qtyButton("−", () -> { gioBus.doiSoLuong(sp.getSku(), -1); renderCart(false); }));
        ctrl.add(qty);
        ctrl.add(qtyButton("+", () -> { gioBus.doiSoLuong(sp.getSku(), 1); renderCart(false); }));

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);
        left.add(name, BorderLayout.NORTH);
        left.add(ctrl, BorderLayout.CENTER);

        JLabel price = new JLabel(Theme.money(d.thanhTien()), SwingConstants.RIGHT);
        price.setFont(Theme.font(Font.BOLD, 13));
        price.setForeground(Theme.TEXT);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(price, BorderLayout.NORTH);

        line.add(left, BorderLayout.CENTER);
        line.add(right, BorderLayout.EAST);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, line.getPreferredSize().height));
        return line;
    }

    private RoundedButton qtyButton(String text, Runnable action) {
        RoundedButton b = new RoundedButton(text, Color.WHITE, HOVER_BG, Theme.TEXT, Theme.BORDER, 8);
        b.setFont(Theme.font(Font.PLAIN, 14));
        b.setPreferredSize(new Dimension(24, 24));
        b.addActionListener(e -> action.run());
        return b;
    }

    private void nhapGiamGia() {
        if (gioBus.trong()) {
            JOptionPane.showMessageDialog(this, "Hãy thêm sản phẩm vào giỏ hàng trước khi nhập giảm giá.",
                    "Giảm giá", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String in = JOptionPane.showInputDialog(this,
                "Nhập giảm giá: số tiền (ví dụ 50000) hoặc phần trăm (ví dụ 10%).\nĐể trống để bỏ giảm giá.",
                "Giảm giá", JOptionPane.PLAIN_MESSAGE);
        if (in == null) return;
        if (!gioBus.datGiamGia(in)) {
            JOptionPane.showMessageDialog(this, "Giá trị giảm giá không hợp lệ hoặc vượt quá tạm tính.",
                    "Giảm giá", JOptionPane.ERROR_MESSAGE);
            return;
        }
        renderCart(false);
    }

    private void thanhToan() {
        if (gioBus.trong()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống.", "Thanh toán", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ma = gioBus.maHoaDon();
        String[] opts = {"Tiền mặt", "Chuyển khoản", "Hủy"};
        int r = JOptionPane.showOptionDialog(this,
                "Hóa đơn #" + ma + "\nTổng thanh toán: " + Theme.money(gioBus.tong()) + "\n\nChọn phương thức thanh toán:",
                "Thanh toán", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        if (r != 0 && r != 1) return;
        long tong = gioBus.tong();
        gioBus.thanhToan();
        renderCart(false);
        JOptionPane.showMessageDialog(this,
                "Thanh toán thành công!\nHóa đơn #" + ma + " · " + opts[r] + " · " + Theme.money(tong),
                "Thanh toán", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- Thành phần giao diện ----------------
    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 11));
        l.setForeground(Theme.MUTED);
        return l;
    }

    private static String cat(String s, FontMetrics fm, int maxW) {
        if (fm.stringWidth(s) <= maxW) return s;
        String dots = "…";
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n).trim() + dots) > maxW) n--;
        return s.substring(0, n).trim() + dots;
    }

    /** Mục danh mục: chữ căn trái, nền đậm khi chọn, nền nhạt khi rê chuột. */
    private static class CategoryItem extends JToggleButton {
        private boolean hovering;

        CategoryItem(String text) {
            super(text);
            setFont(Theme.font(Font.PLAIN, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(0, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (isSelected()) {
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hovering) {
                g2.setColor(HOVER_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            g2.setFont(getFont().deriveFont(isSelected() ? Font.BOLD : Font.PLAIN));
            g2.setColor(isSelected() ? Color.WHITE : Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 14, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Thẻ sản phẩm/dịch vụ: ảnh tạm, tên, SKU, giá và nút "+". Nhấn vào thẻ để thêm vào giỏ. */
    private class ProductCard extends JPanel {
        private final SanPham sp;
        private boolean hover;

        ProductCard(SanPham sp) {
            this.sp = sp;
            setOpaque(false);
            setPreferredSize(new Dimension(150, 176));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(sp.getTen());
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) themVaoGio(ProductCard.this.sp);
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 10;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(hover ? Theme.ACCENT : Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            // Ảnh tạm
            g2.setColor(IMG_BG);
            g2.fillRoundRect(pad, pad, w - pad * 2, 72, 10, 10);
            Icons.draw(g2, sp.isDichVu() ? "paw" : "box", w / 2.0 - 15, pad + 36 - 15, 30, new Color(0xB3C3CC), false);

            int textW = w - pad * 2;
            g2.setFont(Theme.font(Font.BOLD, 12));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(cat(sp.getTen(), fm, textW), pad, 104);

            g2.setFont(Theme.font(Font.PLAIN, 10));
            g2.setColor(Theme.MUTED);
            g2.drawString("SKU: " + sp.getSku(), pad, 119);

            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.TEXT);
            g2.drawString(Theme.money(sp.getDonGia()), pad, h - pad - 6);

            // Nút +
            int d = 26, cx = w - pad - d, cy = h - pad - d;
            g2.setColor(hover ? Theme.ACCENT_HOVER : Theme.ACCENT);
            g2.fillOval(cx, cy, d, d);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx + 8, cy + d / 2, cx + d - 8, cy + d / 2);
            g2.drawLine(cx + d / 2, cy + 8, cx + d / 2, cy + d - 8);
            g2.dispose();
        }
    }
}
