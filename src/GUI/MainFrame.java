package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Entity.TaiKhoan;

/** Khung chính: thanh điều hướng bên trái + vùng nội dung (CardLayout). */
public class MainFrame extends JFrame {
    private static final String[][] MENU = {
        // tên hiển thị, icon, mô tả
        {"Hệ Thống", "settings", "Quản lý phiên đăng nhập, mật khẩu và thông tin cá nhân"},
<<<<<<< HEAD
        {"Danh Mục", "dashboard", "Quản lý các danh mục từ điển"},
        {"Xử Lý", "sync", "Xử lý nghiệp vụ"},
        {"Tìm Kiếm", "search", "Tra cứu thông tin"},
        {"Thống Kê", "bar-chart", "Báo cáo thống kê"},
=======
        {"Danh Mục", "dashboard", "Quản lý các danh mục từ điển (Thú cưng, Khách hàng, Kho hàng)"},
        {"Quản Lý Lịch Hẹn", "calendar", "Hồ sơ, quản lý, đổi, xác nhận và hủy lịch hẹn"},
        {"Xử Lý", "sync", "Xử lý nghiệp vụ (Lập phiếu nhập/xuất kho, lịch sử phiếu)"},
        {"Tìm Kiếm", "search", "Tra cứu thông tin hồ sơ và sản phẩm"},
        {"Thống Kê", "bar-chart", "Báo cáo thống kê hoạt động và tồn kho"},
>>>>>>> stash
    };

    private final TaiKhoan taiKhoan;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, NavButton> navs = new LinkedHashMap<>();

<<<<<<< HEAD
=======
    private KhoHangPanel pnlKhoHang;
    private LichSuNhapXuatPanel pnlLichSuKho;
    private ThuCungPanel pnlThuCung;
    private KhachHangPanel pnlKhachHang;
    private HeThongPanel pnlHeThong;
    private LichHenPanel pnlHoSoLichHen, pnlQuanLyLichHen, pnlDoiLichHen, pnlXacNhanLichHen, pnlHuyLichHen;

>>>>>>> stash
    public MainFrame(TaiKhoan taiKhoan) {
        super("PetCare POS");
        this.taiKhoan = taiKhoan;
        setIconImage(Icons.appIcon());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { xacNhanThoat(); }
        });

        content.setBackground(Theme.BG);
<<<<<<< HEAD
        for (String[] m : MENU) {
            JPanel p;
            if (m[0].equals("Hệ Thống")) {
                p = new HeThongPanel(taiKhoan, this);
            } else if (m[0].equals("Khách hàng")) {
                p = new KhachHangPanel();
            } else {
                p = new ModulePlaceholderPanel(m[0], m[2], m[1]);
            }
            content.add(p, m[0]);
        }
=======

        pnlHeThong = new HeThongPanel(taiKhoan, this);
        pnlThuCung = new ThuCungPanel();
        pnlKhachHang = new KhachHangPanel();
        pnlKhoHang = new KhoHangPanel();
        pnlLichSuKho = new LichSuNhapXuatPanel();
        pnlHoSoLichHen = new LichHenPanel(LichHenPanel.Mode.HO_SO);
        pnlQuanLyLichHen = new LichHenPanel(LichHenPanel.Mode.QUAN_LY);
        pnlDoiLichHen = new LichHenPanel(LichHenPanel.Mode.DOI);
        pnlXacNhanLichHen = new LichHenPanel(LichHenPanel.Mode.XAC_NHAN);
        pnlHuyLichHen = new LichHenPanel(LichHenPanel.Mode.HUY);

        content.add(pnlHeThong, "Hệ Thống");
        content.add(pnlThuCung, "Thú cưng");
        content.add(pnlKhachHang, "Khách hàng");
        content.add(pnlKhoHang, "Kho hàng");
        content.add(pnlLichSuKho, "Lịch sử Nhập / Xuất Kho");
        content.add(pnlHoSoLichHen, LichHenPanel.Mode.HO_SO.title);
        content.add(pnlQuanLyLichHen, LichHenPanel.Mode.QUAN_LY.title);
        content.add(pnlDoiLichHen, LichHenPanel.Mode.DOI.title);
        content.add(pnlXacNhanLichHen, LichHenPanel.Mode.XAC_NHAN.title);
        content.add(pnlHuyLichHen, LichHenPanel.Mode.HUY.title);
        content.add(new ModulePlaceholderPanel("Tìm Kiếm", "Tra cứu thông tin hồ sơ và sản phẩm", "search"), "Tìm Kiếm");
        content.add(new ModulePlaceholderPanel("Thống Kê", "Báo cáo doanh thu, khám chữa và tồn kho", "bar-chart"), "Thống Kê");
