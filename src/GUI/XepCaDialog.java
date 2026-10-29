package GUI;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Entity.NhanVien;

/** Hộp thoại "Xếp ca làm" (lịch tuần) và "Trạng thái ca trực" (hôm nay) của nhân viên. */
public class XepCaDialog extends JDialog {
    private final List<NhanVien> ds;
    private final JList<NhanVien> lst;
    private final JCheckBox[][] ck = new JCheckBox[7][3];
    private final JLabel lblTong = new JLabel();
    private boolean dangNap;

    private final String[] cot = {"Mã NV", "Nhân viên", "Vai trò", "Ca hôm nay", "Khung giờ", "Trạng thái ca trực"};
    private final DefaultTableModel model = new DefaultTableModel(cot, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tbl = new JTable(model);
    private final Runnable listener = this::napTrangThai;

    public XepCaDialog(Window owner, List<NhanVien> ds, int chonIndex) {
        super(owner, "Xếp ca làm & trạng thái ca trực", Dialog.ModalityType.APPLICATION_MODAL);
        this.ds = ds;
        lst = new JList<>(ds.toArray(new NhanVien[0]));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.font(Font.BOLD, 13));
        tabs.setBackground(Theme.BG);
        tabs.addTab("Xếp ca theo tuần", taoTabXepCa());
        tabs.addTab("Trạng thái ca trực hôm nay", taoTabTrangThai());

        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG);
        add(tabs, BorderLayout.CENTER);

        RoundedButton btnDong = new RoundedButton("Đóng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(22, 8);
        btnDong.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        south.setOpaque(false);
        south.add(btnDong);
        add(south, BorderLayout.SOUTH);

        CaLamData.addListener(listener);
        if (!ds.isEmpty()) lst.setSelectedIndex(Math.max(0, Math.min(chonIndex, ds.size() - 1)));
        napTrangThai();
        setSize(900, 540);
        setLocationRelativeTo(owner);
    }

