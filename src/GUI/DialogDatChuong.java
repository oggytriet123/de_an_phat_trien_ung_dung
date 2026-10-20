package GUI;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.ChuongBUS;
import Bus.KhachHangBUS;
import Entity.Chuong;
import Entity.KhachHang;
import Entity.PhieuDatChuong;
import Entity.ThuCung;

/**
 * Hộp thoại Đặt trước Lồng Chuồng lưu trú thú cưng (tương tự DialogDatBan trong prj_spa).
 */
public class DialogDatChuong extends JDialog {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ChuongBUS bus;
    private final String maChuongChon;
    private final Runnable onSuccess;

    private JComboBox<String> cbChuong;
    private JTextField txtSDT;
    private JLabel lblTenKhach;
    private JLabel lblHangThe;
    private JComboBox<String> cbTenPet;   // thú cưng đã có của khách (hoặc gõ tên mới)
    /** SĐT (chỉ chữ số) đã được tra cứu/xác nhận; null = chưa tra cứu. */
    private String sdtDaTraCuu = null;
    private boolean khachMoi = false;
    private final Map<String, ThuCung> petsKhach = new HashMap<>();
    private JComboBox<String> cbLoaiPet;
    private JSpinner spnCanNang;
    private JTextField txtNgayDen;
    private JTextField txtNgayTra;
    private JSpinner spnTienCoc;
    private JTextField txtGhiChu;
    private JLabel lblGiaPhong;

