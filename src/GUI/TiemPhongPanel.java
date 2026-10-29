package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Màn hình Quản lý Tiêm phòng thú cưng:
 * - Tiếp nhận đăng ký tiêm vaccine
 * - Theo dõi lịch tiêm và lịch tiêm nhắc lại
 * - Cập nhật trạng thái mũi tiêm (Đã tiêm / Chờ tiêm / Quá hạn)
 */
public class TiemPhongPanel extends JPanel implements Refreshable {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class PhieuTiem {
        private String maPhieu;
        private String tenThuCung;
        private String loaiThuCung;
        private String tenChu;
        private String sdt;
        private String loaiVaccine;
        private String ngayTiem;
        private String ngayHenNhac;
        private String bacSi;
        private String trangThai; // "Đã tiêm", "Chờ tiêm", "Quá hạn"
        private String ghiChu;

        public PhieuTiem(String maPhieu, String tenThuCung, String loaiThuCung, String tenChu, String sdt,
                         String loaiVaccine, String ngayTiem, String ngayHenNhac, String bacSi,
                         String trangThai, String ghiChu) {
            this.maPhieu = maPhieu;
            this.tenThuCung = tenThuCung;
            this.loaiThuCung = loaiThuCung;
            this.tenChu = tenChu;
            this.sdt = sdt;
            this.loaiVaccine = loaiVaccine;
            this.ngayTiem = ngayTiem;
            this.ngayHenNhac = ngayHenNhac;
            this.bacSi = bacSi;
            this.trangThai = trangThai;
            this.ghiChu = ghiChu;
        }

        public String getMaPhieu() { return maPhieu; }
        public String getTenThuCung() { return tenThuCung; }
        public String getLoaiThuCung() { return loaiThuCung; }
        public String getTenChu() { return tenChu; }
        public String getSdt() { return sdt; }
        public String getLoaiVaccine() { return loaiVaccine; }
        public String getNgayTiem() { return ngayTiem; }
        public String getNgayHenNhac() { return ngayHenNhac; }
        public String getBacSi() { return bacSi; }
        public String getTrangThai() { return trangThai; }
        public String getGhiChu() { return ghiChu; }

        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public void setNgayTiem(String ngayTiem) { this.ngayTiem = ngayTiem; }
        public void setNgayHenNhac(String ngayHenNhac) { this.ngayHenNhac = ngayHenNhac; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    }

    private final List<PhieuTiem> danhSach = new ArrayList<>();
    private final List<PhieuTiem> daLoc = new ArrayList<>();
    private final PhieuTiemTableModel model = new PhieuTiemTableModel();
    private final JTable table = new JTable(model);

    private final PlaceholderField tfTimKiem = new PlaceholderField("Tìm mã phiếu, tên thú cưng, chủ nuôi hoặc SĐT...");
    private String trangThaiLoc = "Tất cả";
    private int demMa = 100;

    public TiemPhongPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        khoiTaoDuLieuMau();
        locDuLieu();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private void khoiTaoDuLieuMau() {
        danhSach.add(new PhieuTiem("TP-001", "Lu Lu", "Chó (Poodle)", "Nguyễn Văn Hùng", "0908123456",
                "Tiêm vaccine 7 bệnh chó", "24/10/2026", "24/11/2026", "BS. Minh Đức", "Đã tiêm", "Sức khỏe tốt, không sốt"));
        danhSach.add(new PhieuTiem("TP-002", "Miu Miu", "Mèo (Anh lông ngắn)", "Trần Thị Mai", "0912345678",
                "Tiêm vaccine 4 bệnh mèo", "26/10/2026", "26/11/2026", "BS. Hoàng Long", "Đã tiêm", "Hẹn tái chủng sau 1 tháng"));
        danhSach.add(new PhieuTiem("TP-003", "Bông Bông", "Chó (Corgi)", "Lê Hoàng Nam", "0987654321",
                "Tiêm vaccine phòng Dại (Rabies)", "27/10/2026", "27/10/2027", "BS. Minh Đức", "Chờ tiêm", "Chờ khám sàng lọc"));
        danhSach.add(new PhieuTiem("TP-004", "Simba", "Mèo (Ba Tư)", "Phạm Quỳnh Chi", "0933445566",
                "Tẩy giun sán định kỳ thú cưng", "27/10/2026", "27/01/2027", "BS. Thu Thảo", "Chờ tiêm", "Tiêm kèm ngừa nội ngoại ký sinh"));
        danhSach.add(new PhieuTiem("TP-005", "Ki Ki", "Chó (Golden)", "Võ Thanh Tùng", "0977889900",
                "Tiêm vaccine 5 bệnh chó", "15/09/2026", "15/10/2026", "BS. Hoàng Long", "Quá hạn", "Chưa đến tiêm nhắc lại mũi 2"));
    }

    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(0, 14));
        head.setOpaque(false);

