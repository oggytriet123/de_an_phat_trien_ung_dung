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
    };

    private final TaiKhoan taiKhoan;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, NavButton> navs = new LinkedHashMap<>();

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
        // Giữ nguyên panel Hệ Thống của nhóm
        content.add(new HeThongPanel(taiKhoan, this), "Hệ Thống");

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        chon("Thú cưng"); // Chọn màn hình Thú cưng theo thiết kế tham chiếu
        setSize(1320, 840);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(230, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        // Thương hiệu PetCare POS
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(26, 20, 22, 10));
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel("PetCare POS");
        name.setFont(Theme.font(Font.BOLD, 18));
        name.setForeground(Color.WHITE);
        brand.add(name);
        side.add(brand);

        // 7 mục menu
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
        navs.forEach((k, b) -> b.setSelected(k.equals(key)));
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

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (isSelected() || hovering) {
                g2.setColor(isSelected() ? Theme.SIDEBAR_ACTIVE : Theme.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            Icons.draw(g2, iconName, 14, (getHeight() - 20) / 2.0, 20, Color.WHITE, false);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 46, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
