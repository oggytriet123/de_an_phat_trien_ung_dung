package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Bus.HoaDonBUS;
import Entity.HoaDon;

/** Màn hình "Quản lý hóa đơn": 20 ô (5 cột x 4 hàng). Ô trống để tạo mới, ô có hóa đơn để mở lại. */
public class QuanLyHoaDonPanel extends JPanel implements Refreshable {
    private static final int COT = 5;

    private final HoaDonBUS bus;
    private final IntConsumer moO;
    private final JLabel lblThongBao = new JLabel(" ");
    private Timer hen;

    /**
     * @param moO     được gọi với chỉ số ô (0..19) khi nhấn một ô
     * @param taoMoi  được gọi khi nhấn nút "Tạo hóa đơn mới"
     */
    public QuanLyHoaDonPanel(HoaDonBUS bus, IntConsumer moO, Runnable taoMoi) {
        super(new BorderLayout(0, 14));
        this.bus = bus;
        this.moO = moO;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 32, 22, 32));

        add(buildHeader(taoMoi), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(HoaDonBUS.SO_O / COT, COT, 20, 18));
        grid.setOpaque(false);
        for (int i = 0; i < HoaDonBUS.SO_O; i++) grid.add(new OTile(i));
        // Đủ chỗ thì các ô giãn đầy vùng hiển thị; cửa sổ thấp quá thì cuộn dọc thay vì chồng chữ.
        ScrollPanel view = new ScrollPanel(new BorderLayout()) {
            @Override public boolean getScrollableTracksViewportHeight() {
                return getParent() instanceof JViewport vp && vp.getHeight() >= getPreferredSize().height;
            }
        };
        view.setOpaque(false);
        view.add(grid, BorderLayout.CENTER);
        JScrollPane sp = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(sp);
        add(sp, BorderLayout.CENTER);

        JLabel foot = new JLabel("Hóa đơn đã lưu sẽ luôn nằm trong ô này cho đến khi được thanh toán hoặc hủy.");
        foot.setFont(Theme.font(Font.PLAIN, 12));
        foot.setForeground(Theme.MUTED);
        foot.setBorder(new EmptyBorder(4, 0, 0, 0));
        add(foot, BorderLayout.SOUTH);
    }

    private JComponent buildHeader(Runnable taoMoi) {
        JLabel t = new JLabel("Quản lý hóa đơn");
        t.setFont(Theme.font(Font.BOLD, 26));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Chọn một ô trống để tạo hóa đơn mới hoặc mở hóa đơn đang chờ thanh toán.");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(6));
        left.add(s);

        RoundedButton btn = new RoundedButton("+   Tạo hóa đơn mới", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btn.setPreferredSize(new Dimension(218, 44));
        btn.addActionListener(e -> taoMoi.run());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btn);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        // Chú thích màu
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        legend.setOpaque(false);
        legend.add(legendItem("Trống", Color.WHITE, Theme.BORDER));
        legend.add(Box.createHorizontalStrut(22));
        legend.add(legendItem("Chưa thanh toán", Theme.AMBER_BG, Theme.AMBER_BORDER));

        lblThongBao.setFont(Theme.font(Font.BOLD, 12));
        lblThongBao.setForeground(Theme.SUCCESS);
        JPanel legendRow = new JPanel(new BorderLayout());
        legendRow.setOpaque(false);
        legendRow.add(legend, BorderLayout.WEST);
        legendRow.add(lblThongBao, BorderLayout.EAST);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(top);
        head.add(Box.createVerticalStrut(14));
        head.add(legendRow);
        head.add(Box.createVerticalStrut(2));
        return head;
    }

    private static JComponent legendItem(String text, Color fill, Color line) {
        Icon sq = new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillRoundRect(x, y, 15, 15, 5, 5);
                g2.setColor(line);
                g2.drawRoundRect(x, y, 14, 14, 5, 5);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
        JLabel l = new JLabel(text, sq, SwingConstants.LEFT);
        l.setIconTextGap(9);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);
        return l;
    }

    /** Thông báo ngắn (tự mất sau vài giây), ví dụ sau khi thanh toán xong. */
    public void thongBao(String text) {
        lblThongBao.setText(text);
        if (hen != null) hen.stop();
        hen = new Timer(6000, e -> lblThongBao.setText(" "));
        hen.setRepeats(false);
        hen.start();
    }

    @Override public void lamMoi() {
        repaint();
    }

    /** Một ô hóa đơn. Nội dung đọc trực tiếp từ BUS lúc vẽ nên luôn đúng với dữ liệu hiện tại. */
    private class OTile extends JPanel {
        private final int viTri;
        private boolean hover;

        OTile(int viTri) {
            this.viTri = viTri;
            setOpaque(false);
            setPreferredSize(new Dimension(150, 120)); // đủ chỗ cho mã, tên, tiền và nhãn trạng thái
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())) moO.accept(OTile.this.viTri);
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            HoaDon hd = bus.layO(viTri);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 16;

            g2.setColor(hd == null ? Color.WHITE : Theme.AMBER_BG);
            g2.fillRoundRect(0, 0, w, h, 10, 10);
            g2.setColor(hover ? Theme.PRIMARY : hd == null ? Theme.BORDER : Theme.AMBER_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            g2.setFont(Theme.font(Font.BOLD, 11));
            g2.setColor(Theme.MUTED);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(String.format("Ô %02d", viTri + 1), pad, 24);

            if (hd == null) {
                // dấu + và dòng hướng dẫn nằm giữa ô
                int cx = w / 2, cy = h / 2 - 8;
                g2.setColor(new Color(0x8795A0));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 8, cy, cx + 8, cy);
                g2.drawLine(cx, cy - 8, cx, cy + 8);
                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.setColor(Theme.MUTED);
                fm = g2.getFontMetrics();
                String s = "Nhấn để tạo hóa đơn";
                g2.drawString(s, (w - fm.stringWidth(s)) / 2, cy + 38);
            } else {
                g2.setFont(Theme.font(Font.BOLD, 18));
                g2.setColor(Theme.TEXT);
                g2.drawString(hd.getMaHD(), pad, 50);

                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.setColor(Theme.TEXT);
                fm = g2.getFontMetrics();
                g2.drawString(cat(hd.getTenKH(), fm, w - pad * 2), pad, 69);

                g2.setFont(Theme.font(Font.BOLD, 15));
                g2.setColor(Theme.TEXT);
                g2.drawString(Theme.money(hd.canThanhToan()), pad, 90);

                // nhãn trạng thái
                g2.setFont(Theme.font(Font.BOLD, 10));
                fm = g2.getFontMetrics();
                int pw = fm.stringWidth(hd.getTrangThai()) + 24, ph = 20, py = Math.max(98, h - 34);
                g2.setColor(Theme.AMBER_PILL);
                g2.fillRoundRect(pad, py, pw, ph, ph, ph);
                g2.setColor(Theme.AMBER_TEXT);
                g2.drawString(hd.getTrangThai(), pad + 12, py + (ph - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    private static String cat(String s, FontMetrics fm, int maxW) {
        if (fm.stringWidth(s) <= maxW) return s;
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n).trim() + "…") > maxW) n--;
        return s.substring(0, n).trim() + "…";
    }
}
