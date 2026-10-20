package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import Bus.ThongKeBUS;
import Entity.KhachHang;
import Entity.PhieuKho;
import Entity.SanPham;

/**
 * Màn hình Báo cáo & Thống kê - Pet Station.
 * Tích hợp 3 Tab phân hệ, 4 Thẻ KPI, Biểu đồ cột BarChart & Biểu đồ tròn DonutChart thuần Java2D,
 * bộ lọc thời gian tương tác với CalendarPicker và xuất Excel/CSV UTF-8.
 */
public class ThongKePanel extends JPanel {
    private static final int PAGE_SIZE = 7;
    private static final String[] TABS = {"Doanh thu & Dòng tiền", "Tồn kho & Mặt hàng", "Khách hàng & Thú cưng"};
    private static final String[] QUICK_DATES = {"Tháng này", "7 ngày qua", "Năm 2026", "Tất cả"};

    private final ThongKeBUS bus = new ThongKeBUS();
    private String currentTab = TABS[0];
    private String quickDate = QUICK_DATES[0];

    // Mặc định là Tháng 3/2026 theo mốc dữ liệu mẫu của đồ án
    private LocalDate tuNgay = LocalDate.of(2026, 3, 1);
    private LocalDate denNgay = LocalDate.of(2026, 3, 31);
    private int trang = 1;

    // Component điều khiển
    private final List<RoundedButton> tabChips = new ArrayList<>();
    private final List<RoundedButton> dateChips = new ArrayList<>();
    private final JLabel lblDateVal = new JLabel();

    // 4 Thẻ KPI
    private final KPICard card1 = new KPICard("DOANH THU XUẤT BÁN", "0 đ", "0 phiếu xuất", "card", Theme.BLUE_TEXT, Theme.BLUE_BG);
    private final KPICard card2 = new KPICard("CHI PHÍ NHẬP KHO", "0 đ", "0 phiếu nhập", "box", Theme.MUTED, Theme.BADGE_GRAY_BG);
    private final KPICard card3 = new KPICard("LỢI NHUẬN GỘP", "0 đ", "Tỷ suất 0%", "bar-chart", Theme.GREEN, Theme.GREEN_BG);
    private final KPICard card4 = new KPICard("CẢNH BÁO TỒN KHO", "0 SKU", "0 sản phẩm sắp hết", "warning", Theme.ORANGE, Theme.ORANGE_BG);

    // Biểu đồ Java2D
    private final BarChartComponent barChart = new BarChartComponent("Doanh thu & Chi phí theo thời gian");
    private final DonutChartComponent donutChart = new DonutChartComponent("Cơ cấu giao dịch");

    // Bảng chi tiết & Phân trang
    private final DetailedTableModel tableModel = new DetailedTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel lblFooter = new JLabel();
    private final JPanel pnlPages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private List<Object[]> allTableData = new ArrayList<>();