        // Tiêu đề
        JLabel title = new JLabel("Quản lý Tiêm phòng Vaccine");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);

        JLabel sub = new JLabel("Lập phiếu tiêm phòng, quản lý lịch tiêm chủng và theo dõi lịch tiêm nhắc lại của thú cưng.");
        sub.setFont(Theme.font(Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        // Nút thêm mới
        RoundedButton btnThem = new RoundedButton("+ Thêm phiếu tiêm", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btnThem.setPreferredSize(new Dimension(180, 42));
        btnThem.addActionListener(e -> moDialogThemPhieu());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(btnThem, BorderLayout.EAST);
        head.add(top, BorderLayout.NORTH);

        // Thanh công cụ: tìm kiếm + chip lọc trạng thái
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setOpaque(false);

        FieldBox searchBox = new FieldBox(tfTimKiem, Color.WHITE, 40);
        searchBox.setPreferredSize(new Dimension(380, 40));
        tfTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void removeUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void changedUpdate(DocumentEvent e) { locDuLieu(); }
        });
        bar.add(searchBox, BorderLayout.WEST);

        // Bộ lọc trạng thái
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);
        String[] tabs = {"Tất cả", "Chờ tiêm", "Đã tiêm", "Quá hạn"};
        ButtonGroup bg = new ButtonGroup();
        for (String t : tabs) {
            JToggleButton btn = new JToggleButton(t);
            btn.setFont(Theme.font(Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBackground(Color.WHITE);
            btn.setForeground(Theme.MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(7, 14, 7, 14)));
            if (t.equals("Tất cả")) btn.setSelected(true);
            btn.addActionListener(e -> {
                trangThaiLoc = t;
                locDuLieu();
            });
            bg.add(btn);
            filters.add(btn);
        }
        bar.add(filters, BorderLayout.EAST);
        head.add(bar, BorderLayout.SOUTH);

        return head;
    }

    private JComponent buildTableCard() {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout());

        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(Theme.font(Font.PLAIN, 13));

        JTableHeader th = table.getTableHeader();
        th.setPreferredSize(new Dimension(0, 42));
        th.setFont(Theme.font(Font.BOLD, 13));
        th.setBackground(Theme.FIELD_BG);
        th.setForeground(Theme.TEXT);
        th.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (isSelected) {
                    c.setBackground(new Color(0xDCE8FA));
                    c.setForeground(Theme.PRIMARY);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFBFD));
                    c.setForeground(Theme.TEXT);
                }
                if (col == 7) { // Trạng thái
                    String st = String.valueOf(value);
                    if ("Đã tiêm".equals(st)) {
                        setForeground(Theme.SUCCESS);
                        setFont(Theme.font(Font.BOLD, 12));
                    } else if ("Chờ tiêm".equals(st)) {
                        setForeground(Theme.PRIMARY);
                        setFont(Theme.font(Font.BOLD, 12));
                    } else if ("Quá hạn".equals(st)) {
                        setForeground(Theme.DANGER);
                        setFont(Theme.font(Font.BOLD, 12));
                    }
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        SlimScrollBarUI.apply(sp);
        card.add(sp, BorderLayout.CENTER);

        // Thanh thao tác dưới bảng
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        foot.setOpaque(false);
        foot.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));

        RoundedButton btnTiemNgay = new RoundedButton("Xác nhận đã tiêm", Theme.SUCCESS, new Color(0x27AE60), Color.WHITE, null, 8);
        btnTiemNgay.addActionListener(e -> capNhatTiemNgay());

        RoundedButton btnXoa = new RoundedButton("Xóa phiếu", Color.WHITE, Theme.FIELD_BG, Theme.DANGER, Theme.BORDER, 8);
        btnXoa.addActionListener(e -> xoaPhieu());

        foot.add(btnTiemNgay);
        foot.add(btnXoa);
        card.add(foot, BorderLayout.SOUTH);

        return card;
    }

    private void locDuLieu() {
        daLoc.clear();
        String kw = tfTimKiem.getText().trim().toLowerCase();
        for (PhieuTiem p : danhSach) {
            if (!"Tất cả".equals(trangThaiLoc) && !p.getTrangThai().equals(trangThaiLoc)) continue;
            if (!kw.isEmpty()) {
                String full = (p.getMaPhieu() + " " + p.getTenThuCung() + " " + p.getTenChu() + " "
                        + p.getSdt() + " " + p.getLoaiVaccine()).toLowerCase();
                if (!full.contains(kw)) continue;
            }
            daLoc.add(p);
        }
        model.fireTableDataChanged();
    }

    private void moDialogThemPhieu() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lập phiếu tiêm phòng mới", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);

        PlaceholderField txtTenTc = new PlaceholderField("Tên thú cưng (ví dụ: Lu Lu)");
        PlaceholderField txtLoai = new PlaceholderField("Loài (Chó / Mèo...)");
        PlaceholderField txtTenChu = new PlaceholderField("Họ tên chủ nuôi");
        PlaceholderField txtSdt = new PlaceholderField("Số điện thoại liên hệ");
        JComboBox<String> cbVaccine = new JComboBox<>(new String[] {
                "Tiêm vaccine phòng Dại (Rabies)",
                "Tiêm vaccine 5 bệnh chó",
                "Tiêm vaccine 7 bệnh chó",
                "Tiêm vaccine 4 bệnh mèo",
                "Tiêm phòng ngừa xoắn khuẩn Lepto",
                "Tẩy giun sán định kỳ thú cưng"
        });
        cbVaccine.setFont(Theme.font(Font.PLAIN, 13));
        cbVaccine.setBackground(Color.WHITE);

        PlaceholderField txtBacSi = new PlaceholderField("Bác sĩ tiêm (ví dụ: BS. Minh Đức)");
        PlaceholderField txtGhiChu = new PlaceholderField("Ghi chú theo dõi sức khỏe");

        p.add(new JLabel("Tên thú cưng:"));
        p.add(new FieldBox(txtTenTc, Color.WHITE, 38));
        p.add(new JLabel("Loài thú cưng:"));
        p.add(new FieldBox(txtLoai, Color.WHITE, 38));
        p.add(new JLabel("Họ tên chủ nuôi:"));
        p.add(new FieldBox(txtTenChu, Color.WHITE, 38));
        p.add(new JLabel("Số điện thoại:"));
        p.add(new FieldBox(txtSdt, Color.WHITE, 38));
        p.add(new JLabel("Loại Vaccine:"));
        p.add(cbVaccine);
        p.add(new JLabel("Bác sĩ phụ trách:"));
        p.add(new FieldBox(txtBacSi, Color.WHITE, 38));
        p.add(new JLabel("Ghi chú:"));
        p.add(new FieldBox(txtGhiChu, Color.WHITE, 38));

        dlg.add(p, BorderLayout.CENTER);

        JPanel act = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        act.setBackground(Theme.FIELD_BG);
        RoundedButton btnLuu = new RoundedButton("Lưu phiếu", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);

        btnLuu.addActionListener(e -> {
            String tenTc = txtTenTc.getText().trim();
            String tenC = txtTenChu.getText().trim();
            if (tenTc.isEmpty() || tenC.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên thú cưng và tên chủ nuôi!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = "TP-" + (++demMa);
            String homNay = LocalDate.now().format(DATE_FMT);
            String nhacLai = LocalDate.now().plusMonths(1).format(DATE_FMT);
            danhSach.add(0, new PhieuTiem(ma, tenTc, txtLoai.getText().trim(), tenC, txtSdt.getText().trim(),
                    (String) cbVaccine.getSelectedItem(), homNay, nhacLai,
                    txtBacSi.getText().trim().isEmpty() ? "BS. Trực ca" : txtBacSi.getText().trim(),
                    "Chờ tiêm", txtGhiChu.getText().trim()));
            locDuLieu();
            dlg.dispose();
            JOptionPane.showMessageDialog(this, "Đã tạo phiếu tiêm phòng " + ma + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        btnHuy.addActionListener(e -> dlg.dispose());

        act.add(btnHuy);
        act.add(btnLuu);
        dlg.add(act, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void capNhatTiemNgay() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu tiêm trong bảng để cập nhật!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PhieuTiem pt = daLoc.get(row);
        pt.setTrangThai("Đã tiêm");
        pt.setNgayTiem(LocalDate.now().format(DATE_FMT));
        pt.setNgayHenNhac(LocalDate.now().plusMonths(1).format(DATE_FMT));
        locDuLieu();
        JOptionPane.showMessageDialog(this, "Đã xác nhận tiêm chủng cho bé " + pt.getTenThuCung() + " thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xoaPhieu() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu tiêm cần xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PhieuTiem pt = daLoc.get(row);
        int cf = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phiếu tiêm " + pt.getMaPhieu() + " của bé " + pt.getTenThuCung() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            danhSach.remove(pt);
            locDuLieu();
        }
    }

    @Override
    public void lamMoi() {
        locDuLieu();
    }

    private class PhieuTiemTableModel extends AbstractTableModel {
        private final String[] COLS = {
                "Mã phiếu", "Tên thú cưng", "Loài", "Chủ nuôi", "Số ĐT", "Loại Vaccine", "Ngày tiêm", "Trạng thái", "Bác sĩ"
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
