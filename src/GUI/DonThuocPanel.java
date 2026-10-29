package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import Bus.DonThuocBUS;
import Bus.KhoHangBUS;
import Bus.PhacDoBUS;
import Bus.ThuCungBUS;
import Entity.ChiTietDonThuoc;
import Entity.DonThuoc;
import Entity.PhacDoDieuTri;
import Entity.SanPham;
import Entity.ThuCung;

/**
 * Giao diện Quản lý Đơn thuốc: Kê đơn, theo dõi, cấp phát và in đơn thuốc.
 */
public class DonThuocPanel extends JPanel {
    private static final int PAGE_SIZE = 8;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DonThuocBUS bus;
    private final ThuCungBUS thuCungBus;
    private final KhoHangBUS khoHangBus;
    private final PhacDoBUS phacDoBus;

    private final String[] FILTERS = {"Tất cả", DonThuoc.CHO_CAP_THUOC, DonThuoc.DA_CAP_PHAT, DonThuoc.DANG_DIEU_TRI, DonThuoc.HOAN_TAT, DonThuoc.DA_HUY};
    private String boLocHienTai = "Tất cả";
    private int trang = 1;
    private List<DonThuoc> filtered = new ArrayList<>();

    // Thẻ KPI
    private JLabel lblStatTong;
    private JLabel lblStatChoCap;
    private JLabel lblStatDangTri;
    private JLabel lblStatDoanhThu;

    private final PlaceholderField txtTim = new PlaceholderField("Tìm mã đơn, thú cưng, chủ nuôi, bác sĩ, chẩn đoán, tên thuốc...");
    private final List<RoundedButton> chips = new ArrayList<>();
    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public DonThuocPanel() {
        this(new DonThuocBUS(), new ThuCungBUS(), new KhoHangBUS(), new PhacDoBUS());
    }

    public DonThuocPanel(DonThuocBUS bus, ThuCungBUS thuCungBus, KhoHangBUS khoHangBus) {
        this(bus, thuCungBus, khoHangBus, new PhacDoBUS());
    }