    private static final DecimalFormat NUM_FMT;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        NUM_FMT = new DecimalFormat("#,##0", s);
    }

    public ThongKePanel() {
        super(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(18, 24, 16, 24));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildToolbar(), BorderLayout.CENTER);
        top.add(buildKPICards(), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Vùng giữa: Biểu đồ (trên) + Bảng dữ liệu (dưới)
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(buildChartsRow(), BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    // ---------------- 1. Header: Tiêu đề + Nút thao tác ----------------
    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JLabel t = new JLabel("Báo cáo & Thống kê");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);

        JLabel s = new JLabel("Theo dõi doanh thu, chi phí xuất nhập kho, tồn kho và khách hàng");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        left.add(t, BorderLayout.NORTH);
        left.add(s, BorderLayout.CENTER);

        // Nút Làm mới & Xuất Excel
        RoundedButton btnRefresh = new RoundedButton("Làm mới", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(14, 9);
        btnRefresh.setFont(Theme.font(Font.PLAIN, 13));
        btnRefresh.setIcon(Icons.of("sync", 15, Theme.ACCENT));
        btnRefresh.setIconTextGap(8);
        btnRefresh.addActionListener(e -> refresh());

        RoundedButton btnExcel = new RoundedButton("Xuất Excel", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(16, 9);
        btnExcel.setFont(Theme.font(Font.BOLD, 13));
        btnExcel.setIcon(Icons.of("excel", 15, Color.WHITE));
        btnExcel.setIconTextGap(8);
        btnExcel.addActionListener(e -> xuatExcel());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnRefresh);
        right.add(btnExcel);

        head.add(left, BorderLayout.CENTER);
        head.add(right, BorderLayout.EAST);
        return head;
    }

    // ---------------- 2. Toolbar: Tab phân hệ + Bộ lọc thời gian ----------------
    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);

        // Trái: 3 Tab phân hệ
        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTabs.setOpaque(false);
        for (String tabName : TABS) {
            RoundedButton chip = new RoundedButton(tabName, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(14, 7);
            chip.setFont(Theme.font(Font.PLAIN, 13));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 34));
            chip.addActionListener(e -> {
                currentTab = tabName;
                trang = 1;
                refresh();
            });
            tabChips.add(chip);
            pnlTabs.add(chip);
        }

        // Phải: Bộ lọc thời gian
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlFilter.setOpaque(false);

        for (String q : QUICK_DATES) {
            RoundedButton chip = new RoundedButton(q, Color.WHITE, new Color(0xF3F6F8), Theme.MUTED, Theme.BORDER, 8).pad(10, 6);
            chip.setFont(Theme.font(Font.PLAIN, 12));
            chip.setPreferredSize(new Dimension(chip.getPreferredSize().width, 32));
            chip.addActionListener(e -> chonQuickDate(q));
            dateChips.add(chip);
            pnlFilter.add(chip);
        }

        // Box ngày click mở CalendarPicker
        RoundedPanel boxDate = new RoundedPanel(8, Color.WHITE, Theme.BORDER);
        boxDate.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        boxDate.setPreferredSize(new Dimension(205, 32));
        boxDate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel icCal = new JLabel(Icons.of("calendar", 13, Theme.MUTED));
        lblDateVal.setFont(Theme.font(Font.PLAIN, 12));
        lblDateVal.setForeground(Theme.TEXT);
        lblDateVal.setText(tuNgay.format(CalendarPicker.DATE_FMT) + " — " + denNgay.format(CalendarPicker.DATE_FMT));
        boxDate.add(icCal);
        boxDate.add(lblDateVal);

        boxDate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CalendarPicker.showDateRangePicker(boxDate, tuNgay, denNgay, (start, end) -> {
                    tuNgay = start;
                    denNgay = end;
                    quickDate = "Tùy chọn";
                    lblDateVal.setText(tuNgay.format(CalendarPicker.DATE_FMT) + " — " + denNgay.format(CalendarPicker.DATE_FMT));
                    trang = 1;
                    refresh();
                });
            }
        });
        pnlFilter.add(boxDate);

        bar.add(pnlTabs, BorderLayout.WEST);
        bar.add(pnlFilter, BorderLayout.EAST);
        return bar;
    }

    private void chonQuickDate(String q) {
        quickDate = q;
        LocalDate now = LocalDate.of(2026, 3, 4); // Căn cứ mốc thời gian mẫu đồ án
        switch (q) {
            case "7 ngày qua" -> {
                tuNgay = now.minusDays(6);
                denNgay = now;
            }
            case "Tháng này" -> {
                tuNgay = LocalDate.of(2026, 3, 1);
                denNgay = LocalDate.of(2026, 3, 31);
            }
            case "Năm 2026" -> {
                tuNgay = LocalDate.of(2026, 1, 1);
                denNgay = LocalDate.of(2026, 12, 31);
            }
            case "Tất cả" -> {
                tuNgay = LocalDate.of(2025, 1, 1);
                denNgay = LocalDate.of(2026, 12, 31);
            }
        }
        lblDateVal.setText(tuNgay.format(CalendarPicker.DATE_FMT) + " — " + denNgay.format(CalendarPicker.DATE_FMT));
        trang = 1;
        refresh();
    }

    // ---------------- 3. Hàng 4 Thẻ KPI ----------------
    private JComponent buildKPICards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.add(card1);
        row.add(card2);
        row.add(card3);
        row.add(card4);
        return row;
    }

    // ---------------- 4. Hàng Biểu đồ trực quan ----------------
    private JComponent buildChartsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.insets = new Insets(0, 0, 0, 14);

        // Biểu đồ cột (62% chiều rộng)
        RoundedPanel pnlBar = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        pnlBar.setLayout(new BorderLayout());
        pnlBar.setBorder(new EmptyBorder(12, 14, 12, 14));
        pnlBar.add(barChart, BorderLayout.CENTER);
        c.gridx = 0;
        c.weightx = 0.62;
        row.add(pnlBar, c);

        // Biểu đồ Donut (38% chiều rộng)
        RoundedPanel pnlDonut = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        pnlDonut.setLayout(new BorderLayout());
        pnlDonut.setBorder(new EmptyBorder(12, 14, 12, 14));
        pnlDonut.add(donutChart, BorderLayout.CENTER);
        c.gridx = 1;
        c.weightx = 0.38;
        c.insets = new Insets(0, 0, 0, 0);
        row.add(pnlDonut, c);

        return row;
    }

    // ---------------- 5. Thẻ Bảng dữ liệu chi tiết ----------------
    private JComponent buildTableCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());

        // Header bảng
        JPanel topPnl = new JPanel(new BorderLayout());
        topPnl.setOpaque(false);
        topPnl.setBorder(new EmptyBorder(14, 18, 10, 18));
        JLabel lblTableTitle = new JLabel("Bảng kê chi tiết");
        lblTableTitle.setFont(Theme.font(Font.BOLD, 14));
        lblTableTitle.setForeground(Theme.TEXT);
        topPnl.add(lblTableTitle, BorderLayout.WEST);
        card.add(topPnl, BorderLayout.NORTH);

        // Bảng JTable
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0xEAF0F4));
        table.setSelectionForeground(Theme.TEXT);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = table.getTableHeader();
        h.setReorderingAllowed(false);
        h.setResizingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new TableHeaderCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.ROW_LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Footer phân trang
        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(10, 18, 10, 18));
        lblFooter.setFont(Theme.font(Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);
        foot.add(lblFooter, BorderLayout.WEST);
        foot.add(pnlPages, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);

        return card;
    }

    // ---------------- REFRESH DATA & CẬP NHẬT GIAO DIỆN ----------------
    public void refresh() {
        // Cập nhật style nút Tab
        for (RoundedButton b : tabChips) {
            boolean active = b.getText().equals(currentTab);
            if (active) b.style(new Color(0xEBF3F8), new Color(0xDEECF5), new Color(0x28566E), new Color(0x7AA3BC));
            else b.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

        // Cập nhật style nút QuickDate
        for (RoundedButton b : dateChips) {
            boolean active = b.getText().equals(quickDate);
            if (active) b.style(Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null);
            else b.style(Color.WHITE, new Color(0xF3F6F8), Theme.MUTED, Theme.BORDER);
        }

        switch (currentTab) {
            case "Doanh thu & Dòng tiền" -> refreshTabDoanhThu();
            case "Tồn kho & Mặt hàng" -> refreshTabKhoHang();
            case "Khách hàng & Thú cưng" -> refreshTabKhachHang();
        }

        updateTablePage();
        revalidate();
        repaint();
    }

    private void refreshTabDoanhThu() {
        long tongDoanhThu = bus.tinhTongDoanhThu(tuNgay, denNgay);
        long tongChiNhap = bus.tinhTongChiNhap(tuNgay, denNgay);
        long loiNhuan = tongDoanhThu - tongChiNhap;
        int soPhieuXuat = bus.demSoPhieu(tuNgay, denNgay, "Xuất bán");
        int soPhieuNhap = bus.demSoPhieu(tuNgay, denNgay, "Nhập mới");

        // Cập nhật 4 thẻ KPI
        card1.update("DOANH THU XUẤT BÁN", Theme.money(tongDoanhThu), soPhieuXuat + " phiếu bán", "card", Theme.BLUE_TEXT, Theme.BLUE_BG);
        card2.update("CHI PHÍ NHẬP KHO", Theme.money(tongChiNhap), soPhieuNhap + " phiếu nhập", "box", Theme.MUTED, Theme.BADGE_GRAY_BG);

        double tySuat = tongDoanhThu > 0 ? (loiNhuan * 100.0 / tongDoanhThu) : 0;
        Color lnColor = loiNhuan >= 0 ? Theme.GREEN : Theme.RED;
        Color lnBg = loiNhuan >= 0 ? Theme.GREEN_BG : Theme.RED_BG;
        card3.update("LỢI NHUẬN GỘP", Theme.money(loiNhuan), String.format("Tỷ suất %.1f%%", tySuat), "bar-chart", lnColor, lnBg);

        int tongPhieu = bus.layDanhSachPhieu(tuNgay, denNgay, null).size();
        card4.update("TỔNG GIAO DỊCH", tongPhieu + " phiếu", "Nhập, xuất & hủy", "sync", Theme.ORANGE, Theme.ORANGE_BG);

        // Biểu đồ cột: Doanh thu vs Chi phí theo ngày
        barChart.clearData();
        barChart.setTitle("Doanh thu xuất bán & Chi phí nhập kho theo ngày");
        Map<LocalDate, long[]> daily = bus.layDoanhThuTheoNgay(tuNgay, denNgay);

        List<String> cats = new ArrayList<>();
        List<Double> valDoanhThu = new ArrayList<>();
        List<Double> valChiNhap = new ArrayList<>();
        DateTimeFormatter dfShort = DateTimeFormatter.ofPattern("dd/MM");

        for (Map.Entry<LocalDate, long[]> e : daily.entrySet()) {
            cats.add(e.getKey().format(dfShort));
            valDoanhThu.add((double) e.getValue()[0]);
            valChiNhap.add((double) e.getValue()[1]);
        }
        barChart.setCategories(cats);
        barChart.addSeries("Doanh thu", Theme.ACCENT, valDoanhThu);
        barChart.addSeries("Chi phí", Theme.ORANGE, valChiNhap);

        // Biểu đồ Donut: Cơ cấu loại phiếu
        donutChart.clearData();
        donutChart.setTitle("Cơ cấu giao dịch phiếu kho");
        Map<String, Integer> coCau = bus.layCoCauLoaiPhieu(tuNgay, denNgay);
        donutChart.addSlice("Xuất bán", coCau.getOrDefault("Xuất bán", 0), Theme.ACCENT);
        donutChart.addSlice("Nhập mới", coCau.getOrDefault("Nhập mới", 0), Theme.ORANGE);
        donutChart.addSlice("Xuất hủy", coCau.getOrDefault("Xuất hủy", 0), Theme.RED);
        donutChart.setCenterText(tongPhieu + "", "Phiếu");

        // Dữ liệu bảng chi tiết: Danh sách phiếu kho
        String[] cols = {"MÃ PHIẾU", "LOẠI PHIẾU", "NGÀY LẬP", "NGƯỜI THỰC HIỆN", "ĐỐI TÁC", "GIÁ TRỊ", "TRẠNG THÁI"};
        allTableData.clear();
        for (PhieuKho pk : bus.layDanhSachPhieu(tuNgay, denNgay, null)) {
            allTableData.add(new Object[]{
                    pk.getMaPhieu(),
                    pk.getLoaiPhieu(),
                    pk.getNgayLap() + " " + pk.getGioLap(),
                    pk.getNguoiThucHien(),
                    pk.getDoiTac(),
                    NUM_FMT.format(pk.getTongGiaTri()) + " đ",
                    pk.getTrangThai()
            });
        }
        tableModel.setColumns(cols);
    }

    private void refreshTabKhoHang() {
        int tongSKU = bus.getTongSKU();
        int soSapHet = bus.getSoSapHet();
        long tongGiaTri = bus.getTongGiaTriTonKho();
        Map<String, Integer> coCauSKU = bus.layCoCauDanhMucSKU();

        card1.update("TỔNG MẶT HÀNG", tongSKU + " SKU", "Đang quản lý trong kho", "box", Theme.BLUE_TEXT, Theme.BLUE_BG);
        card2.update("CẢNH BÁO SẮP HẾT", soSapHet + " SKU", "Tồn kho ≤ 5 đơn vị", "warning", Theme.RED, Theme.RED_BG);
        card3.update("GIÁ TRỊ TỒN KHO", Theme.money(tongGiaTri), "Tổng vốn lưu kho", "card", Theme.GREEN, Theme.GREEN_BG);
        card4.update("NGÀNH HÀNG", coCauSKU.size() + " Danh mục", "Thức ăn, Đồ chơi, Thuốc...", "dashboard", Theme.ORANGE, Theme.ORANGE_BG);

        // Biểu đồ cột: Giá trị vốn tồn kho theo Danh mục
        barChart.clearData();
        barChart.setTitle("Giá trị vốn tồn kho theo Danh mục hàng hóa (VNĐ)");
        Map<String, Long> gtrTon = bus.layGiaTriTonTheoDanhMuc();
        List<String> cats = new ArrayList<>(gtrTon.keySet());
        List<Double> vals = new ArrayList<>();
        for (String c : cats) vals.add(gtrTon.get(c).doubleValue());
        barChart.setCategories(cats);
        barChart.addSeries("Giá trị vốn tồn", Theme.ACCENT, vals);

        // Biểu đồ Donut: Cơ cấu SKU theo danh mục
        donutChart.clearData();
        donutChart.setTitle("Tỷ lệ số lượng SKU theo Danh mục");
        Color[] dmColors = {Theme.ACCENT, Theme.ORANGE, Theme.GREEN, Theme.BLUE_TEXT, new Color(0x8B5CF6)};
        int ci = 0;
        for (Map.Entry<String, Integer> e : coCauSKU.entrySet()) {
            donutChart.addSlice(e.getKey(), e.getValue(), dmColors[ci % dmColors.length]);
            ci++;
        }
        donutChart.setCenterText(tongSKU + "", "SKU");

        // Bảng chi tiết: Danh sách sản phẩm sắp hết hàng cần nhập bổ sung
        String[] cols = {"MÃ SKU", "TÊN SẢN PHẨM", "QUY CÁCH", "DANH MỤC", "ĐƠN VỊ", "ĐƠN GIÁ", "TỒN KHO", "TRẠNG THÁI"};
        allTableData.clear();
        for (SanPham sp : bus.layDanhSachSapHet()) {
            allTableData.add(new Object[]{
                    sp.getMaSKU(),
                    sp.getTenSP(),
                    sp.getQuyCach(),
                    sp.getDanhMuc(),
                    sp.getDonViTinh(),
                    NUM_FMT.format(sp.getDonGia()) + " đ",
                    sp.getSoLuongTon(),
                    sp.getTrangThai()
            });
        }
        tableModel.setColumns(cols);
    }

    private void refreshTabKhachHang() {
        int tongKH = bus.getTongSoKhachHang();
        int tongTC = bus.getTongSoThuCung();
        int dangLuuTru = bus.getSoDangLuuTru();
        int canTheoDoi = bus.getSoCanTheoDoi();

        card1.update("TỔNG KHÁCH HÀNG", tongKH + " khách hàng", "Đã tạo tài khoản", "customers", Theme.BLUE_TEXT, Theme.BLUE_BG);
        card2.update("TỔNG THÚ CƯNG", tongTC + " bé", "Hồ sơ sức khỏe phòng khám", "paw", Theme.GREEN, Theme.GREEN_BG);
        card3.update("ĐANG LƯU TRÚ", dangLuuTru + " bé", "Khách sạn thú cưng", "box", Theme.ORANGE, Theme.ORANGE_BG);
        card4.update("CẦN THEO DÕI", canTheoDoi + " bé", "Đang điều trị / khám", "warning", Theme.RED, Theme.RED_BG);

        // Biểu đồ cột: Thú cưng theo sức khỏe & lưu trú
        barChart.clearData();
        barChart.setTitle("Phân bố thú cưng theo tình trạng chăm sóc");
        Map<String, Integer> skMap = bus.layThongKeSucKhoe();
        List<String> cats = new ArrayList<>(skMap.keySet());
        cats.add("Đang lưu trú");
        List<Double> vals = new ArrayList<>();
        for (String k : skMap.keySet()) vals.add((double) skMap.get(k));
        vals.add((double) dangLuuTru);
        barChart.setCategories(cats);
        barChart.addSeries("Số lượng thú cưng", Theme.ACCENT, vals);

        // Biểu đồ Donut: Phân hạng thẻ khách hàng
        donutChart.clearData();
        donutChart.setTitle("Cơ cấu Hạng thẻ Khách hàng thân thiết");
        Map<String, Integer> hangThe = bus.layCoCauHangThe();
        donutChart.addSlice("VIP", hangThe.getOrDefault("VIP", 0), new Color(0xE11D48));
        donutChart.addSlice("Vàng", hangThe.getOrDefault("Vàng", 0), new Color(0xF59E0B));
        donutChart.addSlice("Bạc", hangThe.getOrDefault("Bạc", 0), new Color(0x64748B));
        donutChart.addSlice("Thường", hangThe.getOrDefault("Thường", 0), new Color(0x94A3B8));
        donutChart.setCenterText(tongKH + "", "Khách");

        // Bảng chi tiết: Top khách hàng chi tiêu nhiều nhất
        String[] cols = {"MÃ KH", "HỌ TÊN KHÁCH HÀNG", "SỐ ĐIỆN THOẠI", "HẠNG THẺ", "ĐIỂM TÍCH LŨY", "THÚ CƯNG", "TỔNG CHI TIÊU"};
        allTableData.clear();
        for (KhachHang kh : bus.layTopKhachHangChiTieu(50)) {
            allTableData.add(new Object[]{
                    kh.getMaKH(),
                    kh.getHoTen(),
                    kh.getSoDienThoai(),
                    kh.getHangThe(),
                    kh.getDiemTichLuy(),
                    kh.getThuCung(),
                    NUM_FMT.format(kh.getTongChiTieu()) + " đ"
            });
        }
        tableModel.setColumns(cols);
    }

    private void updateTablePage() {
        int totalRows = allTableData.size();
        int pages = Math.max(1, (int) Math.ceil(totalRows / (double) PAGE_SIZE));
        trang = Math.min(Math.max(trang, 1), pages);

        int from = (trang - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, totalRows);

        List<Object[]> pageRows = totalRows > 0 ? allTableData.subList(from, to) : Collections.emptyList();
        tableModel.setRows(pageRows);

        lblFooter.setText(totalRows == 0
                ? "Không có dữ liệu trong khoảng thời gian đã chọn"
                : "Hiển thị " + (to - from) + " trong tổng số " + totalRows + " dòng dữ liệu");

        buildPager(pages);
    }

    private void buildPager(int pages) {
        pnlPages.removeAll();
        pnlPages.add(pageButton("‹", trang - 1, trang > 1, false));
        if (pages <= 7) {
            for (int p = 1; p <= pages; p++) pnlPages.add(pageButton(String.valueOf(p), p, true, p == trang));
        } else {
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
        l.setPreferredSize(new Dimension(24, 30));
        return l;
    }

    private RoundedButton pageButton(String text, int target, boolean enabled, boolean active) {
        RoundedButton b = active
                ? new RoundedButton(text, Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8)
                : new RoundedButton(text, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8);
        b.setFont(Theme.font(Font.PLAIN, 12));
        b.setPreferredSize(new Dimension(30, 30));
        b.setEnabled(enabled);
        b.addActionListener(e -> { trang = target; updateTablePage(); });
        return b;
    }

    // ---------------- 6. Xuất File Excel / CSV UTF-8 với BOM ----------------
    private void xuatExcel() {
        if (allTableData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất file!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Báo cáo");
        String defaultFileName = "BaoCao_" + currentTab.replace("&", "va").replace(" ", "_") + "_" + LocalDate.now() + ".csv";
        chooser.setSelectedFile(new File(defaultFileName));

        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (OutputStream os = new FileOutputStream(f);
                 Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                // Ghi UTF-8 BOM để Excel hiển thị tiếng Việt có dấu chuẩn
                os.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

                // Header
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write("\"" + tableModel.getColumnName(i) + "\"");
                    if (i < tableModel.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\r\n");

                // Rows
                for (Object[] row : allTableData) {
                    for (int i = 0; i < row.length; i++) {
                        String s = row[i] == null ? "" : String.valueOf(row[i]).replace("\"", "\"\"");
                        writer.write("\"" + s + "\"");
                        if (i < row.length - 1) writer.write(",");
                    }
                    writer.write("\r\n");
                }

                JOptionPane.showMessageDialog(this,
                        "Đã xuất thành công " + allTableData.size() + " dòng dữ liệu ra file:\n" + f.getAbsolutePath(),
                        "Xuất Báo Cáo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== LỚP CON GIAO DIỆN HỖ TRỢ ====================

    /** Thẻ KPI hiển thị số liệu tóm tắt */
    private static class KPICard extends RoundedPanel {
        private final JLabel lblTitle = new JLabel();
        private final JLabel lblValue = new JLabel();
        private final JLabel lblSubtitle = new JLabel();
        private final JLabel lblIcon = new JLabel();

        KPICard(String title, String val, String sub, String icon, Color valColor, Color iconBg) {
            super(16, Theme.CARD, Theme.BORDER);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(14, 16, 14, 16));

            lblTitle.setFont(Theme.font(Font.BOLD, 11));
            lblTitle.setForeground(Theme.MUTED);

            lblValue.setFont(Theme.font(Font.BOLD, 18));
            lblValue.setForeground(valColor);

            lblSubtitle.setFont(Theme.font(Font.PLAIN, 12));
            lblSubtitle.setForeground(Theme.MUTED);

            JPanel textPnl = new JPanel();
            textPnl.setOpaque(false);
            textPnl.setLayout(new BoxLayout(textPnl, BoxLayout.Y_AXIS));
            textPnl.add(lblTitle);
            textPnl.add(Box.createVerticalStrut(4));
            textPnl.add(lblValue);
            textPnl.add(Box.createVerticalStrut(3));
            textPnl.add(lblSubtitle);

            JPanel iconBox = new RoundedPanel(12, iconBg, null);
            iconBox.setPreferredSize(new Dimension(42, 42));
            iconBox.setLayout(new GridBagLayout());
            lblIcon.setIcon(Icons.of(icon, 20, valColor));
            iconBox.add(lblIcon);

            add(textPnl, BorderLayout.CENTER);
            add(iconBox, BorderLayout.EAST);

            update(title, val, sub, icon, valColor, iconBg);
        }

        void update(String title, String val, String sub, String icon, Color valColor, Color iconBg) {
            lblTitle.setText(title);
            lblValue.setText(val);
            lblValue.setForeground(valColor);
            lblSubtitle.setText(sub);
            lblIcon.setIcon(Icons.of(icon, 20, valColor));
            repaint();
        }
    }

    /** Model bảng dữ liệu chi tiết đa năng */
    private static class DetailedTableModel extends AbstractTableModel {
        private String[] cols = new String[0];
        private List<Object[]> rows = new ArrayList<>();

        void setColumns(String[] c) {
            this.cols = c;
            fireTableStructureChanged();
        }

        void setRows(List<Object[]> r) {
            this.rows = new ArrayList<>(r);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Object[] row = rows.get(r);
            return (c < row.length) ? row[c] : "";
        }
    }

    /** Renderer cho Header bảng */
    private static class TableHeaderCellRenderer extends JLabel implements TableCellRenderer {
        TableHeaderCellRenderer() {
            setOpaque(true);
            setFont(Theme.font(Font.BOLD, 11));
            setForeground(Theme.MUTED);
            setBackground(new Color(0xF8FAFC));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(String.valueOf(v));
            setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, Theme.ROW_LINE),
                    new EmptyBorder(0, 14, 0, 14)));
            return this;
        }
    }
}
