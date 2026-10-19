package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.ChuoiUtil;
import Bus.HoSoThuCungBUS;
import Entity.HoSoThuCung;

/** Màn hình "Hồ sơ thú cưng" (menu Danh Mục): tìm kiếm, danh sách phân trang, xem / sửa / thêm hồ sơ. */
public class HoSoThuCungPanel extends JPanel {
    private static final int PAGE_SIZE = 4;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color HOVER_BG = new Color(0xF3F6F8);
    private static final int COL_THAO_TAC = 7;
    private static final String[] COLS = {"Mã hồ sơ", "Tên thú cưng", "Loài", "Giống", "Chủ nuôi", "Sức khỏe", "Cập nhật", "Thao tác"};

    private final HoSoThuCungBUS bus = new HoSoThuCungBUS();
    private List<HoSoThuCung> filtered = new ArrayList<>();
    private String tuKhoa = "";
    private int trang = 1;

    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final PlaceholderField txtTim = new PlaceholderField("Tên thú cưng / Mã hồ sơ / Số điện thoại chủ");
    private final JLabel lblGhiChu = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private final CardLayout tableCards = new CardLayout();
    private final JPanel tableArea = new JPanel(tableCards);

    public HoSoThuCungPanel() {
        super(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCard(), BorderLayout.CENTER);
        refresh();
    }

    // ---------------- Tiêu đề ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel t = new JLabel("Hồ sơ thú cưng");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Quản lý thông tin, tình trạng sức khỏe và lịch sử chăm sóc");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(4));
        left.add(s);

        RoundedButton btnThem = new RoundedButton("+ Thêm hồ sơ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(18, 11);
        btnThem.addActionListener(e -> moForm(null));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnThem);

