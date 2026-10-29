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
import Entity.DongGioHang;
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
            CodeCard cc = new CodeCard(m, bus.daHetHan(m));
            cc.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) {
                    if (cc.hetHan || !SwingUtilities.isLeftMouseButton(e) || !cc.contains(e.getPoint())) return;
                    tf.setText(cc.m.getMa());
                    tf.requestFocusInWindow();
                }
            });
            cards.add(cc);
            grid.add(cc);
        }
        Runnable dongBo = () -> {
            String cur = tf.getText().trim();
            for (CodeCard c : cards) c.setChon(!c.hetHan && c.m.getMa().equalsIgnoreCase(cur));
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

    /** Nút chọn một mã giảm giá có sẵn. Mã hết hạn bị làm mờ và không chọn được. */
    private static final class CodeCard extends JPanel {
        final MaGiamGia m;
        final boolean hetHan;
        private boolean chon;

        CodeCard(MaGiamGia m, boolean hetHan) {
            this.m = m;
            this.hetHan = hetHan;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 64));
            if (!hetHan) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setChon(boolean c) {
            if (chon != c) { chon = c; repaint(); }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(hetHan ? Theme.FIELD_BG : chon ? Theme.PRIMARY_SOFT : Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(chon ? Theme.PRIMARY : Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

            g2.setFont(Theme.font(Font.BOLD, 14));
            g2.setColor(hetHan ? Theme.MUTED : Theme.TEXT);
            g2.drawString(m.getMa(), 14, 26);
            g2.setFont(Theme.font(Font.PLAIN, 12));
            String han = NGAY.format(m.getHanDung());
            if (hetHan) {
                g2.setColor(Theme.DANGER);
                g2.drawString("Hết hạn " + han, 14, 46);
            } else {
                g2.setColor(Theme.MUTED);
                g2.drawString(m.moTa() + "  ·  HSD " + han, 14, 46);
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

    /**
     * Form tạo mã giảm giá mới.
     * @param onCreated gọi sau khi tạo thành công
     * @param onBack    gọi khi bấm "Quay lại" hoặc Esc
     */
    static void taoMaGiamGia(Component host, MaGiamGiaBUS bus, Consumer<MaGiamGia> onCreated, Runnable onBack) {
        RoundedPanel card = card(600, 520, "Tạo mã giảm giá", "Mã mới sẽ xuất hiện trong danh sách mã có sẵn.");

        PlaceholderField tfMa = new PlaceholderField("Ví dụ: SALE20");
        PlaceholderField tfGia = new PlaceholderField("Ví dụ: 15");
        PlaceholderField tfHan = new PlaceholderField("dd/MM/yyyy");
        tfHan.setText(NGAY.format(LocalDate.now().plusDays(30)));
        GioiHanNhap.ap(tfMa, 20, "[A-Za-z0-9_-]");
        GioiHanNhap.ap(tfGia, 12, "[0-9.,]");
        GioiHanNhap.ap(tfHan, 10, "[0-9/]");
        FieldBox bMa = new FieldBox(tfMa, Color.WHITE, 42);
        FieldBox bGia = new FieldBox(tfGia, Color.WHITE, 42);
        FieldBox bHan = new FieldBox(tfHan, Color.WHITE, 42);
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
        for (PlaceholderField f : new PlaceholderField[] {tfMa, tfGia, tfHan}) {
            f.getDocument().addDocumentListener(new DocumentListener() {
                private void c() { err.setText(" "); bMa.setError(false); bGia.setError(false); bHan.setError(false); }
                @Override public void insertUpdate(DocumentEvent e) { c(); }
                @Override public void removeUpdate(DocumentEvent e) { c(); }
                @Override public void changedUpdate(DocumentEvent e) { }
            });
        }

        JPanel hang1 = new JPanel(new GridLayout(1, 2, 14, 0));
        hang1.setOpaque(false);
        hang1.add(nhom("Mã giảm giá *", bMa));
        hang1.add(nhom("Hạn dùng *", bHan));
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
        for (JComponent c : new JComponent[] {hang1, lLoai, loai, gGia, err}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        loai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        gGia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        body.add(hang1);
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
            LocalDate han;
            try {
                han = LocalDate.parse(tfHan.getText().trim(), NHAP_NGAY);
            } catch (DateTimeParseException ex) {
                err.setText("Hạn dùng không hợp lệ. Nhập theo dạng dd/MM/yyyy.");
                bHan.setError(true);
                tfHan.requestFocusInWindow();
                return;
            }
            try {
                MaGiamGia m = bus.tao(tfMa.getText(), phanTram[0], gt, han);
                onCreated.accept(m);
            } catch (IllegalArgumentException ex) {
                String msg = ex.getMessage();
                err.setText(msg);
                if (msg.contains("Mã") || msg.contains("mã")) bMa.setError(true);
                else if (msg.contains("Hạn")) bHan.setError(true);
                else bGia.setError(true);
            }
        };
        tao.addActionListener(e -> xong.run());
        for (PlaceholderField f : new PlaceholderField[] {tfMa, tfGia, tfHan}) f.addActionListener(e -> xong.run());
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
        RoundedPanel card = card(580, 450, "Thanh toán hóa đơn", "Chọn phương thức thanh toán cho hóa đơn " + maHD + ".");

        JLabel tl = new JLabel("Tổng thanh toán");
        tl.setFont(Theme.font(Font.PLAIN, 12));
        tl.setForeground(Theme.MUTED);
        JLabel amount = new JLabel(Theme.money(tong));
        amount.setFont(Theme.font(Font.BOLD, 26));
        amount.setForeground(Theme.TEXT);

        String[] huongDan = {
            "Xác nhận sau khi đã nhận đủ tiền mặt từ khách.",
            "Xác nhận sau khi giao dịch quẹt thẻ thành công.",
            "Chỉ xác nhận sau khi nhận được thông báo thanh toán MoMo thành công.",
            "Chỉ xác nhận sau khi nhận được thông báo thanh toán ZaloPay thành công.",
        };
        JLabel hint = new JLabel(huongDan[0]);
        hint.setFont(Theme.font(Font.PLAIN, 12));
        hint.setForeground(Theme.MUTED);

        String[] chon = {HoaDonBUS.PHUONG_THUC[0]};
        List<MethodCard> cards = new ArrayList<>();
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);
        for (int i = 0; i < HoaDonBUS.PHUONG_THUC.length; i++) {
            MethodCard mc = new MethodCard(HoaDonBUS.PHUONG_THUC[i], i == 0);
            int idx = i;
            mc.addMouseListener(new MouseAdapter() {
                @Override public void mouseReleased(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e) || !mc.contains(e.getPoint())) return;
                    for (MethodCard c : cards) c.setChon(c == mc);
                    chon[0] = mc.ten;
                    hint.setText(huongDan[idx]);
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
        for (JComponent c : new JComponent[] {tl, amount, grid, hint}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(tl);
        body.add(Box.createVerticalStrut(4));
        body.add(amount);
        body.add(Box.createVerticalStrut(14));
        body.add(grid);
        body.add(Box.createVerticalStrut(10));
        body.add(hint);
        card.add(body, BorderLayout.CENTER);

        RoundedButton dong = secondary("Đóng", 150);
        RoundedButton xacNhan = primary("Xác nhận thanh toán", 210);
        Runnable dongPopup = () -> Overlay.dong(host);
        dong.addActionListener(e -> dongPopup.run());
        xacNhan.addActionListener(e -> {
            Overlay.dong(host);
            onConfirm.accept(chon[0]);
        });
        card.add(buttons(dong, xacNhan), BorderLayout.SOUTH);
        Overlay.hien(host, card, dongPopup);
    }

    /** Ô chọn phương thức thanh toán; ô đang chọn nền xanh nhạt, viền xanh và dấu ✓. */
    private static final class MethodCard extends JPanel {
        final String ten;
        private boolean chon;

        MethodCard(String ten, boolean chon) {
            this.ten = ten;
            this.chon = chon;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 50));
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
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.setColor(Theme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ten, 14, (h - fm.getHeight()) / 2 + fm.getAscent());
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
        for (DongGioHang d : hd.getDong()) {
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

        RoundedButton dong = primary("Đóng", 130);
        Runnable dongPopup = () -> Overlay.dong(host);
        dong.addActionListener(e -> dongPopup.run());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
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
