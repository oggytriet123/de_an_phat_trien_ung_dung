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
import java.time.LocalDateTime;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.DonThuocBUS;
import Bus.PhacDoBUS;
import Bus.ThuCungBUS;
import Entity.GiaiDoanDieuTri;
import Entity.NhatKyTheoDoi;
import Entity.PhacDoDieuTri;
import Entity.ThuCung;

/**
 * Giao diện Quản lý và Tạo Phác đồ điều trị cho thú cưng.
 */
public class PhacDoPanel extends JPanel {
    private static final int PAGE_SIZE = 7;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhacDoBUS bus;
    private final ThuCungBUS thuCungBus;
    private final DonThuocBUS donThuocBus;

    private final String[] FILTERS = {"Tất cả", PhacDoDieuTri.DANG_DIEU_TRI, PhacDoDieuTri.THEO_DOI_TICH_CUC, PhacDoDieuTri.HOAN_THANH, PhacDoDieuTri.TAM_NGUNG};
    private final String[] SEVERITIES = {"Tất cả mức độ", PhacDoDieuTri.MUC_DO_NHE, PhacDoDieuTri.MUC_DO_TRUNG_BINH, PhacDoDieuTri.MUC_DO_NANG, PhacDoDieuTri.MUC_DO_NGUY_KICH};

    private String boLocHienTai = "Tất cả";
    private String mucDoHienTai = "Tất cả mức độ";
    private int trang = 1;
    private List<PhacDoDieuTri> filtered = new ArrayList<>();

    // Thẻ KPI
    private JLabel lblStatTong;
    private JLabel lblStatDangTri;
    private JLabel lblStatTheoDoi;
    private JLabel lblStatTiLe;

    private final PlaceholderField txtTim = new PlaceholderField("Tìm mã phác đồ, tên bệnh, thú cưng, chủ nuôi, bác sĩ...");
    private final List<RoundedButton> chips = new ArrayList<>();
    private final JComboBox<String> cbMucDoFilter = new JComboBox<>(SEVERITIES);
    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public PhacDoPanel() {
        this(new PhacDoBUS(), new ThuCungBUS(), new DonThuocBUS());
    }

    public PhacDoPanel(PhacDoBUS bus, ThuCungBUS thuCungBus, DonThuocBUS donThuocBus) {
        super(new BorderLayout(0, 16));
        this.bus = bus;
        this.thuCungBus = thuCungBus;
        this.donThuocBus = donThuocBus;

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
        JLabel l3 = new JLabel("Phác đồ điều trị");
        l3.setFont(Theme.font(Font.BOLD, 12));
        l3.setForeground(Theme.TEXT);
        p.add(l1); p.add(s1); p.add(l2); p.add(s2); p.add(l3);
        return p;
    }

    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JLabel t = new JLabel("Phác Đồ Điều Trị");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Thiết lập liệu trình chuyên sâu, quản lý các giai đoạn điều trị và theo dõi tiến trình hồi phục của thú cưng");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(4));
        left.add(s);

