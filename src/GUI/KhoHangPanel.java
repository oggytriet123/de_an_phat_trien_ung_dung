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
                        nhapHang(sp);
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

    private void nhapHang(SanPham sp) {
        String input = JOptionPane.showInputDialog(this,
                "Nhập số lượng hàng bổ sung cho " + sp.getTenSP() + " (" + sp.getDonViTinh() + "):",
                "Nhập hàng bổ sung - " + sp.getMaSKU(), JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int sl = Integer.parseInt(input.trim());
                if (sl <= 0) throw new NumberFormatException();
                bus.nhapKho(sp.getMaSKU(), sl);
                refresh();
                JOptionPane.showMessageDialog(this, "Đã nhập thành công " + sl + " " + sp.getDonViTinh() + " cho " + sp.getTenSP(),
                        "Nhập hàng thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Số lượng nhập không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void suaSanPham(SanPham sp) {
        String tenMoi = JOptionPane.showInputDialog(this, "Cập nhật tên sản phẩm:", sp.getTenSP());
        if (tenMoi != null && !tenMoi.trim().isEmpty()) {
            sp.setTenSP(tenMoi.trim());
            refresh();
        }
    }

    private void xemLichSuNhapXuat() {
        JOptionPane.showMessageDialog(this,
                "Tính năng Lịch sử Nhập/Xuất kho đang được kết nối với phân hệ Quản lý Phiếu Kho.",
                "Lịch sử Nhập/Xuất", JOptionPane.INFORMATION_MESSAGE);
    }

    private void hienMenuThaoTac(Component invoker, int x, int y, SanPham sp) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem itNhap = new JMenuItem("Nhập thêm hàng");
        itNhap.setFont(Theme.font(Font.PLAIN, 13));
        itNhap.addActionListener(e -> nhapHang(sp));

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
