package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.KhoHangBUS;
import Entity.SanPham;

/** Màn hình Quản lý Kho hàng - Bám sát thiết kế tham chiếu. */
public class KhoHangPanel extends JPanel {
    private static final int PAGE_SIZE = 7;
    private static final String[] CATEGORIES = {"Tất cả", "Thức ăn", "Đồ chơi", "Thuốc & Y tế", "Phụ kiện", "Sắp hết hàng"};
    private static final String[] SORTS = {"Tồn kho thấp nhất", "Tồn kho cao nhất", "Giá cao nhất", "Giá thấp nhất", "Tên A-Z"};

    private final KhoHangBUS bus = new KhoHangBUS();
    public KhoHangBUS getBus() { return bus; }
    private List<SanPham> filtered = new ArrayList<>();
    private String danhMucLoc = "Tất cả";
    private String sapXep = SORTS[0];
    private int trang = 1;

    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm theo tên sản phẩm, mã SKU, danh mục...");
    private final List<RoundedButton> chips = new ArrayList<>();
    private final RoundedButton btnSort = new RoundedButton("", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10);
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    // Thẻ thống kê
    private JLabel lblStatTong;
    private JLabel lblStatSapHet;
    private JLabel lblStatGiaTri;
    private JLabel lblStatNhap;

