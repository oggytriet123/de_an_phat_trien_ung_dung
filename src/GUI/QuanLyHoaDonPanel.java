package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import Bus.HoaDonBUS;
import Entity.HoaDon;

/**
 * Màn hình "Quản lý hóa đơn & Xử lý quá tải giờ cao điểm":
 * - 20 ô đơn chờ thanh toán / đang xử lý tại quầy (5 cột x 4 hàng).
 * - Dashboard theo dõi thời gian thực: Tổng đơn chờ, Cảnh báo đơn chờ quá 15 phút, Đơn Pre-order.
 * - Dynamic Queue: Cơ chế phân luồng lọc nhanh và tự động tính toán thời gian chờ.
 * - Cảnh báo trực quan viền đỏ / badge khi có đơn chờ vượt ngưỡng 15 phút.
 */
public class QuanLyHoaDonPanel extends JPanel implements Refreshable {
    private static final int COT = 5;

    private final HoaDonBUS bus;
    private final IntConsumer moO;
    private final IntConsumer xoaO;
    private final JLabel lblThongBao = new JLabel(" ");
    private Timer hen;
    private Timer timerDemPhut;

    // Các thẻ KPI Dashboard theo dõi hàng đợi
    private final DashboardCard cardTongCho = new DashboardCard("ĐƠN ĐANG CHỜ", "0 / 20", "0% công suất quầy", Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final DashboardCard cardQuaHan = new DashboardCard("CẢNH BÁO > 15 PHÚT", "0 đơn", "Hàng đợi an toàn", Theme.SUCCESS, Theme.GREEN_BG);
    private final DashboardCard cardPreOrder = new DashboardCard("ĐƠN PRE-ORDER", "0 đơn", "Đặt trước lấy tại quầy", Theme.ACCENT, Theme.BLUE_BG);
    private final DashboardCard cardSanSang = new DashboardCard("Ô TRỐNG KHẢ DỤNG", "20 ô", "Sẵn sàng tiếp nhận", Theme.MUTED, Theme.BADGE_GRAY_BG);

    // Banner cảnh báo quá tải giờ cao điểm
    private final JPanel pnlBannerQuaTai = new JPanel(new BorderLayout(12, 0));
    private final JLabel lblAlertText = new JLabel();

    // Bộ lọc hàng đợi động (Dynamic Queue Filter)
    private String boLocHienTai = "TẤT CẢ";
    private final List<RoundedButton> filterChips = new ArrayList<>();

    public QuanLyHoaDonPanel(HoaDonBUS bus, IntConsumer moO, IntConsumer xoaO, Runnable taoMoi) {
        super(new BorderLayout(0, 14));
        this.bus = bus;
        this.moO = moO;
        this.xoaO = xoaO;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        add(buildHeaderSection(taoMoi), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(HoaDonBUS.SO_O / COT, COT, 16, 16));
        grid.setOpaque(false);
        for (int i = 0; i < HoaDonBUS.SO_O; i++) {
            grid.add(new OTile(i));
        }

        ScrollPanel view = new ScrollPanel(new BorderLayout()) {
            @Override public boolean getScrollableTracksViewportHeight() {
                return getParent() instanceof JViewport vp && vp.getHeight() >= getPreferredSize().height;
            }
        };
        view.setOpaque(false);
        view.add(grid, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        SlimScrollBarUI.apply(sp);
        add(sp, BorderLayout.CENTER);

        JLabel foot = new JLabel("Hóa đơn đã lưu sẽ luôn nằm trong ô này cho đến khi được thanh toán hoặc hủy. "
                + "• Hệ thống tự động tính thời gian chờ và cảnh báo khi đơn vượt quá 15 phút.");
        foot.setFont(Theme.font(Font.PLAIN, 12));
        foot.setForeground(Theme.MUTED);
        foot.setBorder(new EmptyBorder(4, 0, 0, 0));
        add(foot, BorderLayout.SOUTH);

        // Khởi tạo timer cập nhật định kỳ mỗi 15 giây
        timerDemPhut = new Timer(15000, e -> {
            capNhatDashboard();
            repaint();
        });
        timerDemPhut.start();

        capNhatDashboard();
    }

    // =========================================================================
    // 1. KHU VỰC HEADER VÀ DASHBOARD ĐƠN CHỜ
    // =========================================================================

    private JComponent buildHeaderSection(Runnable taoMoi) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Dòng 1: Tiêu đề + Nút tạo mới
        JLabel t = new JLabel("Quản lý hóa đơn & Xử lý quá tải tại quầy");
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);

        JLabel s = new JLabel("Dashboard theo dõi đơn chờ thời gian thực, điều phối Pre-order & cảnh báo ngưỡng 15 phút.");
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(t);
        left.add(Box.createVerticalStrut(4));
        left.add(s);

        RoundedButton btnTaoMoi = new RoundedButton("+   Tạo hóa đơn mới", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btnTaoMoi.setPreferredSize(new Dimension(218, 42));
        btnTaoMoi.addActionListener(e -> taoMoi.run());

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(left, BorderLayout.WEST);
        titleRow.add(btnTaoMoi, BorderLayout.EAST);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(titleRow);
        container.add(Box.createVerticalStrut(14));

        // Dòng 2: Banner cảnh báo quá tải giờ cao điểm
        pnlBannerQuaTai.setOpaque(true);
        pnlBannerQuaTai.setBackground(Theme.RED_BG);
        pnlBannerQuaTai.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, Theme.RED),
                new EmptyBorder(8, 14, 8, 14)));
        pnlBannerQuaTai.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAlertText.setFont(Theme.font(Font.BOLD, 13));
        lblAlertText.setForeground(Theme.DANGER);
        pnlBannerQuaTai.add(lblAlertText, BorderLayout.WEST);
        pnlBannerQuaTai.setVisible(false); // Chỉ hiện khi quá tải
        container.add(pnlBannerQuaTai);
        container.add(Box.createVerticalStrut(12));

        // Dòng 3: Dải 4 thẻ KPI Dashboard
        JPanel pnlStats = new JPanel(new GridLayout(1, 4, 14, 0));
        pnlStats.setOpaque(false);
        pnlStats.add(cardTongCho);
        pnlStats.add(cardQuaHan);
        pnlStats.add(cardPreOrder);
        pnlStats.add(cardSanSang);
        pnlStats.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(pnlStats);
        container.add(Box.createVerticalStrut(14));

        // Dòng 4: Bộ lọc hàng đợi động (Dynamic Queue Filter) + Chú thích màu
        JPanel pnlFilterAndLegend = new JPanel(new BorderLayout());
        pnlFilterAndLegend.setOpaque(false);
        pnlFilterAndLegend.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlChips.setOpaque(false);
        pnlChips.add(createFilterChip("TẤT CẢ"));
        pnlChips.add(createFilterChip("⚠️ QUÁ HẠN >15P"));
        pnlChips.add(createFilterChip("📦 PRE-ORDER"));
        pnlChips.add(createFilterChip("CHỜ BÌNH THƯỜNG"));
        pnlChips.add(createFilterChip("Ô TRỐNG"));

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        legend.setOpaque(false);
        legend.add(legendItem("Ô trống", Color.WHITE, Theme.BORDER));
        legend.add(legendItem("Chờ bình thường", Theme.AMBER_BG, Theme.AMBER_BORDER));
        legend.add(legendItem("Quá hạn >15p", Theme.RED_BG, Theme.RED));
        legend.add(legendItem("Pre-order", Theme.BLUE_BG, Theme.ACCENT));

        lblThongBao.setFont(Theme.font(Font.BOLD, 12));
        lblThongBao.setForeground(Theme.SUCCESS);

        pnlFilterAndLegend.add(pnlChips, BorderLayout.WEST);
        pnlFilterAndLegend.add(legend, BorderLayout.EAST);
        container.add(pnlFilterAndLegend);
        container.add(Box.createVerticalStrut(6));

        return container;
    }

    private RoundedButton createFilterChip(String label) {
        boolean active = label.equals(boLocHienTai);
        RoundedButton b = active
                ? new RoundedButton(label, Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8)
                : new RoundedButton(label, Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        b.setFont(Theme.font(Font.PLAIN, 12));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 10, 30));
        b.addActionListener(e -> {
            boLocHienTai = label;
            for (RoundedButton chip : filterChips) {
                boolean on = chip.getText().equals(boLocHienTai);
                if (on) chip.style(Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null);
                else chip.style(Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER);
            }
            repaint();
        });
        filterChips.add(b);
        return b;
    }

    private static JComponent legendItem(String text, Color fill, Color line) {
        Icon sq = new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillRoundRect(x, y, 14, 14, 4, 4);
                g2.setColor(line);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y, 13, 13, 4, 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 15; }
            @Override public int getIconHeight() { return 15; }
        };
        JLabel l = new JLabel(text, sq, SwingConstants.LEFT);
        l.setIconTextGap(6);
        l.setFont(Theme.font(Font.PLAIN, 11));
        l.setForeground(Theme.MUTED);
        return l;
    }

    // =========================================================================
    // 2. TÍNH TOÁN DỮ LIỆU DASHBOARD & CẢNH BÁO QUÁ HẠN
    // =========================================================================

    public void capNhatDashboard() {
        int soDonDangCho = 0;
        int soQuaHan15p = 0;
        int soPreOrder = 0;
        long maxWait = 0;

        for (int i = 0; i < HoaDonBUS.SO_O; i++) {
            HoaDon h = bus.layO(i);
            if (h != null) {
                soDonDangCho++;
                long phut = h.getSoPhutCho();
                if (phut > maxWait) maxWait = phut;
                if (h.isQuaHan15Phut()) soQuaHan15p++;
                if (h.isPreOrder()) soPreOrder++;
            }
        }

        int oTrong = HoaDonBUS.SO_O - soDonDangCho;
        int tiLeTai = (int) Math.round((soDonDangCho / 20.0) * 100);

        cardTongCho.set(soDonDangCho + " / 20", tiLeTai + "% công suất quầy",
                tiLeTai >= 75 ? Theme.DANGER : (tiLeTai >= 50 ? Theme.ORANGE : Theme.PRIMARY));

        if (soQuaHan15p > 0) {
            cardQuaHan.set(soQuaHan15p + " đơn", "⚠️ Vượt ngưỡng 15p!", Theme.DANGER);
            cardQuaHan.setBackground(Theme.RED_BG);
        } else {
            cardQuaHan.set("0 đơn", "Hàng đợi an toàn", Theme.SUCCESS);
            cardQuaHan.setBackground(Theme.GREEN_BG);
        }

        cardPreOrder.set(soPreOrder + " đơn", "Chờ nhận tại quầy", Theme.ACCENT);
        cardSanSang.set(oTrong + " ô", oTrong <= 3 ? "Sắp hết chỗ!" : "Sẵn sàng nhận thêm",
                oTrong <= 3 ? Theme.DANGER : Theme.MUTED);

        // Hiển thị hoặc ẩn banner cảnh báo quá tải
        if (soQuaHan15p > 0 || tiLeTai >= 75) {
            StringBuilder sb = new StringBuilder("🚨 CẢNH BÁO CAO ĐIỂM: ");
            if (soQuaHan15p > 0) {
                sb.append("Có ").append(soQuaHan15p).append(" đơn chờ quá 15 phút (lâu nhất: ").append(maxWait).append("p)! ");
            }
            if (tiLeTai >= 75) {
                sb.append("Công suất hàng đợi đạt ").append(tiLeTai).append("%! ");
            }
            sb.append("Đề xuất: Kích hoạt Dynamic Queue, phân luồng khách Pre-order / Quẹt thẻ nhanh.");
            lblAlertText.setText(sb.toString());
            pnlBannerQuaTai.setVisible(true);
        } else {
            pnlBannerQuaTai.setVisible(false);
        }

        revalidate();
    }

    public void thongBao(String text) {
        lblThongBao.setText(text);
        if (hen != null) hen.stop();
        hen = new Timer(6000, e -> lblThongBao.setText(" "));
        hen.setRepeats(false);
        hen.start();
    }

    @Override public void lamMoi() {
        bus.taiTuCSDL();
        capNhatDashboard();
        repaint();
    }

    // =========================================================================
    // 3. THẺ THỐNG KÊ DASHBOARD CARD NHỎ GỌN
    // =========================================================================

    private static class DashboardCard extends RoundedPanel {
        private final JLabel lblVal = new JLabel("—");
        private final JLabel lblSub = new JLabel(" ");

        DashboardCard(String title, String val, String sub, Color textColor, Color bgColor) {
            super(10, Color.WHITE, Theme.BORDER);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(10, 14, 10, 14));

            JLabel t = new JLabel(title);
            t.setFont(Theme.font(Font.BOLD, 10));
            t.setForeground(Theme.MUTED);

            lblVal.setText(val);
            lblVal.setFont(Theme.font(Font.BOLD, 18));
            lblVal.setForeground(textColor);

            lblSub.setText(sub);
            lblSub.setFont(Theme.font(Font.PLAIN, 11));
            lblSub.setForeground(Theme.MUTED);

            add(t);
            add(Box.createVerticalStrut(4));
            add(lblVal);
            add(Box.createVerticalStrut(3));
            add(lblSub);
        }

        void set(String val, String sub, Color color) {
            lblVal.setText(val);
            lblVal.setForeground(color);
            lblSub.setText(sub);
        }
    }

    // =========================================================================
    // 4. Ô HÓA ĐƠN THÔNG MINH (OTile - TÍCH HỢP WAIT TIME & PRE-ORDER)
    // =========================================================================

    private class OTile extends JPanel {
        private final int viTri;
        private boolean hover;
        private boolean hoverXoa;

        private Rectangle vungXoa() {
            return new Rectangle(getWidth() - 14 - 26, 10, 26, 26);
        }

        OTile(int viTri) {
            this.viTri = viTri;
            setOpaque(false);
            setPreferredSize(new Dimension(150, 126));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; hoverXoa = false; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        hienThiMenuNguCanh(e.getPoint());
                        return;
                    }
                    if (!SwingUtilities.isLeftMouseButton(e) || !contains(e.getPoint())) return;
                    if (bus.layO(viTri) != null && vungXoa().contains(e.getPoint())) {
                        xoaO.accept(viTri);
                    } else {
                        moO.accept(viTri);
                    }
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    boolean tren = bus.layO(viTri) != null && vungXoa().contains(e.getPoint());
                    if (tren != hoverXoa) {
                        hoverXoa = tren;
                        setToolTipText(tren ? "Xóa hóa đơn này" : null);
                        repaint();
                    }
                }
            });
        }

        private void hienThiMenuNguCanh(Point p) {
            HoaDon hd = bus.layO(viTri);
            if (hd == null) return;

            JPopupMenu popup = new JPopupMenu();
            JMenuItem mMo = new JMenuItem("Mở xử lý hóa đơn " + hd.getMaHD());
            mMo.addActionListener(e -> moO.accept(viTri));
            popup.add(mMo);

            JMenuItem mPreOrder = new JCheckBoxMenuItem("Đánh dấu đơn Pre-order", hd.isPreOrder());
            mPreOrder.addActionListener(e -> {
                hd.setPreOrder(!hd.isPreOrder());
                capNhatDashboard();
                repaint();
            });
            popup.add(mPreOrder);

            popup.addSeparator();
            JMenuItem mXoa = new JMenuItem("Xóa hóa đơn khỏi ô này");
            mXoa.setForeground(Theme.DANGER);
            mXoa.addActionListener(e -> xoaO.accept(viTri));
            popup.add(mXoa);

            popup.show(this, p.x, p.y);
        }

        @Override protected void paintComponent(Graphics g) {
            HoaDon hd = bus.layO(viTri);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 14;

            boolean khớpLọc = kiemTraKhopBoLoc(hd);
            if (!khớpLọc) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            }

            boolean quaHan = hd != null && hd.isQuaHan15Phut();
            boolean preOrder = hd != null && hd.isPreOrder();

            // Màu nền và viền thẻ ô
            Color nen = hd == null ? Color.WHITE : (quaHan ? Theme.RED_BG : (preOrder ? Theme.BLUE_BG : Theme.AMBER_BG));
            Color vien = hover ? Theme.PRIMARY : (hd == null ? Theme.BORDER : (quaHan ? Theme.RED : (preOrder ? Theme.ACCENT : Theme.AMBER_BORDER)));

            g2.setColor(nen);
            g2.fillRoundRect(0, 0, w, h, 10, 10);

            g2.setColor(vien);
            g2.setStroke(new BasicStroke(quaHan ? 2.0f : 1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Số thứ tự ô
            g2.setFont(Theme.font(Font.BOLD, 11));
            g2.setColor(Theme.MUTED);
            g2.drawString(String.format("Ô %02d", viTri + 1), pad, 22);

            if (hd == null) {
                // Ô trống
                int cx = w / 2, cy = h / 2 - 6;
                g2.setColor(new Color(0x8795A0));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 8, cy, cx + 8, cy);
                g2.drawLine(cx, cy - 8, cx, cy + 8);
                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.setColor(Theme.MUTED);
                FontMetrics fm = g2.getFontMetrics();
                String s = "Nhấn để tạo hóa đơn";
                g2.drawString(s, (w - fm.stringWidth(s)) / 2, cy + 36);
            } else {
                long phutCho = hd.getSoPhutCho();

                // Nút dấu x xóa hóa đơn
                Rectangle x = vungXoa();
                if (hoverXoa) {
                    g2.setColor(new Color(0xFBE3E3));
                    g2.fillOval(x.x, x.y, x.width, x.height);
                }
                g2.setColor(hoverXoa ? Theme.DANGER : new Color(0x9AA6B0));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x.x + 9, x.y + 9, x.x + 17, x.y + 17);
                g2.drawLine(x.x + 17, x.y + 9, x.x + 9, x.y + 17);

                // Badge thời gian chờ
                String strWait = (phutCho >= 15 ? "⚠️ " : "⏱ ") + phutCho + "p";
                g2.setFont(Theme.font(Font.BOLD, 10));
                FontMetrics fmWait = g2.getFontMetrics();
                int ww = fmWait.stringWidth(strWait) + 12;
                int wx = x.x - ww - 6, wy = 12;
                g2.setColor(quaHan ? Theme.RED : (phutCho > 8 ? Theme.ORANGE : Theme.GREEN));
                g2.fillRoundRect(wx, wy, ww, 18, 9, 9);
                g2.setColor(Color.WHITE);
                g2.drawString(strWait, wx + 6, wy + 13);

                // Mã hóa đơn
                g2.setFont(Theme.font(Font.BOLD, 17));
                g2.setColor(quaHan ? Theme.DANGER : Theme.TEXT);
                g2.drawString(hd.getMaHD(), pad, 48);

                // Tên khách hàng
                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.setColor(Theme.TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(cat(hd.getTenKH(), fm, w - pad * 2), pad, 68);

                // Tổng tiền
                g2.setFont(Theme.font(Font.BOLD, 14));
                g2.setColor(Theme.TEXT);
                g2.drawString(Theme.money(hd.canThanhToan()), pad, 89);

                // Badge trạng thái / Pre-order ở góc dưới
                g2.setFont(Theme.font(Font.BOLD, 10));
                fm = g2.getFontMetrics();
                int pw = fm.stringWidth(hd.getTrangThai()) + 16, ph = 18, py = h - 26;
                g2.setColor(Theme.AMBER_PILL);
                g2.fillRoundRect(pad, py, pw, ph, ph, ph);
                g2.setColor(Theme.AMBER_TEXT);
                g2.drawString(hd.getTrangThai(), pad + 8, py + 13);

                if (preOrder) {
                    int pPreW = fm.stringWidth("Pre-order") + 16;
                    int pPreX = pad + pw + 6;
                    g2.setColor(Theme.BLUE_BG);
                    g2.fillRoundRect(pPreX, py, pPreW, ph, ph, ph);
                    g2.setColor(Theme.BLUE_TEXT);
                    g2.drawString("Pre-order", pPreX + 8, py + 13);
                }
            }
            g2.dispose();
        }

        private boolean kiemTraKhopBoLoc(HoaDon hd) {
            if ("TẤT CẢ".equals(boLocHienTai)) return true;
            if ("Ô TRỐNG".equals(boLocHienTai)) return hd == null;
            if (hd == null) return false;
            if ("⚠️ QUÁ HẠN >15P".equals(boLocHienTai)) return hd.isQuaHan15Phut();
            if ("📦 PRE-ORDER".equals(boLocHienTai)) return hd.isPreOrder();
            if ("CHỜ BÌNH THƯỜNG".equals(boLocHienTai)) return !hd.isQuaHan15Phut() && !hd.isPreOrder();
            return true;
        }
    }

    private static String cat(String s, FontMetrics fm, int maxW) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= maxW) return s;
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n).trim() + "...") > maxW) n--;
        return s.substring(0, n).trim() + "...";
    }
}
