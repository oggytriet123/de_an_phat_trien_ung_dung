package GUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import Bus.ChuongBUS;
import Entity.Chuong;

/**
 * Hộp thoại Chuyển Lồng Chuồng cho thú cưng (tương tự DialogChuyenBan).
 */
public class DialogChuyenChuong extends JDialog {
    private final ChuongBUS bus;
    private final String maChuongCu;
    private final Runnable onSuccess;

    private JLabel lblMoi;
    private JTable tblTrong;
    private DefaultTableModel modelTrong;
    private JTextField txtTim;
    private String maChuongMoiDuocChon = null;

    public DialogChuyenChuong(Window owner, ChuongBUS bus, String maChuongCu, Runnable onSuccess) {
        super(owner, "Chuyển Lồng Chuồng Lưu Trú", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.maChuongCu = maChuongCu;
        this.onSuccess = onSuccess;

        setSize(760, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadDanhSachTrong("");
    }

    private JPanel buildHeader() {
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.SIDEBAR);
        head.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("CHUYỂN LỒNG CHUỒNG LƯU TRÚ");
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        Chuong cu = bus.timTheoMa(maChuongCu);
        String petName = (cu != null && cu.getTenPet() != null) ? " - Thú cưng: " + cu.getTenPet() : "";
        JLabel sub = new JLabel("Lồng hiện tại: " + maChuongCu + petName);
        sub.setFont(Theme.font(Font.PLAIN, 12));
        sub.setForeground(new Color(0xE0ECF2));

        head.add(title, BorderLayout.NORTH);
        head.add(sub, BorderLayout.SOUTH);
        return head;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(Theme.BG);
        center.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Thanh thông tin chuyển: Cũ >> Mới
        RoundedPanel pnlBar = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        pnlBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlBar.setPreferredSize(new Dimension(0, 48));

        JLabel l1 = new JLabel("Lồng hiện tại: " + maChuongCu);
        l1.setFont(Theme.font(Font.BOLD, 14));
        l1.setForeground(Theme.TEXT);
        pnlBar.add(l1);

        JLabel lArrow = new JLabel(" ➔ ");
        lArrow.setFont(Theme.font(Font.BOLD, 16));
        lArrow.setForeground(Theme.ACCENT);
        pnlBar.add(lArrow);

        lblMoi = new JLabel("Chưa chọn lồng đích");
        lblMoi.setFont(Theme.font(Font.BOLD, 14));
        lblMoi.setForeground(Theme.GREEN);
        pnlBar.add(lblMoi);

        pnlBar.add(Box.createHorizontalStrut(30));

        JLabel lSearch = new JLabel("Tìm kiếm:");
        lSearch.setFont(Theme.font(Font.PLAIN, 13));
        pnlBar.add(lSearch);

        txtTim = new JTextField(12);
        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        txtTim.addActionListener(e -> loadDanhSachTrong(txtTim.getText().trim()));
        pnlBar.add(txtTim);

        RoundedButton btnTim = new RoundedButton("Tìm", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(12, 5);
        btnTim.addActionListener(e -> loadDanhSachTrong(txtTim.getText().trim()));
        pnlBar.add(btnTim);

        center.add(pnlBar, BorderLayout.NORTH);

        // Bảng danh sách các lồng còn trống
        String[] cols = {"Mã lồng", "Tên lồng", "Kích cỡ", "Khu vực", "Đơn giá/ngày", "Tiện ích"};
        modelTrong = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblTrong = new JTable(modelTrong);
        tblTrong.setFont(Theme.font(Font.PLAIN, 13));
        tblTrong.setRowHeight(32);
        tblTrong.getTableHeader().setFont(Theme.font(Font.BOLD, 13));
        tblTrong.getTableHeader().setBackground(Theme.SIDEBAR);
        tblTrong.getTableHeader().setForeground(Color.WHITE);
        tblTrong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblTrong.getSelectionModel().addListSelectionListener(e -> {
            int row = tblTrong.getSelectedRow();
            if (row >= 0) {
                maChuongMoiDuocChon = (String) modelTrong.getValueAt(row, 0);
                String ten = (String) modelTrong.getValueAt(row, 1);
                lblMoi.setText(maChuongMoiDuocChon + " (" + ten + ")");
            }
        });

        JScrollPane scroll = new JScrollPane(tblTrong);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1));
        center.add(scroll, BorderLayout.CENTER);

        return center;
    }