    private static final DecimalFormat NUM_FMT;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        NUM_FMT = new DecimalFormat("#,##0", s);
    }

    public KhoHangPanel() {
        super(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 24, 16, 24));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildStats(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        bus.addListener(this::refresh);
        refresh();
    }

    // ---------------- Header: Tiêu đề + Nút thao tác ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JLabel t = new JLabel("Quản lý Kho hàng");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Quản lý mã SKU, theo dõi tồn kho và phiếu nhập/xuất hàng");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(4));
        left.add(s);

        RoundedButton btnLichSu = new RoundedButton("Lịch sử Nhập/Xuất", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(18, 11);
        RoundedButton btnThem = new RoundedButton("+ Thêm sản phẩm (SKU)", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(18, 11);
        btnLichSu.addActionListener(e -> xemLichSuNhapXuat());
        btnThem.addActionListener(e -> themSanPham());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnLichSu);
        right.add(btnThem);

        head.add(left, BorderLayout.CENTER);
        head.add(right, BorderLayout.EAST);
        return head;
    }

    // ---------------- 4 Thẻ thống kê ----------------
    private JComponent buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        lblStatTong = new JLabel("320");
        lblStatSapHet = new JLabel("12");
        lblStatGiaTri = new JLabel("420,5 triệu đ");
        lblStatNhap = new JLabel("85 triệu đ");

        row.add(statCard("Tổng mã SKU", lblStatTong, "+18 mã SKU trong tháng", Theme.GREEN, Theme.TEXT));
        row.add(statCard("Sản phẩm sắp hết", lblStatSapHet, "Cần bổ sung hàng để duy trì tồn kho", Theme.ORANGE, Theme.ORANGE));
        row.add(statCard("Tổng giá trị tồn kho", lblStatGiaTri, "Giá trị hàng hóa hiện có trong kho", Theme.MUTED, Theme.TEXT));
        row.add(statCard("Nhập kho trong tháng", lblStatNhap, "24 phiếu nhập trong tháng", Theme.GREEN, Theme.TEXT));
        return row;
    }

    private JComponent statCard(String label, JLabel valLabel, String note, Color noteColor, Color valColor) {
        RoundedPanel p = new RoundedPanel(16, Theme.CARD, null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);

        valLabel.setFont(Theme.font(Font.BOLD, 24));
        valLabel.setForeground(valColor);

        JLabel n = new JLabel(note);
        n.setFont(Theme.font(Font.PLAIN, 11));
        n.setForeground(noteColor);

        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(valLabel);
        p.add(Box.createVerticalStrut(4));
        p.add(n);
        return p;
    }

    // ---------------- Thẻ bảng chứa Toolbar, Table và Footer ----------------
    private JComponent buildTableCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, null);
        card.setLayout(new BorderLayout());
        card.add(buildToolbar(), BorderLayout.NORTH);
        card.add(buildTable(), BorderLayout.CENTER);
        card.add(buildFooter(), BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildToolbar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(14, 16, 12, 16));

        // Hàng 1: Ô tìm kiếm (trái) + Nút chọn sắp xếp (phải)
        JPanel row1 = new JPanel(new BorderLayout(16, 0));
        row1.setOpaque(false);

        RoundedPanel search = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        search.setLayout(new BorderLayout());
        search.setPreferredSize(new Dimension(340, 38));
        JLabel ic = new JLabel(Icons.of("search", 16, new Color(0x8795A0)));
        ic.setBorder(new EmptyBorder(0, 12, 0, 4));
        txtTim.setBorder(new EmptyBorder(0, 4, 0, 10));
        txtTim.setOpaque(false);
        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        search.add(ic, BorderLayout.WEST);
        search.add(txtTim, BorderLayout.CENTER);
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() { trang = 1; refresh(); }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        // Sắp xếp
        btnSort.setFont(Theme.font(Font.PLAIN, 13));
        btnSort.pad(16, 8);
        btnSort.addActionListener(e -> {
            JPopupMenu m = new JPopupMenu();
            for (String s : SORTS) {
                JMenuItem it = new JMenuItem(s);
                it.setFont(Theme.font(Font.PLAIN, 13));
                it.addActionListener(ev -> { sapXep = s; trang = 1; refresh(); });
                m.add(it);
            }
            m.show(btnSort, 0, btnSort.getHeight() + 2);
        });

        row1.add(search, BorderLayout.WEST);
        row1.add(btnSort, BorderLayout.EAST);

        // Hàng 2: Chips lọc danh mục
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);
        for (String c : CATEGORIES) {
            RoundedButton chip = new RoundedButton(c, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 7);
            chip.setFont(Theme.font(Font.PLAIN, 13));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 34));
            chip.addActionListener(e -> { danhMucLoc = c; trang = 1; refresh(); });
            chips.add(chip);
            row2.add(chip);
        }

        bar.add(row1);
        bar.add(Box.createVerticalStrut(12));
        bar.add(row2);
        return bar;
    }

    private JComponent buildTable() {
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0xEAF0F4));
        table.setSelectionForeground(Theme.TEXT);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Header
        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new HeaderCell());

        // Cột và renderers
        int[] widths = {85, 330, 110, 115, 105, 115, 120};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setCellRenderer(new SkuCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new ProductCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new CategoryCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new StockCell());
        table.getColumnModel().getColumn(4).setCellRenderer(new PriceCell());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCell());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionCell());

        // Xử lý sự kiện click trên cột thao tác nhanh
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) {
                    SanPham sp = (SanPham) model.getValueAt(row, 1);
                    int xInCell = e.getX() - table.getCellRect(row, col, false).x;
                    if (xInCell < 54) {
                        lapPhieuNhapKho(sp);
                    } else if (xInCell < 84) {
                        suaSanPham(sp);
                    } else {
                        hienMenuThaoTac(table, e.getX(), e.getY(), sp);
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JComponent buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.ROW_LINE), new EmptyBorder(12, 16, 12, 16)));
        lblFooter.setFont(Theme.font(Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);
        pnlPages.setOpaque(false);
        f.add(lblFooter, BorderLayout.WEST);
        f.add(pnlPages, BorderLayout.EAST);
        return f;
    }

    // ---------------- Lọc, sắp xếp và phân trang ----------------
    private void refresh() {
        String kw = norm(txtTim.getText().trim());
        List<SanPham> list = new ArrayList<>();
        for (SanPham sp : bus.layDanhSach()) {
            if ("Sắp hết hàng".equals(danhMucLoc)) {
                if (sp.getSoLuongTon() > 5) continue;
            } else if (!"Tất cả".equals(danhMucLoc) && !danhMucLoc.equals(sp.getDanhMuc())) {
                continue;
            }

            if (!kw.isEmpty()) {
                String hay = norm(sp.getTenSP()) + "|" + norm(sp.getMaSKU()) + "|" + norm(sp.getDanhMuc()) + "|" + norm(sp.getQuyCach());
                if (!hay.contains(kw)) continue;
            }
            list.add(sp);
        }
        list.sort(comparator());
        filtered = list;

        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        lblFooter.setText(filtered.isEmpty()
                ? "Không tìm thấy sản phẩm phù hợp"
                : "Hiển thị " + (from + 1) + "–" + to + " trong " + Theme.number(filtered.size()) + " sản phẩm");
        btnSort.setText("Sắp xếp: " + sapXep + "  v");
        btnSort.setPreferredSize(null);

        // Cập nhật kiểu chip lọc
        for (RoundedButton c : chips) {
            boolean on = c.getText().equals(danhMucLoc);
            if (on) c.style(Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null);
            else c.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

        // Cập nhật thẻ thống kê
        if (lblStatTong != null) lblStatTong.setText(Theme.number(bus.getTongSKU()));
        if (lblStatSapHet != null) lblStatSapHet.setText(Theme.number(bus.getSoSapHet()));

        buildPager(pages);
        revalidate();
        repaint();
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        pnlPages.add(pageButton("‹", trang - 1, trang > 1, false));

        if (pages <= 7) {
            for (int p = 1; p <= pages; p++) pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
        } else {
            if (trang <= 3) {
                pnlPages.add(pageButton("1", 1, true, trang == 1));
                pnlPages.add(pageButton("2", 2, true, trang == 2));
                pnlPages.add(pageButton("3", 3, true, trang == 3));
                pnlPages.add(pageEllipsis());
                pnlPages.add(pageButton(String.valueOf(pages), pages, true, trang == pages));
            } else if (trang >= pages - 2) {
                pnlPages.add(pageButton("1", 1, true, false));
                pnlPages.add(pageEllipsis());
                for (int p = pages - 2; p <= pages; p++) {
                    pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
                }
            } else {
                pnlPages.add(pageButton("1", 1, true, false));
                pnlPages.add(pageEllipsis());
                pnlPages.add(pageButton(String.valueOf(trang), trang, true, true));
                pnlPages.add(pageEllipsis());
                pnlPages.add(pageButton(String.valueOf(pages), pages, true, false));
            }
        }

        pnlPages.add(pageButton("›", trang + 1, trang < pages, false));
        pnlPages.revalidate();
        pnlPages.repaint();
    }

    private JLabel pageEllipsis() {
        JLabel l = new JLabel("...", SwingConstants.CENTER);
        l.setFont(Theme.font(Font.PLAIN, 13));
        l.setForeground(Theme.MUTED);
        l.setPreferredSize(new Dimension(24, 32));
        return l;
    }

    private RoundedButton pageButton(String text, int target, boolean enabled, boolean active) {
        RoundedButton b = active
                ? new RoundedButton(text, Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8)
                : new RoundedButton(text, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8);
        b.setFont(Theme.font(Font.PLAIN, 13));
        b.setPreferredSize(new Dimension(32, 32));
        b.setEnabled(enabled);
        b.addActionListener(e -> { trang = target; refresh(); });
        return b;
    }

    private Comparator<SanPham> comparator() {
        return switch (sapXep) {
            case "Tồn kho cao nhất" -> Comparator.comparingInt(SanPham::getSoLuongTon).reversed();
            case "Giá cao nhất" -> Comparator.comparingLong(SanPham::getDonGia).reversed();
            case "Giá thấp nhất" -> Comparator.comparingLong(SanPham::getDonGia);
            case "Tên A-Z" -> {
                Collator col = Collator.getInstance(Locale.of("vi"));
                yield Comparator.comparing(SanPham::getTenSP, col);
            }
            default -> Comparator.comparingInt(SanPham::getSoLuongTon); // Mặc định: Tồn kho thấp nhất
        };
    }

    private static String norm(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // ---------------- Thao tác nghiệp vụ: Thêm / Nhập / Sửa ----------------
    private void themSanPham() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel card = new RoundedPanel(18, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lblTitle = new JLabel("Thêm sản phẩm mới (SKU)");
        lblTitle.setFont(Theme.font(Font.BOLD, 17));
        lblTitle.setForeground(Theme.TEXT);
        JButton btnClose = new JButton(Icons.of("close", 14, Theme.MUTED));
        btnClose.setBorder(new EmptyBorder(4, 8, 4, 0));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dlg.dispose());
        head.add(lblTitle, BorderLayout.WEST);
        head.add(btnClose, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        PlaceholderField tfTen = new PlaceholderField("Nhập tên sản phẩm...");
        setupField(tfTen);

        PlaceholderField tfQuyCach = new PlaceholderField("Ví dụ: Hộp 3 ống, Túi 2 kg...");
        setupField(tfQuyCach);

        String[] catOpts = {"Thức ăn", "Đồ chơi", "Thuốc & Y tế", "Phụ kiện"};
        JComboBox<String> cbCat = new JComboBox<>(catOpts);
        styleComboBox(cbCat);

        PlaceholderField tfTon = new PlaceholderField("Ví dụ: 10");
        setupField(tfTon);

        String[] dvtOpts = {"hộp", "túi", "bộ", "cái", "tuýp", "lọ", "chai"};
        JComboBox<String> cbDvt = new JComboBox<>(dvtOpts);
        cbDvt.setEditable(true);
        styleComboBox(cbDvt);

        PlaceholderField tfGia = new PlaceholderField("Ví dụ: 150000");
        setupField(tfGia);

        body.add(createFieldGroup("Tên sản phẩm *", wrapInput(tfTen, 38)));
        body.add(Box.createVerticalStrut(12));
        body.add(createFieldGroup("Quy cách đóng gói", wrapInput(tfQuyCach, 38)));
        body.add(Box.createVerticalStrut(12));

        JPanel r1 = new JPanel(new GridLayout(1, 2, 16, 0));
        r1.setOpaque(false);
        r1.add(createFieldGroup("Danh mục *", wrapInput(cbCat, 38)));
        r1.add(createFieldGroup("Đơn giá (VNĐ) *", wrapInput(tfGia, 38)));
        body.add(r1);
        body.add(Box.createVerticalStrut(12));

        JPanel r2 = new JPanel(new GridLayout(1, 2, 16, 0));
        r2.setOpaque(false);
        r2.add(createFieldGroup("Số lượng tồn ban đầu *", wrapInput(tfTon, 38)));
        r2.add(createFieldGroup("Đơn vị tính *", wrapInput(cbDvt, 38)));
        body.add(r2);

        card.add(body, BorderLayout.CENTER);

        // Footer
        JLabel lblErr = new JLabel(" ");
        lblErr.setFont(Theme.font(Font.PLAIN, 12));
        lblErr.setForeground(Theme.RED);

        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(22, 10);
        RoundedButton btnLuu = new RoundedButton("Lưu sản phẩm", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(22, 10);
        btnLuu.setFont(Theme.font(Font.BOLD, 13));

        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            if (ten.isEmpty()) { lblErr.setText("Vui lòng nhập tên sản phẩm."); tfTen.requestFocus(); return; }

            int ton = 10;
            try {
                String sTon = tfTon.getText().trim();
                if (!sTon.isEmpty()) ton = Integer.parseInt(sTon);
            } catch (Exception ex) {
                lblErr.setText("Số lượng tồn phải là số nguyên."); tfTon.requestFocus(); return;
            }

            long gia = 100_000;
            try {
                String sGia = tfGia.getText().trim().replace(".", "").replace(",", "");
                if (!sGia.isEmpty()) gia = Long.parseLong(sGia);
            } catch (Exception ex) {
                lblErr.setText("Đơn giá không hợp lệ."); tfGia.requestFocus(); return;
            }

            String cat = (String) cbCat.getSelectedItem();
            String dvt = (String) cbDvt.getSelectedItem();
            if (dvt == null || dvt.trim().isEmpty()) dvt = "cái";
            String qc = tfQuyCach.getText().trim();

            bus.them(null, ten, qc.isEmpty() ? "Quy cách chuẩn" : qc, cat, ton, dvt, gia, ton > 0 ? "Đang bán" : "Tạm hết hàng");
            dlg.dispose();
            refresh();
        });

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.add(lblErr, BorderLayout.WEST);
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnHuy);
        pnlBtns.add(btnLuu);
        foot.add(pnlBtns, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.setContentPane(card);
        dlg.setSize(580, 520);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    // ---------------- Dữ liệu mặt hàng trong Phiếu nhập ----------------
    public static class PhieuItem {
        public String maSKU;
        public String tenSP;
        public String quyCach;
        public int soLuong;
        public long donGia;

        public PhieuItem(String maSKU, String tenSP, String quyCach, int soLuong, long donGia) {
            this.maSKU = maSKU;
            this.tenSP = tenSP;
            this.quyCach = quyCach;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }

        public long getThanhTien() {
            return (long) soLuong * donGia;
        }
    }

    public void lapPhieuNhapKho(SanPham spChon) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel card = new RoundedPanel(18, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Kéo di chuyển dialog
        final Point[] dragPoint = new Point[1];
        card.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        card.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point p = dlg.getLocation();
                    dlg.setLocation(p.x + e.getX() - dragPoint[0].x, p.y + e.getY() - dragPoint[0].y);
                }
            }
        });

        // 1. Header: Tiêu đề + Nút đóng X
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lblTitle = new JLabel("Lập Phiếu Nhập Kho Mới");
        lblTitle.setFont(Theme.font(Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT);

        JButton btnClose = new JButton(Icons.of("close", 14, Theme.MUTED));
        btnClose.setBorder(new EmptyBorder(4, 8, 4, 0));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dlg.dispose());
        head.add(lblTitle, BorderLayout.WEST);
        head.add(btnClose, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // 2. Nội dung Form + Bảng sản phẩm
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Hàng 1: Loại phiếu + Nhà cung cấp + Ngày lập phiếu
        JPanel row1 = new JPanel(new BorderLayout(14, 0));
        row1.setOpaque(false);

        // 1.1 Loại phiếu
        JComboBox<String> cbLoai = new JComboBox<>(new String[] {"Nhập kho", "Xuất kho", "Kiểm kê"});
        styleComboBox(cbLoai);
        JPanel boxLoai = wrapInput(cbLoai, 38);
        boxLoai.setPreferredSize(new Dimension(140, 38));
        JPanel groupLoai = createFieldGroup("Loại phiếu", boxLoai);
        groupLoai.setPreferredSize(new Dimension(140, 62));

        // 1.2 Nhà cung cấp / Đối tác
        String[] nccList = {
            "Chọn hoặc nhập tên nhà cung cấp...",
            "Công ty Cổ phần PetCare Việt Nam",
            "Nhà phân phối Royal Canin HCM",
            "Công ty Dược Thú Y Marphavet",
            "Đại lý Phụ kiện thú cưng Sài Gòn"
        };
        JComboBox<String> cbNcc = new JComboBox<>(nccList);
        cbNcc.setEditable(true);
        styleComboBox(cbNcc);
        if (cbNcc.getEditor().getEditorComponent() instanceof JTextField editorTf) {
            setupField(editorTf);
            editorTf.setText("Chọn hoặc nhập tên nhà cung cấp...");
            editorTf.setForeground(Theme.MUTED);
            editorTf.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) {
                    if ("Chọn hoặc nhập tên nhà cung cấp...".equals(editorTf.getText())) {
                        editorTf.setText("");
                        editorTf.setForeground(Theme.TEXT);
                    }
                }
                @Override public void focusLost(java.awt.event.FocusEvent e) {
                    if (editorTf.getText().trim().isEmpty()) {
                        editorTf.setText("Chọn hoặc nhập tên nhà cung cấp...");
                        editorTf.setForeground(Theme.MUTED);
                    }
                }
            });
        }
        JPanel boxNcc = wrapInput(cbNcc, 38);
        JPanel groupNcc = createFieldGroup("Nhà cung cấp / Đối tác", boxNcc);

        // 1.3 Ngày lập phiếu
        JTextField tfNgay = new JTextField("04/10/2026");
        setupField(tfNgay);
        tfNgay.setEditable(false);
        tfNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel icCal = new JLabel(Icons.of("calendar", 16, Theme.MUTED));
        icCal.setBorder(new EmptyBorder(0, 0, 0, 10));
        icCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel boxNgay = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        boxNgay.setLayout(new BorderLayout());
        boxNgay.setPreferredSize(new Dimension(150, 38));
        boxNgay.add(tfNgay, BorderLayout.CENTER);
        boxNgay.add(icCal, BorderLayout.EAST);
        boxNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter maNgay = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                java.time.LocalDate curr = java.time.LocalDate.now();
                try {
                    curr = java.time.LocalDate.parse(tfNgay.getText().trim(), CalendarPicker.DATE_FMT);
                } catch (Exception ignored) { }
                CalendarPicker.showDatePicker(boxNgay, curr, d -> tfNgay.setText(d.format(CalendarPicker.DATE_FMT)));
            }
        };
        boxNgay.addMouseListener(maNgay);
        tfNgay.addMouseListener(maNgay);
        icCal.addMouseListener(maNgay);

        JPanel groupNgay = createFieldGroup("Ngày lập phiếu", boxNgay);
        groupNgay.setPreferredSize(new Dimension(150, 62));

        row1.add(groupLoai, BorderLayout.WEST);
        row1.add(groupNcc, BorderLayout.CENTER);
        row1.add(groupNgay, BorderLayout.EAST);
        body.add(row1);
        body.add(Box.createVerticalStrut(12));

        // Hàng 2: Ghi chú
        PlaceholderField tfGhiChu = new PlaceholderField("Nhập mô tả hoặc ghi chú cho phiếu...");
        setupField(tfGhiChu);
        JPanel boxGhiChu = wrapInput(tfGhiChu, 38);
        body.add(createFieldGroup("Ghi chú", boxGhiChu));
        body.add(Box.createVerticalStrut(16));

        // Section Tiêu đề Sản phẩm trong phiếu
        JPanel pnlSecHead = new JPanel(new BorderLayout());
        pnlSecHead.setOpaque(false);

        JPanel leftSec = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSec.setOpaque(false);
        JLabel lblSecTitle = new JLabel("Sản phẩm trong phiếu");
        lblSecTitle.setFont(Theme.font(Font.BOLD, 14));
        lblSecTitle.setForeground(Theme.TEXT);

        JLabel lblSecSub = new JLabel("3 sản phẩm • 28 đơn vị");
        lblSecSub.setFont(Theme.font(Font.PLAIN, 12));
        lblSecSub.setForeground(Theme.MUTED);
        leftSec.add(lblSecTitle);
        leftSec.add(lblSecSub);

        JLabel lblDonVi = new JLabel("Đơn vị: VND");
        lblDonVi.setFont(Theme.font(Font.PLAIN, 11));
        lblDonVi.setForeground(Theme.MUTED);

        pnlSecHead.add(leftSec, BorderLayout.WEST);
        pnlSecHead.add(lblDonVi, BorderLayout.EAST);
        body.add(pnlSecHead);
        body.add(Box.createVerticalStrut(8));

        // Danh sách mặt hàng (3 mặt hàng mẫu theo thiết kế)
        List<PhieuItem> items = new ArrayList<>();
        items.add(new PhieuItem("TA-0042", "Royal Canin Kitten 2 kg", "Thức ăn cho mèo con • Túi 2 kg", 10, 385_000));
        items.add(new PhieuItem("VT-0031", "Gel dinh dưỡng Nutri-Plus", "Bổ sung vitamin • Tuýp 120 g", 6, 210_000));
        items.add(new PhieuItem("TA-0088", "Hạt Me-O vị cá ngừ 1,2 kg", "Thức ăn cho mèo trưởng thành • Túi", 12, 95_000));

        if (spChon != null) {
            boolean exists = items.stream().anyMatch(it -> it.maSKU.equalsIgnoreCase(spChon.getMaSKU()));
            if (!exists) {
                long giaNhap = Math.max(10_000, spChon.getDonGia() * 80 / 100);
                items.add(new PhieuItem(spChon.getMaSKU(), spChon.getTenSP(), spChon.getQuyCach(), 5, giaNhap));
            }
        }

        // Container bảng sản phẩm
        RoundedPanel pnlTableCard = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        pnlTableCard.setLayout(new BorderLayout());

        // Header bảng
        JPanel pnlHeadRow = new JPanel(new GridBagLayout());
        pnlHeadRow.setBackground(new Color(0xF8FAFC));
        pnlHeadRow.setPreferredSize(new Dimension(0, 36));
        pnlHeadRow.setBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE));
        addTableHeadCell(pnlHeadRow, "Mã SKU", 0, 75, SwingConstants.LEFT, 16);
        addTableHeadCell(pnlHeadRow, "Tên sản phẩm", 1, 0, SwingConstants.LEFT, 8);
        addTableHeadCell(pnlHeadRow, "Số lượng thực tế", 2, 120, SwingConstants.CENTER, 8);
        addTableHeadCell(pnlHeadRow, "Đơn giá nhập", 3, 135, SwingConstants.CENTER, 8);
        addTableHeadCell(pnlHeadRow, "Thành tiền", 4, 120, SwingConstants.RIGHT, 16);
        pnlTableCard.add(pnlHeadRow, BorderLayout.NORTH);

        // Vùng các dòng sản phẩm
        JPanel pnlRows = new JPanel();
        pnlRows.setOpaque(false);
        pnlRows.setLayout(new BoxLayout(pnlRows, BoxLayout.Y_AXIS));

        // Footer components
        JLabel lblTongTienVal = new JLabel("6.250.000 đ");
        lblTongTienVal.setFont(Theme.font(Font.BOLD, 22));
        lblTongTienVal.setForeground(Theme.TEXT);

        Runnable updateSummary = () -> {
            int totalUnits = 0;
            long totalMoney = 0;
            for (PhieuItem it : items) {
                totalUnits += it.soLuong;
                totalMoney += it.getThanhTien();
            }
            lblSecSub.setText(items.size() + " sản phẩm • " + totalUnits + " đơn vị");
            lblTongTienVal.setText(NUM_FMT.format(totalMoney) + " đ");
        };

        Runnable[] rebuildRef = new Runnable[1];
        rebuildRef[0] = () -> {
            pnlRows.removeAll();
            for (int i = 0; i < items.size(); i++) {
                final PhieuItem item = items.get(i);
                final boolean isLast = (i == items.size() - 1);
                JPanel r = createItemRow(item, isLast, updateSummary);
                pnlRows.add(r);
            }
            pnlRows.revalidate();
            pnlRows.repaint();
            updateSummary.run();
        };

        rebuildRef[0].run();
        pnlTableCard.add(pnlRows, BorderLayout.CENTER);
        body.add(pnlTableCard);
        body.add(Box.createVerticalStrut(10));

        // Link "+ Thêm sản phẩm vào phiếu"
        JPanel pnlAddLink = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlAddLink.setOpaque(false);
        JLabel lblAdd = new JLabel("+ Thêm sản phẩm vào phiếu");
        lblAdd.setFont(Theme.font(Font.BOLD, 13));
        lblAdd.setForeground(Theme.ACCENT);
        lblAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblAdd.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { lblAdd.setForeground(Theme.ACCENT_HOVER); }
            @Override public void mouseExited(MouseEvent e) { lblAdd.setForeground(Theme.ACCENT); }
            @Override public void mouseClicked(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                for (SanPham sp : bus.layDanhSach()) {
                    boolean alreadyIn = items.stream().anyMatch(it -> it.maSKU.equalsIgnoreCase(sp.getMaSKU()));
                    if (!alreadyIn && popup.getComponentCount() < 8) {
                        JMenuItem mi = new JMenuItem(sp.getMaSKU() + " - " + sp.getTenSP());
                        mi.setFont(Theme.font(Font.PLAIN, 12));
                        mi.addActionListener(ev -> {
                            long giaNhap = Math.max(10_000, sp.getDonGia() * 80 / 100);
                            items.add(new PhieuItem(sp.getMaSKU(), sp.getTenSP(), sp.getQuyCach(), 5, giaNhap));
                            rebuildRef[0].run();
                        });
                        popup.add(mi);
                    }
                }
                popup.show(lblAdd, 0, lblAdd.getHeight() + 2);
            }
        });
        pnlAddLink.add(lblAdd);
        body.add(pnlAddLink);

        card.add(body, BorderLayout.CENTER);

        // 3. Footer: Tổng tiền + Nút Hủy + Hoàn tất & Lưu phiếu
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(new EmptyBorder(14, 0, 0, 0));

        JPanel leftFoot = new JPanel();
        leftFoot.setOpaque(false);
        leftFoot.setLayout(new BoxLayout(leftFoot, BoxLayout.Y_AXIS));
        JLabel lblTongTienText = new JLabel("Tổng tiền nhập kho");
        lblTongTienText.setFont(Theme.font(Font.PLAIN, 12));
        lblTongTienText.setForeground(Theme.MUTED);
        leftFoot.add(lblTongTienText);
        leftFoot.add(Box.createVerticalStrut(2));
        leftFoot.add(lblTongTienVal);

        JPanel rightFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightFoot.setOpaque(false);
        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(22, 10);
        RoundedButton btnLuu = new RoundedButton("Hoàn tất & Lưu phiếu", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(22, 10);
        btnLuu.setFont(Theme.font(Font.BOLD, 13));

        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            for (PhieuItem it : items) {
                bus.nhapKho(it.maSKU, it.soLuong);
            }
            refresh();
            dlg.dispose();
            JOptionPane.showMessageDialog(this,
                    "Đã lưu thành công phiếu nhập kho (" + items.size() + " mặt hàng, tổng tiền " + lblTongTienVal.getText() + ")!",
                    "Nhập kho thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        rightFoot.add(btnHuy);
        rightFoot.add(btnLuu);

        pnlFooter.add(leftFoot, BorderLayout.WEST);
        pnlFooter.add(rightFoot, BorderLayout.EAST);
        card.add(pnlFooter, BorderLayout.SOUTH);

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.setContentPane(card);
        dlg.setSize(890, 635);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private static void addTableHeadCell(JPanel pnl, String text, int gridx, int width, int align, int marginSide) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);
        l.setHorizontalAlignment(align);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = 0;
        c.fill = (gridx == 1) ? GridBagConstraints.BOTH : GridBagConstraints.VERTICAL;
        c.weightx = (gridx == 1) ? 1.0 : 0.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, gridx == 0 ? marginSide : 8, 0, gridx == 4 ? marginSide : 8);
        if (width > 0) l.setPreferredSize(new Dimension(width, 36));
        pnl.add(l, c);
    }

    private static JPanel createItemRow(PhieuItem item, boolean isLast, Runnable onUpdate) {
        JPanel r = new JPanel(new GridBagLayout());
        r.setOpaque(false);
        r.setPreferredSize(new Dimension(0, 52));
        if (!isLast) {
            r.setBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE));
        }

        // Col 0: SKU
        JLabel lblSku = new JLabel(item.maSKU);
        lblSku.setFont(Theme.font(Font.PLAIN, 12));
        lblSku.setForeground(Theme.MUTED);
        lblSku.setPreferredSize(new Dimension(75, 48));
        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0; c0.gridy = 0;
        c0.insets = new Insets(0, 16, 0, 8);
        r.add(lblSku, c0);

        // Col 1: Tên sản phẩm + Quy cách
        JPanel pnlName = new JPanel();
        pnlName.setOpaque(false);
        pnlName.setLayout(new BoxLayout(pnlName, BoxLayout.Y_AXIS));
        JLabel lblName = new JLabel(item.tenSP);
        lblName.setFont(Theme.font(Font.BOLD, 13));
        lblName.setForeground(Theme.TEXT);
        JLabel lblSpec = new JLabel(item.quyCach);
        lblSpec.setFont(Theme.font(Font.PLAIN, 11));
        lblSpec.setForeground(Theme.MUTED);
        pnlName.add(Box.createVerticalGlue());
        pnlName.add(lblName);
        pnlName.add(Box.createVerticalStrut(1));
        pnlName.add(lblSpec);
        pnlName.add(Box.createVerticalGlue());

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 1; c1.gridy = 0;
        c1.fill = GridBagConstraints.BOTH;
        c1.weightx = 1.0;
        c1.insets = new Insets(0, 8, 0, 8);
        r.add(pnlName, c1);

        // Col 2: Số lượng thực tế (Stepper)
        JPanel pnlStepWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        pnlStepWrap.setOpaque(false);
        pnlStepWrap.setPreferredSize(new Dimension(120, 48));

        RoundedPanel pnlStep = new RoundedPanel(8, Color.WHITE, Theme.BORDER);
        pnlStep.setLayout(new BorderLayout());
        pnlStep.setPreferredSize(new Dimension(100, 32));

        JLabel lblQty = new JLabel(String.valueOf(item.soLuong), SwingConstants.CENTER);
        lblQty.setFont(Theme.font(Font.BOLD, 13));
        lblQty.setForeground(Theme.TEXT);

        JButton btnMinus = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.MUTED);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(8, getHeight() / 2, getWidth() - 8, getHeight() / 2);
                g2.dispose();
            }
        };
        btnMinus.setContentAreaFilled(false);
        btnMinus.setBorderPainted(false);
        btnMinus.setFocusPainted(false);
        btnMinus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMinus.setPreferredSize(new Dimension(28, 32));

        JButton btnPlus = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.MUTED);
                g2.setStroke(new BasicStroke(1.5f));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.drawLine(cx - 5, cy, cx + 5, cy);
                g2.drawLine(cx, cy - 5, cx, cy + 5);
                g2.dispose();
            }
        };
        btnPlus.setContentAreaFilled(false);
        btnPlus.setBorderPainted(false);
        btnPlus.setFocusPainted(false);
        btnPlus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPlus.setPreferredSize(new Dimension(28, 32));

        pnlStep.add(btnMinus, BorderLayout.WEST);
        pnlStep.add(lblQty, BorderLayout.CENTER);
        pnlStep.add(btnPlus, BorderLayout.EAST);
        pnlStepWrap.add(pnlStep);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 2; c2.gridy = 0;
        c2.insets = new Insets(0, 8, 0, 8);
        r.add(pnlStepWrap, c2);

        // Col 3: Đơn giá nhập
        JPanel pnlGiaWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        pnlGiaWrap.setOpaque(false);
        pnlGiaWrap.setPreferredSize(new Dimension(135, 48));

        RoundedPanel boxGia = new RoundedPanel(8, Color.WHITE, Theme.BORDER);
        boxGia.setLayout(new BorderLayout());
        boxGia.setPreferredSize(new Dimension(125, 32));
        JTextField tfGia = new JTextField(NUM_FMT.format(item.donGia) + " đ");
        tfGia.setHorizontalAlignment(SwingConstants.RIGHT);
        tfGia.setFont(Theme.font(Font.PLAIN, 13));
        tfGia.setForeground(Theme.TEXT);
        tfGia.setOpaque(false);
        tfGia.setBorder(new EmptyBorder(0, 6, 0, 10));
        boxGia.add(tfGia, BorderLayout.CENTER);
        pnlGiaWrap.add(boxGia);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 3; c3.gridy = 0;
        c3.insets = new Insets(0, 8, 0, 8);
        r.add(pnlGiaWrap, c3);

        // Col 4: Thành tiền
        JLabel lblThanhTien = new JLabel(NUM_FMT.format(item.getThanhTien()) + " đ", SwingConstants.RIGHT);
        lblThanhTien.setFont(Theme.font(Font.BOLD, 13));
        lblThanhTien.setForeground(Theme.TEXT);
        lblThanhTien.setPreferredSize(new Dimension(120, 48));

        GridBagConstraints c4 = new GridBagConstraints();
        c4.gridx = 4; c4.gridy = 0;
        c4.insets = new Insets(0, 8, 0, 16);
        r.add(lblThanhTien, c4);

        // Logic sự kiện stepper và đơn giá
        btnMinus.addActionListener(e -> {
            if (item.soLuong > 1) {
                item.soLuong--;
                lblQty.setText(String.valueOf(item.soLuong));
                lblThanhTien.setText(NUM_FMT.format(item.getThanhTien()) + " đ");
                if (onUpdate != null) onUpdate.run();
            }
        });

        btnPlus.addActionListener(e -> {
            item.soLuong++;
            lblQty.setText(String.valueOf(item.soLuong));
            lblThanhTien.setText(NUM_FMT.format(item.getThanhTien()) + " đ");
            if (onUpdate != null) onUpdate.run();
        });

        java.awt.event.FocusAdapter updateGiaFocus = new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                try {
                    String clean = tfGia.getText().replace(".", "").replace(",", "").replace("đ", "").trim();
                    long val = Long.parseLong(clean);
                    if (val >= 0) {
                        item.donGia = val;
                        tfGia.setText(NUM_FMT.format(val) + " đ");
                        lblThanhTien.setText(NUM_FMT.format(item.getThanhTien()) + " đ");
                        if (onUpdate != null) onUpdate.run();
                    }
                } catch (Exception ex) {
                    tfGia.setText(NUM_FMT.format(item.donGia) + " đ");
                }
            }
        };
        tfGia.addFocusListener(updateGiaFocus);
        tfGia.addActionListener(e -> updateGiaFocus.focusLost(null));

        return r;
    }

    private void suaSanPham(SanPham sp) {
        String tenMoi = JOptionPane.showInputDialog(this, "Cập nhật tên sản phẩm:", sp.getTenSP());
        if (tenMoi != null && !tenMoi.trim().isEmpty()) {
            sp.setTenSP(tenMoi.trim());
            refresh();
        }
    }

    private void xemLichSuNhapXuat() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) {
            mf.chon("Lịch sử Nhập / Xuất Kho");
        }
    }

    private void hienMenuThaoTac(Component invoker, int x, int y, SanPham sp) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem itNhap = new JMenuItem("Lập phiếu nhập kho");
        itNhap.setFont(Theme.font(Font.PLAIN, 13));
        itNhap.addActionListener(e -> lapPhieuNhapKho(sp));

        JMenuItem itSua = new JMenuItem("Chỉnh sửa thông tin");
        itSua.setFont(Theme.font(Font.PLAIN, 13));
        itSua.addActionListener(e -> suaSanPham(sp));

        JMenuItem itXoa = new JMenuItem("Xóa mã SKU");
        itXoa.setFont(Theme.font(Font.PLAIN, 13));
        itXoa.setForeground(Theme.RED);
        itXoa.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa mã SKU " + sp.getMaSKU() + " (" + sp.getTenSP() + ")?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                bus.xoa(sp.getMaSKU());
                refresh();
            }
        });

        m.add(itNhap);
        m.add(itSua);
        m.addSeparator();
        m.add(itXoa);
        m.show(invoker, x, y);
    }

    private static void setupField(JTextField f) {
        f.setFont(Theme.font(Font.PLAIN, 13));
        f.setForeground(Theme.TEXT);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(0, 12, 0, 12));
    }

    private static JPanel wrapInput(JComponent comp, int height) {
        RoundedPanel box = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        box.setLayout(new BorderLayout());
        box.setPreferredSize(new Dimension(0, height));
        box.add(comp, BorderLayout.CENTER);
        return box;
    }

    private static JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.font(Font.BOLD, 13));
        lbl.setForeground(Theme.TEXT);
        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private static void styleComboBox(JComboBox<String> cb) {
        cb.setFont(Theme.font(Font.PLAIN, 13));
        cb.setForeground(Theme.TEXT);
        cb.setBackground(Color.WHITE);
        cb.setOpaque(false);
        cb.setBorder(new EmptyBorder(0, 6, 0, 6));
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton(Icons.of("chevron-down", 14, new Color(0x8795A0)));
                b.setBorder(new EmptyBorder(0, 0, 0, 10));
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return b;
            }
        });
    }

    // ---------------- TableModel & Renderers ----------------
    private static class PageModel extends AbstractTableModel {
        private final String[] cols = {"Mã SKU", "Tên sản phẩm", "Danh mục", "Tồn kho", "Đơn giá", "Trạng thái", "Thao tác nhanh"};
        private List<SanPham> rows = new ArrayList<>();

        void setRows(List<SanPham> r) { rows = new ArrayList<>(r); fireTableDataChanged(); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            SanPham sp = rows.get(r);
            return switch (c) {
                case 0 -> sp.getMaSKU();
                case 1 -> sp;
                case 2 -> sp.getDanhMuc();
                case 3 -> sp;
                case 4 -> sp.getDonGia();
                case 5 -> sp.getTrangThai();
                default -> sp;
            };
        }
    }

    private static class HeaderCell extends JLabel implements TableCellRenderer {
        HeaderCell() {
            setOpaque(true);
            setFont(Theme.font(Font.PLAIN, 12));
            setForeground(Theme.MUTED);
            setBackground(new Color(0xF8FAFC));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            setHorizontalAlignment(c == 6 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, c == 0 ? 16 : 8, 0, c == 6 ? 16 : 8)));
            return this;
        }
    }

    private abstract static class Cell extends JPanel implements TableCellRenderer {
        Cell() { super(new BorderLayout()); setOpaque(true); }
        void prep(JTable t, boolean sel, int col) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, col == 0 ? 16 : 8, 0, col == 6 ? 16 : 8)));
        }
    }

    // Cột 0: Mã SKU
    private static class SkuCell extends Cell {
        private final JLabel lbl = new JLabel();
        SkuCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 13));
            lbl.setForeground(Theme.MUTED);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    // Cột 1: Tên sản phẩm + Quy cách đóng gói bên dưới
    private static class ProductCell extends Cell {
        private final JLabel name = new JLabel();
        private final JLabel spec = new JLabel();

        ProductCell() {
            name.setFont(Theme.font(Font.BOLD, 13));
            name.setForeground(Theme.TEXT);
            spec.setFont(Theme.font(Font.PLAIN, 11));
            spec.setForeground(Theme.MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(name);
            text.add(Box.createVerticalStrut(1));
            text.add(spec);
            text.add(Box.createVerticalGlue());
            add(text, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            SanPham sp = (SanPham) v;
            name.setText(sp.getTenSP());
            spec.setText(sp.getQuyCach());
            return this;
        }
    }

    // Cột 2: Danh mục (Badge bo tròn xám nhạt)
    private static class CategoryCell extends Cell {
        private final CategoryBadge badge = new CategoryBadge();
        CategoryCell() {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            add(wrap, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            badge.setText(String.valueOf(v));
            return this;
        }
    }

    private static class CategoryBadge extends JComponent {
        private String text = "";
        void setText(String t) { this.text = t; }
        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.PLAIN, 11));
            return new Dimension(fm.stringWidth(text) + 18, 22);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BADGE_GRAY_BG);
            g2.fillRoundRect(0, (getHeight() - 22) / 2, getWidth(), 22, 8, 8);
            g2.setColor(Theme.MUTED);
            g2.setFont(Theme.font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // Cột 3: Tồn kho (Số lượng + Badge cảnh báo hết hàng/sắp hết hàng)
    private static class StockCell extends Cell {
        private final JLabel count = new JLabel();
        private final WarningBadge warning = new WarningBadge();

        StockCell() {
            count.setFont(Theme.font(Font.BOLD, 13));

            JPanel pnl = new JPanel();
            pnl.setOpaque(false);
            pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
            pnl.add(Box.createVerticalGlue());
            pnl.add(count);
            pnl.add(Box.createVerticalStrut(2));
            pnl.add(warning);
            pnl.add(Box.createVerticalGlue());
            add(pnl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            SanPham sp = (SanPham) v;
            int ton = sp.getSoLuongTon();
            count.setText(ton + " " + sp.getDonViTinh());

            if (ton == 0) {
                count.setForeground(Theme.RED);
                warning.setVisible(true);
                warning.setWarning("Hết hàng", Theme.RED_BG, Theme.RED);
            } else if (ton <= 5) {
                count.setForeground(Theme.TEXT);
                warning.setVisible(true);
                warning.setWarning("Sắp hết hàng", Theme.ORANGE_BG, Theme.ORANGE);
            } else {
                count.setForeground(Theme.TEXT);
                warning.setVisible(false);
            }
            return this;
        }
    }

    private static class WarningBadge extends JComponent {
        private String text = "";
        private Color bg = Theme.ORANGE_BG;
        private Color fg = Theme.ORANGE;

        void setWarning(String t, Color bg, Color fg) {
            this.text = t;
            this.bg = bg;
            this.fg = fg;
        }

        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.PLAIN, 10));
            return new Dimension(fm.stringWidth(text) + 24, 18);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, (getHeight() - 18) / 2, getWidth(), 18, 6, 6);

            // Icon warning nhỏ
            Icons.draw(g2, "warning", 4, (getHeight() - 11) / 2.0, 11, fg, false);

            g2.setColor(fg);
            g2.setFont(Theme.font(Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, 18, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // Cột 4: Đơn giá
    private static class PriceCell extends Cell {
        private final JLabel lbl = new JLabel();
        PriceCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 13));
            lbl.setForeground(Theme.TEXT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText(NUM_FMT.format((Long) v) + " đ");
            return this;
        }
    }

    // Cột 5: Trạng thái (Đang bán, Tạm hết hàng)
    private static class StatusCell extends Cell {
        private final StatusBadge badge = new StatusBadge();
        StatusCell() {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            add(wrap, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            badge.setStatus(String.valueOf(v));
            return this;
        }
    }

    private static class StatusBadge extends JComponent {
        private String text = "";
        private Color bg = Theme.GREEN_BG, fg = Theme.GREEN;

        void setStatus(String t) {
            this.text = t;
            if ("Tạm hết hàng".equals(t)) {
                bg = Theme.BADGE_GRAY_BG;
                fg = Theme.MUTED;
            } else {
                bg = Theme.GREEN_BG;
                fg = Theme.GREEN;
            }
        }
        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.PLAIN, 11));
            return new Dimension(fm.stringWidth(text) + 20, 22);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, (getHeight() - 22) / 2, getWidth(), 22, 8, 8);
            g2.setColor(fg);
            g2.setFont(Theme.font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // Cột 6: Thao tác nhanh (Nút Nhập + icon edit + icon more)
    private static class ActionCell extends Cell {
        ActionCell() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int y = (getHeight() - 24) / 2;

            // Nút "Nhập" nhỏ bo góc viền xám nhạt
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(10, y, 42, 24, 6, 6);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(10, y, 42, 24, 6, 6);
            g2.setColor(Theme.TEXT);
            g2.setFont(Theme.font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("Nhập", 10 + (42 - fm.stringWidth("Nhập")) / 2, y + (24 - fm.getHeight()) / 2 + fm.getAscent());

            // Icon edit
            Icons.draw(g2, "edit", 62, (getHeight() - 16) / 2.0, 16, Theme.ACTION_ICON, false);

            // Icon more
            Icons.draw(g2, "more", 86, (getHeight() - 16) / 2.0, 16, Theme.ACTION_ICON, true);

            g2.dispose();
        }
    }
}
