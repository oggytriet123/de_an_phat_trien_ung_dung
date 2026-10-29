package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.ChuoiUtil;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.MaGiamGia;
import Entity.SanPham;

/**
 * Menu Xử Lý > Quản lý dịch vụ. Ba tab: Dịch vụ, Sản phẩm, Mã giảm giá.
 * Mỗi tab có thống kê, tìm kiếm, lọc theo nhóm, sắp xếp, phân trang, thêm / sửa / xóa, xem chi tiết và xuất Excel.
 */
public class QuanLyDichVuPanel extends JPanel implements Refreshable {
    private enum Tab { DICH_VU, SAN_PHAM, MA_GIAM_GIA }

    private static final int PAGE_SIZE = 7;
    private static final Color HEADER_BG = new Color(0xE9EFF4);
    private static final Color ALT_BG = new Color(0xF6F9FB);
    private static final Color GRID = new Color(0xDCE3E8);
    private static final Color SEL_BG = new Color(0xDCE8FA);
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY_TEP = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Collator VI = Collator.getInstance(java.util.Locale.forLanguageTag("vi"));

    /** Một cột của bảng: tiêu đề, cách lấy chữ hiển thị, căn lề, bề rộng. */
    private record Cot(String tieuDe, Function<Object, String> gt, int canh, int rong) { }

    private final SanPhamBUS spBus;
    private final MaGiamGiaBUS mgBus;
    private Tab tab = Tab.DICH_VU;
    private String nhom = SanPhamBUS.TAT_CA;
    private String tuKhoa = "";
    private int sapXep = 0;
    private int trang = 1;
    private List<Object> ketQua = new ArrayList<>();
    private List<Cot> cot = new ArrayList<>();

    private final JLabel lblTitle = new JLabel();
    private final JLabel lblSub = new JLabel();
    private final JLabel lblTb = new JLabel(" ");
    private Timer hen;
    private final RoundedButton btnExcel = nutPhu("Xuất Excel", Theme.TEXT);
    private final RoundedButton btnXem = nutPhu("Xem chi tiết", Theme.TEXT);
    private final RoundedButton btnSua = nutPhu("Chỉnh sửa", Theme.TEXT);
    private final RoundedButton btnXoa = nutPhu("Xóa", Theme.DANGER);
    private final RoundedButton btnThem = new RoundedButton("+ Thêm dịch vụ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 10);
    private final List<Pill> tabPills = new ArrayList<>();
    private final JPanel statRow = new JPanel(new GridLayout(1, 4, 16, 0));
    private final PlaceholderField tfTim = new PlaceholderField("");
    private final WrapPanel chips = new WrapPanel(8, 8, 520);
    private final RoundedButton btnSapXep = new RoundedButton("", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
    private final RowModel model = new RowModel();
    private final JTable table = new JTable(model);
    private final CardLayout tableCards = new CardLayout();
    private final JPanel tableArea = new JPanel(tableCards);
    private final JLabel lblTrong = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblFooter = new JLabel(" ");
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public QuanLyDichVuPanel(SanPhamBUS spBus, MaGiamGiaBUS mgBus) {
        super(new BorderLayout(0, 14));
        this.spBus = spBus;
        this.mgBus = mgBus;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 24, 18, 24));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JComponent h = buildHeader(), t = buildTabs();
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        statRow.setOpaque(false);
        statRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(h);
        top.add(Box.createVerticalStrut(12));
        top.add(t);
        top.add(Box.createVerticalStrut(12));
        top.add(statRow);
        add(top, BorderLayout.NORTH);
        add(buildCard(), BorderLayout.CENTER);

        chuyenTab(Tab.DICH_VU);
    }

    @Override public void lamMoi() {
        refresh();
    }

    // =====================================================================
    // Dựng giao diện
    // =====================================================================
    private static RoundedButton nutPhu(String text, Color mau) {
        return new RoundedButton(text, Color.WHITE, Theme.FIELD_BG, mau, Theme.BORDER, 8).pad(18, 10);
    }