    public DialogDatChuong(Window owner, ChuongBUS bus, String maChuong, Runnable onSuccess) {
        super(owner, "Đặt Trước Lồng Chuồng Lưu Trú", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.maChuongChon = maChuong;
        this.onSuccess = onSuccess;

        setSize(560, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Theme.SIDEBAR);
        pnl.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("ĐẶT TRƯỚC LỒNG CHUỒNG");
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Lên lịch giữ lồng khách sạn & chăm sóc thú cưng");
        sub.setFont(Theme.font(Font.PLAIN, 12));
        sub.setForeground(new Color(0xE0ECF2));

        pnl.add(title, BorderLayout.NORTH);
        pnl.add(sub, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(null);
        body.setBackground(Theme.BG);
        body.setBorder(new EmptyBorder(15, 20, 15, 20));

        int y = 15;
        Font lblFont = Theme.font(Font.BOLD, 13);
        Font valFont = Theme.font(Font.PLAIN, 13);

        // --- 1. Chọn Lồng Chuồng ---
        JLabel lblC = new JLabel("Lồng chuồng:");
        lblC.setFont(lblFont);
        lblC.setForeground(Theme.TEXT);
        lblC.setBounds(30, y, 110, 28);
        body.add(lblC);

        cbChuong = new JComboBox<>();
        cbChuong.setFont(valFont);
        cbChuong.setBounds(150, y, 220, 28);
        for (Chuong c : bus.layDanhSach()) {
            if (c.isTrong() || (maChuongChon != null && c.getMaChuong().equalsIgnoreCase(maChuongChon))) {
                cbChuong.addItem(c.getMaChuong() + " - " + c.getTenChuong() + " (" + c.getSize() + ")");
                if (maChuongChon != null && c.getMaChuong().equalsIgnoreCase(maChuongChon)) {
                    cbChuong.setSelectedIndex(cbChuong.getItemCount() - 1);
                }
            }
        }
        body.add(cbChuong);

        lblGiaPhong = new JLabel("Giá: 0₫/ngày");
        lblGiaPhong.setFont(Theme.font(Font.BOLD, 13));
        lblGiaPhong.setForeground(Theme.GREEN);
        lblGiaPhong.setBounds(380, y, 140, 28);
        body.add(lblGiaPhong);

        capNhatGiaTheoChuong();
        cbChuong.addActionListener(e -> capNhatGiaTheoChuong());

        y += 40;
        // --- 2. SĐT Khách hàng & Tra cứu ---
        JLabel lblS = new JLabel("SĐT Chủ nuôi:");
        lblS.setFont(lblFont);
        lblS.setForeground(Theme.TEXT);
        lblS.setBounds(30, y, 110, 28);
        body.add(lblS);

        txtSDT = new JTextField();
        txtSDT.setFont(valFont);
        txtSDT.setBounds(150, y, 180, 28);
        txtSDT.addActionListener(e -> traCuuKhachHang());
        txtSDT.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { khiSdtDoi(); }
            @Override public void removeUpdate(DocumentEvent e) { khiSdtDoi(); }
            @Override public void changedUpdate(DocumentEvent e) { khiSdtDoi(); }
        });
        body.add(txtSDT);

        JButton btnKiemTra = new RoundedButton("Tra cứu", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8);
        btnKiemTra.setBounds(340, y, 85, 28);
        btnKiemTra.addActionListener(e -> traCuuKhachHang());
        body.add(btnKiemTra);

        y += 35;
        // Tên khách & Hạng thẻ
        JLabel lblT = new JLabel("Tên chủ nuôi:");
        lblT.setFont(lblFont);
        lblT.setForeground(Theme.MUTED);
        lblT.setBounds(30, y, 110, 24);
        body.add(lblT);

        lblTenKhach = new JLabel("— Chưa có thông tin —");
        lblTenKhach.setFont(Theme.font(Font.BOLD, 13));
        lblTenKhach.setForeground(Theme.DARK);
        lblTenKhach.setBounds(150, y, 200, 24);
        body.add(lblTenKhach);

        lblHangThe = new JLabel("");
        lblHangThe.setFont(Theme.font(Font.PLAIN, 12));
        lblHangThe.setForeground(Theme.ACCENT);
        lblHangThe.setBounds(360, y, 150, 24);
        body.add(lblHangThe);

        y += 40;
        // Đường kẻ ngăn cách nhẹ
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(Theme.BORDER);
        sep1.setBounds(30, y, 480, 2);
        body.add(sep1);

        y += 15;
        // --- 3. Thông tin thú cưng ---
        JLabel lblP = new JLabel("Tên thú cưng:");
        lblP.setFont(lblFont);
        lblP.setForeground(Theme.TEXT);
        lblP.setBounds(30, y, 110, 28);
        body.add(lblP);

        cbTenPet = new JComboBox<>();
        cbTenPet.setEditable(true);
        cbTenPet.setFont(valFont);
        cbTenPet.setBounds(150, y, 180, 28);
        cbTenPet.setToolTipText("Chọn thú cưng đã có của khách hoặc gõ tên thú cưng mới");
        cbTenPet.addActionListener(e -> apDungThuCungDaChon());
        body.add(cbTenPet);

        JLabel lblW = new JLabel("Cân nặng:");
        lblW.setFont(lblFont);
        lblW.setForeground(Theme.TEXT);
        lblW.setBounds(350, y, 70, 28);
        body.add(lblW);

        spnCanNang = new JSpinner(new SpinnerNumberModel(4.5, 0.5, 60.0, 0.5));
        spnCanNang.setFont(valFont);
        spnCanNang.setBounds(430, y, 80, 28);
        body.add(spnCanNang);

        y += 40;
        JLabel lblL = new JLabel("Loài / Giống:");
        lblL.setFont(lblFont);
        lblL.setForeground(Theme.TEXT);
        lblL.setBounds(30, y, 110, 28);
        body.add(lblL);

        cbLoaiPet = new JComboBox<>(new String[]{
            "Chó Poodle", "Chó Corgi", "Chó Golden", "Chó Pug", "Chó Phốc sóc",
            "Mèo Anh lông ngắn", "Mèo Ba Tư", "Mèo Xiêm", "Mèo Mướp", "Thỏ cảnh", "Khác"
        });
        cbLoaiPet.setEditable(true);
        cbLoaiPet.setFont(valFont);
        cbLoaiPet.setBounds(150, y, 360, 28);
        body.add(cbLoaiPet);

        y += 40;
        // --- 4. Thời gian lưu trú ---
        JLabel lblD1 = new JLabel("Ngày giờ đến:");
        lblD1.setFont(lblFont);
        lblD1.setForeground(Theme.TEXT);
        lblD1.setBounds(30, y, 110, 28);
        body.add(lblD1);

        txtNgayDen = new JTextField(LocalDateTime.now().plusHours(2).format(DT_FMT));
        txtNgayDen.setFont(valFont);
        txtNgayDen.setBounds(150, y, 160, 28);
        body.add(txtNgayDen);

        JLabel lblD2 = new JLabel("Dự kiến trả:");
        lblD2.setFont(lblFont);
        lblD2.setForeground(Theme.TEXT);
        lblD2.setBounds(330, y, 80, 28);
        body.add(lblD2);

        txtNgayTra = new JTextField(LocalDateTime.now().plusDays(2).format(DT_FMT));
        txtNgayTra.setFont(valFont);
        txtNgayTra.setBounds(410, y, 100, 28);
        body.add(txtNgayTra);

        y += 40;
        // --- 5. Tiền đặt cọc & Ghi chú ---
        JLabel lblCoc = new JLabel("Tiền đặt cọc:");
        lblCoc.setFont(lblFont);
        lblCoc.setForeground(Theme.TEXT);
        lblCoc.setBounds(30, y, 110, 28);
        body.add(lblCoc);

        spnTienCoc = new JSpinner(new SpinnerNumberModel(100000, 0, 5000000, 50000));
        spnTienCoc.setFont(valFont);
        spnTienCoc.setBounds(150, y, 160, 28);
        body.add(spnTienCoc);

        JLabel lblDVT = new JLabel("VNĐ");
        lblDVT.setFont(valFont);
        lblDVT.setForeground(Theme.MUTED);
        lblDVT.setBounds(320, y, 40, 28);
        body.add(lblDVT);

        y += 40;
        JLabel lblG = new JLabel("Ghi chú ăn uống:");
        lblG.setFont(lblFont);
        lblG.setForeground(Theme.TEXT);
        lblG.setBounds(30, y, 110, 28);
        body.add(lblG);

        txtGhiChu = new JTextField("Ăn hạt 2 lần/ngày, thích chơi đồ chơi bóng");
        txtGhiChu.setFont(valFont);
        txtGhiChu.setBounds(150, y, 360, 28);
        body.add(txtGhiChu);

        return body;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new LineBorder(Theme.BORDER, 1));

