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
import java.util.Comparator;
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

import Bus.ThuCungBUS;
import DAO.ThuCungDAO;
import Entity.ThongKeThuCung;
import Entity.ThuCung;

/**
 * Giao diện Quản lý Thú cưng - Thiết kế trực quan, dễ hiểu cho đồ án Java Swing.
 */
public class ThuCungPanel extends JPanel {
    private static final int SO_DONG_MOI_TRANG = 7;
    private static final String[] DS_LOAI = {"Tất cả", "Chó", "Mèo", "Thỏ", "Khác"};
    private static final String[] DS_SAP_XEP = {"Mới nhất", "Tên A-Z", "Cân nặng cao nhất", "Cân nặng thấp nhất"};

    private final ThuCungBUS bus = new ThuCungBUS();
    private List<ThuCung> dsHienThi = new ArrayList<>();
    private String loaiDangLoc = "Tất cả";
    private String sapXepHienTai = DS_SAP_XEP[0];
    private int trangHienTai = 1;

    // 4 thẻ thống kê trên cùng
    private final StatCard cardTong = new StatCard("Tổng số thú cưng");
    private final StatCard cardCho = new StatCard("Số lượng Chó");
    private final StatCard cardMeo = new StatCard("Số lượng Mèo");
    private final StatCard cardCanhBao = new StatCard("Cần theo dõi / Cảnh báo");

