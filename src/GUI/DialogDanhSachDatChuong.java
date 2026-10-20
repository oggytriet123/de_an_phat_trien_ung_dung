package GUI;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import Bus.ChuongBUS;
import Entity.PhieuDatChuong;

/**
 * Hộp thoại Danh Sách Đặt Trước Lồng Chuồng (tương tự DialogDanhSachDatBan).
 */
public class DialogDanhSachDatChuong extends JDialog {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ChuongBUS bus;
    private final Runnable onRefresh;

    private DefaultTableModel model;
    private JTable table;

    public DialogDanhSachDatChuong(Window owner, ChuongBUS bus, Runnable onRefresh) {
        super(owner, "Danh Sách Lịch Đặt Trước Lồng Chuồng", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.onRefresh = onRefresh;

        setSize(920, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.SIDEBAR);
        head.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("DANH SÁCH LỊCH HẸN ĐẶT LỒNG CHUỒNG");
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Quản lý các phiếu đặt giữ lồng trước của khách hàng");
        sub.setFont(Theme.font(Font.PLAIN, 12));
        sub.setForeground(new Color(0xE0ECF2));

        head.add(title, BorderLayout.NORTH);
        head.add(sub, BorderLayout.SOUTH);
        return head;
    }

    private JPanel buildTable() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Theme.BG);
        pnl.setBorder(new EmptyBorder(14, 18, 14, 18));

        String[] cols = {"Mã Phiếu", "Khách Hàng", "SĐT", "Tên Thú Cưng", "Loài / Giống", "Giờ Hẹn Đến", "Lồng Cấp", "Tiền Cọc", "Trạng Thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        Theme.styleTable(table, sp, 32);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(2).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(5).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(6).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(7).setCellRenderer(Theme.cellRight());
        table.getColumnModel().getColumn(8).setCellRenderer(Theme.cellCenter());

        pnl.add(sp, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel buildFooter() {
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        foot.setBackground(Color.WHITE);
        foot.setBorder(new LineBorder(Theme.BORDER, 1));

        RoundedButton btnDong = new RoundedButton("Đóng", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 8);
        RoundedButton btnHuy = new RoundedButton("Hủy Giữ Chỗ", new Color(0xFADBD8), new Color(0xF5B7B1), Theme.RED, null, 8).pad(14, 8);
        RoundedButton btnNhan = new RoundedButton("🐾 Nhận Lồng Ngay", Theme.GREEN, new Color(0x278C44), Color.WHITE, null, 8).pad(16, 8);

        btnDong.addActionListener(e -> dispose());
        btnHuy.addActionListener(e -> huyDat());
        btnNhan.addActionListener(e -> nhanLongNgay());

        foot.add(btnHuy);
        foot.add(btnNhan);
        foot.add(btnDong);
        return foot;
    }

    private void loadData() {
        model.setRowCount(0);
        List<PhieuDatChuong> list = bus.layDanhSachPhieuDat();
        for (PhieuDatChuong p : list) {
            model.addRow(new Object[]{
                p.getMaPhieu(),
                p.getTenKhach(),
                p.getSdtKhach(),
                p.getTenPet(),
                p.getLoaiPet(),
                p.getNgayDen() != null ? p.getNgayDen().format(DT_FMT) : "—",
                p.getMaChuong(),
                Theme.money((long) p.getTienCoc()),
                p.getTrangThai()
            });
        }
    }

    private void nhanLongNgay() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu đặt lồng để nhận thú cưng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String st = (String) model.getValueAt(r, 8);
        if (!"Đã đặt".equalsIgnoreCase(st)) {
            JOptionPane.showMessageDialog(this, "Phiếu này đã ở trạng thái: \"" + st + "\", không thể nhận lồng lại!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPhieu = (String) model.getValueAt(r, 0);
        String tenKhach = (String) model.getValueAt(r, 1);
        String sdt = (String) model.getValueAt(r, 2);
        String tenPet = (String) model.getValueAt(r, 3);
        String loaiPet = (String) model.getValueAt(r, 4);
        String maChuong = (String) model.getValueAt(r, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận nhận thú cưng \"" + tenPet + "\" vào lồng " + maChuong + " cho khách \"" + tenKhach + "\"?",
                "Xác nhận nhận lồng", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            bus.nhanChuong(maChuong, tenPet, loaiPet, 4.0, tenKhach, sdt, "Khách nhận từ lịch hẹn " + maPhieu, null);
            JOptionPane.showMessageDialog(this, "Đã nhận bé \"" + tenPet + "\" vào lồng " + maChuong + " thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            if (onRefresh != null) onRefresh.run();
        }
    }

    private void huyDat() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu để hủy đặt!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String st = (String) model.getValueAt(r, 8);
        if (!"Đã đặt".equalsIgnoreCase(st)) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể hủy phiếu đang ở trạng thái 'Đã đặt'!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPhieu = (String) model.getValueAt(r, 0);
        String maChuong = (String) model.getValueAt(r, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận hủy đặt chỗ phiếu " + maPhieu + " và giải phóng lồng " + maChuong + "?",
                "Xác nhận hủy đặt", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            bus.huyDat(maChuong);
            JOptionPane.showMessageDialog(this, "Đã hủy phiếu đặt và chuyển lồng " + maChuong + " về trạng thái Trống!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            if (onRefresh != null) onRefresh.run();
        }
    }
}
