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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import Bus.NhanVienBUS;
import DAO.NhanVienDAO;
import Entity.NhanVien;
import Entity.TaiKhoan;
import Entity.ThongKeNhanVien;

/**
 * Giao diện Quản lý Nhân Viên - Thiết kế tinh gọn, dễ hiểu và chuẩn mực.
 */
public class NhanVienPanel extends JPanel {
    private static final int SO_DONG_MOI_TRANG = 7;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] DS_VAI_TRO = {"Tất cả", "Quản lý", "Bán hàng", "Kho", "Bác sĩ"};
    private static final String[] DS_SAP_XEP = {"Mới nhất", "Tên A-Z", "Mã NV tăng dần", "Vai trò"};

    private final NhanVienBUS bus = new NhanVienBUS();
    private final TaiKhoan taiKhoanHienTai;
    private List<NhanVien> dsHienThi = new ArrayList<>();
    private String vaiTroDangLoc = "Tất cả";
    private String sapXepHienTai = DS_SAP_XEP[0];
    private int trangHienTai = 1;

    // 4 thẻ KPI thống kê
    private final StatCard cardTong = new StatCard("Tổng số nhân viên");
    private final StatCard cardBacSi = new StatCard("Bác sĩ Thú y");
    private final StatCard cardBanHang = new StatCard("Nhân viên Bán hàng");
    private final StatCard cardKhoQL = new StatCard("Kho & Quản trị");

    // Bảng và tìm kiếm
    private final String[] tieuDeCot = {"Mã NV", "Họ và tên", "Tên đăng nhập", "Vai trò", "Số điện thoại", "Email", "Ca làm", "Trạng thái"};
    private final DefaultTableModel tableModel = new DefaultTableModel(tieuDeCot, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final PlaceholderField txtTim = new PlaceholderField("Tìm tên nhân viên, mã NV, tài khoản, SĐT, email...");
    private final List<RoundedButton> chipsVaiTro = new ArrayList<>();
    private final RoundedButton btnSort = new RoundedButton("", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10);
    private final JLabel lblTrangThai = new JLabel();
    private final JPanel pnlPhanTrang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    public NhanVienPanel() {
        this(null);
    }

    public NhanVienPanel(TaiKhoan taiKhoanHienTai) {
        super(new BorderLayout(0, 16));
        this.taiKhoanHienTai = taiKhoanHienTai;
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

        JLabel lblTieuDe = new JLabel("Quản lý Nhân Viên");
        lblTieuDe.setFont(Theme.font(Font.BOLD, 22));
        lblTieuDe.setForeground(Theme.TEXT);

        JLabel lblPhuDe = new JLabel("Hồ sơ nhân sự, chức danh phân quyền, ca làm việc và chuyên môn y tế");
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
        RoundedButton btnThem = new RoundedButton("+ Thêm nhân viên", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(16, 9);

        btnExcel.addActionListener(e -> xuatExcel());
        btnThem.addActionListener(e -> moDialogThemSua(null));
        btnXem.addActionListener(e -> {
            NhanVien nv = layNhanVienDangChon();
            if (nv != null) moDialogChiTiet(nv);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnSua.addActionListener(e -> {
            NhanVien nv = layNhanVienDangChon();
            if (nv != null) moDialogThemSua(nv);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        btnXoa.addActionListener(e -> {
            NhanVien nv = layNhanVienDangChon();
            if (nv != null) xoaNhanVien(nv);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
        row.add(cardBacSi);
        row.add(cardBanHang);
        row.add(cardKhoQL);
        return row;
    }

    private void taiDuLieu() {
        try {
            bus.taiLai();
            ThongKeNhanVien tk = bus.layThongKe();
            cardTong.set(Theme.number(tk.tongNhanVien), tk.soDangLamViec + " người đang hoạt động", Theme.GREEN);
            cardBacSi.set(Theme.number(tk.soBacSi), "Phụ trách khám chữa bệnh", new Color(0x0284C7));
            cardBanHang.set(Theme.number(tk.soBanHang), "Tư vấn & Bán hàng", Theme.MUTED);
            cardKhoQL.set(Theme.number(tk.soKho + tk.soQuanLy), tk.soQuanLy + " Quản lý · " + tk.soKho + " Thủ kho", new Color(0xD97706));
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

        for (String vt : DS_VAI_TRO) {
            RoundedButton chip = new RoundedButton(vt, Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(12, 6);
            chip.setFont(Theme.font(Font.PLAIN, 12));
            chip.addActionListener(e -> {
                vaiTroDangLoc = vt;
                trangHienTai = 1;
                capNhatBang();
            });
            chipsVaiTro.add(chip);
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
        table.getColumnModel().getColumn(3).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(4).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(6).setCellRenderer(Theme.cellCenter());
        table.getColumnModel().getColumn(7).setCellRenderer(Theme.cellCenter());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    NhanVien nv = layNhanVienDangChon();
                    if (nv != null) moDialogChiTiet(nv);
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
        List<NhanVien> list = bus.timKiemVaLoc(txtTim.getText(), vaiTroDangLoc, "Tất cả", "Tất cả");

        if (sapXepHienTai.equals("Tên A-Z")) {
            Collator c = Collator.getInstance(new Locale("vi"));
            list.sort((a, b) -> c.compare(a.getHoTen(), b.getHoTen()));
        } else if (sapXepHienTai.equals("Mã NV tăng dần")) {
            list.sort((a, b) -> a.getMaNV().compareTo(b.getMaNV()));
        } else if (sapXepHienTai.equals("Vai trò")) {
            list.sort((a, b) -> Integer.compare(a.getMaQuyen(), b.getMaQuyen()));
        }
        dsHienThi = list;

        int tongSoTrang = Math.max(1, (int) Math.ceil((double) dsHienThi.size() / SO_DONG_MOI_TRANG));
        trangHienTai = Math.min(Math.max(trangHienTai, 1), tongSoTrang);

        int tuDong = (trangHienTai - 1) * SO_DONG_MOI_TRANG;
        int denDong = Math.min(tuDong + SO_DONG_MOI_TRANG, dsHienThi.size());

        tableModel.setRowCount(0);
        for (int i = tuDong; i < denDong; i++) {
            NhanVien nv = dsHienThi.get(i);
            tableModel.addRow(new Object[] {
                nv.getMaNV(),
                nv.getHoTen(),
                "@" + nv.getTenDangNhap(),
                nv.getTenQuyen(),
                nv.getSoDienThoai().isEmpty() ? "—" : nv.getSoDienThoai(),
                nv.getEmail().isEmpty() ? "—" : nv.getEmail(),
                nv.getCaLamViec(),
                nv.getTrangThai()
            });
        }

        lblTrangThai.setText(dsHienThi.isEmpty() ? "Không tìm thấy nhân viên nào phù hợp."
                : "Hiển thị " + (tuDong + 1) + "–" + denDong + " trong " + dsHienThi.size() + " nhân viên");
        btnSort.setText("Sắp xếp: " + sapXepHienTai);

        for (RoundedButton b : chipsVaiTro) {
            boolean active = b.getText().equals(vaiTroDangLoc);
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

    private NhanVien layNhanVienDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int indexTrongDS = (trangHienTai - 1) * SO_DONG_MOI_TRANG + row;
        return (indexTrongDS < dsHienThi.size()) ? dsHienThi.get(indexTrongDS) : null;
    }

    // ----------------- 5. DIALOG THÊM / SỬA NHÂN VIÊN -----------------
    private void moDialogThemSua(NhanVien nvHienTai) {
        boolean laSua = (nvHienTai != null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), laSua ? "Chỉnh sửa nhân viên" : "Tiếp nhận nhân viên mới", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField txtHoTen = taoTextField();
        JTextField txtTenDangNhap = taoTextField();
        JPasswordField txtMatKhau = new JPasswordField();
        txtMatKhau.setFont(Theme.font(Font.PLAIN, 13));
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xD5DCE1)), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JComboBox<NhanVienDAO.QuyenOption> cboQuyen = new JComboBox<>();
        try {
            for (NhanVienDAO.QuyenOption q : bus.layDanhSachQuyen()) cboQuyen.addItem(q);
        } catch (SQLException ignored) {}

        JTextField txtSdt = taoTextField();
        JTextField txtNgaySinh = taoTextField();
        txtNgaySinh.setToolTipText("dd/MM/yyyy (ví dụ: 15/03/1995)");
        JTextField txtEmail = taoTextField();
        JComboBox<String> cboCaLam = new JComboBox<>(new String[] {"Toàn thời gian", "Ca sáng", "Ca chiều", "Ca tối"});
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {"Đang làm việc", "Tạm nghỉ", "Đã nghỉ việc"});
        JTextField txtChuyenMon = taoTextField();

        if (laSua) {
            txtHoTen.setText(nvHienTai.getHoTen());
            txtTenDangNhap.setText(nvHienTai.getTenDangNhap());
            txtSdt.setText(nvHienTai.getSoDienThoai());
            if (nvHienTai.getNgaySinh() != null) txtNgaySinh.setText(nvHienTai.getNgaySinh().format(DATE_FMT));
            txtEmail.setText(nvHienTai.getEmail());
            cboCaLam.setSelectedItem(nvHienTai.getCaLamViec());
            cboTrangThai.setSelectedItem(nvHienTai.getTrangThai());
            txtChuyenMon.setText(nvHienTai.getChuyenMon());

            for (int i = 0; i < cboQuyen.getItemCount(); i++) {
                if (cboQuyen.getItemAt(i).maQuyen == nvHienTai.getMaQuyen()) { cboQuyen.setSelectedIndex(i); break; }
            }
        } else {
            txtMatKhau.setText("123456");
        }

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 8));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setBackground(Color.WHITE);

        form.add(new JLabel("Họ và tên (*):")); form.add(txtHoTen);
        form.add(new JLabel("Tên đăng nhập (*):")); form.add(txtTenDangNhap);
        form.add(new JLabel(laSua ? "Mật khẩu mới:" : "Mật khẩu ban đầu:")); form.add(txtMatKhau);
        form.add(new JLabel("Chức danh (*):")); form.add(cboQuyen);
        form.add(new JLabel("Số điện thoại:")); form.add(txtSdt);
        form.add(new JLabel("Ngày sinh (dd/MM/yyyy):")); form.add(txtNgaySinh);
        form.add(new JLabel("Email:")); form.add(txtEmail);
        form.add(new JLabel("Ca làm việc:")); form.add(cboCaLam);
        form.add(new JLabel("Chuyên môn (nếu là BS):")); form.add(txtChuyenMon);

        RoundedButton btnLuu = new RoundedButton("Lưu thông tin", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 8);
        RoundedButton btnHuy = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(18, 8);
        btnHuy.addActionListener(e -> dlg.dispose());

        btnLuu.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            String username = txtTenDangNhap.getText().trim();
            String rawPass = new String(txtMatKhau.getPassword()).trim();
            NhanVienDAO.QuyenOption qOpt = (NhanVienDAO.QuyenOption) cboQuyen.getSelectedItem();

            if (hoTen.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ họ tên và tên đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate ngaySinh = null;
            if (!txtNgaySinh.getText().trim().isEmpty()) {
                try { ngaySinh = LocalDate.parse(txtNgaySinh.getText().trim(), DATE_FMT); }
                catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Ngày sinh phải theo định dạng dd/MM/yyyy!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            }

            NhanVien nv = new NhanVien(laSua ? nvHienTai.getMaNV() : null, hoTen, username, null,
                    qOpt.maQuyen, qOpt.tenQuyen, txtSdt.getText().trim(), ngaySinh, txtEmail.getText().trim(),
                    txtChuyenMon.getText().trim(), (String) cboTrangThai.getSelectedItem(),
                    (String) cboCaLam.getSelectedItem(), laSua ? nvHienTai.getNgayVaoLam() : LocalDate.now());

            try {
                if (laSua) {
                    bus.capNhat(nv, rawPass.isEmpty() ? null : rawPass);
                    JOptionPane.showMessageDialog(dlg, "Cập nhật thành công!");
                } else {
                    String maMoi = bus.them(nv, rawPass);
                    JOptionPane.showMessageDialog(dlg, "Đã thêm thành công nhân viên: " + maMoi);
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
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(pnlNut, BorderLayout.SOUTH);
        dlg.setSize(480, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ----------------- 6. XEM CHI TIẾT NHÂN VIÊN -----------------
    private void moDialogChiTiet(NhanVien nv) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Hồ sơ: " + nv.getHoTen() + " (" + nv.getMaNV() + ")", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);

        p.add(new JLabel("Mã nhân viên:")); p.add(new JLabel("<b>" + nv.getMaNV() + "</b>"));
        p.add(new JLabel("Tài khoản:")); p.add(new JLabel("@" + nv.getTenDangNhap()));
        p.add(new JLabel("Chức vụ:")); p.add(new JLabel(nv.getTenQuyen() + (nv.getChuyenMon().isEmpty() ? "" : " (" + nv.getChuyenMon() + ")")));
        p.add(new JLabel("Ca làm việc:")); p.add(new JLabel(nv.getCaLamViec() + " · Trạng thái: " + nv.getTrangThai()));
        p.add(new JLabel("Liên hệ:")); p.add(new JLabel(nv.getSoDienThoai() + " · " + nv.getEmail()));
        p.add(new JLabel("Ngày sinh:")); p.add(new JLabel(nv.getNgaySinh() != null ? nv.getNgaySinh().format(DATE_FMT) : "Chưa cập nhật"));

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dlg.dispose());
        JPanel pnlDong = new JPanel(); pnlDong.setBackground(Color.WHITE); pnlDong.add(btnDong);
        dlg.add(pnlDong, BorderLayout.SOUTH);
        dlg.setSize(440, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ----------------- 7. XÓA AN TOÀN -----------------
    private void xoaNhanVien(NhanVien nv) {
        String maDangNhap = (taiKhoanHienTai != null) ? taiKhoanHienTai.getMaTaiKhoan() : "";
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhân viên \"" + nv.getHoTen() + "\" (" + nv.getMaNV() + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                bus.xoa(nv.getMaNV(), maDangNhap);
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
        fc.setSelectedFile(new java.io.File("DanhSachNhanVien.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Mã NV,Họ và tên,Tài khoản,Vai trò,Số điện thoại,Email,Ca làm,Trạng thái\r\n");
            for (NhanVien nv : dsHienThi) {
                w.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\r\n",
                        nv.getMaNV(), nv.getHoTen(), nv.getTenDangNhap(), nv.getTenQuyen(),
                        nv.getSoDienThoai(), nv.getEmail(), nv.getCaLamViec(), nv.getTrangThai()));
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
