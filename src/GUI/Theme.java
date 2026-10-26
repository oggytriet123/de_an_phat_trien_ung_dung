package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Màu sắc, font và định dạng dùng chung cho toàn bộ giao diện. */
public final class Theme {
    private Theme() {}

    public static final Color SIDEBAR        = new Color(0x5B7B8C);
    public static final Color SIDEBAR_HOVER  = new Color(255, 255, 255, 30);
    public static final Color SIDEBAR_ACTIVE = new Color(255, 255, 255, 60);
    public static final Color ACCENT         = new Color(0x56788A);
    public static final Color ACCENT_HOVER   = new Color(0x466778);
    public static final Color DARK           = new Color(0x1E3442);
    public static final Color BG             = new Color(0xF3F6F8);
    public static final Color CARD           = Color.WHITE;
    public static final Color BORDER         = new Color(0xDDE3E8);
    public static final Color ROW_LINE       = new Color(0xE9EDF0);
    public static final Color TEXT           = new Color(0x1F2A33);
    public static final Color MUTED          = new Color(0x6B7A86);
    public static final Color GREEN          = new Color(0x2E9E4F);
    public static final Color RED            = new Color(0xD64545);

    // Màu của nhóm màn hình hóa đơn (nút chính xanh dương, Lưu xanh lá, Hủy đỏ)
    public static final Color PRIMARY        = new Color(0x2A5CB8);
    public static final Color PRIMARY_HOVER  = new Color(0x224C9A);
    public static final Color PRIMARY_SOFT   = new Color(0xE9F0FC);
    public static final Color PRIMARY_LINE   = new Color(0x9DB9E8);
    public static final Color SUCCESS        = new Color(0x1F8A4F);
    public static final Color SUCCESS_HOVER  = new Color(0x18703F);
    public static final Color DANGER         = new Color(0xC93B3B);
    public static final Color DANGER_HOVER   = new Color(0xAD2F2F);
    public static final Color AMBER_BG       = new Color(0xFFF8E8);
    public static final Color AMBER_BORDER   = new Color(0xF0C060);
    public static final Color AMBER_PILL     = new Color(0xFDEBC4);
    public static final Color AMBER_TEXT     = new Color(0xB7791F);
    public static final Color FIELD_BG       = new Color(0xF3F6F8);

    public static final String FONT = "Segoe UI";

    public static Font font(int style, int size) {
        return new Font(FONT, style, size);
    }