    private JPanel buildFooter() {
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        foot.setBackground(Color.WHITE);
        foot.setBorder(new LineBorder(Theme.BORDER, 1));

        RoundedButton btnDong = new RoundedButton("Hủy bỏ", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 8);
        btnDong.addActionListener(e -> dispose());

        RoundedButton btnChuyen = new RoundedButton("Xác Nhận Chuyển Lồng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 8);
        btnChuyen.addActionListener(e -> thucHienChuyen());

        foot.add(btnDong);
        foot.add(btnChuyen);
        return foot;
    }

    private void loadDanhSachTrong(String tuKhoa) {
        modelTrong.setRowCount(0);
        String kw = tuKhoa.toLowerCase();

        for (Chuong c : bus.layDanhSach()) {
            if (c.isTrong() && !c.getMaChuong().equalsIgnoreCase(maChuongCu)) {
                if (kw.isEmpty() || c.getMaChuong().toLowerCase().contains(kw)
                        || c.getTenChuong().toLowerCase().contains(kw)
                        || c.getKhuVuc().toLowerCase().contains(kw)
                        || c.getSize().toLowerCase().contains(kw)) {
                    modelTrong.addRow(new Object[]{
                        c.getMaChuong(),
                        c.getTenChuong(),
                        c.getSize(),
                        c.getKhuVuc(),
                        Theme.money((long) c.getGiaTheoNgay()),
                        c.isVip() ? "VIP Suite ⭐" : "Tiêu chuẩn"
                    });
                }
            }
        }
    }

    private void thucHienChuyen() {
        if (maChuongMoiDuocChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lồng trống từ danh sách để chuyển đến!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chuong cu = bus.timTheoMa(maChuongCu);
        Chuong moi = bus.timTheoMa(maChuongMoiDuocChon);
        if (cu != null && moi != null) {
            String loai = cu.getLoaiPet() != null ? cu.getLoaiPet() : "";
            String kvMoi = moi.getKhuVuc() != null ? moi.getKhuVuc() : "";
            if (kvMoi.equalsIgnoreCase("Khu Mèo") && !loai.equalsIgnoreCase("Mèo")) {
                JOptionPane.showMessageDialog(this, "Không thể chuyển! Lồng đích thuộc Khu Mèo chỉ dành riêng cho Mèo (bé này là " + loai + ").", "Ràng buộc khu vực", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ((kvMoi.equalsIgnoreCase("Khu Chó Nhỏ") || kvMoi.equalsIgnoreCase("Khu Chó Lớn")) && !loai.equalsIgnoreCase("Chó")) {
                JOptionPane.showMessageDialog(this, "Không thể chuyển! Lồng đích thuộc Khu Chó chỉ dành riêng cho Chó (bé này là " + loai + ").", "Ràng buộc khu vực", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (kvMoi.equalsIgnoreCase("Khu Loài Khác") && (loai.equalsIgnoreCase("Chó") || loai.equalsIgnoreCase("Mèo"))) {
                JOptionPane.showMessageDialog(this, "Không thể chuyển! Lồng đích thuộc Khu Loài Khác dành cho loài khác (thỏ, chim,...).", "Ràng buộc khu vực", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (moi.getCanNangToiDa() > 0 && cu.getCanNang() > moi.getCanNangToiDa()) {
                JOptionPane.showMessageDialog(this, "Cân nặng bé (" + cu.getCanNang() + " kg) vượt quá tải trọng của lồng đích (" + moi.getCanNangToiDa() + " kg)!", "Cảnh báo tải trọng", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String petName = (cu != null && cu.getTenPet() != null) ? "cho bé " + cu.getTenPet() : "";

        int r = JOptionPane.showConfirmDialog(this,
                "Xác nhận chuyển " + petName + " từ lồng " + maChuongCu + " sang lồng " + maChuongMoiDuocChon + "?",
                "Xác nhận chuyển lồng", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (r == JOptionPane.YES_OPTION) {
            if (bus.chuyenChuong(maChuongCu, maChuongMoiDuocChon)) {
                JOptionPane.showMessageDialog(this, "Chuyển lồng chuồng thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                if (onSuccess != null) onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Chuyển lồng thất bại, vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
