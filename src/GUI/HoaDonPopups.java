package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.HoaDonBUS;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.MaGiamGia;
import Entity.SanPham;

/** Các popup của nhóm màn hình hóa đơn. Mỗi popup phủ lên trang hiện tại (xem {@link Overlay}). */
final class HoaDonPopups {
    private HoaDonPopups() {}

    private static final int PAD = 36;
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =====================================================================
    // Nhập mã giảm giá
    // =====================================================================
    static void maGiamGia(Component host, MaGiamGia hienTai, MaGiamGiaBUS bus, Consumer<MaGiamGia> onApply) {
        moMaGiamGia(host, hienTai == null ? "" : hienTai.getMa(), bus, onApply);
    }

    /** Popup chọn mã: các mã có sẵn là từng nút bấm chọn, hoặc nhập mã khác, hoặc tạo mã mới. */
    private static void moMaGiamGia(Component host, String maChon, MaGiamGiaBUS bus, Consumer<MaGiamGia> onApply) {
        List<MaGiamGia> ds = bus.layDanhSach();
        int hang = Math.max(1, (ds.size() + 1) / 2);
        int caoDs = Math.min(3, hang) * 74;
        RoundedPanel card = card(620, 390 + caoDs, "Nhập mã giảm giá",
                "Chọn mã khuyến mãi có sẵn hoặc nhập mã khác để áp dụng cho hóa đơn.");

        PlaceholderField tf = new PlaceholderField("Nhập mã giảm giá");
        tf.setText(maChon);
        FieldBox box = new FieldBox(tf, Color.WHITE, 42);
        JLabel err = errorLabel();

        // ---- các mã có sẵn dạng nút
        List<CodeCard> cards = new ArrayList<>();
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setOpaque(false);
        if (ds.isEmpty()) {
            JLabel trong = new JLabel("Chưa có mã giảm giá nào. Thêm mã tại Danh Mục > Quản lý dịch vụ.");
            trong.setFont(Theme.font(Font.PLAIN, 12));
            trong.setForeground(Theme.MUTED);
            grid.setLayout(new BorderLayout());
            grid.add(trong, BorderLayout.NORTH);
        }
        for (MaGiamGia m : ds) {
            CodeCard cc = new CodeCard(m, bus.daHetHan(m), bus.chuaDenHan(m));
            cc.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) {
                    if (cc.khongDungDuoc() || !SwingUtilities.isLeftMouseButton(e) || !cc.contains(e.getPoint())) return;
                    tf.setText(cc.m.getMa());
                    tf.requestFocusInWindow();
                }
            });
            cards.add(cc);
            grid.add(cc);
        }
        Runnable dongBo = () -> {
            String cur = tf.getText().trim();
            for (CodeCard c : cards) c.setChon(!c.khongDungDuoc() && c.m.getMa().equalsIgnoreCase(cur));
        };
        tf.getDocument().addDocumentListener(clearError(err, box));
        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { dongBo.run(); }
            @Override public void removeUpdate(DocumentEvent e) { dongBo.run(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });
        dongBo.run();

        ScrollPanel view = new ScrollPanel(new BorderLayout());
        view.setOpaque(false);
        view.add(grid, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(0, caoDs));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, caoDs));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        SlimScrollBarUI.apply(sp);

        JLabel l1 = label("Mã khuyến mãi có sẵn");
        JLabel l2 = label("Hoặc nhập mã khác");
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 0, 0, 0));
        for (JComponent c : new JComponent[] {l1, box, l2, err}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        body.add(l1);
        body.add(Box.createVerticalStrut(10));
        body.add(sp);
        body.add(Box.createVerticalStrut(16));
        body.add(l2);
        body.add(Box.createVerticalStrut(8));
        body.add(box);
        body.add(Box.createVerticalStrut(6));
        body.add(err);
        card.add(body, BorderLayout.CENTER);

        RoundedButton boQua = secondary("Bỏ qua", 110);
        RoundedButton apDung = primary("Áp dụng", 150);
        Runnable dong = () -> Overlay.dong(host);
        Runnable ap = () -> {
            try {
                MaGiamGia m = bus.kiemTra(tf.getText());
                Overlay.dong(host);
                onApply.accept(m);
            } catch (IllegalArgumentException ex) {
                err.setText(ex.getMessage());
                box.setError(true);
                tf.requestFocusInWindow();
            }
        };
        boQua.addActionListener(e -> dong.run());
        apDung.addActionListener(e -> ap.run());
        tf.addActionListener(e -> ap.run());
        card.add(buttons(boQua, apDung), BorderLayout.SOUTH);

        Overlay.hien(host, card, dong);
        SwingUtilities.invokeLater(tf::requestFocusInWindow);
    }

    /** Nút chọn một mã giảm giá có sẵn. Mã hết hạn hoặc chưa đến ngày áp dụng bị làm mờ và không chọn được. */
    private static final class CodeCard extends JPanel {
        final MaGiamGia m;
        final boolean hetHan;
        final boolean chuaDen;
        private boolean chon;

        CodeCard(MaGiamGia m, boolean hetHan, boolean chuaDen) {
            this.m = m;
            this.hetHan = hetHan;
            this.chuaDen = chuaDen;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 64));
            if (!khongDungDuoc()) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        boolean khongDungDuoc() {
            return hetHan || chuaDen;
        }

        void setChon(boolean c) {
            if (chon != c) { chon = c; repaint(); }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(khongDungDuoc() ? Theme.FIELD_BG : chon ? Theme.PRIMARY_SOFT : Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(chon ? Theme.PRIMARY : Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

            g2.setFont(Theme.font(Font.BOLD, 14));
            g2.setColor(khongDungDuoc() ? Theme.MUTED : Theme.TEXT);
            g2.drawString(m.getMa(), 14, 26);
            g2.setFont(Theme.font(Font.PLAIN, 12));
            String tu = NGAY.format(m.getNgayBatDau());
            String han = NGAY.format(m.getHanDung());
            if (hetHan) {
                g2.setColor(Theme.DANGER);
                g2.drawString("Hết hạn " + han, 14, 46);
            } else if (chuaDen) {
                g2.setColor(Theme.AMBER_TEXT);
                g2.drawString("Áp dụng từ " + tu + " - " + han, 14, 46);
            } else {
                g2.setColor(Theme.MUTED);
                g2.drawString(m.moTa() + "  ·  " + tu + " - " + han, 14, 46);
            }
            if (chon) {
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = w - 24, cy = 24;
                g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
                g2.drawLine(cx - 1, cy + 4, cx + 6, cy - 4);
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // Tạo mã giảm giá
    // =====================================================================
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NHAP_NGAY =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Số tiền / phần trăm nhập vào; -1 nếu không phải số. */
    static long docSo(String s) {
        String t = s == null ? "" : s.trim();
        if (!t.matches("\\d[\\d.,\\s]*")) return -1;
        String d = t.replaceAll("\\D", "");
        return d.isEmpty() || d.length() > 12 ? -1 : Long.parseLong(d);
    }

    private static JButton nutLich(JComponent invoker, PlaceholderField tfTuNgay, PlaceholderField tfDenNgay) {
        JButton btn = new JButton(Icons.of("calendar", 16, Theme.MUTED));
        btn.setBorder(new EmptyBorder(0, 8, 0, 10));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Chọn khoảng ngày (Từ ngày — Đến ngày)");
        btn.addActionListener(e -> {
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusDays(7);
            try { start = LocalDate.parse(tfTuNgay.getText().trim(), NHAP_NGAY); } catch (Exception ignored) {}
            try { end = LocalDate.parse(tfDenNgay.getText().trim(), NHAP_NGAY); } catch (Exception ignored) {}
            CalendarPicker.showDateRangePicker(invoker, start, end, (tu, den) -> {
                tfTuNgay.setText(NGAY.format(tu));
                tfDenNgay.setText(NGAY.format(den));
            });
        });
        return btn;
    }

    /**
     * Form tạo mã giảm giá mới (hỗ trợ chọn Từ ngày -> Đến ngày).
     * @param onCreated gọi sau khi tạo thành công
     * @param onBack    gọi khi bấm "Quay lại" hoặc Esc
     */
    static void taoMaGiamGia(Component host, MaGiamGiaBUS bus, Consumer<MaGiamGia> onCreated, Runnable onBack) {
        RoundedPanel card = card(600, 595, "Tạo mã giảm giá", "Mã mới sẽ xuất hiện trong danh sách mã có sẵn.");

        PlaceholderField tfMa = new PlaceholderField("Ví dụ: SALE20");
        PlaceholderField tfGia = new PlaceholderField("Ví dụ: 15");
        PlaceholderField tfTuNgay = new PlaceholderField("dd/MM/yyyy");
        PlaceholderField tfDenNgay = new PlaceholderField("dd/MM/yyyy");
        tfTuNgay.setText(NGAY.format(LocalDate.now()));
        tfDenNgay.setText(NGAY.format(LocalDate.now().plusDays(30)));
        GioiHanNhap.ap(tfMa, 20, "[A-Za-z0-9_-]");
        GioiHanNhap.ap(tfGia, 12, "[0-9.,]");
        GioiHanNhap.ap(tfTuNgay, 10, "[0-9/]");
        GioiHanNhap.ap(tfDenNgay, 10, "[0-9/]");
        FieldBox bMa = new FieldBox(tfMa, Color.WHITE, 42);
        FieldBox bGia = new FieldBox(tfGia, Color.WHITE, 42);
        FieldBox bTuNgay = new FieldBox(tfTuNgay, Color.WHITE, 42);
        FieldBox bDenNgay = new FieldBox(tfDenNgay, Color.WHITE, 42);
        bTuNgay.add(nutLich(bTuNgay, tfTuNgay, tfDenNgay), BorderLayout.EAST);
        bDenNgay.add(nutLich(bDenNgay, tfTuNgay, tfDenNgay), BorderLayout.EAST);
        JLabel err = errorLabel();

        boolean[] phanTram = {true};
        MethodCard cPt = new MethodCard("Giảm theo %", true);
        MethodCard cTien = new MethodCard("Giảm theo số tiền", false);
        MouseAdapter chonLoai = new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                MethodCard src = (MethodCard) e.getSource();
                if (!SwingUtilities.isLeftMouseButton(e) || !src.contains(e.getPoint())) return;
                phanTram[0] = src == cPt;
                cPt.setChon(phanTram[0]);
                cTien.setChon(!phanTram[0]);
                tfGia.setText("");
                tfGia.setPlaceholder(phanTram[0] ? "Ví dụ: 15 (tức 15%)" : "Ví dụ: 50000 (tức 50.000₫)");
                err.setText(" ");
            }
        };
        cPt.addMouseListener(chonLoai);
        cTien.addMouseListener(chonLoai);
        tfGia.setPlaceholder("Ví dụ: 15 (tức 15%)");
        for (PlaceholderField f : new PlaceholderField[] {tfMa, tfGia, tfTuNgay, tfDenNgay}) {
            f.getDocument().addDocumentListener(new DocumentListener() {
                private void c() {
                    err.setText(" ");
                    bMa.setError(false);
                    bGia.setError(false);
                    bTuNgay.setError(false);
                    bDenNgay.setError(false);
                }
                @Override public void insertUpdate(DocumentEvent e) { c(); }
                @Override public void removeUpdate(DocumentEvent e) { c(); }
                @Override public void changedUpdate(DocumentEvent e) { }
            });
        }

        JComponent gMa = nhom("Mã giảm giá *", bMa);
        JPanel hangNgay = new JPanel(new GridLayout(1, 2, 14, 0));
        hangNgay.setOpaque(false);
        hangNgay.add(nhom("Từ ngày *", bTuNgay));
        hangNgay.add(nhom("Đến ngày *", bDenNgay));
        JPanel loai = new JPanel(new GridLayout(1, 2, 12, 0));
        loai.setOpaque(false);
        loai.add(cPt);
        loai.add(cTien);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 0, 0, 0));
        JLabel lLoai = label("Loại giảm giá");
        JComponent gGia = nhom("Giá trị giảm *", bGia);
        for (JComponent c : new JComponent[] {gMa, hangNgay, lLoai, loai, gGia, err}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        gMa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        hangNgay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        loai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        gGia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        body.add(gMa);
        body.add(Box.createVerticalStrut(14));
        body.add(hangNgay);
        body.add(Box.createVerticalStrut(14));
        body.add(lLoai);
        body.add(Box.createVerticalStrut(8));
        body.add(loai);
        body.add(Box.createVerticalStrut(14));
        body.add(gGia);
        body.add(Box.createVerticalStrut(6));
        body.add(err);
        card.add(body, BorderLayout.CENTER);

        RoundedButton quayLai = secondary("Quay lại", 130);
        RoundedButton tao = primary("Tạo mã", 150);
        quayLai.addActionListener(e -> onBack.run());
        Runnable xong = () -> {
            long gt = docSo(tfGia.getText());
            if (gt < 0) {
                err.setText("Giá trị giảm phải là số.");
                bGia.setError(true);
                tfGia.requestFocusInWindow();
                return;
            }
            LocalDate tuNgay;
            try {
                tuNgay = LocalDate.parse(tfTuNgay.getText().trim(), NHAP_NGAY);
            } catch (DateTimeParseException ex) {
                err.setText("Ngày bắt đầu không hợp lệ. Nhập theo dạng dd/MM/yyyy.");
                bTuNgay.setError(true);
                tfTuNgay.requestFocusInWindow();
                return;
            }
            LocalDate denNgay;
            try {
                denNgay = LocalDate.parse(tfDenNgay.getText().trim(), NHAP_NGAY);
            } catch (DateTimeParseException ex) {
                err.setText("Ngày kết thúc không hợp lệ. Nhập theo dạng dd/MM/yyyy.");
                bDenNgay.setError(true);
                tfDenNgay.requestFocusInWindow();
                return;
            }
            try {
                MaGiamGia m = bus.tao(tfMa.getText(), phanTram[0], gt, tuNgay, denNgay);
                onCreated.accept(m);
            } catch (IllegalArgumentException ex) {
                String msg = ex.getMessage();
                err.setText(msg);
                if (msg.contains("Mã") || msg.contains("mã")) bMa.setError(true);
                else if (msg.contains("bắt đầu")) bTuNgay.setError(true);
                else if (msg.contains("kết thúc") || msg.contains("Hạn")) bDenNgay.setError(true);
                else bGia.setError(true);
            }
        };
        tao.addActionListener(e -> xong.run());
        for (PlaceholderField f : new PlaceholderField[] {tfMa, tfGia, tfTuNgay, tfDenNgay}) f.addActionListener(e -> xong.run());
        card.add(buttons(quayLai, tao), BorderLayout.SOUTH);

        Overlay.hien(host, card, onBack);
        SwingUtilities.invokeLater(tfMa::requestFocusInWindow);
    }

    /** Nhãn + ô nhập xếp dọc. */
    static JComponent nhom(String nhan, JComponent o) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label(nhan), BorderLayout.NORTH);
        p.add(o, BorderLayout.CENTER);
        return p;
    }

    // =====================================================================
    // Tạo nhóm dịch vụ / loại hàng mới
    // =====================================================================
    /**
     * @param coDinh    TRUE: tạo nhóm dịch vụ; FALSE: tạo loại sản phẩm; null: cho chọn dịch vụ hay sản phẩm
     * @param onCreated gọi với tên nhóm vừa tạo
     * @param onBack    gọi khi bấm "Quay lại" hoặc Esc
     */
    static void taoDanhMuc(Component host, SanPhamBUS bus, Boolean coDinh, Consumer<String> onCreated, Runnable onBack) {
        boolean chon = coDinh == null;
        String tieuDe = chon ? "Tạo loại hàng mới" : coDinh ? "Tạo nhóm dịch vụ mới" : "Tạo loại sản phẩm mới";
        RoundedPanel card = card(580, chon ? 450 : 330, tieuDe,
                "Nhóm mới xuất hiện ở bộ lọc trang hóa đơn và trong trang quản lý.");

        PlaceholderField tfTen = new PlaceholderField(coDinh != null && coDinh
                ? "Ví dụ: Huấn luyện, Chụp ảnh thú cưng" : "Ví dụ: Phụ kiện, Quần áo, Vệ sinh");
        GioiHanNhap.ap(tfTen, 30, null);
        FieldBox bTen = new FieldBox(tfTen, Color.WHITE, 42);
        JLabel err = errorLabel();
        JLabel goiY = new JLabel(" ");
        goiY.setFont(Theme.font(Font.PLAIN, 12));
        goiY.setForeground(Theme.MUTED);

        boolean[] dichVu = {coDinh != null && coDinh};
        MethodCard cSp = new MethodCard("Sản phẩm", !dichVu[0]);
        MethodCard cDv = new MethodCard("Dịch vụ", dichVu[0]);
        Runnable capNhatGoiY = () -> {
            String ten = tfTen.getText().trim();
            goiY.setText(ten.isEmpty() ? " " : "Mã sẽ có dạng: " + bus.tienToChoLoaiMoi(ten) + "-001");
        };
        MouseAdapter chonKieu = new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                MethodCard src = (MethodCard) e.getSource();
                if (!SwingUtilities.isLeftMouseButton(e) || !src.contains(e.getPoint())) return;
                dichVu[0] = src == cDv;
                cSp.setChon(!dichVu[0]);
                cDv.setChon(dichVu[0]);
            }
        };
        cSp.addMouseListener(chonKieu);
        cDv.addMouseListener(chonKieu);
        tfTen.getDocument().addDocumentListener(new DocumentListener() {
            private void c() { err.setText(" "); bTen.setError(false); capNhatGoiY.run(); }
            @Override public void insertUpdate(DocumentEvent e) { c(); }
            @Override public void removeUpdate(DocumentEvent e) { c(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 0, 0, 0));
        JComponent gTen = nhom(coDinh != null && coDinh ? "Tên nhóm dịch vụ *" : "Tên loại hàng *", bTen);
        for (JComponent c : new JComponent[] {gTen, goiY, err}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        gTen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        body.add(gTen);
        body.add(Box.createVerticalStrut(6));
        body.add(goiY);
        if (chon) {   // chỉ khi được phép chọn: hỏi đây là sản phẩm hay dịch vụ
            JPanel kieu = new JPanel(new GridLayout(1, 2, 12, 0));
            kieu.setOpaque(false);
            kieu.add(cSp);
            kieu.add(cDv);
            JLabel note = new JLabel("Dịch vụ hiển thị \"DV:\" và tính theo lần; sản phẩm hiển thị \"SKU:\" và tính theo cái.");
            note.setFont(Theme.font(Font.PLAIN, 11));
            note.setForeground(Theme.MUTED);
            JLabel lKieu = label("Loại hàng này là");
            for (JComponent c : new JComponent[] {lKieu, kieu, note}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
            kieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            body.add(Box.createVerticalStrut(14));
            body.add(lKieu);
            body.add(Box.createVerticalStrut(8));
            body.add(kieu);
            body.add(Box.createVerticalStrut(8));
            body.add(note);
        }
        body.add(Box.createVerticalStrut(6));
        body.add(err);
        card.add(body, BorderLayout.CENTER);

        RoundedButton quayLai = secondary("Quay lại", 130);
        RoundedButton tao = primary(chon ? "Tạo loại" : "Tạo nhóm", 150);
        quayLai.addActionListener(e -> onBack.run());
        Runnable xong = () -> {
            try {
                String ten = bus.themDanhMuc(tfTen.getText(), dichVu[0]);
                onCreated.accept(ten);
            } catch (IllegalArgumentException ex) {
                err.setText(ex.getMessage());
                bTen.setError(true);
                tfTen.requestFocusInWindow();
            }
        };
        tao.addActionListener(e -> xong.run());
        tfTen.addActionListener(e -> xong.run());
        card.add(buttons(quayLai, tao), BorderLayout.SOUTH);

        Overlay.hien(host, card, onBack);
        SwingUtilities.invokeLater(tfTen::requestFocusInWindow);
    }

    // =====================================================================
    // Thanh toán
    // =====================================================================
    static void thanhToan(Component host, String maHD, long tong, Consumer<String> onConfirm) {
        thanhToan(host, maHD, tong, (pt, tk) -> onConfirm.accept(pt));
    }

    static void thanhToan(Component host, String maHD, long tong, java.util.function.BiConsumer<String, Long> onConfirm) {
        RoundedPanel card = card(580, 560, "Thanh toán hóa đơn", "Chọn phương thức thanh toán cho hóa đơn " + maHD + ".");

        JLabel tl = new JLabel("Tổng thanh toán");
        tl.setFont(Theme.font(Font.PLAIN, 12));
        tl.setForeground(Theme.MUTED);
        JLabel amount = new JLabel(Theme.money(tong));
        amount.setFont(Theme.font(Font.BOLD, 26));
        amount.setForeground(Theme.TEXT);

        String[] huongDan = {
            "Nhập số tiền khách đưa để hệ thống tự động tính tiền thừa trả lại.",
            "Xác nhận sau khi giao dịch quẹt thẻ thành công.",
            "Phương thức QR MoMo (Đang phát triển / Thử nghiệm) - Vui lòng kiểm tra và xác nhận thủ công.",
            "Phương thức QR ZaloPay (Đang phát triển / Thử nghiệm) - Vui lòng kiểm tra và xác nhận thủ công.",
        };
        JLabel hint = new JLabel(huongDan[0]);
        hint.setFont(Theme.font(Font.PLAIN, 12));
        hint.setForeground(Theme.MUTED);

        String[] chon = {HoaDonBUS.PHUONG_THUC[0]};
        List<MethodCard> cards = new ArrayList<>();
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);

        RoundedButton dong = secondary("Đóng", 150);
        RoundedButton xacNhan = primary("Xác nhận thanh toán", 210);

        // Panel nhập tiền mặt & tính tiền thừa
        JPanel pnlTienMat = new JPanel();
        pnlTienMat.setOpaque(false);
        pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS));
        pnlTienMat.setAlignmentX(Component.LEFT_ALIGNMENT);

        long[] tienKhachDua = {tong};
        JLabel lblTienKhach = new JLabel("Tiền khách đưa (₫):");
        lblTienKhach.setFont(Theme.font(Font.BOLD, 12));
        lblTienKhach.setForeground(Theme.TEXT);

        JTextField tfTienKhach = new JTextField(String.valueOf(tong));
        tfTienKhach.setFont(Theme.font(Font.BOLD, 15));
        tfTienKhach.setPreferredSize(new Dimension(520, 36));
        tfTienKhach.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblTienTra = new JLabel("Tiền trả lại khách: 0₫");
        lblTienTra.setFont(Theme.font(Font.BOLD, 13));
        lblTienTra.setForeground(Theme.GREEN);

        Runnable capNhatTienTra = () -> {
            try {
                String raw = tfTienKhach.getText().replaceAll("[^0-9]", "");
                long k = raw.isEmpty() ? 0 : Long.parseLong(raw);
                tienKhachDua[0] = k;
                long tra = k - tong;
                if (tra >= 0) {
                    lblTienTra.setText("Tiền trả lại khách: " + Theme.money(tra));
                    lblTienTra.setForeground(Theme.GREEN);
                    xacNhan.setEnabled(true);
                } else {
                    lblTienTra.setText("⚠ Khách đưa thiếu: " + Theme.money(-tra));
                    lblTienTra.setForeground(Theme.RED);
                    xacNhan.setEnabled(false);
                }
            } catch (Exception ex) {
                lblTienTra.setText("Số tiền không hợp lệ");
                lblTienTra.setForeground(Theme.RED);
                xacNhan.setEnabled(false);
            }
        };

        tfTienKhach.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { capNhatTienTra.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { capNhatTienTra.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { capNhatTienTra.run(); }
        });

        JPanel pnlGoiY = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        pnlGoiY.setOpaque(false);
        long[] goiY = {tong, ((tong + 9999) / 10000) * 10000, ((tong + 49999) / 50000) * 50000, ((tong + 99999) / 100000) * 100000, 500000, 1000000};
        java.util.Set<Long> daThem = new java.util.LinkedHashSet<>();
        daThem.add(tong);
        for (long v : goiY) {
            if (v >= tong && daThem.add(v) && daThem.size() <= 4) {
                String txt = (v == tong) ? "Đúng tiền" : Theme.money(v);
                RoundedButton btnY = new RoundedButton(txt, Color.WHITE, new Color(0xF1F5F9), Theme.PRIMARY, Theme.BORDER, 6).pad(8, 4);
                btnY.setFont(Theme.font(Font.PLAIN, 11));
                btnY.addActionListener(ev -> {
                    tfTienKhach.setText(String.valueOf(v));
                    capNhatTienTra.run();
                });
                pnlGoiY.add(btnY);
            }
        }

        pnlTienMat.add(Box.createVerticalStrut(6));
        pnlTienMat.add(lblTienKhach);
        pnlTienMat.add(Box.createVerticalStrut(4));
        pnlTienMat.add(tfTienKhach);
        pnlTienMat.add(Box.createVerticalStrut(4));
        pnlTienMat.add(pnlGoiY);
        pnlTienMat.add(Box.createVerticalStrut(2));
        pnlTienMat.add(lblTienTra);

        // Panel QR động chuẩn EMVCo VietQR có nhúng sẵn số tiền & mã hóa đơn
        JPanel pnlMoMo = new JPanel();
        pnlMoMo.setOpaque(false);
        pnlMoMo.setLayout(new BoxLayout(pnlMoMo, BoxLayout.Y_AXIS));
        pnlMoMo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMoMo.setVisible(false);

        // Nhãn trạng thái (Đang phát triển / Thử nghiệm)
        JLabel lblBetaBadge = new JLabel("⚠ Cổng thanh toán QR (Đang phát triển / Thử nghiệm)");
        lblBetaBadge.setFont(Theme.font(Font.BOLD, 12));
        lblBetaBadge.setForeground(Theme.AMBER_TEXT);
        lblBetaBadge.setOpaque(true);
        lblBetaBadge.setBackground(Theme.AMBER_PILL);
        lblBetaBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.AMBER_BORDER, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        lblBetaBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        QRPanel qrComp = new QRPanel(VietQRUtil.taoAnhQR(tong, maHD, 185));
        qrComp.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAutoAmt = new JLabel("✓ Quét mã tự động hiện số tiền " + Theme.money(tong) + " (Nội dung: TT " + maHD + ")");
        lblAutoAmt.setFont(Theme.font(Font.BOLD, 12));
        lblAutoAmt.setForeground(Theme.GREEN);
        lblAutoAmt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTimer = new JLabel("⏱ Mã QR có hiệu lực trong: 01:00");
        lblTimer.setFont(Theme.font(Font.BOLD, 13));
        lblTimer.setForeground(new Color(0x0284C7));
        lblTimer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Khung hướng dẫn xác nhận thủ công
        JPanel boxHuongDan = new JPanel();
        boxHuongDan.setLayout(new BoxLayout(boxHuongDan, BoxLayout.Y_AXIS));
        boxHuongDan.setBackground(Theme.AMBER_BG);
        boxHuongDan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.AMBER_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        boxHuongDan.setAlignmentX(Component.CENTER_ALIGNMENT);
        boxHuongDan.setMaximumSize(new Dimension(520, 90));

        JLabel lblHdTitle = new JLabel("Hướng dẫn xác nhận thủ công:");
        lblHdTitle.setFont(Theme.font(Font.BOLD, 12));
        lblHdTitle.setForeground(Theme.AMBER_TEXT);

        JLabel lblHdStep1 = new JLabel("1. Khách quét mã QR trên ứng dụng MoMo / ZaloPay / Ngân hàng để chuyển khoản.");
        lblHdStep1.setFont(Theme.font(Font.PLAIN, 11));
        lblHdStep1.setForeground(Theme.TEXT);

        JLabel lblHdStep2 = new JLabel("2. Thu ngân kiểm tra thông báo nhận tiền hoặc màn hình giao dịch thành công của khách.");
        lblHdStep2.setFont(Theme.font(Font.PLAIN, 11));
        lblHdStep2.setForeground(Theme.TEXT);

        JLabel lblHdStep3 = new JLabel("3. Nhấn nút \"Xác nhận thủ công\" bên dưới để hoàn tất thanh toán và xuất hóa đơn.");
        lblHdStep3.setFont(Theme.font(Font.BOLD, 11));
        lblHdStep3.setForeground(Theme.PRIMARY);

        boxHuongDan.add(lblHdTitle);
        boxHuongDan.add(Box.createVerticalStrut(3));
        boxHuongDan.add(lblHdStep1);
        boxHuongDan.add(lblHdStep2);
        boxHuongDan.add(lblHdStep3);

        pnlMoMo.add(Box.createVerticalStrut(6));
        pnlMoMo.add(lblBetaBadge);
        pnlMoMo.add(Box.createVerticalStrut(6));
        pnlMoMo.add(qrComp);
        pnlMoMo.add(Box.createVerticalStrut(4));
        pnlMoMo.add(lblAutoAmt);
        pnlMoMo.add(Box.createVerticalStrut(2));
        pnlMoMo.add(lblTimer);
        pnlMoMo.add(Box.createVerticalStrut(6));
        pnlMoMo.add(boxHuongDan);

        int[] giayConLai = {60};
        Timer[] timerHolder = new Timer[1];
        timerHolder[0] = new Timer(1000, e -> {
            giayConLai[0]--;
            if (giayConLai[0] > 0) {
                lblTimer.setText("⏱ Mã QR có hiệu lực trong: " + String.format("%02d:%02d", giayConLai[0] / 60, giayConLai[0] % 60));
                if (giayConLai[0] <= 15) {
                    lblTimer.setForeground(Theme.RED);
                } else {
                    lblTimer.setForeground(new Color(0x0284C7));
                }
            } else {
                timerHolder[0].stop();
                Overlay.dong(host);
                JOptionPane.showMessageDialog(host,
                    "Thanh toán không thành công!\nMã QR đã hết hạn (quá 1 phút).\nVui lòng thực hiện lại giao dịch.",
                    "Thanh toán thất bại",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        for (int i = 0; i < HoaDonBUS.PHUONG_THUC.length; i++) {
            MethodCard mc = new MethodCard(HoaDonBUS.PHUONG_THUC[i], i == 0);
            int idx = i;
            mc.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e) || !mc.contains(e.getPoint())) return;
                    for (MethodCard c : cards) c.setChon(c == mc);
                    chon[0] = mc.ten;
                    hint.setText(huongDan[idx]);

                    if ("MoMo".equalsIgnoreCase(mc.ten) || "ZaloPay".equalsIgnoreCase(mc.ten)) {
                        card.setPreferredSize(new Dimension(580, 760));
                        pnlTienMat.setVisible(false);
                        pnlMoMo.setVisible(true);
                        xacNhan.setText("Xác nhận thủ công");
                        xacNhan.setEnabled(true);
                        giayConLai[0] = 60;
                        lblTimer.setText("⏱ Mã QR có hiệu lực trong: 01:00");
                        lblTimer.setForeground(new Color(0x0284C7));
                        timerHolder[0].restart();
                    } else if ("Tiền mặt".equalsIgnoreCase(mc.ten)) {
                        timerHolder[0].stop();
                        pnlMoMo.setVisible(false);
                        pnlTienMat.setVisible(true);
                        card.setPreferredSize(new Dimension(580, 560));
                        xacNhan.setText("Xác nhận thanh toán");
                        capNhatTienTra.run();
                    } else {
                        timerHolder[0].stop();
                        pnlMoMo.setVisible(false);
                        pnlTienMat.setVisible(false);
                        card.setPreferredSize(new Dimension(580, 455));
                        xacNhan.setText("Xác nhận thanh toán");
                        xacNhan.setEnabled(true);
                    }
                    card.revalidate();
                    card.repaint();
                    JRootPane rp = SwingUtilities.getRootPane(host);
                    if (rp != null) rp.repaint();
                }
            });
            cards.add(mc);
            grid.add(mc);
        }
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 0, 0, 0));
        for (JComponent c : new JComponent[] {tl, amount, grid, hint, pnlTienMat, pnlMoMo}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(tl);
        body.add(Box.createVerticalStrut(4));
        body.add(amount);
        body.add(Box.createVerticalStrut(14));
        body.add(grid);
        body.add(Box.createVerticalStrut(10));
        body.add(hint);
        body.add(pnlTienMat);
        body.add(pnlMoMo);
        card.add(body, BorderLayout.CENTER);

        Runnable dongPopup = () -> {
            if (timerHolder[0] != null) timerHolder[0].stop();
            Overlay.dong(host);
        };
        dong.addActionListener(e -> dongPopup.run());
        xacNhan.addActionListener(e -> {
            if (timerHolder[0] != null) timerHolder[0].stop();
            Overlay.dong(host);
            long tk = "Tiền mặt".equalsIgnoreCase(chon[0]) ? tienKhachDua[0] : tong;
            onConfirm.accept(chon[0], tk);
        });
        card.add(buttons(dong, xacNhan), BorderLayout.SOUTH);
        Overlay.hien(host, card, dongPopup);
    }

    private static Image layAnhQRMomo() {
        try {
            java.io.File f = new java.io.File("data/qr_momo.png");
            if (f.exists()) return javax.imageio.ImageIO.read(f);

            java.net.URL url = HoaDonPopups.class.getResource("qr_momo.png");
            if (url != null) return javax.imageio.ImageIO.read(url);

            url = HoaDonPopups.class.getResource("/data/qr_momo.png");
            if (url != null) return javax.imageio.ImageIO.read(url);

            java.io.File f2 = new java.io.File("src/GUI/qr_momo.png");
            if (f2.exists()) return javax.imageio.ImageIO.read(f2);
        } catch (Exception ignored) {}
        return null;
    }

    private static class QRPanel extends JPanel {
        private final Image img;
        QRPanel(Image img) {
            this.img = img;
            setOpaque(false);
            Dimension d = new Dimension(185, 185);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int w = getWidth(), h = getHeight();
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(new Color(0xD1D5DB));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
            if (img != null) {
                g2.drawImage(img, 6, 6, w - 12, h - 12, this);
            } else {
                g2.setColor(Theme.MUTED);
                g2.setFont(Theme.font(Font.PLAIN, 12));
                g2.drawString("Không tìm thấy mã QR", 25, h / 2);
            }
            g2.dispose();
        }
    }

    /** Ô chọn phương thức thanh toán; ô đang chọn nền xanh nhạt, viền xanh và dấu ✓. */
    private static final class MethodCard extends JPanel {
        final String ten;
        private boolean chon;
        private final boolean laQR;

        MethodCard(String ten, boolean chon) {
            this.ten = ten;
            this.chon = chon;
            this.laQR = "MoMo".equalsIgnoreCase(ten) || "ZaloPay".equalsIgnoreCase(ten);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 52));
        }

        void setChon(boolean c) { chon = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(chon ? Theme.PRIMARY_SOFT : Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(chon ? Theme.PRIMARY : Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

            if (laQR) {
                g2.setFont(Theme.font(Font.BOLD, 13));
                g2.setColor(Theme.TEXT);
                g2.drawString(ten + " (QR)", 14, 22);

                g2.setFont(Theme.font(Font.BOLD, 10));
                g2.setColor(Theme.AMBER_TEXT);
                g2.drawString("(Đang phát triển / Thử nghiệm)", 14, 39);
            } else {
                g2.setFont(Theme.font(Font.BOLD, 13));
                g2.setColor(Theme.TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ten, 14, (h - fm.getHeight()) / 2 + fm.getAscent());
            }

            if (chon) {
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = w - 24, cy = h / 2;
                g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
                g2.drawLine(cx - 1, cy + 4, cx + 6, cy - 4);
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // Xác nhận hủy
    // =====================================================================
    static void xacNhanHuy(Component host, Runnable onYes) {
        xacNhan(host, "Bạn có muốn hủy hóa đơn này không?", "Thông tin chưa lưu sẽ bị mất nếu bạn xác nhận hủy.",
                "Thao tác này không thể hoàn tác.", "Không", "Có, hủy", onYes);
    }

    /** Hỏi xác nhận trước khi xóa một hóa đơn đã lưu / dịch vụ / mã giảm giá. */
    static void xacNhanXoa(Component host, String tieuDe, String moTa, String canhBao, Runnable onYes) {
        xacNhan(host, tieuDe, moTa, canhBao, "Không", "Có, xóa", onYes);
    }

    /** Popup xác nhận có biểu tượng cảnh báo; nút "xác nhận" màu đỏ. Esc hoặc "Không" thì đóng. */
    static void xacNhan(Component host, String tieuDe, String moTa, String canhBao, String nutKhong, String nutCo, Runnable onYes) {
        RoundedPanel card = card(580, 340, tieuDe, moTa);

        JComponent icon = new JComponent() {
            { setPreferredSize(new Dimension(46, 46)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0xFEEBCB));
                g2.fillOval(0, 0, 46, 46);
                g2.setColor(new Color(0xD9822B));
                g2.setFont(Theme.font(Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("!", (46 - fm.stringWidth("!")) / 2, (46 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        JLabel msg = new JLabel(canhBao);
        msg.setFont(Theme.font(Font.PLAIN, 13));
        msg.setForeground(Theme.TEXT);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(22, 0, 0, 0));
        row.add(icon);
        row.add(msg);
        card.add(row, BorderLayout.CENTER);

        RoundedButton khong = secondary(nutKhong, 150);
        RoundedButton co = new RoundedButton(nutCo, Theme.DANGER, Theme.DANGER_HOVER, Color.WHITE, null, 8);
        co.setPreferredSize(new Dimension(172, 42));
        Runnable dong = () -> Overlay.dong(host);
        khong.addActionListener(e -> dong.run());
        co.addActionListener(e -> {
            Overlay.dong(host);
            onYes.run();
        });
        card.add(buttons(khong, co), BorderLayout.SOUTH);
        Overlay.hien(host, card, dong);
    }

    /** Thông báo ngắn kèm một nút "Đóng". */
    static void thongBao(Component host, String tieuDe, String noiDung) {
        RoundedPanel card = card(540, 250, tieuDe, noiDung);
        RoundedButton dong = primary("Đóng", 130);
        Runnable d = () -> Overlay.dong(host);
        dong.addActionListener(e -> d.run());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.add(dong);
        card.add(south, BorderLayout.SOUTH);
        Overlay.hien(host, card, d);
    }

    // =====================================================================
    // Quét mã
    // =====================================================================
    static void quetMa(Component host, SanPhamBUS bus, Consumer<SanPham> onFound) {
        RoundedPanel card = card(580, 350, "Quét mã sản phẩm", "Quét mã vạch hoặc nhập mã SKU của sản phẩm / dịch vụ.");
        PlaceholderField tf = new PlaceholderField("Nhập hoặc quét mã SKU");
        FieldBox box = new FieldBox(tf, Color.WHITE, 42);
        JLabel err = errorLabel();
        tf.getDocument().addDocumentListener(clearError(err, box));
        card.add(body(label("Mã SKU"), box, err), BorderLayout.CENTER);

        RoundedButton dong = secondary("Đóng", 150);
        RoundedButton them = primary("Thêm vào hóa đơn", 200);
        Runnable dongPopup = () -> Overlay.dong(host);
        Runnable xong = () -> {
            SanPham sp = bus.timTheoSku(tf.getText());
            if (sp == null) {
                err.setText(tf.getText().trim().isEmpty() ? "Vui lòng nhập mã SKU."
                        : "Không tìm thấy sản phẩm có mã \"" + tf.getText().trim() + "\".");
                box.setError(true);
                tf.requestFocusInWindow();
                return;
            }
            Overlay.dong(host);
            onFound.accept(sp);
        };
        dong.addActionListener(e -> dongPopup.run());
        them.addActionListener(e -> xong.run());
        tf.addActionListener(e -> xong.run());
        card.add(buttons(dong, them), BorderLayout.SOUTH);
        Overlay.hien(host, card, dongPopup);
        SwingUtilities.invokeLater(tf::requestFocusInWindow);
    }

    // =====================================================================
    // Chi tiết hóa đơn (dùng ở Lịch sử hóa đơn)
    // =====================================================================
    static void chiTiet(Component host, HoaDon hd) {
        String phuLuc = hd.daThanhToan()
                ? hd.getTrangThai() + " · " + NGAY_GIO.format(hd.getNgayThanhToan()) + " · " + hd.getPhuongThuc()
                : hd.getTrangThai();
        int cao = 420 + 40 * Math.min(hd.getDong().size(), 6);
        RoundedPanel card = card(640, cao, "Hóa đơn " + hd.getMaHD(), phuLuc);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(18, 0, 14, 0));

        body.add(infoRow("Khách hàng", hd.getTenKH() + (hd.getMaKH().isEmpty() ? "" : " (" + hd.getMaKH() + ")")));
        body.add(Box.createVerticalStrut(6));
        body.add(infoRow("Số điện thoại", hd.getSdt()));
        body.add(Box.createVerticalStrut(6));
        body.add(infoRow("Ngày tạo", NGAY_GIO.format(hd.getNgayTao())));
        body.add(Box.createVerticalStrut(16));

        JPanel head = lineRow("Sản phẩm / dịch vụ", "SL", "Thành tiền", true);
        body.add(head);

        JPanel lines = new JPanel();
        lines.setOpaque(false);
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
        for (ChiTietHoaDon d : hd.getDong()) {
            String ten = d.getSanPham().getTen() + "  ·  " + Theme.money(d.getSanPham().getDonGia());
            JPanel r = lineRow(ten, String.valueOf(d.getSoLuong()), Theme.money(d.thanhTien()), false);
            lines.add(r);
        }
        ScrollPanel view = new ScrollPanel(new BorderLayout());
        view.setOpaque(false);
        view.add(lines, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        SlimScrollBarUI.apply(sp);
        body.add(sp);
        body.add(Box.createVerticalStrut(12));

        body.add(sumRow("Tạm tính", Theme.money(hd.tamTinh()), false));
        body.add(Box.createVerticalStrut(6));
        String giamTxt = hd.getMaGiamGia() == null ? "Giảm giá" : "Giảm giá (" + hd.getMaGiamGia().getMa() + ")";
        body.add(sumRow(giamTxt, hd.tienGiam() > 0 ? "-" + Theme.money(hd.tienGiam()) : Theme.money(0), false));
        body.add(Box.createVerticalStrut(10));
        body.add(sumRow("Tổng thanh toán", Theme.money(hd.canThanhToan()), true));
        card.add(body, BorderLayout.CENTER);

        RoundedButton dong = primary("Đóng", 110);
        Runnable dongPopup = () -> Overlay.dong(host);
        dong.addActionListener(e -> dongPopup.run());

        RoundedButton inHd = new RoundedButton("In / Xuất hóa đơn", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        inHd.setPreferredSize(new Dimension(150, 40));
        inHd.addActionListener(e -> {
            dongPopup.run();
            Window w = SwingUtilities.getWindowAncestor(host);
            DialogXuatHoaDon.hienThi(w, hd, null);
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        south.setOpaque(false);
        south.add(inHd);
        south.add(dong);
        card.add(south, BorderLayout.SOUTH);
        Overlay.hien(host, card, dongPopup);
    }

    static JPanel infoRow(String k, String v) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel a = new JLabel(k);
        a.setFont(Theme.font(Font.PLAIN, 13));
        a.setForeground(Theme.MUTED);
        a.setPreferredSize(new Dimension(130, 20));
        JLabel b = new JLabel(v);
        b.setFont(Theme.font(Font.BOLD, 13));
        b.setForeground(Theme.TEXT);
        p.add(a, BorderLayout.WEST);
        p.add(b, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return p;
    }

    private static JPanel lineRow(String ten, String sl, String tien, boolean header) {
        int pad = header ? 6 : 9;
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new javax.swing.border.CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.ROW_LINE),
                new EmptyBorder(pad, 0, pad, 0)));
        Font f = Theme.font(header ? Font.BOLD : Font.PLAIN, header ? 11 : 13);
        Color c = header ? Theme.MUTED : Theme.TEXT;
        JLabel a = new JLabel(ten);
        JLabel b = new JLabel(sl, SwingConstants.CENTER);
        JLabel d = new JLabel(tien, SwingConstants.RIGHT);
        for (JLabel l : new JLabel[] {a, b, d}) { l.setFont(f); l.setForeground(c); }
        d.setFont(Theme.font(Font.BOLD, header ? 11 : 13));
        b.setPreferredSize(new Dimension(40, 18));
        d.setPreferredSize(new Dimension(110, 18));
        p.add(a, BorderLayout.CENTER);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(b, BorderLayout.WEST);
        right.add(d, BorderLayout.EAST);
        p.add(right, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private static JPanel sumRow(String k, String v, boolean big) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel a = new JLabel(k);
        a.setFont(Theme.font(big ? Font.BOLD : Font.PLAIN, big ? 14 : 13));
        a.setForeground(big ? Theme.TEXT : Theme.MUTED);
        JLabel b = new JLabel(v, SwingConstants.RIGHT);
        b.setFont(Theme.font(big ? Font.BOLD : Font.PLAIN, big ? 20 : 13));
        b.setForeground(Theme.TEXT);
        p.add(a, BorderLayout.WEST);
        p.add(b, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    // =====================================================================
    // Khung chung
    // =====================================================================
    static RoundedPanel card(int w, int h, String title, String subtitle) {
        RoundedPanel c = new RoundedPanel(16, Color.WHITE, null);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(32, PAD, 30, PAD));
        c.setPreferredSize(new Dimension(w, h));

        JLabel t = new JLabel(title);
        t.setFont(Theme.font(Font.BOLD, 22));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(t);
        head.add(Box.createVerticalStrut(8));
        head.add(s);
        c.add(head, BorderLayout.NORTH);
        return c;
    }

    static JPanel body(JLabel label, FieldBox box, JLabel err) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(26, 0, 0, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        err.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(label);
        p.add(Box.createVerticalStrut(8));
        p.add(box);
        p.add(Box.createVerticalStrut(8));
        p.add(err);
        return p;
    }

    static JPanel buttons(RoundedButton left, RoundedButton right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.TEXT);
        return l;
    }

    static JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.DANGER);
        return l;
    }

    static DocumentListener clearError(JLabel err, FieldBox box) {
        return new DocumentListener() {
            private void c() { err.setText(" "); box.setError(false); }
            @Override public void insertUpdate(DocumentEvent e) { c(); }
            @Override public void removeUpdate(DocumentEvent e) { c(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        };
    }

    static RoundedButton primary(String text, int w) {
        RoundedButton b = new RoundedButton(text, Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        b.setPreferredSize(new Dimension(w, 42));
        return b;
    }

    static RoundedButton secondary(String text, int w) {
        RoundedButton b = new RoundedButton(text, Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        b.setPreferredSize(new Dimension(w, 42));
        return b;
    }
}
