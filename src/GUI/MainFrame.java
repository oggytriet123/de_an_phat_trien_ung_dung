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
import Entity.ChucNang;
import Entity.TaiKhoan;

/** Khung chính: thanh menu ngang có combobox (menu xổ xuống) + vùng nội dung (CardLayout). */
public class MainFrame extends JFrame {
    // Màu thanh menu — dùng màu SIDEBAR của Trung_prj (xanh xám đậm) giống thanh bên trái Trung_prj
    private static final Color BAR_BG     = Theme.SIDEBAR;
    private static final Color BAR_ACTIVE = Theme.SIDEBAR_ACTIVE;
    private static final Color BAR_HOVER  = Theme.SIDEBAR_HOVER;
    private static final Color BAR_TEXT   = Color.WHITE;
    private static final Color POPUP_HOVER = new Color(255, 255, 255, 30);

    /** Một mục menu: có thể là lệnh, đường kẻ ngăn cách hoặc menu con. */
    private static class Item {
        final String label, icon, desc, accel;
        final Item[] children;
        String cn; // chức năng (CHUCNANG) cần có để thấy/dùng mục này; null = ai cũng dùng được
        Item(String label, String icon, String desc, String accel, Item... children) {
            this.label = label; this.icon = icon; this.desc = desc; this.accel = accel; this.children = children;
        }
        Item cn(String c) { this.cn = c; return this; }
        static Item sep() { return new Item(null, null, null, null); }
        static Item leaf(String label, String icon, String desc, String cn) {
            return new Item(label, icon, desc, null).cn(cn);
        }
    }

    private static final Item[] MENU = {
        new Item("Hệ Thống", "settings", null, null,
            Item.leaf("Trang Chủ", "home", "Tổng quan hoạt động cửa hàng thú cưng", ChucNang.TRANG_CHU),
            Item.leaf("Tài Khoản", "person", "Quản lý phiên đăng nhập, mật khẩu và thông tin cá nhân", ChucNang.TAI_KHOAN),
            Item.leaf("Trợ Giúp", "help", "Hướng dẫn sử dụng", null),
            Item.sep(),
            new Item("Đăng Xuất", "logout", null, "Ctrl+X").cn(ChucNang.DANG_XUAT),
            new Item("Thoát", "close", null, "Ctrl+O")),
        new Item("Danh Mục", "list", null, null,
            Item.leaf("Hoá Đơn", "invoice", "Danh sách hoá đơn bán hàng và dịch vụ", ChucNang.HOA_DON),
            Item.leaf("Khách Hàng", "customers", "Danh sách chủ nuôi", ChucNang.KHACH_HANG),
            Item.leaf("Thú Cưng", "paw", "Hồ sơ thú cưng: giống, tuổi, tiêm chủng", ChucNang.THU_CUNG),
            Item.leaf("Nhân Viên", "person", "Danh sách nhân viên cửa hàng", ChucNang.NHAN_VIEN),
            Item.leaf("Sản Phẩm", "box", "Thức ăn, phụ kiện, tồn kho", ChucNang.SAN_PHAM),
            Item.leaf("Dịch Vụ", "service", "Spa, grooming, khám bệnh, gửi thú cưng", ChucNang.DICH_VU),
            Item.leaf("Danh Mục Bệnh", "list", "Danh mục bệnh và hướng điều trị", ChucNang.DANH_MUC_BENH),
            Item.leaf("Khuyến Mãi", "tag", "Chương trình khuyến mãi và ưu đãi", ChucNang.KHUYEN_MAI)),
        new Item("Xử Lý", "sync", null, null,
            Item.leaf("Lồng Chuồng",   "cage",     "Quản lý lồng, chuồng gửi thú cưng",                ChucNang.CHUONG),
            Item.leaf("Đặt Lịch Hẹn",  "calendar", "Đặt lịch spa, khám, tiêm phòng",                   ChucNang.LICH_HEN),
            Item.leaf("Lập Hoá Đơn",   "invoice",  "Lập hoá đơn bán hàng và dịch vụ",                  ChucNang.HOA_DON),
            Item.leaf("Lịch Sử HĐ",    "calendar", "Xem lại các hoá đơn đã thanh toán",                 ChucNang.HOA_DON)),
        new Item("Tìm Kiếm", "search", null, null,
            Item.leaf("Tìm Nhân Viên", "person", "Tra cứu nhân viên", ChucNang.TIM_KIEM),
            Item.leaf("Tìm Khách Hàng", "customers", "Tra cứu khách hàng", ChucNang.TIM_KIEM),
            Item.leaf("Tìm Thú Cưng", "paw", "Tra cứu thú cưng", ChucNang.TIM_KIEM),
            Item.leaf("Phiếu Đặt Lịch", "calendar", "Tra cứu phiếu đặt lịch hẹn", ChucNang.TIM_KIEM),
            Item.leaf("Tìm Hoá Đơn", "invoice", "Tra cứu hoá đơn", ChucNang.TIM_KIEM)),
        new Item("Thống Kê", "bar-chart", null, null,
            new Item("Doanh Thu", "bar-chart", null, null,
                Item.leaf("Theo Ngày", "calendar", "Doanh thu và hoá đơn theo từng khung giờ trong ngày.", ChucNang.TK_DOANH_THU),
                Item.leaf("Theo Tháng", "calendar", "Doanh thu và hoá đơn theo từng ngày trong tháng.", ChucNang.TK_DOANH_THU),
                Item.leaf("Theo Năm", "calendar", "Doanh thu và hoá đơn theo từng tháng trong năm.", ChucNang.TK_DOANH_THU)),
            Item.leaf("Tồn Kho & Mặt Hàng", "box", "Thống kê tồn kho, mặt hàng sắp hết", ChucNang.TK_DOANH_THU),
            Item.leaf("Khách Hàng", "customers", "Thống kê khách hàng thân thiết, khách mới", ChucNang.TK_KHACH_HANG),
            Item.leaf("Thú Cưng", "paw", "Thống kê thú cưng theo loài, giống", ChucNang.TK_THU_CUNG)),
    };