        RoundedButton btnHuy = new RoundedButton("Hủy bỏ", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 8);
        RoundedButton btnXacNhan = new RoundedButton("✓ Xác Nhận Đặt Lồng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 8);

        btnHuy.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> thucHienDatLoi());

        footer.add(btnHuy);
        footer.add(btnXacNhan);
        return footer;
    }

    private void capNhatGiaTheoChuong() {
        String sel = (String) cbChuong.getSelectedItem();
        if (sel != null && sel.contains(" - ")) {
            String ma = sel.split(" - ")[0].trim();
            Chuong c = bus.timTheoMa(ma);
            if (c != null) {
                lblGiaPhong.setText("Giá: " + Theme.money((long) c.getGiaTheoNgay()) + "/ngày");
            }
        }
    }

    /** Đổi SĐT sau khi đã tra cứu -> xóa kết quả cũ để không đặt nhầm cho khách khác. */
    private void khiSdtDoi() {
        if (sdtDaTraCuu != null && !KhachHangBUS.chiSo(txtSDT.getText()).equals(sdtDaTraCuu)) {
            sdtDaTraCuu = null;
            khachMoi = false;
            lblTenKhach.setText("— Chưa có thông tin —");
            lblHangThe.setText("");
            petsKhach.clear();
            cbTenPet.removeAllItems();
        }
    }

    private String tenPetNhap() {
        Object o = cbTenPet.getEditor().getItem();
        return o == null ? "" : o.toString().trim();
    }

    /** Chọn thú cưng đã có của khách -> tự điền loài và cân nặng. */
    private void apDungThuCungDaChon() {
        ThuCung t = petsKhach.get(tenPetNhap().toLowerCase());
        if (t == null) return;
        if (t.getLoai() != null && !t.getLoai().isEmpty()) cbLoaiPet.setSelectedItem(t.getLoai());
        if (t.getCanNang() > 0) spnCanNang.setValue(Math.min(60.0, Math.max(0.5, t.getCanNang())));
    }

    private void traCuuKhachHang() {
        String raw = KhachHangBUS.chiSo(txtSDT.getText());
        if (raw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại chủ nuôi cần tra cứu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (raw.length() < 9 || raw.length() > 11) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 9–11 chữ số!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tra cứu trực tiếp trong CSDL khách hàng (cùng nguồn với panel Khách Hàng)
        KhachHang found;
        try {
            found = new KhachHangBUS().timTheoSdt(raw);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không tra cứu được khách hàng:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        petsKhach.clear();
        cbTenPet.removeAllItems();

        if (found != null) {
            khachMoi = false;
            sdtDaTraCuu = raw;
            lblTenKhach.setText(found.getHoTen());
            lblHangThe.setText("Hạng: " + found.getHangThe() + " (" + found.getDiemTichLuy() + " điểm)");
            for (ThuCung t : found.getDanhSachThuCung()) {
                petsKhach.put(t.getTen().toLowerCase(), t);
                cbTenPet.addItem(t.getTen());
            }
            String ghiChuPet;
            if (cbTenPet.getItemCount() > 0) {
                cbTenPet.setSelectedIndex(0);
                ghiChuPet = "\nThú cưng đã đăng ký: " + found.getThuCung();
            } else {
                ghiChuPet = "\nKhách chưa đăng ký thú cưng nào, hãy nhập tên bé để thêm mới.";
            }
            JOptionPane.showMessageDialog(this, "Đã tìm thấy khách hàng: " + found.getHoTen() + ghiChuPet,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String inputName = JOptionPane.showInputDialog(this,
                    "Khách hàng mới! Nhập họ tên chủ nuôi (sẽ được thêm vào danh sách Khách hàng khi đặt lồng):",
                    "Khách hàng mới", JOptionPane.QUESTION_MESSAGE);
            if (inputName != null && !inputName.trim().isEmpty()) {
                khachMoi = true;
                sdtDaTraCuu = raw;
                lblTenKhach.setText(inputName.trim().replaceAll("\\s+", " "));
                lblHangThe.setText("Khách mới (tự thêm)");
            } else {
                khachMoi = false;
                sdtDaTraCuu = null;
                lblTenKhach.setText("— Chưa có thông tin —");
                lblHangThe.setText("");
            }
        }
    }

    private void thucHienDatLoi() {
        String sel = (String) cbChuong.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lồng chuồng muốn đặt!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maChuong = sel.split(" - ")[0].trim();
        String sdt = KhachHangBUS.chiSo(txtSDT.getText());
        String tenKhach = lblTenKhach.getText();
        String tenPet = tenPetNhap();

        // Lồng chỉ nhận gửi khi có chủ: phải tra cứu SĐT (khách có sẵn hoặc khách mới được tạo khi đặt)
        if (sdt.isEmpty() || sdtDaTraCuu == null || !sdtDaTraCuu.equals(sdt) || tenKhach.contains("Chưa có thông tin")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT và bấm \"Tra cứu\" để xác định chủ nuôi trước khi đặt lồng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tenPet.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên bé thú cưng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime ngayDen;
        LocalDateTime ngayTra;
        try {
            ngayDen = LocalDateTime.parse(txtNgayDen.getText().trim(), DT_FMT);
        } catch (Exception ex) {
            ngayDen = LocalDateTime.now().plusHours(1);
        }
        try {
            ngayTra = LocalDateTime.parse(txtNgayTra.getText().trim(), DT_FMT);
        } catch (Exception ex) {
            ngayTra = LocalDateTime.now().plusDays(2);
        }

        double canNang = ((Number) spnCanNang.getValue()).doubleValue();
        double tienCoc = ((Number) spnTienCoc.getValue()).doubleValue();
        String loaiPet = (String) cbLoaiPet.getSelectedItem();
        String ghiChu = txtGhiChu.getText().trim();

        String maPhieu = "PDC-" + String.format("%03d", bus.layDanhSachPhieuDat().size() + 1);

        PhieuDatChuong pdc = new PhieuDatChuong(
            maPhieu, maChuong, tenKhach, sdt, tenPet, loaiPet, canNang,
            LocalDateTime.now(), ngayDen, ngayTra, tienCoc, "Đã đặt", ghiChu
        );

        if (bus.datChuong(pdc)) {
            JOptionPane.showMessageDialog(this, "Đặt lồng chuồng thành công!\nMã phiếu: " + maPhieu + "\nLồng cấp: " + maChuong + "\nThú cưng: " + tenPet
                    + (khachMoi ? "\nĐã thêm khách hàng mới: " + tenKhach : ""), "Đặt lồng thành công", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể đặt lồng chuồng này, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
