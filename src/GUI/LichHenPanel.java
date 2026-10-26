package GUI;

<<<<<<< HEAD
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class LichHenPanel extends JPanel {
    public LichHenPanel() {
        // Cài đặt nền chuẩn theo UI Style Guide
        setBackground(Color.decode("#F4F7F9"));
        setLayout(new BorderLayout(24, 24));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // 1. TOP BAR (Tiêu đề và Nút thao tác)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        
        JLabel title = new JLabel("Lịch hẹn & Phân bổ dịch vụ");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.decode("#263238"));
        
        JButton btnAdd = new JButton("+ Tạo lịch hẹn");
        btnAdd.setBackground(Color.decode("#607D8B"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 14));
        
        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnAdd, BorderLayout.EAST);

        // 2. MAIN CONTENT (Chứa Lưới lịch và Sơ đồ lồng)
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // MODULE 1: TIME-GRID (Ma trận lịch hẹn)
        JPanel timeGrid = new JPanel(new BorderLayout());
        timeGrid.setBackground(Color.WHITE);
        timeGrid.setPreferredSize(new Dimension(1000, 400));
        timeGrid.setBorder(BorderFactory.createLineBorder(Color.decode("#CFD8DC")));
        
        JLabel gridLabel = new JLabel("Khu vực Lưới Lịch Hẹn (Time-Grid) - Sẽ tích hợp kéo thả (Drag & Drop) tại đây", SwingConstants.CENTER);
        gridLabel.setForeground(Color.decode("#78909C"));
        timeGrid.add(gridLabel, BorderLayout.CENTER);

        // MODULE 2: PET HOTEL (Sơ đồ lồng lưu trú)
        JPanel petHotel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        petHotel.setBackground(Color.WHITE);
        petHotel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#CFD8DC")), 
            "Sơ đồ lưu trú (Pet Hotel)"
        ));
        
        // Tạo các lồng mẫu theo trạng thái
        JPanel cage1 = createCage("VIP 02 - Trống", "#F4F7F9", "#78909C", false);
        JPanel cage2 = createCage("Mochi (Dị ứng)", "#FFFFFF", "#E53935", true);
        
        petHotel.add(cage1);
        petHotel.add(cage2);

        // Lắp ráp các khối
        centerContent.add(timeGrid);
        centerContent.add(Box.createRigidArea(new Dimension(0, 24)));
        centerContent.add(petHotel);

        add(topBar, BorderLayout.NORTH);
        add(centerContent, BorderLayout.CENTER);
    }

    // Hàm tiện ích tạo khối Lồng thú cưng
    private JPanel createCage(String text, String bgColor, String fgColor, boolean hasAlert) {
        JPanel cage = new JPanel(new BorderLayout());
        cage.setPreferredSize(new Dimension(150, 120));
        cage.setBackground(Color.decode(bgColor));
        
        if (hasAlert) {
            cage.setBorder(BorderFactory.createLineBorder(Color.decode(fgColor), 2));
        } else {
            cage.setBorder(BorderFactory.createLineBorder(Color.decode("#CFD8DC"), 1));
        }
        
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.decode(fgColor));
        label.setFont(new Font("Arial", Font.BOLD, 14));
        cage.add(label, BorderLayout.CENTER);
        
        return cage;
=======
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
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

import GUI.LichHenData.LichHen;

/**
 * Mục "Quản Lý Lịch Hẹn": 5 màn hình dùng chung một khung giao diện (bám theo màn Lịch sử Nhập / Xuất Kho):
 * Hồ sơ lịch hẹn, Quản lý lịch hẹn, Đổi lịch hẹn, Xác nhận lịch hẹn, Hủy lịch hẹn.
 */
public class LichHenPanel extends JPanel {

    public enum Mode {
        HO_SO("Hồ sơ lịch hẹn", "Tra cứu toàn bộ hồ sơ lịch hẹn của khách hàng và thú cưng"),
        QUAN_LY("Quản lý lịch hẹn", "Thêm, cập nhật và theo dõi lịch hẹn của phòng khám"),
        DOI("Đổi lịch hẹn", "Thay đổi ngày giờ hẹn theo yêu cầu của khách hàng"),
        XAC_NHAN("Xác nhận lịch hẹn", "Xác nhận các lịch hẹn đang chờ xử lý"),
        HUY("Hủy lịch hẹn", "Hủy lịch hẹn và ghi nhận lý do hủy");

        public final String title;
        public final String subtitle;
        Mode(String title, String subtitle) { this.title = title; this.subtitle = subtitle; }
    }

    public static final String PARENT_TITLE = "Quản Lý Lịch Hẹn";

    private static final int PAGE_SIZE = 9;
    private static final int ICON = 18, GAP = 14;
    private static final int COL_ACTION = 7;

    private final Mode mode;
    private String[] filters;
    private String[] acts;

    private List<LichHen> filtered = new ArrayList<>();
    private String trangThaiLoc = "Tất cả";
    private int trang = 1;
    private LocalDate tuNgay = LocalDate.now().minusDays(14);
    private LocalDate denNgay = LocalDate.now().plusDays(30);

