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
 * Màn hình Quản lý Phẫu thuật thú cưng:
 * - Đăng ký tiếp nhận các ca phẫu thuật
 * - Theo dõi tiến độ mổ (Chờ mổ / Đang mổ / Hậu phẫu / Hoàn thành)
 * - Quản lý bác sĩ mổ chính, chi phí và ghi chú dặn dò
 */
public class PhauThuatPanel extends JPanel implements Refreshable {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class CaPhauThuat {
        private String maCa;
        private String tenThuCung;
        private String loaiThuCung;
        private String tenChu;
        private String sdt;
        private String loaiPhauThuat;
        private String ngayMo;
        private String gioMo;
        private String bacSiChinh;
        private long chiPhi;
        private String trangThai; // "Chờ phẫu thuật", "Đang phẫu thuật", "Theo dõi hậu phẫu", "Hoàn thành"
        private String ghiChu;

        public CaPhauThuat(String maCa, String tenThuCung, String loaiThuCung, String tenChu, String sdt,
                           String loaiPhauThuat, String ngayMo, String gioMo, String bacSiChinh,
                           long chiPhi, String trangThai, String ghiChu) {
            this.maCa = maCa;
            this.tenThuCung = tenThuCung;
            this.loaiThuCung = loaiThuCung;
            this.tenChu = tenChu;
            this.sdt = sdt;
            this.loaiPhauThuat = loaiPhauThuat;
            this.ngayMo = ngayMo;
            this.gioMo = gioMo;
            this.bacSiChinh = bacSiChinh;
            this.chiPhi = chiPhi;
            this.trangThai = trangThai;
            this.ghiChu = ghiChu;
        }

        public String getMaCa() { return maCa; }
        public String getTenThuCung() { return tenThuCung; }
        public String getLoaiThuCung() { return loaiThuCung; }
        public String getTenChu() { return tenChu; }
        public String getSdt() { return sdt; }
        public String getLoaiPhauThuat() { return loaiPhauThuat; }
        public String getNgayMo() { return ngayMo; }
        public String getGioMo() { return gioMo; }
        public String getBacSiChinh() { return bacSiChinh; }
        public long getChiPhi() { return chiPhi; }
        public String getTrangThai() { return trangThai; }
        public String getGhiChu() { return ghiChu; }

        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    }

    private final List<CaPhauThuat> danhSach = new ArrayList<>();
    private final List<CaPhauThuat> daLoc = new ArrayList<>();
    private final PhauThuatTableModel model = new PhauThuatTableModel();
    private final JTable table = new JTable(model);

    private final PlaceholderField tfTimKiem = new PlaceholderField("Tìm mã ca mổ, tên thú cưng, chủ nuôi, bác sĩ...");
    private String trangThaiLoc = "Tất cả";
    private int demMa = 100;