    // Bảng và tìm kiếm
    private final String[] tieuDeCot = {"Mã thú cưng", "Tên thú cưng", "Loài", "Giống loài", "Giới tính", "Tuổi", "Cân nặng", "Chủ nuôi", "Tình trạng"};
    private final DefaultTableModel tableModel = new DefaultTableModel(tieuDeCot, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm theo tên bé, mã PET, giống, tên chủ, số điện thoại...");
    private final List<RoundedButton> chipsLoai = new ArrayList<>();
    private final RoundedButton btnSort = new RoundedButton("", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10);
    private final JLabel lblTrangThai = new JLabel();
    private final JPanel pnlPhanTrang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public ThuCungPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        // Khung trên: Tiêu đề + 4 thẻ thống kê
        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(taoPhanHeader(), BorderLayout.NORTH);
        top.add(taoPhanThongKe(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // Khung giữa: Bảng dữ liệu + Thanh công cụ
        add(taoPhanBang(), BorderLayout.CENTER);

        taiDuLieu();
        capNhatBang();

        // Tự động load lại khi mở lại tab này
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

        JLabel lblTieuDe = new JLabel("Quản lý Thú Cưng");
        lblTieuDe.setFont(Theme.font(Font.BOLD, 22));
        lblTieuDe.setForeground(Theme.TEXT);

        JLabel lblPhuDe = new JLabel("Hồ sơ sức khỏe, chủng loại, thể trạng và thông tin liên hệ chủ nuôi");
        lblPhuDe.setFont(Theme.font(Font.PLAIN, 13));
        lblPhuDe.setForeground(Theme.MUTED);

        JPanel pnlTitle = new JPanel();
        pnlTitle.setOpaque(false);
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.add(lblTieuDe);
        pnlTitle.add(Box.createVerticalStrut(4));
        pnlTitle.add(lblPhuDe);

        // Nút chức năng: Xuất Excel, Xem chi tiết, Sửa, Xóa, Thêm mới
        RoundedButton btnExcel = new RoundedButton("Xuất Excel", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnXem = new RoundedButton("Xem chi tiết", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnSua = new RoundedButton("Chỉnh sửa", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnXoa = new RoundedButton("Xóa", Color.WHITE, new Color(0xFEE2E2), Theme.RED, Theme.BORDER, 10).pad(14, 9);
        RoundedButton btnThem = new RoundedButton("+ Thêm thú cưng", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(16, 9);

        btnExcel.addActionListener(e -> xuatExcel());
        btnThem.addActionListener(e -> moDialogThemSua(null));
        btnXem.addActionListener(e -> {
            ThuCung tc = layThuCungDangChon();
            if (tc != null) moDialogChiTiet(tc);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn một thú cưng trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnSua.addActionListener(e -> {
            ThuCung tc = layThuCungDangChon();
            if (tc != null) moDialogThemSua(tc);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn thú cưng cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnXoa.addActionListener(e -> {
            ThuCung tc = layThuCungDangChon();
            if (tc != null) xoaThuCung(tc);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn thú cưng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
        row.add(cardCho);
        row.add(cardMeo);
        row.add(cardCanhBao);
        return row;
    }

    private void taiDuLieu() {
        try {
            bus.taiLai();
            ThongKeThuCung tk = bus.layThongKe();
            cardTong.set(Theme.number(tk.tongThuCung), "+" + tk.soTiepNhanThang + " bé mới tháng này", Theme.GREEN);
            cardCho.set(Theme.number(tk.soCho), "Chiếm " + (tk.tongThuCung > 0 ? (tk.soCho * 100 / tk.tongThuCung) : 0) + "% tổng số", Theme.MUTED);
            cardMeo.set(Theme.number(tk.soMeo), "Chiếm " + (tk.tongThuCung > 0 ? (tk.soMeo * 100 / tk.tongThuCung) : 0) + "% tổng số", Theme.MUTED);
            cardCanhBao.set(Theme.number(tk.soCanhBao), tk.soCanhBao > 0 ? "Có dị ứng hoặc đang theo dõi" : "Sức khỏe ổn định", tk.soCanhBao > 0 ? new Color(0xD97706) : Theme.GREEN);
        } catch (SQLException e) {
            cardTong.set("—", "Lỗi CSDL", Theme.MUTED);
        }
    }

    // ----------------- 3. BẢNG DỮ LIỆU & THANH TÌM KIẾM -----------------
    private JPanel taoPhanBang() {
        RoundedPanel card = new RoundedPanel(14, Theme.CARD, null);
        card.setLayout(new BorderLayout());

        // Thanh công cụ: Ô tìm kiếm + Chip lọc loài + Nút sắp xếp
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

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(0).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(2).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(4).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(5).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(6).setCellRenderer(Theme.cellRight());
        table.getColumnModel().getColumn(8).setCellRenderer(Theme.cellCenter());

        // Double click vào dòng để mở chi tiết
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ThuCung tc = layThuCungDangChon();
                    if (tc != null) moDialogChiTiet(tc);
                }
            }
        });

        card.add(scroll, BorderLayout.CENTER);

        // Footer phân trang
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

    // ----------------- 4. LỌC, SẮP XẾP & ĐỔ DỮ LIỆU VÀO BẢNG -----------------
    private void capNhatBang() {
        // Lấy danh sách lọc từ BUS
        List<ThuCung> list = bus.timKiemVaLoc(txtTim.getText(), loaiDangLoc, "Tất cả", "Tất cả");

        // Sắp xếp
        if (sapXepHienTai.equals("Tên A-Z")) {
            Collator c = Collator.getInstance(new Locale("vi"));
            list.sort((a, b) -> c.compare(a.getTen(), b.getTen()));
        } else if (sapXepHienTai.equals("Cân nặng cao nhất")) {
            list.sort((a, b) -> Double.compare(b.getCanNang(), a.getCanNang()));
        } else if (sapXepHienTai.equals("Cân nặng thấp nhất")) {
            list.sort((a, b) -> Double.compare(a.getCanNang(), b.getCanNang()));
        }
        dsHienThi = list;

        // Phân trang
        int tongSoTrang = Math.max(1, (int) Math.ceil((double) dsHienThi.size() / SO_DONG_MOI_TRANG));
        trangHienTai = Math.min(Math.max(trangHienTai, 1), tongSoTrang);

        int tuDong = (trangHienTai - 1) * SO_DONG_MOI_TRANG;
        int denDong = Math.min(tuDong + SO_DONG_MOI_TRANG, dsHienThi.size());

        // Đổ dữ liệu vào TableModel
        tableModel.setRowCount(0);
        for (int i = tuDong; i < denDong; i++) {
            ThuCung tc = dsHienThi.get(i);
            String chuNuoi = tc.getTenChuNuoi();
            if (tc.getSdtChuNuoi() != null && !tc.getSdtChuNuoi().isEmpty()) {
                chuNuoi += " (" + tc.getSdtChuNuoi() + ")";
            }
            tableModel.addRow(new Object[] {
                tc.getPetID(),
                tc.getTen(),
                tc.getLoai(),
                tc.getGiong().isEmpty() ? "—" : tc.getGiong(),
                tc.getGioiTinh(),
                tc.getTuoi() + " tuổi",
                tc.getCanNang() > 0 ? (tc.getCanNang() + " kg") : "—",
                chuNuoi,
                tc.getTrangThai()
            });
        }

        lblTrangThai.setText(dsHienThi.isEmpty() ? "Không tìm thấy thú cưng nào phù hợp."
                : "Hiển thị " + (tuDong + 1) + "–" + denDong + " trong " + dsHienThi.size() + " thú cưng");
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

    private ThuCung layThuCungDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int indexTrongDS = (trangHienTai - 1) * SO_DONG_MOI_TRANG + row;
        return (indexTrongDS < dsHienThi.size()) ? dsHienThi.get(indexTrongDS) : null;
    }

    // ----------------- 5. DIALOG THÊM / SỬA THÚ CƯNG -----------------
    private void moDialogThemSua(ThuCung tcHienTai) {
        boolean laSua = (tcHienTai != null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), laSua ? "Chỉnh sửa thông tin bé" : "Tiếp nhận thú cưng mới", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtTen = taoTextField();
        JComboBox<String> cboLoai = new JComboBox<>(new String[] {"Chó", "Mèo", "Thỏ", "Chuột Hamster", "Khác"});
        JTextField txtGiong = taoTextField();
        JComboBox<String> cboGioiTinh = new JComboBox<>(new String[] {"Đực", "Cái", "Không rõ"});
        JSpinner spnTuoi = new JSpinner(new SpinnerNumberModel(1, 0, 50, 1));
        JTextField txtCanNang = taoTextField();
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {"Khỏe mạnh", "Đang theo dõi", "Cần chú ý", "Đang tiêm phòng", "Đang điều trị"});
        JTextArea txtCanhBao = new JTextArea(3, 20);
        txtCanhBao.setBorder(BorderFactory.createLineBorder(new Color(0xD5DCE1)));

        // Chọn chủ nuôi từ Khách hàng
        JComboBox<ThuCungDAO.KhachHangOption> cboChuNuoi = new JComboBox<>();
        try {
            cboChuNuoi.addItem(new ThuCungDAO.KhachHangOption("", "-- Khách vãng lai (Chưa đăng ký) --", "", ""));
            for (ThuCungDAO.KhachHangOption kh : bus.layDanhSachKhachHang()) cboChuNuoi.addItem(kh);
        } catch (SQLException ignored) {}

        if (laSua) {
            txtTen.setText(tcHienTai.getTen());
            cboLoai.setSelectedItem(tcHienTai.getLoai());
            txtGiong.setText(tcHienTai.getGiong());
            cboGioiTinh.setSelectedItem(tcHienTai.getGioiTinh());
            spnTuoi.setValue(tcHienTai.getTuoi());
            txtCanNang.setText(tcHienTai.getCanNang() > 0 ? String.valueOf(tcHienTai.getCanNang()) : "");
            cboTrangThai.setSelectedItem(tcHienTai.getTrangThai());
            txtCanhBao.setText(tcHienTai.getTheCanhBao());
            for (int i = 0; i < cboChuNuoi.getItemCount(); i++) {
                ThuCungDAO.KhachHangOption op = cboChuNuoi.getItemAt(i);
                if (op.maKH.equalsIgnoreCase(tcHienTai.getMaKH())) { cboChuNuoi.setSelectedIndex(i); break; }
            }
        }

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setBackground(Color.WHITE);

        form.add(new JLabel("Tên thú cưng (*):")); form.add(txtTen);
        form.add(new JLabel("Loài (*):")); form.add(cboLoai);
        form.add(new JLabel("Giống loài:")); form.add(txtGiong);
        form.add(new JLabel("Giới tính:")); form.add(cboGioiTinh);
        form.add(new JLabel("Tuổi:")); form.add(spnTuoi);
        form.add(new JLabel("Cân nặng (kg):")); form.add(txtCanNang);
        form.add(new JLabel("Chủ nuôi sở hữu:")); form.add(cboChuNuoi);
        form.add(new JLabel("Tình trạng sức khỏe:")); form.add(cboTrangThai);

        JPanel pnlGhiChu = new JPanel(new BorderLayout(0, 4));
        pnlGhiChu.setOpaque(false);
        pnlGhiChu.add(new JLabel("Thẻ cảnh báo / Ghi chú dị ứng:"), BorderLayout.NORTH);
        pnlGhiChu.add(new JScrollPane(txtCanhBao), BorderLayout.CENTER);

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 8));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.add(form, BorderLayout.NORTH);
        pnlCenter.add(pnlGhiChu, BorderLayout.CENTER);
        pnlCenter.setBorder(new EmptyBorder(0, 20, 10, 20));

        RoundedButton btnLuu = new RoundedButton("Lưu thông tin", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 8);
        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(18, 8);
        btnHuy.addActionListener(e -> dlg.dispose());

        btnLuu.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Vui lòng nhập tên bé!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }

            double canNang = 0;
            if (!txtCanNang.getText().trim().isEmpty()) {
                try { canNang = Double.parseDouble(txtCanNang.getText().trim().replace(',', '.')); }
                catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Cân nặng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            }

            ThuCungDAO.KhachHangOption khOpt = (ThuCungDAO.KhachHangOption) cboChuNuoi.getSelectedItem();
            String maKH = (khOpt != null && !khOpt.maKH.isEmpty()) ? khOpt.maKH : null;

            ThuCung tc = new ThuCung(laSua ? tcHienTai.getPetID() : null, ten, (String) cboLoai.getSelectedItem(),
                    txtGiong.getText().trim(), (String) cboGioiTinh.getSelectedItem(),
                    (int) spnTuoi.getValue(), canNang, txtCanhBao.getText().trim(),
                    (String) cboTrangThai.getSelectedItem(), maKH);

            try {
                if (laSua) {
                    bus.capNhat(tc);
                    JOptionPane.showMessageDialog(dlg, "Cập nhật thành công!");
                } else {
                    String maMoi = bus.them(tc);
                    JOptionPane.showMessageDialog(dlg, "Tiếp nhận thành công bé với mã: " + maMoi);
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
        dlg.setSize(480, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ----------------- 6. XEM CHI TIẾT HỒ SƠ -----------------
    private void moDialogChiTiet(ThuCung tc) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Hồ sơ: " + tc.getTen() + " (" + tc.getPetID() + ")", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);

        p.add(new JLabel("Mã thú cưng:")); p.add(new JLabel("<b>" + tc.getPetID() + "</b>"));
        p.add(new JLabel("Tên thú cưng:")); p.add(new JLabel(tc.getTen()));
        p.add(new JLabel("Chủng loại:")); p.add(new JLabel(tc.getLoai() + " - " + (tc.getGiong().isEmpty() ? "Không rõ giống" : tc.getGiong())));
        p.add(new JLabel("Thể trạng:")); p.add(new JLabel(tc.getGioiTinh() + " · " + tc.getTuoi() + " tuổi · " + (tc.getCanNang() > 0 ? (tc.getCanNang() + "kg") : "Chưa cân")));
        p.add(new JLabel("Chủ nuôi:")); p.add(new JLabel(tc.getTenChuNuoi() + " (" + (tc.getSdtChuNuoi().isEmpty() ? "Không có SĐT" : tc.getSdtChuNuoi()) + ")"));
        p.add(new JLabel("Cảnh báo/Lưu ý:")); p.add(new JLabel("<html>" + (tc.getTheCanhBao().isEmpty() ? "Khỏe mạnh bình thường" : tc.getTheCanhBao()) + "</html>"));

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dlg.dispose());
        JPanel pnlDong = new JPanel(); pnlDong.setBackground(Color.WHITE); pnlDong.add(btnDong);
        dlg.add(pnlDong, BorderLayout.SOUTH);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ----------------- 7. XÓA AN TOÀN -----------------
    private void xoaThuCung(ThuCung tc) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa bé \"" + tc.getTen() + "\" (" + tc.getPetID() + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                bus.xoa(tc.getPetID());
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                taiDuLieu();
                capNhatBang();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa: " + ex.getMessage(), "Cảnh báo ràng buộc", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ----------------- 8. XUẤT FILE EXCEL (CSV UTF-8) -----------------
    private void xuatExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("DanhSachThuCung.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF'); // BOM tiếng Việt
            w.write("Mã,Tên,Loài,Giống,Giới tính,Tuổi,Cân nặng,Chủ nuôi,SĐT,Tình trạng\r\n");
            for (ThuCung t : dsHienThi) {
                w.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\",\"%.1f\",\"%s\",\"%s\",\"%s\"\r\n",
                        t.getPetID(), t.getTen(), t.getLoai(), t.getGiong(), t.getGioiTinh(), t.getTuoi(), t.getCanNang(),
                        t.getTenChuNuoi(), t.getSdtChuNuoi(), t.getTrangThai()));
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

    // Thẻ thống kê nhỏ gọn
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