    private final PageModel model = new PageModel();
    private final JTable table = new JTable(model) {
        @Override public String getToolTipText(MouseEvent e) {
            int row = rowAtPoint(e.getPoint());
            if (row >= 0 && columnAtPoint(e.getPoint()) == COL_ACTION) {
                int i = actionAt(e);
                if (i >= 0) return tip(acts[i]);
            }
            return null;
        }
    };
    private final PlaceholderField txtTim = new PlaceholderField("Tìm mã lịch hẹn, khách hàng, thú cưng, bác sĩ...");
    private final List<RoundedButton> filterChips = new ArrayList<>();
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public LichHenPanel(Mode mode) {
        super(new BorderLayout(0, 14));
        this.mode = mode;
        configure();

        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 24, 16, 24));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(buildBreadcrumb(), BorderLayout.NORTH);
        top.add(buildHeader(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        LichHenData.addListener(this::refresh);
        refresh();
    }

    private void configure() {
        switch (mode) {
            case HO_SO -> {
                filters = new String[] {"Tất cả", LichHenData.CHO_XAC_NHAN, LichHenData.DA_XAC_NHAN, LichHenData.DA_DOI,
                        LichHenData.HOAN_TAT, LichHenData.DA_HUY};
                acts = new String[] {"eye"};
            }
            case QUAN_LY -> {
                filters = new String[] {"Tất cả", LichHenData.CHO_XAC_NHAN, LichHenData.DA_XAC_NHAN, LichHenData.DA_DOI,
                        LichHenData.HOAN_TAT, LichHenData.DA_HUY};
                acts = new String[] {"eye", "edit", "trash"};
            }
            case DOI -> {
                filters = new String[] {"Tất cả", LichHenData.CHO_XAC_NHAN, LichHenData.DA_XAC_NHAN, LichHenData.DA_DOI};
                acts = new String[] {"eye", "calendar"};
            }
            case XAC_NHAN -> {
                filters = new String[] {"Tất cả", LichHenData.CHO_XAC_NHAN, LichHenData.DA_DOI};
                acts = new String[] {"eye", "check"};
            }
            case HUY -> {
                filters = new String[] {"Tất cả", LichHenData.CHO_XAC_NHAN, LichHenData.DA_XAC_NHAN, LichHenData.DA_DOI};
                acts = new String[] {"eye", "close"};
            }
        }
    }

    private static String tip(String act) {
        return switch (act) {
            case "eye" -> "Xem hồ sơ";
            case "edit" -> "Cập nhật";
            case "trash" -> "Xóa";
            case "calendar" -> "Đổi lịch";
            case "check" -> "Xác nhận";
            case "close" -> "Hủy lịch hẹn";
            default -> null;
        };
    }

    /** Lịch hẹn nào được hiển thị ở màn hình này. */
    private boolean hienThi(LichHen l) {
        return switch (mode) {
            case HO_SO, QUAN_LY -> true;
            case DOI, HUY -> l.conHieuLuc();
            case XAC_NHAN -> LichHenData.CHO_XAC_NHAN.equals(l.trangThai) || LichHenData.DA_DOI.equals(l.trangThai);
        };
    }

    // ---------------- Breadcrumb ----------------
    private JComponent buildBreadcrumb() {
        JPanel bc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bc.setOpaque(false);

        JLabel parent = new JLabel(PARENT_TITLE);
        parent.setFont(Theme.font(Font.PLAIN, 12));
        parent.setForeground(Theme.MUTED);
        if (mode != Mode.QUAN_LY) {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            parent.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    Window w = SwingUtilities.getWindowAncestor(LichHenPanel.this);
                    if (w instanceof MainFrame mf) mf.chon(Mode.QUAN_LY.title);
                }
                @Override public void mouseEntered(MouseEvent e) { parent.setForeground(Theme.TEXT); }
                @Override public void mouseExited(MouseEvent e) { parent.setForeground(Theme.MUTED); }
            });
        }

        JLabel sep = new JLabel("›");
        sep.setFont(Theme.font(Font.PLAIN, 13));
        sep.setForeground(Theme.MUTED);

        JLabel current = new JLabel(mode.title);
        current.setFont(Theme.font(Font.BOLD, 12));
        current.setForeground(new Color(0x28566E));

        bc.add(parent);
        bc.add(sep);
        bc.add(current);
        return bc;
    }

    // ---------------- Header ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JLabel t = new JLabel(mode.title);
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel(mode.subtitle);
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        left.add(t, BorderLayout.NORTH);
        left.add(s, BorderLayout.CENTER);
        head.add(left, BorderLayout.CENTER);

        RoundedButton btn = null;
        switch (mode) {
            case HO_SO -> {
                btn = new RoundedButton("Xuất Excel", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 10);
                btn.setIcon(Icons.of("excel", 15, Color.WHITE));
                btn.addActionListener(e -> xuatExcel());
            }
            case QUAN_LY -> {
                btn = new RoundedButton("Thêm lịch hẹn", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 10);
                btn.setIcon(Icons.of("plus", 15, Color.WHITE));
                btn.addActionListener(e -> moForm(null));
            }
            case XAC_NHAN -> {
                btn = new RoundedButton("Xác nhận tất cả", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 10);
                btn.setIcon(Icons.of("check", 15, Color.WHITE));
                btn.addActionListener(e -> xacNhanTatCa());
            }
            default -> { }
        }
        if (btn != null) {
            btn.setFont(Theme.font(Font.BOLD, 13));
            btn.setIconTextGap(8);
            head.add(btn, BorderLayout.EAST);
        }
        return head;
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
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(16, 16, 12, 16));

        JPanel row1 = new JPanel(new BorderLayout(16, 0));
        row1.setOpaque(false);

        JPanel pnlDateGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlDateGroup.setOpaque(false);
        JLabel lblTime = new JLabel("Thời gian");
        lblTime.setFont(Theme.font(Font.PLAIN, 13));
        lblTime.setForeground(Theme.MUTED);

        RoundedPanel boxDate = new RoundedPanel(8, Color.WHITE, Theme.BORDER);
        boxDate.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        boxDate.setPreferredSize(new Dimension(215, 36));
        JLabel icCal = new JLabel(Icons.of("calendar", 14, Theme.MUTED));
        JLabel lblDateVal = new JLabel(tuNgay.format(CalendarPicker.DATE_FMT) + "  —  " + denNgay.format(CalendarPicker.DATE_FMT));
        lblDateVal.setFont(Theme.font(Font.PLAIN, 12));
        lblDateVal.setForeground(Theme.TEXT);
        JLabel icDown = new JLabel(Icons.of("chevron-down", 12, Theme.MUTED));
        boxDate.add(icCal);
        boxDate.add(lblDateVal);
        boxDate.add(icDown);
        boxDate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boxDate.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                CalendarPicker.showDateRangePicker(boxDate, tuNgay, denNgay, (start, end) -> {
                    tuNgay = start;
                    denNgay = end;
                    lblDateVal.setText(tuNgay.format(CalendarPicker.DATE_FMT) + "  —  " + denNgay.format(CalendarPicker.DATE_FMT));
                    trang = 1;
                    refresh();
                });
            }
        });
        pnlDateGroup.add(lblTime);
        pnlDateGroup.add(boxDate);

        RoundedPanel search = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        search.setLayout(new BorderLayout());
        search.setPreferredSize(new Dimension(360, 36));
        JLabel icSearch = new JLabel(Icons.of("search", 15, new Color(0x8795A0)));
        icSearch.setBorder(new EmptyBorder(0, 12, 0, 4));
        txtTim.setBorder(new EmptyBorder(0, 4, 0, 10));
        txtTim.setOpaque(false);
        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        search.add(icSearch, BorderLayout.WEST);
        search.add(txtTim, BorderLayout.CENTER);
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() { trang = 1; refresh(); }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        row1.add(pnlDateGroup, BorderLayout.WEST);
        row1.add(search, BorderLayout.EAST);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);
        for (String f : filters) {
            RoundedButton chip = new RoundedButton(f, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(14, 7);
            chip.setFont(Theme.font(Font.PLAIN, 13));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 34));
            chip.addActionListener(e -> { trangThaiLoc = f; trang = 1; refresh(); });
            filterChips.add(chip);
            row2.add(chip);
        }

        bar.add(row1);
        bar.add(Box.createVerticalStrut(12));
        bar.add(row2);
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
        ToolTipManager.sharedInstance().registerComponent(table);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderCell());

        int[] widths = {115, 100, 160, 165, 125, 150, 105, 20 + acts.length * (ICON + GAP) + 40};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setCellRenderer(new TextCell(Font.PLAIN, Theme.TEXT));
        table.getColumnModel().getColumn(1).setCellRenderer(new DateCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new CustomerCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new TextCell(Font.PLAIN, Theme.MUTED));
        table.getColumnModel().getColumn(4).setCellRenderer(new TextCell(Font.PLAIN, Theme.TEXT));
        table.getColumnModel().getColumn(5).setCellRenderer(new TextCell(Font.PLAIN, Theme.MUTED));
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCell());
        table.getColumnModel().getColumn(COL_ACTION).setCellRenderer(new ActionCell());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col != COL_ACTION) return;
                int i = actionAt(e);
                if (i < 0) return;
                LichHen l = model.get(row);
                switch (acts[i]) {
                    case "eye" -> xemHoSo(l);
                    case "edit" -> moForm(l);
                    case "trash" -> xoaLich(l);
                    case "calendar" -> doiLich(l);
                    case "check" -> xacNhan(l);
                    case "close" -> huyLich(l);
                    default -> { }
                }
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                boolean over = row >= 0 && table.columnAtPoint(e.getPoint()) == COL_ACTION && actionAt(e) >= 0;
                table.setCursor(Cursor.getPredefinedCursor(over ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
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

    /** Biểu tượng thao tác nằm dưới con trỏ (hoặc -1). */
    private int actionAt(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return -1;
        Rectangle r = table.getCellRect(row, COL_ACTION, false);
        int total = acts.length * ICON + (acts.length - 1) * GAP;
        int rel = e.getX() - (r.x + (r.width - total) / 2);
        if (rel < 0 || rel >= total) return -1;
        int idx = rel / (ICON + GAP);
        return rel - idx * (ICON + GAP) <= ICON ? idx : -1;
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

    // ---------------- Lọc và phân trang ----------------
    private void refresh() {
        String kw = norm(txtTim.getText().trim());
        List<LichHen> list = new ArrayList<>();
        for (LichHen l : LichHenData.layDanhSach()) {
            if (!hienThi(l)) continue;
            if (!"Tất cả".equals(trangThaiLoc) && !l.trangThai.equals(trangThaiLoc)) continue;
            if (!kw.isEmpty()) {
                String hay = norm(l.ma) + "|" + norm(l.khachHang) + "|" + l.sdt + "|" + norm(l.thuCung) + "|" + norm(l.bacSi)
                        + "|" + norm(l.dichVu);
                if (!hay.contains(kw)) continue;
            }
            if (tuNgay != null && denNgay != null && (l.ngay.isBefore(tuNgay) || l.ngay.isAfter(denNgay))) continue;
            list.add(l);
        }
        filtered = list;

        int pages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);
        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());
        model.setRows(filtered.subList(from, to));

        lblFooter.setText(filtered.isEmpty()
                ? "Không tìm thấy lịch hẹn nào phù hợp"
                : "Hiển thị " + (to - from) + " trong tổng số " + filtered.size() + " lịch hẹn");

        for (RoundedButton c : filterChips) {
            boolean on = c.getText().equals(trangThaiLoc);
            if (on) c.style(new Color(0xEBF3F8), new Color(0xDEECF5), new Color(0x28566E), new Color(0x7AA3BC));
            else c.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

        buildPager(pages);
        revalidate();
        repaint();
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        pnlPages.add(pageButton("‹", trang - 1, trang > 1, false));
        if (pages <= 7) {
            for (int p = 1; p <= pages; p++) pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
        } else if (trang <= 3) {
            for (int p = 1; p <= 3; p++) pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
            pnlPages.add(pageEllipsis());
            pnlPages.add(pageButton(String.valueOf(pages), pages, true, trang == pages));
        } else if (trang >= pages - 2) {
            pnlPages.add(pageButton("1", 1, true, false));
            pnlPages.add(pageEllipsis());
            for (int p = pages - 2; p <= pages; p++) pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
        } else {
            pnlPages.add(pageButton("1", 1, true, false));
            pnlPages.add(pageEllipsis());
            pnlPages.add(pageButton(String.valueOf(trang), trang, true, true));
            pnlPages.add(pageEllipsis());
            pnlPages.add(pageButton(String.valueOf(pages), pages, true, false));
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

    private static String norm(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // =====================================================================
    //                              NGHIỆP VỤ
    // =====================================================================

    private Window owner() { return SwingUtilities.getWindowAncestor(this); }

    private static String thoiGian(LichHen l) {
        return l.gio.format(LichHenData.TIME_FMT) + " " + l.ngay.format(CalendarPicker.DATE_FMT);
    }

    private boolean hoi(String title, String msg, String ok) {
        return JOptionPane.showOptionDialog(this, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new String[] {ok, "Đóng"}, "Đóng") == 0;
    }

    private void xacNhan(LichHen l) {
        if (!hoi("Xác nhận lịch hẹn",
                "Xác nhận lịch hẹn " + l.ma + "?\n" + l.khachHang + " - " + l.thuCung + "\n" + thoiGian(l), "Xác nhận")) return;
        LichHenData.xacNhan(l);
    }

    private void xacNhanTatCa() {
        List<LichHen> ds = new ArrayList<>(filtered);
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có lịch hẹn nào cần xác nhận.", "Xác nhận lịch hẹn",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!hoi("Xác nhận tất cả", "Xác nhận " + ds.size() + " lịch hẹn đang hiển thị?", "Xác nhận tất cả")) return;
        for (LichHen l : ds) LichHenData.xacNhan(l);
    }

    private void xoaLich(LichHen l) {
        if (!hoi("Xóa lịch hẹn",
                "Xóa vĩnh viễn lịch hẹn " + l.ma + " (" + l.khachHang + ")?\n"
                        + "Nếu chỉ muốn hủy lịch, hãy dùng mục \"Hủy lịch hẹn\" để còn lưu hồ sơ.", "Xóa")) return;
        LichHenData.xoa(l);
    }

    private void xuatExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("ho_so_lich_hen.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new java.io.FileOutputStream(fc.getSelectedFile()),
                StandardCharsets.UTF_8))) {
            w.print('﻿'); // BOM để Excel đọc đúng tiếng Việt
            w.println("Mã lịch hẹn,Ngày,Giờ,Khách hàng,SĐT,Thú cưng,Dịch vụ,Bác sĩ,Trạng thái,Ghi chú");
            for (LichHen l : filtered) {
                String[] c = {l.ma, l.ngay.format(CalendarPicker.DATE_FMT), l.gio.format(LichHenData.TIME_FMT), l.khachHang,
                        l.sdt, l.thuCung, l.dichVu, l.bacSi, l.trangThai, l.ghiChu};
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < c.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append('"').append(c[i].replace("\"", "\"\"")).append('"');
                }
                w.println(sb);
            }
            JOptionPane.showMessageDialog(this, "Đã xuất " + filtered.size() + " lịch hẹn ra file:\n" + fc.getSelectedFile(),
                    "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- Hồ sơ lịch hẹn ----------------
    private void xemHoSo(LichHen l) {
        JDialog d = newDialog("Hồ sơ lịch hẹn");

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.NORTHWEST;

        JPanel title = new JPanel(new BorderLayout(12, 0));
        title.setOpaque(false);
        JLabel code = new JLabel(l.ma);
        code.setFont(Theme.font(Font.BOLD, 20));
        code.setForeground(Theme.TEXT);
        JPanel bw = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        bw.setOpaque(false);
        bw.add(new Badge(l.trangThai));
        title.add(code, BorderLayout.WEST);
        title.add(bw, BorderLayout.CENTER);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.insets = new Insets(0, 0, 14, 0);
        body.add(title, gc);

        gc.gridwidth = 1; gc.insets = new Insets(0, 0, 12, 16);
        int r = 1;
        String[][] info = {
            {"Khách hàng", l.khachHang}, {"Số điện thoại", l.sdt},
            {"Thú cưng", l.thuCung}, {"Dịch vụ", l.dichVu},
            {"Thời gian hẹn", thoiGian(l)}, {"Bác sĩ phụ trách", l.bacSi},
        };
        for (int i = 0; i < info.length; i++) {
            gc.gridx = i % 2; gc.gridy = r + i / 2;
            body.add(infoItem(info[i][0], info[i][1]), gc);
        }
        r += 3;
        gc.gridx = 0; gc.gridy = r++; gc.gridwidth = 2;
        body.add(infoItem("Ghi chú", l.ghiChu.isEmpty() ? "—" : l.ghiChu), gc);

        JLabel lh = new JLabel("LỊCH SỬ THAY ĐỔI");
        lh.setFont(Theme.font(Font.BOLD, 11));
        lh.setForeground(Theme.MUTED);
        gc.gridy = r++; gc.insets = new Insets(4, 0, 6, 0);
        body.add(lh, gc);

        JPanel log = new JPanel();
        log.setLayout(new BoxLayout(log, BoxLayout.Y_AXIS));
        log.setBackground(new Color(0xF8FAFC));
        log.setBorder(new EmptyBorder(8, 12, 8, 12));
        for (String s : l.lichSu) {
            JLabel x = new JLabel("•  " + s);
            x.setFont(Theme.font(Font.PLAIN, 12));
            x.setForeground(Theme.TEXT);
            x.setBorder(new EmptyBorder(2, 0, 2, 0));
            log.add(x);
        }
        JScrollPane sp = new JScrollPane(log);
        sp.setBorder(new javax.swing.border.LineBorder(Theme.BORDER, 1, true));
        sp.setPreferredSize(new Dimension(0, 110));
        gc.gridy = r++; gc.insets = new Insets(0, 0, 0, 0);
        body.add(sp, gc);

        RoundedButton close = secondaryButton("Đóng");
        close.addActionListener(e -> d.dispose());
        showDialog(d, body, close, null);
    }

    private JPanel infoItem(String label, String value) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(label);
        a.setFont(Theme.font(Font.PLAIN, 11));
        a.setForeground(Theme.MUTED);
        JLabel b = new JLabel(value);
        b.setFont(Theme.font(Font.BOLD, 13));
        b.setForeground(Theme.TEXT);
        p.add(a);
        p.add(Box.createVerticalStrut(2));
        p.add(b);
        return p;
    }

    // ---------------- Thêm / Cập nhật ----------------
    private void moForm(LichHen edit) {
        boolean isEdit = edit != null;
        JDialog d = newDialog(isEdit ? "Cập nhật lịch hẹn" : "Thêm lịch hẹn");

        JTextField tfKh = textField(isEdit ? edit.khachHang : "");
        JTextField tfSdt = textField(isEdit ? edit.sdt : "");
        JTextField tfTc = textField(isEdit ? edit.thuCung : "");
        JComboBox<String> cbDv = combo(LichHenData.DICH_VU, isEdit ? edit.dichVu : null);
        JComboBox<String> cbBs = combo(LichHenData.BAC_SI, isEdit ? edit.bacSi : null);
        JComboBox<String> cbGio = combo(LichHenData.GIO, isEdit ? edit.gio.format(LichHenData.TIME_FMT) : "09:00");
        DateBox dbNgay = new DateBox(isEdit ? edit.ngay : LocalDate.now().plusDays(1));
        JComboBox<String> cbTt = combo(new String[] {LichHenData.CHO_XAC_NHAN, LichHenData.DA_XAC_NHAN, LichHenData.DA_DOI,
                LichHenData.HOAN_TAT, LichHenData.DA_HUY}, isEdit ? edit.trangThai : LichHenData.CHO_XAC_NHAN);
        JTextArea taGhiChu = new JTextArea(isEdit ? edit.ghiChu : "", 3, 20);
        taGhiChu.setFont(Theme.font(Font.PLAIN, 13));
        taGhiChu.setOpaque(false);
        taGhiChu.setLineWrap(true);
        taGhiChu.setWrapStyleWord(true);
        taGhiChu.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        addField(body, 0, 0, 1, "Khách hàng *", boxed(tfKh));
        addField(body, 1, 0, 1, "Số điện thoại *", boxed(tfSdt));
        addField(body, 0, 1, 1, "Thú cưng *", boxed(tfTc));
        addField(body, 1, 1, 1, "Dịch vụ", cbDv);
        addField(body, 0, 2, 1, "Bác sĩ phụ trách", cbBs);
        addField(body, 1, 2, 1, "Ngày hẹn", dbNgay);
        addField(body, 0, 3, 1, "Giờ hẹn", cbGio);
        if (isEdit) addField(body, 1, 3, 1, "Trạng thái", cbTt);
        RoundedPanel noteBox = new RoundedPanel(8, Color.WHITE, Theme.BORDER);
        noteBox.setLayout(new BorderLayout());
        noteBox.add(taGhiChu);
        addField(body, 0, 4, 2, "Ghi chú", noteBox);

        JLabel err = errorLabel();
        RoundedButton save = primaryButton(isEdit ? "Lưu thay đổi" : "Thêm lịch hẹn");
        save.addActionListener(e -> {
            String kh = tfKh.getText().trim(), sdt = tfSdt.getText().trim(), tc = tfTc.getText().trim();
            if (kh.isEmpty() || sdt.isEmpty() || tc.isEmpty()) { err.setText("Vui lòng nhập đủ khách hàng, số điện thoại và thú cưng."); return; }
            if (!sdt.matches("\\d{9,11}")) { err.setText("Số điện thoại phải gồm 9–11 chữ số."); return; }
            LocalDate ngay = dbNgay.value;
            if (!isEdit && ngay.isBefore(LocalDate.now())) { err.setText("Ngày hẹn không được ở quá khứ."); return; }
            LocalTime gio = LocalTime.parse((String) cbGio.getSelectedItem());
            String bs = (String) cbBs.getSelectedItem();
            String tt = isEdit ? (String) cbTt.getSelectedItem() : LichHenData.CHO_XAC_NHAN;
            boolean conHieuLuc = !LichHenData.HOAN_TAT.equals(tt) && !LichHenData.DA_HUY.equals(tt);
            if (conHieuLuc && LichHenData.biTrung(bs, ngay, gio, isEdit ? edit.ma : null)) {
                err.setText(bs + " đã có lịch lúc " + gio.format(LichHenData.TIME_FMT) + " ngày "
                        + ngay.format(CalendarPicker.DATE_FMT) + ".");
                return;
            }
            LichHen l = isEdit ? edit : new LichHen();
            l.khachHang = kh;
            l.sdt = sdt;
            l.thuCung = tc;
            l.dichVu = (String) cbDv.getSelectedItem();
            l.bacSi = bs;
            l.ngay = ngay;
            l.gio = gio;
            l.ghiChu = taGhiChu.getText().trim();
            l.trangThai = tt;
            d.dispose();
            if (isEdit) LichHenData.capNhat(l); else LichHenData.them(l);
        });
        RoundedButton cancel = secondaryButton("Hủy bỏ");
        cancel.addActionListener(e -> d.dispose());
        showDialog(d, body, cancel, save, err);
    }

    // ---------------- Đổi lịch ----------------
    private void doiLich(LichHen l) {
        JDialog d = newDialog("Đổi lịch hẹn " + l.ma);

        DateBox dbNgay = new DateBox(l.ngay.isBefore(LocalDate.now()) ? LocalDate.now() : l.ngay);
        JComboBox<String> cbGio = combo(LichHenData.GIO, l.gio.format(LichHenData.TIME_FMT));
        JTextField tfLyDo = textField("");

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        addInfoCard(body, l, 0);
        addField(body, 0, 1, 1, "Ngày hẹn mới *", dbNgay);
        addField(body, 1, 1, 1, "Giờ hẹn mới *", cbGio);
        addField(body, 0, 2, 2, "Lý do đổi lịch *", boxed(tfLyDo));

        JLabel err = errorLabel();
        RoundedButton save = primaryButton("Đổi lịch");
        save.addActionListener(e -> {
            LocalDate ngay = dbNgay.value;
            LocalTime gio = LocalTime.parse((String) cbGio.getSelectedItem());
            String lyDo = tfLyDo.getText().trim();
            if (ngay.isBefore(LocalDate.now())) { err.setText("Ngày hẹn mới không được ở quá khứ."); return; }
            if (ngay.equals(l.ngay) && gio.equals(l.gio)) { err.setText("Thời gian mới trùng với lịch hiện tại."); return; }
            if (lyDo.isEmpty()) { err.setText("Vui lòng nhập lý do đổi lịch."); return; }
            if (LichHenData.biTrung(l.bacSi, ngay, gio, l.ma)) {
                err.setText(l.bacSi + " đã có lịch lúc " + gio.format(LichHenData.TIME_FMT) + " ngày "
                        + ngay.format(CalendarPicker.DATE_FMT) + ".");
                return;
            }
            d.dispose();
            LichHenData.doiLich(l, ngay, gio, lyDo);
        });
        RoundedButton cancel = secondaryButton("Đóng");
        cancel.addActionListener(e -> d.dispose());
        showDialog(d, body, cancel, save, err);
    }

    // ---------------- Hủy lịch ----------------
    private void huyLich(LichHen l) {
        JDialog d = newDialog("Hủy lịch hẹn " + l.ma);

        JComboBox<String> cbLyDo = combo(new String[] {"Khách yêu cầu hủy", "Khách không liên lạc được",
                "Bác sĩ có việc đột xuất", "Trùng lịch", "Lý do khác"}, null);
        JTextField tfThem = textField("");

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        addInfoCard(body, l, 0);
        addField(body, 0, 1, 2, "Lý do hủy *", cbLyDo);
        addField(body, 0, 2, 2, "Ghi chú thêm", boxed(tfThem));

        JLabel err = errorLabel();
        RoundedButton save = new RoundedButton("Xác nhận hủy", Theme.RED, new Color(0xB93A3A), Color.WHITE, null, 8).pad(18, 9);
        save.setFont(Theme.font(Font.BOLD, 13));
        save.addActionListener(e -> {
            String lyDo = (String) cbLyDo.getSelectedItem();
            String them = tfThem.getText().trim();
            if (!them.isEmpty()) lyDo += " - " + them;
            d.dispose();
            LichHenData.huy(l, lyDo);
        });
        RoundedButton cancel = secondaryButton("Đóng");
        cancel.addActionListener(e -> d.dispose());
        showDialog(d, body, cancel, save, err);
    }

    private void addInfoCard(JPanel body, LichHen l, int row) {
        RoundedPanel card = new RoundedPanel(10, new Color(0xF3F6F8), null);
        card.setLayout(new GridLayout(1, 3, 12, 0));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.add(infoItem("Khách hàng", l.khachHang));
        card.add(infoItem("Thú cưng", l.thuCung));
        card.add(infoItem("Lịch hiện tại", thoiGian(l)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        gc.insets = new Insets(0, 0, 14, 0);
        body.add(card, gc);
    }

    // ---------------- Tiện ích dialog ----------------
    private JDialog newDialog(String title) {
        JDialog d = new JDialog(owner(), title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setResizable(false);
        d.getContentPane().setBackground(Color.WHITE);
        return d;
    }

    private void showDialog(JDialog d, JPanel body, JComponent... buttons) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 24, 18, 24));

        JLabel t = new JLabel(d.getTitle());
        t.setFont(Theme.font(Font.BOLD, 18));
        t.setForeground(Theme.TEXT);
        root.add(t, BorderLayout.NORTH);

        Dimension ps = body.getPreferredSize();
        body.setPreferredSize(new Dimension(Math.max(540, ps.width), ps.height));
        root.add(body, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        for (JComponent b : buttons) {
            if (b instanceof JLabel lb) south.add(lb, BorderLayout.NORTH);
            else if (b != null) btns.add(b);
        }
        south.add(btns, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.pack();
        d.setLocationRelativeTo(owner());
        d.setVisible(true);
    }

    private static JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.RED);
        return l;
    }

    private static RoundedButton primaryButton(String text) {
        RoundedButton b = new RoundedButton(text, Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 9);
        b.setFont(Theme.font(Font.BOLD, 13));
        return b;
    }

    private static RoundedButton secondaryButton(String text) {
        RoundedButton b = new RoundedButton(text, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(18, 9);
        b.setFont(Theme.font(Font.PLAIN, 13));
        return b;
    }

    private static JTextField textField(String v) {
        JTextField f = new JTextField(v);
        f.setFont(Theme.font(Font.PLAIN, 13));
        f.setForeground(Theme.TEXT);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(0, 10, 0, 10));
        return f;
    }

    private static RoundedPanel boxed(JComponent c) {
        RoundedPanel b = new RoundedPanel(8, Color.WHITE, Theme.BORDER);
        b.setLayout(new BorderLayout());
        b.setPreferredSize(new Dimension(0, 36));
        b.add(c);
        return b;
    }

    private static JComboBox<String> combo(String[] items, String selected) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(Theme.font(Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(0, 36));
        if (selected != null) c.setSelectedItem(selected);
        return c;
    }

    private static void addField(JPanel body, int col, int row, int span, String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = col; gc.gridy = row + 1; gc.gridwidth = span;
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1; gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, col == 1 ? 8 : 0, 12, col == 0 && span == 1 ? 8 : 0);
        body.add(p, gc);
    }

    /** Ô chọn ngày: bấm vào để mở lịch. */
    private static class DateBox extends RoundedPanel {
        LocalDate value;
        private final JLabel lbl = new JLabel();

        DateBox(LocalDate init) {
            super(8, Color.WHITE, Theme.BORDER);
            this.value = init;
            setPreferredSize(new Dimension(0, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.setFont(Theme.font(Font.PLAIN, 13));
            lbl.setForeground(Theme.TEXT);
            lbl.setText(value.format(CalendarPicker.DATE_FMT));
            JPanel in = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 9));
            in.setOpaque(false);
            in.add(new JLabel(Icons.of("calendar", 14, Theme.MUTED)));
            in.add(lbl);
            setLayout(new BorderLayout());
            add(in);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    CalendarPicker.showDatePicker(DateBox.this, value, dt -> {
                        value = dt;
                        lbl.setText(dt.format(CalendarPicker.DATE_FMT));
                    });
                }
            });
            in.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    CalendarPicker.showDatePicker(DateBox.this, value, dt -> {
                        value = dt;
                        lbl.setText(dt.format(CalendarPicker.DATE_FMT));
                    });
                }
            });
        }
    }

    // =====================================================================
    //                       TableModel & Renderers
    // =====================================================================
    private static class PageModel extends AbstractTableModel {
        private final String[] cols = {
            "MÃ LỊCH HẸN", "THỜI GIAN", "KHÁCH HÀNG", "THÚ CƯNG", "DỊCH VỤ", "BÁC SĨ", "TRẠNG THÁI", "THAO TÁC"
        };
        private List<LichHen> rows = new ArrayList<>();

        void setRows(List<LichHen> r) { rows = new ArrayList<>(r); fireTableDataChanged(); }
        LichHen get(int r) { return rows.get(r); }
        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            LichHen l = rows.get(r);
            return switch (c) {
                case 0 -> l.ma;
                case 3 -> l.thuCung;
                case 4 -> l.dichVu;
                case 5 -> l.bacSi;
                case 6 -> l.trangThai;
                default -> l;
            };
        }
    }

    private static class HeaderCell extends JLabel implements TableCellRenderer {
        HeaderCell() {
            setOpaque(true);
            setFont(Theme.font(Font.BOLD, 11));
            setForeground(Theme.MUTED);
            setBackground(new Color(0xF8FAFC));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            setHorizontalAlignment(c == COL_ACTION ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, c == 0 ? 16 : 8, 0, c == COL_ACTION ? 16 : 8)));
            return this;
        }
    }

    private abstract static class Cell extends JPanel implements TableCellRenderer {
        Cell() { super(new BorderLayout()); setOpaque(true); }
        void prep(JTable t, boolean sel, int col) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, col == 0 ? 16 : 8, 0, col == COL_ACTION ? 16 : 8)));
        }
    }

    private static class TextCell extends Cell {
        private final JLabel lbl = new JLabel();
        TextCell(int style, Color color) {
            lbl.setFont(Theme.font(style, 13));
            lbl.setForeground(color);
            add(lbl, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            lbl.setText(String.valueOf(v));
            return this;
        }
    }

    /** Hai dòng: dòng trên đậm/đen, dòng dưới nhỏ/mờ. */
    private abstract static class TwoLineCell extends Cell {
        final JLabel top = new JLabel();
        final JLabel bottom = new JLabel();
        TwoLineCell(int topStyle, int topSize) {
            top.setFont(Theme.font(topStyle, topSize));
            top.setForeground(Theme.TEXT);
            bottom.setFont(Theme.font(Font.PLAIN, 11));
            bottom.setForeground(Theme.MUTED);
            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(top);
            text.add(Box.createVerticalStrut(1));
            text.add(bottom);
            text.add(Box.createVerticalGlue());
            add(text, BorderLayout.CENTER);
        }
    }

    private static class DateCell extends TwoLineCell {
        DateCell() { super(Font.PLAIN, 12); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            LichHen l = (LichHen) v;
            top.setText(l.ngay.format(CalendarPicker.DATE_FMT));
            bottom.setText(l.gio.format(LichHenData.TIME_FMT));
            return this;
        }
    }

    private static class CustomerCell extends TwoLineCell {
        CustomerCell() { super(Font.BOLD, 13); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            LichHen l = (LichHen) v;
            top.setText(l.khachHang);
            bottom.setText(l.sdt);
            return this;
        }
    }

    private static class StatusCell extends Cell {
        private final Badge badge = new Badge("");
        StatusCell() {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(badge);
            add(wrap, BorderLayout.CENTER);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            badge.setStatus(String.valueOf(v));
            return this;
        }
    }

    /** Nhãn trạng thái bo góc (cùng kiểu với màn Lịch sử kho). */
    private static class Badge extends JComponent {
        private String text = "";
        private Color bg = Theme.GREEN_BG, fg = Theme.GREEN;

        Badge(String status) { setStatus(status); }

        void setStatus(String t) {
            this.text = t;
            switch (t) {
                case LichHenData.CHO_XAC_NHAN -> { bg = Theme.ORANGE_BG; fg = Theme.ORANGE; }
                case LichHenData.DA_XAC_NHAN -> { bg = Theme.BLUE_BG; fg = Theme.BLUE_TEXT; }
                case LichHenData.DA_DOI -> { bg = Theme.PEACH_BG; fg = Theme.PEACH_TEXT; }
                case LichHenData.HOAN_TAT -> { bg = Theme.GREEN_BG; fg = Theme.GREEN; }
                default -> { bg = Theme.RED_BG; fg = Theme.RED; }
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

    private class ActionCell extends Cell {
        ActionCell() { setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            prep(t, s, c);
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int total = acts.length * ICON + (acts.length - 1) * GAP;
            int x = (getWidth() - total) / 2;
            int y = (getHeight() - ICON) / 2;
            for (String a : acts) {
                Color c = switch (a) {
                    case "edit", "calendar" -> Theme.ACCENT;
                    case "check" -> Theme.GREEN;
                    case "trash", "close" -> Theme.RED;
                    default -> Theme.ACTION_ICON;
                };
                Icons.draw(g2, a, x, y, ICON, c, false);
                x += ICON + GAP;
            }
            g2.dispose();
        }
>>>>>>> stash
    }
}