        head.add(left, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);
        return head;
    }

    // ---------------- Thẻ chứa tìm kiếm + bảng ----------------
    private JComponent buildCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(16, 18, 6, 18));
        top.add(buildSearchRow());
        top.add(Box.createVerticalStrut(18));
        JLabel lbl = new JLabel("DANH SÁCH HỒ SƠ THÚ CƯNG");
        lbl.setFont(Theme.font(Font.BOLD, 11));
        lbl.setForeground(Theme.MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(lbl);
        card.add(top, BorderLayout.NORTH);

        tableArea.setOpaque(false);
        tableArea.setBorder(new EmptyBorder(0, 18, 0, 18));
        tableArea.add(buildTable(), "table");
        JLabel empty = new JLabel("Không tìm thấy hồ sơ phù hợp", SwingConstants.CENTER);
        empty.setFont(Theme.font(Font.PLAIN, 13));
        empty.setForeground(Theme.MUTED);
        tableArea.add(empty, "empty");
        card.add(tableArea, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(14, 18, 18, 18));
        lblGhiChu.setFont(Theme.font(Font.PLAIN, 11));
        lblGhiChu.setForeground(Theme.MUTED);
        pnlPages.setOpaque(false);
        foot.add(lblGhiChu, BorderLayout.WEST);
        foot.add(pnlPages, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildSearchRow() {
        RoundedPanel search = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        search.setLayout(new BorderLayout());
        search.setPreferredSize(new Dimension(0, 42));
        JLabel ic = new JLabel(Icons.of("search", 16, new Color(0x8795A0)));
        ic.setBorder(new EmptyBorder(0, 12, 0, 4));
        txtTim.setBorder(new EmptyBorder(0, 4, 0, 12));
        txtTim.setOpaque(false);
        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        search.add(ic, BorderLayout.WEST);
        search.add(txtTim, BorderLayout.CENTER);

        RoundedButton btn = new RoundedButton("Tìm kiếm", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(22, 0);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 42));
        Runnable tim = () -> { tuKhoa = txtTim.getText().trim(); trang = 1; refresh(); };
        btn.addActionListener(e -> tim.run());
        txtTim.addActionListener(e -> tim.run());

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(search, BorderLayout.CENTER);
        row.add(btn, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return row;
    }

    private JComponent buildTable() {
        table.setRowHeight(54);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] widths = {90, 140, 80, 140, 170, 140, 110, 110};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setCellRenderer(new TextCell(false, HoSoThuCung::getMaHoSo));
        table.getColumnModel().getColumn(1).setCellRenderer(new TextCell(true, HoSoThuCung::getTenThuCung));
        table.getColumnModel().getColumn(2).setCellRenderer(new TextCell(false, HoSoThuCung::getLoai));
        table.getColumnModel().getColumn(3).setCellRenderer(new TextCell(false, HoSoThuCung::getGiong));
        table.getColumnModel().getColumn(4).setCellRenderer(new TextCell(false, HoSoThuCung::getChuNuoi));
        table.getColumnModel().getColumn(5).setCellRenderer(new BadgeCell());
        table.getColumnModel().getColumn(6).setCellRenderer(new TextCell(false, h -> DATE.format(h.getCapNhat())));
        table.getColumnModel().getColumn(COL_THAO_TAC).setCellRenderer(new ActionCell());

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new HeaderCell());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int hit = hitAction(e.getPoint());
                if (hit == 0) return;
                HoSoThuCung hs = model.rows.get(table.rowAtPoint(e.getPoint()));
                if (hit == 1) xemHoSo(hs); else moForm(hs);
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                table.setCursor(Cursor.getPredefinedCursor(hitAction(e.getPoint()) != 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setColumnHeaderView(table.getTableHeader());
        SlimScrollBarUI.apply(sp);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /** 0 = không trúng; 1 = "Xem"; 2 = "Sửa". */
    private int hitAction(Point p) {
        int row = table.rowAtPoint(p), col = table.columnAtPoint(p);
        if (row < 0 || row >= model.rows.size() || col != COL_THAO_TAC) return 0;
        Rectangle r = table.getCellRect(row, col, false);
        return ActionCell.hit(table, p.x - r.x);
    }

    // ---------------- Tìm kiếm / phân trang ----------------
    private void refresh() {
        filtered = bus.tim(tuKhoa);
        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        if (filtered.isEmpty()) {
            tableCards.show(tableArea, "empty");
            lblGhiChu.setText("Không có hồ sơ nào khớp \"" + tuKhoa + "\"");
        } else {
            tableCards.show(tableArea, "table");
            lblGhiChu.setText("* Chọn \"Xem\" để mở chi tiết hồ sơ và tình trạng sức khỏe, \"Sửa\" để chỉnh sửa hồ sơ.");
        }
        buildPager(filtered.isEmpty() ? 0 : pages);
        revalidate();
        repaint();
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        for (int p : pageList(pages, trang)) {
            if (p < 0) {
                RoundedButton dots = new RoundedButton("...", Color.WHITE, Color.WHITE, Theme.MUTED, Theme.BORDER, 8);
                dots.setFont(Theme.font(Font.PLAIN, 13));
                dots.setPreferredSize(new Dimension(32, 32));
                dots.setEnabled(false);
                pnlPages.add(dots);
            } else {
                pnlPages.add(pageButton(p));
            }
        }
        pnlPages.revalidate();
        pnlPages.repaint();
    }

    /** Ví dụ: 1 2 3 ... 10. Giữ trang đầu, trang cuối và các trang quanh trang hiện tại; -1 là dấu "...". */
    static List<Integer> pageList(int pages, int cur) {
        List<Integer> r = new ArrayList<>();
        if (pages <= 0) return r;
        TreeSet<Integer> s = new TreeSet<>();
        s.add(1);
        s.add(pages);
        for (int p = cur - 1; p <= cur + 1; p++) if (p >= 1 && p <= pages) s.add(p);
        if (cur <= 3) for (int p = 1; p <= Math.min(3, pages); p++) s.add(p);
        if (cur >= pages - 2) for (int p = Math.max(1, pages - 2); p <= pages; p++) s.add(p);
        int prev = 0;
        for (int p : s) {
            if (prev != 0 && p - prev == 2) r.add(prev + 1);
            else if (prev != 0 && p - prev > 2) r.add(-1);
            r.add(p);
            prev = p;
        }
        return r;
    }

    private RoundedButton pageButton(int p) {
        boolean active = p == trang;
        RoundedButton b = active
                ? new RoundedButton(String.valueOf(p), Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8)
                : new RoundedButton(String.valueOf(p), Color.WHITE, HOVER_BG, Theme.TEXT, Theme.BORDER, 8);
        b.setFont(Theme.font(Font.PLAIN, 13));
        b.setPreferredSize(new Dimension(32, 32));
        b.addActionListener(e -> { trang = p; refresh(); });
        return b;
    }

    // ---------------- Xem chi tiết ----------------
    private void xemHoSo(HoSoThuCung h) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Hồ sơ " + h.getMaHoSo(), Dialog.ModalityType.APPLICATION_MODAL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(22, 26, 10, 26));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        String[][] rows = {
            {"Mã hồ sơ", h.getMaHoSo()}, {"Tên thú cưng", h.getTenThuCung()}, {"Loài", h.getLoai()},
            {"Giống", h.getGiong().isEmpty() ? "—" : h.getGiong()}, {"Chủ nuôi", h.getChuNuoi()},
            {"Số điện thoại chủ", h.getSdtChu()}, {"Sức khỏe", h.getSucKhoe()},
            {"Cập nhật gần nhất", DATE.format(h.getCapNhat())},
        };
        for (int i = 0; i < rows.length; i++) {
            JLabel k = new JLabel(rows[i][0]);
            k.setFont(Theme.font(Font.PLAIN, 13));
            k.setForeground(Theme.MUTED);
            JLabel v = new JLabel(rows[i][1]);
            v.setFont(Theme.font(Font.BOLD, 13));
            v.setForeground(rows[i][0].equals("Sức khỏe") ? badgeColors(h.getSucKhoe())[1] : Theme.TEXT);
            c.gridy = i;
            c.gridx = 0;
            c.insets = new Insets(0, 0, 12, 28);
            form.add(k, c);
            c.gridx = 1;
            c.insets = new Insets(0, 0, 12, 0);
            form.add(v, c);
        }

        RoundedButton sua = new RoundedButton("Sửa hồ sơ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(22, 9);
        RoundedButton dong = new RoundedButton("Đóng", Color.WHITE, HOVER_BG, Theme.TEXT, Theme.BORDER, 10).pad(24, 9);
        dong.setFont(Theme.font(Font.PLAIN, 13));
        sua.addActionListener(e -> { dlg.dispose(); moForm(h); });
        dong.addActionListener(e -> dlg.dispose());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(new EmptyBorder(4, 26, 18, 26));
        btns.add(dong);
        btns.add(sua);

        dlg.getRootPane().setDefaultButton(dong);
        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(380, 0));
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    // ---------------- Thêm / sửa hồ sơ ----------------
    /** @param sua hồ sơ cần sửa; null = thêm mới. */
    private void moForm(HoSoThuCung sua) {
        boolean them = sua == null;
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, them ? "Thêm hồ sơ thú cưng" : "Sửa hồ sơ " + sua.getMaHoSo(),
                Dialog.ModalityType.APPLICATION_MODAL);

        JTextField tfTen = field();
        JTextField tfGiong = field();
        JTextField tfChu = field();
        JTextField tfSdt = field();
        JComboBox<String> cbLoai = new JComboBox<>(HoSoThuCungBUS.LOAI);
        JComboBox<String> cbSk = new JComboBox<>(HoSoThuCungBUS.SUC_KHOE);
        for (JComboBox<String> cb : List.of(cbLoai, cbSk)) {
            cb.setFont(Theme.font(Font.PLAIN, 14));
            cb.setPreferredSize(new Dimension(320, 36));
        }
        if (!them) {
            tfTen.setText(sua.getTenThuCung());
            tfGiong.setText(sua.getGiong());
            tfChu.setText(sua.getChuNuoi());
            tfSdt.setText(sua.getSdtChu());
            cbLoai.setSelectedItem(sua.getLoai());
            cbSk.setSelectedItem(sua.getSucKhoe());
        }
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
        String[] labels = {"Tên thú cưng *", "Loài", "Giống", "Chủ nuôi *", "Số điện thoại chủ *", "Tình trạng sức khỏe"};
        JComponent[] inputs = {tfTen, cbLoai, tfGiong, tfChu, tfSdt, cbSk};
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
        RoundedButton cancel = new RoundedButton("Hủy", Color.WHITE, HOVER_BG, Theme.TEXT, Theme.BORDER, 10).pad(24, 9);
        cancel.setFont(Theme.font(Font.PLAIN, 13));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(new EmptyBorder(4, 24, 18, 24));
        btns.add(cancel);
        btns.add(ok);

        cancel.addActionListener(e -> dlg.dispose());
        ok.addActionListener(e -> {
            String ten = tfTen.getText().trim().replaceAll("\\s+", " ");
            String giong = tfGiong.getText().trim().replaceAll("\\s+", " ");
            String chu = tfChu.getText().trim().replaceAll("\\s+", " ");
            String sdt = ChuoiUtil.chiSo(tfSdt.getText());
            if (ten.isEmpty()) { err.setText("Vui lòng nhập tên thú cưng."); tfTen.requestFocus(); return; }
            if (chu.isEmpty()) { err.setText("Vui lòng nhập tên chủ nuôi."); tfChu.requestFocus(); return; }
            if (sdt.length() < 9 || sdt.length() > 11) { err.setText("Số điện thoại phải gồm 9–11 chữ số."); tfSdt.requestFocus(); return; }
            String loai = (String) cbLoai.getSelectedItem();
            String sk = (String) cbSk.getSelectedItem();
            if (them) {
                bus.them(ten, loai, giong, chu, sdt, sk);
                tuKhoa = "";
                txtTim.setText("");
                trang = 1;
            } else {
                bus.capNhat(sua, ten, loai, giong, chu, sdt, sk);
            }
            dlg.dispose();
            refresh();
        });

        dlg.getRootPane().setDefaultButton(ok);
        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(320, 36));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0xD5DCE1)), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }

    // ---------------- Màu nhãn sức khỏe ----------------
    /** {màu nền, màu chữ} */
    private static Color[] badgeColors(String sucKhoe) {
        return switch (sucKhoe) {
            case "Tốt" -> new Color[] {new Color(0xE3F4E8), new Color(0x2E9E4F)};
            case "Cần theo dõi" -> new Color[] {new Color(0xFFF1D6), new Color(0xB7791F)};
            case "Dị ứng thuốc" -> new Color[] {new Color(0xFDE8D9), new Color(0xC2571A)};
            default -> new Color[] {new Color(0xFBE3E3), new Color(0xD64545)}; // Đang điều trị
        };
    }

    // ---------------- Model + renderer ----------------
    private static class PageModel extends AbstractTableModel {
        List<HoSoThuCung> rows = new ArrayList<>();

        void setRows(List<HoSoThuCung> r) {
            rows = new ArrayList<>(r);
            fireTableDataChanged();
        }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public Object getValueAt(int r, int c) { return rows.get(r); }
        @Override public boolean isCellEditable(int r, int c) { return false; }
    }

    private static class HeaderCell extends JLabel implements TableCellRenderer {
        HeaderCell() {
            setOpaque(true);
            setBackground(new Color(0xF5F7F9));
            setForeground(Theme.MUTED);
            setFont(Theme.font(Font.BOLD, 12));
            setBorder(new EmptyBorder(0, 12, 0, 8));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            return this;
        }
    }

    private static class TextCell extends JLabel implements TableCellRenderer {
        private final boolean bold;
        private final java.util.function.Function<HoSoThuCung, String> fn;

        TextCell(boolean bold, java.util.function.Function<HoSoThuCung, String> fn) {
            this.bold = bold;
            this.fn = fn;
            setOpaque(true);
            setBackground(Color.WHITE);
            setForeground(Theme.TEXT);
            setFont(Theme.font(bold ? Font.BOLD : Font.PLAIN, 13));
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE), new EmptyBorder(0, 12, 0, 8)));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(fn.apply((HoSoThuCung) v));
            return this;
        }
    }

    private static class BadgeCell extends JPanel implements TableCellRenderer {
        private String text = "";
        BadgeCell() { setOpaque(true); setBackground(Color.WHITE); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            text = ((HoSoThuCung) v).getSucKhoe();
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.ROW_LINE);
            g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            Color[] col = badgeColors(text);
            g2.setFont(Theme.font(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text) + 20, h = 22, x = 12, y = (getHeight() - 1 - h) / 2;
            g2.setColor(col[0]);
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(col[1]);
            g2.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Ô thao tác: hai liên kết chữ "Xem" và "Sửa". */
    private static class ActionCell extends JPanel implements TableCellRenderer {
        private static final String[] LABELS = {"Xem", "Sửa"};
        private static final int PAD = 12, GAP = 14;

        ActionCell() { setOpaque(true); setBackground(Color.WHITE); }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return this;
        }

        /** x tính từ mép trái ô; trả về 1 (Xem), 2 (Sửa) hoặc 0. */
        static int hit(JComponent ref, int x) {
            FontMetrics fm = ref.getFontMetrics(Theme.font(Font.BOLD, 13));
            int cur = PAD;
            for (int i = 0; i < LABELS.length; i++) {
                int w = fm.stringWidth(LABELS[i]);
                if (x >= cur - 4 && x <= cur + w + 4) return i + 1;
                cur += w + GAP;
            }
            return 0;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.ROW_LINE);
            g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.ACCENT);
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - 1 - fm.getHeight()) / 2 + fm.getAscent();
            int cur = PAD;
            for (String l : LABELS) {
                g2.drawString(l, cur, y);
                cur += fm.stringWidth(l) + GAP;
            }
            g2.dispose();
        }
    }
}
