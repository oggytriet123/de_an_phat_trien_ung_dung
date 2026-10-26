package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.HoaDonBUS;
import Entity.HoaDon;

/** Màn hình "Lịch sử hóa đơn": các hóa đơn đã thanh toán thành công, có tìm kiếm, lọc ngày và xem chi tiết. */
public class LichSuHoaDonPanel extends JPanel implements Refreshable {
    private static final int PAGE_SIZE = 8;
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter NHAP_NGAY = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final String[] COLS = {"Mã hóa đơn", "Khách hàng", "Mã KH", "Số điện thoại", "Ngày thanh toán",
            "Tổng tiền", "Phương thức", "Trạng thái", "Thao tác"};
    private static final int COL_TONG_TIEN = 5;
    private static final int COL_THAO_TAC = 8;

    private final HoaDonBUS bus;
    private List<HoaDon> filtered = new ArrayList<>();
    private String tuKhoa = "";
    private LocalDate tu, den;
    private int trang = 1;

    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final PlaceholderField tfTim = new PlaceholderField("Tìm theo mã hóa đơn, tên khách, mã KH hoặc số điện thoại");
    private final PlaceholderField tfTu = new PlaceholderField("Từ ngày dd/MM/yyyy");
    private final PlaceholderField tfDen = new PlaceholderField("Đến ngày dd/MM/yyyy");
    private final FieldBox boxTu = new FieldBox(tfTu, Color.WHITE, 40);
    private final FieldBox boxDen = new FieldBox(tfDen, Color.WHITE, 40);
    private final JLabel lblLoi = new JLabel(" ");
    private final JLabel lblFooter = new JLabel();
    private final JLabel lblTrong = new JLabel("", SwingConstants.CENTER);
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private final CardLayout tableCards = new CardLayout();
    private final JPanel tableArea = new JPanel(tableCards);

