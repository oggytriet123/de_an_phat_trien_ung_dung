package GUI;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import Bus.DanhMucBenhBUS;
import Entity.DanhMucBenh;

/** Màn hình Danh mục bệnh: tra cứu, thêm, sửa, xóa bệnh và triệu chứng. */
public class DanhMucBenhPanel extends JPanel {
    private final DanhMucBenhBUS bus = new DanhMucBenhBUS();
    private final DefaultTableModel model = new DefaultTableModel(new String[] {"Mã bệnh", "Tên bệnh", "Triệu chứng"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtTim = new JTextField();
    private final JLabel lblDem = new JLabel();
    private List<DanhMucBenh> hienTai = List.of();

    public DanhMucBenhPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        JLabel t = new JLabel("Danh mục bệnh");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Danh mục bệnh và hướng điều trị");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(t);
        head.add(Box.createVerticalStrut(4));
        head.add(s);

        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        txtTim.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(7, 12, 7, 12)));
        txtTim.setPreferredSize(new Dimension(320, 36));
        txtTim.setToolTipText("Tìm theo mã, tên bệnh hoặc triệu chứng");
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { hienThi(); }
            public void removeUpdate(DocumentEvent e) { hienThi(); }
            public void changedUpdate(DocumentEvent e) { hienThi(); }
        });

        RoundedButton btnThem = new RoundedButton("+ Thêm bệnh", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10);
        RoundedButton btnSua = new RoundedButton("Sửa", Theme.BLUE_BG, Theme.BORDER, Theme.BLUE_TEXT, null, 10);
        RoundedButton btnXoa = new RoundedButton("Xóa", Theme.RED_BG, Theme.RED_BG.darker(), Theme.RED, null, 10);
        btnThem.addActionListener(e -> moDialog(null));
        btnSua.addActionListener(e -> {
            DanhMucBenh b = dangChon();
            if (b != null) moDialog(b);
        });
        btnXoa.addActionListener(e -> xoaDangChon());

        JPanel tools = new JPanel(new BorderLayout(12, 0));
        tools.setOpaque(false);
        tools.add(txtTim, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(btnSua);
        btns.add(btnXoa);
        btns.add(btnThem);
        tools.add(btns, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(tools, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.setRowHeight(48);
        table.setFont(Theme.font(Font.PLAIN, 13));
        table.setForeground(Theme.TEXT);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xE2E8F0));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0xEFF6FF));
        table.setSelectionForeground(Theme.TEXT);

        JTableHeader th = table.getTableHeader();
        th.setFont(Theme.font(Font.BOLD, 12));
        th.setPreferredSize(new Dimension(100, 42));
        th.setForeground(new Color(0x475569));
        th.setBackground(new Color(0xF8FAFC));
        th.setBorder(new MatteBorder(0, 0, 1, 0, new Color(0xCBD5E1)));
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setFont(Theme.font(Font.BOLD, 12));
                l.setForeground(new Color(0x475569));
                l.setBackground(new Color(0xF8FAFC));
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                return l;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(600);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                String raw = v == null ? "" : v.toString();
                String txt = raw.replace("&", "&amp;").replace("<", "&lt;");
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, "<html>" + txt + "</html>", sel, foc, r, c);
                l.setBorder(new EmptyBorder(6, 14, 6, 14));

                if (!sel) {
                    l.setBackground(r % 2 == 1 ? new Color(0xF8FAFC) : Color.WHITE);
                }

                if (c == 0) {
                    l.setFont(Theme.font(Font.BOLD, 13));
                    l.setForeground(Theme.PRIMARY);
                } else if (c == 1) {
                    l.setFont(Theme.font(Font.BOLD, 13));
                    l.setForeground(new Color(0x0F172A));
                } else {
                    l.setFont(Theme.font(Font.PLAIN, 13));
                    l.setForeground(new Color(0x334155));
                }
                return l;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { DanhMucBenh b = dangChon(); if (b != null) moDialog(b); }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(6, 6, 6, 6));
        card.add(sp, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        lblDem.setFont(Theme.font(Font.PLAIN, 12));
        lblDem.setForeground(Theme.MUTED);
        add(lblDem, BorderLayout.SOUTH);

        lamMoi();
    }

    /** Tải lại dữ liệu từ CSDL rồi hiển thị. */
    public void lamMoi() {
        bus.taiLai();
        hienThi();
    }

    private void hienThi() {
        hienTai = bus.timKiem(txtTim.getText());
        model.setRowCount(0);
        for (DanhMucBenh b : hienTai) model.addRow(new Object[] {b.getMaBenh(), b.getTenBenh(), b.getTrieuChung()});
        String che = bus.laNgoaiTuyen() ? "  •  Chưa kết nối SQL Server: không có dữ liệu, thay đổi sẽ không được lưu." : "";
        lblDem.setText(hienTai.size() + " bệnh" + che);
    }

    private DanhMucBenh dangChon() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bệnh trong bảng.", "Chưa chọn", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return hienTai.get(table.convertRowIndexToModel(r));
    }

    private void xoaDangChon() {
        DanhMucBenh b = dangChon();
        if (b == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Xóa bệnh \"" + b.getTenBenh() + "\" khỏi danh mục?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            bus.xoa(b.getMaBenh());
            hienThi();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không xóa được: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moDialog(DanhMucBenh sua) {
        boolean laSua = sua != null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), laSua ? "Sửa bệnh" : "Thêm bệnh",
                Dialog.ModalityType.APPLICATION_MODAL);
        JTextField txtTen = new JTextField(laSua ? sua.getTenBenh() : "");
        JTextArea txtTc = new JTextArea(laSua ? sua.getTrieuChung() : "", 5, 30);
        txtTc.setLineWrap(true);
        txtTc.setWrapStyleWord(true);
        txtTen.setFont(Theme.font(Font.PLAIN, 13));
        txtTc.setFont(Theme.font(Font.PLAIN, 13));
        txtTen.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
        JScrollPane spTc = new JScrollPane(txtTc);
        spTc.setBorder(new LineBorder(Theme.BORDER, 1, true));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 8, 24));
        if (laSua) form.add(nhan("Mã bệnh: " + sua.getMaBenh()));
        form.add(nhan("Tên bệnh *"));
        form.add(txtTen);
        form.add(Box.createVerticalStrut(12));
        form.add(nhan("Triệu chứng"));
        form.add(spTc);

        JLabel lblLoi = new JLabel(" ");
        lblLoi.setForeground(Theme.RED);
        lblLoi.setFont(Theme.font(Font.PLAIN, 12));
        RoundedButton luu = new RoundedButton("Lưu", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10);
        RoundedButton huy = new RoundedButton("Hủy", Theme.BADGE_GRAY_BG, Theme.BORDER, Theme.TEXT, null, 10);
        huy.addActionListener(e -> dlg.dispose());
        luu.addActionListener(e -> {
            try {
                if (laSua) bus.sua(sua.getMaBenh(), txtTen.getText(), txtTc.getText());
                else bus.them(txtTen.getText(), txtTc.getText());
                dlg.dispose();
                hienThi();
            } catch (IllegalArgumentException ex) {
                lblLoi.setText(ex.getMessage());
            } catch (SQLException ex) {
                lblLoi.setText("Lỗi CSDL: " + ex.getMessage());
            }
        });
        JPanel foot = new JPanel(new BorderLayout());
        foot.setBorder(new EmptyBorder(4, 24, 16, 24));
        foot.add(lblLoi, BorderLayout.CENTER);
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        b.add(huy);
        b.add(luu);
        foot.add(b, BorderLayout.EAST);

        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(foot, BorderLayout.SOUTH);
        dlg.setMinimumSize(new Dimension(480, 0));
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private static JLabel nhan(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }
}