    private static final DecimalFormat NUM;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        NUM = new DecimalFormat("#,##0", s);
    }

    public static synchronized String number(long v) { return NUM.format(v); }
    /** Dialog "Hồ sơ" dùng chung: header (tên, phụ đề, nhãn) + các dòng nhãn/giá trị + nút Đóng. */
    public static void hoSoDialog(Window owner, String name, String sub, String badgeText,
                                  String[][] rows, boolean lastBold, int height) {
        JDialog dlg = new JDialog(owner, "Hồ sơ", Dialog.ModalityType.APPLICATION_MODAL);

        JLabel lblTen = new JLabel(name);
        lblTen.setFont(font(Font.BOLD, 18));
        lblTen.setForeground(TEXT);
        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(font(Font.PLAIN, 12));
        lblSub.setForeground(MUTED);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(lblTen); titles.add(lblSub);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(FIELD_BG);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(16, 24, 16, 24)));
        header.add(titles, BorderLayout.CENTER);
        if (badgeText != null && !badgeText.isBlank()) {
            JLabel badge = new JLabel(badgeText, SwingConstants.CENTER);
            badge.setFont(font(Font.BOLD, 12));
            badge.setForeground(PRIMARY);
            badge.setOpaque(true);
            badge.setBackground(PRIMARY_SOFT);
            badge.setBorder(new CompoundBorder(new LineBorder(PRIMARY_LINE, 1, true), new EmptyBorder(4, 12, 4, 12)));
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
            wrap.setOpaque(false);
            wrap.add(badge);
            header.add(wrap, BorderLayout.EAST);
        }

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(8, 24, 8, 24));
        for (int i = 0; i < rows.length; i++) {
            boolean strong = lastBold && i == rows.length - 1;
            String val = rows[i][1] == null || rows[i][1].isBlank() ? "—" : rows[i][1];
            JLabel l = new JLabel(rows[i][0]);
            l.setFont(font(Font.PLAIN, 13));
            l.setForeground(MUTED);
            l.setVerticalAlignment(SwingConstants.TOP);
            String esc = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            JLabel v = new JLabel("<html><div style='text-align:right;width:250px'>" + esc + "</div></html>");
            v.setFont(font(strong ? Font.BOLD : Font.PLAIN, strong ? 15 : 13));
            v.setForeground(TEXT);
            JPanel row = new JPanel(new BorderLayout(16, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, ROW_LINE), new EmptyBorder(11, 0, 11, 0)));
            row.add(l, BorderLayout.WEST);
            row.add(v, BorderLayout.EAST);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            body.add(row);
        }

        RoundedButton btnDong = new RoundedButton("Đóng", ACCENT, ACCENT_HOVER, Color.WHITE, null, 10).pad(24, 9);
        btnDong.addActionListener(e -> dlg.dispose());
        JPanel pnlDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 12));
        pnlDong.setBackground(Color.WHITE);
        pnlDong.add(btnDong);

        dlg.setLayout(new BorderLayout());
        dlg.add(header, BorderLayout.NORTH);
        dlg.add(body, BorderLayout.CENTER);
        dlg.add(pnlDong, BorderLayout.SOUTH);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setSize(480, height);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    public static javax.swing.JLabel boldLbl(String text) {
        javax.swing.JLabel l = new javax.swing.JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }
    public static String money(long v) { return number(v) + "₫"; }

    // =========================================================================
    // ĐỊNH DẠNG BẢNG (JTABLE) KẺ LƯỚI ĐẸP, RÕ RÀNG VÀ CHUYÊN NGHIỆP
    // =========================================================================

    /** Tùy biến bảng chính với đường kẻ lưới nhẹ nhàng, header trang nhã và viền bao quanh. */
    public static void styleTable(JTable table, JScrollPane scroll) {
        styleTable(table, scroll, 40);
    }

    /** Tùy biến bảng với độ cao dòng chỉ định (dùng cho cả bảng chính và dialog). */
    public static void styleTable(JTable table, JScrollPane scroll, int rowHeight) {
        table.setRowHeight(rowHeight);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(0xE2E8F0));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0xEEF4F8));
        table.setSelectionForeground(TEXT);
        table.setFont(font(Font.PLAIN, 13));

        // Tùy biến Header của bảng
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setPreferredSize(new Dimension(0, Math.max(36, rowHeight - 2)));
            header.setReorderingAllowed(false);
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean isSel, boolean hasFocus, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFocus, r, c);
                    lbl.setFont(font(Font.BOLD, 13));
                    lbl.setBackground(new Color(0xF1F5F9));
                    lbl.setForeground(TEXT);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0xCBD5E1)),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)
                    ));
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    return lbl;
                }
            });
        }

        // Tùy biến viền ngoài của JScrollPane
        if (scroll != null) {
            scroll.setBorder(new LineBorder(new Color(0xDDE3E8), 1));
            scroll.getViewport().setBackground(Color.WHITE);
        }

        // Renderer mặc định có xen kẽ màu dòng cực nhẹ và đệm lề 8px
        table.setDefaultRenderer(Object.class, createCellRenderer(SwingConstants.LEFT));
    }

    /** Tạo Cell Renderer với căn lề chỉ định, đệm lề và màu dòng xen kẽ nhẹ. */
    public static DefaultTableCellRenderer createCellRenderer(int horizontalAlignment) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFBFDFE));
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                }
                return c;
            }
        };
        r.setHorizontalAlignment(horizontalAlignment);
        return r;
    }

    public static DefaultTableCellRenderer cellCenter() {
        return createCellRenderer(SwingConstants.CENTER);
    }

    public static DefaultTableCellRenderer cellRight() {
        return createCellRenderer(SwingConstants.RIGHT);
    }

    public static DefaultTableCellRenderer cellLeft() {
        return createCellRenderer(SwingConstants.LEFT);
    }
}