    public LichSuHoaDonPanel(HoaDonBUS bus) {
        super(new BorderLayout(0, 18));
        this.bus = bus;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCard(), BorderLayout.CENTER);
        refresh();
    }

    @Override public void lamMoi() {
        refresh();
    }

    // ---------------- Tiêu đề ----------------
    private JComponent buildHeader() {
        JLabel t = new JLabel("Lịch sử hóa đơn");
        t.setFont(Theme.font(Font.BOLD, 26));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Tra cứu các hóa đơn đã thanh toán thành công.");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(t);
        head.add(Box.createVerticalStrut(4));
        head.add(s);
        return head;
    }

    // ---------------- Thẻ lọc + bảng ----------------
    private JComponent buildCard() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(16, 18, 6, 18));
        top.add(buildFilterRow());
        lblLoi.setFont(Theme.font(Font.PLAIN, 12));
        lblLoi.setForeground(Theme.DANGER);
        lblLoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblLoi.setBorder(new EmptyBorder(6, 0, 0, 0));
        top.add(lblLoi);
        card.add(top, BorderLayout.NORTH);

        tableArea.setOpaque(false);
        tableArea.setBorder(new EmptyBorder(0, 18, 0, 18));
        tableArea.add(buildTable(), "table");
        lblTrong.setFont(Theme.font(Font.PLAIN, 13));
        lblTrong.setForeground(Theme.MUTED);
        tableArea.add(lblTrong, "empty");
        card.add(tableArea, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(14, 18, 18, 18));
        lblFooter.setFont(Theme.font(Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);
        pnlPages.setOpaque(false);
        foot.add(lblFooter, BorderLayout.WEST);
        foot.add(pnlPages, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildFilterRow() {
        FieldBox tim = new FieldBox(tfTim, Color.WHITE, 40);
        boxTu.setPreferredSize(new Dimension(170, 40));
        boxDen.setPreferredSize(new Dimension(170, 40));

        RoundedButton btnTim = new RoundedButton("Tìm kiếm", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btnTim.setPreferredSize(new Dimension(110, 40));
        RoundedButton btnXoa = new RoundedButton("Xóa lọc", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        btnXoa.setFont(Theme.font(Font.PLAIN, 13));
        btnXoa.setPreferredSize(new Dimension(90, 40));
        btnTim.addActionListener(e -> timKiem());
        for (JTextField f : new JTextField[] {tfTim, tfTu, tfDen}) f.addActionListener(e -> timKiem());
        btnXoa.addActionListener(e -> {
            tfTim.setText("");
            tfTu.setText("");
            tfDen.setText("");
            timKiem();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(boxTu);
        right.add(boxDen);
        right.add(btnTim);
        right.add(btnXoa);
        right.setBorder(new EmptyBorder(0, 0, 0, -10));

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.add(tim, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return row;
    }

    private JComponent buildTable() {
        table.setRowHeight(52);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] widths = {100, 150, 70, 120, 140, 110, 100, 120, 70};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setCellRenderer(new TextCell(true, SwingConstants.LEFT, HoaDon::getMaHD));
        table.getColumnModel().getColumn(1).setCellRenderer(new TextCell(false, SwingConstants.LEFT, HoaDon::getTenKH));
        table.getColumnModel().getColumn(2).setCellRenderer(new TextCell(false, SwingConstants.LEFT,
                h -> h.getMaKH().isEmpty() ? "—" : h.getMaKH()));
        table.getColumnModel().getColumn(3).setCellRenderer(new TextCell(false, SwingConstants.LEFT, HoaDon::getSdt));
        table.getColumnModel().getColumn(4).setCellRenderer(new TextCell(false, SwingConstants.LEFT,
                h -> NGAY_GIO.format(h.getNgayThanhToan())));
        table.getColumnModel().getColumn(COL_TONG_TIEN).setCellRenderer(new TextCell(true, SwingConstants.RIGHT,
                h -> Theme.money(h.canThanhToan())));
        table.getColumnModel().getColumn(6).setCellRenderer(new TextCell(false, SwingConstants.LEFT, HoaDon::getPhuongThuc));
        table.getColumnModel().getColumn(7).setCellRenderer(new BadgeCell());
        table.getColumnModel().getColumn(COL_THAO_TAC).setCellRenderer(new ActionCell());

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new HeaderCell());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!trungXem(e.getPoint())) return;
                HoaDonPopups.chiTiet(LichSuHoaDonPanel.this, model.rows.get(table.rowAtPoint(e.getPoint())));
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                table.setCursor(Cursor.getPredefinedCursor(trungXem(e.getPoint()) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setColumnHeaderView(table.getTableHeader());
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        SlimScrollBarUI.apply(sp);
        return sp;
    }

    private boolean trungXem(Point p) {
        int row = table.rowAtPoint(p), col = table.columnAtPoint(p);
        if (row < 0 || row >= model.rows.size() || col != COL_THAO_TAC) return false;
        Rectangle r = table.getCellRect(row, col, false);
        return ActionCell.hit(table, p.x - r.x);
    }

    // ---------------- Tìm kiếm / phân trang ----------------
    private void timKiem() {
        LocalDate a, b;
        boxTu.setError(false);
        boxDen.setError(false);
        lblLoi.setText(" ");
        try {
            a = docNgay(tfTu.getText());
        } catch (DateTimeParseException ex) {
            boxTu.setError(true);
            lblLoi.setText("Ngày không hợp lệ. Nhập theo dạng dd/MM/yyyy, ví dụ 19/10/2026.");
            return;
        }
        try {
            b = docNgay(tfDen.getText());
        } catch (DateTimeParseException ex) {
            boxDen.setError(true);
            lblLoi.setText("Ngày không hợp lệ. Nhập theo dạng dd/MM/yyyy, ví dụ 19/10/2026.");
            return;
        }
        if (a != null && b != null && a.isAfter(b)) {
            boxTu.setError(true);
            boxDen.setError(true);
            lblLoi.setText("\"Từ ngày\" phải trước hoặc bằng \"Đến ngày\".");
            return;
        }
        tuKhoa = tfTim.getText().trim();
        tu = a;
        den = b;
        trang = 1;
        refresh();
    }

    private static LocalDate docNgay(String s) {
        String t = s == null ? "" : s.trim();
        return t.isEmpty() ? null : LocalDate.parse(t, NHAP_NGAY);
    }

    private void refresh() {
        filtered = bus.timLichSu(tuKhoa, tu, den);
        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        if (filtered.isEmpty()) {
            boolean coLoc = !tuKhoa.isEmpty() || tu != null || den != null;
            lblTrong.setText(coLoc ? "Không tìm thấy hóa đơn phù hợp."
                    : "Chưa có hóa đơn nào được thanh toán.");
            tableCards.show(tableArea, "empty");
            lblFooter.setText(" ");
        } else {
            tableCards.show(tableArea, "table");
            lblFooter.setText("Hiển thị " + (from + 1) + "–" + to + " trong " + filtered.size() + " hóa đơn");
        }
        buildPager(filtered.isEmpty() ? 0 : pages);
        revalidate();
        repaint();
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        for (int p : pageList(pages, trang)) {
            RoundedButton b;
            if (p < 0) {
                b = new RoundedButton("...", Color.WHITE, Color.WHITE, Theme.MUTED, Theme.BORDER, 8);
                b.setEnabled(false);
            } else if (p == trang) {
                b = new RoundedButton(String.valueOf(p), Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
            } else {
                b = new RoundedButton(String.valueOf(p), Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
            }
            b.setFont(Theme.font(Font.PLAIN, 13));
            b.setPreferredSize(new Dimension(32, 32));
            if (p > 0) b.addActionListener(e -> { trang = p; refresh(); });
            pnlPages.add(b);
        }
        pnlPages.revalidate();
        pnlPages.repaint();
    }

    /** Ví dụ: 1 2 3 ... 10. Giữ trang đầu, trang cuối và các trang quanh trang hiện tại; -1 là dấu "...". */
    private static List<Integer> pageList(int pages, int cur) {
        List<Integer> r = new ArrayList<>();
        if (pages <= 0) return r;
        java.util.TreeSet<Integer> s = new java.util.TreeSet<>();
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

    // ---------------- Model + renderer ----------------
    private static class PageModel extends AbstractTableModel {
        List<HoaDon> rows = new ArrayList<>();

        void setRows(List<HoaDon> r) {
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
            setBorder(new EmptyBorder(0, 12, 0, 12));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            setHorizontalAlignment(c == COL_TONG_TIEN ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return this;
        }
    }

    private static class TextCell extends JLabel implements TableCellRenderer {
        private final Function<HoaDon, String> fn;

        TextCell(boolean bold, int align, Function<HoaDon, String> fn) {
            this.fn = fn;
            setOpaque(true);
            setBackground(Color.WHITE);
            setForeground(Theme.TEXT);
            setFont(Theme.font(bold ? Font.BOLD : Font.PLAIN, 13));
            setHorizontalAlignment(align);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE), new EmptyBorder(0, 12, 0, 12)));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(fn.apply((HoaDon) v));
            return this;
        }
    }

    private static class BadgeCell extends JPanel implements TableCellRenderer {
        private String text = "";
        BadgeCell() { setOpaque(true); setBackground(Color.WHITE); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            text = ((HoaDon) v).getTrangThai();
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.ROW_LINE);
            g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            g2.setFont(Theme.font(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text) + 20, h = 22, x = 12, y = (getHeight() - 1 - h) / 2;
            g2.setColor(new Color(0xE3F4E8));
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(Theme.GREEN);
            g2.drawString(text, x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Ô thao tác: liên kết chữ "Xem". */
    private static class ActionCell extends JPanel implements TableCellRenderer {
        private static final String LABEL = "Xem";
        private static final int PAD = 12;

        ActionCell() { setOpaque(true); setBackground(Color.WHITE); }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return this;
        }

        static boolean hit(JComponent ref, int x) {
            int w = ref.getFontMetrics(Theme.font(Font.BOLD, 13)).stringWidth(LABEL);
            return x >= PAD - 4 && x <= PAD + w + 4;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.ROW_LINE);
            g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(LABEL, PAD, (getHeight() - 1 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