>>>>>>> stash

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

<<<<<<< HEAD
        chon("Hệ Thống"); // màn hình đã hoàn thiện; đổi thành "Tổng quan" nếu muốn mở trang đầu
        setSize(1280, 800);
        setMinimumSize(new Dimension(1050, 680));
=======
        chon("Lịch sử Nhập / Xuất Kho"); // Mở thẳng màn hình lịch sử kho vừa hoàn thiện
        setSize(1320, 840);
        setMinimumSize(new Dimension(1280, 720));
>>>>>>> stash
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(Theme.SIDEBAR); // Keep the header color the same
        header.setPreferredSize(new Dimension(0, 56));
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        // Thương hiệu
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(10, 20, 10, 20));
        brand.setAlignmentY(Component.CENTER_ALIGNMENT);
        JLabel logo = new JLabel(Icons.of("logo", 24, Color.WHITE));
        JLabel name = new JLabel("Pet Station");
        name.setFont(Theme.font(Font.BOLD, 18));
        name.setForeground(Color.WHITE);
        brand.add(logo);
        brand.add(name);
        header.add(brand);
        header.add(Box.createHorizontalStrut(20));

        // Menu
        for (String[] m : MENU) {
<<<<<<< HEAD
            NavButton b = new NavButton(m[0], m[1]);
            b.addActionListener(e -> chon(m[0]));
=======
            boolean hasDropdown = "Danh Mục".equals(m[0]) || "Xử Lý".equals(m[0]) || LichHenPanel.PARENT_TITLE.equals(m[0]);
            NavButton b = new NavButton(m[0], m[1], hasDropdown);
            b.addActionListener(e -> {
                if ("Danh Mục".equals(m[0])) {
                    showDanhMucMenu(b);
                } else if ("Xử Lý".equals(m[0])) {
                    showXuLyMenu(b);
                } else if (LichHenPanel.PARENT_TITLE.equals(m[0])) {
                    showLichHenMenu(b);
                } else {
                    chon(m[0]);
                }
            });
>>>>>>> stash
            navs.put(m[0], b);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
<<<<<<< HEAD
            wrap.setBorder(new EmptyBorder(8, 5, 8, 5));
=======
            wrap.setBorder(new EmptyBorder(8, 2, 8, 2));
>>>>>>> stash
            wrap.setAlignmentY(Component.CENTER_ALIGNMENT);
            wrap.add(b);
            header.add(wrap);
        }
        header.add(Box.createHorizontalGlue());
        header.add(buildUserBox());
        return header;
    }

<<<<<<< HEAD
=======
    private void showDanhMucMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        menu.setBackground(Color.WHITE);

        JMenuItem itThuCung = createMenuItem("Quản lý Thú cưng", "paw", () -> chon("Thú cưng"));
        JMenuItem itKhachHang = createMenuItem("Quản lý Khách hàng", "customers", () -> chon("Khách hàng"));
        JMenuItem itKhoHang = createMenuItem("Quản lý Kho hàng", "box", () -> chon("Kho hàng"));

        menu.add(itThuCung);
        menu.add(itKhachHang);
        menu.add(itKhoHang);
        menu.show(invoker, 0, invoker.getHeight() + 2);
    }

    private void showLichHenMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        menu.setBackground(Color.WHITE);

        menu.add(createMenuItem("Hồ sơ lịch hẹn", "card", () -> chon(LichHenPanel.Mode.HO_SO.title)));
        menu.add(createMenuItem("Quản lý lịch hẹn", "calendar", () -> chon(LichHenPanel.Mode.QUAN_LY.title)));
        menu.add(createMenuItem("Đổi lịch hẹn", "sync", () -> chon(LichHenPanel.Mode.DOI.title)));
        menu.add(createMenuItem("Xác nhận lịch hẹn", "check", () -> chon(LichHenPanel.Mode.XAC_NHAN.title)));
        menu.add(createMenuItem("Hủy lịch hẹn", "close", () -> chon(LichHenPanel.Mode.HUY.title)));
        menu.show(invoker, 0, invoker.getHeight() + 2);
    }

    private void showXuLyMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        menu.setBackground(Color.WHITE);

        JMenuItem itLichSu = createMenuItem("Lịch sử Nhập / Xuất Kho", "eye", () -> chon("Lịch sử Nhập / Xuất Kho"));
        JMenuItem itLapPhieu = createMenuItem("Lập phiếu nhập kho", "plus", () -> {
            chon("Kho hàng");
            if (pnlKhoHang != null) {
                pnlKhoHang.lapPhieuNhapKho(null);
            }
        });

        menu.add(itLichSu);
        menu.add(itLapPhieu);
        menu.show(invoker, 0, invoker.getHeight() + 2);
    }

    private JMenuItem createMenuItem(String title, String iconName, Runnable action) {
        JMenuItem item = new JMenuItem(title);
        item.setFont(Theme.font(Font.PLAIN, 13));
        item.setForeground(Theme.TEXT);
        item.setIcon(Icons.of(iconName, 16, Theme.ACCENT));
        item.setIconTextGap(10);
        item.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 18));
        item.setBackground(Color.WHITE);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addActionListener(e -> action.run());
        return item;
    }