    public PhauThuatPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        khoiTaoDuLieuMau();
        locDuLieu();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private void khoiTaoDuLieuMau() {
        danhSach.add(new CaPhauThuat("PT-001", "Rocky", "Chó (Husky)", "Trần Đình Trọng", "0918112233",
                "Phẫu thuật lấy sỏi bàng quang", "27/10/2026", "08:30", "BS. Lê Tuấn Anh", 1500000,
                "Đang phẫu thuật", "Gây mê hồi sức ổn định"));
        danhSach.add(new CaPhauThuat("PT-002", "Miu Con", "Mèo (Anh lông dài)", "Lê Thảo My", "0977223344",
                "Triệt sản mèo cái nội soi", "27/10/2026", "10:00", "BS. Hoàng Long", 650000,
                "Chờ phẫu thuật", "Đã nhịn ăn uống trước 8 tiếng"));
        danhSach.add(new CaPhauThuat("PT-003", "Bé Bự", "Chó (Pug)", "Phạm Gia Bảo", "0934556677",
                "Phẫu thuật kết hợp xương gãy", "26/10/2026", "14:15", "BS. Lê Tuấn Anh", 2500000,
                "Theo dõi hậu phẫu", "Nẹp cố định xương cẳng chân, theo dõi 2 ngày"));
        danhSach.add(new CaPhauThuat("PT-004", "Tom", "Mèo (Mướp)", "Nguyễn Văn An", "0909001122",
                "Triệt sản mèo đực", "26/10/2026", "09:00", "BS. Thu Thảo", 350000,
                "Hoàn thành", "Vết mổ khô ráo, đã giao cho chủ chăm sóc"));
        danhSach.add(new CaPhauThuat("PT-005", "Lucky", "Chó (Corgi)", "Đặng Tuyết Mai", "0988776655",
                "Mổ đẻ / Lấy thai cấp cứu", "25/10/2026", "16:00", "BS. Hoàng Long", 1800000,
                "Hoàn thành", "Cứu sống mẹ và 4 con khỏe mạnh"));
    }

    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(0, 14));
        head.setOpaque(false);

        // Tiêu đề
        JLabel title = new JLabel("Quản lý Phẫu thuật Ngoại khoa");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);

        JLabel sub = new JLabel("Tiếp nhận ca mổ, theo dõi tiến độ phòng phẫu thuật và chăm sóc hậu phẫu cho thú cưng.");
        sub.setFont(Theme.font(Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        // Nút thêm mới
        RoundedButton btnThem = new RoundedButton("+ Đăng ký ca mổ", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btnThem.setPreferredSize(new Dimension(180, 42));
        btnThem.addActionListener(e -> moDialogThemCa());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(btnThem, BorderLayout.EAST);
        head.add(top, BorderLayout.NORTH);

        // Thanh công cụ: tìm kiếm + chip lọc
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
        String[] tabs = {"Tất cả", "Chờ phẫu thuật", "Đang phẫu thuật", "Theo dõi hậu phẫu", "Hoàn thành"};
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
                    if ("Hoàn thành".equals(st)) {
                        setForeground(Theme.SUCCESS);
                        setFont(Theme.font(Font.BOLD, 12));
                    } else if ("Đang phẫu thuật".equals(st)) {
                        setForeground(new Color(0xE67E22));
                        setFont(Theme.font(Font.BOLD, 12));
                    } else if ("Theo dõi hậu phẫu".equals(st)) {
                        setForeground(Theme.PRIMARY);
                        setFont(Theme.font(Font.BOLD, 12));
                    } else if ("Chờ phẫu thuật".equals(st)) {
                        setForeground(Theme.MUTED);
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

        // Nút thao tác
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        foot.setOpaque(false);
        foot.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));

        RoundedButton btnCapNhat = new RoundedButton("Chuyển trạng thái tiến độ", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btnCapNhat.addActionListener(e -> capNhatTienDo());

        RoundedButton btnXoa = new RoundedButton("Xóa ca", Color.WHITE, Theme.FIELD_BG, Theme.DANGER, Theme.BORDER, 8);
        btnXoa.addActionListener(e -> xoaCa());

        foot.add(btnCapNhat);
        foot.add(btnXoa);
        card.add(foot, BorderLayout.SOUTH);

        return card;
    }

    private void locDuLieu() {
        daLoc.clear();
        String kw = tfTimKiem.getText().trim().toLowerCase();
        for (CaPhauThuat c : danhSach) {
            if (!"Tất cả".equals(trangThaiLoc) && !c.getTrangThai().equals(trangThaiLoc)) continue;
            if (!kw.isEmpty()) {
                String full = (c.getMaCa() + " " + c.getTenThuCung() + " " + c.getTenChu() + " "
                        + c.getSdt() + " " + c.getLoaiPhauThuat() + " " + c.getBacSiChinh()).toLowerCase();
                if (!full.contains(kw)) continue;
            }
            daLoc.add(c);
        }
        model.fireTableDataChanged();
    }

    private void moDialogThemCa() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Đăng ký ca phẫu thuật mới", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(520, 600);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);

        PlaceholderField txtTenTc = new PlaceholderField("Tên thú cưng (ví dụ: Bé Miu)");
        PlaceholderField txtLoai = new PlaceholderField("Loài (Chó / Mèo...)");
        PlaceholderField txtTenChu = new PlaceholderField("Họ tên chủ nuôi");
        PlaceholderField txtSdt = new PlaceholderField("Số điện thoại liên hệ");
        JComboBox<String> cbPhauThuat = new JComboBox<>(new String[] {
                "Triệt sản mèo đực",
                "Triệt sản mèo cái nội soi",
                "Triệt sản chó đực (< 10kg)",
                "Triệt sản chó cái (< 10kg)",
                "Phẫu thuật lấy sỏi bàng quang",
                "Mổ đẻ / Lấy thai cấp cứu",
                "Phẫu thuật may phục hồi vết thương",
                "Phẫu thuật kết hợp xương gãy"
        });
        cbPhauThuat.setFont(Theme.font(Font.PLAIN, 13));
        cbPhauThuat.setBackground(Color.WHITE);

        PlaceholderField txtBacSi = new PlaceholderField("Bác sĩ mổ chính (ví dụ: BS. Lê Tuấn Anh)");
        PlaceholderField txtChiPhi = new PlaceholderField("Chi phí phẫu thuật (VNĐ)");
        PlaceholderField txtGhiChu = new PlaceholderField("Dặn dò trước mổ / Tiền sử gây mê");

        p.add(new JLabel("Tên thú cưng:"));
        p.add(new FieldBox(txtTenTc, Color.WHITE, 38));
        p.add(new JLabel("Loài thú cưng:"));
        p.add(new FieldBox(txtLoai, Color.WHITE, 38));
        p.add(new JLabel("Họ tên chủ nuôi:"));
        p.add(new FieldBox(txtTenChu, Color.WHITE, 38));
        p.add(new JLabel("Số điện thoại:"));
        p.add(new FieldBox(txtSdt, Color.WHITE, 38));
        p.add(new JLabel("Loại phẫu thuật:"));
        p.add(cbPhauThuat);
        p.add(new JLabel("Bác sĩ mổ chính:"));
        p.add(new FieldBox(txtBacSi, Color.WHITE, 38));
        p.add(new JLabel("Chi phí dự kiến (VNĐ):"));
        p.add(new FieldBox(txtChiPhi, Color.WHITE, 38));
        p.add(new JLabel("Ghi chú:"));
        p.add(new FieldBox(txtGhiChu, Color.WHITE, 38));

        dlg.add(p, BorderLayout.CENTER);

        JPanel act = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        act.setBackground(Theme.FIELD_BG);
        RoundedButton btnLuu = new RoundedButton("Lưu ca mổ", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);

        btnLuu.addActionListener(e -> {
            String tenTc = txtTenTc.getText().trim();
            String tenC = txtTenChu.getText().trim();
            if (tenTc.isEmpty() || tenC.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên thú cưng và tên chủ nuôi!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long cp = 500000;
            try {
                if (!txtChiPhi.getText().trim().isEmpty()) {
                    cp = Long.parseLong(txtChiPhi.getText().trim().replaceAll("[^0-9]", ""));
                }
            } catch (Exception ignored) { }

            String ma = "PT-" + (++demMa);
            String homNay = LocalDate.now().format(DATE_FMT);
            danhSach.add(0, new CaPhauThuat(ma, tenTc, txtLoai.getText().trim(), tenC, txtSdt.getText().trim(),
                    (String) cbPhauThuat.getSelectedItem(), homNay, "09:00",
                    txtBacSi.getText().trim().isEmpty() ? "BS. Trưởng khoa Ngoại" : txtBacSi.getText().trim(),
                    cp, "Chờ phẫu thuật", txtGhiChu.getText().trim()));
            locDuLieu();
            dlg.dispose();
            JOptionPane.showMessageDialog(this, "Đã tiếp nhận ca phẫu thuật " + ma + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        btnHuy.addActionListener(e -> dlg.dispose());

        act.add(btnHuy);
        act.add(btnLuu);
        dlg.add(act, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void capNhatTienDo() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ca phẫu thuật trong bảng để cập nhật tiến độ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        CaPhauThuat ca = daLoc.get(row);
        String[] options = {"Chờ phẫu thuật", "Đang phẫu thuật", "Theo dõi hậu phẫu", "Hoàn thành"};
        String opt = (String) JOptionPane.showInputDialog(this,
                "Chọn trạng thái mới cho ca mổ " + ca.getMaCa() + " (" + ca.getTenThuCung() + "):",
                "Cập nhật tiến độ ca mổ", JOptionPane.QUESTION_MESSAGE, null, options, ca.getTrangThai());
        if (opt != null) {
            ca.setTrangThai(opt);
            locDuLieu();
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái ca mổ thành: " + opt, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void xoaCa() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ca phẫu thuật cần xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        CaPhauThuat ca = daLoc.get(row);
        int cf = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa ca phẫu thuật " + ca.getMaCa() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            danhSach.remove(ca);
            locDuLieu();
        }
    }

    @Override
    public void lamMoi() {
        locDuLieu();
    }

    private class PhauThuatTableModel extends AbstractTableModel {
        private final String[] COLS = {
                "Mã ca", "Tên thú cưng", "Loài", "Chủ nuôi", "Số ĐT", "Phẫu thuật", "Ngày mổ", "Trạng thái", "Bác sĩ mổ", "Chi phí"
        };

        @Override public int getRowCount() { return daLoc.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override
        public Object getValueAt(int r, int c) {
            CaPhauThuat p = daLoc.get(r);
            return switch (c) {
                case 0 -> p.getMaCa();
                case 1 -> p.getTenThuCung();
                case 2 -> p.getLoaiThuCung();
                case 3 -> p.getTenChu();
                case 4 -> p.getSdt();
                case 5 -> p.getLoaiPhauThuat();
                case 6 -> p.getNgayMo();
                case 7 -> p.getTrangThai();
                case 8 -> p.getBacSiChinh();
                case 9 -> Theme.money(p.getChiPhi());
                default -> "";
            };
        }
    }
}
