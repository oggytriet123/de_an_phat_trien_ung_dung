package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import Bus.DichVuBUS;
import Entity.DichVu;
import Entity.ThongKeDichVu;

/**
 * Giao diện Quản lý Dịch Vụ - Tinh gọn, trực quan và dễ hiểu cho đồ án Java Swing.
 */
public class DichVuPanel extends JPanel {
    private static final int SO_DONG_MOI_TRANG = 7;
    private static final String[] DS_LOAI = {"Tất cả", "Spa & Grooming", "Khám & Điều trị", "Tiêm phòng & Xét nghiệm", "Phẫu thuật", "Khách sạn & Lưu trú"};
    private static final String[] DS_SAP_XEP = {"Mới nhất", "Tên A-Z", "Giá cao nhất", "Giá thấp nhất"};

    private final DichVuBUS bus = new DichVuBUS();
    private List<DichVu> dsHienThi = new ArrayList<>();
    private String loaiDangLoc = "Tất cả";
    private String sapXepHienTai = DS_SAP_XEP[0];
    private int trangHienTai = 1;

    // 4 thẻ KPI thống kê
    private final StatCard cardTong = new StatCard("Tổng số dịch vụ");
    private final StatCard cardSpa = new StatCard("Spa & Grooming");
    private final StatCard cardKham = new StatCard("Khám & Điều trị");
    private final StatCard cardGiaTB = new StatCard("Đơn giá trung bình");