    private JComponent buildHeader() {
        lblTitle.setFont(Theme.font(Font.BOLD, 26));
        lblTitle.setForeground(Theme.TEXT);
        lblSub.setFont(Theme.font(Font.PLAIN, 13));
        lblSub.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblSub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        for (RoundedButton b : new RoundedButton[] {btnExcel, btnXem, btnSua, btnXoa, btnThem}) {
            b.setFont(Theme.font(Font.BOLD, 13));
            right.add(b);
        }
        right.setBorder(new EmptyBorder(0, 0, 0, -10));
        btnExcel.addActionListener(e -> xuatExcel());
        btnXem.addActionListener(e -> xemChiTiet());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnThem.addActionListener(e -> them());

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent buildTabs() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pills.setOpaque(false);
        pills.setBorder(new EmptyBorder(0, -8, 0, 0));
        String[] ten = {"Dịch vụ", "Sản phẩm", "Mã giảm giá"};
        Tab[] tabs = Tab.values();
        for (int i = 0; i < ten.length; i++) {
            Pill p = new Pill(ten[i], Theme.ACCENT, Color.WHITE, 8);
            Tab t = tabs[i];
            p.addActionListener(e -> chuyenTab(t));
            tabPills.add(p);
            pills.add(p);
        }
        lblTb.setFont(Theme.font(Font.BOLD, 12));
        lblTb.setForeground(Theme.SUCCESS);
        row.add(pills, BorderLayout.WEST);
        row.add(lblTb, BorderLayout.EAST);
        return row;
    }

