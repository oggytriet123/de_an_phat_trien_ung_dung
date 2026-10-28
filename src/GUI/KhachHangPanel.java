package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.Collator;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Bus.KhachHangBUS;
import Entity.KhachHang;
import Entity.ThongKeKhachHang;
import Entity.ThuCung;

/** Màn hình Quản lý Khách hàng - Đầy đủ Thêm, Sửa, Xóa, Chi tiết và Xuất Excel. */
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
    private String loiTai = null;

    private final StatCard cardTong = new StatCard("Tổng khách hàng");
    private final StatCard cardVip = new StatCard("Thành viên VIP");
    private final StatCard cardDiem = new StatCard("Điểm đang lưu hành");
    private final StatCard cardDoanhThu = new StatCard("Doanh thu từ thành viên");

    private final String[] cols = {"Mã KH", "Khách hàng", "Số điện thoại", "Hạng thẻ", "Điểm tích lũy", "Thú cưng", "Giao dịch gần nhất", "Tổng chi tiêu"};
    private final DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
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

        taiDuLieu();
        refresh();

        addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                taiDuLieu();
                refresh();
            }
        });
    }

    // ---------------- Tiêu đề + nút chức năng (Thêm, Sửa, Xóa, Xem) ----------------
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

        RoundedButton btnExcel = new RoundedButton("Xuất Excel", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnXem = new RoundedButton("Xem chi tiết", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnSua = new RoundedButton("Chỉnh sửa", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnXoa = new RoundedButton("Xóa", Color.WHITE, new Color(0xFEE2E2), Theme.RED, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnThem = new RoundedButton("+ Thêm khách hàng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(16, 9);

        btnExcel.addActionListener(e -> xuatExcel());
        btnThem.addActionListener(e -> themKhachHang());
        btnXem.addActionListener(e -> {
            KhachHang kh = layKhachHangDangChon();
            if (kh != null) xemChiTiet(kh);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnSua.addActionListener(e -> {
            KhachHang kh = layKhachHangDangChon();
            if (kh != null) suaKhachHang(kh);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnXoa.addActionListener(e -> {
            KhachHang kh = layKhachHangDangChon();
            if (kh != null) xoaKhachHang(kh);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnExcel);
        right.add(btnXem);
        right.add(btnSua);
        right.add(btnXoa);
        right.add(btnThem);

        head.add(left, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);
        return head;
    }

    // ---------------- 4 thẻ thống kê ----------------
    private JComponent buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setOpaque(false);
        row.add(cardTong);
        row.add(cardVip);
        row.add(cardDiem);
        row.add(cardDoanhThu);
        return row;
    }

    private void taiDuLieu() {
        try {
            bus.taiLai();
            capNhatThongKe(bus.layThongKe());
            loiTai = null;
        } catch (SQLException e) {
            loiTai = e.getMessage();
            for (StatCard c : new StatCard[] {cardTong, cardVip, cardDiem, cardDoanhThu}) c.set("—", " ", Theme.MUTED);
        }
    }

    private void capNhatThongKe(ThongKeKhachHang t) {
        cardTong.set(Theme.number(t.tongKhach),
                t.khachMoiThang > 0 ? "+" + Theme.number(t.khachMoiThang) + " khách mới trong tháng" : "Chưa có khách mới trong tháng",
                t.khachMoiThang > 0 ? Theme.GREEN : Theme.MUTED);

        cardVip.set(Theme.number(t.soVip),
                t.tongKhach == 0 ? "Chưa có khách hàng" : phanTram(100.0 * t.soVip / t.tongKhach) + " tổng khách hàng",
                Theme.MUTED);

        cardDiem.set(Theme.number(t.diemLuuHanh),
                t.tongKhach == 0 ? "Chưa có khách hàng" : "Trung bình " + Theme.number(Math.round((double) t.diemLuuHanh / t.tongKhach)) + " điểm/khách",
                Theme.MUTED);

        String note;
        Color color = Theme.GREEN;
        if (t.doanhThuThangTruoc > 0) {
            double d = 100.0 * (t.doanhThuThang - t.doanhThuThangTruoc) / t.doanhThuThangTruoc;
            note = (d >= 0 ? "+" : "−") + phanTram(Math.abs(d)) + " so với tháng trước";
            if (d < 0) color = Theme.RED;
        } else if (t.doanhThuThang > 0) {
            note = "Tháng trước chưa có doanh thu";
        } else {
            note = "Chưa có doanh thu trong tháng";
            color = Theme.MUTED;
        }
        cardDoanhThu.set(tienRutGon(t.doanhThuThang), note, color);
    }

    private static String phanTram(double v) {
        return String.format(Locale.ROOT, "%.1f", v).replace('.', ',') + "%";
    }

    private static String tienRutGon(long v) {
        return v >= 1_000_000 ? String.format(Locale.ROOT, "%.1f", v / 1_000_000.0).replace('.', ',') + " triệu₫" : Theme.money(v);
    }

    private static class StatCard extends RoundedPanel {
        private final JLabel value = new JLabel("—");
        private final JLabel note = new JLabel(" ");

        StatCard(String label) {
            super(16, Theme.CARD, null);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(14, 16, 14, 16));
            JLabel l = new JLabel(label);
            l.setFont(Theme.font(Font.PLAIN, 12));
            l.setForeground(Theme.MUTED);
            value.setFont(Theme.font(Font.BOLD, 22));
            value.setForeground(Theme.TEXT);
            note.setFont(Theme.font(Font.PLAIN, 11));
            note.setForeground(Theme.MUTED);
            add(l);
            add(Box.createVerticalStrut(6));
            add(value);
            add(Box.createVerticalStrut(6));
            add(note);
        }

        void set(String v, String n, Color c) {
            value.setText(v);
            note.setText(n);
            note.setForeground(c);
        }
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
        JScrollPane sp = new JScrollPane(table);
        Theme.styleTable(table, sp);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(2).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(3).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(4).setCellRenderer(Theme.cellRight());
        table.getColumnModel().getColumn(6).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(7).setCellRenderer(Theme.cellRight());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    KhachHang kh = layKhachHangDangChon();
                    if (kh != null) xemChiTiet(kh);
                }
            }
        });

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

        tableModel.setRowCount(0);
        for (int i = from; i < to; i++) {
            KhachHang k = filtered.get(i);
            tableModel.addRow(new Object[] {
                k.getMaKH(),
                k.getHoTen(),
                k.getSoDienThoai(),
                k.getHangThe(),
                Theme.number(k.getDiemTichLuy()) + " điểm",
                k.getThuCung(),
                k.getGiaoDichGanNhat() == null ? "—" : k.getGiaoDichGanNhat().format(DATE),
                Theme.money(k.getTongChiTieu())
            });
        }

        lblFooter.setText(filtered.isEmpty()
                ? (loiTai != null ? "Không đọc được CSDL: " + loiTai
                   : bus.layDanhSach().isEmpty() ? "Chưa có khách hàng nào — bấm \"+ Thêm khách hàng\" để bắt đầu"
                   : "Không tìm thấy khách hàng phù hợp")
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
        if ("Chi tiêu cao nhất".equals(sapXep)) {
            return Comparator.comparingLong(KhachHang::getTongChiTieu).reversed();
        } else if ("Giao dịch gần nhất".equals(sapXep)) {
            return Comparator.comparing(KhachHang::getGiaoDichGanNhat,
                    Comparator.nullsLast(Comparator.<LocalDate>naturalOrder().reversed()));
        } else if ("Tên A-Z".equals(sapXep)) {
            Collator col = Collator.getInstance(new Locale("vi"));
            return Comparator.comparing((KhachHang k) -> tenGoi(k.getHoTen()), col)
                    .thenComparing(KhachHang::getHoTen, col);
        } else {
            return Comparator.comparingInt(KhachHang::getDiemTichLuy).reversed();
        }
    }

    private static String tenGoi(String hoTen) {
        String[] w = hoTen.trim().split("\\s+");
        return w[w.length - 1];
    }

    private static String norm(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    private KhachHang layKhachHangDangChon() {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        int from = (trang - 1) * PAGE_SIZE;
        int idx = from + r;
        return (idx < filtered.size()) ? filtered.get(idx) : null;
    }

    // ---------------- Thêm khách hàng ----------------
    private static final class PetRow {
        final JPanel panel = new JPanel(new GridBagLayout());
        final JTextField tfTen = new JTextField();
        final JComboBox<String> cbLoai = new JComboBox<>(new String[] {"Chó", "Mèo", "Thỏ", "Khác"});
        final JTextField tfCan = new JTextField();
    }

    private PetRow taoDongThuCung(JPanel list, List<PetRow> rows, JDialog dlg) {
        PetRow r = new PetRow();
        Font f = Theme.font(Font.PLAIN, 13);
        r.tfTen.setFont(f);
        r.cbLoai.setFont(f);
        r.tfCan.setFont(f);
        for (JTextField t : new JTextField[] {r.tfTen, r.tfCan}) {
            t.setBorder(new CompoundBorder(new LineBorder(new Color(0xD5DCE1)), new EmptyBorder(0, 8, 0, 8)));
        }
        RoundedButton del = new RoundedButton("✕", Color.WHITE, new Color(0xFBE9E9), Theme.RED, Theme.BORDER, 8);
        r.panel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 6, 6);
        c.gridx = 0; c.weightx = 1;   r.panel.add(r.tfTen, c);
        c.gridx = 1; c.weightx = 0;   r.cbLoai.setPreferredSize(new Dimension(78, 32)); r.panel.add(r.cbLoai, c);
        c.gridx = 2;                  r.tfCan.setPreferredSize(new Dimension(64, 32)); r.panel.add(r.tfCan, c);
        c.gridx = 3; c.insets = new Insets(0, 0, 6, 0);
        del.setPreferredSize(new Dimension(32, 32));
        r.panel.add(del, c);
        r.tfTen.setPreferredSize(new Dimension(120, 32));
        del.addActionListener(e -> {
            rows.remove(r);
            list.remove(r.panel);
            list.revalidate();
            list.repaint();
        });
        rows.add(r);
        list.add(r.panel);
        list.revalidate();
        list.repaint();
        return r;
    }

    private void themKhachHang() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Thêm khách hàng", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField tfTen = field();
        JTextField tfSdt = field();
        JSpinner spDiem = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 10));
        spDiem.setFont(Theme.font(Font.PLAIN, 14));
        spDiem.setPreferredSize(new Dimension(300, 36));

        JPanel petList = new JPanel();
        petList.setLayout(new BoxLayout(petList, BoxLayout.Y_AXIS));
        petList.setBackground(Color.WHITE);
        List<PetRow> petRows = new ArrayList<>();
        taoDongThuCung(petList, petRows, dlg);
        JScrollPane petScroll = new JScrollPane(petList);
        petScroll.setBorder(null);
        petScroll.setPreferredSize(new Dimension(300, 120));
        RoundedButton addPet = new RoundedButton("+ Thêm thú cưng", Color.WHITE, new Color(0xF3F6F8), Theme.ACCENT, Theme.BORDER, 8).pad(14, 6);
        addPet.setFont(Theme.font(Font.PLAIN, 12));
        addPet.addActionListener(e -> taoDongThuCung(petList, petRows, dlg));

        JLabel err = new JLabel(" ");
        err.setFont(Theme.font(Font.PLAIN, 12));
        err.setForeground(Theme.RED);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(new JLabel("Họ tên *"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(tfTen, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(new JLabel("Số điện thoại *"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(tfSdt, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(new JLabel("Điểm tích lũy ban đầu:"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(spDiem, c);

        JPanel petHead = new JPanel(new BorderLayout());
        petHead.setBackground(Color.WHITE);
        petHead.add(new JLabel("Thú cưng (tên · loài · cân nặng kg)"), BorderLayout.WEST);
        petHead.add(addPet, BorderLayout.EAST);
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0); form.add(petHead, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 8, 0); form.add(petScroll, c);
        c.gridy = y; c.insets = new Insets(0, 0, 0, 0); form.add(err, c);

        RoundedButton ok = new RoundedButton("Lưu", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(24, 9);
        RoundedButton cancel = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(24, 9);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(new EmptyBorder(4, 24, 18, 24));
        btns.add(cancel); btns.add(ok);

        cancel.addActionListener(e -> dlg.dispose());
        ok.addActionListener(e -> {
            String ten = tfTen.getText().trim().replaceAll("\\s+", " ");
            String sdt = KhachHangBUS.chiSo(tfSdt.getText());
            if (ten.isEmpty()) { err.setText("Vui lòng nhập họ tên."); return; }
            if (sdt.length() < 9 || sdt.length() > 11) { err.setText("Số điện thoại phải gồm 9–11 chữ số."); return; }

            List<ThuCung> pets = new ArrayList<>();
            for (PetRow r : petRows) {
                String tenPet = r.tfTen.getText().trim();
                if (tenPet.isEmpty()) continue;
                double can = 0;
                try { if (!r.tfCan.getText().trim().isEmpty()) can = Double.parseDouble(r.tfCan.getText().trim().replace(',', '.')); }
                catch (Exception ex) {}
                pets.add(new ThuCung(null, tenPet, (String) r.cbLoai.getSelectedItem(), can));
            }
            try {
                if (bus.tonTaiSoDienThoai(sdt)) { err.setText("Số điện thoại này đã tồn tại."); return; }
                bus.them(ten, sdt, ((Number) spDiem.getValue()).intValue(), pets);
                dlg.dispose();
                taiDuLieu();
                refresh();
            } catch (SQLException ex) {
                err.setText("Lỗi: " + ex.getMessage());
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setSize(470, 560);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    // ---------------- CHỈNH SỬA KHÁCH HÀNG ----------------
    private void suaKhachHang(KhachHang kh) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Chỉnh sửa khách hàng: " + kh.getMaKH(), Dialog.ModalityType.APPLICATION_MODAL);

        JTextField tfTen = field();
        tfTen.setText(kh.getHoTen());
        JTextField tfSdt = field();
        tfSdt.setText(KhachHangBUS.chiSo(kh.getSoDienThoai()));
        JSpinner spDiem = new JSpinner(new SpinnerNumberModel(kh.getDiemTichLuy(), 0, 1_000_000, 10));
        spDiem.setFont(Theme.font(Font.PLAIN, 14));
        spDiem.setPreferredSize(new Dimension(300, 36));

        JLabel lblHang = new JLabel("Hạng thẻ: " + kh.getHangThe() + " (Tự động cập nhật theo điểm)");
        lblHang.setFont(Theme.font(Font.PLAIN, 12));
        lblHang.setForeground(Theme.MUTED);
        spDiem.addChangeListener(e -> lblHang.setText("Hạng thẻ: " + KhachHangBUS.tinhHang(((Number) spDiem.getValue()).intValue())));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        form.add(new JLabel("Mã khách hàng:")); form.add(new JLabel("<b>" + kh.getMaKH() + "</b>"));
        form.add(new JLabel("Họ và tên (*):")); form.add(tfTen);
        form.add(new JLabel("Số điện thoại (*):")); form.add(tfSdt);
        form.add(new JLabel("Điểm tích lũy:")); form.add(spDiem);

        JPanel pCenter = new JPanel(new BorderLayout(0, 8));
        pCenter.setBackground(Color.WHITE);
        pCenter.add(form, BorderLayout.NORTH);
        pCenter.add(lblHang, BorderLayout.CENTER);
        lblHang.setBorder(new EmptyBorder(0, 24, 0, 24));

        RoundedButton ok = new RoundedButton("Lưu cập nhật", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(20, 9);
        RoundedButton cancel = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(20, 9);
        cancel.addActionListener(e -> dlg.dispose());

        ok.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            String sdt = tfSdt.getText().trim();
            int diem = ((Number) spDiem.getValue()).intValue();
            if (ten.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Vui lòng nhập họ tên!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            try {
                bus.capNhat(kh.getMaKH(), ten, sdt, diem);
                JOptionPane.showMessageDialog(dlg, "Cập nhật khách hàng thành công!");
                dlg.dispose();
                taiDuLieu();
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setBackground(Color.WHITE);
        btns.add(cancel); btns.add(ok);

        dlg.setLayout(new BorderLayout());
        dlg.add(pCenter, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setSize(440, 280);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    // ---------------- XÓA KHÁCH HÀNG ----------------
    private void xoaKhachHang(KhachHang kh) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng \"" + kh.getHoTen() + "\" (" + kh.getMaKH() + ")?\n"
                        + "Lưu ý: Hành động này sẽ xóa các thú cưng liên kết của khách hàng.",
                "Xác nhận xóa khách hàng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                bus.xoa(kh.getMaKH());
                JOptionPane.showMessageDialog(this, "Đã xóa khách hàng thành công!");
                taiDuLieu();
                refresh();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa: " + ex.getMessage(), "Cảnh báo ràng buộc", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------- XEM CHI TIẾT KHÁCH HÀNG ----------------
    private void xemChiTiet(KhachHang kh) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Hồ sơ: " + kh.getHoTen() + " (" + kh.getMaKH() + ")", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);

        p.add(new JLabel("Mã khách hàng:")); p.add(new JLabel("<b>" + kh.getMaKH() + "</b>"));
        p.add(new JLabel("Họ và tên:")); p.add(new JLabel(kh.getHoTen()));
        p.add(new JLabel("Số điện thoại:")); p.add(new JLabel(kh.getSoDienThoai()));
        p.add(new JLabel("Hạng thẻ / Điểm:")); p.add(new JLabel("Thẻ " + kh.getHangThe() + " (" + Theme.number(kh.getDiemTichLuy()) + " điểm)"));
        p.add(new JLabel("Thú cưng sở hữu:")); p.add(new JLabel(kh.getThuCung()));
        p.add(new JLabel("Tổng chi tiêu:")); p.add(new JLabel("<b>" + Theme.money(kh.getTongChiTieu()) + "</b> (GD gần nhất: " + (kh.getGiaoDichGanNhat() == null ? "Chưa có" : kh.getGiaoDichGanNhat().format(DATE)) + ")"));

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dlg.dispose());
        JPanel pnlDong = new JPanel(); pnlDong.setBackground(Color.WHITE); pnlDong.add(btnDong);
        dlg.add(pnlDong, BorderLayout.SOUTH);
        dlg.setSize(450, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ---------------- Xuất file CSV ----------------
    private void xuatExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Xuất danh sách khách hàng");
        fc.setSelectedFile(new java.io.File("KhachHang.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new java.io.File(file.getPath() + ".csv");
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Mã KH,Họ tên,Số điện thoại,Hạng thẻ,Điểm tích lũy,Thú cưng,Giao dịch gần nhất,Tổng chi tiêu\r\n");
            for (KhachHang k : filtered) {
                w.write(String.join(",",
                        csv(k.getMaKH()), csv(k.getHoTen()),
                        csv("=\"" + KhachHangBUS.chiSo(k.getSoDienThoai()) + "\""),
                        csv(k.getHangThe()), String.valueOf(k.getDiemTichLuy()), csv(k.getThuCung()),
                        k.getGiaoDichGanNhat() == null ? "" : k.getGiaoDichGanNhat().format(DATE),
                        String.valueOf(k.getTongChiTieu())));
                w.write("\r\n");
            }
            JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " khách hàng:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(300, 36));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0xD5DCE1)), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }
}
