package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import Bus.ChuongBUS;
import Entity.Chuong;

/**
 * Giao diện Quản lý Lồng Chuồng lưu trú thú cưng (Pet Care Hotel & Boarding).
 * Thiết kế tương tự như phòng bàn ở project prj_spa nhưng tối ưu hoàn hảo cho hệ thống quản lý cửa hàng thú cưng,
 * chuẩn màu sắc và phong cách của PetCare.
 */
public class LongChuongPanel extends JPanel {
    private final ChuongBUS bus = new ChuongBUS();

    private JPanel pnlGrid;
    private JPanel selectedCageCard = null;
    private String selectedCageId = null;

    // Filters
    private JComboBox<String> cbTrangThai;
    private JComboBox<String> cbKichCo;
    private JComboBox<String> cbKhuVuc;
    private PlaceholderField txtTimKiem;

    // Mini stats labels
    private JLabel lblStatTong;
    private JLabel lblStatLuuTru;
    private JLabel lblStatTrong;
    private JLabel lblStatDaDat;
    private JLabel lblStatBaoTri;

    public LongChuongPanel() {
        super(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        add(buildTopHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        // Khởi động load danh sách lồng
        loadCages();
    }

    // ================= 1. TOP HEADER & STATS =================
    private JPanel buildTopHeader() {
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Lồng Chuồng Lưu Trú");
        lblTitle.setFont(Theme.font(Font.BOLD, 22));
        lblTitle.setForeground(Theme.TEXT);

        JLabel lblSub = new JLabel("Hệ thống phòng lưu trú thú cưng, chăm sóc dinh dưỡng và đặt giữ chỗ");
        lblSub.setFont(Theme.font(Font.PLAIN, 13));
        lblSub.setForeground(Theme.MUTED);

        JPanel pnlTitle = new JPanel();
        pnlTitle.setOpaque(false);
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.add(lblTitle);
        pnlTitle.add(Box.createVerticalStrut(2));
        pnlTitle.add(lblSub);

        titleRow.add(pnlTitle, BorderLayout.WEST);

        // Stats Cards Row: 5 thẻ cân đối chính xác
        JPanel pnlStats = new JPanel(new GridLayout(1, 5, 12, 0));
        pnlStats.setOpaque(false);

        lblStatTong = new JLabel("0");
        lblStatLuuTru = new JLabel("0");
        lblStatTrong = new JLabel("0");
        lblStatDaDat = new JLabel("0");
        lblStatBaoTri = new JLabel("0");

        pnlStats.add(createStatCard("Tổng số lồng", lblStatTong, "Quy mô hệ thống", Theme.SIDEBAR));
        pnlStats.add(createStatCard("Đang lưu trú", lblStatLuuTru, "Thú cưng đang ở", Theme.RED));
        pnlStats.add(createStatCard("Lồng trống", lblStatTrong, "Sẵn sàng đón bé", Theme.GREEN));
        pnlStats.add(createStatCard("Đã đặt hẹn", lblStatDaDat, "Lịch giữ trước", new Color(0xE67E22)));
        pnlStats.add(createStatCard("Bảo trì / Vệ sinh", lblStatBaoTri, "Khử trùng UV", new Color(0x7F8C8D)));

        top.add(titleRow, BorderLayout.NORTH);
        top.add(pnlStats, BorderLayout.CENTER);
        return top;
    }

    private JPanel createStatCard(String title, JLabel valLabel, String note, Color accentColor) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(Theme.font(Font.PLAIN, 12));
        lTitle.setForeground(Theme.MUTED);

        valLabel.setFont(Theme.font(Font.BOLD, 20));
        valLabel.setForeground(accentColor);

        JLabel lNote = new JLabel(note);
        lNote.setFont(Theme.font(Font.PLAIN, 11));
        lNote.setForeground(Theme.MUTED);

        card.add(lTitle);
        card.add(Box.createVerticalStrut(2));
        card.add(valLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(lNote);
        return card;
    }

    private void updateStats() {
        int tong = bus.demTongSoChuong();
        int luuTru = bus.demChuongTheoTrangThai("Đang lưu trú");
        int trong = bus.demChuongTheoTrangThai("Trống");
        int daDat = bus.demChuongTheoTrangThai("Đã đặt");
        int baoTri = bus.demChuongTheoTrangThai("Bảo trì");

        lblStatTong.setText(String.valueOf(tong));
        lblStatLuuTru.setText(String.valueOf(luuTru));
        lblStatTrong.setText(String.valueOf(trong));
        lblStatDaDat.setText(String.valueOf(daDat));
        lblStatBaoTri.setText(String.valueOf(baoTri));
    }

    // ================= 2. MAIN CONTENT (LEFT ACTION BAR + CENTER GRID) =================
    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(14, 0));
        content.setOpaque(false);

        // --- LEFT ACTION BAR ---
        content.add(buildLeftPanel(), BorderLayout.WEST);

        // --- CENTER FILTER & CAGES GRID ---
        content.add(buildCenterPanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setPreferredSize(new Dimension(210, 0));
        left.setOpaque(false);

        // Clock Panel: Đồng hồ số thời gian thực
        RoundedPanel pnlClock = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        pnlClock.setLayout(new GridLayout(2, 1, 0, 2));
        pnlClock.setBorder(new EmptyBorder(10, 12, 10, 12));
        pnlClock.setPreferredSize(new Dimension(210, 75));

        JLabel lblTime = new JLabel("00:00:00", JLabel.CENTER);
        lblTime.setFont(Theme.font(Font.BOLD, 22));
        lblTime.setForeground(Theme.DARK);

        JLabel lblDate = new JLabel("01/01/2026", JLabel.CENTER);
        lblDate.setFont(Theme.font(Font.PLAIN, 12));
        lblDate.setForeground(Theme.MUTED);

        pnlClock.add(lblTime);
        pnlClock.add(lblDate);
        left.add(pnlClock, BorderLayout.NORTH);

        Timer timer = new Timer(1000, e -> {
            lblTime.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            lblDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        });
        timer.start();

        // 8 Nút chức năng hành động (Phím tắt F4 - F11)
        JPanel pnlBtns = new JPanel(new GridLayout(8, 1, 0, 10));
        pnlBtns.setOpaque(false);

        RoundedButton btnDatTruoc = createActionButton("Đặt lồng trước (F4)", Theme.ACCENT, Theme.ACCENT_HOVER);
        RoundedButton btnNhanLong = createActionButton("Nhận lồng / Pet (F5)", Theme.GREEN, new Color(0x278C44));
        RoundedButton btnHuyDat   = createActionButton("Hủy đặt hẹn (F6)", new Color(0xCB4335), new Color(0xB03A2E));
        RoundedButton btnXemCT    = createActionButton("Xem chi tiết (F7)", Theme.SIDEBAR, new Color(0x4C6877));
        RoundedButton btnChuyen   = createActionButton("Chuyển lồng (F8)", new Color(0x7F8C8D), new Color(0x707B7C));
        RoundedButton btnChamSoc  = createActionButton("Chăm sóc / Care (F9)", new Color(0x2980B9), new Color(0x2471A3));
        RoundedButton btnDonLong  = createActionButton("Trả & Dọn lồng (F10)", new Color(0x8E44AD), new Color(0x7D3C98));
        RoundedButton btnDanhSach = createActionButton("Danh sách đặt (F11)", new Color(0x34495E), new Color(0x2C3E50));

        // Sự kiện các nút
        btnDatTruoc.addActionListener(e -> moDialogDatTruoc());
        btnNhanLong.addActionListener(e -> moDialogNhanLong());
        btnHuyDat.addActionListener(e -> thucHienHuyDat());
        btnXemCT.addActionListener(e -> moDialogXemChiTiet());
        btnChuyen.addActionListener(e -> moDialogChuyenLong());
        btnChamSoc.addActionListener(e -> moDialogChamSoc());
        btnDonLong.addActionListener(e -> thucHienDonLong());
        btnDanhSach.addActionListener(e -> moDialogDanhSachDat());

        pnlBtns.add(btnDatTruoc);
        pnlBtns.add(btnNhanLong);
        pnlBtns.add(btnHuyDat);
        pnlBtns.add(btnXemCT);
        pnlBtns.add(btnChuyen);
        pnlBtns.add(btnChamSoc);
        pnlBtns.add(btnDonLong);
        pnlBtns.add(btnDanhSach);

        left.add(pnlBtns, BorderLayout.CENTER);

        // Gắn KeyBindings (F4 - F11) cho toàn bộ màn hình
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("F4"), "F4");
        am.put("F4", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnDatTruoc.doClick(); } });

        im.put(KeyStroke.getKeyStroke("F5"), "F5");
        am.put("F5", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnNhanLong.doClick(); } });

        im.put(KeyStroke.getKeyStroke("F6"), "F6");
        am.put("F6", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnHuyDat.doClick(); } });

        im.put(KeyStroke.getKeyStroke("F7"), "F7");
        am.put("F7", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnXemCT.doClick(); } });

        im.put(KeyStroke.getKeyStroke("F8"), "F8");
        am.put("F8", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnChuyen.doClick(); } });

        im.put(KeyStroke.getKeyStroke("F9"), "F9");
        am.put("F9", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnChamSoc.doClick(); } });

        im.put(KeyStroke.getKeyStroke("F10"), "F10");
        am.put("F10", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnDonLong.doClick(); } });

        im.put(KeyStroke.getKeyStroke("F11"), "F11");
        am.put("F11", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { btnDanhSach.doClick(); } });

        return left;
    }

    private RoundedButton createActionButton(String text, Color bg, Color hover) {
        RoundedButton btn = new RoundedButton(text, bg, hover, Color.WHITE, null, 10);
        btn.setFont(Theme.font(Font.BOLD, 12));
        btn.pad(10, 8);
        return btn;
    }

    // ================= 3. CENTER PANEL (FILTER + CAGES GRID) =================
    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);

        // Thanh Bộ Lọc
        RoundedPanel pnlFilter = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        pnlFilter.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));

        Font lblFont = Theme.font(Font.BOLD, 13);
        Font compFont = Theme.font(Font.PLAIN, 13);

        JLabel lTT = new JLabel("Trạng thái:");
        lTT.setFont(lblFont);
        lTT.setForeground(Theme.TEXT);
        pnlFilter.add(lTT);

        cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Trống", "Đang lưu trú", "Đã đặt", "Bảo trì"});
        cbTrangThai.setFont(compFont);
        cbTrangThai.setPreferredSize(new Dimension(130, 32));
        cbTrangThai.addActionListener(e -> loadCages());
        pnlFilter.add(cbTrangThai);

        JLabel lSize = new JLabel("Kích cỡ:");
        lSize.setFont(lblFont);
        lSize.setForeground(Theme.TEXT);
        pnlFilter.add(lSize);

        cbKichCo = new JComboBox<>(new String[]{"Tất cả", "Nhỏ", "Vừa", "Lớn", "VIP"});
        cbKichCo.setFont(compFont);
        cbKichCo.setPreferredSize(new Dimension(100, 32));
        cbKichCo.addActionListener(e -> loadCages());
        pnlFilter.add(cbKichCo);

        JLabel lKV = new JLabel("Khu vực:");
        lKV.setFont(lblFont);
        lKV.setForeground(Theme.TEXT);
        pnlFilter.add(lKV);

        cbKhuVuc = new JComboBox<>(new String[]{"Tất cả", "Khu Mèo", "Khu Chó Nhỏ", "Khu Chó Lớn", "Khu VIP"});
        cbKhuVuc.setFont(compFont);
        cbKhuVuc.setPreferredSize(new Dimension(125, 32));
        cbKhuVuc.addActionListener(e -> loadCages());
        pnlFilter.add(cbKhuVuc);

        JLabel lTim = new JLabel("Tìm kiếm:");
        lTim.setFont(lblFont);
        lTim.setForeground(Theme.TEXT);
        pnlFilter.add(lTim);

        txtTimKiem = new PlaceholderField("Mã lồng, tên thú cưng, SĐT...");
        txtTimKiem.setFont(compFont);
        txtTimKiem.setPreferredSize(new Dimension(150, 32));
        txtTimKiem.addActionListener(e -> loadCages());
        pnlFilter.add(txtTimKiem);

        RoundedButton btnTim = new RoundedButton("Tìm", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(14, 6);
        btnTim.addActionListener(e -> loadCages());
        pnlFilter.add(btnTim);

        RoundedButton btnLamMoi = new RoundedButton("Làm mới", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(14, 6);
        btnLamMoi.addActionListener(e -> {
            cbTrangThai.setSelectedIndex(0);
            cbKichCo.setSelectedIndex(0);
            cbKhuVuc.setSelectedIndex(0);
            txtTimKiem.setText("");
            loadCages();
        });
        pnlFilter.add(btnLamMoi);

        center.add(pnlFilter, BorderLayout.NORTH);

        // Lưới hiển thị các thẻ Lồng Chuồng
        pnlGrid = new JPanel(new GridLayout(0, 4, 12, 14));
        pnlGrid.setOpaque(false);
        pnlGrid.setBorder(new EmptyBorder(8, 4, 14, 4));

        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.add(pnlGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(pnlWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        center.add(scroll, BorderLayout.CENTER);
        return center;
    }

    // ================= 4. LOAD & TẠO CARD LỒNG CHUỒNG =================
    public void loadCages() {
        pnlGrid.removeAll();
        selectedCageCard = null;
        selectedCageId = null;

        String tt = cbTrangThai != null ? (String) cbTrangThai.getSelectedItem() : "Tất cả";
        String sz = cbKichCo != null ? (String) cbKichCo.getSelectedItem() : "Tất cả";
        String kv = cbKhuVuc != null ? (String) cbKhuVuc.getSelectedItem() : "Tất cả";
        String kw = txtTimKiem != null ? txtTimKiem.getText().trim() : "";

        List<Chuong> list = bus.locChuong(tt, sz, kv, kw);

        for (Chuong c : list) {
            pnlGrid.add(createCageCard(c));
        }

        updateStats();
        pnlGrid.revalidate();
        pnlGrid.repaint();
    }

    /**
     * Tạo một Card lồng chuồng hiển thị hình vẽ vector lồng, thông tin thú cưng và trạng thái.
     */
    private JPanel createCageCard(Chuong c) {
        Color statusColor;
        String statusLabel = c.getTrangThai();

        if (c.isTrong()) {
            statusColor = Theme.GREEN;
        } else if (c.isDaDat()) {
            statusColor = new Color(0xE67E22);
        } else if (c.isDangLuuTru()) {
            statusColor = Theme.RED;
        } else {
            statusColor = new Color(0x95A5A6);
        }

        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth() - 4;
                int h = getHeight() - 4;
                boolean hovered = Boolean.TRUE.equals(getClientProperty("hovered"));
                boolean selected = Boolean.TRUE.equals(getClientProperty("selected"));

                // Đổ bóng mềm
                if (hovered && !selected) {
                    g2.setColor(new Color(0, 0, 0, 25));
                    g2.fillRoundRect(3, 3, w, h, 16, 16);
                } else if (selected) {
                    g2.setColor(new Color(0x56, 0x78, 0x8A, 60));
                    g2.fillRoundRect(3, 3, w, h, 16, 16);
                } else {
                    g2.setColor(new Color(0, 0, 0, 10));
                    g2.fillRoundRect(2, 2, w, h, 16, 16);
                }

                // Thẻ trắng
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 16, 16);

                // Viền chọn hoặc viền mặc định
                if (selected) {
                    g2.setColor(Theme.ACCENT);
                    g2.setStroke(new BasicStroke(2.8f));
                    g2.drawRoundRect(0, 0, w, h, 16, 16);
                } else if (hovered) {
                    g2.setColor(Theme.ACCENT_HOVER);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, w, h, 16, 16);
                } else {
                    g2.setColor(Theme.BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, w, h, 16, 16);
                }
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 12, 12));
        card.putClientProperty("cageId", c.getMaChuong());

        // Header thẻ: Size + VIP Badge
        JPanel cardHead = new JPanel(new BorderLayout());
        cardHead.setOpaque(false);

        JLabel lblSizeKhu = new JLabel(c.getSize() + " • " + c.getKhuVuc());
        lblSizeKhu.setFont(Theme.font(Font.PLAIN, 11));
        lblSizeKhu.setForeground(Theme.MUTED);
        cardHead.add(lblSizeKhu, BorderLayout.WEST);

        if (c.isVip()) {
            JLabel lblVip = new JLabel("⭐ VIP");
            lblVip.setFont(Theme.font(Font.BOLD, 11));
            lblVip.setForeground(new Color(0xD4AC0D));
            cardHead.add(lblVip, BorderLayout.EAST);
        }
        card.add(cardHead, BorderLayout.NORTH);

        // Center thẻ: Vẽ hình minh họa Lồng Chuồng vector chất lượng cao theo màu trạng thái
        JComponent cageGraphic = new JComponent() {
            { setPreferredSize(new Dimension(140, 95)); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cw = getWidth();
                int ch = getHeight();

                // Tọa độ lồng chuồng
                int boxW = 86;
                int boxH = 68;
                int bx = (cw - boxW) / 2;
                int by = (ch - boxH) / 2 + 2;

                // Nền lồng chuồng nhẹ
                g2.setColor(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 20));
                g2.fillRoundRect(bx, by, boxW, boxH, 12, 12);

                // Khung viền ngoài lồng
                g2.setColor(statusColor);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(bx, by, boxW, boxH, 12, 12);

                // Mái vòm / Tay xách trên nóc lồng
                g2.draw(new Arc2D.Double(bx + boxW / 2.0 - 14, by - 8, 28, 16, 0, 180, Arc2D.OPEN));

                // Các nan sắt lồng chuồng
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int bars = 4;
                for (int i = 1; i <= bars; i++) {
                    int barX = bx + (boxW * i) / (bars + 1);
                    g2.drawLine(barX, by + 4, barX, by + boxH - 4);
                }

                // Dấu chân thú cưng (Paw) ở giữa cửa lồng
                Icons.draw(g2, "paw", bx + (boxW - 24) / 2.0, by + (boxH - 24) / 2.0, 24, statusColor, true);

                g2.dispose();
            }
        };
        card.add(cageGraphic, BorderLayout.CENTER);

        // Footer thẻ: Thông tin mã lồng, tên thú cưng và trạng thái
        JPanel cardFoot = new JPanel();
        cardFoot.setLayout(new BoxLayout(cardFoot, BoxLayout.Y_AXIS));
        cardFoot.setOpaque(false);

        JLabel lblMa = new JLabel(c.getMaChuong() + ": " + c.getTenChuong(), JLabel.CENTER);
        lblMa.setFont(Theme.font(Font.BOLD, 14));
        lblMa.setForeground(Theme.TEXT);
        lblMa.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardFoot.add(lblMa);

        cardFoot.add(Box.createVerticalStrut(2));

        // Thông tin thú cưng hoặc giá
        JLabel lblDetail;
        if (c.isDangLuuTru()) {
            lblDetail = new JLabel(" " + (c.getTenPet() != null ? c.getTenPet() : "Thú cưng") + " (" + (c.getLoaiPet() != null ? c.getLoaiPet() : "") + ")");
            lblDetail.setForeground(new Color(0x922B21));
            lblDetail.setFont(Theme.font(Font.BOLD, 12));
        } else if (c.isDaDat()) {
            lblDetail = new JLabel(" Đã giữ chỗ cho: " + (c.getTenPet() != null ? c.getTenPet() : "Khách"));
            lblDetail.setForeground(new Color(0xB9770E));
            lblDetail.setFont(Theme.font(Font.PLAIN, 12));
        } else if (c.isBaoTri()) {
            lblDetail = new JLabel(" Đang vệ sinh / khử trùng");
            lblDetail.setForeground(Color.GRAY);
            lblDetail.setFont(Theme.font(Font.ITALIC, 11));
        } else {
            lblDetail = new JLabel(Theme.money((long) c.getGiaTheoNgay()) + "/ngày");
            lblDetail.setForeground(Theme.MUTED);
            lblDetail.setFont(Theme.font(Font.PLAIN, 12));
        }
        lblDetail.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardFoot.add(lblDetail);

        cardFoot.add(Box.createVerticalStrut(4));

        // Dòng trạng thái (Chấm tròn màu + chữ trạng thái)
        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        pnlStatus.setOpaque(false);

        JComponent dot = new JComponent() {
            { setPreferredSize(new Dimension(8, 8)); }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(statusColor);
                g2.fillOval(0, 0, 8, 8);
                g2.dispose();
            }
        };
        pnlStatus.add(dot);

        JLabel lblSt = new JLabel(statusLabel);
        lblSt.setFont(Theme.font(Font.BOLD, 12));
        lblSt.setForeground(statusColor);
        pnlStatus.add(lblSt);

        cardFoot.add(pnlStatus);

        card.add(cardFoot, BorderLayout.SOUTH);

        // Sự kiện chuột: Hover, Click chọn, Double Click mở chi tiết
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.putClientProperty("hovered", true);
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.putClientProperty("hovered", false);
                card.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                card.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedCageCard != null && selectedCageCard != card) {
                    selectedCageCard.putClientProperty("selected", false);
                    selectedCageCard.repaint();
                }
                selectedCageCard = card;
                selectedCageId = c.getMaChuong();
                card.putClientProperty("selected", true);
                card.repaint();

                // Double click: Mở nhanh chi tiết lồng
                if (e.getClickCount() == 2) {
                    moDialogXemChiTiet();
                }
            }
        });

        return card;
    }

    // ================= 5. CÁC HÀNH ĐỘNG / DIALOG CALLS =================

    /** F4: Đặt lồng trước */
    public void moDialogDatTruoc() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        new DialogDatChuong(owner, bus, selectedCageId, this::loadCages).setVisible(true);
    }

    /** F5: Nhận lồng / Check-in */
    public void moDialogNhanLong() {
        if (selectedCageId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng click chọn một lồng chuồng trên màn hình!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chuong c = bus.timTheoMa(selectedCageId);
        if (c == null) return;

        if (c.isDangLuuTru()) {
            JOptionPane.showMessageDialog(this, "Lồng " + c.getMaChuong() + " hiện đang có bé \"" + c.getTenPet() + "\" lưu trú rồi!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (c.isBaoTri()) {
            JOptionPane.showMessageDialog(this, "Lồng " + c.getMaChuong() + " đang bảo trì / vệ sinh, không thể nhận thú cưng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        if (c.isDaDat()) {
            // Mở Dialog nhận lồng nhanh từ thông tin đã đặt
            new DialogChiTietChuong(owner, bus, selectedCageId, this::loadCages).setVisible(true);
        } else {
            // Lồng đang trống: Mở dialog đặt & nhận trực tiếp
            new DialogDatChuong(owner, bus, selectedCageId, this::loadCages).setVisible(true);
        }
    }

    /** F6: Hủy đặt lồng */
    public void thucHienHuyDat() {
        if (selectedCageId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lồng đã đặt để hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chuong c = bus.timTheoMa(selectedCageId);
        if (c == null || !c.isDaDat()) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể hủy khi trạng thái lồng đang là \"Đã đặt\"!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int r = JOptionPane.showConfirmDialog(this,
                "Xác nhận hủy đặt chỗ cho lồng " + c.getMaChuong() + " (Khách: " + c.getTenKhach() + ")?",
                "Hủy đặt lồng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (r == JOptionPane.YES_OPTION) {
            bus.huyDat(selectedCageId);
            JOptionPane.showMessageDialog(this, "Đã hủy đặt chỗ lồng " + selectedCageId + " thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            loadCages();
        }
    }

    /** F7: Xem chi tiết */
    public void moDialogXemChiTiet() {
        if (selectedCageId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lồng chuồng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        new DialogChiTietChuong(owner, bus, selectedCageId, this::loadCages).setVisible(true);
    }

    /** F8: Chuyển lồng */
    public void moDialogChuyenLong() {
        if (selectedCageId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lồng cần chuyển thú cưng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chuong c = bus.timTheoMa(selectedCageId);
        if (c == null || c.isTrong()) {
            JOptionPane.showMessageDialog(this, "Lồng này đang trống, không có thú cưng để chuyển!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        new DialogChuyenChuong(owner, bus, selectedCageId, this::loadCages).setVisible(true);
    }

    /** F9: Chăm sóc & Dịch vụ (tương tự Gọi món) */
    public void moDialogChamSoc() {
        if (selectedCageId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lồng thú cưng cần thêm dịch vụ chăm sóc!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chuong c = bus.timTheoMa(selectedCageId);
        if (c == null || !c.isDangLuuTru()) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể thêm dịch vụ / thức ăn khi lồng đang có thú cưng lưu trú!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        new DialogChamSocDichVu(owner, bus, selectedCageId, this::loadCages).setVisible(true);
    }

    /** F10: Trả & Dọn lồng */
    public void thucHienDonLong() {
        if (selectedCageId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lồng cần trả / dọn dẹp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chuong c = bus.timTheoMa(selectedCageId);
        if (c == null) return;

        if (c.isTrong()) {
            JOptionPane.showMessageDialog(this, "Lồng này đã ở trạng thái Trống sạch sẽ rồi!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (c.isDangLuuTru()) {
            // Mở hộp thoại thanh toán trước khi dọn lồng
            Window owner = SwingUtilities.getWindowAncestor(this);
            new DialogChiTietChuong(owner, bus, selectedCageId, this::loadCages).setVisible(true);
            return;
        }

        // Nếu là Đã đặt hoặc Bảo trì: cho phép chuyển về Trống ngay
        int r = JOptionPane.showConfirmDialog(this,
                "Xác nhận hoàn tất vệ sinh / dọn dẹp lồng " + c.getMaChuong() + " và chuyển về trạng thái Trống?",
                "Dọn lồng", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            bus.donChuong(selectedCageId);
            JOptionPane.showMessageDialog(this, "Đã dọn dẹp và làm sạch lồng " + selectedCageId + " thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            loadCages();
        }
    }

    /** F11: Danh sách đặt hẹn */
    public void moDialogDanhSachDat() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        new DialogDanhSachDatChuong(owner, bus, this::loadCages).setVisible(true);
    }
}
