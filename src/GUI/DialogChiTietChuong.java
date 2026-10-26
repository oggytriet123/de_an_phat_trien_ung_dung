package GUI;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import Bus.ChuongBUS;
import Entity.ChiTietDichVuChuong;
import Entity.Chuong;

/**
 * Hộp thoại Xem chi tiết / Nhận lồng / Thanh toán trả lồng thú cưng.
 */
public class DialogChiTietChuong extends JDialog {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ChuongBUS bus;
    private final String maChuong;
    private final Runnable onRefresh;

    private DefaultTableModel modelDV;
    private JTable tblDV;
    private JLabel lblTongTien;
    private JLabel lblTienPhong;
    private JLabel lblTienDV;

    public DialogChiTietChuong(Window owner, ChuongBUS bus, String maChuong, Runnable onRefresh) {
        super(owner, "Chi Tiết Lồng Chuồng - " + maChuong, ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.maChuong = maChuong;
        this.onRefresh = onRefresh;

        setSize(780, 540);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        Chuong c = bus.timTheoMa(maChuong);

        add(buildHeader(c), BorderLayout.NORTH);
        add(buildCenter(c), BorderLayout.CENTER);
        add(buildFooter(c), BorderLayout.SOUTH);
    }

    private JPanel buildHeader(Chuong c) {
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.SIDEBAR);
        head.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel title = new JLabel((c != null ? c.getTenChuong() : "Lồng " + maChuong) + " (" + (c != null ? c.getSize() : "") + ")");
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        left.add(title);

        String st = c != null ? c.getTrangThai() : "Trống";
        JLabel badge = new JLabel(" " + st + " ");
        badge.setFont(Theme.font(Font.BOLD, 12));
        badge.setOpaque(true);
        if ("Trống".equalsIgnoreCase(st)) {
            badge.setBackground(Theme.GREEN);
            badge.setForeground(Color.WHITE);
        } else if ("Đã đặt".equalsIgnoreCase(st)) {
            badge.setBackground(new Color(0xE67E22));
            badge.setForeground(Color.WHITE);
        } else if ("Đang lưu trú".equalsIgnoreCase(st)) {
            badge.setBackground(Theme.RED);
            badge.setForeground(Color.WHITE);
        } else {
            badge.setBackground(Color.GRAY);
            badge.setForeground(Color.WHITE);
        }
        left.add(badge);

        JLabel price = new JLabel("Đơn giá: " + Theme.money((long) (c != null ? c.getGiaTheoNgay() : 0)) + "/ngày");
        price.setFont(Theme.font(Font.BOLD, 14));
        price.setForeground(new Color(0xD6EAF8));

        head.add(left, BorderLayout.WEST);
        head.add(price, BorderLayout.EAST);
        return head;
    }

