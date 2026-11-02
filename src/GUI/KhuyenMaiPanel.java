package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import Bus.MaGiamGiaBUS;
import Entity.MaGiamGia;

/**
 * Màn hình Quản lý Khuyến Mãi - Danh Mục > Khuyến Mãi.
 * Cho phép tạo mã khuyến mãi, chỉnh sửa, xóa, tìm kiếm, lọc, xem chi tiết và xuất Excel.
 */
public class KhuyenMaiPanel extends JPanel {
    private static final int PAGE_SIZE = 8;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] FILTERS = {"Tất cả", "Còn hạn", "Hết hạn", "Giảm theo %", "Giảm tiền mặt"};
    private static final String[] SORTS = {"Mới nhất", "Hạn dùng gần nhất", "Mức giảm cao nhất", "Mã A-Z"};

    private final MaGiamGiaBUS bus;
    private List<MaGiamGia> filtered = new ArrayList<>();
    private String boLocHienTai = "Tất cả";
    private String sapXepHienTai = SORTS[0];
    private int trang = 1;

    // 4 Thẻ thống kê
    private final StatCard cardTong = new StatCard("Tổng mã khuyến mãi");
    private final StatCard cardConHan = new StatCard("Đang áp dụng (Còn hạn)");
    private final StatCard cardHetHan = new StatCard("Đã hết hạn");
    private final StatCard cardGiamCaoNhat = new StatCard("Mức giảm tốt nhất");

    // Bảng dữ liệu
    private final String[] cols = {"STT", "Mã khuyến mãi", "Loại giảm giá", "Mức giảm", "Hạn sử dụng", "Trạng thái", "Ghi chú & Điều kiện"};
    private final DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm theo mã khuyến mãi, mức giảm, ghi chú...");
    private final List<RoundedButton> chips = new ArrayList<>();
    private final RoundedButton btnSort = new RoundedButton("", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10);
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public KhuyenMaiPanel(MaGiamGiaBUS bus) {
        super(new BorderLayout(0, 18));
        this.bus = bus;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildStats(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        refresh();

        addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refresh();
            }
        });
    }

    // ---------------- Header: Tiêu đề + Nút thao tác ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel t = new JLabel("Khuyến Mãi");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Chương trình khuyến mãi, ưu đãi và quản lý mã giảm giá");
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
        RoundedButton btnXoa = new RoundedButton("Xóa mã", Color.WHITE, new Color(0xFEE2E2), Theme.RED, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnThem = new RoundedButton("+ Tạo mã khuyến mãi", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(16, 9);

        btnExcel.addActionListener(e -> xuatExcel());
        btnXem.addActionListener(e -> {
            MaGiamGia m = layMaDangChon();
            if (m != null) xemChiTiet(m);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn một mã khuyến mãi trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnSua.addActionListener(e -> {
            MaGiamGia m = layMaDangChon();
            if (m != null) suaMaKhuyenMai(m);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn mã khuyến mãi cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnXoa.addActionListener(e -> {
            MaGiamGia m = layMaDangChon();
            if (m != null) xoaMaKhuyenMai(m);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn mã khuyến mãi cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnThem.addActionListener(e -> taoMaKhuyenMai());

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

    // ---------------- 4 Thẻ thống kê ----------------
    private JComponent buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 18, 0));
        row.setOpaque(false);
        row.add(cardTong);
        row.add(cardConHan);
        row.add(cardHetHan);
        row.add(cardGiamCaoNhat);
        return row;
    }

    private void capNhatThongKe() {
        List<MaGiamGia> ds = bus.layDanhSach();
        int tong = ds.size();
        long conHan = ds.stream().filter(m -> !bus.daHetHan(m)).count();
        long hetHan = tong - conHan;

        cardTong.set(tong + " mã", "Tổng chương trình khuyến mãi", Theme.MUTED);
        cardConHan.set(conHan + " mã", tong == 0 ? "Chưa có mã" : (Math.round(100.0 * conHan / tong) + "% đang áp dụng"), Theme.GREEN);
        cardHetHan.set(hetHan + " mã", hetHan > 0 ? "Không thể áp dụng khi lập HĐ" : "Tất cả đều còn hạn", hetHan > 0 ? Theme.RED : Theme.MUTED);

        // Tìm ưu đãi tốt nhất
        String topGiam = "—";
        String topNote = "Chưa có ưu đãi";
        MaGiamGia topTien = null;
        MaGiamGia topPt = null;
        for (MaGiamGia m : ds) {
            if (bus.daHetHan(m)) continue;
            if (m.isPhanTram()) {
                if (topPt == null || m.getGiaTri() > topPt.getGiaTri()) topPt = m;
            } else {
                if (topTien == null || m.getGiaTri() > topTien.getGiaTri()) topTien = m;
            }
        }
        if (topTien != null && topPt != null) {
            topGiam = topPt.getGiaTri() + "% / " + Theme.money(topTien.getGiaTri());
            topNote = "Ưu đãi lớn nhất đang chạy";
        } else if (topPt != null) {
            topGiam = topPt.getGiaTri() + "%";
            topNote = "Mã " + topPt.getMa();
        } else if (topTien != null) {
            topGiam = Theme.money(topTien.getGiaTri());
            topNote = "Mã " + topTien.getMa();
        }
        cardGiamCaoNhat.set(topGiam, topNote, Theme.ORANGE);
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

    // ---------------- Thẻ bảng dữ liệu ----------------
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
        search.setPreferredSize(new Dimension(320, 36));
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
        for (String f : FILTERS) {
            RoundedButton chip = new RoundedButton(f, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(15, 8);
            chip.setFont(Theme.font(Font.PLAIN, 13));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 36));
            chip.addActionListener(e -> { boLocHienTai = f; trang = 1; refresh(); });
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
                it.addActionListener(ev -> { sapXepHienTai = s; trang = 1; refresh(); });
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

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setCellRenderer(Theme.cellCenter());

        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setCellRenderer(new PromoCodeCellRenderer());

        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setCellRenderer(Theme.cellCenter());

        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setCellRenderer(new DiscountValueCellRenderer());

        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setCellRenderer(Theme.cellCenter());

        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        table.getColumnModel().getColumn(6).setPreferredWidth(300);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MaGiamGia m = layMaDangChon();
                    if (m != null) xemChiTiet(m);
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

    // ---------------- Lọc, sắp xếp & phân trang ----------------
    public void lamMoi() {
        refresh();
    }

    private void refresh() {
        capNhatThongKe();

        String kw = txtTim.getText().trim().toUpperCase();
        List<MaGiamGia> dsGoc = bus.layDanhSach();
        List<MaGiamGia> list = new ArrayList<>();

        for (MaGiamGia m : dsGoc) {
            boolean hetHan = bus.daHetHan(m);
            // Lọc theo chip
            if (boLocHienTai.equals("Còn hạn") && hetHan) continue;
            if (boLocHienTai.equals("Hết hạn") && !hetHan) continue;
            if (boLocHienTai.equals("Giảm theo %") && !m.isPhanTram()) continue;
            if (boLocHienTai.equals("Giảm tiền mặt") && m.isPhanTram()) continue;

            // Tìm kiếm
            if (!kw.isEmpty()) {
                String hay = m.getMa() + "|" + m.moTa().toUpperCase() + "|" + m.getGhiChu().toUpperCase();
                if (!hay.contains(kw)) continue;
            }
            list.add(m);
        }

        // Sắp xếp
        switch (sapXepHienTai) {
            case "Hạn dùng gần nhất" -> list.sort(Comparator.comparing(MaGiamGia::getHanDung));
            case "Mức giảm cao nhất" -> list.sort((a, b) -> Long.compare(b.getGiaTri(), a.getGiaTri()));
            case "Mã A-Z" -> list.sort(Comparator.comparing(MaGiamGia::getMa));
            default -> { /* Mới nhất giữ nguyên thứ tự */ }
        }
        filtered = list;

        // Cập nhật trạng thái active của chips
        for (int i = 0; i < FILTERS.length && i < chips.size(); i++) {
            boolean active = FILTERS[i].equals(boLocHienTai);
            RoundedButton chip = chips.get(i);
            chip.style(active ? Theme.DARK : Color.WHITE,
                       active ? Theme.DARK : new Color(0xF3F6F8),
                       active ? Color.WHITE : Theme.TEXT,
                       active ? Theme.DARK : Theme.BORDER);
        }
        btnSort.setText("Sắp xếp: " + sapXepHienTai + " ▾");

        // Phân trang
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), totalPages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());

        tableModel.setRowCount(0);
        for (int i = from; i < to; i++) {
            MaGiamGia m = filtered.get(i);
            boolean hetHan = bus.daHetHan(m);
            tableModel.addRow(new Object[] {
                i + 1,
                m.getMa(),
                m.isPhanTram() ? "Giảm theo %" : "Giảm theo số tiền",
                m.moTa(),
                DATE_FMT.format(m.getHanDung()),
                hetHan ? "Hết hạn" : "Còn hạn",
                m.getGhiChu().isEmpty() ? "Áp dụng khi thanh toán hóa đơn" : m.getGhiChu()
            });
        }

        if (filtered.isEmpty()) {
            lblFooter.setText("Không tìm thấy mã khuyến mãi nào phù hợp");
        } else {
            lblFooter.setText("Hiển thị " + (from + 1) + "–" + to + " trong tổng số " + filtered.size() + " mã khuyến mãi");
        }

        // Vẽ nút phân trang
        pnlPages.removeAll();
        if (totalPages > 1) {
            for (int p = 1; p <= totalPages; p++) {
                final int pNum = p;
                boolean active = (p == trang);
                RoundedButton b = new RoundedButton(String.valueOf(p), active ? Theme.DARK : Color.WHITE,
                        active ? Theme.DARK : new Color(0xF3F6F8), active ? Color.WHITE : Theme.TEXT,
                        active ? Theme.DARK : Theme.BORDER, 8).pad(12, 6);
                b.setFont(Theme.font(Font.PLAIN, 12));
                b.addActionListener(e -> { trang = pNum; refresh(); });
                pnlPages.add(b);
            }
        }
        pnlPages.revalidate();
        pnlPages.repaint();
    }

    private MaGiamGia layMaDangChon() {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        int from = (trang - 1) * PAGE_SIZE;
        int idx = from + r;
        return (idx < filtered.size()) ? filtered.get(idx) : null;
    }

    // =====================================================================
    // Chức năng Tạo mã khuyến mãi (Dialog)
    // =====================================================================
    private void taoMaKhuyenMai() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel card = new RoundedPanel(18, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Header Dialog
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Tạo mã khuyến mãi mới");
        lblTitle.setFont(Theme.font(Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT);
        JLabel lblSub = new JLabel("Mã khuyến mãi sẽ được áp dụng trực tiếp khi lập hóa đơn thanh toán.");
        lblSub.setFont(Theme.font(Font.PLAIN, 12));
        lblSub.setForeground(Theme.MUTED);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(lblSub);

        JButton btnClose = new JButton(Icons.of("close", 14, Theme.MUTED));
        btnClose.setBorder(new EmptyBorder(4, 8, 4, 0));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dlg.dispose());
        head.add(titleBox, BorderLayout.WEST);
        head.add(btnClose, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Body Dialog
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        PlaceholderField tfMa = new PlaceholderField("Ví dụ: SALE20, PETCARE, VIP50K...");
        PlaceholderField tfGia = new PlaceholderField("Ví dụ: 15 (tức 15%)");
        PlaceholderField tfHan = new PlaceholderField("dd/MM/yyyy");
        PlaceholderField tfGhiChu = new PlaceholderField("Ví dụ: Giảm giá cho thành viên mới, áp dụng toàn bộ dịch vụ...");

        tfHan.setText(DATE_FMT.format(LocalDate.now().plusDays(30)));
        setupField(tfMa);
        setupField(tfGia);
        setupField(tfHan);
        setupField(tfGhiChu);

        // 2 nút chọn loại giảm giá trực quan
        boolean[] phanTram = {true};
        RoundedButton btnOptPt = new RoundedButton("Giảm theo %", Theme.DARK, Theme.DARK, Color.WHITE, Theme.DARK, 10).pad(14, 10);
        RoundedButton btnOptTien = new RoundedButton("Giảm theo số tiền (₫)", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 10);

        Runnable capNhatNutLoai = () -> {
            btnOptPt.style(phanTram[0] ? Theme.DARK : Color.WHITE,
                           phanTram[0] ? Theme.DARK : new Color(0xF3F6F8),
                           phanTram[0] ? Color.WHITE : Theme.TEXT,
                           phanTram[0] ? Theme.DARK : Theme.BORDER);

            btnOptTien.style(!phanTram[0] ? Theme.DARK : Color.WHITE,
                             !phanTram[0] ? Theme.DARK : new Color(0xF3F6F8),
                             !phanTram[0] ? Color.WHITE : Theme.TEXT,
                             !phanTram[0] ? Theme.DARK : Theme.BORDER);

            tfGia.setPlaceholder(phanTram[0] ? "Ví dụ: 15 (tức 15%)" : "Ví dụ: 50.000 (tức 50.000₫)");
        };

        btnOptPt.addActionListener(e -> { phanTram[0] = true; capNhatNutLoai.run(); });
        btnOptTien.addActionListener(e -> { phanTram[0] = false; capNhatNutLoai.run(); });

        JPanel pnlLoai = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlLoai.setOpaque(false);
        pnlLoai.add(btnOptPt);
        pnlLoai.add(btnOptTien);

        // Nút chọn nhanh hạn dùng
        JPanel pnlQuickDates = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlQuickDates.setOpaque(false);
        pnlQuickDates.setBorder(new EmptyBorder(4, 0, 0, 0));
        String[] qdNames = {"+7 ngày", "+30 ngày", "+90 ngày", "Cuối năm"};
        for (String qdn : qdNames) {
            RoundedButton qb = new RoundedButton(qdn, Color.WHITE, new Color(0xF3F6F8), Theme.MUTED, Theme.BORDER, 6).pad(8, 4);
            qb.setFont(Theme.font(Font.PLAIN, 11));
            qb.addActionListener(e -> {
                LocalDate now = LocalDate.now();
                LocalDate target = switch (qdn) {
                    case "+7 ngày" -> now.plusDays(7);
                    case "+30 ngày" -> now.plusDays(30);
                    case "+90 ngày" -> now.plusDays(90);
                    default -> LocalDate.of(now.getYear(), 12, 31);
                };
                tfHan.setText(DATE_FMT.format(target));
            });
            pnlQuickDates.add(qb);
        }

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.add(createFieldGroup("Mã khuyến mãi *", wrapInput(tfMa, 38)));
        row1.add(createFieldGroup("Loại giảm giá *", pnlLoai));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);
        row2.add(createFieldGroup("Mức giảm *", wrapInput(tfGia, 38)));
        
        JPanel pnlHanWrap = new JPanel(new BorderLayout(0, 4));
        pnlHanWrap.setOpaque(false);
        pnlHanWrap.add(wrapInput(tfHan, 38), BorderLayout.CENTER);
        pnlHanWrap.add(pnlQuickDates, BorderLayout.SOUTH);
        row2.add(createFieldGroup("Hạn sử dụng (dd/MM/yyyy) *", pnlHanWrap));

        body.add(row1);
        body.add(Box.createVerticalStrut(14));
        body.add(row2);
        body.add(Box.createVerticalStrut(14));
        body.add(createFieldGroup("Ghi chú / Điều kiện áp dụng", wrapInput(tfGhiChu, 38)));

        card.add(body, BorderLayout.CENTER);

        // Footer Dialog
        JLabel lblErr = new JLabel(" ");
        lblErr.setFont(Theme.font(Font.PLAIN, 12));
        lblErr.setForeground(Theme.RED);

        RoundedButton btnHuy = new RoundedButton("Hủy bỏ", Color.WHITE, new Color(0xF3F6F8), Theme.MUTED, Theme.BORDER, 10).pad(18, 9);
        RoundedButton btnLuu = new RoundedButton("Tạo mã khuyến mãi", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(20, 9);

        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            String ma = tfMa.getText().trim().toUpperCase();
            if (ma.isEmpty()) {
                lblErr.setText("Vui lòng nhập mã khuyến mãi.");
                tfMa.requestFocus();
                return;
            }
            if (!ma.matches("[A-Z0-9_-]{3,20}")) {
                lblErr.setText("Mã gồm 3–20 ký tự: chữ cái, chữ số, gạch ngang/dưới (không dấu/khoảng trắng).");
                tfMa.requestFocus();
                return;
            }
            if (bus.tonTai(ma)) {
                lblErr.setText("Mã khuyến mãi \"" + ma + "\" đã tồn tại.");
                tfMa.requestFocus();
                return;
            }

            long giaTri;
            try {
                String sGia = tfGia.getText().trim().replace(".", "").replace(",", "");
                giaTri = Long.parseLong(sGia);
            } catch (Exception ex) {
                lblErr.setText("Mức giảm phải là số hợp lệ.");
                tfGia.requestFocus();
                return;
            }

            if (phanTram[0]) {
                if (giaTri < 1 || giaTri > 100) {
                    lblErr.setText("Phần trăm giảm phải từ 1% đến 100%.");
                    tfGia.requestFocus();
                    return;
                }
            } else {
                if (giaTri < 1_000 || giaTri > 100_000_000) {
                    lblErr.setText("Số tiền giảm phải từ 1.000₫ đến 100.000.000₫.");
                    tfGia.requestFocus();
                    return;
                }
            }

            LocalDate han;
            try {
                han = LocalDate.parse(tfHan.getText().trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                lblErr.setText("Hạn dùng không hợp lệ. Định dạng yêu cầu: dd/MM/yyyy.");
                tfHan.requestFocus();
                return;
            }
            if (han.isBefore(LocalDate.now())) {
                lblErr.setText("Hạn dùng không được ở quá khứ.");
                tfHan.requestFocus();
                return;
            }

            String ghiChu = tfGhiChu.getText().trim();
            try {
                bus.tao(ma, phanTram[0], giaTri, han, ghiChu);
                dlg.dispose();
                refresh();
                // Chọn ngay mã vừa tạo trong bảng
                chonMaTrongBang(ma);
                JOptionPane.showMessageDialog(this, "Đã tạo thành công mã khuyến mãi \"" + ma + "\"!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                lblErr.setText(ex.getMessage());
            }
        });

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.add(lblErr, BorderLayout.WEST);
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnHuy);
        pnlBtns.add(btnLuu);
        foot.add(pnlBtns, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.setContentPane(card);
        dlg.setSize(620, 480);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    // =====================================================================
    // Chức năng Chỉnh sửa mã khuyến mãi
    // =====================================================================
    private void suaMaKhuyenMai(MaGiamGia m) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel card = new RoundedPanel(18, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lblTitle = new JLabel("Chỉnh sửa mã khuyến mãi: " + m.getMa());
        lblTitle.setFont(Theme.font(Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT);
        JButton btnClose = new JButton(Icons.of("close", 14, Theme.MUTED));
        btnClose.setBorder(new EmptyBorder(4, 8, 4, 0));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dlg.dispose());
        head.add(lblTitle, BorderLayout.WEST);
        head.add(btnClose, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JTextField tfMa = new JTextField(m.getMa());
        tfMa.setEnabled(false);
        tfMa.setBackground(new Color(0xF3F6F8));
        setupField(tfMa);

        PlaceholderField tfGia = new PlaceholderField("");
        tfGia.setText(m.isPhanTram() ? String.valueOf(m.getGiaTri()) : String.format("%,d", m.getGiaTri()).replace(',', '.'));
        setupField(tfGia);

        PlaceholderField tfHan = new PlaceholderField("dd/MM/yyyy");
        tfHan.setText(DATE_FMT.format(m.getHanDung()));
        setupField(tfHan);

        PlaceholderField tfGhiChu = new PlaceholderField("");
        tfGhiChu.setText(m.getGhiChu());
        setupField(tfGhiChu);

        boolean[] phanTram = {m.isPhanTram()};
        RoundedButton btnOptPt = new RoundedButton("Giảm theo %", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 10);
        RoundedButton btnOptTien = new RoundedButton("Giảm theo số tiền (₫)", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 10);

        Runnable capNhatNutLoai = () -> {
            btnOptPt.style(phanTram[0] ? Theme.DARK : Color.WHITE,
                           phanTram[0] ? Theme.DARK : new Color(0xF3F6F8),
                           phanTram[0] ? Color.WHITE : Theme.TEXT,
                           phanTram[0] ? Theme.DARK : Theme.BORDER);

            btnOptTien.style(!phanTram[0] ? Theme.DARK : Color.WHITE,
                             !phanTram[0] ? Theme.DARK : new Color(0xF3F6F8),
                             !phanTram[0] ? Color.WHITE : Theme.TEXT,
                             !phanTram[0] ? Theme.DARK : Theme.BORDER);
        };
        capNhatNutLoai.run();

        btnOptPt.addActionListener(e -> { phanTram[0] = true; capNhatNutLoai.run(); });
        btnOptTien.addActionListener(e -> { phanTram[0] = false; capNhatNutLoai.run(); });

        JPanel pnlLoai = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlLoai.setOpaque(false);
        pnlLoai.add(btnOptPt);
        pnlLoai.add(btnOptTien);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);
        row1.add(createFieldGroup("Mã khuyến mãi (Cố định)", wrapInput(tfMa, 38)));
        row1.add(createFieldGroup("Loại giảm giá *", pnlLoai));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);
        row2.add(createFieldGroup("Mức giảm *", wrapInput(tfGia, 38)));
        row2.add(createFieldGroup("Hạn sử dụng (dd/MM/yyyy) *", wrapInput(tfHan, 38)));

        body.add(row1);
        body.add(Box.createVerticalStrut(14));
        body.add(row2);
        body.add(Box.createVerticalStrut(14));
        body.add(createFieldGroup("Ghi chú / Điều kiện áp dụng", wrapInput(tfGhiChu, 38)));

        card.add(body, BorderLayout.CENTER);

        JLabel lblErr = new JLabel(" ");
        lblErr.setFont(Theme.font(Font.PLAIN, 12));
        lblErr.setForeground(Theme.RED);

        RoundedButton btnHuy = new RoundedButton("Hủy bỏ", Color.WHITE, new Color(0xF3F6F8), Theme.MUTED, Theme.BORDER, 10).pad(18, 9);
        RoundedButton btnLuu = new RoundedButton("Lưu thay đổi", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(20, 9);

        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            long giaTri;
            try {
                String sGia = tfGia.getText().trim().replace(".", "").replace(",", "");
                giaTri = Long.parseLong(sGia);
            } catch (Exception ex) {
                lblErr.setText("Mức giảm phải là số hợp lệ.");
                return;
            }

            if (phanTram[0]) {
                if (giaTri < 1 || giaTri > 100) {
                    lblErr.setText("Phần trăm giảm phải từ 1% đến 100%.");
                    return;
                }
            } else {
                if (giaTri < 1_000 || giaTri > 100_000_000) {
                    lblErr.setText("Số tiền giảm phải từ 1.000₫ đến 100.000.000₫.");
                    return;
                }
            }

            LocalDate han;
            try {
                han = LocalDate.parse(tfHan.getText().trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                lblErr.setText("Hạn dùng không hợp lệ. Định dạng yêu cầu: dd/MM/yyyy.");
                return;
            }

            try {
                bus.sua(m.getMa(), phanTram[0], giaTri, han, tfGhiChu.getText().trim());
                dlg.dispose();
                refresh();
                chonMaTrongBang(m.getMa());
                JOptionPane.showMessageDialog(this, "Đã cập nhật mã khuyến mãi \"" + m.getMa() + "\" thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                lblErr.setText(ex.getMessage());
            }
        });

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.add(lblErr, BorderLayout.WEST);
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnHuy);
        pnlBtns.add(btnLuu);
        foot.add(pnlBtns, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.setContentPane(card);
        dlg.setSize(620, 440);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    // =====================================================================
    // Chức năng Xóa mã khuyến mãi
    // =====================================================================
    private void xoaMaKhuyenMai(MaGiamGia m) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa mã khuyến mãi \"" + m.getMa() + "\" không?\n"
                + "Lưu ý: Hóa đơn cũ đã áp dụng mã này vẫn được bảo lưu số tiền giảm.",
                "Xác nhận xóa mã", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                bus.xoa(m.getMa());
                refresh();
                JOptionPane.showMessageDialog(this, "Đã xóa mã khuyến mãi \"" + m.getMa() + "\".", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // Chức năng Xem chi tiết
    // =====================================================================
    private void xemChiTiet(MaGiamGia m) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel card = new RoundedPanel(18, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lblTitle = new JLabel("Chi tiết mã khuyến mãi");
        lblTitle.setFont(Theme.font(Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT);
        JButton btnClose = new JButton(Icons.of("close", 14, Theme.MUTED));
        btnClose.setBorder(new EmptyBorder(4, 8, 4, 0));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dlg.dispose());
        head.add(lblTitle, BorderLayout.WEST);
        head.add(btnClose, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 2, 16, 12));
        body.setOpaque(false);

        boolean hetHan = bus.daHetHan(m);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), m.getHanDung());
        String tgHan = hetHan ? "Đã hết hạn cách đây " + Math.abs(days) + " ngày" : "Còn " + days + " ngày sử dụng";

        body.add(createDetailItem("Mã khuyến mãi", m.getMa(), true));
        body.add(createDetailItem("Hình thức giảm", m.isPhanTram() ? "Giảm theo phần trăm (%)" : "Giảm tiền mặt cố định", false));
        body.add(createDetailItem("Mức giảm", m.moTa(), true));
        body.add(createDetailItem("Trạng thái", hetHan ? "Hết hạn" : "Còn hạn", false));
        body.add(createDetailItem("Hạn sử dụng", DATE_FMT.format(m.getHanDung()) + " (" + tgHan + ")", false));
        body.add(createDetailItem("Phạm vi áp dụng", "Toàn bộ hóa đơn thanh toán", false));
        body.add(createDetailItem("Ghi chú", m.getGhiChu().isEmpty() ? "Không có" : m.getGhiChu(), false));

        card.add(body, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        foot.setOpaque(false);
        RoundedButton btnDong = new RoundedButton("Đóng", Theme.DARK, Theme.DARK, Color.WHITE, Theme.DARK, 10).pad(22, 9);
        btnDong.addActionListener(e -> dlg.dispose());
        foot.add(btnDong);
        card.add(foot, BorderLayout.SOUTH);

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.setContentPane(card);
        dlg.setSize(540, 360);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private JPanel createDetailItem(String label, String value, boolean highlight) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(Theme.font(highlight ? Font.BOLD : Font.PLAIN, 14));
        v.setForeground(highlight ? Theme.PRIMARY : Theme.TEXT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(v);
        return p;
    }

    // =====================================================================
    // Chức năng Xuất Excel (CSV)
    // =====================================================================
    private void xuatExcel() {
        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu danh sách mã khuyến mãi (Excel / CSV)");
        fc.setSelectedFile(new File("DanhSachKhuyenMai_" + LocalDate.now().toString() + ".csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new File(f.getParentFile(), f.getName() + ".csv");
            }
            try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
                // UTF-8 BOM để Excel hiển thị đúng dấu tiếng Việt
                w.write('\ufeff');
                w.write("STT,Mã Khuyến Mãi,Loại Giảm Giá,Mức Giảm,Hạn Sử Dụng,Trạng Thái,Ghi Chú\n");
                for (int i = 0; i < filtered.size(); i++) {
                    MaGiamGia m = filtered.get(i);
                    boolean hetHan = bus.daHetHan(m);
                    w.write(String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            i + 1,
                            m.getMa(),
                            m.isPhanTram() ? "Giảm theo %" : "Giảm tiền mặt",
                            m.moTa(),
                            DATE_FMT.format(m.getHanDung()),
                            hetHan ? "Hết hạn" : "Còn hạn",
                            m.getGhiChu().replace("\"", "\"\"")));
                }
                JOptionPane.showMessageDialog(this, "Xuất file thành công:\n" + f.getAbsolutePath(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chonMaTrongBang(String ma) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (ma.equalsIgnoreCase(String.valueOf(table.getValueAt(i, 1)))) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                break;
            }
        }
    }

    // ---------------- UI Helpers ----------------
    private JPanel createFieldGroup(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.TEXT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel wrapInput(JComponent comp, int h) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(p.getPreferredSize().width, h));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void setupField(JTextField f) {
        f.setFont(Theme.font(Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1), new EmptyBorder(0, 10, 0, 10)));
        f.setBackground(Color.WHITE);
    }

    // ---------------- Renderers ----------------
    private static class PromoCodeCellRenderer extends JLabel implements TableCellRenderer {
        PromoCodeCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(Theme.font(Font.BOLD, 13));
            setForeground(Theme.PRIMARY);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            setText(v == null ? "" : v.toString());
            setBackground(sel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : Theme.FIELD_BG));
            setForeground(sel ? t.getSelectionForeground() : Theme.PRIMARY);
            return this;
        }
    }

    private static class DiscountValueCellRenderer extends JLabel implements TableCellRenderer {
        DiscountValueCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setFont(Theme.font(Font.BOLD, 13));
            setBorder(new EmptyBorder(0, 10, 0, 14));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            setText(v == null ? "" : v.toString());
            setBackground(sel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : Theme.FIELD_BG));
            setForeground(sel ? t.getSelectionForeground() : Theme.TEXT);
            return this;
        }
    }

    private static class StatusBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        StatusBadgeRenderer() {
            setLayout(new GridBagLayout());
            lbl.setFont(Theme.font(Font.BOLD, 11));
            lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
            add(lbl);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            String text = v == null ? "" : v.toString();
            boolean hetHan = text.equalsIgnoreCase("Hết hạn");
            lbl.setText(text);
            lbl.setOpaque(true);

            if (hetHan) {
                lbl.setBackground(new Color(0xFEE2E2));
                lbl.setForeground(new Color(0xDC2626));
            } else {
                lbl.setBackground(new Color(0xDCFCE7));
                lbl.setForeground(new Color(0x16A34A));
            }

            setBackground(sel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : Theme.FIELD_BG));
            return this;
        }
    }
}