    // Bảng và tìm kiếm
    private final String[] tieuDeCot = {"Mã DV", "Tên dịch vụ", "Nhóm dịch vụ", "Đơn giá", "Thời lượng", "Đối tượng", "Trạng thái"};
    private final DefaultTableModel tableModel = new DefaultTableModel(tieuDeCot, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm tên dịch vụ, mã DV, quy trình, đối tượng...");
    private final List<RoundedButton> chipsLoai = new ArrayList<>();
    private final RoundedButton btnSort = new RoundedButton("", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10);
    private final JLabel lblTrangThai = new JLabel();
    private final JPanel pnlPhanTrang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public DichVuPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(taoPhanHeader(), BorderLayout.NORTH);
        top.add(taoPhanThongKe(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        add(taoPhanBang(), BorderLayout.CENTER);

        taiDuLieu();
        capNhatBang();

        addHierarchyListener(e -> {
            if (isShowing()) {
                taiDuLieu();
                capNhatBang();
            }
        });
    }

    // ----------------- 1. HEADER & CÁC NÚT CHỨC NĂNG -----------------
    private JPanel taoPhanHeader() {
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Quản lý Dịch Vụ");
        lblTieuDe.setFont(Theme.font(Font.BOLD, 22));
        lblTieuDe.setForeground(Theme.TEXT);

        JLabel lblPhuDe = new JLabel("Danh mục dịch vụ chăm sóc, khám chữa bệnh, bảng giá niêm yết và thời lượng");
        lblPhuDe.setFont(Theme.font(Font.PLAIN, 13));
        lblPhuDe.setForeground(Theme.MUTED);

        JPanel pnlTitle = new JPanel();
        pnlTitle.setOpaque(false);
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.add(lblTieuDe);
        pnlTitle.add(Box.createVerticalStrut(4));
        pnlTitle.add(lblPhuDe);

        RoundedButton btnExcel = new RoundedButton("Xuất Excel", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnXem = new RoundedButton("Xem chi tiết", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnSua = new RoundedButton("Chỉnh sửa", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnXoa = new RoundedButton("Xóa", Color.WHITE, new Color(0xFEE2E2), Theme.RED, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnThem = new RoundedButton("+ Thêm dịch vụ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(16, 9);

        btnExcel.addActionListener(e -> xuatExcel());
        btnThem.addActionListener(e -> moDialogThemSua(null));
        btnXem.addActionListener(e -> {
            DichVu dv = layDichVuDangChon();
            if (dv != null) moDialogChiTiet(dv);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnSua.addActionListener(e -> {
            DichVu dv = layDichVuDangChon();
            if (dv != null) moDialogThemSua(dv);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnXoa.addActionListener(e -> {
            DichVu dv = layDichVuDangChon();
            if (dv != null) xoaDichVu(dv);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnExcel);
        pnlNut.add(btnXem);
        pnlNut.add(btnSua);
        pnlNut.add(btnXoa);
        pnlNut.add(btnThem);

        head.add(pnlTitle, BorderLayout.WEST);
        head.add(pnlNut, BorderLayout.EAST);
        return head;
    }

    // ----------------- 2. THỐNG KÊ KPI (4 THẺ) -----------------
    private JPanel taoPhanThongKe() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(cardTong);
        row.add(cardSpa);
        row.add(cardKham);
        row.add(cardGiaTB);
        return row;
    }

    private void taiDuLieu() {
        try {
            bus.taiLai();
            ThongKeDichVu tk = bus.layThongKe();
            cardTong.set(Theme.number(tk.tongDichVu), tk.soDangCungCap + " dịch vụ đang mở", Theme.GREEN);
            cardSpa.set(Theme.number(tk.soSpaGrooming), "Tắm, tỉa, làm đẹp", new Color(0xBE185D));
            cardKham.set(Theme.number(tk.soKhamChua), "Khám bệnh & Phẫu thuật", new Color(0x0284C7));
            cardGiaTB.set(Theme.money(tk.giaTrungBinh), "Mức giá trung bình", new Color(0xD97706));
        } catch (SQLException e) {
            cardTong.set("—", "Lỗi CSDL", Theme.MUTED);
        }
    }

    // ----------------- 3. BẢNG DỮ LIỆU & THANH TÌM KIẾM -----------------
    private JPanel taoPhanBang() {
        RoundedPanel card = new RoundedPanel(14, Theme.CARD, null);
        card.setLayout(new BorderLayout());

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(14, 16, 14, 16));

        RoundedPanel searchBox = new RoundedPanel(10, Color.WHITE, Theme.BORDER);
        searchBox.setLayout(new BorderLayout());
        searchBox.setPreferredSize(new Dimension(300, 36));
        JLabel ic = new JLabel(Icons.of("search", 16, new Color(0x8795A0)));
        ic.setBorder(new EmptyBorder(0, 10, 0, 4));
        txtTim.setBorder(new EmptyBorder(0, 4, 0, 8));
        txtTim.setOpaque(false);
        txtTim.setFont(Theme.font(Font.PLAIN, 13));
        searchBox.add(ic, BorderLayout.WEST);
        searchBox.add(txtTim, BorderLayout.CENTER);

        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { trangHienTai = 1; capNhatBang(); }
            @Override public void removeUpdate(DocumentEvent e) { trangHienTai = 1; capNhatBang(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        JPanel pnlTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTrai.setOpaque(false);
        pnlTrai.add(searchBox);

        for (String loai : DS_LOAI) {
            RoundedButton chip = new RoundedButton(loai, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(12, 6);
            chip.setFont(Theme.font(Font.PLAIN, 12));
            chip.addActionListener(e -> {
                loaiDangLoc = loai;
                trangHienTai = 1;
                capNhatBang();
            });
            chipsLoai.add(chip);
            pnlTrai.add(chip);
        }

        btnSort.setFont(Theme.font(Font.PLAIN, 12));
        btnSort.pad(14, 6);
        btnSort.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            for (String s : DS_SAP_XEP) {
                JMenuItem mi = new JMenuItem(s);
                mi.addActionListener(ev -> {
                    sapXepHienTai = s;
                    trangHienTai = 1;
                    capNhatBang();
                });
                menu.add(mi);
            }
            menu.show(btnSort, 0, btnSort.getHeight() + 2);
        });

        toolbar.add(pnlTrai, BorderLayout.WEST);
        toolbar.add(btnSort, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // Bảng JTable - kẻ khung lưới đẹp
        JScrollPane scroll = new JScrollPane(table);
        Theme.styleTable(table, scroll);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(3).setCellRenderer(Theme.cellRight()); // Đơn giá căn phải
        table.getColumnModel().getColumn(4).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(6).setCellRenderer(Theme.cellCenter());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DichVu dv = layDichVuDangChon();
                    if (dv != null) moDialogChiTiet(dv);
                }
            }
        });

        card.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.ROW_LINE), new EmptyBorder(12, 16, 12, 16)));
        lblTrangThai.setFont(Theme.font(Font.PLAIN, 12));
        lblTrangThai.setForeground(Theme.MUTED);
        pnlPhanTrang.setOpaque(false);
        footer.add(lblTrangThai, BorderLayout.WEST);
        footer.add(pnlPhanTrang, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    // ----------------- 4. LỌC & ĐỔ DỮ LIỆU VÀO BẢNG -----------------
    private void capNhatBang() {
        List<DichVu> list = bus.timKiemVaLoc(txtTim.getText(), loaiDangLoc, "Tất cả", "Tất cả");

        if (sapXepHienTai.equals("Tên A-Z")) {
            Collator c = Collator.getInstance(new Locale("vi"));
            list.sort((a, b) -> c.compare(a.getTenDV(), b.getTenDV()));
        } else if (sapXepHienTai.equals("Giá cao nhất")) {
            list.sort((a, b) -> Double.compare(b.getGiaTien(), a.getGiaTien()));
        } else if (sapXepHienTai.equals("Giá thấp nhất")) {
            list.sort((a, b) -> Double.compare(a.getGiaTien(), b.getGiaTien()));
        }
        dsHienThi = list;

        int tongSoTrang = Math.max(1, (int) Math.ceil((double) dsHienThi.size() / SO_DONG_MOI_TRANG));
        trangHienTai = Math.min(Math.max(trangHienTai, 1), tongSoTrang);

        int tuDong = (trangHienTai - 1) * SO_DONG_MOI_TRANG;
        int denDong = Math.min(tuDong + SO_DONG_MOI_TRANG, dsHienThi.size());

        tableModel.setRowCount(0);
        for (int i = tuDong; i < denDong; i++) {
            DichVu dv = dsHienThi.get(i);
            String thoiGian = dv.getThoiGianThucHien() >= 1440 ? (dv.getThoiGianThucHien() / 1440 + " ngày") : (dv.getThoiGianThucHien() + " phút");
            tableModel.addRow(new Object[] {
                dv.getMaDV(),
                dv.getTenDV(),
                dv.getLoaiDV(),
                Theme.money((long) dv.getGiaTien()),
                thoiGian,
                dv.getDoiTuong(),
                dv.getTrangThai()
            });
        }

        lblTrangThai.setText(dsHienThi.isEmpty() ? "Không tìm thấy dịch vụ nào phù hợp."
                : "Hiển thị " + (tuDong + 1) + "–" + denDong + " trong " + dsHienThi.size() + " dịch vụ");
        btnSort.setText("Sắp xếp: " + sapXepHienTai);

        for (RoundedButton b : chipsLoai) {
            boolean active = b.getText().equals(loaiDangLoc);
            if (active) b.style(Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null);
            else b.style(Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER);
        }

        veNutPhanTrang(tongSoTrang);
    }

    private void veNutPhanTrang(int tongTrang) {
        pnlPhanTrang.removeAll();
        JButton btnTruoc = taoNutTrang("‹", trangHienTai - 1, trangHienTai > 1, false);
        pnlPhanTrang.add(btnTruoc);

        for (int p = 1; p <= tongTrang; p++) {
            if (p == 1 || p == tongTrang || (p >= trangHienTai - 1 && p <= trangHienTai + 1)) {
                pnlPhanTrang.add(taoNutTrang(String.valueOf(p), p, true, p == trangHienTai));
            }
        }

        JButton btnSau = taoNutTrang("›", trangHienTai + 1, trangHienTai < tongTrang, false);
        pnlPhanTrang.add(btnSau);
        pnlPhanTrang.revalidate();
        pnlPhanTrang.repaint();
    }

    private RoundedButton taoNutTrang(String chu, int trangDen, boolean enabled, boolean active) {
        RoundedButton b = active ? new RoundedButton(chu, Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8)
                                 : new RoundedButton(chu, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8);
        b.setPreferredSize(new Dimension(32, 32));
        b.setEnabled(enabled);
        b.addActionListener(e -> { trangHienTai = trangDen; capNhatBang(); });
        return b;
    }

    private DichVu layDichVuDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int indexTrongDS = (trangHienTai - 1) * SO_DONG_MOI_TRANG + row;
        return (indexTrongDS < dsHienThi.size()) ? dsHienThi.get(indexTrongDS) : null;
    }

    // ----------------- 5. DIALOG THÊM / SỬA DỊCH VỤ -----------------
    private void moDialogThemSua(DichVu dvHienTai) {
        boolean laSua = (dvHienTai != null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), laSua ? "Chỉnh sửa dịch vụ" : "Thêm dịch vụ mới", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtTenDV = taoTextField();
        JComboBox<String> cboLoai = new JComboBox<>(new String[] {"Spa & Grooming", "Khám & Điều trị", "Tiêm phòng & Xét nghiệm", "Phẫu thuật", "Khách sạn & Lưu trú"});
        JTextField txtGia = taoTextField();
        JSpinner spnThoiGian = new JSpinner(new SpinnerNumberModel(30, 5, 10080, 5));
        JComboBox<String> cboDoiTuong = new JComboBox<>(new String[] {"Tất cả thú cưng", "Chó & Mèo", "Chỉ dành cho Chó", "Chỉ dành cho Mèo"});
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {"Đang cung cấp", "Tạm ngưng"});
        JTextArea txtMoTa = new JTextArea(3, 20);
        txtMoTa.setBorder(BorderFactory.createLineBorder(new Color(0xD5DCE1)));

        if (laSua) {
            txtTenDV.setText(dvHienTai.getTenDV());
            cboLoai.setSelectedItem(dvHienTai.getLoaiDV());
            txtGia.setText(String.format(Locale.ROOT, "%.0f", dvHienTai.getGiaTien()));
            spnThoiGian.setValue(dvHienTai.getThoiGianThucHien());
            cboDoiTuong.setSelectedItem(dvHienTai.getDoiTuong());
            cboTrangThai.setSelectedItem(dvHienTai.getTrangThai());
            txtMoTa.setText(dvHienTai.getMoTa());
        }

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 8));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setBackground(Color.WHITE);

        form.add(new JLabel("Tên dịch vụ (*):")); form.add(txtTenDV);
        form.add(new JLabel("Nhóm dịch vụ (*):")); form.add(cboLoai);
        form.add(new JLabel("Đơn giá (VNĐ) (*):")); form.add(txtGia);
        form.add(new JLabel("Thời lượng (phút):")); form.add(spnThoiGian);
        form.add(new JLabel("Đối tượng áp dụng:")); form.add(cboDoiTuong);
        form.add(new JLabel("Trạng thái:")); form.add(cboTrangThai);

        JPanel pnlMoTa = new JPanel(new BorderLayout(0, 4));
        pnlMoTa.setOpaque(false);
        pnlMoTa.add(new JLabel("Mô tả / Quy trình dịch vụ:"), BorderLayout.NORTH);
        pnlMoTa.add(new JScrollPane(txtMoTa), BorderLayout.CENTER);

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 8));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.add(form, BorderLayout.NORTH);
        pnlCenter.add(pnlMoTa, BorderLayout.CENTER);
        pnlCenter.setBorder(new EmptyBorder(0, 20, 10, 20));

        RoundedButton btnLuu = new RoundedButton("Lưu dịch vụ", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 8);
        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(18, 8);
        btnHuy.addActionListener(e -> dlg.dispose());

        btnLuu.addActionListener(e -> {
            String ten = txtTenDV.getText().trim();
            if (ten.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên dịch vụ!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }

            double gia = 0;
            try { gia = Double.parseDouble(txtGia.getText().trim().replaceAll("[^0-9.]", "")); }
            catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Đơn giá phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }

            DichVu dv = new DichVu(laSua ? dvHienTai.getMaDV() : null, ten, (String) cboLoai.getSelectedItem(),
                    gia, (int) spnThoiGian.getValue(), txtMoTa.getText().trim(),
                    (String) cboTrangThai.getSelectedItem(), (String) cboDoiTuong.getSelectedItem());

            try {
                if (laSua) {
                    bus.capNhat(dv);
                    JOptionPane.showMessageDialog(dlg, "Cập nhật thành công!");
                } else {
                    String maMoi = bus.them(dv);
                    JOptionPane.showMessageDialog(dlg, "Đã thêm thành công dịch vụ: " + maMoi);
                }
                dlg.dispose();
                taiDuLieu();
                capNhatBang();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlNut.setBackground(Color.WHITE);
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuu);

        dlg.setLayout(new BorderLayout());
        dlg.add(pnlCenter, BorderLayout.CENTER);
        dlg.add(pnlNut, BorderLayout.SOUTH);
        dlg.setSize(480, 440);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ----------------- 6. XEM CHI TIẾT DỊCH VỤ -----------------
    private void moDialogChiTiet(DichVu dv) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Dịch vụ: " + dv.getTenDV() + " (" + dv.getMaDV() + ")", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);

        p.add(new JLabel("Mã dịch vụ:")); p.add(Theme.boldLbl(dv.getMaDV()));
        p.add(new JLabel("Tên dịch vụ:")); p.add(new JLabel(dv.getTenDV()));
        p.add(new JLabel("Đơn giá niêm yết:")); p.add(Theme.boldLbl(Theme.money((long) dv.getGiaTien())));
        p.add(new JLabel("Thời lượng & Đối tượng:")); p.add(new JLabel(dv.getThoiGianThucHien() + " phút · " + dv.getDoiTuong()));
        p.add(new JLabel("Mô tả / Quy trình:")); p.add(new JLabel("<html>" + (dv.getMoTa().isEmpty() ? "Theo quy trình chuẩn" : dv.getMoTa()) + "</html>"));

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dlg.dispose());
        JPanel pnlDong = new JPanel(); pnlDong.setBackground(Color.WHITE); pnlDong.add(btnDong);
        dlg.add(pnlDong, BorderLayout.SOUTH);
        dlg.setSize(440, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ----------------- 7. XÓA AN TOÀN -----------------
    private void xoaDichVu(DichVu dv) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa dịch vụ \"" + dv.getTenDV() + "\" (" + dv.getMaDV() + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                bus.xoa(dv.getMaDV());
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                taiDuLieu();
                capNhatBang();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa: " + ex.getMessage(), "Ràng buộc dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ----------------- 8. XUẤT FILE EXCEL (CSV UTF-8) -----------------
    private void xuatExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("DanhSachDichVu.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Mã DV,Tên dịch vụ,Nhóm loại,Đơn giá,Thời lượng,Đối tượng,Trạng thái\r\n");
            for (DichVu dv : dsHienThi) {
                w.write(String.format("\"%s\",\"%s\",\"%s\",\"%.0f\",\"%d\",\"%s\",\"%s\"\r\n",
                        dv.getMaDV(), dv.getTenDV(), dv.getLoaiDV(), dv.getGiaTien(),
                        dv.getThoiGianThucHien(), dv.getDoiTuong(), dv.getTrangThai()));
            }
            JOptionPane.showMessageDialog(this, "Xuất file thành công: " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JTextField taoTextField() {
        JTextField tf = new JTextField();
        tf.setFont(Theme.font(Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xD5DCE1)), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    private static class StatCard extends RoundedPanel {
        private final JLabel lblGiaTri = new JLabel("—");
        private final JLabel lblGhiChu = new JLabel(" ");

        StatCard(String tieuDe) {
            super(14, Theme.CARD, null);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(12, 14, 12, 14));

            JLabel lblTieuDe = new JLabel(tieuDe);
            lblTieuDe.setFont(Theme.font(Font.PLAIN, 12));
            lblTieuDe.setForeground(Theme.MUTED);

            lblGiaTri.setFont(Theme.font(Font.BOLD, 20));
            lblGiaTri.setForeground(Theme.TEXT);

            lblGhiChu.setFont(Theme.font(Font.PLAIN, 11));
            lblGhiChu.setForeground(Theme.MUTED);

            add(lblTieuDe);
            add(Box.createVerticalStrut(4));
            add(lblGiaTri);
            add(Box.createVerticalStrut(4));
            add(lblGhiChu);
        }

        void set(String v, String n, Color c) {
            lblGiaTri.setText(v);
            lblGhiChu.setText(n);
            lblGhiChu.setForeground(c);
        }
    }
}