    // ---------------- Tab 1: xếp ca ----------------
    private JPanel taoTabXepCa() {
        JPanel root = new JPanel(new BorderLayout(14, 0));
        root.setBorder(new EmptyBorder(14, 14, 6, 14));
        root.setOpaque(false);

        lst.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                NhanVien nv = (NhanVien) v;
                JLabel c = (JLabel) super.getListCellRendererComponent(l, "<html><b>" + nv.getHoTen() + "</b><br><span style='color:#6B7A86'>"
                        + nv.getMaNV() + " · " + nv.getTenQuyen() + "</span></html>", i, sel, foc);
                c.setBorder(new EmptyBorder(6, 10, 6, 10));
                if (sel) { c.setBackground(Theme.PRIMARY_LINE); c.setForeground(Theme.TEXT); }
                return c;
            }
        });
        lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lst.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) napLich(); });
        JScrollPane sp = new JScrollPane(lst);
        sp.setPreferredSize(new Dimension(230, 0));
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        root.add(sp, BorderLayout.WEST);

        RoundedPanel card = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH; gc.insets = new Insets(2, 2, 2, 2); gc.gridy = 0; gc.weighty = 1;
        gc.gridx = 0; gc.weightx = 2.2;
        grid.add(new JLabel(), gc);
        for (String t : CaLamData.TEN_THU) {
            JLabel l = new JLabel(t, SwingConstants.CENTER);
            l.setFont(Theme.font(Font.BOLD, 13));
            l.setForeground(t.equals("CN") ? Theme.RED : Theme.TEXT);
            gc.gridx++; gc.weightx = 1;
            grid.add(l, gc);
        }
        for (int c = 0; c < 3; c++) {
            gc.gridy = c + 1; gc.gridx = 0; gc.weightx = 2.2;
            JLabel l = new JLabel("<html><nobr><b>" + CaLamData.TEN_CA[c] + "</b></nobr><br><nobr><span style='color:#6B7A86'>"
                    + CaLamData.GIO_BD[c].format(LichHenData.TIME_FMT) + "–" + CaLamData.GIO_KT[c].format(LichHenData.TIME_FMT) + "</span></nobr></html>");
            l.setFont(Theme.font(Font.PLAIN, 12));
            grid.add(l, gc);
            for (int d = 0; d < 7; d++) {
                final int dd = d, cc = c;
                JCheckBox b = new JCheckBox();
                b.setHorizontalAlignment(SwingConstants.CENTER);
                b.setOpaque(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(e -> {
                    NhanVien nv = lst.getSelectedValue();
                    if (nv == null || dangNap) return;
                    CaLamData.datCa(nv, dd, cc, b.isSelected());
                });
                ck[d][c] = b;
                gc.gridx = d + 1; gc.weightx = 1;
                grid.add(b, gc);
            }
        }

        JLabel tieuDe = new JLabel("Lịch làm việc cố định hằng tuần");
        tieuDe.setFont(Theme.font(Font.BOLD, 15));
        tieuDe.setForeground(Theme.TEXT);
        lblTong.setFont(Theme.font(Font.PLAIN, 12));
        lblTong.setForeground(Theme.MUTED);
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 2));
        head.setOpaque(false);
        head.add(tieuDe);
        head.add(lblTong);
        card.add(head, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);

        JPanel nhanh = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nhanh.setOpaque(false);
        nhanh.add(nutNhanh("Toàn thời gian", new int[] {0, 1}, 6));
        nhanh.add(nutNhanh("Ca sáng", new int[] {0}, 6));
        nhanh.add(nutNhanh("Ca chiều", new int[] {1}, 6));
        nhanh.add(nutNhanh("Ca tối", new int[] {2}, 6));
        RoundedButton xoa = new RoundedButton("Xóa lịch", Color.WHITE, new Color(0xFEE2E2), Theme.RED, Theme.BORDER, 10).pad(12, 7);
        xoa.addActionListener(e -> ap(new int[] {}, 0));
        nhanh.add(xoa);
        card.add(nhanh, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private RoundedButton nutNhanh(String text, int[] cas, int soNgay) {
        RoundedButton b = new RoundedButton(text, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(12, 7);
        b.addActionListener(e -> ap(cas, soNgay));
        return b;
    }

    private void ap(int[] cas, int soNgay) {
        NhanVien nv = lst.getSelectedValue();
        if (nv == null) return;
        boolean[][] mau = new boolean[7][3];
        for (int d = 0; d < soNgay; d++) for (int c : cas) mau[d][c] = true;
        CaLamData.datTatCa(nv, mau);
        napLich();
    }

    private void napLich() {
        NhanVien nv = lst.getSelectedValue();
        dangNap = true;
        for (int d = 0; d < 7; d++) for (int c = 0; c < 3; c++) {
            ck[d][c].setEnabled(nv != null);
            ck[d][c].setSelected(nv != null && CaLamData.layLich(nv)[d][c]);
        }
        dangNap = false;
        lblTong.setText(nv == null ? " " : nv.getHoTen() + " · " + CaLamData.tongCaTuan(nv) + " ca/tuần");
    }

    // ---------------- Tab 2: trạng thái ca trực ----------------
    private JPanel taoTabTrangThai() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(14, 14, 6, 14));
        root.setOpaque(false);

        JScrollPane scroll = new JScrollPane(tbl);
        Theme.styleTable(tbl, scroll);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(70);
        for (int c : new int[] {0, 2, 3, 4}) tbl.getColumnModel().getColumn(c).setCellRenderer(Theme.cellCenter());
        tbl.getColumnModel().getColumn(5).setCellRenderer(rendererTrangThai());
        root.add(scroll, BorderLayout.CENTER);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nut.setOpaque(false);
        JLabel hd = new JLabel("Đánh dấu hôm nay cho nhân viên đã chọn:");
        hd.setFont(Theme.font(Font.PLAIN, 13));
        nut.add(hd);
        nut.add(nutDanhDau("Nghỉ phép", CaLamData.NGHI_PHEP));
        nut.add(nutDanhDau("Vắng mặt", CaLamData.VANG_MAT));
        nut.add(nutDanhDau("Bỏ đánh dấu", null));
        root.add(nut, BorderLayout.SOUTH);
        return root;
    }

    private RoundedButton nutDanhDau(String text, String tt) {
        RoundedButton b = new RoundedButton(text, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(12, 7);
        b.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CaLamData.danhDauHomNay(ds.get(r), tt);
        });
        return b;
    }

    private void napTrangThai() {
        int sel = tbl.getSelectedRow();
        model.setRowCount(0);
        for (NhanVien nv : ds) {
            model.addRow(new Object[] {nv.getMaNV(), nv.getHoTen(), nv.getTenQuyen(),
                    CaLamData.moTaCaHomNay(nv), CaLamData.khungGioHomNay(nv), CaLamData.trangThaiCaTruc(nv)});
        }
        if (sel >= 0 && sel < model.getRowCount()) tbl.setRowSelectionInterval(sel, sel);
        napLich();
    }

    @Override public void dispose() {
        super.dispose();
    }

    // ---------------- Màu trạng thái ----------------
    static Color mauChu(String tt) {
        switch (tt) {
            case CaLamData.DANG_TRUC: return Theme.GREEN;
            case CaLamData.SAP_VAO_CA: return Theme.BLUE_TEXT;
            case CaLamData.NGHI_PHEP: case CaLamData.CHUA_XEP: return Theme.ORANGE;
            case CaLamData.VANG_MAT: case CaLamData.DA_NGHI_VIEC: return Theme.RED;
            default: return Theme.MUTED;   // hết ca, nghỉ hôm nay, tạm nghỉ
        }
    }

    /** Ô "● Đang trực" có chấm màu. */
    static DefaultTableCellRenderer rendererTrangThai() {
        DefaultTableCellRenderer base = Theme.cellCenter();
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) base.getTableCellRendererComponent(t, "● " + v, sel, foc, r, c);
                l.setForeground(mauChu(String.valueOf(v)));
                l.setFont(Theme.font(Font.BOLD, 13));
                return l;
            }
        };
    }
}