    private JComponent buildCard() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());

        // Hàng tìm kiếm + nhóm + sắp xếp
        JLabel ic = new JLabel(Icons.of("search", 16, new Color(0x8795A0)));
        ic.setBorder(new EmptyBorder(0, 12, 0, 0));
        FieldBox tim = new FieldBox(tfTim, Color.WHITE, 38);
        tim.add(ic, BorderLayout.WEST);
        tim.setPreferredSize(new Dimension(330, 38));
        tfTim.addActionListener(e -> { tuKhoa = tfTim.getText().trim(); trang = 1; refresh(); });
        tfTim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void c() { tuKhoa = tfTim.getText().trim(); trang = 1; refresh(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { c(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { c(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { }
        });
        btnSapXep.setFont(Theme.font(Font.PLAIN, 13));
        btnSapXep.setPreferredSize(new Dimension(190, 38));
        btnSapXep.addActionListener(e -> { sapXep = (sapXep + 1) % tenSapXep().length; trang = 1; refresh(); });

        JPanel chipWrap = new JPanel(new BorderLayout());
        chipWrap.setOpaque(false);
        chipWrap.setBorder(new EmptyBorder(3, 14, 0, 14));
        chipWrap.add(chips, BorderLayout.CENTER);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(16, 18, 14, 18));
        JPanel timWrap = new JPanel(new BorderLayout());
        timWrap.setOpaque(false);
        timWrap.add(tim, BorderLayout.NORTH);
        bar.add(timWrap, BorderLayout.WEST);
        bar.add(chipWrap, BorderLayout.CENTER);
        JPanel sx = new JPanel(new BorderLayout());
        sx.setOpaque(false);
        sx.add(btnSapXep, BorderLayout.NORTH);
        bar.add(sx, BorderLayout.EAST);
        card.add(bar, BorderLayout.NORTH);

        // Bảng
        table.setRowHeight(46);
        table.setShowGrid(true);
        table.setGridColor(GRID);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setFocusable(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader hd = table.getTableHeader();
        hd.setReorderingAllowed(false);
        hd.setResizingAllowed(false);
        hd.setPreferredSize(new Dimension(0, 42));
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0) xemChiTiet();
            }
        });
        JScrollPane sp = new JScrollPane(table);
        sp.setColumnHeaderView(table.getTableHeader());
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        SlimScrollBarUI.apply(sp);

        tableArea.setOpaque(false);
        tableArea.setBorder(new MatteBorder(1, 0, 1, 0, GRID));
        tableArea.add(sp, "table");
        lblTrong.setFont(Theme.font(Font.PLAIN, 13));
        lblTrong.setForeground(Theme.MUTED);
        tableArea.add(lblTrong, "empty");
        card.add(tableArea, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(12, 18, 14, 18));
        lblFooter.setFont(Theme.font(Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);
        pnlPages.setOpaque(false);
        foot.add(lblFooter, BorderLayout.WEST);
        foot.add(pnlPages, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // Chuyển tab / dữ liệu
    // =====================================================================
    private boolean laDichVu() { return tab == Tab.DICH_VU; }
    private boolean laHang() { return tab != Tab.MA_GIAM_GIA; }

    private void chuyenTab(Tab t) {
        tab = t;
        lblTb.setText(" ");
        nhom = SanPhamBUS.TAT_CA;
        tuKhoa = "";
        tfTim.setText("");
        sapXep = 0;
        trang = 1;

        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabPills.size(); i++) tabPills.get(i).setSelected(tabs[i] == t);
        switch (t) {
            case DICH_VU -> {
                lblTitle.setText("Quản lý Dịch Vụ");
                lblSub.setText("Danh mục dịch vụ chăm sóc, khám chữa bệnh, tiêm phòng, phẫu thuật, bảng giá niêm yết");
                btnThem.setText("+ Thêm dịch vụ");
                tfTim.setPlaceholder("Tìm tên dịch vụ, mã DV, nhóm, đối tượng...");
            }
            case SAN_PHAM -> {
                lblTitle.setText("Quản lý Sản Phẩm");
                lblSub.setText("Danh mục hàng hóa bán tại quầy: thức ăn, đồ chơi, thuốc và các loại hàng khác");
                btnThem.setText("+ Thêm sản phẩm");
                tfTim.setPlaceholder("Tìm tên sản phẩm, mã SP, loại hàng...");
            }
            default -> {
                lblTitle.setText("Quản lý Mã Giảm Giá");
                lblSub.setText("Các mã khuyến mãi áp dụng khi lập hóa đơn");
                btnThem.setText("+ Thêm mã giảm giá");
                tfTim.setPlaceholder("Tìm mã giảm giá...");
            }
        }
        btnXem.setVisible(true);
        btnSua.setVisible(laHang());
        cot = cotCua(t);
        model.fireTableStructureChanged();
        ganCot();
        dungChips();
        refresh();
    }

    private List<Cot> cotCua(Tab t) {
        List<Cot> c = new ArrayList<>();
        Function<Object, SanPham> sp = o -> (SanPham) o;
        Function<Object, MaGiamGia> mg = o -> (MaGiamGia) o;
        switch (t) {
            case DICH_VU -> {
                c.add(new Cot("Mã DV", o -> sp.apply(o).getSku(), SwingConstants.CENTER, 90));
                c.add(new Cot("Tên dịch vụ", o -> sp.apply(o).getTen(), SwingConstants.LEFT, 230));
                c.add(new Cot("Nhóm dịch vụ", o -> sp.apply(o).getDanhMuc(), SwingConstants.LEFT, 170));
                c.add(new Cot("Đơn giá", o -> Theme.money(sp.apply(o).getDonGia()), SwingConstants.RIGHT, 110));
                c.add(new Cot("Thời lượng", o -> sp.apply(o).getThoiLuong(), SwingConstants.CENTER, 100));
                c.add(new Cot("Đối tượng", o -> sp.apply(o).getDoiTuong(), SwingConstants.LEFT, 140));
                c.add(new Cot("Trạng thái", o -> DichVuPopups.nhanTrangThai(true, sp.apply(o).isDangMo()), SwingConstants.CENTER, 130));
            }
            case SAN_PHAM -> {
                c.add(new Cot("Mã SP", o -> sp.apply(o).getSku(), SwingConstants.CENTER, 100));
                c.add(new Cot("Tên sản phẩm", o -> sp.apply(o).getTen(), SwingConstants.LEFT, 280));
                c.add(new Cot("Loại hàng", o -> sp.apply(o).getDanhMuc(), SwingConstants.LEFT, 180));
                c.add(new Cot("Đơn giá", o -> Theme.money(sp.apply(o).getDonGia()), SwingConstants.RIGHT, 130));
                c.add(new Cot("Trạng thái", o -> DichVuPopups.nhanTrangThai(false, sp.apply(o).isDangMo()), SwingConstants.CENTER, 130));
            }
            default -> {
                c.add(new Cot("Mã giảm giá", o -> mg.apply(o).getMa(), SwingConstants.CENTER, 140));
                c.add(new Cot("Loại giảm", o -> mg.apply(o).isPhanTram() ? "Giảm theo %" : "Giảm theo số tiền", SwingConstants.LEFT, 170));
                c.add(new Cot("Mức giảm", o -> mucGiam(mg.apply(o)), SwingConstants.RIGHT, 130));
                c.add(new Cot("Hạn dùng", o -> NGAY.format(mg.apply(o).getHanDung()), SwingConstants.CENTER, 130));
                c.add(new Cot("Trạng thái", o -> trangThaiMa(mg.apply(o)), SwingConstants.CENTER, 120));
            }
        }
        return c;
    }

    private static String mucGiam(MaGiamGia m) {
        return m.isPhanTram() ? m.getGiaTri() + "%" : Theme.money(m.getGiaTri());
    }

    private String trangThaiMa(MaGiamGia m) {
        return mgBus.daHetHan(m) ? "Hết hạn" : "Còn hạn";
    }

    private void ganCot() {
        for (int i = 0; i < cot.size() && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(cot.get(i).rong());
            table.getColumnModel().getColumn(i).setCellRenderer(new CellR());
            table.getColumnModel().getColumn(i).setHeaderRenderer(new HeaderR());
        }
    }

    private String[] tenSapXep() {
        return laHang() ? new String[] {"Mới nhất", "Tên A-Z", "Giá thấp → cao", "Giá cao → thấp"}
                        : new String[] {"Mới nhất", "Mã A-Z", "Hạn dùng gần nhất"};
    }

    private void dungChips() {
        chips.removeAll();
        List<String> ds = new ArrayList<>();
        ds.add(SanPhamBUS.TAT_CA);
        if (laHang()) ds.addAll(spBus.layNhom(laDichVu()));
        else { ds.add("Còn hạn"); ds.add("Hết hạn"); }
        if (!ds.contains(nhom)) nhom = SanPhamBUS.TAT_CA;
        for (String ten : ds) {
            Pill p = new Pill(ten, Theme.ACCENT, Color.WHITE, 8);
            p.setSelected(ten.equals(nhom));
            p.addActionListener(e -> {
                nhom = ten;
                trang = 1;
                for (Component c : chips.getComponents()) if (c instanceof Pill q) q.setSelected(q.getText().equals(nhom));
                refresh();
            });
            chips.add(p);
        }
        chips.revalidate();
        chips.repaint();
    }

    private void tai() {
        List<Object> r = new ArrayList<>();
        if (laHang()) {
            List<SanPham> l = new ArrayList<>(spBus.locQuanLy(laDichVu(), nhom, tuKhoa));
            Comparator<SanPham> cmp = switch (sapXep) {
                case 1 -> Comparator.comparing(SanPham::getTen, VI);
                case 2 -> Comparator.comparingLong(SanPham::getDonGia);
                case 3 -> Comparator.comparingLong(SanPham::getDonGia).reversed();
                default -> Comparator.comparingInt(SanPham::getStt).reversed();
            };
            l.sort(cmp);
            r.addAll(l);
        } else {
            String kw = ChuoiUtil.chuanHoa(tuKhoa);
            List<MaGiamGia> goc = mgBus.layDanhSach();
            List<MaGiamGia> l = new ArrayList<>();
            for (MaGiamGia m : goc) {
                boolean het = mgBus.daHetHan(m);
                if (nhom.equals("Còn hạn") && het) continue;
                if (nhom.equals("Hết hạn") && !het) continue;
                if (!kw.isEmpty() && !ChuoiUtil.chuanHoa(m.getMa()).contains(kw)) continue;
                l.add(m);
            }
            if (sapXep == 0) java.util.Collections.reverse(l);
            else if (sapXep == 1) l.sort(Comparator.comparing(MaGiamGia::getMa));
            else l.sort(Comparator.comparing(MaGiamGia::getHanDung));
            r.addAll(l);
        }
        ketQua = r;
    }

    private String donVi() {
        return laDichVu() ? "dịch vụ" : laHang() ? "sản phẩm" : "mã giảm giá";
    }

    private void refresh() {
        tai();
        btnSapXep.setText("Sắp xếp: " + tenSapXep()[Math.min(sapXep, tenSapXep().length - 1)]);
        dungThongKe();

        int pages = Math.max(1, (int) Math.ceil(ketQua.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, ketQua.size());
        model.setRows(ketQua.subList(from, to));
        if (ketQua.isEmpty()) {
            boolean coLoc = !tuKhoa.isEmpty() || !nhom.equals(SanPhamBUS.TAT_CA);
            lblTrong.setText(coLoc ? "Không tìm thấy kết quả phù hợp." : "Chưa có " + donVi() + " nào. Bấm \"" + btnThem.getText() + "\" để thêm.");
            tableCards.show(tableArea, "empty");
            lblFooter.setText("Hiển thị 0 " + donVi());
        } else {
            tableCards.show(tableArea, "table");
            lblFooter.setText("Hiển thị " + (from + 1) + "-" + to + " trong " + ketQua.size() + " " + donVi());
        }
        dungPager(ketQua.isEmpty() ? 0 : pages);
        revalidate();
        repaint();
    }

    // =====================================================================
    // Thống kê + phân trang
    // =====================================================================
    private void dungThongKe() {
        statRow.removeAll();
        if (laHang()) {
            boolean dv = laDichVu();
            List<SanPham> tatCa = spBus.locQuanLy(dv, SanPhamBUS.TAT_CA, "");
            long dangMo = tatCa.stream().filter(SanPham::isDangMo).count();
            long tong = tatCa.stream().mapToLong(SanPham::getDonGia).sum();
            long tb = tatCa.isEmpty() ? 0 : Math.round(tong / (double) tatCa.size());
            List<String> nhoms = new ArrayList<>(spBus.layNhom(dv));
            nhoms.sort(Comparator.comparingLong((String n) -> -tatCa.stream().filter(s -> s.getDanhMuc().equals(n)).count()));
            statRow.add(theThongKe("Tổng số " + donVi(), String.valueOf(tatCa.size()), dangMo + " " + donVi() + (dv ? " đang mở" : " đang bán"), Theme.SUCCESS));
            Color[] mau = {new Color(0xC2185B), new Color(0x0E7C86)};
            for (int i = 0; i < 2; i++) {
                if (i < nhoms.size()) {
                    String n = nhoms.get(i);
                    long dem = tatCa.stream().filter(s -> s.getDanhMuc().equals(n)).count();
                    long pt = tatCa.isEmpty() ? 0 : Math.round(dem * 100.0 / tatCa.size());
                    statRow.add(theThongKe(n, String.valueOf(dem), "Chiếm " + pt + "% tổng số", mau[i]));
                } else {
                    statRow.add(theThongKe(i == 0 ? "Nhóm lớn nhất" : "Nhóm lớn thứ hai", "-", "Chưa có nhóm", Theme.MUTED));
                }
            }
            statRow.add(theThongKe("Đơn giá trung bình", Theme.money(tb), "Mức giá trung bình", Theme.AMBER_TEXT));
        } else {
            List<MaGiamGia> ds = mgBus.layDanhSach();
            long con = ds.stream().filter(m -> !mgBus.daHetHan(m)).count();
            long pt = ds.stream().filter(MaGiamGia::isPhanTram).count();
            statRow.add(theThongKe("Tổng số mã", String.valueOf(ds.size()), con + " mã dùng được", Theme.SUCCESS));
            statRow.add(theThongKe("Còn hạn", String.valueOf(con), "Áp dụng được ngay", new Color(0x0E7C86)));
            statRow.add(theThongKe("Hết hạn", String.valueOf(ds.size() - con), "Không áp dụng được", Theme.DANGER));
            statRow.add(theThongKe("Giảm theo %", String.valueOf(pt), "Giảm theo số tiền: " + (ds.size() - pt), Theme.AMBER_TEXT));
        }
        statRow.revalidate();
        statRow.repaint();
    }

    private static JComponent theThongKe(String nhan, String giaTri, String phu, Color mauPhu) {
        RoundedPanel c = new RoundedPanel(12, Theme.CARD, null);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel a = new JLabel(nhan);
        a.setFont(Theme.font(Font.PLAIN, 12));
        a.setForeground(Theme.MUTED);
        JLabel b = new JLabel(giaTri);
        b.setFont(Theme.font(Font.BOLD, 22));
        b.setForeground(Theme.TEXT);
        JLabel d = new JLabel(phu);
        d.setFont(Theme.font(Font.PLAIN, 11));
        d.setForeground(mauPhu);
        for (JLabel l : new JLabel[] {a, b, d}) l.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(a);
        c.add(Box.createVerticalStrut(4));
        c.add(b);
        c.add(Box.createVerticalStrut(4));
        c.add(d);
        return c;
    }

    private static List<Integer> pageList(int totalPages, int current) {
        List<Integer> list = new ArrayList<>();
        if (totalPages <= 7) {
            for (int i = 1; i <= totalPages; i++) list.add(i);
        } else {
            list.add(1);
            if (current > 4) list.add(-1);
            int start = Math.max(2, current - 1);
            int end = Math.min(totalPages - 1, current + 1);
            for (int i = start; i <= end; i++) list.add(i);
            if (current < totalPages - 3) list.add(-1);
            list.add(totalPages);
        }
        return list;
    }

    private void dungPager(int pages) {
        pnlPages.removeAll();
        pnlPages.add(nutTrang("<", trang - 1, pages > 0 && trang > 1, false));
        for (int p : pageList(pages, trang)) {
            if (p < 0) {
                RoundedButton d = new RoundedButton("...", Color.WHITE, Color.WHITE, Theme.MUTED, Theme.BORDER, 8);
                d.setEnabled(false);
                d.setPreferredSize(new Dimension(34, 34));
                pnlPages.add(d);
            } else {
                pnlPages.add(nutTrang(String.valueOf(p), p, true, p == trang));
            }
        }
        pnlPages.add(nutTrang(">", trang + 1, pages > 0 && trang < pages, false));
        pnlPages.revalidate();
        pnlPages.repaint();
    }

    private RoundedButton nutTrang(String text, int toi, boolean batDuoc, boolean dangChon) {
        RoundedButton b = dangChon
                ? new RoundedButton(text, Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8)
                : new RoundedButton(text, Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        b.setFont(Theme.font(Font.PLAIN, 13));
        b.setPreferredSize(new Dimension(34, 34));
        b.setEnabled(batDuoc);
        b.addActionListener(e -> { trang = toi; refresh(); });
        return b;
    }

    // =====================================================================
    // Thao tác
    // =====================================================================
    private Object dongChon() {
        int r = table.getSelectedRow();
        return r < 0 || r >= model.rows.size() ? null : model.rows.get(r);
    }

    private Object canChon() {
        Object o = dongChon();
        if (o == null) {
            HoaDonPopups.thongBao(this, "Chưa chọn dòng nào", "Hãy bấm chọn một dòng trong bảng rồi thử lại.");
        }
        return o;
    }

    private void chonDong(Object o) {
        int i = model.rows.indexOf(o);
        if (i >= 0) table.setRowSelectionInterval(i, i);
    }

    private void thongBao(String text) {
        lblTb.setText(text);
        if (hen != null) hen.stop();
        hen = new Timer(6000, e -> lblTb.setText(" "));
        hen.setRepeats(false);
        hen.start();
    }

    private void xemChiTiet() {
        Object o = canChon();
        if (o == null) return;
        if (o instanceof SanPham sp) {
            List<String[]> r = new ArrayList<>();
            r.add(new String[] {sp.isDichVu() ? "Mã DV" : "Mã SP", sp.getSku()});
            r.add(new String[] {"Tên", sp.getTen()});
            r.add(new String[] {sp.isDichVu() ? "Nhóm dịch vụ" : "Loại hàng", sp.getDanhMuc()});
            r.add(new String[] {"Đơn giá", Theme.money(sp.getDonGia())});
            if (sp.isDichVu()) {
                r.add(new String[] {"Thời lượng", sp.getThoiLuong()});
                r.add(new String[] {"Đối tượng", sp.getDoiTuong()});
            }
            r.add(new String[] {"Trạng thái", DichVuPopups.nhanTrangThai(sp.isDichVu(), sp.isDangMo())});
            DichVuPopups.chiTiet(this, sp.getTen(), (sp.isDichVu() ? "Dịch vụ " : "Sản phẩm ") + sp.getSku(), r.toArray(new String[0][]));
        } else if (o instanceof MaGiamGia m) {
            DichVuPopups.chiTiet(this, "Mã " + m.getMa(), m.moTa(), new String[][] {
                {"Mã giảm giá", m.getMa()},
                {"Loại giảm", m.isPhanTram() ? "Giảm theo %" : "Giảm theo số tiền"},
                {"Mức giảm", mucGiam(m)},
                {"Hạn dùng", NGAY.format(m.getHanDung())},
                {"Trạng thái", trangThaiMa(m)},
            });
        }
    }

    private void sua() {
        Object o = canChon();
        if (!(o instanceof SanPham sp)) return;
        DichVuPopups.formMuc(this, spBus, sp.isDichVu(), sp, null, r -> {
            dungChips();
            refresh();
            chonDong(r);
            thongBao("Đã cập nhật " + r.getSku());
        });
    }

    private void xoa() {
        Object o = canChon();
        if (o instanceof SanPham sp) {
            String loai = sp.isDichVu() ? "dịch vụ" : "sản phẩm";
            HoaDonPopups.xacNhanXoa(this, "Xóa " + loai + " " + sp.getSku() + "?",
                    "\"" + sp.getTen() + "\" sẽ bị xóa khỏi danh mục.",
                    "Hóa đơn đã lưu hoặc đã thanh toán vẫn giữ nguyên dòng hàng này.", () -> {
                        spBus.xoa(sp.getSku());
                        dungChips();
                        refresh();
                        thongBao("Đã xóa " + loai + " " + sp.getSku());
                    });
        } else if (o instanceof MaGiamGia m) {
            HoaDonPopups.xacNhanXoa(this, "Xóa mã giảm giá " + m.getMa() + "?",
                    m.moTa() + "  •  hạn dùng " + NGAY.format(m.getHanDung()) + ".",
                    "Hóa đơn đang áp dụng mã này vẫn giữ mức giảm đã áp dụng.", () -> {
                        mgBus.xoa(m.getMa());
                        dungChips();
                        refresh();
                        thongBao("Đã xóa mã giảm giá " + m.getMa());
                    });
        }
    }

    private void them() {
        if (laHang()) {
            String macDinh = nhom.equals(SanPhamBUS.TAT_CA) ? null : nhom;
            DichVuPopups.formMuc(this, spBus, laDichVu(), null, macDinh, r -> {
                nhom = SanPhamBUS.TAT_CA;
                tuKhoa = "";
                tfTim.setText("");
                sapXep = 0;
                trang = 1;
                dungChips();
                refresh();
                chonDong(r);
                thongBao("Đã thêm " + r.getTen() + " (" + r.getSku() + ")");
            });
        } else {
            HoaDonPopups.taoMaGiamGia(this, mgBus, m -> {
                Overlay.dong(this);
                nhom = SanPhamBUS.TAT_CA;
                tuKhoa = "";
                tfTim.setText("");
                sapXep = 0;
                trang = 1;
                dungChips();
                refresh();
                chonDong(m);
                thongBao("Đã thêm mã giảm giá " + m.getMa());
            }, () -> Overlay.dong(this));
        }
    }

    // =====================================================================
    // Xuất Excel
    // =====================================================================
    private void xuatExcel() {
        if (ketQua.isEmpty()) {
            HoaDonPopups.thongBao(this, "Không có dữ liệu để xuất", "Danh sách đang trống nên chưa có gì để ghi ra Excel.");
            return;
        }
        String ten = (laDichVu() ? "dich-vu" : laHang() ? "san-pham" : "ma-giam-gia") + "-" + NGAY_TEP.format(LocalDate.now()) + ".xlsx";
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Xuất Excel");
        fc.setSelectedFile(new File(ten));
        fc.setFileFilter(new FileNameExtensionFilter("Tệp Excel (*.xlsx)", "xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".xlsx")) f = new File(f.getParentFile(), f.getName() + ".xlsx");
        File dich = f;
        if (dich.exists()) {
            HoaDonPopups.xacNhan(this, "Ghi đè tệp này?", dich.getName() + " đã tồn tại.", "Tệp cũ sẽ bị thay thế.",
                    "Không", "Có, ghi đè", () -> xuatRa(dich));
        } else {
            xuatRa(dich);
        }
    }

    int xuatRa(File f) {
        String[] th;
        List<Object[]> dong = new ArrayList<>();
        if (laHang()) {
            boolean dv = laDichVu();
            th = dv ? new String[] {"Mã DV", "Tên dịch vụ", "Nhóm dịch vụ", "Đơn giá (đ)", "Thời lượng", "Đối tượng", "Trạng thái"}
                    : new String[] {"Mã SP", "Tên sản phẩm", "Loại hàng", "Đơn giá (đ)", "Trạng thái"};
            for (Object o : ketQua) {
                SanPham s = (SanPham) o;
                String tt = DichVuPopups.nhanTrangThai(dv, s.isDangMo());
                dong.add(dv ? new Object[] {s.getSku(), s.getTen(), s.getDanhMuc(), s.getDonGia(), s.getThoiLuong(), s.getDoiTuong(), tt}
                            : new Object[] {s.getSku(), s.getTen(), s.getDanhMuc(), s.getDonGia(), tt});
            }
        } else {
            th = new String[] {"Mã giảm giá", "Loại giảm", "Giá trị", "Đơn vị", "Hạn dùng", "Trạng thái"};
            for (Object o : ketQua) {
                MaGiamGia m = (MaGiamGia) o;
                dong.add(new Object[] {m.getMa(), m.isPhanTram() ? "Giảm theo %" : "Giảm theo số tiền", m.getGiaTri(),
                        m.isPhanTram() ? "%" : "đ", NGAY.format(m.getHanDung()), trangThaiMa(m)});
            }
        }
        try {
            XlsxWriter.ghi(f, lblTitle.getText().replace("Quản lý ", ""), th, dong);
            thongBao("Đã xuất " + dong.size() + " dòng ra " + f.getName());
        } catch (IOException ex) {
            HoaDonPopups.thongBao(this, "Không xuất được Excel", "Không ghi được tệp: " + ex.getMessage());
            return 0;
        }
        return dong.size();
    }

    // =====================================================================
    // Model + renderer
    // =====================================================================
    private class RowModel extends AbstractTableModel {
        List<Object> rows = new ArrayList<>();

        void setRows(List<Object> r) {
            rows = new ArrayList<>(r);
            fireTableDataChanged();
        }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cot.size(); }
        @Override public String getColumnName(int c) { return c < cot.size() ? cot.get(c).tieuDe() : ""; }
        @Override public Object getValueAt(int r, int c) { return rows.get(r); }
        @Override public boolean isCellEditable(int r, int c) { return false; }
    }

    private static class HeaderR extends JLabel implements TableCellRenderer {
        HeaderR() {
            setOpaque(true);
            setBackground(HEADER_BG);
            setForeground(Theme.TEXT);
            setFont(Theme.font(Font.BOLD, 13));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new MatteBorder(0, 0, 2, 1, new Color(0xC9D3DB)));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            return this;
        }
    }

    private class CellR extends JLabel implements TableCellRenderer {
        CellR() {
            setOpaque(true);
            setFont(Theme.font(Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 12, 0, 12));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Cot col = cot.get(c);
            String txt = col.gt().apply(v);
            setText(txt);
            setHorizontalAlignment(col.canh());
            setBackground(sel ? SEL_BG : r % 2 == 0 ? Color.WHITE : ALT_BG);
            Color mau = Theme.TEXT;
            if (col.tieuDe().equals("Trạng thái")) {
                if (txt.equals("Đang cung cấp") || txt.equals("Đang bán") || txt.equals("Còn hạn")) mau = Theme.SUCCESS;
                else if (txt.equals("Hết hạn")) mau = Theme.DANGER;
                else mau = Theme.AMBER_TEXT;
            }
            setForeground(mau);
            setFont(Theme.font(col.tieuDe().equals("Trạng thái") ? Font.BOLD : Font.PLAIN, 13));
            return this;
        }
    }
}
