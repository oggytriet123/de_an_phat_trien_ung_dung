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
            Item.leaf("Khách Hàng", "customers", "Danh sách chủ nuôi", ChucNang.KHACH_HANG),
            Item.leaf("Thú Cưng", "paw", "Hồ sơ thú cưng: giống, tuổi, tiêm chủng", ChucNang.THU_CUNG),
            Item.leaf("Nhân Viên", "person", "Danh sách nhân viên cửa hàng", ChucNang.NHAN_VIEN),
            Item.leaf("Sản Phẩm", "box", "Thức ăn, phụ kiện, tồn kho", ChucNang.SAN_PHAM),
            Item.leaf("Kho Hàng", "box", "Quản lý kho hàng và tồn kho", ChucNang.SAN_PHAM),
            Item.leaf("Danh Mục Bệnh", "list", "Danh mục bệnh và hướng điều trị", ChucNang.DANH_MUC_BENH),
            Item.leaf("Khuyến Mãi", "tag", "Chương trình khuyến mãi và ưu đãi", ChucNang.KHUYEN_MAI)),
        new Item("Xử Lý", "sync", null, null,
            Item.leaf("Lập Hoá Đơn",   "invoice",  "Lập hoá đơn bán hàng và dịch vụ",                  ChucNang.HOA_DON),
            Item.leaf("Lịch Sử Hoá Đơn","calendar","Xem lại các hoá đơn đã thanh toán",                 ChucNang.HOA_DON),
            Item.leaf("Quản Lý Dịch Vụ","service",  "Quản lý dịch vụ, sản phẩm và mã giảm giá",        ChucNang.QUAN_LY_DICH_VU),
            Item.leaf("Tiêm Phòng",    "paw",      "Tiếp nhận và theo dõi lịch tiêm vaccine thú cưng", ChucNang.TIEM_PHONG),
            Item.leaf("Phẫu Thuật",    "service",  "Tiếp nhận và theo dõi tiến độ ca phẫu thuật",      ChucNang.PHAU_THUAT),
            Item.leaf("Lồng Chuồng",   "cage",     "Quản lý lồng, chuồng gửi thú cưng",                ChucNang.CHUONG),
            Item.leaf("Đặt Lịch Hẹn",  "calendar", "Đặt lịch spa, khám, tiêm phòng",                   ChucNang.LICH_HEN),
            new Item("Quản Lý Lịch Hẹn", "calendar", null, null,
                Item.leaf("Hồ sơ lịch hẹn", "card", "Tra cứu toàn bộ hồ sơ lịch hẹn", ChucNang.LICH_HEN),
                Item.leaf("Quản lý lịch hẹn", "calendar", "Tiếp nhận và theo dõi lịch hẹn", ChucNang.LICH_HEN),
                Item.leaf("Đổi lịch hẹn", "sync", "Thay đổi ngày giờ hẹn theo yêu cầu", ChucNang.LICH_HEN),
                Item.leaf("Xác nhận lịch hẹn", "check", "Xác nhận các lịch hẹn đang chờ", ChucNang.LICH_HEN),
                Item.leaf("Hủy lịch hẹn", "close", "Hủy lịch hẹn và ghi nhận lý do", ChucNang.LICH_HEN)),
            Item.leaf("Lịch Sử Nhập / Xuất Kho", "eye", "Lịch sử nhập xuất kho hàng",                   ChucNang.SAN_PHAM)),
        new Item("Điều Trị & Đơn Thuốc", "medical", null, null,
            Item.leaf("Quản lý Đơn thuốc", "prescription", "Kê đơn và quản lý đơn thuốc", ChucNang.DICH_VU),
            Item.leaf("Phác đồ Điều trị", "protocol", "Thiết lập và theo dõi phác đồ điều trị", ChucNang.DICH_VU)),
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
    private QuanLyDichVuPanel dichVuPanel;
    private TiemPhongPanel tiemPhongPanel;
    private PhauThuatPanel phauThuatPanel;

    // Các phân hệ bổ sung từ nhánh ngoc-toan
    private KhoHangPanel pnlKhoHang;
    private LichSuNhapXuatPanel pnlLichSuKho;
    private ThuCungPanel pnlThuCung;
    private KhachHangPanel pnlKhachHang;
    private LichHenPanel pnlHoSoLichHen, pnlQuanLyLichHen, pnlDoiLichHen, pnlXacNhanLichHen, pnlHuyLichHen;
    private DonThuocPanel pnlDonThuoc;
    private PhacDoPanel pnlPhacDo;
    private Bus.DonThuocBUS sharedDonThuocBus;
    private Bus.PhacDoBUS sharedPhacDoBus;

    public MainFrame(TaiKhoan taiKhoan) {
        super("Pet Station");
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
        setSize(1320, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // mở toàn màn hình
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
                    + "• Danh Mục: quản lý khách hàng, thú cưng, nhân viên, sản phẩm, kho hàng, danh mục bệnh, khuyến mãi\n"
                    + "• Xử Lý: lập hoá đơn, lịch sử hoá đơn, dịch vụ, tiêm phòng, phẫu thuật, lồng chuồng, đặt lịch hẹn, lịch sử kho\n"
                    + "• Điều Trị & Đơn Thuốc: quản lý đơn thuốc, phác đồ điều trị\n"
                    + "• Quản Lý Lịch Hẹn: hồ sơ, quản lý, đổi, xác nhận và hủy lịch hẹn\n"
                    + "• Tìm Kiếm: tra cứu nhanh thông tin\n"
                    + "• Thống Kê: báo cáo doanh thu, khách hàng, thú cưng",
                    "Trợ Giúp", JOptionPane.INFORMATION_MESSAGE);
            default -> hienTrang(tab, it);
        }
    }

    public void chon(String tenTrang) {
        if (tenTrang == null) return;
        String t = tenTrang.trim();
        for (Item m : MENU) {
            Item c = timMucLa(m.children, t);
            if (c != null) {
                hienTrang(m.label, c);
                return;
            }
        }
    }

    /** Tìm mục lá theo tên, kể cả trong nhóm con (ví dụ Xử Lý > Quản Lý Lịch Hẹn). */
    private static Item timMucLa(Item[] items, String ten) {
        for (Item c : items) {
            if (c.label == null) continue;
            if (c.children.length > 0) {
                Item r = timMucLa(c.children, ten);
                if (r != null) return r;
            } else if (c.label.equalsIgnoreCase(ten)) {
                return c;
            }
        }
        return null;
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
                if (pnlKhachHang == null) pnlKhachHang = new KhachHangPanel();
                p = pnlKhachHang;
            } else if (it.label.equals("Thú Cưng") || it.label.equals("Tìm Thú Cưng")) {
                if (pnlThuCung == null) pnlThuCung = new ThuCungPanel();
                p = pnlThuCung;
            } else if (it.label.equals("Kho Hàng")) {
                if (pnlKhoHang == null) pnlKhoHang = new KhoHangPanel();
                p = pnlKhoHang;
            } else if (it.label.equals("Lịch Sử Nhập / Xuất Kho")) {
                if (pnlLichSuKho == null) pnlLichSuKho = new LichSuNhapXuatPanel();
                p = pnlLichSuKho;
            } else if (it.label.equals("Quản lý Đơn thuốc")) {
                if (pnlDonThuoc == null) {
                    if (pnlThuCung == null) pnlThuCung = new ThuCungPanel();
                    if (pnlKhoHang == null) pnlKhoHang = new KhoHangPanel();
                    if (sharedDonThuocBus == null) sharedDonThuocBus = new Bus.DonThuocBUS();
                    if (sharedPhacDoBus == null) sharedPhacDoBus = new Bus.PhacDoBUS();
                    pnlDonThuoc = new DonThuocPanel(sharedDonThuocBus, pnlThuCung.getBus(), pnlKhoHang.getBus(), sharedPhacDoBus);
                }
                p = pnlDonThuoc;
            } else if (it.label.equals("Phác đồ Điều trị")) {
                if (pnlPhacDo == null) {
                    if (pnlThuCung == null) pnlThuCung = new ThuCungPanel();
                    if (sharedDonThuocBus == null) sharedDonThuocBus = new Bus.DonThuocBUS();
                    if (sharedPhacDoBus == null) sharedPhacDoBus = new Bus.PhacDoBUS();
                    pnlPhacDo = new PhacDoPanel(sharedPhacDoBus, pnlThuCung.getBus(), sharedDonThuocBus);
                }
                p = pnlPhacDo;
            } else if (it.label.equals("Hồ sơ lịch hẹn")) {
                if (pnlHoSoLichHen == null) pnlHoSoLichHen = new LichHenPanel(LichHenPanel.Mode.HO_SO);
                p = pnlHoSoLichHen;
            } else if (it.label.equals("Quản lý lịch hẹn") || it.label.equals("Đặt Lịch Hẹn")) {
                if (pnlQuanLyLichHen == null) pnlQuanLyLichHen = new LichHenPanel(LichHenPanel.Mode.QUAN_LY);
                p = pnlQuanLyLichHen;
            } else if (it.label.equals("Đổi lịch hẹn")) {
                if (pnlDoiLichHen == null) pnlDoiLichHen = new LichHenPanel(LichHenPanel.Mode.DOI);
                p = pnlDoiLichHen;
            } else if (it.label.equals("Xác nhận lịch hẹn")) {
                if (pnlXacNhanLichHen == null) pnlXacNhanLichHen = new LichHenPanel(LichHenPanel.Mode.XAC_NHAN);
                p = pnlXacNhanLichHen;
            } else if (it.label.equals("Hủy lịch hẹn")) {
                if (pnlHuyLichHen == null) pnlHuyLichHen = new LichHenPanel(LichHenPanel.Mode.HUY);
                p = pnlHuyLichHen;
            } else if (it.label.equals("Nhân Viên") || it.label.equals("Tìm Nhân Viên")) {
                p = new NhanVienPanel(taiKhoan);
            } else if (it.label.equals("Lồng Chuồng")) {
                p = new LongChuongPanel();
            } else if (it.label.equals("Lập Hoá Đơn") || it.label.equals("Lập Hóa Đơn")) {
                if (xuLyPanel == null) {
                    xuLyPanel = new XuLyPanel(hoaDonBus, sanPhamBus, maGiamGiaBus, khachHangBus);
                }
                xuLyPanel.hienQuanLy();
                p = xuLyPanel;
            } else if (it.label.equals("Lịch Sử Hoá Đơn") || it.label.equals("Lịch Sử Hóa Đơn") || it.label.equals("Lịch Sử HĐ") || (tab.equals("Danh Mục") && it.label.equals("Hoá Đơn"))) {
                if (lichSuPanel == null) {
                    lichSuPanel = new LichSuHoaDonPanel(hoaDonBus);
                }
                lichSuPanel.lamMoi();
                p = lichSuPanel;
            } else if (it.label.equals("Quản Lý Dịch Vụ")) {
                if (dichVuPanel == null) {
                    dichVuPanel = new QuanLyDichVuPanel(sanPhamBus, maGiamGiaBus);
                }
                dichVuPanel.lamMoi();
                p = dichVuPanel;
            } else if (it.label.equals("Tiêm Phòng")) {
                if (tiemPhongPanel == null) {
                    tiemPhongPanel = new TiemPhongPanel();
                }
                tiemPhongPanel.lamMoi();
                p = tiemPhongPanel;
            } else if (it.label.equals("Phẫu Thuật")) {
                if (phauThuatPanel == null) {
                    phauThuatPanel = new PhauThuatPanel();
                }
                phauThuatPanel.lamMoi();
                p = phauThuatPanel;
            } else {
                String title = tab.equals("Thống Kê") || tab.equals("Danh Mục") || tab.equals("Xử Lý") || tab.equals("Tìm Kiếm")
                        ? (tab.equals("Thống Kê") ? "Thống Kê " + it.label : it.label) : it.label;
                p = new ModulePlaceholderPanel(title, it.desc == null ? "" : it.desc, it.icon);
            }
            content.add(p, key);
        } else {
            if ((it.label.equals("Lập Hoá Đơn") || it.label.equals("Lập Hóa Đơn")) && xuLyPanel != null) {
                xuLyPanel.hienQuanLy();
            } else if ((it.label.equals("Lịch Sử Hoá Đơn") || it.label.equals("Lịch Sử Hóa Đơn") || it.label.equals("Lịch Sử HĐ") || (tab.equals("Danh Mục") && it.label.equals("Hoá Đơn"))) && lichSuPanel != null) {
                lichSuPanel.lamMoi();
            } else if (it.label.equals("Quản Lý Dịch Vụ") && dichVuPanel != null) {
                dichVuPanel.lamMoi();
            } else if (it.label.equals("Tiêm Phòng") && tiemPhongPanel != null) {
                tiemPhongPanel.lamMoi();
            } else if (it.label.equals("Phẫu Thuật") && phauThuatPanel != null) {
                phauThuatPanel.lamMoi();
            }
        }
        cards.show(content, key);
        navs.forEach((k, b) -> b.setSelected(k.equals(tab)));
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

    public void moKeDonChoThuCung(Entity.ThuCung tc) {
        chon("Quản lý Đơn thuốc");
        if (pnlDonThuoc != null) {
            pnlDonThuoc.keDonChoThuCung(tc);
        }
    }

    public void moPhacDoChoThuCung(Entity.ThuCung tc) {
        chon("Phác đồ Điều trị");
        if (pnlPhacDo != null) {
            pnlPhacDo.taoPhacDoChoThuCung(tc);
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