    private JPanel buildCenter(Chuong c) {
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setBackground(Theme.BG);
        center.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Nửa trái: Thông tin bé Pet & Chủ nuôi
        RoundedPanel pnlPet = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        pnlPet.setLayout(new BoxLayout(pnlPet, BoxLayout.Y_AXIS));
        pnlPet.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel t1 = new JLabel("🐾 THÔNG TIN THÚ CƯNG & CHỦ NUÔI");
        t1.setFont(Theme.font(Font.BOLD, 13));
        t1.setForeground(Theme.SIDEBAR);
        pnlPet.add(t1);
        pnlPet.add(Box.createVerticalStrut(10));

        if (c != null && (c.isDangLuuTru() || c.isDaDat())) {
            pnlPet.add(infoRow("Tên thú cưng:", c.getTenPet() != null ? c.getTenPet() : "—"));
            pnlPet.add(infoRow("Giống loài:", c.getLoaiPet() != null ? c.getLoaiPet() : "—"));
            pnlPet.add(infoRow("Cân nặng:", (c.getCanNang() > 0 ? c.getCanNang() + " kg" : "—")));
            pnlPet.add(infoRow("Chủ nuôi:", c.getTenKhach() != null ? c.getTenKhach() : "—"));
            pnlPet.add(infoRow("SĐT liên hệ:", c.getSdtKhach() != null ? c.getSdtKhach() : "—"));
            pnlPet.add(infoRow("Check-in:", c.getNgayVao() != null ? c.getNgayVao().format(DT_FMT) : "Chưa nhận"));
            pnlPet.add(infoRow("Dự kiến trả:", c.getNgayRaDuKien() != null ? c.getNgayRaDuKien().format(DT_FMT) : "—"));
            pnlPet.add(infoRow("Số ngày ở:", bus.tinhSoNgayLuuTru(c) + " ngày"));
            pnlPet.add(infoRow("Ghi chú đặc biệt:", c.getGhiChu() != null ? c.getGhiChu() : "Bình thường"));
        } else {
            JLabel lblTrong = new JLabel("Lồng hiện đang trống, sẵn sàng tiếp nhận thú cưng.");
            lblTrong.setFont(Theme.font(Font.ITALIC, 13));
            lblTrong.setForeground(Theme.MUTED);
            pnlPet.add(lblTrong);
        }
        center.add(pnlPet);

        // Nửa phải: Bảng dịch vụ chăm sóc đã sử dụng
        RoundedPanel pnlDichVu = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        pnlDichVu.setLayout(new BorderLayout(0, 8));
        pnlDichVu.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel topDV = new JPanel(new BorderLayout());
        topDV.setOpaque(false);
        JLabel t2 = new JLabel("🍖 DỊCH VỤ & THỨC ĂN ĐÃ DÙNG");
        t2.setFont(Theme.font(Font.BOLD, 13));
        t2.setForeground(Theme.SIDEBAR);
        topDV.add(t2, BorderLayout.WEST);

        if (c != null && c.isDangLuuTru()) {
            RoundedButton btnThemDV = new RoundedButton("+ Thêm DV", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(10, 4);
            btnThemDV.addActionListener(e -> {
                Window owner = SwingUtilities.getWindowAncestor(this);
                new DialogChamSocDichVu(owner, bus, maChuong, this::loadDichVu).setVisible(true);
            });
            topDV.add(btnThemDV, BorderLayout.EAST);
        }
        pnlDichVu.add(topDV, BorderLayout.NORTH);

        String[] cols = {"Dịch vụ / Món ăn", "SL", "Đơn giá", "Thành tiền"};
        modelDV = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDV = new JTable(modelDV);
        JScrollPane sp = new JScrollPane(tblDV);
        Theme.styleTable(tblDV, sp, 30);
        tblDV.getColumnModel().getColumn(0).setPreferredWidth(160);
        tblDV.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblDV.getColumnModel().getColumn(1).setCellRenderer(Theme.cellCenter());
        tblDV.getColumnModel().getColumn(2).setCellRenderer(Theme.cellRight());
        tblDV.getColumnModel().getColumn(3).setCellRenderer(Theme.cellRight());
        pnlDichVu.add(sp, BorderLayout.CENTER);

        // Bảng tổng kết chi phí
        JPanel pnlSum = new JPanel(new GridLayout(3, 1, 0, 3));
        pnlSum.setOpaque(false);
        lblTienPhong = new JLabel("Tiền phòng: 0₫");
        lblTienPhong.setFont(Theme.font(Font.PLAIN, 12));
        lblTienDV = new JLabel("Tiền dịch vụ & thức ăn: 0₫");
        lblTienDV.setFont(Theme.font(Font.PLAIN, 12));
        lblTongTien = new JLabel("TỔNG CỘNG: 0₫");
        lblTongTien.setFont(Theme.font(Font.BOLD, 14));
        lblTongTien.setForeground(Theme.RED);

        pnlSum.add(lblTienPhong);
        pnlSum.add(lblTienDV);
        pnlSum.add(lblTongTien);
        pnlDichVu.add(pnlSum, BorderLayout.SOUTH);

        center.add(pnlDichVu);

        loadDichVu();
        return center;
    }

    private JPanel infoRow(String k, String v) {
        JPanel r = new JPanel(new BorderLayout(5, 0));
        r.setOpaque(false);
        r.setMaximumSize(new Dimension(350, 24));
        JLabel lk = new JLabel(k);
        lk.setFont(Theme.font(Font.PLAIN, 12));
        lk.setForeground(Theme.MUTED);
        lk.setPreferredSize(new Dimension(105, 20));
        JLabel lv = new JLabel(v);
        lv.setFont(Theme.font(Font.BOLD, 12));
        lv.setForeground(Theme.TEXT);
        r.add(lk, BorderLayout.WEST);
        r.add(lv, BorderLayout.CENTER);
        return r;
    }

    private void loadDichVu() {
        if (modelDV == null) return;
        modelDV.setRowCount(0);
        Chuong c = bus.timTheoMa(maChuong);
        if (c == null) return;

        double sumDV = 0;
        List<ChiTietDichVuChuong> dvs = bus.layDichVuCuaChuong(maChuong);
        for (ChiTietDichVuChuong d : dvs) {
            modelDV.addRow(new Object[]{
                d.getTenDV(),
                d.getSoLuong(),
                Theme.money((long) d.getDonGia()),
                Theme.money((long) d.getThanhTien())
            });
            sumDV += d.getThanhTien();
        }

        long soNgay = bus.tinhSoNgayLuuTru(c);
        double tienPhong = c.isDangLuuTru() ? (soNgay * c.getGiaTheoNgay()) : 0;
        double tong = tienPhong + sumDV;

        lblTienPhong.setText("Tiền lưu trú (" + soNgay + " ngày): " + Theme.money((long) tienPhong));
        lblTienDV.setText("Tiền dịch vụ & dinh dưỡng: " + Theme.money((long) sumDV));
        lblTongTien.setText("TỔNG TẠM TÍNH: " + Theme.money((long) tong));
    }

    private JPanel buildFooter(Chuong c) {
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        foot.setBackground(Color.WHITE);
        foot.setBorder(new LineBorder(Theme.BORDER, 1));

        RoundedButton btnDong = new RoundedButton("Đóng", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 8);
        btnDong.addActionListener(e -> dispose());
        foot.add(btnDong);

        if (c != null && c.isDaDat()) {
            RoundedButton btnNhan = new RoundedButton("🐾 Nhận Lồng Ngay (F5)", Theme.GREEN, new Color(0x278C44), Color.WHITE, null, 8).pad(16, 8);
            btnNhan.addActionListener(e -> nhanLong(c));
            foot.add(btnNhan);
        }

        if (c != null && c.isDangLuuTru()) {
            RoundedButton btnTra = new RoundedButton("✨ Trả Lồng & Thanh Toán (F10)", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 8);
            btnTra.addActionListener(e -> traLongThanhToan(c));
            foot.add(btnTra);
        }

        return foot;
    }

    private void nhanLong(Chuong c) {
        int r = JOptionPane.showConfirmDialog(this,
                "Xác nhận nhận thú cưng \"" + c.getTenPet() + "\" của khách \"" + c.getTenKhach() + "\" vào lồng " + c.getMaChuong() + "?",
                "Xác nhận nhận thú cưng", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            bus.nhanChuong(c.getMaChuong(), c.getTenPet(), c.getLoaiPet(), c.getCanNang(),
                    c.getTenKhach(), c.getSdtKhach(), c.getGhiChu(), c.getNgayRaDuKien());
            JOptionPane.showMessageDialog(this, "Đã nhận bé \"" + c.getTenPet() + "\" vào lồng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (onRefresh != null) onRefresh.run();
            dispose();
        }
    }

    private void traLongThanhToan(Chuong c) {
        double tong = bus.tinhTongChiPhi(c);
        long soNgay = bus.tinhSoNgayLuuTru(c);

        String msg = String.format("Thanh toán cho thú cưng: %s (Chủ nuôi: %s)\n\n"
                + "• Số ngày lưu trú: %d ngày x %s = %s\n"
                + "• Tiền dịch vụ chăm sóc & thức ăn: %s\n"
                + "--------------------------------------------------\n"
                + "TỔNG TIỀN THANH TOÁN: %s\n\n"
                + "Xác nhận in hóa đơn và dọn khử trùng lồng chuyển về trạng thái Trống?",
                c.getTenPet(), c.getTenKhach(),
                soNgay, Theme.money((long) c.getGiaTheoNgay()), Theme.money((long) (soNgay * c.getGiaTheoNgay())),
                lblTienDV.getText().replace("Tiền dịch vụ & dinh dưỡng: ", ""),
                Theme.money((long) tong));

        int r = JOptionPane.showConfirmDialog(this, msg, "Xác nhận thanh toán & Trả lồng", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            boolean ok = bus.donChuong(c.getMaChuong());
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Không thể hoàn tất thanh toán, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            long diem = Bus.KhachHangBUS.diemTuSoTien((long) tong);
            JOptionPane.showMessageDialog(this, "Thanh toán thành công! Lồng " + c.getMaChuong() + " đã được chuyển về trạng thái Trống sạch sẽ."
                    + (diem > 0 ? "\nĐã lập hóa đơn và cộng " + diem + " điểm tích lũy cho khách \"" + c.getTenKhach() + "\"." : ""),
                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            if (onRefresh != null) onRefresh.run();
            dispose();
        }
    }
}
