package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Bus.HoaDonBUS;
import Bus.KhachHangBUS;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.TaiKhoan;

/** Khung chính: thanh điều hướng bên trái + vùng nội dung (CardLayout). */
public class MainFrame extends JFrame {
    private static final String[][] MENU = {
        // tên hiển thị, icon, mô tả
        {"Hệ Thống", "settings", "Quản lý phiên đăng nhập, mật khẩu và thông tin cá nhân"},
        {"Danh Mục", "dashboard", "Quản lý các danh mục từ điển"},
        {"Xử Lý", "sync", "Xử lý nghiệp vụ"},
        {"Tìm Kiếm", "search", "Tra cứu thông tin"},
        {"Thống Kê", "bar-chart", "Báo cáo thống kê"},
    };

    /** Các mục xổ xuống dưới "Xử Lý". */
    private static final String MUC_QUAN_LY = "Quản lý hóa đơn";
    private static final String MUC_TAO = "Tạo hóa đơn";
    private static final String MUC_LICH_SU = "Lịch sử hóa đơn";
    private static final String[] MENU_XU_LY = {MUC_QUAN_LY, MUC_TAO, MUC_LICH_SU};
    private static final String KEY_XU_LY = "Xử Lý";

    private final TaiKhoan taiKhoan;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, NavButton> navs = new LinkedHashMap<>();
    private final Map<String, JPanel> pages = new LinkedHashMap<>();
    private XuLyPanel xuLy;
    private String hienTai = "";

    public MainFrame(TaiKhoan taiKhoan) {
        super("PetCare POS");
        this.taiKhoan = taiKhoan;
        setIconImage(Icons.appIcon());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { xacNhanThoat(); }
        });

        content.setBackground(Theme.BG);
        // Dùng chung giữa "Xử Lý" (20 ô hóa đơn) và "Lịch sử hóa đơn"
        SanPhamBUS sanPhamBus = new SanPhamBUS();
        HoaDonBUS hoaDonBus = new HoaDonBUS(sanPhamBus);
        for (String[] m : MENU) {
            JPanel p;
            if (m[0].equals("Hệ Thống")) {
                p = new HeThongPanel(taiKhoan, this);
            } else if (m[0].equals("Xử Lý")) {
                xuLy = new XuLyPanel(hoaDonBus, sanPhamBus, new MaGiamGiaBUS(), new KhachHangBUS());
                p = xuLy;
            } else if (m[0].equals("Danh Mục")) {
                p = new HoSoThuCungPanel();
            } else if (m[0].equals("Khách hàng")) {
                p = new KhachHangPanel();
            } else {
                p = new ModulePlaceholderPanel(m[0], m[2], m[1]);
            }
            pages.put(m[0], p);
            content.add(p, m[0]);
        }
        // Trang lịch sử không có nút riêng trên thanh menu: mở từ menu xổ xuống của "Xử Lý"
        LichSuHoaDonPanel lichSu = new LichSuHoaDonPanel(hoaDonBus);
        pages.put(MUC_LICH_SU, lichSu);
        content.add(lichSu, MUC_LICH_SU);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        chon("Hệ Thống"); // màn hình đã hoàn thiện; đổi thành "Tổng quan" nếu muốn mở trang đầu
        setSize(1280, 800);
        setMinimumSize(new Dimension(1180, 700));
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
            boolean xoXuong = m[0].equals(KEY_XU_LY);
            NavButton b = new NavButton(m[0], m[1], xoXuong);
            if (xoXuong) {
                JPopupMenu menu = NavMenu.tao(MENU_XU_LY, this::chonMucXuLy);
                b.addActionListener(e -> {
                    b.setSelected(navCua(hienTai).equals(m[0]));   // mở menu không tự đổi mục đang chọn
                    menu.show(b, 0, b.getHeight() + 6);
                });
            } else {
                b.addActionListener(e -> chon(m[0]));
            }
            navs.put(m[0], b);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(8, 5, 8, 5));
            wrap.setAlignmentY(Component.CENTER_ALIGNMENT);
            wrap.add(b);
            header.add(wrap);
        }
        header.add(Box.createHorizontalGlue());
        header.add(buildUserBox());
        return header;
    }

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
        hienTai = key;
        cards.show(content, key);
        if (pages.get(key) instanceof Refreshable r) r.lamMoi();
        navs.forEach((k, b) -> b.setSelected(k.equals(navCua(key))));
    }

    /** Trang lịch sử hóa đơn thuộc nhóm "Xử Lý" nên vẫn sáng mục "Xử Lý" trên thanh menu. */
    private static String navCua(String key) {
        return key.equals(MUC_LICH_SU) ? KEY_XU_LY : key;
    }

    private void chonMucXuLy(String muc) {
        switch (muc) {
            case MUC_QUAN_LY -> { chon(KEY_XU_LY); xuLy.hienQuanLy(); }
            case MUC_TAO -> { chon(KEY_XU_LY); xuLy.taoMoi(); }
            case MUC_LICH_SU -> chon(MUC_LICH_SU);
            default -> { }
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
        private final boolean xoXuong;
        private boolean hovering;

        NavButton(String text, String iconName, boolean xoXuong) {
            super(text);
            this.iconName = iconName;
            this.xoXuong = xoXuong;
            setFont(Theme.font(Font.PLAIN, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Calculate width dynamically
            FontMetrics fm = getFontMetrics(getFont());
            int width = 12 + 20 + 8 + fm.stringWidth(getText()) + 12 + (xoXuong ? 16 : 0); // padding + icon + gap + text + padding (+ mũi tên)
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
            if (xoXuong) {   // mũi tên ▾ báo hiệu đây là menu xổ xuống
                int cx = getWidth() - 16, cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
            }
            g2.dispose();
        }
    }
}
