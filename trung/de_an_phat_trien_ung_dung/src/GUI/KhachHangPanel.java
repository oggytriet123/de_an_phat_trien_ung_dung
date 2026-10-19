package GUI;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Collator;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
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

import Bus.KhachHangBUS;
import Entity.KhachHang;

/** Màn hình Quản lý Khách hàng. */
public class KhachHangPanel extends JPanel {
    private static final int PAGE_SIZE = 7;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] HANG = {"Tất cả", "VIP", "Vàng", "Bạc", "Thường"};
    private static final String[] SORTS = {"Điểm cao nhất", "Chi tiêu cao nhất", "Giao dịch gần nhất", "Tên A-Z"};

    private final KhachHangBUS bus = new KhachHangBUS();
    private List<KhachHang> filtered = new ArrayList<>();
    private String hangLoc = "Tất cả";
    private String sapXep = SORTS[0];
    private int trang = 1;

    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm theo tên, số điện thoại, mã thẻ...");
    private final List<RoundedButton> chips = new ArrayList<>();
    private final RoundedButton btnSort = new RoundedButton("", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10);
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public KhachHangPanel() {
        super(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildStats(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        refresh();
    }

    // ---------------- Tiêu đề + nút ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel t = new JLabel("Quản lý Khách hàng");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Hồ sơ khách hàng, thẻ thành viên, điểm tích lũy và lịch sử giao dịch");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(4));
        left.add(s);

        RoundedButton btnExcel = new RoundedButton("Xuất Excel", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(18, 11);
        RoundedButton btnThem = new RoundedButton("+ Thêm khách hàng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(18, 11);
        btnExcel.addActionListener(e -> xuatExcel());
        btnThem.addActionListener(e -> themKhachHang());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnExcel);
        right.add(btnThem);

        head.add(left, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);
        return head;
    }

    // ---------------- 4 thẻ thống kê ----------------
    private JComponent buildStats() {
        // Số liệu tổng hợp: tạm là giá trị mẫu, sau này lấy từ DAO.
        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setOpaque(false);
        row.add(statCard("Tổng khách hàng", "1.248", "+37 khách mới trong tháng"));
        row.add(statCard("Thành viên VIP", "86", "6,9% tổng khách hàng"));
        row.add(statCard("Điểm đang lưu hành", "482.350", "+12.480 điểm tuần này"));
        row.add(statCard("Doanh thu từ thành viên", "312,5 triệu₫", "+8,2% so với tháng trước"));
        return row;
    }

    private JComponent statCard(String label, String value, String note) {
        RoundedPanel p = new RoundedPanel(16, Theme.CARD, null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(Theme.font(Font.BOLD, 22));
        v.setForeground(Theme.TEXT);
        JLabel n = new JLabel(note);
        n.setFont(Theme.font(Font.PLAIN, 11));
        n.setForeground(Theme.GREEN);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(v);
        p.add(Box.createVerticalStrut(6));
        p.add(n);
        return p;
    }

    // ---------------- Thẻ bảng ----------------
    private JComponent buildTableCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, null);
        card.setLayout(new BorderLayout());
        card.add(buildToolbar(), BorderLayout.NORTH);
        card.add(buildTable(), BorderLayout.CENTER);
        card.add(buildFooter(), BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Ô tìm kiếm
        RoundedPanel search = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        search.setLayout(new BorderLayout());
        search.setPreferredSize(new Dimension(300, 36));
        JLabel ic = new JLabel(Icons.of("search", 16, new Color(0x8795A0)));
        ic.setBorder(new EmptyBorder(0, 10, 0, 4));
        txtTim.setBorder(new EmptyBorder(0, 4, 0, 8));
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

        // Chip lọc hạng thẻ
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(search);
        for (String h : HANG) {
            RoundedButton chip = new RoundedButton(h, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(15, 8);
            chip.setFont(Theme.font(Font.PLAIN, 13));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 36));
            chip.addActionListener(e -> { hangLoc = h; trang = 1; refresh(); });
            chips.add(chip);
            left.add(chip);
        }

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
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnSort);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JComponent buildTable() {
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0xEAF0F4));
        table.setSelectionForeground(Theme.TEXT);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] widths = {250, 90, 120, 150, 170, 170};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setCellRenderer(new CustomerCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new BadgeCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new PointsCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new TextCell(SwingConstants.LEFT, false, v -> String.valueOf(v)));
        table.getColumnModel().getColumn(4).setCellRenderer(new TextCell(SwingConstants.LEFT, false, v -> String.valueOf(v)));
        table.getColumnModel().getColumn(5).setCellRenderer(new TextCell(SwingConstants.RIGHT, true, v -> Theme.money((Long) v)));

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new HeaderCell());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JComponent buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.ROW_LINE), new EmptyBorder(14, 16, 14, 16)));
        lblFooter.setFont(Theme.font(Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);
        pnlPages.setOpaque(false);
        f.add(lblFooter, BorderLayout.WEST);
        f.add(pnlPages, BorderLayout.EAST);
        return f;
    }

    // ---------------- Lọc / sắp xếp / phân trang ----------------
    private void refresh() {
        String kw = norm(txtTim.getText().trim());
        List<KhachHang> list = new ArrayList<>();
        for (KhachHang k : bus.layDanhSach()) {
            if (!hangLoc.equals("Tất cả") && !k.getHangThe().equals(hangLoc)) continue;
            if (!kw.isEmpty()) {
                String hay = norm(k.getHoTen()) + "|" + KhachHangBUS.chiSo(k.getSoDienThoai()) + "|" + norm(k.getMaKH());
                if (!hay.contains(kw) && !hay.contains(KhachHangBUS.chiSo(kw).isEmpty() ? "\u0000" : KhachHangBUS.chiSo(kw))) continue;
            }
            list.add(k);
        }
        list.sort(comparator());
        filtered = list;

        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        lblFooter.setText(filtered.isEmpty()
                ? "Không tìm thấy khách hàng phù hợp"
                : "Hiển thị " + (from + 1) + "–" + to + " trong " + Theme.number(filtered.size()) + " khách hàng");
        btnSort.setText("Sắp xếp: " + sapXep);
        btnSort.setPreferredSize(null);
        for (RoundedButton c : chips) {
            boolean on = c.getText().equals(hangLoc);
            if (on) c.style(Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null);
            else c.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }
        buildPager(pages);
        revalidate();
        repaint();
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        pnlPages.add(pageButton("‹", trang - 1, trang > 1, false));
        int start = Math.max(1, Math.min(trang - 2, pages - 4));
        int end = Math.min(pages, start + 4);
        for (int p = start; p <= end; p++) pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
        pnlPages.add(pageButton("›", trang + 1, trang < pages, false));
        pnlPages.revalidate();
        pnlPages.repaint();
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

    private Comparator<KhachHang> comparator() {
        return switch (sapXep) {
            case "Chi tiêu cao nhất" -> Comparator.comparingLong(KhachHang::getTongChiTieu).reversed();
            case "Giao dịch gần nhất" -> Comparator.comparing(KhachHang::getGiaoDichGanNhat,
                    Comparator.nullsLast(Comparator.<LocalDate>naturalOrder().reversed()));
            case "Tên A-Z" -> {
                Collator col = Collator.getInstance(Locale.of("vi"));
                yield Comparator.comparing((KhachHang k) -> tenGoi(k.getHoTen()), col)
                        .thenComparing(KhachHang::getHoTen, col);
            }
            default -> Comparator.comparingInt(KhachHang::getDiemTichLuy).reversed();
        };
    }

    private static String tenGoi(String hoTen) {
        String[] w = hoTen.trim().split("\\s+");
        return w[w.length - 1];
    }

    /** Bỏ dấu tiếng Việt + chữ thường để tìm kiếm không phân biệt dấu. */
    private static String norm(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // ---------------- Thêm khách hàng ----------------
    private void themKhachHang() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Thêm khách hàng", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField tfTen = field();
        JTextField tfSdt = field();
        JTextField tfPet = field();
        JComboBox<String> cbHang = new JComboBox<>(new String[] {"Thường", "Bạc", "Vàng", "VIP"});
        cbHang.setFont(Theme.font(Font.PLAIN, 14));
        JLabel err = new JLabel(" ");
        err.setFont(Theme.font(Font.PLAIN, 12));
        err.setForeground(Theme.RED);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        String[] labels = {"Họ tên *", "Số điện thoại *", "Hạng thẻ", "Thú cưng (nếu có)"};
        JComponent[] inputs = {tfTen, tfSdt, cbHang, tfPet};
        int y = 0;
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(Theme.font(Font.BOLD, 13));
            c.gridy = y++;
            c.insets = new Insets(0, 0, 4, 0);
            form.add(l, c);
            c.gridy = y++;
            c.insets = new Insets(0, 0, 12, 0);
            form.add(inputs[i], c);
        }
        c.gridy = y;
        c.insets = new Insets(0, 0, 0, 0);
        form.add(err, c);

        RoundedButton ok = new RoundedButton("Lưu", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(24, 9);
        RoundedButton cancel = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(24, 9);
        cancel.setFont(Theme.font(Font.PLAIN, 13));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(new EmptyBorder(4, 24, 18, 24));
        btns.add(cancel);
        btns.add(ok);

        cancel.addActionListener(e -> dlg.dispose());
        ok.addActionListener(e -> {
            String ten = tfTen.getText().trim().replaceAll("\\s+", " ");
            String sdt = KhachHangBUS.chiSo(tfSdt.getText());
            if (ten.isEmpty()) { err.setText("Vui lòng nhập họ tên."); tfTen.requestFocus(); return; }
            if (sdt.length() < 9 || sdt.length() > 11) { err.setText("Số điện thoại phải gồm 9–11 chữ số."); tfSdt.requestFocus(); return; }
            if (bus.tonTaiSoDienThoai(sdt)) { err.setText("Số điện thoại này đã tồn tại."); tfSdt.requestFocus(); return; }
            bus.them(ten, sdt, (String) cbHang.getSelectedItem(), tfPet.getText().trim());
            dlg.dispose();
            txtTim.setText("");
            hangLoc = "Tất cả";
            sapXep = "Điểm cao nhất";
            refresh();
        });

        dlg.getRootPane().setDefaultButton(ok);
        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setSize(400, 480);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(300, 36));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0xD5DCE1)), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }

    // ---------------- Xuất file (CSV mở được bằng Excel) ----------------
    private void xuatExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Xuất danh sách khách hàng");
        fc.setSelectedFile(new java.io.File("KhachHang.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new java.io.File(file.getPath() + ".csv");
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF'); // BOM để Excel đọc đúng tiếng Việt
            w.write("Mã KH,Họ tên,Số điện thoại,Hạng thẻ,Điểm tích lũy,Thú cưng,Giao dịch gần nhất,Tổng chi tiêu\r\n");
            for (KhachHang k : filtered) {
                w.write(String.join(",",
                        csv(k.getMaKH()), csv(k.getHoTen()),
                        csv("=\"" + KhachHang_sdt(k) + "\""),
                        csv(k.getHangThe()), String.valueOf(k.getDiemTichLuy()), csv(k.getThuCung()),
                        k.getGiaoDichGanNhat() == null ? "" : k.getGiaoDichGanNhat().format(DATE),
                        String.valueOf(k.getTongChiTieu())));
                w.write("\r\n");
            }
            JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " khách hàng:\n" + file.getAbsolutePath(),
                    "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String KhachHang_sdt(KhachHang k) { return KhachHangBUS.chiSo(k.getSoDienThoai()); }

    private static String csv(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }

    // ---------------- Model + renderer ----------------
    private static class PageModel extends AbstractTableModel {
        private final String[] cols = {"Khách hàng", "Hạng thẻ", "Điểm tích lũy", "Thú cưng", "Giao dịch gần nhất", "Tổng chi tiêu"};
        private List<KhachHang> rows = new ArrayList<>();

        void setRows(List<KhachHang> r) { rows = new ArrayList<>(r); fireTableDataChanged(); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            KhachHang k = rows.get(r);
            return switch (c) {
                case 0 -> k;
                case 1 -> k.getHangThe();
                case 2 -> k.getDiemTichLuy();
                case 3 -> k.getThuCung();
                case 4 -> k.getGiaoDichGanNhat() == null ? "—" : k.getGiaoDichGanNhat().format(DATE);
                default -> k.getTongChiTieu();
            };
        }
    }

    private static class HeaderCell extends JLabel implements TableCellRenderer {
        HeaderCell() {
            setOpaque(true);
            setFont(Theme.font(Font.PLAIN, 12));
            setForeground(Theme.MUTED);
            setBackground(new Color(0xF1F5F8));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            setHorizontalAlignment(c == 5 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, c == 0 ? 16 : 8, 0, c == 5 ? 16 : 8)));
            return this;
        }
    }

    /** Nền + viền dưới + lề trái/phải chung cho mọi ô. */
    private abstract static class Cell extends JPanel implements TableCellRenderer {
        Cell() { super(new BorderLayout()); setOpaque(true); }

        void prep(JTable t, boolean sel, int col) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, col == 0 ? 16 : 8, 0, col == 5 ? 16 : 8)));
        }
    }

    private static class TextCell extends Cell {
        private final JLabel lbl = new JLabel();
        private final Function<Object, String> fmt;

        TextCell(int align, boolean bold, Function<Object, String> fmt) {
            this.fmt = fmt;
            lbl.setHorizontalAlignment(align);
            lbl.setFont(Theme.font(bold ? Font.BOLD : Font.PLAIN, 13));
            lbl.setForeground(Theme.TEXT);
            add(lbl);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText(fmt.apply(v));
            return this;
        }
    }

    private static class PointsCell extends Cell {
        private final JLabel lbl = new JLabel();
        PointsCell() { lbl.setFont(Theme.font(Font.PLAIN, 13)); lbl.setForeground(Theme.TEXT); add(lbl); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText("<html><b>" + Theme.number((Integer) v) + "</b> <font color='#6B7A86' size='2'>điểm</font></html>");
            return this;
        }
    }

    private static class BadgeCell extends Cell {
        private final Badge badge = new Badge();
        BadgeCell() { setLayout(new GridBagLayout()); GridBagConstraints g = new GridBagConstraints(); g.weightx = 1; g.anchor = GridBagConstraints.WEST; add(badge, g); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            badge.set(String.valueOf(v));
            return this;
        }
    }

    private static class Badge extends JComponent {
        private String text = "";
        private Color bg = Color.LIGHT_GRAY, fg = Color.DARK_GRAY;

        void set(String t) {
            text = t;
            switch (t) {
                case "VIP" -> { bg = new Color(0xDCEAF8); fg = new Color(0x2B6CB0); }
                case "Vàng" -> { bg = new Color(0xFDEBD3); fg = new Color(0xC26A00); }
                case "Bạc" -> { bg = new Color(0xE3E8EC); fg = new Color(0x4B5865); }
                default -> { bg = new Color(0xE9EDF0); fg = new Color(0x5A6772); }
            }
        }
        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.BOLD, 11));
            return new Dimension(fm.stringWidth(text) + 20, 22);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(fg);
            g2.setFont(Theme.font(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private static class CustomerCell extends Cell {
        private final Avatar avatar = new Avatar();
        private final JLabel name = new JLabel();
        private final JLabel phone = new JLabel();

        CustomerCell() {
            name.setFont(Theme.font(Font.BOLD, 13));
            name.setForeground(Theme.TEXT);
            phone.setFont(Theme.font(Font.PLAIN, 11));
            phone.setForeground(Theme.MUTED);
            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(name);
            text.add(phone);
            text.add(Box.createVerticalGlue());
            add(avatar, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            KhachHang k = (KhachHang) v;
            avatar.initials = initials(k.getHoTen());
            name.setText(k.getHoTen());
            phone.setText(k.getSoDienThoai());
            return this;
        }
    }

    private static class Avatar extends JComponent {
        String initials = "";
        Avatar() { setPreferredSize(new Dimension(46, 34)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int y = (getHeight() - 34) / 2;
            g2.setColor(new Color(0xD3DBE1));
            g2.fillOval(0, y, 34, 34);
            g2.setColor(new Color(0x3C4B57));
            g2.setFont(Theme.font(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials, (34 - fm.stringWidth(initials)) / 2, y + (34 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    private static String initials(String hoTen) {
        String[] w = hoTen.trim().split("\\s+");
        if (w.length == 1) return w[0].substring(0, 1).toUpperCase();
        return (w[0].substring(0, 1) + w[w.length - 1].substring(0, 1)).toUpperCase();
    }
}