    /** Mục (hoặc ít nhất một mục con) mà tài khoản được phép dùng. */
    private boolean choPhep(Item it) {
        if (it.label == null) return true;
        if (it.children.length == 0) return taiKhoan.coQuyen(it.cn);
        for (Item c : it.children) if (c.label != null && choPhep(c)) return true;
        return false;
    }

    /** Lọc bỏ mục không được phép; bỏ đường kẻ thừa ở đầu/cuối/liên tiếp. */
    private java.util.List<Item> loc(Item[] items) {
        java.util.List<Item> out = new java.util.ArrayList<>();
        for (Item it : items) {
            if (it.label == null) {
                if (!out.isEmpty() && out.get(out.size() - 1).label != null) out.add(it);
            } else if (choPhep(it)) {
                out.add(it);
            }
        }
        while (!out.isEmpty() && out.get(out.size() - 1).label == null) out.remove(out.size() - 1);
        return out;
    }

    private final TaiKhoan taiKhoan;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, NavButton> navs = new LinkedHashMap<>();
    private final java.util.Set<String> pages = new java.util.HashSet<>();
    private NavButton openNav; // menu đang xổ xuống

    // Shared BUS dùng chung cho màn hình hóa đơn
    private final KhachHangBUS khachHangBus = new KhachHangBUS();
    private final SanPhamBUS sanPhamBus     = new SanPhamBUS();
    private final HoaDonBUS hoaDonBus       = new HoaDonBUS(khachHangBus);
    private final MaGiamGiaBUS maGiamGiaBus = new MaGiamGiaBUS();

    // Trang Xử Lý (Lập hoá đơn) và Lịch sử hoá đơn — khởi tạo lười để tái sử dụng
    private XuLyPanel xuLyPanel;
    private LichSuHoaDonPanel lichSuPanel;
    private ThongKePanel pnlThongKe;

