package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.ThuCungBUS;
import Entity.ThuCung;

/** Màn hình Quản lý Thú cưng - Bám sát thiết kế tham chiếu. */
public class ThuCungPanel extends JPanel {
    private static final int PAGE_SIZE = 7;
    private static final String[] FILTERS = {"Tất cả", "Chó", "Mèo", "Đang lưu trú", "Cần lưu ý"};
    private static final String[] SORTS = {"Mới nhất", "Tên A-Z", "Cân nặng cao nhất", "Cân nặng thấp nhất"};

    private final ThuCungBUS bus = new ThuCungBUS();
    public ThuCungBUS getBus() { return bus; }
    private List<ThuCung> filtered = new ArrayList<>();
    private String boLocHienTai = "Tất cả";
    private String sapXep = SORTS[0];
    private int trang = 1;

    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm theo tên thú cưng, giống, tên chủ...");
    private final List<RoundedButton> chips = new ArrayList<>();
    private final RoundedButton btnSort = new RoundedButton("", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10);
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    // Thẻ thống kê
    private JLabel lblStatTong;
    private JLabel lblStatLuuTru;
    private JLabel lblStatKham;
    private JLabel lblStatTheoDoi;

    private static final DecimalFormat WEIGHT_FMT;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setDecimalSeparator(',');
        WEIGHT_FMT = new DecimalFormat("#0.0", s);
    }

    public ThuCungPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 26, 20, 26));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildStats(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        refresh();
    }

    // ---------------- Tiêu đề + Nút thao tác trên cùng ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JLabel t = new JLabel("Quản lý Thú cưng");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Quản lý hồ sơ, thông tin chăm sóc và sức khỏe của thú cưng");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(4));
        left.add(s);

        RoundedButton btnExcel = new RoundedButton("Xuất Excel", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(18, 11);
        RoundedButton btnThem = new RoundedButton("+ Thêm thú cưng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(18, 11);
        btnExcel.addActionListener(e -> xuatExcel());
        btnThem.addActionListener(e -> themThuCung());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnExcel);
        right.add(btnThem);

        head.add(left, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);
        return head;
    }

    // ---------------- 4 thẻ thống kê ----------------
    private JComponent buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        lblStatTong = new JLabel("450");
        lblStatLuuTru = new JLabel("28");
        lblStatKham = new JLabel("14");
        lblStatTheoDoi = new JLabel("5");

        row.add(statCard("Tổng số thú cưng", lblStatTong, "+12 thú cưng trong tháng", Theme.GREEN));
        row.add(statCard("Đang lưu trú", lblStatLuuTru, "Đang được chăm sóc tại cửa hàng", Theme.MUTED));
        row.add(statCard("Lịch khám hôm nay", lblStatKham, "8 lịch đã hoàn thành", Theme.GREEN));
        row.add(statCard("Cần theo dõi sức khỏe", lblStatTheoDoi, "Cần kiểm tra và theo dõi thêm", Theme.ORANGE));
        return row;
    }

    private JComponent statCard(String label, JLabel valLabel, String note, Color noteColor) {
        RoundedPanel p = new RoundedPanel(16, Theme.CARD, null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);

        valLabel.setFont(Theme.font(Font.BOLD, 24));
        valLabel.setForeground(Theme.TEXT);

        JLabel n = new JLabel(note);
        n.setFont(Theme.font(Font.PLAIN, 11));
        n.setForeground(noteColor);

        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(valLabel);
        p.add(Box.createVerticalStrut(4));
        p.add(n);
        return p;
    }

    // ---------------- Thẻ bảng chứa Toolbar, Table và Footer ----------------
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
        bar.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Ô tìm kiếm
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

        // Filter chips (Tất cả, Chó, Mèo, Đang lưu trú, Cần lưu ý)
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

        // Sắp xếp
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
        table.setRowHeight(49);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0xEAF0F4));
        table.setSelectionForeground(Theme.TEXT);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Header
        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new HeaderCell());

        // Cột và renderers
        int[] widths = {250, 80, 220, 90, 140, 110};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setCellRenderer(new PetCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new SpeciesCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new OwnerCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new WeightCell());
        table.getColumnModel().getColumn(4).setCellRenderer(new HealthCell());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionCell());

        // Lắng nghe click cột thao tác
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    ThuCung tc = (ThuCung) model.getValueAt(row, 0);
                    int xInCell = e.getX() - table.getCellRect(row, col, false).x;
                    if (xInCell < 36) {
                        xemChiTiet(tc);
                    } else if (xInCell < 72) {
                        suaThuCung(tc);
                    } else {
                        hienMenuThaoTac(table, e.getX(), e.getY(), tc);
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JComponent buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.ROW_LINE), new EmptyBorder(12, 16, 12, 16)));
        lblFooter.setFont(Theme.font(Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);
        pnlPages.setOpaque(false);
        f.add(lblFooter, BorderLayout.WEST);
        f.add(pnlPages, BorderLayout.EAST);
        return f;
    }

    // ---------------- Lọc, sắp xếp và phân trang ----------------
    private void refresh() {
        String kw = norm(txtTim.getText().trim());
        List<ThuCung> list = new ArrayList<>();
        for (ThuCung tc : bus.layDanhSach()) {
            if ("Chó".equals(boLocHienTai) && !"Chó".equals(tc.getLoai())) continue;
            if ("Mèo".equals(boLocHienTai) && !"Mèo".equals(tc.getLoai())) continue;
            if ("Đang lưu trú".equals(boLocHienTai) && !tc.isDangLuuTru()) continue;
            if ("Cần lưu ý".equals(boLocHienTai) && !("Cần theo dõi".equals(tc.getSucKhoe()) || "Đang điều trị".equals(tc.getSucKhoe()))) continue;

            if (!kw.isEmpty()) {
                String hay = norm(tc.getTen()) + "|" + norm(tc.getGiong()) + "|" + norm(tc.getTenChu()) + "|" + ThuCungBUS.chiSo(tc.getSdtChu());
                if (!hay.contains(kw) && !hay.contains(ThuCungBUS.chiSo(kw).isEmpty() ? "\u0000" : ThuCungBUS.chiSo(kw))) continue;
            }
            list.add(tc);
        }
        list.sort(comparator());
        filtered = list;

        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        lblFooter.setText(filtered.isEmpty()
                ? "Không tìm thấy thú cưng phù hợp"
                : "Hiển thị " + (from + 1) + "–" + to + " trong " + Theme.number(filtered.size()) + " thú cưng");
        btnSort.setText("Sắp xếp: " + sapXep + "  v");
        btnSort.setPreferredSize(null);

        // Cập nhật kiểu chip lọc
        for (RoundedButton c : chips) {
            boolean on = c.getText().equals(boLocHienTai);
            if (on) c.style(Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null);
            else c.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

        // Cập nhật thẻ thống kê
        if (lblStatTong != null) lblStatTong.setText(Theme.number(bus.getTongSoThuCung()));
        if (lblStatLuuTru != null) lblStatLuuTru.setText(Theme.number(bus.getSoDangLuuTru()));
        if (lblStatTheoDoi != null) lblStatTheoDoi.setText(Theme.number(bus.getSoCanTheoDoi()));

        buildPager(pages);
        revalidate();
        repaint();
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        pnlPages.add(pageButton("‹", trang - 1, trang > 1, false));

        if (pages <= 7) {
            for (int p = 1; p <= pages; p++) pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
        } else {
            // Hiển thị dạng: 1 2 3 ... 65 khớp với ảnh mẫu
            if (trang <= 3) {
                pnlPages.add(pageButton("1", 1, true, trang == 1));
                pnlPages.add(pageButton("2", 2, true, trang == 2));
                pnlPages.add(pageButton("3", 3, true, trang == 3));
                pnlPages.add(pageEllipsis());
                pnlPages.add(pageButton(String.valueOf(pages), pages, true, trang == pages));
            } else if (trang >= pages - 2) {
                pnlPages.add(pageButton("1", 1, true, false));
                pnlPages.add(pageEllipsis());
                for (int p = pages - 2; p <= pages; p++) {
                    pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
                }
            } else {
                pnlPages.add(pageButton("1", 1, true, false));
                pnlPages.add(pageEllipsis());
                pnlPages.add(pageButton(String.valueOf(trang), trang, true, true));
                pnlPages.add(pageEllipsis());
                pnlPages.add(pageButton(String.valueOf(pages), pages, true, false));
            }
        }

        pnlPages.add(pageButton("›", trang + 1, trang < pages, false));
        pnlPages.revalidate();
        pnlPages.repaint();
    }

    private JLabel pageEllipsis() {
        JLabel l = new JLabel("...", SwingConstants.CENTER);
        l.setFont(Theme.font(Font.PLAIN, 13));
        l.setForeground(Theme.MUTED);
        l.setPreferredSize(new Dimension(24, 32));
        return l;
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

    private Comparator<ThuCung> comparator() {
        return switch (sapXep) {
            case "Tên A-Z" -> {
                Collator col = Collator.getInstance(Locale.of("vi"));
                yield Comparator.comparing(ThuCung::getTen, col);
            }
            case "Cân nặng cao nhất" -> Comparator.comparingDouble(ThuCung::getCanNang).reversed();
            case "Cân nặng thấp nhất" -> Comparator.comparingDouble(ThuCung::getCanNang);
            default -> Comparator.comparing(ThuCung::getNgayTiepNhan,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        };
    }

    private static String norm(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // ---------------- Thêm / Sửa / Chi tiết Thú cưng ----------------
    private void themThuCung() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel card = new RoundedPanel(18, Color.WHITE, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Kéo di chuyển dialog bằng chuột
        final Point[] dragPoint = new Point[1];
        card.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        card.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point p = dlg.getLocation();
                    dlg.setLocation(p.x + e.getX() - dragPoint[0].x, p.y + e.getY() - dragPoint[0].y);
                }
            }
        });

        // 1. Header: Tiêu đề + nút đóng X
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel lblTitle = new JLabel("Thêm mới hồ sơ thú cưng");
        lblTitle.setFont(Theme.font(Font.BOLD, 17));
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

        // 2. Nội dung Form
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Phân vùng ảnh thú cưng
        AvatarUploadBox avatarBox = new AvatarUploadBox();
        avatarBox.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Chọn ảnh thú cưng");
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "jpeg", "png"));
                if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                    try {
                        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(fc.getSelectedFile());
                        if (img != null) avatarBox.setImage(img);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dlg, "Không đọc được file ảnh: " + ex.getMessage());
                    }
                }
            }
        });

        JPanel pnlAvatarText = new JPanel();
        pnlAvatarText.setOpaque(false);
        pnlAvatarText.setLayout(new BoxLayout(pnlAvatarText, BoxLayout.Y_AXIS));

        JLabel lblAvTitle = new JLabel("Ảnh thú cưng");
        lblAvTitle.setFont(Theme.font(Font.BOLD, 13));
        lblAvTitle.setForeground(Theme.TEXT);

        JLabel lblAvSub1 = new JLabel("Nhấn vào biểu tượng camera để tải ảnh lên.");
        lblAvSub1.setFont(Theme.font(Font.PLAIN, 12));
        lblAvSub1.setForeground(Theme.MUTED);

        JLabel lblAvSub2 = new JLabel("JPG, PNG · Tối đa 5 MB");
        lblAvSub2.setFont(Theme.font(Font.PLAIN, 11));
        lblAvSub2.setForeground(Theme.MUTED);

        pnlAvatarText.add(lblAvTitle);
        pnlAvatarText.add(Box.createVerticalStrut(3));
        pnlAvatarText.add(lblAvSub1);
        pnlAvatarText.add(Box.createVerticalStrut(2));
        pnlAvatarText.add(lblAvSub2);

        JPanel pnlAvatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pnlAvatarRow.setOpaque(false);
        pnlAvatarRow.add(avatarBox);
        pnlAvatarRow.add(pnlAvatarText);
        body.add(pnlAvatarRow);
        body.add(Box.createVerticalStrut(18));

        // Hàng 1: Tên thú cưng & Loài (2 cột)
        PlaceholderField tfTen = new PlaceholderField("Nhập tên thú cưng...");
        setupField(tfTen);

        String[] loaiOptions = {"Chọn loài (Chó, Mèo)", "Chó", "Mèo"};
        JComboBox<String> cbLoai = new JComboBox<>(loaiOptions);
        styleComboBox(cbLoai, loaiOptions[0]);

        JPanel r1 = new JPanel(new GridLayout(1, 2, 16, 0));
        r1.setOpaque(false);
        r1.add(createFieldGroup("Tên thú cưng", wrapInput(tfTen, 38)));
        r1.add(createFieldGroup("Loài", wrapInput(cbLoai, 38)));
        body.add(r1);
        body.add(Box.createVerticalStrut(14));

        // Hàng 2: Giống (Breed) & Cân nặng ban đầu (Weight)
        String[] giongOptions = {
            "Ví dụ: Poodle, Corgi...", "Poodle", "Corgi", "Golden Retriever", "Husky",
            "Anh lông ngắn", "Anh lông dài", "Munchkin", "Mèo ta", "Shiba Inu", "Pug"
        };
        JComboBox<String> cbGiong = new JComboBox<>(giongOptions);
        cbGiong.setEditable(true);
        styleComboBox(cbGiong, giongOptions[0]);
        if (cbGiong.getEditor().getEditorComponent() instanceof JTextField editorTf) {
            editorTf.setFont(Theme.font(Font.PLAIN, 13));
            editorTf.setBorder(new EmptyBorder(0, 10, 0, 4));
        }

        cbLoai.addActionListener(e -> {
            String sel = (String) cbLoai.getSelectedItem();
            cbGiong.removeAllItems();
            cbGiong.addItem("Ví dụ: Poodle, Corgi...");
            if ("Chó".equals(sel)) {
                String[] choBreeds = {"Poodle", "Corgi", "Golden Retriever", "Husky", "Shiba Inu", "Pug", "Chihuahua", "Bulldog"};
                for (String b : choBreeds) cbGiong.addItem(b);
            } else if ("Mèo".equals(sel)) {
                String[] meoBreeds = {"Anh lông ngắn", "Anh lông dài", "Munchkin", "Mèo ta", "Mèo Ba Tư", "Scottish Fold", "Xiêm"};
                for (String b : meoBreeds) cbGiong.addItem(b);
            } else {
                for (int i = 1; i < giongOptions.length; i++) cbGiong.addItem(giongOptions[i]);
            }
        });

        PlaceholderField tfCanNang = new PlaceholderField("Ví dụ: 4.2 kg");
        setupField(tfCanNang);

        JPanel r2 = new JPanel(new GridLayout(1, 2, 16, 0));
        r2.setOpaque(false);
        r2.add(createFieldGroup("Giống (Breed)", wrapInput(cbGiong, 38)));
        r2.add(createFieldGroup("Cân nặng ban đầu (Weight)", wrapInput(tfCanNang, 38)));
        body.add(r2);
        body.add(Box.createVerticalStrut(14));

        // Hàng 3: Chủ sở hữu (Owner) - Full width
        PlaceholderField tfChu = new PlaceholderField("Tìm tên hoặc số điện thoại khách hàng...");
        setupField(tfChu);

        JPanel boxChu = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        boxChu.setLayout(new BorderLayout());
        boxChu.setPreferredSize(new Dimension(0, 38));
        JLabel icSearch = new JLabel(Icons.of("search", 16, new Color(0x8795A0)));
        icSearch.setBorder(new EmptyBorder(0, 12, 0, 4));
        JLabel icDown = new JLabel(Icons.of("chevron-down", 14, new Color(0x8795A0)));
        icDown.setBorder(new EmptyBorder(0, 4, 0, 12));
        boxChu.add(icSearch, BorderLayout.WEST);
        boxChu.add(tfChu, BorderLayout.CENTER);
        boxChu.add(icDown, BorderLayout.EAST);

        body.add(createFieldGroup("Chủ sở hữu (Owner)", boxChu));
        body.add(Box.createVerticalStrut(14));

        // Hàng 4: Cảnh báo y tế / Dị ứng (Medical alerts / Allergies)
        PlaceholderArea taCanhBao = new PlaceholderArea("Ví dụ: Dị ứng thuốc, bệnh nền...");
        JPanel boxCanhBao = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        boxCanhBao.setLayout(new BorderLayout());
        boxCanhBao.setPreferredSize(new Dimension(0, 78));
        boxCanhBao.add(taCanhBao, BorderLayout.CENTER);

        body.add(createFieldGroup("Cảnh báo y tế / Dị ứng (Medical alerts / Allergies)", boxCanhBao));
        card.add(body, BorderLayout.CENTER);

        // 3. Footer: Nút Hủy và Lưu hồ sơ
        JLabel lblErr = new JLabel(" ");
        lblErr.setFont(Theme.font(Font.PLAIN, 12));
        lblErr.setForeground(Theme.RED);

        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(22, 10);
        RoundedButton btnLuu = new RoundedButton("Lưu hồ sơ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(22, 10);
        btnLuu.setFont(Theme.font(Font.BOLD, 13));

        btnHuy.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            String loai = (String) cbLoai.getSelectedItem();
            if (loai == null || loai.startsWith("Chọn loài")) loai = "Chó";

            Object giongObj = cbGiong.isEditable() ? cbGiong.getEditor().getItem() : cbGiong.getSelectedItem();
            String giong = giongObj == null ? "" : giongObj.toString().trim();
            if (giong.isEmpty() || giong.startsWith("Ví dụ:")) {
                giong = "Chó".equals(loai) ? "Poodle" : "Anh lông ngắn";
            }

            String chuInput = tfChu.getText().trim();
            String tenChu = "Nguyễn Minh Anh";
            String sdtChu = "0901 234 456";
            if (!chuInput.isEmpty() && !chuInput.startsWith("Tìm tên")) {
                tenChu = chuInput;
                sdtChu = "09" + String.format("%08d", (int)(Math.random() * 90000000 + 10000000));
            }

            String cnStr = tfCanNang.getText().trim().replace("kg", "").replace(',', '.').trim();
            double canNang = 4.2;
            if (!cnStr.isEmpty() && !cnStr.startsWith("Ví dụ:")) {
                try {
                    canNang = Double.parseDouble(cnStr);
                    if (canNang <= 0 || canNang > 120) throw new NumberFormatException();
                } catch (Exception ex) {
                    lblErr.setText("Cân nặng không hợp lệ (ví dụ: 4.2 kg).");
                    tfCanNang.requestFocus();
                    return;
                }
            }

            if (ten.isEmpty()) {
                lblErr.setText("Vui lòng nhập tên thú cưng.");
                tfTen.requestFocus();
                return;
            }

            String canhBao = taCanhBao.getText().trim();
            bus.them(ten, giong, loai, tenChu, sdtChu, canNang, "Khỏe mạnh", false, canhBao);
            dlg.dispose();
            txtTim.setText("");
            boLocHienTai = "Tất cả";
            sapXep = "Mới nhất";
            refresh();
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

        // Hỗ trợ đóng bằng ESC
        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.getRootPane().setDefaultButton(btnLuu);

        dlg.setContentPane(card);
        dlg.setSize(630, 560);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private static void setupField(JTextField f) {
        f.setFont(Theme.font(Font.PLAIN, 13));
        f.setForeground(Theme.TEXT);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(0, 12, 0, 12));
    }

    private static JPanel wrapInput(JComponent comp, int height) {
        RoundedPanel box = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        box.setLayout(new BorderLayout());
        box.setPreferredSize(new Dimension(0, height));
        box.add(comp, BorderLayout.CENTER);
        return box;
    }

    private static JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.font(Font.BOLD, 13));
        lbl.setForeground(Theme.TEXT);
        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private static void styleComboBox(JComboBox<String> cb, String placeholder) {
        cb.setFont(Theme.font(Font.PLAIN, 13));
        cb.setForeground(Theme.TEXT);
        cb.setBackground(Color.WHITE);
        cb.setOpaque(false);
        cb.setBorder(new EmptyBorder(0, 6, 0, 6));
        cb.setRenderer(new PlaceholderComboRenderer(placeholder));
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton(Icons.of("chevron-down", 14, new Color(0x8795A0)));
                b.setBorder(new EmptyBorder(0, 0, 0, 10));
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return b;
            }
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Không vẽ ô nền vuông
            }
        });
    }

    private static class PlaceholderComboRenderer extends DefaultListCellRenderer {
        private final String placeholder;
        PlaceholderComboRenderer(String placeholder) { this.placeholder = placeholder; }
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            l.setFont(Theme.font(Font.PLAIN, 13));
            l.setBorder(new EmptyBorder(6, 10, 6, 10));
            if (placeholder != null && placeholder.equals(value)) {
                l.setForeground(new Color(0x9AA6B0));
            } else {
                l.setForeground(isSelected ? Color.WHITE : Theme.TEXT);
            }
            return l;
        }
    }

    private static class PlaceholderArea extends JTextArea {
        private final String placeholder;

        PlaceholderArea(String placeholder) {
            this.placeholder = placeholder;
            setFont(Theme.font(Font.PLAIN, 13));
            setForeground(Theme.TEXT);
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0x9AA6B0));
                g2.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, in.left, in.top + fm.getAscent());
                g2.dispose();
            }
        }
    }

    private static class AvatarUploadBox extends JComponent {
        private BufferedImage image = null;

        AvatarUploadBox() {
            setPreferredSize(new Dimension(64, 64));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Nhấn vào biểu tượng camera để tải ảnh lên");
        }

        void setImage(BufferedImage img) {
            this.image = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int d = 60;
            g2.setColor(new Color(0xEEF3F6));
            g2.fillOval(0, 0, d, d);

            if (image != null) {
                Shape oldClip = g2.getClip();
                g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, d, d));
                g2.drawImage(image, 0, 0, d, d, null);
                g2.setClip(oldClip);
            } else {
                Icons.draw(g2, "paw", (d - 28) / 2.0, (d - 28) / 2.0, 28, new Color(0x9BA8B2), false);
            }

            int badgeD = 22;
            int bx = d - badgeD + 2;
            int by = d - badgeD + 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(bx - 1, by - 1, badgeD + 2, badgeD + 2);
            g2.setColor(new Color(0x56788A));
            g2.fillOval(bx, by, badgeD, badgeD);
            Icons.draw(g2, "camera", bx + (badgeD - 12) / 2.0, by + (badgeD - 12) / 2.0, 12, Color.WHITE, false);

            g2.dispose();
        }
    }

    private void suaThuCung(ThuCung tc) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Chỉnh sửa thông tin thú cưng: " + tc.getTen(), Dialog.ModalityType.APPLICATION_MODAL);

        JTextField tfTen = field(); tfTen.setText(tc.getTen());
        JComboBox<String> cbLoai = new JComboBox<>(new String[] {"Chó", "Mèo"});
        cbLoai.setSelectedItem(tc.getLoai());
        cbLoai.setFont(Theme.font(Font.PLAIN, 14));
        JTextField tfGiong = field(); tfGiong.setText(tc.getGiong());
        JTextField tfChu = field(); tfChu.setText(tc.getTenChu());
        JTextField tfSdt = field(); tfSdt.setText(tc.getSdtChu());
        JTextField tfCanNang = field(); tfCanNang.setText(String.valueOf(tc.getCanNang()));
        JComboBox<String> cbSucKhoe = new JComboBox<>(new String[] {"Khỏe mạnh", "Cần theo dõi", "Đang điều trị"});
        cbSucKhoe.setSelectedItem(tc.getSucKhoe());
        cbSucKhoe.setFont(Theme.font(Font.PLAIN, 14));
        JCheckBox chkLuuTru = new JCheckBox("Đang gửi chăm sóc / lưu trú tại cửa hàng");
        chkLuuTru.setSelected(tc.isDangLuuTru());
        chkLuuTru.setFont(Theme.font(Font.PLAIN, 13));
        chkLuuTru.setOpaque(false);
        JLabel err = new JLabel(" ");
        err.setFont(Theme.font(Font.PLAIN, 12));
        err.setForeground(Theme.RED);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Tên thú cưng *", "Loài *", "Giống loài *", "Tên chủ sở hữu *", "Số điện thoại chủ *", "Cân nặng (kg) *", "Trạng thái sức khỏe"};
        JComponent[] inputs = {tfTen, cbLoai, tfGiong, tfChu, tfSdt, tfCanNang, cbSucKhoe};
        int y = 0;
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(Theme.font(Font.BOLD, 13));
            c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(l, c);
            c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(inputs[i], c);
        }
        c.gridy = y++; c.insets = new Insets(4, 0, 10, 0); form.add(chkLuuTru, c);
        c.gridy = y; c.insets = new Insets(0, 0, 0, 0); form.add(err, c);

        RoundedButton ok = new RoundedButton("Cập nhật", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(22, 9);
        RoundedButton cancel = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(22, 9);
        cancel.setFont(Theme.font(Font.PLAIN, 13));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(new EmptyBorder(4, 24, 18, 24));
        btns.add(cancel);
        btns.add(ok);

        cancel.addActionListener(e -> dlg.dispose());
        ok.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            String giong = tfGiong.getText().trim();
            String loai = (String) cbLoai.getSelectedItem();
            String chu = tfChu.getText().trim();
            String sdt = ThuCungBUS.chiSo(tfSdt.getText());
            String cnStr = tfCanNang.getText().trim().replace(',', '.');

            if (ten.isEmpty()) { err.setText("Vui lòng nhập tên thú cưng."); return; }
            if (giong.isEmpty()) { err.setText("Vui lòng nhập giống loài."); return; }
            if (chu.isEmpty()) { err.setText("Vui lòng nhập tên chủ."); return; }

            double canNang;
            try {
                canNang = Double.parseDouble(cnStr);
            } catch (Exception ex) {
                err.setText("Cân nặng không hợp lệ.");
                return;
            }

            tc.setTen(ten);
            tc.setGiong(giong);
            tc.setLoai(loai);
            tc.setTenChu(chu);
            tc.setSdtChu(ThuCungBUS.dinhDangSdt(sdt));
            tc.setCanNang(canNang);
            tc.setSucKhoe((String) cbSucKhoe.getSelectedItem());
            tc.setDangLuuTru(chkLuuTru.isSelected());

            bus.capNhat(tc);
            dlg.dispose();
            refresh();
        });

        dlg.getRootPane().setDefaultButton(ok);
        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setSize(440, 560);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private void xemChiTiet(ThuCung tc) {
        String msg = String.format("""
                Mã thú cưng: %s
                Tên: %s
                Giống: %s (%s)
                Chủ sở hữu: %s - %s
                Cân nặng: %s kg
                Sức khỏe: %s
                Đang lưu trú: %s
                Ghi chú: %s
                """, tc.getMaTC(), tc.getTen(), tc.getGiong(), tc.getLoai(),
                tc.getTenChu(), tc.getSdtChu(), WEIGHT_FMT.format(tc.getCanNang()),
                tc.getSucKhoe(), tc.isDangLuuTru() ? "Có" : "Không",
                tc.getGhiChu().isEmpty() ? "Không có" : tc.getGhiChu());

        JOptionPane.showMessageDialog(this, msg, "Chi tiết thú cưng - " + tc.getTen(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void hienMenuThaoTac(Component invoker, int x, int y, ThuCung tc) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem itXem = new JMenuItem("Xem chi tiết");
        itXem.setFont(Theme.font(Font.PLAIN, 13));
        itXem.addActionListener(e -> xemChiTiet(tc));

        JMenuItem itSua = new JMenuItem("Chỉnh sửa thông tin");
        itSua.setFont(Theme.font(Font.PLAIN, 13));
        itSua.addActionListener(e -> suaThuCung(tc));

        JMenuItem itXoa = new JMenuItem("Xóa thú cưng");
        itXoa.setFont(Theme.font(Font.PLAIN, 13));
        itXoa.setForeground(Theme.RED);
        itXoa.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa thú cưng " + tc.getTen() + " không?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                bus.xoa(tc.getMaTC());
                refresh();
            }
        });

        JMenuItem itKeDon = new JMenuItem("Kê đơn thuốc");
        itKeDon.setFont(Theme.font(Font.PLAIN, 13));
        itKeDon.setIcon(Icons.of("prescription", 16, Theme.ACCENT));
        itKeDon.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainFrame mf) {
                mf.moKeDonChoThuCung(tc);
            }
        });

        JMenuItem itPhacDo = new JMenuItem("Lập phác đồ điều trị");
        itPhacDo.setFont(Theme.font(Font.PLAIN, 13));
        itPhacDo.setIcon(Icons.of("protocol", 16, Theme.ACCENT));
        itPhacDo.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainFrame mf) {
                mf.moPhacDoChoThuCung(tc);
            }
        });

        m.add(itXem);
        m.add(itSua);
        m.addSeparator();
        m.add(itKeDon);
        m.add(itPhacDo);
        m.addSeparator();
        m.add(itXoa);
        m.show(invoker, x, y);
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(300, 36));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0xD5DCE1)), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }

    // ---------------- Xuất file Excel (CSV BOM UTF-8) ----------------
    private void xuatExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Xuất danh sách thú cưng");
        fc.setSelectedFile(new java.io.File("ThuCung.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) file = new java.io.File(file.getPath() + ".csv");
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF'); // BOM tiếng Việt
            w.write("Mã TC,Tên thú cưng,Loài,Giống loài,Chủ sở hữu,Số điện thoại,Cân nặng (kg),Sức khỏe,Đang lưu trú,Ghi chú\r\n");
            for (ThuCung tc : filtered) {
                w.write(String.join(",",
                        csv(tc.getMaTC()), csv(tc.getTen()), csv(tc.getLoai()), csv(tc.getGiong()),
                        csv(tc.getTenChu()), csv("=\"" + ThuCungBUS.chiSo(tc.getSdtChu()) + "\""),
                        String.valueOf(tc.getCanNang()), csv(tc.getSucKhoe()),
                        tc.isDangLuuTru() ? "Có" : "Không", csv(tc.getGhiChu())));
                w.write("\r\n");
            }
            JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " thú cưng:\n" + file.getAbsolutePath(),
                    "Xuất thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }

    // ---------------- TableModel & Renderers ----------------
    private static class PageModel extends AbstractTableModel {
        private final String[] cols = {"Thú cưng", "Loài", "Chủ sở hữu", "Cân nặng", "Trạng thái sức khỏe", "Thao tác"};
        private List<ThuCung> rows = new ArrayList<>();

        void setRows(List<ThuCung> r) { rows = new ArrayList<>(r); fireTableDataChanged(); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            ThuCung tc = rows.get(r);
            return switch (c) {
                case 0 -> tc;
                case 1 -> tc.getLoai();
                case 2 -> tc;
                case 3 -> tc.getCanNang();
                case 4 -> tc.getSucKhoe();
                default -> tc;
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
            setHorizontalAlignment(c == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, c == 0 ? 16 : 8, 0, c == 5 ? 16 : 8)));
            return this;
        }
    }

    private abstract static class Cell extends JPanel implements TableCellRenderer {
        Cell() { super(new BorderLayout()); setOpaque(true); }
        void prep(JTable t, boolean sel, int col) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, col == 0 ? 16 : 8, 0, col == 5 ? 16 : 8)));
        }
    }

    // Cột 0: Thú cưng (Avatar chữ cái + Tên thú cưng bold + Giống xám)
    private static class PetCell extends Cell {
        private final PetAvatar avatar = new PetAvatar();
        private final JLabel name = new JLabel();
        private final JLabel breed = new JLabel();

        PetCell() {
            name.setFont(Theme.font(Font.BOLD, 13));
            name.setForeground(Theme.TEXT);
            breed.setFont(Theme.font(Font.PLAIN, 11));
            breed.setForeground(Theme.MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(name);
            text.add(Box.createVerticalStrut(1));
            text.add(breed);
            text.add(Box.createVerticalGlue());

            JPanel wrap = new JPanel(new BorderLayout(10, 0));
            wrap.setOpaque(false);
            wrap.add(avatar, BorderLayout.WEST);
            wrap.add(text, BorderLayout.CENTER);
            add(wrap, BorderLayout.CENTER);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            ThuCung tc = (ThuCung) v;
            avatar.letter = tc.getTen().isEmpty() ? "" : tc.getTen().substring(0, 1).toUpperCase();
            name.setText(tc.getTen());
            breed.setText(tc.getGiong());
            return this;
        }
    }

    private static class PetAvatar extends JComponent {
        String letter = "";
        PetAvatar() { setPreferredSize(new Dimension(36, 36)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int y = (getHeight() - 34) / 2;
            g2.setColor(Theme.AVATAR_BG);
            g2.fillOval(0, y, 34, 34);
            g2.setColor(Theme.TEXT);
            g2.setFont(Theme.font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letter, (34 - fm.stringWidth(letter)) / 2, y + (34 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // Cột 1: Loài (Badge bo tròn xám nhạt)
    private static class SpeciesCell extends Cell {
        private final SpeciesBadge badge = new SpeciesBadge();

        SpeciesCell() {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            add(wrap, BorderLayout.CENTER);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            badge.set(String.valueOf(v));
            return this;
        }
    }

    private static class SpeciesBadge extends JComponent {
        private String text = "";
        void set(String t) { text = t; }

        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.PLAIN, 11));
            return new Dimension(fm.stringWidth(text) + 20, 22);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.BADGE_GRAY_BG);
            g2.fillRoundRect(0, (getHeight() - 22) / 2, getWidth(), 22, 8, 8);
            g2.setColor(Theme.MUTED);
            g2.setFont(Theme.font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // Cột 2: Chủ sở hữu (Tên chủ bold + SĐT xám bên dưới)
    private static class OwnerCell extends Cell {
        private final JLabel name = new JLabel();
        private final JLabel phone = new JLabel();

        OwnerCell() {
            name.setFont(Theme.font(Font.BOLD, 13));
            name.setForeground(Theme.TEXT);
            phone.setFont(Theme.font(Font.PLAIN, 11));
            phone.setForeground(Theme.MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(name);
            text.add(Box.createVerticalStrut(1));
            text.add(phone);
            text.add(Box.createVerticalGlue());
            add(text, BorderLayout.CENTER);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            ThuCung tc = (ThuCung) v;
            name.setText(tc.getTenChu());
            phone.setText(tc.getSdtChu());
            return this;
        }
    }

    // Cột 3: Cân nặng ("4,2 kg")
    private static class WeightCell extends Cell {
        private final JLabel lbl = new JLabel();
        WeightCell() {
            lbl.setFont(Theme.font(Font.PLAIN, 13));
            lbl.setForeground(Theme.TEXT);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText(WEIGHT_FMT.format((Double) v) + " kg");
            return this;
        }
    }

    // Cột 4: Trạng thái sức khỏe (Badge: Khỏe mạnh, Cần theo dõi, Đang điều trị)
    private static class HealthCell extends Cell {
        private final HealthBadge badge = new HealthBadge();

        HealthCell() {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            add(wrap, BorderLayout.CENTER);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            badge.set(String.valueOf(v));
            return this;
        }
    }

    private static class HealthBadge extends JComponent {
        private String text = "";
        private Color bg = Theme.GREEN_BG, fg = Theme.GREEN;

        void set(String t) {
            text = t;
            if ("Cần theo dõi".equals(t) || "Đang điều trị".equals(t)) {
                bg = Theme.ORANGE_BG;
                fg = Theme.ORANGE;
            } else {
                bg = Theme.GREEN_BG;
                fg = Theme.GREEN;
            }
        }

        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.PLAIN, 11));
            return new Dimension(fm.stringWidth(text) + 20, 22);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, (getHeight() - 22) / 2, getWidth(), 22, 8, 8);
            g2.setColor(fg);
            g2.setFont(Theme.font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // Cột 5: Thao tác (3 icon: eye, edit, more)
    private static class ActionCell extends Cell {
        ActionCell() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = (getHeight() - 18) / 2;
            int startX = 14;
            Icons.draw(g2, "eye", startX, y, 18, Theme.ACTION_ICON, false);
            Icons.draw(g2, "edit", startX + 26, y, 18, Theme.ACTION_ICON, false);
            Icons.draw(g2, "more", startX + 52, y, 18, Theme.ACTION_ICON, true);
            g2.dispose();
        }
    }
}