        RoundedButton btnExcel = new RoundedButton("Xuất báo cáo", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(16, 10);
        RoundedButton btnThem = new RoundedButton("+ Lập phác đồ mới", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(18, 10);
        btnExcel.addActionListener(e -> xuatBaoCao());
        btnThem.addActionListener(e -> moDialogLapPhacDo(null, null));

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

        lblStatTong = new JLabel(String.valueOf(bus.getTongSoPhacDo()));
        lblStatDangTri = new JLabel(String.valueOf(bus.getSoDangDieuTri()));
        lblStatTheoDoi = new JLabel(String.valueOf(bus.getSoTheoDoi()));
        lblStatTiLe = new JLabel(bus.getTiLeHoanThanh() + "%");

        row.add(buildStatCard("TỔNG PHÁC ĐỒ", lblStatTong, "protocol", Theme.ACCENT, Theme.BLUE_BG));
        row.add(buildStatCard("ĐANG ĐIỀU TRỊ", lblStatDangTri, "sync", Theme.ORANGE, Theme.ORANGE_BG));
        row.add(buildStatCard("THEO DÕI TÍCH CỰC", lblStatTheoDoi, "medical", Theme.BLUE_TEXT, Theme.BLUE_BG));
        row.add(buildStatCard("TỶ LỆ HOÀN THÀNH", lblStatTiLe, "check", Theme.GREEN, Theme.GREEN_BG));
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
        searchBox.setPreferredSize(new Dimension(280, 36));
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

        // Dropdown mức độ
        cbMucDoFilter.setFont(Theme.font(Font.PLAIN, 12));
        cbMucDoFilter.setPreferredSize(new Dimension(140, 34));
        cbMucDoFilter.addActionListener(e -> {
            mucDoHienTai = (String) cbMucDoFilter.getSelectedItem();
            trang = 1;
            refresh();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(cbMucDoFilter);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JComponent buildTable() {
        table.setRowHeight(50);
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

        int[] widths = {80, 210, 150, 130, 95, 145, 125, 115, 140};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getColumnModel().getColumn(0).setCellRenderer(new CodeCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new ProtocolNameCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new PetCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new DoctorCell());
        table.getColumnModel().getColumn(4).setCellRenderer(new SeverityCell());
        table.getColumnModel().getColumn(5).setCellRenderer(new PeriodCell());
        table.getColumnModel().getColumn(6).setCellRenderer(new ProgressCell());
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCell());
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionCell());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 8) {
                    PhacDoDieuTri pd = (PhacDoDieuTri) model.getValueAt(row, 0);
                    int xInCell = e.getX() - table.getCellRect(row, col, false).x;
                    int slot = Math.min(4, Math.max(0, xInCell / 28));
                    switch (slot) {
                        case 0 -> xemChiTiet(pd);
                        case 1 -> moDialogLapPhacDo(pd, null);
                        case 2 -> moDialogTheoDoiTienDo(pd);
                        case 3 -> inPhacDo(pd);
                        case 4 -> xoaPhacDo(pd);
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
        List<PhacDoDieuTri> list = new ArrayList<>();
        for (PhacDoDieuTri pd : bus.layDanhSach()) {
            if (!"Tất cả".equals(boLocHienTai) && !boLocHienTai.equals(pd.getTrangThai())) continue;
            if (!"Tất cả mức độ".equals(mucDoHienTai) && !mucDoHienTai.equals(pd.getMucDo())) continue;

            if (!kw.isEmpty()) {
                String hay = norm(pd.getMaPhacDo()) + "|" + norm(pd.getTenPhacDo()) + "|" +
                             norm(pd.getTenThuCung()) + "|" + norm(pd.getTenChu()) + "|" +
                             norm(pd.getBacSi()) + "|" + norm(pd.getChanDoan());
                if (!hay.contains(kw)) continue;
            }
            list.add(pd);
        }
        filtered = list;

        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        lblFooter.setText(filtered.isEmpty()
                ? "Không tìm thấy phác đồ điều trị nào phù hợp"
                : "Hiển thị " + (from + 1) + "–" + to + " trong tổng số " + filtered.size() + " phác đồ");

        for (RoundedButton c : chips) {
            boolean on = c.getText().equals(boLocHienTai);
            if (on) c.style(Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null);
            else c.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

        if (lblStatTong != null) lblStatTong.setText(String.valueOf(bus.getTongSoPhacDo()));
        if (lblStatDangTri != null) lblStatDangTri.setText(String.valueOf(bus.getSoDangDieuTri()));
        if (lblStatTheoDoi != null) lblStatTheoDoi.setText(String.valueOf(bus.getSoTheoDoi()));
        if (lblStatTiLe != null) lblStatTiLe.setText(bus.getTiLeHoanThanh() + "%");

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
    public void taoPhacDoChoThuCung(ThuCung tc) {
        moDialogLapPhacDo(null, tc);
    }

    private void xoaPhacDo(PhacDoDieuTri pd) {
        int r = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa phác đồ điều trị " + pd.getMaPhacDo() + " (" + pd.getTenPhacDo() + ") không?",
                "Xác nhận xóa phác đồ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            bus.xoa(pd.getMaPhacDo());
        }
    }

    private void xuatBaoCao() {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File("BaoCao_PhacDoDieuTri_" + LocalDate.now() + ".csv"));
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = ch.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
                pw.write('\ufeff'); // UTF-8 BOM
                pw.println("Mã phác đồ,Tên phác đồ,Bệnh nhân,Loài,Chủ nuôi,Bác sĩ,Mức độ,Bắt đầu,Kết thúc,Tiến độ (%),Trạng thái,Chẩn đoán");
                for (PhacDoDieuTri d : filtered) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"\n",
                            d.getMaPhacDo(), d.getTenPhacDo().replace("\"", "\"\""), d.getTenThuCung(), d.getLoai(),
                            d.getTenChu(), d.getBacSi(), d.getMucDo(), d.getNgayBatDau().format(DATE_FMT),
                            d.getNgayKetThuc().format(DATE_FMT), d.getTienDo(), d.getTrangThai(), d.getChanDoan().replace("\"", "\"\""));
                }
                JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " phác đồ điều trị thành công:\n" + f.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------- Dialog Xem chi tiết phác đồ ----------------
    private void xemChiTiet(PhacDoDieuTri pd) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Chi Tiết Phác Đồ Điều Trị - " + pd.getMaPhacDo(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(760, 750);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 18, 24));

        // Header
        JPanel pHead = new JPanel(new BorderLayout());
        pHead.setOpaque(false);
        pHead.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, Theme.ACCENT), new EmptyBorder(0, 0, 10, 0)));

        JLabel lblTitle = new JLabel(pd.getTenPhacDo());
        lblTitle.setFont(Theme.font(Font.BOLD, 17));
        lblTitle.setForeground(Theme.DARK);

        JLabel lblMeta = new JLabel("Mã: " + pd.getMaPhacDo() + " • Mức độ: " + pd.getMucDo() + " • Trạng thái: " + pd.getTrangThai());
        lblMeta.setFont(Theme.font(Font.PLAIN, 12));
        lblMeta.setForeground(Theme.MUTED);

        JPanel pLeftHead = new JPanel();
        pLeftHead.setOpaque(false);
        pLeftHead.setLayout(new BoxLayout(pLeftHead, BoxLayout.Y_AXIS));
        pLeftHead.add(lblTitle);
        pLeftHead.add(Box.createVerticalStrut(3));
        pLeftHead.add(lblMeta);

        JLabel lblTienDo = new JLabel(pd.getTienDo() + "% Hoàn thành");
        lblTienDo.setFont(Theme.font(Font.BOLD, 16));
        lblTienDo.setForeground(pd.getTienDo() == 100 ? Theme.GREEN : Theme.ACCENT);

        pHead.add(pLeftHead, BorderLayout.WEST);
        pHead.add(lblTienDo, BorderLayout.EAST);

        // Body with ScrollPane
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Info Block
        JPanel pInfo = new JPanel(new GridLayout(2, 2, 16, 6));
        pInfo.setOpaque(false);
        pInfo.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE), new EmptyBorder(8, 0, 10, 0)));
        pInfo.add(makeInfoLabel("Bệnh nhân (Thú cưng):", pd.getTenThuCung() + " (" + pd.getLoai() + " - " + pd.getGiong() + " • " + pd.getCanNang() + "kg)"));
        pInfo.add(makeInfoLabel("Chủ nuôi:", pd.getTenChu() + " - " + pd.getSdtChu()));
        pInfo.add(makeInfoLabel("Bác sĩ phụ trách:", pd.getBacSi()));
        pInfo.add(makeInfoLabel("Thời gian điều trị:", pd.getNgayBatDau().format(DATE_FMT) + " ➔ " + pd.getNgayKetThuc().format(DATE_FMT)));
        body.add(pInfo);

        // Chẩn đoán & Mục tiêu
        JPanel pGoals = new JPanel(new GridLayout(2, 1, 0, 4));
        pGoals.setOpaque(false);
        pGoals.setBorder(new EmptyBorder(8, 0, 10, 0));
        pGoals.add(makeInfoLabel("Chẩn đoán bệnh:", pd.getChanDoan()));
        pGoals.add(makeInfoLabel("Mục tiêu điều trị:", pd.getMucTieuDieuTri()));
        body.add(pGoals);

        if (!pd.getCheDoDinhDuong().isEmpty()) {
            JPanel pCare = new JPanel(new BorderLayout(4, 0));
            pCare.setOpaque(false);
            pCare.add(makeInfoLabel("Chế độ chăm sóc & Dinh dưỡng:", pd.getCheDoDinhDuong()));
            body.add(pCare);
        }

        // Timeline Các giai đoạn điều trị
        JLabel lblGiaiDoan = new JLabel("CÁC GIAI ĐOẠN ĐIỀU TRỊ TRONG LIỆU TRÌNH");
        lblGiaiDoan.setFont(Theme.font(Font.BOLD, 13));
        lblGiaiDoan.setForeground(Theme.ACCENT);
        lblGiaiDoan.setBorder(new EmptyBorder(12, 0, 6, 0));
        body.add(lblGiaiDoan);

        JPanel pPhases = new JPanel();
        pPhases.setOpaque(false);
        pPhases.setLayout(new BoxLayout(pPhases, BoxLayout.Y_AXIS));

        for (int i = 0; i < pd.getCacGiaiDoan().size(); i++) {
            GiaiDoanDieuTri gd = pd.getCacGiaiDoan().get(i);
            RoundedPanel pBox = new RoundedPanel(10, new Color(0xFAFBFD), Theme.BORDER);
            pBox.setLayout(new BorderLayout(8, 4));
            pBox.setBorder(new EmptyBorder(8, 12, 8, 12));

            JLabel gdTitle = new JLabel(gd.getTenGiaiDoan() + " (" + gd.getThoiGian() + ")");
            gdTitle.setFont(Theme.font(Font.BOLD, 12));
            gdTitle.setForeground(Theme.DARK);

            JLabel gdStatus = new JLabel(gd.getTrangThai());
            gdStatus.setFont(Theme.font(Font.BOLD, 11));
            if (GiaiDoanDieuTri.DA_HOAN_THANH.equals(gd.getTrangThai())) {
                gdStatus.setForeground(Theme.GREEN);
            } else if (GiaiDoanDieuTri.DANG_TIEN_HANH.equals(gd.getTrangThai())) {
                gdStatus.setForeground(Theme.ORANGE);
            } else {
                gdStatus.setForeground(Theme.MUTED);
            }

            JLabel gdChiDinh = new JLabel("<html><b>Chỉ định & Thuốc:</b> " + gd.getChiDinh() + "<br><b>Mục tiêu giai đoạn:</b> " + gd.getMucTieu() + "</html>");
            gdChiDinh.setFont(Theme.font(Font.PLAIN, 12));
            gdChiDinh.setForeground(Theme.TEXT);

            pBox.add(gdTitle, BorderLayout.WEST);
            pBox.add(gdStatus, BorderLayout.EAST);
            pBox.add(gdChiDinh, BorderLayout.SOUTH);

            pPhases.add(pBox);
            pPhases.add(Box.createVerticalStrut(6));
        }
        body.add(pPhases);

        // Nhật ký theo dõi diễn tiến
        JLabel lblNk = new JLabel("NHẬT KÝ THEO DÕI DIỄN TIẾN BỆNH ÁN");
        lblNk.setFont(Theme.font(Font.BOLD, 13));
        lblNk.setForeground(Theme.ACCENT);
        lblNk.setBorder(new EmptyBorder(12, 0, 6, 0));
        body.add(lblNk);

        String[] nkCols = {"Thời gian", "Bác sĩ", "Cân nặng", "Nhiệt độ", "Đánh giá tiến triển", "Ghi chú"};
        Object[][] nkData = new Object[pd.getNhatKy().size()][6];
        for (int i = 0; i < pd.getNhatKy().size(); i++) {
            NhatKyTheoDoi nk = pd.getNhatKy().get(i);
            nkData[i][0] = nk.getThoiGianStr();
            nkData[i][1] = nk.getBacSi();
            nkData[i][2] = nk.getCanNang() + "kg";
            nkData[i][3] = nk.getThanNhiet() + "°C";
            nkData[i][4] = nk.getTinhTrang();
            nkData[i][5] = nk.getGhiChu();
        }
        JTable tblNk = new JTable(nkData, nkCols);
        tblNk.setRowHeight(32);
        tblNk.setFont(Theme.font(Font.PLAIN, 12));
        tblNk.getTableHeader().setFont(Theme.font(Font.BOLD, 12));
        tblNk.getTableHeader().setBackground(new Color(0xF4F7F9));
        JScrollPane spNk = new JScrollPane(tblNk);
        spNk.setPreferredSize(new Dimension(680, 130));
        body.add(spNk);

        List<Entity.DonThuoc> dsDonLienKet = new ArrayList<>();
        if (donThuocBus != null) {
            for (Entity.DonThuoc d : donThuocBus.layDanhSach()) {
                if (pd.getMaPhacDo().equalsIgnoreCase(d.getMaPhacDo()) ||
                   (pd.getMaTC() != null && pd.getMaTC().equalsIgnoreCase(d.getMaTC()))) {
                    dsDonLienKet.add(d);
                }
            }
        }
        if (!dsDonLienKet.isEmpty()) {
            JLabel lblDtTitle = new JLabel("ĐƠN THUỐC ĐI KÈM TRONG LIỆU TRÌNH (" + dsDonLienKet.size() + " đơn)");
            lblDtTitle.setFont(Theme.font(Font.BOLD, 13));
            lblDtTitle.setForeground(Theme.ACCENT);
            lblDtTitle.setBorder(new EmptyBorder(12, 0, 6, 0));
            body.add(lblDtTitle);

            String[] dtCols = {"Mã đơn", "Ngày kê", "Chẩn đoán", "Số thuốc", "Tổng tiền", "Trạng thái"};
            Object[][] dtData = new Object[dsDonLienKet.size()][6];
            for (int i = 0; i < dsDonLienKet.size(); i++) {
                Entity.DonThuoc d = dsDonLienKet.get(i);
                dtData[i][0] = d.getMaDonThuoc();
                dtData[i][1] = d.getNgayKe().format(DATE_FMT);
                dtData[i][2] = d.getChanDoan();
                dtData[i][3] = d.getSoLoaiThuoc() + " loại";
                dtData[i][4] = Theme.money(d.tinhTongTien());
                dtData[i][5] = d.getTrangThai();
            }
            JTable tblDt = new JTable(dtData, dtCols);
            tblDt.setRowHeight(30);
            tblDt.setFont(Theme.font(Font.PLAIN, 12));
            tblDt.getTableHeader().setFont(Theme.font(Font.BOLD, 12));
            tblDt.getTableHeader().setBackground(new Color(0xF4F7F9));
            JScrollPane spDt = new JScrollPane(tblDt);
            spDt.setPreferredSize(new Dimension(680, Math.min(130, 32 + dsDonLienKet.size() * 30)));
            body.add(spDt);
        }

        JScrollPane mainScroll = new JScrollPane(body);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(Color.WHITE);

        root.add(pHead, BorderLayout.NORTH);
        root.add(mainScroll, BorderLayout.CENTER);

        // Footer buttons
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtns.setOpaque(false);

        RoundedButton btnPrint = new RoundedButton("In phác đồ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(16, 9);
        btnPrint.setIcon(Icons.of("print", 16, Color.WHITE));
        btnPrint.addActionListener(e -> inPhacDo(pd));

        RoundedButton btnEdit = new RoundedButton("Chỉnh sửa phác đồ", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 9);
        btnEdit.setIcon(Icons.of("edit", 16, Theme.TEXT));
        btnEdit.addActionListener(e -> {
            dlg.dispose();
            moDialogLapPhacDo(pd, null);
        });

        RoundedButton btnTheoDoi = new RoundedButton("Cập nhật tiến độ & Nhật ký", Theme.BLUE_TEXT, Theme.ACCENT, Color.WHITE, null, 8).pad(16, 9);
        btnTheoDoi.addActionListener(e -> {
            dlg.dispose();
            moDialogTheoDoiTienDo(pd);
        });

        RoundedButton btnClose = new RoundedButton("Đóng", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 9);
        btnClose.addActionListener(e -> dlg.dispose());

        pBtns.add(btnPrint);
        pBtns.add(btnEdit);
        pBtns.add(btnTheoDoi);
        pBtns.add(btnClose);
        root.add(pBtns, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ---------------- Dialog Cập nhật Tiến độ & Nhật ký theo dõi ----------------
    private void moDialogTheoDoiTienDo(PhacDoDieuTri pd) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Theo dõi diễn tiến: " + pd.getMaPhacDo() + " (" + pd.getTenThuCung() + ")", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(840, 700);
        dlg.setMinimumSize(new Dimension(800, 640));
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(0xF8FAFC));

        // 1. Header banner
        JPanel pHeader = new JPanel(new BorderLayout(16, 8));
        pHeader.setBackground(Color.WHITE);
        pHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        JPanel pHeaderLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        pHeaderLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("Theo Dõi Diễn Tiến & Nhật Ký Điều Trị");
        lblTitle.setFont(Theme.font(Font.BOLD, 17));
        lblTitle.setForeground(Theme.DARK);

        String petInfo = "Mã: " + pd.getMaPhacDo() + "  •  Bệnh nhân: " + pd.getTenThuCung() +
                " (" + pd.getLoai() + " - " + pd.getGiong() + ", " + pd.getCanNang() + "kg)  •  Chủ nuôi: " + pd.getTenChu() +
                (pd.getBacSi() != null && !pd.getBacSi().isEmpty() ? "  •  BS: " + pd.getBacSi() : "");
        JLabel lblSub = new JLabel(petInfo);
        lblSub.setFont(Theme.font(Font.PLAIN, 12));
        lblSub.setForeground(Theme.MUTED);

        pHeaderLeft.add(lblTitle);
        pHeaderLeft.add(lblSub);
        pHeader.add(pHeaderLeft, BorderLayout.CENTER);

        // Status badge
        JLabel lblBadge = new JLabel(pd.getTrangThai());
        lblBadge.setFont(Theme.font(Font.BOLD, 12));
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(6, 14, 6, 14));
        if (PhacDoDieuTri.HOAN_THANH.equals(pd.getTrangThai())) {
            lblBadge.setBackground(Theme.GREEN_BG);
            lblBadge.setForeground(Theme.GREEN);
        } else if (PhacDoDieuTri.THEO_DOI_TICH_CUC.equals(pd.getTrangThai())) {
            lblBadge.setBackground(Theme.BLUE_BG);
            lblBadge.setForeground(Theme.BLUE_TEXT);
        } else if (PhacDoDieuTri.TAM_NGUNG.equals(pd.getTrangThai())) {
            lblBadge.setBackground(Theme.BADGE_GRAY_BG);
            lblBadge.setForeground(Theme.MUTED);
        } else {
            lblBadge.setBackground(Theme.PEACH_BG);
            lblBadge.setForeground(Theme.PEACH_TEXT);
        }
        JPanel pBadgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        pBadgeWrapper.setOpaque(false);
        pBadgeWrapper.add(lblBadge);
        pHeader.add(pBadgeWrapper, BorderLayout.EAST);

        root.add(pHeader, BorderLayout.NORTH);

        // 2. Main Scrollable Container
        JPanel pBody = new JPanel();
        pBody.setLayout(new BoxLayout(pBody, BoxLayout.Y_AXIS));
        pBody.setBackground(new Color(0xF8FAFC));
        pBody.setBorder(new EmptyBorder(16, 24, 16, 24));

        // --- Card 1: Tiến độ & Trạng thái ---
        JPanel card1 = new JPanel(new BorderLayout(0, 10));
        card1.setBackground(Color.WHITE);
        card1.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(14, 18, 14, 18)));

        JLabel lblCard1 = new JLabel("1. TIẾN ĐỘ HỒI PHỤC & TRẠNG THÁI HIỆN TẠI");
        lblCard1.setFont(Theme.font(Font.BOLD, 12));
        lblCard1.setForeground(Theme.ACCENT);
        card1.add(lblCard1, BorderLayout.NORTH);

        JPanel pC1Grid = new JPanel(new GridLayout(1, 2, 24, 0));
        pC1Grid.setOpaque(false);

        // Slider box
        JPanel pSliderBox = new JPanel(new BorderLayout(0, 6));
        pSliderBox.setOpaque(false);

        JPanel pSliderHeader = new JPanel(new BorderLayout());
        pSliderHeader.setOpaque(false);
        JLabel lblSliderTitle = new JLabel("Tiến độ hồi phục:");
        lblSliderTitle.setFont(Theme.font(Font.BOLD, 12));
        lblSliderTitle.setForeground(Theme.TEXT);
        JLabel lblPct = new JLabel(pd.getTienDo() + "%");
        lblPct.setFont(Theme.font(Font.BOLD, 15));
        lblPct.setForeground(Theme.ACCENT);
        pSliderHeader.add(lblSliderTitle, BorderLayout.WEST);
        pSliderHeader.add(lblPct, BorderLayout.EAST);

        JSlider slider = new JSlider(0, 100, pd.getTienDo());
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(Color.WHITE);
        slider.setFont(Theme.font(Font.PLAIN, 11));

        pSliderBox.add(pSliderHeader, BorderLayout.NORTH);
        pSliderBox.add(slider, BorderLayout.CENTER);
        pC1Grid.add(pSliderBox);

        // Status box
        JPanel pStatusBox = new JPanel(new BorderLayout(0, 6));
        pStatusBox.setOpaque(false);

        JLabel lblStatusTitle = new JLabel("Trạng thái hiện tại:");
        lblStatusTitle.setFont(Theme.font(Font.BOLD, 12));
        lblStatusTitle.setForeground(Theme.TEXT);

        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{
                PhacDoDieuTri.DANG_DIEU_TRI, PhacDoDieuTri.THEO_DOI_TICH_CUC,
                PhacDoDieuTri.HOAN_THANH, PhacDoDieuTri.TAM_NGUNG
        });
        cbTrangThai.setSelectedItem(pd.getTrangThai());
        cbTrangThai.setPreferredSize(new Dimension(cbTrangThai.getPreferredSize().width, 34));

        JLabel lblTipStatus = new JLabel("💡 Khi tiến độ kéo đạt 100%, hệ thống tự động hoàn thành phác đồ.");
        lblTipStatus.setFont(Theme.font(Font.ITALIC, 11));
        lblTipStatus.setForeground(Theme.MUTED);

        pStatusBox.add(lblStatusTitle, BorderLayout.NORTH);
        pStatusBox.add(cbTrangThai, BorderLayout.CENTER);
        pStatusBox.add(lblTipStatus, BorderLayout.SOUTH);
        pC1Grid.add(pStatusBox);

        slider.addChangeListener(e -> {
            int val = slider.getValue();
            lblPct.setText(val + "%");
            if (val == 100) {
                cbTrangThai.setSelectedItem(PhacDoDieuTri.HOAN_THANH);
            } else if (val < 100 && PhacDoDieuTri.HOAN_THANH.equals(cbTrangThai.getSelectedItem())) {
                cbTrangThai.setSelectedItem(PhacDoDieuTri.DANG_DIEU_TRI);
            }
        });

        card1.add(pC1Grid, BorderLayout.CENTER);
        pBody.add(card1);
        pBody.add(Box.createVerticalStrut(14));

        // --- Card 2: Ghi nhận lâm sàng lần này ---
        JPanel card2 = new JPanel(new BorderLayout(0, 10));
        card2.setBackground(Color.WHITE);
        card2.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(14, 18, 14, 18)));

        JLabel lblCard2 = new JLabel("2. GHI NHẬN LÂM SÀNG LẦN NÀY");
        lblCard2.setFont(Theme.font(Font.BOLD, 12));
        lblCard2.setForeground(Theme.ACCENT);
        card2.add(lblCard2, BorderLayout.NORTH);

        JPanel pFormNew = new JPanel();
        pFormNew.setLayout(new BoxLayout(pFormNew, BoxLayout.Y_AXIS));
        pFormNew.setOpaque(false);

        // Row 1: Thân nhiệt, Cân nặng, Đánh giá (3 columns)
        JPanel pRowVitals = new JPanel(new GridLayout(1, 3, 16, 0));
        pRowVitals.setOpaque(false);

        JTextField txtNhietDo = field(); txtNhietDo.setText("38.5");
        JTextField txtCanNang = field(); txtCanNang.setText(String.valueOf(pd.getCanNang()));
        JComboBox<String> cbDanhGia = new JComboBox<>(new String[]{
                "Tiến triển tốt", "Ổn định", "Có sốt nhẹ", "Cần theo dõi sát", "Hồi phục tích cực", "Hồi phục hoàn toàn"
        });
        cbDanhGia.setPreferredSize(new Dimension(cbDanhGia.getPreferredSize().width, 32));

        pRowVitals.add(formRow("Thân nhiệt (°C):", txtNhietDo));
        pRowVitals.add(formRow("Cân nặng đo được (kg):", txtCanNang));
        pRowVitals.add(formRow("Đánh giá tiến triển:", cbDanhGia));

        pFormNew.add(pRowVitals);
        pFormNew.add(Box.createVerticalStrut(10));

        // Row 2: Chi tiết ghi nhận
        JTextField txtGhiChu = field();
        txtGhiChu.setText("Bé ăn uống tốt, vết thương se khô...");
        txtGhiChu.setPreferredSize(new Dimension(txtGhiChu.getPreferredSize().width, 34));
        pFormNew.add(formRow("Chi tiết ghi nhận / Diễn biến triệu chứng:", txtGhiChu));

        card2.add(pFormNew, BorderLayout.CENTER);
        pBody.add(card2);
        pBody.add(Box.createVerticalStrut(14));

        // --- Card 3: Lịch sử nhật ký điều trị (JTable) ---
        JPanel card3 = new JPanel(new BorderLayout(0, 10));
        card3.setBackground(Color.WHITE);
        card3.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(14, 18, 14, 18)));

        JPanel pC3Header = new JPanel(new BorderLayout());
        pC3Header.setOpaque(false);
        JLabel lblCard3 = new JLabel("3. LỊCH SỬ NHẬT KÝ THEO DÕI ĐIỀU TRỊ");
        lblCard3.setFont(Theme.font(Font.BOLD, 12));
        lblCard3.setForeground(Theme.ACCENT);

        List<NhatKyTheoDoi> dsNK = pd.getNhatKy();
        JLabel lblCount = new JLabel(dsNK.size() + " lần ghi nhận");
        lblCount.setFont(Theme.font(Font.BOLD, 11));
        lblCount.setForeground(Theme.MUTED);

        pC3Header.add(lblCard3, BorderLayout.WEST);
        pC3Header.add(lblCount, BorderLayout.EAST);
        card3.add(pC3Header, BorderLayout.NORTH);

        String[] cols = {"Thời gian", "Bác sĩ", "Thân nhiệt", "Cân nặng", "Đánh giá", "Chi tiết ghi nhận"};
        DefaultTableModel diaryModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int i = dsNK.size() - 1; i >= 0; i--) {
            NhatKyTheoDoi nk = dsNK.get(i);
            diaryModel.addRow(new Object[]{
                    nk.getThoiGianStr(),
                    nk.getBacSi() == null || nk.getBacSi().isEmpty() ? pd.getBacSi() : nk.getBacSi(),
                    nk.getThanNhiet() > 0 ? nk.getThanNhiet() + " °C" : "--",
                    nk.getCanNang() > 0 ? nk.getCanNang() + " kg" : "--",
                    nk.getTinhTrang(),
                    nk.getGhiChu()
            });
        }

        JTable tableDiary = new JTable(diaryModel);
        tableDiary.setFont(Theme.font(Font.PLAIN, 12));
        tableDiary.setRowHeight(28);
        tableDiary.setShowGrid(false);
        tableDiary.setIntercellSpacing(new Dimension(0, 1));
        tableDiary.setSelectionBackground(new Color(0xE8F0FE));
        tableDiary.setSelectionForeground(Theme.DARK);

        DefaultTableCellRenderer leftRender = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!s) {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xFAFBFD));
                }
                return this;
            }
        };
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 4, 0, 4));
                if (!s) {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xFAFBFD));
                }
                return this;
            }
        };

        for (int i = 0; i < cols.length; i++) {
            if (i == 0 || i == 2 || i == 3 || i == 4) {
                tableDiary.getColumnModel().getColumn(i).setCellRenderer(centerRender);
            } else {
                tableDiary.getColumnModel().getColumn(i).setCellRenderer(leftRender);
            }
        }

        tableDiary.getColumnModel().getColumn(0).setPreferredWidth(125);
        tableDiary.getColumnModel().getColumn(1).setPreferredWidth(110);
        tableDiary.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableDiary.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableDiary.getColumnModel().getColumn(4).setPreferredWidth(120);
        tableDiary.getColumnModel().getColumn(5).setPreferredWidth(230);

        JTableHeader th = tableDiary.getTableHeader();
        th.setFont(Theme.font(Font.BOLD, 12));
        th.setForeground(Theme.TEXT);
        th.setBackground(new Color(0xF1F5F9));
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 30));
        th.setBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE));

        JScrollPane spDiary = new JScrollPane(tableDiary);
        spDiary.setBorder(BorderFactory.createLineBorder(Theme.ROW_LINE, 1));
        spDiary.setPreferredSize(new Dimension(560, 150));
        spDiary.getViewport().setBackground(Color.WHITE);

        if (dsNK.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có lịch sử theo dõi nào. Ghi chép lần này sẽ được lưu thành nhật ký đầu tiên.", SwingConstants.CENTER);
            lblEmpty.setFont(Theme.font(Font.ITALIC, 12));
            lblEmpty.setForeground(Theme.MUTED);
            lblEmpty.setPreferredSize(new Dimension(560, 70));
            card3.add(lblEmpty, BorderLayout.CENTER);
        } else {
            card3.add(spDiary, BorderLayout.CENTER);
        }

        pBody.add(card3);

        JScrollPane scrollBody = new JScrollPane(pBody);
        scrollBody.setBorder(BorderFactory.createEmptyBorder());
        scrollBody.setOpaque(false);
        scrollBody.getViewport().setOpaque(false);
        scrollBody.getVerticalScrollBar().setUnitIncrement(14);
        root.add(scrollBody, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel pFooter = new JPanel(new BorderLayout());
        pFooter.setBackground(Color.WHITE);
        pFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(12, 24, 12, 24)));

        JLabel lblFooterTip = new JLabel("💡 Thay đổi thanh tiến độ và bấm \"Lưu cập nhật\" để ghi nhận vào phác đồ.");
        lblFooterTip.setFont(Theme.font(Font.ITALIC, 12));
        lblFooterTip.setForeground(Theme.MUTED);
        pFooter.add(lblFooterTip, BorderLayout.WEST);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtns.setOpaque(false);

        RoundedButton btnHuy = new RoundedButton("Đóng", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 9);
        btnHuy.addActionListener(e -> dlg.dispose());

        RoundedButton btnLuu = new RoundedButton("Lưu cập nhật", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(20, 9);
        btnLuu.addActionListener(e -> {
            int newProgress = slider.getValue();
            String newStatus = (String) cbTrangThai.getSelectedItem();

            try {
                String tStr = txtNhietDo.getText().trim();
                String wStr = txtCanNang.getText().trim();
                double t = tStr.isEmpty() ? 38.5 : Double.parseDouble(tStr);
                double w = wStr.isEmpty() ? pd.getCanNang() : Double.parseDouble(wStr);
                String dg = (String) cbDanhGia.getSelectedItem();
                String gc = txtGhiChu.getText().trim();
                if (!gc.isEmpty()) {
                    NhatKyTheoDoi nk = new NhatKyTheoDoi(LocalDateTime.now(), pd.getBacSi(), w, t, dg, gc);
                    bus.themNhatKy(pd.getMaPhacDo(), nk);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Thân nhiệt và Cân nặng phải là số hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            } catch (Exception ignored) {}

            bus.capNhatTienDo(pd.getMaPhacDo(), newProgress, newStatus);
            dlg.dispose();
            refresh();
            JOptionPane.showMessageDialog(this, "Đã cập nhật tiến độ điều trị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        pBtns.add(btnHuy);
        pBtns.add(btnLuu);
        pFooter.add(pBtns, BorderLayout.EAST);

        root.add(pFooter, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ---------------- Dialog Lập / Chỉnh sửa Phác đồ điều trị ----------------
    private void moDialogLapPhacDo(PhacDoDieuTri editTarget, ThuCung preSelectedTC) {
        boolean isEdit = (editTarget != null && editTarget.getMaPhacDo() != null && !editTarget.getMaPhacDo().isEmpty());
        Window owner = SwingUtilities.getWindowAncestor(this);

        String title;
        if (isEdit) {
            title = "Chỉnh sửa phác đồ: " + editTarget.getMaPhacDo() + " (" + editTarget.getTenThuCung() + ")";
        } else if (preSelectedTC != null) {
            title = "Thiết lập Phác đồ Điều trị cho: " + preSelectedTC.getTen() + " (" + preSelectedTC.getMaTC() + ")";
        } else {
            title = "Thiết lập Phác đồ Điều trị mới";
        }

        JDialog dlg = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(880, 740);
        dlg.setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 22, 16, 22));

        // Form Controls - Thú cưng
        List<ThuCung> dsTC = thuCungBus.layDanhSach();
        JComboBox<String> cbThuCung = new JComboBox<>();

        boolean hasPlaceholder = (!isEdit && preSelectedTC == null);
        if (hasPlaceholder) {
            cbThuCung.addItem("-- Chọn thú cưng điều trị --");
        }
        for (ThuCung tc : dsTC) {
            cbThuCung.addItem(tc.getMaTC() + " - " + tc.getTen() + " (" + tc.getLoai() + " - " + tc.getGiong() + ") - Chủ: " + tc.getTenChu());
        }

        JTextField txtChu = field(); txtChu.setEditable(false);
        JTextField txtSdt = field(); txtSdt.setEditable(false);
        JTextField txtCanNang = field(); txtCanNang.setEditable(false);
        JTextField txtLoaiGiong = field(); txtLoaiGiong.setEditable(false);

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
        };
        cbThuCung.addActionListener(e -> syncPetInfo.run());

        // Chọn thú cưng mục tiêu
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

        // Tên phác đồ
        PlaceholderField txtTenPhacDo = new PlaceholderField("Ví dụ: Phác đồ điều trị Viêm da dị ứng tiếp xúc...");
        if (isEdit) txtTenPhacDo.setText(editTarget.getTenPhacDo());

        // Bác sĩ phụ trách
        JComboBox<String> cbBacSi = new JComboBox<>(new String[]{
                "BS. Nguyễn Thanh Tùng", "BS. Lê Mai Anh", "BS. Phạm Quốc Việt"
        });
        if (isEdit && editTarget.getBacSi() != null) cbBacSi.setSelectedItem(editTarget.getBacSi());

        // Mức độ bệnh
        JComboBox<String> cbMucDo = new JComboBox<>(new String[]{
                PhacDoDieuTri.MUC_DO_NHE, PhacDoDieuTri.MUC_DO_TRUNG_BINH,
                PhacDoDieuTri.MUC_DO_NANG, PhacDoDieuTri.MUC_DO_NGUY_KICH
        });
        if (isEdit) cbMucDo.setSelectedItem(editTarget.getMucDo());
        else cbMucDo.setSelectedItem(PhacDoDieuTri.MUC_DO_TRUNG_BINH);

        // Ngày bắt đầu / kết thúc
        JTextField txtNgayBD = field(); txtNgayBD.setText(LocalDate.now().format(DATE_FMT));
        JTextField txtNgayKT = field(); txtNgayKT.setText(LocalDate.now().plusDays(7).format(DATE_FMT));
        if (isEdit) {
            txtNgayBD.setText(editTarget.getNgayBatDau().format(DATE_FMT));
            txtNgayKT.setText(editTarget.getNgayKetThuc().format(DATE_FMT));
        }

        // Chẩn đoán, mục tiêu, dinh dưỡng
        PlaceholderField txtChanDoan = new PlaceholderField("Nhập chẩn đoán xác định bệnh...");
        if (isEdit) txtChanDoan.setText(editTarget.getChanDoan());

        PlaceholderField txtMucTieu = new PlaceholderField("Nhập mục tiêu điều trị cần đạt được...");
        if (isEdit) txtMucTieu.setText(editTarget.getMucTieuDieuTri());

        PlaceholderField txtDinhDuong = new PlaceholderField("Chế độ chăm sóc, khẩu phần dinh dưỡng, kiêng cữ...");
        if (isEdit) txtDinhDuong.setText(editTarget.getCheDoDinhDuong());

        // Bảng giai đoạn điều trị
        List<GiaiDoanDieuTri> listGiaiDoan = new ArrayList<>();
        if (isEdit && !editTarget.getCacGiaiDoan().isEmpty()) {
            for (GiaiDoanDieuTri g : editTarget.getCacGiaiDoan()) {
                listGiaiDoan.add(new GiaiDoanDieuTri(g.getThuTu(), g.getTenGiaiDoan(), g.getThoiGian(), g.getMucTieu(), g.getChiDinh(), g.getTrangThai()));
            }
        } else {
            // Khi thêm mới: khởi tạo 1 giai đoạn sạch để bác sĩ điền
            listGiaiDoan.add(new GiaiDoanDieuTri(1, "Giai đoạn 1: Tấn công & Kiểm soát triệu chứng", "3 ngày", "", "", GiaiDoanDieuTri.DANG_TIEN_HANH));
        }

        String[] gdCols = {"Giai đoạn", "Thời gian", "Mục tiêu", "Chỉ định y tế / Thuốc", "Trạng thái", "Xóa"};
        AbstractTableModel gdModel = new AbstractTableModel() {
            @Override public int getRowCount() { return listGiaiDoan.size(); }
            @Override public int getColumnCount() { return gdCols.length; }
            @Override public String getColumnName(int c) { return gdCols[c]; }
            @Override public boolean isCellEditable(int r, int c) { return c != 5; }
            @Override public Object getValueAt(int r, int c) {
                GiaiDoanDieuTri g = listGiaiDoan.get(r);
                return switch (c) {
                    case 0 -> g.getTenGiaiDoan();
                    case 1 -> g.getThoiGian();
                    case 2 -> g.getMucTieu();
                    case 3 -> g.getChiDinh();
                    case 4 -> g.getTrangThai();
                    case 5 -> "Xóa";
                    default -> "";
                };
            }
            @Override public void setValueAt(Object val, int r, int c) {
                GiaiDoanDieuTri g = listGiaiDoan.get(r);
                switch (c) {
                    case 0 -> g.setTenGiaiDoan(String.valueOf(val));
                    case 1 -> g.setThoiGian(String.valueOf(val));
                    case 2 -> g.setMucTieu(String.valueOf(val));
                    case 3 -> g.setChiDinh(String.valueOf(val));
                    case 4 -> g.setTrangThai(String.valueOf(val));
                }
                fireTableRowsUpdated(r, r);
            }
        };

        JTable tblGd = new JTable(gdModel);
        tblGd.setRowHeight(34);
        tblGd.setFont(Theme.font(Font.PLAIN, 12));
        tblGd.getColumnModel().getColumn(0).setPreferredWidth(170);
        tblGd.getColumnModel().getColumn(1).setPreferredWidth(65);
        tblGd.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblGd.getColumnModel().getColumn(3).setPreferredWidth(180);
        tblGd.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblGd.getColumnModel().getColumn(5).setPreferredWidth(50);

        tblGd.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = tblGd.rowAtPoint(e.getPoint());
                int c = tblGd.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 5) {
                    if (listGiaiDoan.size() > 1) {
                        listGiaiDoan.remove(r);
                        gdModel.fireTableDataChanged();
                    } else {
                        JOptionPane.showMessageDialog(dlg, "Phác đồ cần có ít nhất 1 giai đoạn điều trị!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        // Form Layout Top: Thú cưng, Chủ nuôi, Tên phác đồ, Bác sĩ, v.v.
        JPanel pForm = new JPanel(new GridLayout(4, 2, 14, 8));
        pForm.setOpaque(false);
        pForm.add(formRow("Bệnh nhân (Thú cưng)*:", cbThuCung));
        pForm.add(formRow("Tên phác đồ điều trị*:", txtTenPhacDo));
        pForm.add(formRow("Bác sĩ phụ trách chính*:", cbBacSi));
        pForm.add(formRow("Mức độ bệnh:", cbMucDo));
        pForm.add(formRow("Ngày bắt đầu (dd/MM/yyyy):", txtNgayBD));
        pForm.add(formRow("Ngày kết thúc dự kiến:", txtNgayKT));
        pForm.add(formRow("Chủ nuôi & SĐT:", makeTwoFieldPanel(txtChu, txtSdt)));
        pForm.add(formRow("Thể trạng (Loài & Cân nặng):", makeTwoFieldPanel(txtLoaiGiong, txtCanNang)));

        // Center Table Giai đoạn & Chẩn đoán
        JPanel pMidHead = new JPanel(new BorderLayout());
        pMidHead.setOpaque(false);
        JLabel lblTableLabel = new JLabel("Các giai đoạn / Bước trong liệu trình điều trị");
        lblTableLabel.setFont(Theme.font(Font.BOLD, 13));
        lblTableLabel.setForeground(Theme.TEXT);

        JPanel pMidBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pMidBtns.setOpaque(false);

        RoundedButton btnAddGd = new RoundedButton("+ Thêm giai đoạn", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(12, 6);
        btnAddGd.addActionListener(e -> {
            int step = listGiaiDoan.size() + 1;
            listGiaiDoan.add(new GiaiDoanDieuTri(step, "Giai đoạn " + step + ": Điều trị duy trì", "5 ngày", "", "", GiaiDoanDieuTri.CHUA_THUC_HIEN));
            gdModel.fireTableDataChanged();
        });

        RoundedButton btnTemplate = new RoundedButton("Mẫu 3 giai đoạn chuẩn", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(12, 6);
        btnTemplate.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(dlg,
                    "Bạn có muốn nạp khung 3 giai đoạn tiêu chuẩn (Tấn công, Duy trì, Phục hồi) không?",
                    "Nạp mẫu chuẩn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                listGiaiDoan.clear();
                listGiaiDoan.add(new GiaiDoanDieuTri(1, "Giai đoạn 1: Tấn công & Kiểm soát triệu chứng", "3 ngày", "Cắt cơn cấp, chống nhiễm trùng", "Kháng sinh + thuốc đặc trị liều khởi đầu", GiaiDoanDieuTri.DANG_TIEN_HANH));
                listGiaiDoan.add(new GiaiDoanDieuTri(2, "Giai đoạn 2: Điều trị duy trì & Ổn định", "5 ngày", "Tiêu trừ hoàn toàn mầm bệnh", "Kháng sinh đường uống + men vi sinh", GiaiDoanDieuTri.CHUA_THUC_HIEN));
                listGiaiDoan.add(new GiaiDoanDieuTri(3, "Giai đoạn 3: Phục hồi chức năng & Dưỡng chất", "3 ngày", "Tái tạo thể trạng, kích thích đề kháng", "Dinh dưỡng cao cấp + khoáng vi lượng", GiaiDoanDieuTri.CHUA_THUC_HIEN));
                gdModel.fireTableDataChanged();
            }
        });

        pMidBtns.add(btnAddGd);
        pMidBtns.add(btnTemplate);
        pMidHead.add(lblTableLabel, BorderLayout.WEST);
        pMidHead.add(pMidBtns, BorderLayout.EAST);

        JPanel pGoals = new JPanel(new GridLayout(2, 1, 0, 6));
        pGoals.setOpaque(false);
        pGoals.add(formRow("Chẩn đoán xác định*:", txtChanDoan));
        pGoals.add(formRow("Mục tiêu điều trị cốt lõi:", txtMucTieu));

        JPanel pCenter = new JPanel(new BorderLayout(0, 8));
        pCenter.setOpaque(false);
        pCenter.add(pGoals, BorderLayout.NORTH);
        pCenter.add(new JScrollPane(tblGd), BorderLayout.CENTER);
        pCenter.add(formRow("Chế độ dinh dưỡng & Chăm sóc đặc biệt:", txtDinhDuong), BorderLayout.SOUTH);

        JPanel pMiddleWrapper = new JPanel(new BorderLayout(0, 8));
        pMiddleWrapper.setOpaque(false);
        pMiddleWrapper.add(pMidHead, BorderLayout.NORTH);
        pMiddleWrapper.add(pCenter, BorderLayout.CENTER);

        root.add(pForm, BorderLayout.NORTH);
        root.add(pMiddleWrapper, BorderLayout.CENTER);

        // Footer buttons
        JPanel pFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pFoot.setOpaque(false);

        RoundedButton btnHuy = new RoundedButton("Hủy bỏ", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(18, 9);
        btnHuy.addActionListener(e -> dlg.dispose());

        RoundedButton btnLuu = new RoundedButton(isEdit ? "Cập nhật phác đồ" : "Lưu & Tạo phác đồ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 9);
        btnLuu.addActionListener(e -> {
            int idx = cbThuCung.getSelectedIndex();
            int tcIndex = hasPlaceholder ? idx - 1 : idx;
            if (tcIndex < 0 || tcIndex >= dsTC.size()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn thú cưng điều trị!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ThuCung tc = dsTC.get(tcIndex);

            if (txtTenPhacDo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên phác đồ điều trị!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                txtTenPhacDo.requestFocus();
                return;
            }
            if (txtChanDoan.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập chẩn đoán bệnh!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                txtChanDoan.requestFocus();
                return;
            }
            if (listGiaiDoan.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Phác đồ điều trị cần có ít nhất 1 giai đoạn!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate bd = LocalDate.now();
            LocalDate kt = LocalDate.now().plusDays(7);
            try {
                if (!txtNgayBD.getText().trim().isEmpty()) bd = LocalDate.parse(txtNgayBD.getText().trim(), DATE_FMT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Ngày bắt đầu không đúng định dạng (dd/MM/yyyy)!", "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (!txtNgayKT.getText().trim().isEmpty()) kt = LocalDate.parse(txtNgayKT.getText().trim(), DATE_FMT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Ngày kết thúc không đúng định dạng (dd/MM/yyyy)!", "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (kt.isBefore(bd)) {
                JOptionPane.showMessageDialog(dlg, "Ngày kết thúc không được trước ngày bắt đầu!", "Lỗi logic", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                editTarget.setTenPhacDo(txtTenPhacDo.getText().trim());
                editTarget.setBacSi((String) cbBacSi.getSelectedItem());
                editTarget.setMucDo((String) cbMucDo.getSelectedItem());
                editTarget.setNgayBatDau(bd);
                editTarget.setNgayKetThuc(kt);
                editTarget.setChanDoan(txtChanDoan.getText().trim());
                editTarget.setMucTieuDieuTri(txtMucTieu.getText().trim());
                editTarget.setCheDoDinhDuong(txtDinhDuong.getText().trim());
                editTarget.setCacGiaiDoan(listGiaiDoan);
                bus.capNhat(editTarget);
                JOptionPane.showMessageDialog(this, "Đã cập nhật phác đồ điều trị thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                PhacDoDieuTri pdMoi = new PhacDoDieuTri("", txtTenPhacDo.getText().trim(), tc.getMaTC(),
                        tc.getTen(), tc.getLoai(), tc.getGiong(), tc.getCanNang(), tc.getTenChu(),
                        tc.getSdtChu(), (String) cbBacSi.getSelectedItem(), bd, kt,
                        (String) cbMucDo.getSelectedItem(), txtChanDoan.getText().trim(),
                        txtMucTieu.getText().trim(), txtDinhDuong.getText().trim(), 0,
                        PhacDoDieuTri.DANG_DIEU_TRI, "");
                pdMoi.setCacGiaiDoan(listGiaiDoan);
                bus.them(pdMoi);
                JOptionPane.showMessageDialog(this, "Đã tạo phác đồ điều trị mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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

    private void inPhacDo(PhacDoDieuTri pd) {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("PhacDo_" + pd.getMaPhacDo());
            if (job.printDialog()) {
                job.print();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối máy in vật lý (" + ex.getMessage() + ").\nBạn có thể dùng chức năng 'Xuất báo cáo' để lưu file.",
                    "Thông báo in", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void moDialogLapPhacDo(PhacDoDieuTri editTarget) {
        moDialogLapPhacDo(editTarget, null);
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

    private static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // ---------------- Table Model & Cell Renderers ----------------
    private static class PageModel extends AbstractTableModel {
        private final String[] cols = {"Mã phác đồ", "Bệnh & Phác đồ", "Bệnh nhân (Thú cưng)", "Bác sĩ", "Mức độ", "Thời gian", "Tiến độ", "Trạng thái", "Thao tác"};
        private List<PhacDoDieuTri> rows = new ArrayList<>();

        void setRows(List<PhacDoDieuTri> r) { rows = new ArrayList<>(r); fireTableDataChanged(); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            PhacDoDieuTri pd = rows.get(r);
            return switch (c) {
                case 0 -> pd;
                case 1 -> pd;
                case 2 -> pd;
                case 3 -> pd.getBacSi();
                case 4 -> pd.getMucDo();
                case 5 -> pd.getNgayBatDau().format(DATE_FMT) + " - " + pd.getNgayKetThuc().format(DATE_FMT);
                case 6 -> pd.getTienDo();
                case 7 -> pd.getTrangThai();
                case 8 -> pd;
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
            setHorizontalAlignment(c == 8 || c == 4 || c == 6 ? SwingConstants.CENTER : SwingConstants.LEFT);
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
            PhacDoDieuTri pd = (PhacDoDieuTri) v;
            lbl.setText(pd.getMaPhacDo());
            return this;
        }
    }

    private static class ProtocolNameCell extends Cell {
        private final JLabel l1 = new JLabel();
        private final JLabel l2 = new JLabel();
        ProtocolNameCell() {
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
            PhacDoDieuTri pd = (PhacDoDieuTri) v;
            l1.setText(pd.getTenPhacDo());
            l2.setText(pd.getChanDoan());
            setToolTipText(pd.getTenPhacDo());
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
            PhacDoDieuTri pd = (PhacDoDieuTri) v;
            l1.setText(pd.getTenThuCung() + " (" + pd.getLoai() + " - " + pd.getGiong() + ")");
            l2.setText(pd.getTenChu() + " • " + pd.getSdtChu());
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

    private static class SeverityCell extends Cell {
        private final JLabel b = new JLabel();
        SeverityCell() {
            b.setFont(Theme.font(Font.BOLD, 11));
            b.setOpaque(true);
            b.setHorizontalAlignment(SwingConstants.CENTER);
            b.setBorder(new EmptyBorder(3, 8, 3, 8));
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 11));
            wrap.setOpaque(false);
            wrap.add(b);
            add(wrap, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            String s = String.valueOf(v);
            b.setText(s);
            switch (s) {
                case PhacDoDieuTri.MUC_DO_NHE -> { b.setBackground(Theme.GREEN_BG); b.setForeground(Theme.GREEN); }
                case PhacDoDieuTri.MUC_DO_TRUNG_BINH -> { b.setBackground(Theme.ORANGE_BG); b.setForeground(Theme.ORANGE); }
                case PhacDoDieuTri.MUC_DO_NANG -> { b.setBackground(Theme.RED_BG); b.setForeground(Theme.RED); }
                case PhacDoDieuTri.MUC_DO_NGUY_KICH -> { b.setBackground(new Color(0x7F1D1D)); b.setForeground(Color.WHITE); }
                default -> { b.setBackground(Theme.BADGE_GRAY_BG); b.setForeground(Theme.MUTED); }
            }
            return this;
        }
    }

    private static class PeriodCell extends Cell {
        private final JLabel lbl = new JLabel();
        PeriodCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 12));
            lbl.setForeground(Theme.MUTED);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    private static class ProgressCell extends Cell {
        private int pct = 0;
        private final JLabel lbl = new JLabel();

        ProgressCell() {
            setLayout(new BorderLayout(6, 0));
            lbl.setFont(Theme.font(Font.BOLD, 11));
            lbl.setForeground(Theme.DARK);
            lbl.setPreferredSize(new Dimension(36, 16));
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            add(lbl, BorderLayout.EAST);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            pct = (Integer) v;
            lbl.setText(pct + "%");
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int barW = getWidth() - 56;
            int barH = 8;
            int x = 8;
            int y = (getHeight() - barH) / 2;

            // Background
            g2.setColor(new Color(0xE2E8F0));
            g2.fillRoundRect(x, y, barW, barH, 4, 4);

            // Fill
            if (pct > 0) {
                int fillW = Math.max(6, (int) (barW * (pct / 100.0)));
                g2.setColor(pct == 100 ? Theme.GREEN : Theme.ACCENT);
                g2.fillRoundRect(x, y, fillW, barH, 4, 4);
            }
            g2.dispose();
        }
    }

    private static class StatusCell extends Cell {
        private final JLabel b = new JLabel();
        StatusCell() {
            b.setFont(Theme.font(Font.BOLD, 11));
            b.setOpaque(true);
            b.setHorizontalAlignment(SwingConstants.CENTER);
            b.setBorder(new EmptyBorder(3, 8, 3, 8));
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 11));
            wrap.setOpaque(false);
            wrap.add(b);
            add(wrap, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            String s = String.valueOf(v);
            b.setText(s);
            switch (s) {
                case PhacDoDieuTri.DANG_DIEU_TRI   -> { b.setBackground(Theme.ORANGE_BG); b.setForeground(Theme.ORANGE); }
                case PhacDoDieuTri.THEO_DOI_TICH_CUC -> { b.setBackground(Theme.BLUE_BG); b.setForeground(Theme.BLUE_TEXT); }
                case PhacDoDieuTri.HOAN_THANH     -> { b.setBackground(Theme.GREEN_BG); b.setForeground(Theme.GREEN); }
                case PhacDoDieuTri.TAM_NGUNG      -> { b.setBackground(Theme.BADGE_GRAY_BG); b.setForeground(Theme.MUTED); }
                case PhacDoDieuTri.DA_HUY         -> { b.setBackground(Theme.RED_BG); b.setForeground(Theme.RED); }
                default -> { b.setBackground(Theme.BADGE_GRAY_BG); b.setForeground(Theme.MUTED); }
            }
            return this;
        }
    }

    private static class ActionCell extends Cell {
        private final JLabel btnEye = new JLabel(Icons.of("eye", 16, Theme.ACTION_ICON));
        private final JLabel btnEdit = new JLabel(Icons.of("edit", 16, Theme.ACTION_ICON));
        private final JLabel btnChart = new JLabel(Icons.of("chart", 16, Theme.ACCENT));
        private final JLabel btnPrint = new JLabel(Icons.of("print", 16, Theme.ACTION_ICON));
        private final JLabel btnDel = new JLabel(Icons.of("trash", 16, Theme.RED));

        ActionCell() {
            btnEye.setToolTipText("Xem chi tiết phác đồ");
            btnEdit.setToolTipText("Chỉnh sửa phác đồ");
            btnChart.setToolTipText("Cập nhật tiến độ & Nhật ký");
            btnPrint.setToolTipText("In phác đồ");
            btnDel.setToolTipText("Xóa phác đồ");

            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 14));
            p.setOpaque(false);
            p.add(btnEye);
            p.add(btnEdit);
            p.add(btnChart);
            p.add(btnPrint);
            p.add(btnDel);
            add(p, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            prep(t, sel, c);
            return this;
        }
    }
}