>>>>>>> stash
    private JComponent buildUserBox() {
        JPanel box = new JPanel(new BorderLayout(10, 0));
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(5, 10, 5, 20));
        box.setAlignmentY(Component.CENTER_ALIGNMENT);
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JComponent avatar = new JComponent() {
            { setPreferredSize(new Dimension(32, 32)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0xEEF3F6));
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(Theme.TEXT);
                g2.setFont(Theme.font(Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String s = initials(taiKhoan.getHoTen());
                g2.drawString(s, (32 - fm.stringWidth(s)) / 2, (32 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(taiKhoan.getHoTen());
        n.setFont(Theme.font(Font.BOLD, 12));
        n.setForeground(Color.WHITE);
        JLabel r = new JLabel(taiKhoan.getVaiTro());
        r.setFont(Theme.font(Font.PLAIN, 10));
        r.setForeground(new Color(255, 255, 255, 200));
        text.add(Box.createVerticalGlue());
        text.add(n);
        text.add(r);
        text.add(Box.createVerticalGlue());
        
        box.add(avatar, BorderLayout.WEST);
        box.add(text, BorderLayout.CENTER);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem logout = new JMenuItem("Đăng xuất");
        logout.setFont(Theme.font(Font.PLAIN, 13));
        logout.addActionListener(e -> dangXuat());
        menu.add(logout);
        box.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                menu.show(box, 0, box.getHeight());
            }
        });
        return box;
    }

    private static String initials(String hoTen) {
        String[] w = hoTen.trim().split("\\s+");
        if (w.length == 1) return w[0].substring(0, 1).toUpperCase();
        return (w[0].substring(0, 1) + w[w.length - 1].substring(0, 1)).toUpperCase();
    }

    private void chon(String key) {
        cards.show(content, key);
<<<<<<< HEAD
        navs.forEach((k, b) -> b.setSelected(k.equals(key)));
=======

        // Xác định nút menu cha cần sáng (active)
        String parentMenu;
        if ("Thú cưng".equals(key) || "Khách hàng".equals(key) || "Kho hàng".equals(key)) {
            parentMenu = "Danh Mục";
        } else if ("Lịch sử Nhập / Xuất Kho".equals(key)) {
            parentMenu = "Xử Lý";
        } else if (isLichHenKey(key)) {
            parentMenu = LichHenPanel.PARENT_TITLE;
        } else {
            parentMenu = key;
        }

        navs.forEach((k, b) -> b.setSelected(k.equals(parentMenu)));
>>>>>>> stash
    }

    private static boolean isLichHenKey(String key) {
        for (LichHenPanel.Mode m : LichHenPanel.Mode.values()) if (m.title.equals(key)) return true;
        return false;
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
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Calculate width dynamically
            FontMetrics fm = getFontMetrics(getFont());
            int width = 12 + 20 + 8 + fm.stringWidth(getText()) + 12; // padding + icon + gap + text + padding
            setPreferredSize(new Dimension(width, 34));
            
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            Icons.draw(g2, iconName, 12, (getHeight() - 20) / 2.0, 20, Color.WHITE, false);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 12 + 20 + 8, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
