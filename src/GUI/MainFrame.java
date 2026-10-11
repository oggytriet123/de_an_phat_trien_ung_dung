package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Entity.TaiKhoan;

/** Khung chính: thanh điều hướng bên trái (Sidebar dọc 7 mục) + vùng nội dung (CardLayout). */
public class MainFrame extends JFrame {
    private static final String[][] MENU = {
        // tên hiển thị, icon, mô tả
        {"Tổng quan", "dashboard", "Số liệu hoạt động của cửa hàng trong ngày"},
        {"Lịch hẹn", "calendar", "Đặt lịch khám, spa, grooming và lưu trú"},
        {"Thú cưng", "paw", "Quản lý hồ sơ, thông tin chăm sóc và sức khỏe của thú cưng"},
        {"Khách hàng", "customers", "Hồ sơ khách hàng, thẻ thành viên và lịch sử giao dịch"},
        {"Dịch vụ", "scissors", "Dịch vụ khám, spa, grooming"},
        {"Kho hàng", "box", "Theo dõi sản phẩm, tồn kho và nhập xuất"},
        {"Nhân viên", "person", "Quản lý nhân viên, ca làm và phân quyền"},
        {"Báo cáo", "chart", "Báo cáo doanh thu, tồn kho và hoạt động"},
    };

    private final TaiKhoan taiKhoan;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, NavButton> navs = new LinkedHashMap<>();
    private JPanel pnlSubKhoHang;
    private SubNavButton btnSubLichSu;

