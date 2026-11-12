package GUI;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import Bus.TiemPhongBUS;
import Entity.KhachHang;
import Entity.PhieuTiem;
import Entity.SanPham;
import Entity.ThuCung;

/**
 * Man hinh Quan ly Tiem phong Thu cung:
 * Dong bo truc tiep co so du lieu SQL Server (bang TIEMPHONG, KHACHHANG, THUCUNG).
 */
public class TiemPhongPanel extends JPanel implements Refreshable {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TiemPhongBUS bus = new TiemPhongBUS();
    private final List<PhieuTiem> danhSach = new ArrayList<>();
    private final List<PhieuTiem> daLoc = new ArrayList<>();
    private final TiemPhongTableModel model = new TiemPhongTableModel();
    private final JTable table = new JTable(model);

    private final PlaceholderField tfTimKiem = new PlaceholderField("T\u00ecm m\u00e3 phi\u1ebfu, t\u00ean th\u00fa c\u01b0ng, ch\u1ee7 nu\u00f4i ho\u1eb7c S\u0110T...");
    private String trangThaiLoc = "T\u1ea5t c\u1ea3";

    public TiemPhongPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        taiTuCSDL();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    public synchronized void taiTuCSDL() {
        danhSach.clear();
        danhSach.addAll(bus.layTatCa());
        locDuLieu();
    }

    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(0, 14));
        head.setOpaque(false);

        JLabel title = new JLabel("Qu\u1ea3n l\u00fd Ti\u00eam ph\u00f2ng Th\u00fa c\u01b0ng");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);

        JLabel sub = new JLabel("Theo d\u00f5i l\u1ecbch tr\u00ecnh ti\u00eam ch\u1ee7ng, danh m\u1ee5c vaccine v\u00e0 ng\u00e0y h\u1eb9n nh\u1eafc ti\u00eam cho th\u00fa c\u01b0ng.");
        sub.setFont(Theme.font(Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        RoundedButton btnThem = new RoundedButton("\u0110\u0103ng k\u00fd m\u0169i ti\u00eam m\u1edbi", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8).pad(16, 9);
        btnThem.setFont(Theme.font(Font.BOLD, 13));
        btnThem.addActionListener(e -> moDialogThemPhieu());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(btnThem, BorderLayout.EAST);
        head.add(top, BorderLayout.CENTER);

        return head;
    }

    private JComponent buildTableCard() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout());

        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(16, 20, 16, 20));

        tfTimKiem.setFont(Theme.font(Font.PLAIN, 13));
        tfTimKiem.setPreferredSize(new Dimension(340, 36));
        FieldBox searchBox = new FieldBox(tfTimKiem, Color.WHITE, 36);
        tfTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void removeUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void changedUpdate(DocumentEvent e) { locDuLieu(); }
        });
        toolbar.add(searchBox, BorderLayout.WEST);

        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlFilters.setOpaque(false);
        String[] filters = {"T\u1ea5t c\u1ea3", "Ch\u1edd ti\u00eam", "\u0110\u00e3 ti\u00eam", "Qu\u00e1 h\u1ea1n"};
        for (String f : filters) {
            RoundedButton b = new RoundedButton(f, Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8).pad(12, 6);
            b.setFont(Theme.font(Font.PLAIN, 12));
            b.addActionListener(e -> {
                trangThaiLoc = f;
                locDuLieu();
            });
            pnlFilters.add(b);
        }

        RoundedButton btnAction = new RoundedButton("X\u00e1c nh\u1eadn \u0111\u00e3 ti\u00eam", Theme.SUCCESS, Theme.SUCCESS_HOVER, Color.WHITE, null, 8).pad(14, 7);
        btnAction.setFont(Theme.font(Font.BOLD, 12));
        btnAction.addActionListener(e -> capNhatTiemNgay());
        pnlFilters.add(btnAction);

        RoundedButton btnXoa = new RoundedButton("X\u00f3a phi\u1ebfu", Theme.RED, new Color(0xB93A3A), Color.WHITE, null, 8).pad(14, 7);
        btnXoa.setFont(Theme.font(Font.BOLD, 12));
        btnXoa.addActionListener(e -> xoaPhieu());
        pnlFilters.add(btnXoa);

        toolbar.add(pnlFilters, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        table.setRowHeight(46);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xE2E8F0));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0xEFF6FF));
        table.setSelectionForeground(Theme.TEXT);
        table.setFont(Theme.font(Font.PLAIN, 13));

        JTableHeader th = table.getTableHeader();
        th.setFont(Theme.font(Font.BOLD, 12));
        th.setForeground(new Color(0x475569));
        th.setBackground(new Color(0xF8FAFC));
        th.setPreferredSize(new Dimension(0, 42));
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                JLabel l = (JLabel) c;
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                l.setFont(Theme.font(Font.PLAIN, 13));

                if (!isSel) {
                    l.setBackground(row % 2 == 1 ? new Color(0xF8FAFC) : Color.WHITE);
                }

                if (col == 0) {
                    l.setFont(Theme.font(Font.BOLD, 13));
                    l.setForeground(Theme.PRIMARY);
                } else if (col == 7) {
                    String st = String.valueOf(val);
                    JPanel pnlBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
                    pnlBadge.setOpaque(true);
                    pnlBadge.setBackground(isSel ? t.getSelectionBackground() : (row % 2 == 1 ? new Color(0xF8FAFC) : Color.WHITE));

                    JLabel badge = new JLabel(st);
                    badge.setFont(Theme.font(Font.BOLD, 11));
                    badge.setOpaque(true);
                    badge.setBorder(new EmptyBorder(3, 10, 3, 10));

                    if ("Đã tiêm".equalsIgnoreCase(st)) {
                        badge.setBackground(new Color(0xDCFCE7));
                        badge.setForeground(new Color(0x166534));
                    } else if ("Chờ tiêm".equalsIgnoreCase(st)) {
                        badge.setBackground(new Color(0xFEF3C7));
                        badge.setForeground(new Color(0x92400E));
                    } else if ("Quá hạn".equalsIgnoreCase(st)) {
                        badge.setBackground(new Color(0xFEE2E2));
                        badge.setForeground(new Color(0x991B1B));
                    } else {
                        badge.setBackground(new Color(0xF1F5F9));
                        badge.setForeground(new Color(0x475569));
                    }
                    pnlBadge.add(badge);
                    return pnlBadge;
                } else {
                    l.setForeground(Theme.TEXT);
                }
                return l;
            }
        });

        // Thiết lập độ rộng cột cân đối
        int[] colWidths = {90, 130, 100, 140, 105, 200, 100, 125, 130};
        for (int i = 0; i < colWidths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private void locDuLieu() {
        daLoc.clear();
        String kw = tfTimKiem.getText().trim().toLowerCase();
        for (PhieuTiem p : danhSach) {
            if (!trangThaiLoc.equals("T\u1ea5t c\u1ea3") && !p.getTrangThai().equalsIgnoreCase(trangThaiLoc)) continue;
            if (!kw.isEmpty()) {
                String full = (p.getMaPhieu() + " " + p.getTenThuCung() + " " + p.getTenChu() + " "
                        + p.getSdt() + " " + p.getLoaiVaccine() + " " + p.getBacSi()).toLowerCase();
                if (!full.contains(kw)) continue;
            }
            daLoc.add(p);
        }
        model.fireTableDataChanged();
    }

    private void moDialogThemPhieu() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "\u0110\u0103ng k\u00fd phi\u1ebfu ti\u00eam ph\u00f2ng", true);
        dlg.setSize(520, 660);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        List<KhachHang> dsKh = new ArrayList<>();
        List<ThuCung> dsTc = new ArrayList<>();
        try {
            dsKh = bus.layDanhSachKhachHang();
            dsTc = bus.layDanhSachThuCung();
        } catch (Exception ignored) {}

        JComboBox<String> cbKh = new JComboBox<>();
        cbKh.setFont(Theme.font(Font.PLAIN, 13));
        cbKh.setBackground(Color.WHITE);
        cbKh.setEditable(true);
        cbKh.addItem("-- Ch\u1ecdn kh\u00e1ch h\u00e0ng c\u00f3 s\u1eb5n --");
        for (KhachHang kh : dsKh) {
            cbKh.addItem(kh.getHoTen() + " (" + kh.getSoDienThoai() + ")");
        }

        JTextField txtSdt = new JTextField();
        txtSdt.setFont(Theme.font(Font.PLAIN, 13));

        JComboBox<String> cbThuCung = new JComboBox<>();
        cbThuCung.setFont(Theme.font(Font.PLAIN, 13));
        cbThuCung.setBackground(Color.WHITE);
        cbThuCung.setEditable(true);
        cbThuCung.addItem("-- Ch\u1ecdn th\u00fa c\u01b0ng --");

        JTextField txtLoai = new JTextField();
        txtLoai.setFont(Theme.font(Font.PLAIN, 13));

        final List<KhachHang> finalKhList = dsKh;
        final List<ThuCung> finalTcList = dsTc;

        cbKh.addActionListener(e -> {
            int idx = cbKh.getSelectedIndex();
            if (idx > 0 && idx - 1 < finalKhList.size()) {
                KhachHang kh = finalKhList.get(idx - 1);
                txtSdt.setText(kh.getSoDienThoai());
                cbThuCung.removeAllItems();
                boolean coPet = false;
                for (ThuCung tc : finalTcList) {
                    if (tc.getMaKH() != null && tc.getMaKH().equalsIgnoreCase(kh.getMaKH())) {
                        cbThuCung.addItem(tc.getTen() + " (" + tc.getGiong() + ")");
                        coPet = true;
                    }
                }
                if (!coPet) {
                    cbThuCung.addItem("Ch\u01b0a c\u00f3 th\u00fa c\u01b0ng (nh\u1eadp m\u1edbi)");
                }
            }
        });

        cbThuCung.addActionListener(e -> {
            Object sel = cbThuCung.getSelectedItem();
            if (sel != null) {
                String str = sel.toString();
                for (ThuCung tc : finalTcList) {
                    if (str.startsWith(tc.getTen())) {
                        txtLoai.setText(tc.getLoai() + " - " + tc.getGiong());
                        break;
                    }
                }
            }
        });

        JComboBox<String> cbVaccine = new JComboBox<>();
        cbVaccine.setFont(Theme.font(Font.PLAIN, 13));
        cbVaccine.setBackground(Color.WHITE);
        List<SanPham> dsVaccine = bus.layDichVuTiemPhong();
        for (SanPham sp : dsVaccine) {
            cbVaccine.addItem(sp.getTen());
        }
        if (dsVaccine.isEmpty()) {
            cbVaccine.setEditable(true);
        }

        JComboBox<String> cbBacSi = new JComboBox<>(new String[]{
                "BS. Nguy\u1ec5n Ho\u00e0i Anh",
                "BS. V\u00f5 Thanh H\u00f9ng",
                "BS. Ng\u00f4 Gia B\u1ea3o"
        });
        cbBacSi.setFont(Theme.font(Font.PLAIN, 13));
        cbBacSi.setBackground(Color.WHITE);

        JTextField txtGhiChu = new JTextField();
        txtGhiChu.setFont(Theme.font(Font.PLAIN, 13));

        addFormRow(form, "Kh\u00e1ch h\u00e0ng / Ch\u1ee7 nu\u00f4i:", cbKh);
        addFormRow(form, "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i:", txtSdt);
        addFormRow(form, "Th\u00fa c\u01b0ng:", cbThuCung);
        addFormRow(form, "Gi\u1ed1ng lo\u00e0i:", txtLoai);
        addFormRow(form, "Lo\u1ea1i vaccine ti\u00eam:", cbVaccine);
        addFormRow(form, "B\u00e1c s\u0129 ph\u1ee5 tr\u00e1ch:", cbBacSi);
        addFormRow(form, "Ghi ch\u00fa / Ti\u1ec1n s\u1eed d\u1ecb \u1ee9ng:", txtGhiChu);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel act = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        act.setBackground(Color.WHITE);
        act.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));

        RoundedButton btnHuy = new RoundedButton("H\u1ee7y", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8).pad(14, 7);
        RoundedButton btnLuu = new RoundedButton("\u0110\u0103ng k\u00fd ti\u00eam", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8).pad(16, 7);

        btnLuu.addActionListener(e -> {
            Object cObj = cbKh.getSelectedItem();
            String tenC = (cObj != null) ? cObj.toString().trim() : "";
            if (tenC.contains("(") && tenC.endsWith(")")) {
                tenC = tenC.substring(0, tenC.lastIndexOf('(')).trim();
            }
            if (tenC.startsWith("-- Ch\u1ecdn")) tenC = "";

            Object tcObj = cbThuCung.getSelectedItem();
            String tenTc = (tcObj != null) ? tcObj.toString().trim() : "";
            if (tenTc.contains("(") && tenTc.endsWith(")")) {
                tenTc = tenTc.substring(0, tenTc.lastIndexOf('(')).trim();
            }
            if (tenTc.startsWith("-- Ch\u1ecdn") || tenTc.startsWith("Ch\u01b0a c\u00f3")) tenTc = "";

            if (tenTc.isEmpty() || tenC.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui l\u00f2ng ch\u1ecdn ho\u1eb7c nh\u1eadp t\u00ean th\u00fa c\u01b0ng v\u00e0 t\u00ean ch\u1ee7 nu\u00f4i!", "L\u1ed7i nh\u1eadp li\u1ec7u", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String ma = bus.maTiepTheo();
            String homNay = LocalDate.now().format(DATE_FMT);
            String nhacLai = LocalDate.now().plusMonths(12).format(DATE_FMT);
            String bs = (String) cbBacSi.getSelectedItem();

            PhieuTiem pt = new PhieuTiem(ma, tenTc, txtLoai.getText().trim(), tenC, txtSdt.getText().trim(),
                    (String) cbVaccine.getSelectedItem(), homNay, nhacLai, bs,
                    "Ch\u1edd ti\u00eam", txtGhiChu.getText().trim());

            try {
                bus.them(pt);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            danhSach.add(0, pt);
            locDuLieu();
            dlg.dispose();
            JOptionPane.showMessageDialog(this, "\u0110\u00e3 t\u1ea1o phi\u1ebfu ti\u00eam ph\u00f2ng " + ma + " th\u00e0nh c\u00f4ng!", "Th\u00f4ng b\u00e1o", JOptionPane.INFORMATION_MESSAGE);
        });
        btnHuy.addActionListener(e -> dlg.dispose());

        act.add(btnHuy);
        act.add(btnLuu);
        dlg.add(act, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void addFormRow(JPanel form, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.font(Font.PLAIN, 13));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lbl);
        form.add(Box.createVerticalStrut(4));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(field);
        form.add(Box.createVerticalStrut(10));
    }

    private void capNhatTiemNgay() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui l\u00f2ng ch\u1ecdn phi\u1ebfu ti\u00eam c\u1ea7n x\u00e1c nh\u1eadn!", "Th\u00f4ng b\u00e1o", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PhieuTiem pt = daLoc.get(row);
        pt.setTrangThai("\u0110\u00e3 ti\u00eam");
        pt.setNgayTiem(LocalDate.now().format(DATE_FMT));
        pt.setNgayHenNhac(LocalDate.now().plusMonths(12).format(DATE_FMT));
        try {
            bus.capNhat(pt);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        locDuLieu();
        JOptionPane.showMessageDialog(this, "\u0110\u00e3 x\u00e1c nh\u1eadn ti\u00eam th\u00e0nh c\u00f4ng cho b\u00e9 " + pt.getTenThuCung() + "!", "Th\u00e0nh c\u00f4ng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xoaPhieu() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui l\u00f2ng ch\u1ecdn phi\u1ebfu c\u1ea7n x\u00f3a!", "Th\u00f4ng b\u00e1o", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PhieuTiem pt = daLoc.get(row);
        int cf = JOptionPane.showConfirmDialog(this, "B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n x\u00f3a phi\u1ebfu ti\u00eam " + pt.getMaPhieu() + "?", "X\u00e1c nh\u1eadn x\u00f3a", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            danhSach.remove(pt);
            try {
                bus.xoa(pt.getMaPhieu());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            locDuLieu();
        }
    }

    @Override
    public void lamMoi() {
        taiTuCSDL();
    }

    private class TiemPhongTableModel extends AbstractTableModel {
        private final String[] COLS = {
                "M\u00e3 phi\u1ebfu", "T\u00ean th\u00fa c\u01b0ng", "Lo\u00e0i", "Ch\u1ee7 nu\u00f4i", "S\u1ed1 \u0110T", "Lo\u1ea1i Vaccine", "Ng\u00e0y ti\u00eam", "Tr\u1ea1ng th\u00e1i", "B\u00e1c s\u0129"
        };

        @Override public int getRowCount() { return daLoc.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override
        public Object getValueAt(int r, int c) {
            PhieuTiem p = daLoc.get(r);
            return switch (c) {
                case 0 -> p.getMaPhieu();
                case 1 -> p.getTenThuCung();
                case 2 -> p.getLoaiThuCung();
                case 3 -> p.getTenChu();
                case 4 -> p.getSdt();
                case 5 -> p.getLoaiVaccine();
                case 6 -> p.getNgayTiem();
                case 7 -> p.getTrangThai();
                case 8 -> p.getBacSi();
                default -> "";
            };
        }
    }
}