    public MainFrame(TaiKhoan taiKhoan) {
        super("PetCare POS");
        this.taiKhoan = taiKhoan;
        setIconImage(Icons.appIcon());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { xacNhanThoat(); }
        });

        content.setBackground(Theme.BG);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        // Phím tắt hiển thị trong menu Hệ Thống
        bindKey(root, "ctrl X", () -> { if (taiKhoan.coQuyen(ChucNang.DANG_XUAT)) dangXuat(); });
        bindKey(root, "ctrl O", this::xacNhanThoat);

        hienTrang("Hệ Thống", MENU[0].children[0]); // Trang Chủ: mọi nhóm quyền đều có
        setSize(1280, 800);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private static void bindKey(JComponent c, String stroke, Runnable r) {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke), stroke);
        c.getActionMap().put(stroke, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { r.run(); }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(BAR_BG);
        header.setPreferredSize(new Dimension(0, 56));
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        // Thương hiệu
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(10, 20, 10, 10));
        brand.setAlignmentY(Component.CENTER_ALIGNMENT);
        JLabel logo = new JLabel(Icons.of("logo", 24, Color.WHITE));
        JLabel name = new JLabel("Pet Station");
        name.setFont(Theme.font(Font.BOLD, 18));
        name.setForeground(BAR_TEXT);
        brand.add(logo);
        brand.add(name);
        header.add(brand);
        header.add(Box.createHorizontalStrut(10));

        // Menu có combobox xổ xuống
        for (Item m : MENU) {
            if (!choPhep(m)) continue;
            NavButton b = new NavButton(m);
            navs.put(m.label, b);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(0, 0, 0, 0));
            wrap.setAlignmentY(Component.CENTER_ALIGNMENT);
            wrap.add(b);
            header.add(wrap);
        }
        header.add(Box.createHorizontalGlue());
        header.add(buildUserBox());
        return header;
    }

    /** Dựng popup xổ xuống (hỗ trợ menu con) cho một mục menu cha. */
    private JPopupMenu buildPopup(Item parent, String tab) {
        JPopupMenu pm = new JPopupMenu();
        pm.setBackground(new Color(0xFAFAFA));
        pm.setBorder(BorderFactory.createLineBorder(new Color(0xB5B5B5)));
        fill(pm, parent.children, tab);
        return pm;
    }

    private void fill(JComponent target, Item[] items, String tab) {
        for (Item it : loc(items)) {
            if (it.label == null) {
                JSeparator sp = new JSeparator();
                sp.setForeground(new Color(0xD0D0D0));
                target.add(sp);
                continue;
            }
            JMenuItem mi;
            if (it.children.length > 0) {
                JMenu sub = new JMenu(it.label);
                fill(sub, it.children, tab);
                sub.getPopupMenu().setBorder(BorderFactory.createLineBorder(new Color(0xB5B5B5)));
                mi = sub;
            } else {
                mi = new JMenuItem(it.label);
                if (it.accel != null) mi.setAccelerator(KeyStroke.getKeyStroke(it.accel.replace("Ctrl+", "ctrl ")));
                mi.addActionListener(e -> chonMuc(tab, it));
            }
            mi.setIcon(Icons.colored(it.icon, 20));
            mi.setIconTextGap(14);
            mi.setFont(Theme.font(Font.PLAIN, 14));
            mi.setForeground(Theme.TEXT);
            mi.setOpaque(true);
            mi.setBackground(new Color(0xFAFAFA));
            mi.setBorder(new EmptyBorder(8, 18, 8, 22));
            mi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            target.add(mi);
        }
    }

    private void chonMuc(String tab, Item it) {
        switch (it.label) {
            case "Đăng Xuất" -> dangXuat();
            case "Thoát" -> xacNhanThoat();
            case "Trợ Giúp" -> JOptionPane.showMessageDialog(this,
                    "Pet Station - Hệ thống quản lý cửa hàng thú cưng\n\n"
                    + "• Danh Mục: quản lý khách hàng, thú cưng, nhân viên, dịch vụ, khuyến mãi\n"
                    + "• Xử Lý: lồng chuồng, đặt lịch hẹn, lập hoá đơn\n"
                    + "• Tìm Kiếm: tra cứu nhanh thông tin\n"
                    + "• Thống Kê: báo cáo doanh thu, khách hàng, thú cưng",
                    "Trợ Giúp", JOptionPane.INFORMATION_MESSAGE);
            default -> hienTrang(tab, it);
        }
    }

    /** Hiển thị trang tương ứng mục menu (tạo lần đầu khi cần). */
    private void hienTrang(String tab, Item it) {
        if (!taiKhoan.coQuyen(it.cn)) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền sử dụng chức năng \"" + it.label + "\".",
                    "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String key = tab + "/" + it.label;
        if (pages.add(key)) {
            JPanel p;
            if (tab.equals("Hệ Thống") && it.label.equals("Tài Khoản")) {
                p = new TaiKhoanPanel(taiKhoan);
            } else if (tab.equals("Hệ Thống")) {
                p = new HeThongPanel(taiKhoan, this);
            } else if (tab.equals("Danh Mục") && it.label.equals("Khách Hàng")) {
                p = new KhachHangPanel();
            } else if (it.label.equals("Thú Cưng") || it.label.equals("Tìm Thú Cưng")) {
                p = new ThuCungPanel();
            } else if (it.label.equals("Nhân Viên") || it.label.equals("Tìm Nhân Viên")) {
                p = new NhanVienPanel(taiKhoan);
            } else if (it.label.equals("Dịch Vụ")) {
                p = new DichVuPanel();
            } else if (it.label.equals("Lồng Chuồng")) {
                p = new LongChuongPanel();
            } else if (it.label.equals("Lập Hoá Đơn")) {
                if (xuLyPanel == null) {
                    xuLyPanel = new XuLyPanel(hoaDonBus, sanPhamBus, maGiamGiaBus, khachHangBus);
                }
                xuLyPanel.hienQuanLy();
                p = xuLyPanel;
            } else if (it.label.equals("Lịch Sử HĐ") || (tab.equals("Danh Mục") && it.label.equals("Hoá Đơn"))) {
                if (lichSuPanel == null) {
                    lichSuPanel = new LichSuHoaDonPanel(hoaDonBus);
                }
                lichSuPanel.lamMoi();
                p = lichSuPanel;
            } else if (tab.equals("Thống Kê")) {
                if (pnlThongKe == null) {
                    pnlThongKe = new ThongKePanel();
                }
                if ("Theo Ngày".equals(it.label) || "Theo Tháng".equals(it.label) || "Theo Năm".equals(it.label) || "Doanh Thu".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Doanh thu & Dòng tiền");
                } else if ("Khách Hàng".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Khách hàng & Thú cưng");
                } else if ("Thú Cưng".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Khách hàng & Thú cưng");
                } else if ("Tồn Kho & Mặt Hàng".equalsIgnoreCase(it.label) || "Tồn Kho".equalsIgnoreCase(it.label) || "Sản Phẩm".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Tồn kho & Mặt hàng");
                }
                p = pnlThongKe;
            } else if (it.label.equals("Sản Phẩm") && tab.equals("Danh Mục")) {
                p = new KhoHangPanel();
            } else if (it.label.equals("Lịch sử Nhập / Xuất Kho")) {
                p = new LichSuNhapXuatPanel();
            } else {
                String title = tab.equals("Thống Kê") || tab.equals("Danh Mục") || tab.equals("Xử Lý") || tab.equals("Tìm Kiếm")
                        ? (tab.equals("Thống Kê") ? "Thống Kê " + it.label : it.label) : it.label;
                p = new ModulePlaceholderPanel(title, it.desc == null ? "" : it.desc, it.icon);
            }
            content.add(p, key);
        } else {
            if (tab.equals("Thống Kê") && pnlThongKe != null) {
                if ("Theo Ngày".equals(it.label) || "Theo Tháng".equals(it.label) || "Theo Năm".equals(it.label) || "Doanh Thu".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Doanh thu & Dòng tiền");
                } else if ("Khách Hàng".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Khách hàng & Thú cưng");
                } else if ("Thú Cưng".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Khách hàng & Thú cưng");
                } else if ("Tồn Kho & Mặt Hàng".equalsIgnoreCase(it.label) || "Tồn Kho".equalsIgnoreCase(it.label) || "Sản Phẩm".equalsIgnoreCase(it.label)) {
                    pnlThongKe.chonTab("Tồn kho & Mặt hàng");
                }
            } else if (it.label.equals("Lập Hoá Đơn") && xuLyPanel != null) {
                xuLyPanel.hienQuanLy();
            } else if ((it.label.equals("Lịch Sử HĐ") || (tab.equals("Danh Mục") && it.label.equals("Hoá Đơn"))) && lichSuPanel != null) {
                lichSuPanel.lamMoi();
            }
        }
        cards.show(content, key);
        navs.forEach((k, b) -> b.setSelected(k.equals(tab)));
    }

    public void chon(String key) {
        if (key == null) return;
        for (Item top : MENU) {
            for (Item sub : top.children) {
                if (sub.label != null && sub.label.equalsIgnoreCase(key)) {
                    hienTrang(top.label, sub);
                    return;
                }
                for (Item child : sub.children) {
                    if (child.label != null && child.label.equalsIgnoreCase(key)) {
                        hienTrang(top.label, child);
                        return;
                    }
                }
            }
        }
        if (key.equalsIgnoreCase("Thống Kê")) {
            if (pnlThongKe == null) pnlThongKe = new ThongKePanel();
            pnlThongKe.refresh();
            content.add(pnlThongKe, "Thống Kê");
            cards.show(content, "Thống Kê");
            navs.forEach((k, b) -> b.setSelected(k.equals("Thống Kê")));
            return;
        }
        if (key.equalsIgnoreCase("Kho Hàng") || key.equalsIgnoreCase("Kho hàng")) {
            String k = "Danh Mục/Kho Hàng";
            if (pages.add(k)) content.add(new KhoHangPanel(), k);
            cards.show(content, k);
            navs.forEach((t, b) -> b.setSelected(t.equals("Danh Mục")));
            return;
        }
        if (key.equalsIgnoreCase("Lịch sử Nhập / Xuất Kho")) {
            String k = "Xử Lý/Lịch sử Nhập / Xuất Kho";
            if (pages.add(k)) content.add(new LichSuNhapXuatPanel(), k);
            cards.show(content, k);
            navs.forEach((t, b) -> b.setSelected(t.equals("Xử Lý")));
            return;
        }
        cards.show(content, key);
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


    /** Mở trang Tài Khoản (thông tin cá nhân). */
    public void moTaiKhoan() { hienTrang("Hệ Thống", MENU[0].children[1]); }

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

    /** Nút menu ngang: icon màu + chữ đậm; bấm / rê chuột để xổ combobox. */
    private class NavButton extends JToggleButton {
        private final Item item;
        private final JPopupMenu popup;
        private boolean hovering;

        NavButton(Item item) {
            super(item.label);
            this.item = item;
            this.popup = buildPopup(item, item.label);
            setFont(Theme.font(Font.PLAIN, 14));
            setForeground(BAR_TEXT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            FontMetrics fm = getFontMetrics(getFont());
            int width = 12 + 20 + 8 + fm.stringWidth(getText()) + 18;
            setPreferredSize(new Dimension(width, 56));

            popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) { openNav = NavButton.this; repaint(); }
                @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                    if (openNav == NavButton.this) openNav = null; repaint();
                }
                @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
            });

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    hovering = true; repaint();
                    if (openNav != null && openNav != NavButton.this) { // đang xổ menu khác -> chuyển sang menu này
                        openNav.popup.setVisible(false);
                        showPopup();
                    }
                }
                @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) {
                    if (popup.isVisible()) popup.setVisible(false); else showPopup();
                }
            });
        }

        void showPopup() { popup.show(this, 0, getHeight() + 1); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (popup.isVisible() || isSelected() || hovering) {
                Color bg = popup.isVisible() || isSelected() ? new Color(255,255,255,60) : new Color(255,255,255,30);
                g2.setColor(bg);
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 8, 8);
            }
            Icons.draw(g2, item.icon, 12, (getHeight() - 20) / 2.0, 20, Color.WHITE, false);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 12 + 20 + 8, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