    public DonThuocPanel(DonThuocBUS bus, ThuCungBUS thuCungBus, KhoHangBUS khoHangBus, PhacDoBUS phacDoBus) {
        super(new BorderLayout(0, 16));
        this.bus = bus;
        this.thuCungBus = thuCungBus;
        this.khoHangBus = khoHangBus;
        this.phacDoBus = phacDoBus;

        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 26, 20, 26));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(buildBreadcrumb(), BorderLayout.NORTH);
        top.add(buildHeader(), BorderLayout.CENTER);
        top.add(buildStats(), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        bus.addListener(this::refresh);
        refresh();
    }

    private JComponent buildBreadcrumb() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel l1 = new JLabel("Trang chủ");
        l1.setFont(Theme.font(Font.PLAIN, 12));
        l1.setForeground(Theme.MUTED);
        JLabel s1 = new JLabel("›");
        s1.setFont(Theme.font(Font.PLAIN, 12));
        s1.setForeground(Theme.MUTED);
        JLabel l2 = new JLabel("Điều Trị & Đơn Thuốc");
        l2.setFont(Theme.font(Font.PLAIN, 12));
        l2.setForeground(Theme.MUTED);
        JLabel s2 = new JLabel("›");
        s2.setFont(Theme.font(Font.PLAIN, 12));
        s2.setForeground(Theme.MUTED);
        JLabel l3 = new JLabel("Quản lý Đơn thuốc");
        l3.setFont(Theme.font(Font.BOLD, 12));
        l3.setForeground(Theme.TEXT);
        p.add(l1); p.add(s1); p.add(l2); p.add(s2); p.add(l3);
        return p;
    }

    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JLabel t = new JLabel("Quản lý Đơn thuốc");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Kê đơn thuốc y tế, quản lý cấp phát và theo dõi liều dùng cho thú cưng");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(4));
        left.add(s);

        RoundedButton btnExcel = new RoundedButton("Xuất danh sách", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(16, 10);
        RoundedButton btnThem = new RoundedButton("+ Kê đơn thuốc mới", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(18, 10);
        btnExcel.addActionListener(e -> xuatDanhSach());
        btnThem.addActionListener(e -> moDialogKeDon(null, null));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnExcel);
        right.add(btnThem);

        head.add(left, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);
        return head;
    }

    private JComponent buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        lblStatTong = new JLabel(String.valueOf(bus.getTongSoDon()));
        lblStatChoCap = new JLabel(String.valueOf(bus.getSoChoCapThuoc()));
        lblStatDangTri = new JLabel(String.valueOf(bus.getSoDangDieuTri()));
        lblStatDoanhThu = new JLabel(Theme.money(bus.getTongDoanhThu()));

        row.add(buildStatCard("TỔNG ĐƠN THUỐC", lblStatTong, "medical", Theme.ACCENT, Theme.BLUE_BG));
        row.add(buildStatCard("CHỜ CẤP THUỐC", lblStatChoCap, "box", Theme.ORANGE, Theme.ORANGE_BG));
        row.add(buildStatCard("ĐANG ĐIỀU TRỊ", lblStatDangTri, "sync", Theme.BLUE_TEXT, Theme.BLUE_BG));
        row.add(buildStatCard("DOANH THU TIỀN THUỐC", lblStatDoanhThu, "bar-chart", Theme.GREEN, Theme.GREEN_BG));
        return row;
    }

    private JPanel buildStatCard(String title, JLabel val, String icon, Color icColor, Color icBg) {
        RoundedPanel c = new RoundedPanel(14, Color.WHITE, Theme.BORDER);
        c.setLayout(new BorderLayout(12, 0));
        c.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(Theme.font(Font.BOLD, 11));
        t.setForeground(Theme.MUTED);

        val.setFont(Theme.font(Font.BOLD, 20));
        val.setForeground(Theme.TEXT);

        text.add(t);
        text.add(Box.createVerticalStrut(4));
        text.add(val);

        JPanel icWrap = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(icBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icWrap.setOpaque(false);
        icWrap.setPreferredSize(new Dimension(42, 42));
        JLabel icLbl = new JLabel(Icons.of(icon, 20, icColor));
        icWrap.add(icLbl);

        c.add(text, BorderLayout.CENTER);
        c.add(icWrap, BorderLayout.EAST);
        return c;
    }

    private JComponent buildTableCard() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 18, 12, 18));

        card.add(buildFilterBar(), BorderLayout.NORTH);
        card.add(buildTable(), BorderLayout.CENTER);
        card.add(buildFooter(), BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        // Search field
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(320, 36));
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { trang = 1; refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { trang = 1; refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { trang = 1; refresh(); }
        });
        searchBox.add(txtTim, BorderLayout.CENTER);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(searchBox);

        for (String f : FILTERS) {
            RoundedButton chip = new RoundedButton(f, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(12, 7);
            chip.setFont(Theme.font(Font.PLAIN, 12));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 34));
            chip.addActionListener(e -> { boLocHienTai = f; trang = 1; refresh(); });
            chips.add(chip);
            left.add(chip);
        }

        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JComponent buildTable() {
        table.setRowHeight(48);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0xEEF4F8));
        table.setSelectionForeground(Theme.TEXT);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new HeaderCell());

        int[] widths = {85, 95, 165, 140, 160, 75, 110, 115, 140};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getColumnModel().getColumn(0).setCellRenderer(new CodeCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new DateCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new PetCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new DoctorCell());
        table.getColumnModel().getColumn(4).setCellRenderer(new DiagnosisCell());
        table.getColumnModel().getColumn(5).setCellRenderer(new DrugCountCell());
        table.getColumnModel().getColumn(6).setCellRenderer(new MoneyCell());
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCell());
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionCell());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 8) {
                    DonThuoc dt = (DonThuoc) model.getValueAt(row, 0);
                    int xInCell = e.getX() - table.getCellRect(row, col, false).x;
                    int slot = Math.min(4, Math.max(0, xInCell / 28));
                    switch (slot) {
                        case 0 -> xemChiTiet(dt);
                        case 1 -> {
                            if (DonThuoc.CHO_CAP_THUOC.equals(dt.getTrangThai())) {
                                capPhatThuoc(dt);
                            } else {
                                JOptionPane.showMessageDialog(DonThuocPanel.this,
                                        "Đơn thuốc " + dt.getMaDonThuoc() + " đang ở trạng thái '" + dt.getTrangThai() + "'.\nChỉ đơn thuốc 'Chờ cấp thuốc' mới cần cấp phát.",
                                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        case 2 -> moDialogKeDon(dt, null);
                        case 3 -> inDonThuoc(dt);
                        case 4 -> xoaDon(dt);
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JComponent buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.ROW_LINE), new EmptyBorder(12, 10, 4, 10)));
        lblFooter.setFont(Theme.font(Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);
        f.add(lblFooter, BorderLayout.WEST);
        f.add(pnlPages, BorderLayout.EAST);
        return f;
    }

    public void refresh() {
        String kw = norm(txtTim.getText().trim());
        List<DonThuoc> list = new ArrayList<>();
        for (DonThuoc dt : bus.layDanhSach()) {
            if (!"Tất cả".equals(boLocHienTai) && !boLocHienTai.equals(dt.getTrangThai())) continue;

            if (!kw.isEmpty()) {
                String hay = norm(dt.getMaDonThuoc()) + "|" + norm(dt.getTenThuCung()) + "|" +
                             norm(dt.getTenChu()) + "|" + norm(dt.getBacSi()) + "|" +
                             norm(dt.getChanDoan()) + "|" + dt.getSdtChu().replaceAll("\\D", "");
                for (ChiTietDonThuoc ct : dt.getDanhSachThuoc()) {
                    hay += "|" + norm(ct.getTenThuoc()) + "|" + norm(ct.getMaSKU());
                }
                if (!hay.contains(kw)) continue;
            }
            list.add(dt);
        }
        filtered = list;

        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        lblFooter.setText(filtered.isEmpty()
                ? "Không tìm thấy đơn thuốc nào phù hợp"
                : "Hiển thị " + (from + 1) + "–" + to + " trong tổng số " + filtered.size() + " đơn thuốc");

        for (RoundedButton c : chips) {
            boolean on = c.getText().equals(boLocHienTai);
            if (on) c.style(Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null);
            else c.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

        if (lblStatTong != null) lblStatTong.setText(String.valueOf(bus.getTongSoDon()));
        if (lblStatChoCap != null) lblStatChoCap.setText(String.valueOf(bus.getSoChoCapThuoc()));
        if (lblStatDangTri != null) lblStatDangTri.setText(String.valueOf(bus.getSoDangDieuTri()));
        if (lblStatDoanhThu != null) lblStatDoanhThu.setText(Theme.money(bus.getTongDoanhThu()));

        buildPager(pages);
        revalidate();
        repaint();
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        RoundedButton btnPrev = new RoundedButton("‹", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8);
        btnPrev.setPreferredSize(new Dimension(30, 30));
        btnPrev.setEnabled(trang > 1);
        btnPrev.addActionListener(e -> { trang--; refresh(); });
        pnlPages.add(btnPrev);

        for (int p = 1; p <= pages; p++) {
            final int tp = p;
            boolean active = (p == trang);
            RoundedButton b = active
                    ? new RoundedButton(String.valueOf(p), Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8)
                    : new RoundedButton(String.valueOf(p), Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8);
            b.setPreferredSize(new Dimension(30, 30));
            b.setFont(Theme.font(Font.PLAIN, 12));
            b.addActionListener(e -> { trang = tp; refresh(); });
            pnlPages.add(b);
        }

        RoundedButton btnNext = new RoundedButton("›", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8);
        btnNext.setPreferredSize(new Dimension(30, 30));
        btnNext.setEnabled(trang < pages);
        btnNext.addActionListener(e -> { trang++; refresh(); });
        pnlPages.add(btnNext);
        pnlPages.revalidate();
    }

    // ---------------- Actions ----------------
    public void keDonChoThuCung(ThuCung tc) {
        moDialogKeDon(null, tc);
    }

    private void capPhatThuoc(DonThuoc dt) {
        int r = JOptionPane.showConfirmDialog(this,
                "Xác nhận xuất kho và cấp phát thuốc cho đơn thuốc " + dt.getMaDonThuoc() + " (" + dt.getTenThuCung() + ")?\n" +
                "Hệ thống sẽ tự động trừ số lượng tồn kho tương ứng của các thuốc trong danh mục.",
                "Cấp phát thuốc", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            boolean ok = bus.capPhatThuoc(dt.getMaDonThuoc(), khoHangBus);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Đã xuất kho và cấp phát thuốc thành công!\nTrạng thái đơn chuyển sang: Đã cấp phát.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cấp phát thuốc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaDon(DonThuoc dt) {
        int r = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa/hủy đơn thuốc " + dt.getMaDonThuoc() + " không?",
                "Xác nhận xóa đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            bus.xoa(dt.getMaDonThuoc());
        }
    }

    private void xuatDanhSach() {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File("DanhSach_DonThuoc_" + LocalDate.now() + ".csv"));
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = ch.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
                pw.write('\ufeff'); // UTF-8 BOM
                pw.println("Mã đơn,Ngày kê,Thú cưng,Loài,Chủ nuôi,SĐT,Bác sĩ,Chẩn đoán,Số thuốc,Tổng tiền (VNĐ),Trạng thái,Lời dặn");
                for (DonThuoc d : filtered) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\"\n",
                            d.getMaDonThuoc(), d.getNgayKe().format(DATE_FMT), d.getTenThuCung(), d.getLoai(),
                            d.getTenChu(), d.getSdtChu(), d.getBacSi(), d.getChanDoan().replace("\"", "\"\""),
                            d.getSoLoaiThuoc(), d.tinhTongTien(), d.getTrangThai(), d.getLoiDan().replace("\"", "\"\""));
                }
                JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " đơn thuốc thành công:\n" + f.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------- Dialog Xem chi tiết đơn thuốc ----------------
    private void xemChiTiet(DonThuoc dt) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Phiếu Đơn Thuốc Y Tế - " + dt.getMaDonThuoc(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(680, 720);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 28, 20, 28));

        // Header phiếu
        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        pnlHead.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, Theme.ACCENT), new EmptyBorder(0, 0, 12, 0)));

        JLabel lblBrand = new JLabel("PHÒNG KHÁM THÚ Y PET STATION");
        lblBrand.setFont(Theme.font(Font.BOLD, 17));
        lblBrand.setForeground(Theme.ACCENT);
        JLabel lblSub = new JLabel("Địa chỉ: 123 Đường Thú Cưng, TP.HCM • Hotline: 1900 6868");
        lblSub.setFont(Theme.font(Font.PLAIN, 12));
        lblSub.setForeground(Theme.MUTED);
        JPanel pTitle = new JPanel();
        pTitle.setOpaque(false);
        pTitle.setLayout(new BoxLayout(pTitle, BoxLayout.Y_AXIS));
        pTitle.add(lblBrand);
        pTitle.add(Box.createVerticalStrut(3));
        pTitle.add(lblSub);

        JPanel pCode = new JPanel();
        pCode.setOpaque(false);
        pCode.setLayout(new BoxLayout(pCode, BoxLayout.Y_AXIS));
        JLabel lblCode = new JLabel("MÃ ĐƠN: " + dt.getMaDonThuoc());
        lblCode.setFont(Theme.font(Font.BOLD, 15));
        lblCode.setForeground(Theme.DARK);
        JLabel lblDate = new JLabel("Ngày kê: " + dt.getNgayKe().format(DATE_FMT));
        lblDate.setFont(Theme.font(Font.PLAIN, 12));
        lblDate.setForeground(Theme.MUTED);
        pCode.add(lblCode);
        pCode.add(Box.createVerticalStrut(3));
        pCode.add(lblDate);

        pnlHead.add(pTitle, BorderLayout.WEST);
        pnlHead.add(pCode, BorderLayout.EAST);

        // Body: Thông tin thú cưng & Bệnh án
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        boolean hasPD = (dt.getMaPhacDo() != null && !dt.getMaPhacDo().trim().isEmpty());
        JPanel pInfo = new JPanel(new GridLayout(hasPD ? 3 : 2, 2, 14, 6));
        pInfo.setOpaque(false);
        pInfo.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE), new EmptyBorder(10, 0, 12, 0)));

        pInfo.add(makeInfoLabel("Bệnh nhân (Thú cưng):", dt.getTenThuCung() + " (" + dt.getLoai() + " - " + dt.getGiong() + " • " + dt.getCanNang() + "kg)"));
        pInfo.add(makeInfoLabel("Chủ sở hữu:", dt.getTenChu() + " - " + dt.getSdtChu()));
        pInfo.add(makeInfoLabel("Bác sĩ kê đơn:", dt.getBacSi()));
        pInfo.add(makeInfoLabel("Trạng thái đơn:", dt.getTrangThai()));
        if (hasPD) {
            pInfo.add(makeInfoLabel("Phác đồ liên kết:", dt.getMaPhacDo()));
            pInfo.add(makeInfoLabel("Hẹn tái khám:", dt.getNgayTaiKham() != null ? dt.getNgayTaiKham().format(DATE_FMT) : "Không"));
        }

        JPanel pDiag = new JPanel(new BorderLayout(8, 0));
        pDiag.setOpaque(false);
        pDiag.setBorder(new EmptyBorder(10, 0, 10, 0));
        JLabel lblD1 = new JLabel("Chẩn đoán lâm sàng: ");
        lblD1.setFont(Theme.font(Font.BOLD, 13));
        lblD1.setForeground(Theme.TEXT);
        JLabel lblD2 = new JLabel(dt.getChanDoan());
        lblD2.setFont(Theme.font(Font.PLAIN, 13));
        lblD2.setForeground(Theme.TEXT);
        pDiag.add(lblD1, BorderLayout.WEST);
        pDiag.add(lblD2, BorderLayout.CENTER);

        body.add(pInfo);
        body.add(pDiag);

        // Bảng danh sách thuốc
        JLabel lblListTitle = new JLabel("CHỈ ĐỊNH THUỐC VÀ HƯỚNG DẪN SỬ DỤNG");
        lblListTitle.setFont(Theme.font(Font.BOLD, 13));
        lblListTitle.setForeground(Theme.ACCENT);
        lblListTitle.setBorder(new EmptyBorder(6, 0, 6, 0));
        body.add(lblListTitle);

        String[] cols = {"STT", "Tên thuốc / Biệt dược", "ĐVT", "SL", "Đơn giá", "Thành tiền", "Cách dùng"};
        Object[][] data = new Object[dt.getDanhSachThuoc().size()][7];
        for (int i = 0; i < dt.getDanhSachThuoc().size(); i++) {
            ChiTietDonThuoc ct = dt.getDanhSachThuoc().get(i);
            data[i][0] = i + 1;
            data[i][1] = ct.getTenThuoc() + (ct.getMaSKU().isEmpty() ? "" : " [" + ct.getMaSKU() + "]");
            data[i][2] = ct.getDonViTinh();
            data[i][3] = ct.getSoLuong();
            data[i][4] = Theme.money(ct.getDonGia());
            data[i][5] = Theme.money(ct.getThanhTien());
            data[i][6] = ct.getCachDung();
        }
        JTable tblThuoc = new JTable(data, cols);
        tblThuoc.setRowHeight(36);
        tblThuoc.setFont(Theme.font(Font.PLAIN, 12));
        tblThuoc.getTableHeader().setFont(Theme.font(Font.BOLD, 12));
        tblThuoc.getTableHeader().setBackground(new Color(0xF4F7F9));
        tblThuoc.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblThuoc.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblThuoc.getColumnModel().getColumn(2).setPreferredWidth(45);
        tblThuoc.getColumnModel().getColumn(3).setPreferredWidth(40);
        tblThuoc.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblThuoc.getColumnModel().getColumn(5).setPreferredWidth(90);
        tblThuoc.getColumnModel().getColumn(6).setPreferredWidth(180);

        JScrollPane spThuoc = new JScrollPane(tblThuoc);
        spThuoc.setPreferredSize(new Dimension(620, 180));
        body.add(spThuoc);

        // Tổng tiền & Lời dặn
        JPanel pTotal = new JPanel(new BorderLayout());
        pTotal.setOpaque(false);
        pTotal.setBorder(new EmptyBorder(10, 0, 8, 0));
        JLabel lblSum = new JLabel("TỔNG TIỀN THUỐC: " + Theme.money(dt.tinhTongTien()));
        lblSum.setFont(Theme.font(Font.BOLD, 15));
        lblSum.setForeground(Theme.GREEN);
        pTotal.add(lblSum, BorderLayout.EAST);
        body.add(pTotal);

        if (!dt.getLoiDan().isEmpty()) {
            JPanel pLoiDan = new JPanel(new BorderLayout(6, 0));
            pLoiDan.setOpaque(false);
            JLabel ldTitle = new JLabel("Lời dặn của bác sĩ: ");
            ldTitle.setFont(Theme.font(Font.BOLD, 12));
            JLabel ldContent = new JLabel("<html>" + dt.getLoiDan() + "</html>");
            ldContent.setFont(Theme.font(Font.PLAIN, 12));
            pLoiDan.add(ldTitle, BorderLayout.WEST);
            pLoiDan.add(ldContent, BorderLayout.CENTER);
            body.add(pLoiDan);
        }

        if (dt.getNgayTaiKham() != null) {
            JLabel lblTk = new JLabel("Hẹn ngày tái khám: " + dt.getNgayTaiKham().format(DATE_FMT));
            lblTk.setFont(Theme.font(Font.BOLD, 12));
            lblTk.setForeground(Theme.BLUE_TEXT);
            lblTk.setBorder(new EmptyBorder(4, 0, 4, 0));
            body.add(lblTk);
        }

        // Chữ ký
        JPanel pSign = new JPanel(new BorderLayout());
        pSign.setOpaque(false);
        pSign.setBorder(new EmptyBorder(16, 0, 10, 30));
        JPanel signBox = new JPanel();
        signBox.setOpaque(false);
        signBox.setLayout(new BoxLayout(signBox, BoxLayout.Y_AXIS));
        JLabel s1 = new JLabel("Bác sĩ khám bệnh");
        s1.setFont(Theme.font(Font.BOLD, 13));
        JLabel s2 = new JLabel("(Ký và ghi rõ họ tên)");
        s2.setFont(Theme.font(Font.ITALIC, 11));
        s2.setForeground(Theme.MUTED);
        JLabel s3 = new JLabel(dt.getBacSi());
        s3.setFont(Theme.font(Font.BOLD, 13));
        s3.setForeground(Theme.TEXT);
        s3.setBorder(new EmptyBorder(30, 0, 0, 0));
        signBox.add(s1);
        signBox.add(s2);
        signBox.add(s3);
        pSign.add(signBox, BorderLayout.EAST);
        body.add(pSign);

        root.add(pnlHead, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        // Buttons
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtns.setOpaque(false);

        RoundedButton btnPrint = new RoundedButton("In đơn thuốc", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(16, 9);
        btnPrint.setIcon(Icons.of("print", 16, Color.WHITE));
        btnPrint.addActionListener(e -> inDonThuoc(dt));

        RoundedButton btnEdit = new RoundedButton("Chỉnh sửa đơn thuốc", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 9);
        btnEdit.setIcon(Icons.of("edit", 16, Theme.TEXT));
        btnEdit.addActionListener(e -> {
            dlg.dispose();
            moDialogKeDon(dt, null);
        });

        RoundedButton btnClose = new RoundedButton("Đóng", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 9);
        btnClose.addActionListener(e -> dlg.dispose());

        pBtns.add(btnPrint);
        pBtns.add(btnEdit);
        if (DonThuoc.CHO_CAP_THUOC.equals(dt.getTrangThai())) {
            RoundedButton btnCap = new RoundedButton("Cấp phát thuốc", Theme.GREEN, new Color(0x23823F), Color.WHITE, null, 8).pad(16, 9);
            btnCap.addActionListener(e -> {
                dlg.dispose();
                capPhatThuoc(dt);
            });
            pBtns.add(btnCap);
        }
        pBtns.add(btnClose);
        root.add(pBtns, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JPanel makeInfoLabel(String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(Theme.font(Font.PLAIN, 12));
        v.setForeground(Theme.TEXT);
        p.add(l);
        p.add(v);
        return p;
    }

    // ---------------- Dialog Kê Đơn Thuốc Mới / Sửa ----------------
    // ---------------- Dialog Kê Đơn Thuốc Mới / Sửa ----------------
    private void moDialogKeDon(DonThuoc editTarget) {
        moDialogKeDon(editTarget, null);
    }

    private void moDialogKeDon(DonThuoc editTarget, ThuCung preSelectedTC) {
        boolean isEdit = (editTarget != null && editTarget.getMaDonThuoc() != null && !editTarget.getMaDonThuoc().trim().isEmpty());
        Window owner = SwingUtilities.getWindowAncestor(this);

        String title;
        if (isEdit) {
            title = "Chỉnh sửa đơn thuốc: " + editTarget.getMaDonThuoc() + " (" + editTarget.getTenThuCung() + ")";
        } else if (preSelectedTC != null) {
            title = "Kê đơn thuốc mới cho: " + preSelectedTC.getTen() + " (" + preSelectedTC.getMaTC() + ")";
        } else {
            title = "Kê đơn thuốc mới cho thú cưng";
        }

        JDialog dlg = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(880, 720);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 22, 16, 22));

        // Form Fields
        List<ThuCung> dsTC = thuCungBus.layDanhSach();
        JComboBox<String> cbThuCung = new JComboBox<>();
        boolean hasPlaceholder = (!isEdit && preSelectedTC == null);
        if (hasPlaceholder) {
            cbThuCung.addItem("-- Chọn thú cưng kê đơn --");
        }
        for (ThuCung tc : dsTC) {
            cbThuCung.addItem(tc.getMaTC() + " - " + tc.getTen() + " (" + tc.getLoai() + " - " + tc.getGiong() + ") - Chủ: " + tc.getTenChu());
        }

        JTextField txtChu = field(); txtChu.setEditable(false);
        JTextField txtSdt = field(); txtSdt.setEditable(false);
        JTextField txtCanNang = field(); txtCanNang.setEditable(false);
        JTextField txtLoaiGiong = field(); txtLoaiGiong.setEditable(false);

        JComboBox<String> cbPhacDo = new JComboBox<>();
        List<PhacDoDieuTri> listPD = new ArrayList<>();

        Runnable updatePhacDoList = () -> {
            cbPhacDo.removeAllItems();
            cbPhacDo.addItem("-- Không liên kết phác đồ --");
            int idx = cbThuCung.getSelectedIndex();
            int tcIndex = hasPlaceholder ? idx - 1 : idx;
            if (tcIndex >= 0 && tcIndex < dsTC.size()) {
                ThuCung tc = dsTC.get(tcIndex);
                listPD.clear();
                listPD.addAll(phacDoBus.layPhacDoCuaThuCung(tc.getMaTC()));
                for (PhacDoDieuTri pd : listPD) {
                    cbPhacDo.addItem(pd.getMaPhacDo() + " - " + pd.getTenPhacDo() + " (" + pd.getTrangThai() + ")");
                }
                if (isEdit && editTarget.getMaPhacDo() != null && !editTarget.getMaPhacDo().trim().isEmpty()) {
                    for (int i = 0; i < listPD.size(); i++) {
                        if (listPD.get(i).getMaPhacDo().equalsIgnoreCase(editTarget.getMaPhacDo())) {
                            cbPhacDo.setSelectedIndex(i + 1);
                            break;
                        }
                    }
                }
            }
        };

        Runnable syncPetInfo = () -> {
            int idx = cbThuCung.getSelectedIndex();
            int tcIndex = hasPlaceholder ? idx - 1 : idx;
            if (tcIndex >= 0 && tcIndex < dsTC.size()) {
                ThuCung tc = dsTC.get(tcIndex);
                txtChu.setText(tc.getTenChu());
                txtSdt.setText(tc.getSdtChu());
                txtCanNang.setText(tc.getCanNang() + " kg");
                txtLoaiGiong.setText(tc.getLoai() + " - " + tc.getGiong());
            } else {
                txtChu.setText("");
                txtSdt.setText("");
                txtCanNang.setText("");
                txtLoaiGiong.setText("");
            }
            updatePhacDoList.run();
        };
        cbThuCung.addActionListener(e -> syncPetInfo.run());

        if (isEdit && editTarget.getMaTC() != null) {
            for (int i = 0; i < dsTC.size(); i++) {
                if (dsTC.get(i).getMaTC().equalsIgnoreCase(editTarget.getMaTC())) {
                    cbThuCung.setSelectedIndex(i);
                    break;
                }
            }
            cbThuCung.setEnabled(false);
        } else if (preSelectedTC != null) {
            for (int i = 0; i < dsTC.size(); i++) {
                if (dsTC.get(i).getMaTC().equalsIgnoreCase(preSelectedTC.getMaTC())) {
                    cbThuCung.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cbThuCung.setSelectedIndex(0);
        }
        syncPetInfo.run();

        JComboBox<String> cbBacSi = new JComboBox<>(new String[]{
                "BS. Nguyễn Thanh Tùng", "BS. Lê Mai Anh", "BS. Phạm Quốc Việt"
        });
        if (isEdit && editTarget.getBacSi() != null) cbBacSi.setSelectedItem(editTarget.getBacSi());

        PlaceholderField txtChanDoan = new PlaceholderField("Nhập chẩn đoán bệnh...");
        if (isEdit) txtChanDoan.setText(editTarget.getChanDoan());

        PlaceholderField txtTrieuChung = new PlaceholderField("Nhập triệu chứng lâm sàng...");
        if (isEdit) txtTrieuChung.setText(editTarget.getTrieuChung());

        PlaceholderField txtLoiDan = new PlaceholderField("Lời dặn của bác sĩ về kiêng cữ, theo dõi...");
        if (isEdit) txtLoiDan.setText(editTarget.getLoiDan());

        JTextField txtTaiKham = field();
        txtTaiKham.setText(LocalDate.now().plusDays(7).format(DATE_FMT));
        if (isEdit && editTarget.getNgayTaiKham() != null) {
            txtTaiKham.setText(editTarget.getNgayTaiKham().format(DATE_FMT));
        }

        // Bảng danh mục thuốc kê
        List<ChiTietDonThuoc> listThuoc = new ArrayList<>();
        if (isEdit && editTarget.getDanhSachThuoc() != null) {
            for (ChiTietDonThuoc c : editTarget.getDanhSachThuoc()) {
                listThuoc.add(new ChiTietDonThuoc(c.getMaSKU(), c.getTenThuoc(), c.getDonViTinh(), c.getSoLuong(), c.getDonGia(), c.getCachDung()));
            }
        }

        String[] subCols = {"Tên thuốc / Biệt dược", "ĐVT", "Số lượng", "Đơn giá (₫)", "Thành tiền", "Liều lượng & Hướng dẫn sử dụng", "Xóa"};
        JLabel lblTotal = new JLabel("Tổng tiền thuốc: 0₫");
        lblTotal.setFont(Theme.font(Font.BOLD, 15));
        lblTotal.setForeground(Theme.GREEN);

        Runnable tinhTongTien = () -> {
            long sum = 0;
            for (ChiTietDonThuoc x : listThuoc) sum += x.getThanhTien();
            lblTotal.setText("Tổng tiền thuốc: " + Theme.money(sum));
        };

        AbstractTableModel subModel = new AbstractTableModel() {
            @Override public int getRowCount() { return listThuoc.size(); }
            @Override public int getColumnCount() { return subCols.length; }
            @Override public String getColumnName(int c) { return subCols[c]; }
            @Override public boolean isCellEditable(int r, int c) { return c != 4 && c != 6; }
            @Override public Object getValueAt(int r, int c) {
                ChiTietDonThuoc ct = listThuoc.get(r);
                return switch (c) {
                    case 0 -> ct.getTenThuoc();
                    case 1 -> ct.getDonViTinh();
                    case 2 -> ct.getSoLuong();
                    case 3 -> ct.getDonGia();
                    case 4 -> Theme.money(ct.getThanhTien());
                    case 5 -> ct.getCachDung();
                    case 6 -> "Xóa";
                    default -> "";
                };
            }
            @Override public void setValueAt(Object aValue, int r, int c) {
                ChiTietDonThuoc ct = listThuoc.get(r);
                try {
                    switch (c) {
                        case 0 -> ct.setTenThuoc(String.valueOf(aValue));
                        case 1 -> ct.setDonViTinh(String.valueOf(aValue));
                        case 2 -> {
                            int sl = Integer.parseInt(String.valueOf(aValue).trim());
                            ct.setSoLuong(Math.max(1, sl));
                        }
                        case 3 -> {
                            long dg = Long.parseLong(String.valueOf(aValue).trim().replaceAll("\\D", ""));
                            ct.setDonGia(Math.max(0, dg));
                        }
                        case 5 -> ct.setCachDung(String.valueOf(aValue));
                    }
                } catch (Exception ignored) {}
                fireTableRowsUpdated(r, r);
                tinhTongTien.run();
            }
        };

        JTable subTable = new JTable(subModel);
        subTable.setRowHeight(34);
        subTable.setFont(Theme.font(Font.PLAIN, 12));
        subTable.getColumnModel().getColumn(0).setPreferredWidth(190);
        subTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        subTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        subTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        subTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        subTable.getColumnModel().getColumn(5).setPreferredWidth(210);
        subTable.getColumnModel().getColumn(6).setPreferredWidth(50);

        subTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = subTable.rowAtPoint(e.getPoint());
                int c = subTable.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 6) {
                    listThuoc.remove(r);
                    subModel.fireTableDataChanged();
                    tinhTongTien.run();
                }
            }
        });

        tinhTongTien.run();

        // Form Layout
        JPanel pForm = new JPanel(new GridLayout(5, 2, 14, 8));
        pForm.setOpaque(false);
        pForm.add(formRow("Bệnh nhân (Thú cưng)*:", cbThuCung));
        pForm.add(formRow("Bác sĩ phụ trách*:", cbBacSi));
        pForm.add(formRow("Chủ nuôi & SĐT:", makeTwoFieldPanel(txtChu, txtSdt)));
        pForm.add(formRow("Thể trạng (Loài & Cân nặng):", makeTwoFieldPanel(txtLoaiGiong, txtCanNang)));
        pForm.add(formRow("Chẩn đoán xác định*:", txtChanDoan));
        pForm.add(formRow("Triệu chứng lâm sàng:", txtTrieuChung));
        pForm.add(formRow("Liên kết Phác đồ điều trị:", cbPhacDo));
        pForm.add(formRow("Hạn tái khám (dd/MM/yyyy):", txtTaiKham));
        pForm.add(formRow("Lời dặn bác sĩ:", txtLoiDan));
        pForm.add(new JPanel()); // dummy empty cell for grid balance

        // Nút thuốc
        RoundedButton btnAddKho = new RoundedButton("+ Chọn thuốc từ kho", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(12, 6);
        btnAddKho.addActionListener(e -> {
            List<SanPham> spThuoc = new ArrayList<>();
            for (SanPham sp : khoHangBus.layDanhSach()) {
                if ("Thuốc & Y tế".equals(sp.getDanhMuc()) || sp.getMaSKU().startsWith("TH")) spThuoc.add(sp);
            }
            if (spThuoc.isEmpty()) spThuoc = khoHangBus.layDanhSach();
            if (spThuoc.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Không tìm thấy sản phẩm thuốc nào trong kho!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] names = new String[spThuoc.size()];
            for (int i = 0; i < spThuoc.size(); i++) {
                SanPham sp = spThuoc.get(i);
                names[i] = sp.getMaSKU() + " - " + sp.getTenSP() + " (Tồn: " + sp.getSoLuongTon() + " " + sp.getDonViTinh() + " • " + Theme.money(sp.getDonGia()) + ")";
            }
            String sel = (String) JOptionPane.showInputDialog(dlg, "Chọn thuốc từ danh mục Kho hàng:", "Thêm thuốc từ kho",
                    JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (sel != null) {
                int sIdx = -1;
                for (int i = 0; i < names.length; i++) if (names[i].equals(sel)) { sIdx = i; break; }
                if (sIdx >= 0) {
                    SanPham sp = spThuoc.get(sIdx);
                    listThuoc.add(new ChiTietDonThuoc(sp.getMaSKU(), sp.getTenSP(), sp.getDonViTinh(), 1, sp.getDonGia(), "Uống 1 " + sp.getDonViTinh() + "/lần sau ăn"));
                    subModel.fireTableDataChanged();
                    tinhTongTien.run();
                }
            }
        });

        RoundedButton btnAddManual = new RoundedButton("+ Thêm dòng thuốc ngoài", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(12, 6);
        btnAddManual.addActionListener(e -> {
            listThuoc.add(new ChiTietDonThuoc("", "Tên thuốc mới", "viên", 1, 10000, "Uống theo chỉ định"));
            subModel.fireTableDataChanged();
            tinhTongTien.run();
        });

        RoundedButton btnMau = new RoundedButton("Mẫu đơn cơ bản", Color.WHITE, new Color(0xF3F6F8), Theme.MUTED, Theme.BORDER, 8).pad(12, 6);
        btnMau.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(dlg, "Bạn có muốn nạp 2 loại thuốc mẫu cơ bản (Kháng sinh & Men tiêu hóa) không?", "Nạp mẫu", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                listThuoc.add(new ChiTietDonThuoc("TH-001", "Kháng sinh Clavamox 125mg", "viên", 10, 18000, "Uống 1 viên sau ăn, ngày 2 lần"));
                listThuoc.add(new ChiTietDonThuoc("TH-004", "Men vi sinh Bio-Scour", "gói", 5, 12000, "Pha 1 gói vào nước uống mỗi sáng"));
                subModel.fireTableDataChanged();
                tinhTongTien.run();
            }
        });

        JPanel pMidHead = new JPanel(new BorderLayout());
        pMidHead.setOpaque(false);
        JLabel lblTableLabel = new JLabel("Danh sách thuốc kê đơn & Hướng dẫn sử dụng (" + listThuoc.size() + " thuốc)");
        lblTableLabel.setFont(Theme.font(Font.BOLD, 13));
        lblTableLabel.setForeground(Theme.TEXT);
        JPanel pMidBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pMidBtns.setOpaque(false);
        pMidBtns.add(btnMau);
        pMidBtns.add(btnAddManual);
        pMidBtns.add(btnAddKho);
        pMidHead.add(lblTableLabel, BorderLayout.WEST);
        pMidHead.add(pMidBtns, BorderLayout.EAST);

        JPanel pCenter = new JPanel(new BorderLayout(0, 8));
        pCenter.setOpaque(false);
        pCenter.add(pMidHead, BorderLayout.NORTH);
        pCenter.add(new JScrollPane(subTable), BorderLayout.CENTER);
        pCenter.add(lblTotal, BorderLayout.SOUTH);

        root.add(pForm, BorderLayout.NORTH);
        root.add(pCenter, BorderLayout.CENTER);

        // Buttons Footer
        JPanel pFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pFoot.setOpaque(false);

        RoundedButton btnHuy = new RoundedButton("Hủy bỏ", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(18, 9);
        btnHuy.addActionListener(e -> dlg.dispose());

        RoundedButton btnLuu = new RoundedButton(isEdit ? "Cập nhật đơn thuốc" : "Lưu & Tạo đơn thuốc", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 9);
        btnLuu.addActionListener(e -> {
            int idx = cbThuCung.getSelectedIndex();
            int tcIndex = hasPlaceholder ? idx - 1 : idx;
            if (tcIndex < 0 || tcIndex >= dsTC.size()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn thú cưng kê đơn!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ThuCung tc = dsTC.get(tcIndex);

            if (txtChanDoan.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập chẩn đoán bệnh!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                txtChanDoan.requestFocus();
                return;
            }

            if (listThuoc.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Đơn thuốc cần có ít nhất 1 loại thuốc!\nVui lòng nhấn '+ Chọn thuốc từ kho' hoặc '+ Thêm dòng thuốc ngoài'.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (ChiTietDonThuoc ct : listThuoc) {
                if (ct.getTenThuoc() == null || ct.getTenThuoc().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Tên thuốc không được để trống!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            LocalDate tk = null;
            try {
                if (!txtTaiKham.getText().trim().isEmpty()) tk = LocalDate.parse(txtTaiKham.getText().trim(), DATE_FMT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Hạn tái khám không đúng định dạng (dd/MM/yyyy)!", "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maPDChon = "";
            int pdIdx = cbPhacDo.getSelectedIndex();
            if (pdIdx > 0 && (pdIdx - 1) < listPD.size()) {
                maPDChon = listPD.get(pdIdx - 1).getMaPhacDo();
            }

            if (isEdit) {
                editTarget.setMaTC(tc.getMaTC());
                editTarget.setTenThuCung(tc.getTen());
                editTarget.setLoai(tc.getLoai());
                editTarget.setGiong(tc.getGiong());
                editTarget.setCanNang(tc.getCanNang());
                editTarget.setTenChu(tc.getTenChu());
                editTarget.setSdtChu(tc.getSdtChu());
                editTarget.setBacSi((String) cbBacSi.getSelectedItem());
                editTarget.setChanDoan(txtChanDoan.getText().trim());
                editTarget.setTrieuChung(txtTrieuChung.getText().trim());
                editTarget.setLoiDan(txtLoiDan.getText().trim());
                editTarget.setNgayTaiKham(tk);
                editTarget.setMaPhacDo(maPDChon);
                editTarget.setDanhSachThuoc(listThuoc);
                bus.capNhat(editTarget);
                JOptionPane.showMessageDialog(this, "Đã cập nhật đơn thuốc thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                DonThuoc dtMoi = new DonThuoc("", tc.getMaTC(), tc.getTen(), tc.getLoai(), tc.getGiong(),
                        tc.getCanNang(), tc.getTenChu(), tc.getSdtChu(), (String) cbBacSi.getSelectedItem(),
                        LocalDate.now(), java.time.LocalTime.now(), txtChanDoan.getText().trim(),
                        txtTrieuChung.getText().trim(), DonThuoc.CHO_CAP_THUOC, txtLoiDan.getText().trim(),
                        tk, maPDChon);
                dtMoi.setDanhSachThuoc(listThuoc);
                bus.them(dtMoi);
                JOptionPane.showMessageDialog(this, "Đã tạo đơn thuốc mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            dlg.dispose();
            refresh();
        });

        pFoot.add(btnHuy);
        pFoot.add(btnLuu);
        root.add(pFoot, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JPanel makeTwoFieldPanel(JComponent c1, JComponent c2) {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);
        p.add(c1);
        p.add(c2);
        return p;
    }

    private JPanel formRow(String lbl, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(lbl);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.TEXT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(Theme.font(Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private void inDonThuoc(DonThuoc dt) {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("DonThuoc_" + dt.getMaDonThuoc());
            if (job.printDialog()) {
                job.print();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối máy in vật lý (" + ex.getMessage() + ").\nBạn có thể sử dụng chức năng 'Xuất danh sách' để lưu file.",
                    "Thông báo in", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // ---------------- Table Model & Cell Renderers ----------------
    private static class PageModel extends AbstractTableModel {
        private final String[] cols = {"Mã đơn", "Ngày kê", "Thú cưng / Chủ", "Bác sĩ", "Chẩn đoán", "Thuốc", "Tổng tiền", "Trạng thái", "Thao tác"};
        private List<DonThuoc> rows = new ArrayList<>();

        void setRows(List<DonThuoc> r) { rows = new ArrayList<>(r); fireTableDataChanged(); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            DonThuoc dt = rows.get(r);
            return switch (c) {
                case 0 -> dt;
                case 1 -> dt.getNgayKe().format(DATE_FMT);
                case 2 -> dt;
                case 3 -> dt.getBacSi();
                case 4 -> dt.getChanDoan();
                case 5 -> dt.getSoLoaiThuoc() + " loại";
                case 6 -> Theme.money(dt.tinhTongTien());
                case 7 -> dt.getTrangThai();
                case 8 -> dt;
                default -> "";
            };
        }
    }

    private static class HeaderCell extends JLabel implements TableCellRenderer {
        HeaderCell() {
            setOpaque(true);
            setFont(Theme.font(Font.PLAIN, 12));
            setForeground(Theme.MUTED);
            setBackground(new Color(0xF8FAFC));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            setHorizontalAlignment(c == 8 || c == 5 ? SwingConstants.CENTER : (c == 6 ? SwingConstants.RIGHT : SwingConstants.LEFT));
            setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, c == 0 ? 14 : 8, 0, c == 8 ? 14 : 8)));
            return this;
        }
    }

    private abstract static class Cell extends JPanel implements TableCellRenderer {
        Cell() { super(new BorderLayout()); setOpaque(true); }
        void prep(JTable t, boolean sel, int col) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, col == 0 ? 14 : 8, 0, col == 8 ? 14 : 8)));
        }
    }

    private static class CodeCell extends Cell {
        private final JLabel lbl = new JLabel();
        CodeCell() {
            lbl.setFont(Theme.font(Font.BOLD, 13));
            lbl.setForeground(Theme.DARK);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            DonThuoc dt = (DonThuoc) v;
            lbl.setText(dt.getMaDonThuoc());
            return this;
        }
    }

    private static class DateCell extends Cell {
        private final JLabel lbl = new JLabel();
        DateCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 12));
            lbl.setForeground(Theme.TEXT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    private static class PetCell extends Cell {
        private final JLabel l1 = new JLabel();
        private final JLabel l2 = new JLabel();
        PetCell() {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            l1.setFont(Theme.font(Font.BOLD, 12));
            l1.setForeground(Theme.TEXT);
            l2.setFont(Theme.font(Font.PLAIN, 11));
            l2.setForeground(Theme.MUTED);
            p.add(l1); p.add(l2);
            add(p, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            DonThuoc dt = (DonThuoc) v;
            l1.setText(dt.getTenThuCung() + " (" + dt.getLoai() + " - " + dt.getGiong() + ")");
            l2.setText(dt.getTenChu() + " • " + dt.getSdtChu());
            return this;
        }
    }

    private static class DoctorCell extends Cell {
        private final JLabel lbl = new JLabel();
        DoctorCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 12));
            lbl.setForeground(Theme.TEXT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    private static class DiagnosisCell extends Cell {
        private final JLabel lbl = new JLabel();
        DiagnosisCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 12));
            lbl.setForeground(Theme.TEXT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            String txt = String.valueOf(v);
            lbl.setText(txt);
            setToolTipText(txt);
            return this;
        }
    }

    private static class DrugCountCell extends Cell {
        private final JLabel lbl = new JLabel();
        DrugCountCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 12));
            lbl.setForeground(Theme.MUTED);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    private static class MoneyCell extends Cell {
        private final JLabel lbl = new JLabel();
        MoneyCell() {
            lbl.setFont(Theme.font(Font.BOLD, 12));
            lbl.setForeground(Theme.GREEN);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    private static class StatusCell extends Cell {
        private final JLabel b = new JLabel();
        StatusCell() {
            b.setFont(Theme.font(Font.BOLD, 11));
            b.setOpaque(true);
            b.setHorizontalAlignment(SwingConstants.CENTER);
            b.setBorder(new EmptyBorder(3, 8, 3, 8));
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            wrap.setOpaque(false);
            wrap.add(b);
            add(wrap, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            String s = String.valueOf(v);
            b.setText(s);
            switch (s) {
                case DonThuoc.CHO_CAP_THUOC -> { b.setBackground(Theme.ORANGE_BG); b.setForeground(Theme.ORANGE); }
                case DonThuoc.DA_CAP_PHAT   -> { b.setBackground(Theme.BLUE_BG); b.setForeground(Theme.BLUE_TEXT); }
                case DonThuoc.DANG_DIEU_TRI -> { b.setBackground(new Color(0xE0F2FE)); b.setForeground(new Color(0x0369A1)); }
                case DonThuoc.HOAN_TAT      -> { b.setBackground(Theme.GREEN_BG); b.setForeground(Theme.GREEN); }
                case DonThuoc.DA_HUY        -> { b.setBackground(Theme.RED_BG); b.setForeground(Theme.RED); }
                default -> { b.setBackground(Theme.BADGE_GRAY_BG); b.setForeground(Theme.MUTED); }
            }
            return this;
        }
    }

    private static class ActionCell extends Cell {
        private final JLabel btnEye = new JLabel(Icons.of("eye", 16, Theme.ACTION_ICON));
        private final JLabel btnCheck = new JLabel(Icons.of("check", 16, Theme.GREEN));
        private final JLabel btnEdit = new JLabel(Icons.of("edit", 16, Theme.ACTION_ICON));
        private final JLabel btnPrint = new JLabel(Icons.of("print", 16, Theme.ACTION_ICON));
        private final JLabel btnDel = new JLabel(Icons.of("trash", 16, Theme.RED));

        ActionCell() {
            btnEye.setToolTipText("Xem chi tiết đơn thuốc");
            btnCheck.setToolTipText("Cấp phát thuốc");
            btnEdit.setToolTipText("Chỉnh sửa đơn thuốc");
            btnPrint.setToolTipText("In đơn thuốc");
            btnDel.setToolTipText("Xóa đơn thuốc");

            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 12));
            p.setOpaque(false);
            p.add(btnEye);
            p.add(btnCheck);
            p.add(btnEdit);
            p.add(btnPrint);
            p.add(btnDel);
            add(p, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            DonThuoc dt = (DonThuoc) v;
            boolean choCap = DonThuoc.CHO_CAP_THUOC.equals(dt.getTrangThai());
            btnCheck.setIcon(Icons.of("check", 16, choCap ? Theme.GREEN : new Color(0xCBD5E1)));
            btnCheck.setToolTipText(choCap ? "Cấp phát thuốc (Xuất kho)" : "Đã cấp phát / " + dt.getTrangThai());
            return this;
        }
    }
}