    public MainFrame(TaiKhoan taiKhoan) {
        super("PetCare POS");
        this.taiKhoan = taiKhoan;
        setIconImage(Icons.appIcon());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { xacNhanThoat(); }
        });

        content.setBackground(Theme.BG);
        for (String[] m : MENU) {
            JPanel p;
            if (m[0].equals("Thú cưng")) {
                p = new ThuCungPanel();
            } else if (m[0].equals("Khách hàng")) {
                p = new KhachHangPanel();
            } else if (m[0].equals("Kho hàng")) {
                p = new KhoHangPanel();
            } else {
                p = new ModulePlaceholderPanel(m[0], m[2], m[1]);
            }
            content.add(p, m[0]);
        }
        content.add(new LichSuNhapXuatPanel(), "Lịch sử Nhập / Xuất Kho");
        // Giữ nguyên panel Hệ Thống của nhóm
        content.add(new HeThongPanel(taiKhoan, this), "Hệ Thống");

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        chon("Lịch sử Nhập / Xuất Kho"); // Mở thẳng màn hình Lịch sử Nhập / Xuất Kho vừa hoàn thiện
        setSize(1320, 840);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(230, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        // Thương hiệu PetCare POS (icon chân thú + tên)
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(24, 18, 16, 10));
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel icPaw = new JLabel(Icons.of("paw", 22, Color.WHITE));
        JLabel name = new JLabel("PetCare POS");
        name.setFont(Theme.font(Font.BOLD, 18));
        name.setForeground(Color.WHITE);
        brand.add(icPaw);
        brand.add(name);
        side.add(brand);

        // Nhãn phân mục "Điều hành"
        JLabel lblSec = new JLabel("Điều hành");
        lblSec.setFont(Theme.font(Font.PLAIN, 11));
        lblSec.setForeground(new Color(255, 255, 255, 140));
        JPanel secWrap = new JPanel(new BorderLayout());
        secWrap.setOpaque(false);
        secWrap.setBorder(new EmptyBorder(0, 20, 8, 10));
        secWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        secWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        secWrap.add(lblSec, BorderLayout.WEST);
        side.add(secWrap);

        // Sub-item: Lịch sử Nhập / Xuất Kho (dưới Kho hàng)
        pnlSubKhoHang = new JPanel(new BorderLayout());
        pnlSubKhoHang.setOpaque(false);
        pnlSubKhoHang.setBorder(new EmptyBorder(1, 12, 1, 12));
        pnlSubKhoHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pnlSubKhoHang.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSubLichSu = new SubNavButton("Lịch sử Nhập / Xuất Kho");
        btnSubLichSu.addActionListener(e -> chon("Lịch sử Nhập / Xuất Kho"));
        pnlSubKhoHang.add(btnSubLichSu, BorderLayout.CENTER);
        pnlSubKhoHang.setVisible(false);

        // Các mục menu chính
        for (String[] m : MENU) {
            NavButton b = new NavButton(m[0], m[1]);
            b.addActionListener(e -> chon(m[0]));
            navs.put(m[0], b);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(2, 12, 2, 12));
            wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.add(b);
            side.add(wrap);

            if (m[0].equals("Kho hàng")) {
                side.add(pnlSubKhoHang);
            }
        }

        side.add(Box.createVerticalGlue());
        side.add(buildUserBox());
        return side;
    }

    private JComponent buildUserBox() {
        JPanel box = new JPanel(new BorderLayout(10, 0));
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(16, 16, 20, 16));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JComponent avatar = new JComponent() {
            { setPreferredSize(new Dimension(40, 40)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0xEEF3F6));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Theme.TEXT);
                g2.setFont(Theme.font(Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String s = initials(taiKhoan.getHoTen());
                g2.drawString(s, (40 - fm.stringWidth(s)) / 2, (40 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(taiKhoan.getHoTen());
        n.setFont(Theme.font(Font.BOLD, 13));
        n.setForeground(Color.WHITE);
        JLabel r = new JLabel(taiKhoan.getVaiTro());
        r.setFont(Theme.font(Font.PLAIN, 11));
        r.setForeground(new Color(255, 255, 255, 200));
        text.add(Box.createVerticalGlue());
        text.add(n);
        text.add(r);
        text.add(Box.createVerticalGlue());

        box.add(avatar, BorderLayout.WEST);
        box.add(text, BorderLayout.CENTER);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem heThong = new JMenuItem("Hệ Thống");
        heThong.setFont(Theme.font(Font.PLAIN, 13));
        heThong.addActionListener(e -> chon("Hệ Thống"));
        JMenuItem logout = new JMenuItem("Đăng xuất");
        logout.setFont(Theme.font(Font.PLAIN, 13));
        logout.addActionListener(e -> dangXuat());
        menu.add(heThong);
        menu.addSeparator();
        menu.add(logout);

        box.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                menu.show(box, 16, -menu.getPreferredSize().height - 4);
            }
        });
        return box;
    }

    private static String initials(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) return "HT";
        String[] w = hoTen.trim().split("\\s+");
        if (w.length == 1) return w[0].substring(0, 1).toUpperCase();
        return (w[0].substring(0, 1) + w[w.length - 1].substring(0, 1)).toUpperCase();
    }

    public void chon(String key) {
        cards.show(content, key);
        boolean isLichSu = "Lịch sử Nhập / Xuất Kho".equals(key);
        boolean isKho = "Kho hàng".equals(key);

        navs.forEach((k, b) -> {
            boolean active = k.equals(key);
            b.setSelected(active);
            if ("Kho hàng".equals(k)) {
                b.setParentActive(isLichSu);
                b.setShowDot(isLichSu);
            } else {
                b.setParentActive(false);
                b.setShowDot(false);
            }
        });

        if (pnlSubKhoHang != null) {
            pnlSubKhoHang.setVisible(isKho || isLichSu);
            btnSubLichSu.setActive(isLichSu);
        }
    }

    public void dangXuat() {
        int r = JOptionPane.showOptionDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Đăng xuất",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[] {"Đăng xuất", "Hủy"}, "Hủy");
        if (r == 0) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void xacNhanThoat() {
        int r = JOptionPane.showOptionDialog(this, "Bạn có chắc chắn muốn thoát ứng dụng?", "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[] {"Thoát", "Ở lại"}, "Ở lại");
        if (r == 0) System.exit(0);
    }

    /** Nút menu bên trái: icon + chữ, nền sáng khi chọn / rê chuột. */
    private static class NavButton extends JToggleButton {
        private final String iconName;
        private boolean hovering;
        private boolean parentActive;
        private boolean showDot;

        NavButton(String text, String iconName) {
            super(text);
            this.iconName = iconName;
            setFont(Theme.font(Font.PLAIN, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 40));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            });
        }

        void setParentActive(boolean pa) {
            this.parentActive = pa;
            repaint();
        }

        void setShowDot(boolean dot) {
            this.showDot = dot;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean active = isSelected() || parentActive;
            Color iconColor = Color.WHITE;
            Color textColor = Color.WHITE;

            if (parentActive) {
                // Khi trang con được chọn: nền trắng xám sáng, icon và chữ xanh đậm
                g2.setColor(new Color(0xDEE7EC));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                iconColor = new Color(0x28566E);
                textColor = new Color(0x28566E);
            } else if (isSelected()) {
                g2.setColor(Theme.SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hovering) {
                g2.setColor(Theme.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }

            Icons.draw(g2, iconName, 14, (getHeight() - 20) / 2.0, 20, iconColor, false);
            g2.setFont(active ? Theme.font(Font.BOLD, 14) : getFont());
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 46, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());

            // Chấm tròn nhỏ bên phải khi có trang con đang mở
            if (showDot) {
                g2.setColor(new Color(0x28566E));
                g2.fillOval(getWidth() - 22, (getHeight() - 6) / 2, 6, 6);
            }

            g2.dispose();
        }
    }

    /** Nút menu con thụt lề dưới mục cha: thanh dọc chỉ báo + chữ. */
    private static class SubNavButton extends JButton {
        private boolean active;
        private boolean hovering;

        SubNavButton(String text) {
            super(text);
            setFont(Theme.font(Font.PLAIN, 12));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 32));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            });
        }

        void setActive(boolean a) { this.active = a; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (hovering) {
                g2.setColor(Theme.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            // Thanh chỉ báo dọc trắng bên trái
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(18, (getHeight() - 14) / 2, 18, (getHeight() + 14) / 2);

            g2.setFont(active ? Theme.font(Font.BOLD, 12) : getFont());
            g2.setColor(active ? Color.WHITE : new Color(255, 255, 255, 210));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 28, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
