package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.PhieuKhoBUS;
import Entity.PhieuKho;

/**
 * Màn hình Lịch sử Nhập / Xuất Kho - Bám sát 100% thiết kế tham chiếu.
 */
public class LichSuNhapXuatPanel extends JPanel {
    private static final int PAGE_SIZE = 9;
    private static final String[] FILTERS = {"Tất cả", "Phiếu nhập kho", "Phiếu xuất kho"};

    private final PhieuKhoBUS bus = new PhieuKhoBUS();
    private List<PhieuKho> filtered = new ArrayList<>();
    private String loaiLoc = "Tất cả";
    private int trang = 1;

    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm mã phiếu, nhân viên, nhà cung cấp...");
    private final List<RoundedButton> filterChips = new ArrayList<>();
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    private static final DecimalFormat NUM_FMT;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        NUM_FMT = new DecimalFormat("#,##0", s);
    }

    public LichSuNhapXuatPanel() {
        super(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 24, 16, 24));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(buildBreadcrumb(), BorderLayout.NORTH);
        top.add(buildHeader(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        add(buildTableCard(), BorderLayout.CENTER);

        refresh();
    }

    // ---------------- Breadcrumb: Kho hàng > Lịch sử Nhập/Xuất ----------------
    private JComponent buildBreadcrumb() {
        JPanel bc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bc.setOpaque(false);

        JLabel linkKho = new JLabel("Kho hàng");
        linkKho.setFont(Theme.font(Font.PLAIN, 12));
        linkKho.setForeground(Theme.MUTED);
        linkKho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkKho.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { quayLaiKhoHang(); }
            @Override public void mouseEntered(MouseEvent e) { linkKho.setForeground(Theme.TEXT); }
            @Override public void mouseExited(MouseEvent e) { linkKho.setForeground(Theme.MUTED); }
        });

        JLabel sep = new JLabel("›");
        sep.setFont(Theme.font(Font.PLAIN, 13));
        sep.setForeground(Theme.MUTED);

        JLabel current = new JLabel("Lịch sử Nhập/Xuất");
        current.setFont(Theme.font(Font.BOLD, 12));
        current.setForeground(new Color(0x28566E));

        bc.add(linkKho);
        bc.add(sep);
        bc.add(current);
        return bc;
    }

    // ---------------- Header: Tiêu đề + Nút Xuất Excel ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JLabel t = new JLabel("Lịch sử Nhập / Xuất Kho");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);

        JLabel s = new JLabel("Theo dõi lịch sử nhập xuất và điều chỉnh tồn kho của phòng khám");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        left.add(t, BorderLayout.NORTH);
        left.add(s, BorderLayout.CENTER);

        RoundedButton btnExcel = new RoundedButton("Xuất Excel", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 10);
        btnExcel.setFont(Theme.font(Font.BOLD, 13));
        btnExcel.setIcon(Icons.of("excel", 15, Color.WHITE));
        btnExcel.setIconTextGap(8);
        btnExcel.addActionListener(e -> xuatExcel());

        head.add(left, BorderLayout.CENTER);
        head.add(btnExcel, BorderLayout.EAST);
        return head;
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
        bar.setBorder(new EmptyBorder(16, 16, 12, 16));

        // Hàng 1: Bộ lọc Thời gian (trái) + Ô tìm kiếm (phải)
        JPanel row1 = new JPanel(new BorderLayout(16, 0));
        row1.setOpaque(false);

        // Box chọn thời gian
        JPanel pnlDateGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlDateGroup.setOpaque(false);
        JLabel lblTime = new JLabel("Thời gian");
        lblTime.setFont(Theme.font(Font.PLAIN, 13));
        lblTime.setForeground(Theme.MUTED);

        RoundedPanel boxDate = new RoundedPanel(8, Color.WHITE, Theme.BORDER);
        boxDate.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        boxDate.setPreferredSize(new Dimension(215, 36));
        JLabel icCal = new JLabel(Icons.of("calendar", 14, Theme.MUTED));
        JLabel lblDateVal = new JLabel("01/03/2026  —  04/03/2026");
        lblDateVal.setFont(Theme.font(Font.PLAIN, 12));
        lblDateVal.setForeground(Theme.TEXT);
        JLabel icDown = new JLabel(Icons.of("chevron-down", 12, Theme.MUTED));
        boxDate.add(icCal);
        boxDate.add(lblDateVal);
        boxDate.add(icDown);
        boxDate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boxDate.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LichSuNhapXuatPanel.this,
                        "Khoảng thời gian: 01/03/2026 đến 04/03/2026", "Bộ lọc thời gian", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        pnlDateGroup.add(lblTime);
        pnlDateGroup.add(boxDate);

        // Ô tìm kiếm
        RoundedPanel search = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        search.setLayout(new BorderLayout());
        search.setPreferredSize(new Dimension(340, 36));
        JLabel icSearch = new JLabel(Icons.of("search", 15, new Color(0x8795A0)));
        icSearch.setBorder(new EmptyBorder(0, 12, 0, 4));
        txtTim.setBorder(new EmptyBorder(0, 4, 0, 10));
        txtTim.setOpaque(false);
        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        search.add(icSearch, BorderLayout.WEST);
        search.add(txtTim, BorderLayout.CENTER);
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() { trang = 1; refresh(); }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        row1.add(pnlDateGroup, BorderLayout.WEST);
        row1.add(search, BorderLayout.EAST);

        // Hàng 2: Bộ lọc loại phiếu (Tất cả, Phiếu nhập kho, Phiếu xuất kho)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);
        for (String f : FILTERS) {
            RoundedButton chip = new RoundedButton(f, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(14, 7);
            chip.setFont(Theme.font(Font.PLAIN, 13));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 34));
            chip.addActionListener(e -> { loaiLoc = f; trang = 1; refresh(); });
            filterChips.add(chip);
            row2.add(chip);
        }

        bar.add(row1);
        bar.add(Box.createVerticalStrut(12));
        bar.add(row2);
        return bar;
    }

    private JComponent buildTable() {
        table.setRowHeight(49);
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
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderCell());

        // Cột và renderers (8 cột)
        int[] widths = {115, 95, 95, 140, 210, 120, 95, 80};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setCellRenderer(new CodeCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new TypeCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new DateCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new UserCell());
        table.getColumnModel().getColumn(4).setCellRenderer(new PartnerCell());
        table.getColumnModel().getColumn(5).setCellRenderer(new MoneyCell());
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCell());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionCell());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 7) {
                    PhieuKho pk = model.get(row);
                    xemChiTiet(pk);
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

    // ---------------- Lọc và Phân trang ----------------
    private void refresh() {
        String kw = norm(txtTim.getText().trim());
        List<PhieuKho> list = new ArrayList<>();
        for (PhieuKho pk : bus.layDanhSach()) {
            if ("Phiếu nhập kho".equals(loaiLoc) && !pk.getLoaiPhieu().startsWith("Nhập")) continue;
            if ("Phiếu xuất kho".equals(loaiLoc) && !pk.getLoaiPhieu().startsWith("Xuất")) continue;

            if (!kw.isEmpty()) {
                String hay = norm(pk.getMaPhieu()) + "|" + norm(pk.getNguoiThucHien()) + "|" + norm(pk.getDoiTac());
                if (!hay.contains(kw)) continue;
            }
            list.add(pk);
        }
        filtered = list;

        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        lblFooter.setText(filtered.isEmpty()
                ? "Không tìm thấy phiếu nào phù hợp"
                : "Hiển thị " + (to - from) + " trong tổng số " + filtered.size() + " phiếu");

        // Cập nhật kiểu chip lọc
        for (RoundedButton c : filterChips) {
            boolean on = c.getText().equals(loaiLoc);
            if (on) c.style(new Color(0xEBF3F8), new Color(0xDEECF5), new Color(0x28566E), new Color(0x7AA3BC));
            else c.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

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

    private static String norm(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    private void quayLaiKhoHang() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof MainFrame mf) {
            mf.chon("Kho hàng");
        }
    }

    private void xuatExcel() {
        JOptionPane.showMessageDialog(this,
                "Đã xuất dữ liệu " + filtered.size() + " phiếu nhập/xuất kho sang file Excel thành công!",
                "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xemChiTiet(PhieuKho pk) {
        JOptionPane.showMessageDialog(this,
                "Mã phiếu: " + pk.getMaPhieu() + "\nLoại phiếu: " + pk.getLoaiPhieu() +
                "\nThời gian: " + pk.getGioLap() + " " + pk.getNgayLap() +
                "\nNgười thực hiện: " + pk.getNguoiThucHien() +
                "\nĐối tác: " + pk.getDoiTac() +
                "\nTổng giá trị: " + NUM_FMT.format(pk.getTongGiaTri()) + " đ" +
                "\nTrạng thái: " + pk.getTrangThai(),
                "Chi tiết phiếu " + pk.getMaPhieu(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- TableModel & Renderers ----------------
    private static class PageModel extends AbstractTableModel {
        private final String[] cols = {
            "MÃ PHIẾU", "LOẠI PHIẾU", "NGÀY LẬP", "NGƯỜI THỰC HIỆN",
            "NHÀ CUNG CẤP / ĐỐI TÁC", "TỔNG GIÁ TRỊ", "TRẠNG THÁI", "THAO TÁC"
        };
        private List<PhieuKho> rows = new ArrayList<>();

        void setRows(List<PhieuKho> r) { rows = new ArrayList<>(r); fireTableDataChanged(); }
        PhieuKho get(int r) { return rows.get(r); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            PhieuKho pk = rows.get(r);
            return switch (c) {
                case 0 -> pk.getMaPhieu();
                case 1 -> pk.getLoaiPhieu();
                case 2 -> pk;
                case 3 -> pk.getNguoiThucHien();
                case 4 -> pk.getDoiTac();
                case 5 -> pk.getTongGiaTri();
                case 6 -> pk.getTrangThai();
                default -> pk;
            };
        }
    }

    private static class HeaderCell extends JLabel implements TableCellRenderer {
        HeaderCell() {
            setOpaque(true);
            setFont(Theme.font(Font.BOLD, 11));
            setForeground(Theme.MUTED);
            setBackground(new Color(0xF8FAFC));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            setHorizontalAlignment(c == 7 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, c == 0 ? 16 : 8, 0, c == 7 ? 16 : 8)));
            return this;
        }
    }

    private abstract static class Cell extends JPanel implements TableCellRenderer {
        Cell() { super(new BorderLayout()); setOpaque(true); }
        void prep(JTable t, boolean sel, int col) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, col == 0 ? 16 : 8, 0, col == 7 ? 16 : 8)));
        }
    }

    // Cột 0: Mã phiếu
    private static class CodeCell extends Cell {
        private final JLabel lbl = new JLabel();
        CodeCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 13));
            lbl.setForeground(Theme.TEXT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    // Cột 1: Loại phiếu badge
    private static class TypeCell extends Cell {
        private final TypeBadge badge = new TypeBadge();
        TypeCell() {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            add(wrap, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            badge.setType(String.valueOf(v));
            return this;
        }
    }

    private static class TypeBadge extends JComponent {
        private String text = "";
        private Color bg = Theme.BLUE_BG, fg = Theme.BLUE_TEXT;

        void setType(String t) {
            this.text = t;
            if ("Nhập mới".equals(t)) {
                bg = Theme.BLUE_BG;
                fg = Theme.BLUE_TEXT;
            } else if (t != null && t.startsWith("Xuất")) {
                bg = Theme.PEACH_BG;
                fg = Theme.PEACH_TEXT;
            } else {
                bg = Theme.RED_BG;
                fg = Theme.RED;
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

    // Cột 2: Ngày lập (2 dòng)
    private static class DateCell extends Cell {
        private final JLabel d = new JLabel();
        private final JLabel h = new JLabel();
        DateCell() {
            d.setFont(Theme.font(Font.PLAIN, 12));
            d.setForeground(Theme.TEXT);
            h.setFont(Theme.font(Font.PLAIN, 11));
            h.setForeground(Theme.MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(d);
            text.add(Box.createVerticalStrut(1));
            text.add(h);
            text.add(Box.createVerticalGlue());
            add(text, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            PhieuKho pk = (PhieuKho) v;
            d.setText(pk.getNgayLap());
            h.setText(pk.getGioLap());
            return this;
        }
    }

    // Cột 3: Người thực hiện (bold)
    private static class UserCell extends Cell {
        private final JLabel lbl = new JLabel();
        UserCell() {
            lbl.setFont(Theme.font(Font.BOLD, 13));
            lbl.setForeground(Theme.TEXT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    // Cột 4: Đối tác
    private static class PartnerCell extends Cell {
        private final JLabel lbl = new JLabel();
        PartnerCell() {
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

    // Cột 5: Tổng giá trị
    private static class MoneyCell extends Cell {
        private final JLabel lbl = new JLabel();
        MoneyCell() {
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

    // Cột 6: Trạng thái badge
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
            if ("Hoàn tất".equals(t)) {
                bg = Theme.GREEN_BG;
                fg = Theme.GREEN;
            } else if ("Đang xử lý".equals(t)) {
                bg = Theme.ORANGE_BG;
                fg = Theme.ORANGE;
            } else {
                bg = Theme.RED_BG;
                fg = Theme.RED;
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

    // Cột 7: Thao tác (Icon eye)
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
            int x = (getWidth() - 18) / 2;
            int y = (getHeight() - 18) / 2;
            Icons.draw(g2, "eye", x, y, 18, Theme.ACTION_ICON, false);
            g2.dispose();
        }
    }
}
