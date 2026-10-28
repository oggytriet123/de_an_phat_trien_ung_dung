package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import Bus.ChuongBUS;
import Entity.ChiTietDichVuChuong;
import Entity.Chuong;

/**
 * Hộp thoại Thêm Dịch vụ Chăm sóc & Thức ăn dinh dưỡng cho bé thú cưng tại lồng (tương tự DialogGoiMon).
 */
public class DialogChamSocDichVu extends JDialog {
    private final ChuongBUS bus;
    private final String maChuong;
    private final Runnable onSuccess;

    // Menu danh mục
    private DefaultTableModel modelMenu;
    private JTable tblMenu;
    private JTextField txtTimMenu;
    private JComboBox<String> cbLoaiMenu;

    // Phiếu Order hiện tại
    private DefaultTableModel modelOrder;
    private JTable tblOrder;
    private JLabel lblTongTien;
    private JLabel lblGiamGia;
    private JLabel lblCanTra;

    // Danh sách các món trong menu chăm sóc thú cưng
    private static class MenuItem {
        final String ma, ten, loai;
        final double gia;
        MenuItem(String ma, String ten, String loai, double gia) {
            this.ma = ma; this.ten = ten; this.loai = loai; this.gia = gia;
        }
    }

    private final List<MenuItem> menuItems = new ArrayList<>();
    private final List<ChiTietDichVuChuong> currentList = new ArrayList<>();

    public DialogChamSocDichVu(Window owner, ChuongBUS bus, String maChuong, Runnable onSuccess) {
        super(owner, "Chăm Sóc & Dịch Vụ - Lồng " + maChuong, ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.maChuong = maChuong;
        this.onSuccess = onSuccess;

        setSize(1040, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        initSampleMenu();
        loadExistingServices();

        Chuong c = bus.timTheoMa(maChuong);
        add(buildHeader(c), BorderLayout.NORTH);
        add(buildCenter(c), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        filterMenu();
        capNhatBangOrder();
    }

    private void initSampleMenu() {
        menuItems.clear();
        // Thức ăn dinh dưỡng
        menuItems.add(new MenuItem("TA01", "Pate cá ngừ Whiskas lon 400g", "Thức ăn & Dinh dưỡng", 35000));
        menuItems.add(new MenuItem("TA02", "Pate bò rau củ tươi hấp thơm lừng", "Thức ăn & Dinh dưỡng", 45000));
        menuItems.add(new MenuItem("TA03", "Hạt Royal Canin Mini Adult (phần ngày)", "Thức ăn & Dinh dưỡng", 40000));
        menuItems.add(new MenuItem("TA04", "Hạt Royal Canin Kitten / Puppy 500g", "Thức ăn & Dinh dưỡng", 75000));
        menuItems.add(new MenuItem("TA05", "Súp thưởng Ciao gói 4 thanh", "Thức ăn & Dinh dưỡng", 38000));
        menuItems.add(new MenuItem("TA06", "Bánh quy canxi sạch răng cho cún", "Thức ăn & Dinh dưỡng", 30000));
        menuItems.add(new MenuItem("TA07", "Thịt ức gà luộc xé sợi dinh dưỡng", "Thức ăn & Dinh dưỡng", 35000));

        // Dịch vụ Spa & Grooming
        menuItems.add(new MenuItem("SP01", "Tắm massage thảo dược & sấy bồng lông", "Spa & Grooming", 120000));
        menuItems.add(new MenuItem("SP02", "Cắt móng, mài móng & vệ sinh tai mắt", "Spa & Grooming", 50000));
        menuItems.add(new MenuItem("SP03", "Combo Spa toàn diện + cắt tỉa tạo kiểu", "Spa & Grooming", 220000));
        menuItems.add(new MenuItem("SP04", "Vắt tuyến hôi & khử mùi hôi cơ thể", "Spa & Grooming", 40000));
        menuItems.add(new MenuItem("SP05", "Dưỡng bóng lông Collagen cao cấp", "Spa & Grooming", 80000));

        // Y tế & Chăm sóc thú y
        menuItems.add(new MenuItem("YT01", "Bác sĩ thú y khám tổng quát hàng ngày", "Y tế & Chăm sóc", 90000));
        menuItems.add(new MenuItem("YT02", "Nhỏ gáy trị ve rận bọ chét Frontline", "Y tế & Chăm sóc", 120000));
        menuItems.add(new MenuItem("YT03", "Cho uống thuốc theo toa bác sĩ dặn", "Y tế & Chăm sóc", 20000));
        menuItems.add(new MenuItem("YT04", "Vệ sinh bôi thuốc trị nấm ngứa da", "Y tế & Chăm sóc", 40000));
    }

    private void loadExistingServices() {
        currentList.clear();
        currentList.addAll(bus.layDichVuCuaChuong(maChuong));
    }

    private JPanel buildHeader(Chuong c) {
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.SIDEBAR);
        head.setBorder(new EmptyBorder(14, 20, 14, 20));

        String petInfo = (c != null && c.getTenPet() != null) ? " - Bé " + c.getTenPet() + " (" + c.getLoaiPet() + ")" : "";
        JLabel title = new JLabel("CHĂM SÓC & DỊCH VỤ - LỒNG " + maChuong + petInfo);
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        String owner = (c != null && c.getTenKhach() != null) ? "Chủ nuôi: " + c.getTenKhach() + " | SĐT: " + c.getSdtKhach() : "";
        JLabel sub = new JLabel(owner);
        sub.setFont(Theme.font(Font.PLAIN, 12));
        sub.setForeground(new Color(0xE0ECF2));

        head.add(title, BorderLayout.NORTH);
        head.add(sub, BorderLayout.SOUTH);
        return head;
    }

    private JPanel buildCenter(Chuong c) {
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setBackground(Theme.BG);
        center.setBorder(new EmptyBorder(14, 16, 14, 16));

        // ================= NỬA TRÁI: MENU DỊCH VỤ / THỨC ĂN =================
        RoundedPanel pnlLeft = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        pnlLeft.setLayout(new BorderLayout(0, 10));
        pnlLeft.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel topFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topFilters.setOpaque(false);

        topFilters.add(new JLabel("Tìm:"));
        txtTimMenu = new JTextField(10);
        txtTimMenu.setFont(Theme.font(Font.PLAIN, 12));
        txtTimMenu.addActionListener(e -> filterMenu());
        topFilters.add(txtTimMenu);

        cbLoaiMenu = new JComboBox<>(new String[]{"Tất cả", "Thức ăn & Dinh dưỡng", "Spa & Grooming", "Y tế & Chăm sóc"});
        cbLoaiMenu.setFont(Theme.font(Font.PLAIN, 12));
        cbLoaiMenu.addActionListener(e -> filterMenu());
        topFilters.add(cbLoaiMenu);

        RoundedButton btnTim = new RoundedButton("Tìm", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 6).pad(8, 4);
        btnTim.addActionListener(e -> filterMenu());
        topFilters.add(btnTim);

        pnlLeft.add(topFilters, BorderLayout.NORTH);

        String[] colsMenu = {"Mã", "Tên Dịch Vụ / Món Ăn", "Loại", "Đơn Giá"};
        modelMenu = new DefaultTableModel(colsMenu, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMenu = new JTable(modelMenu);
        JScrollPane spMenu = new JScrollPane(tblMenu);
        Theme.styleTable(tblMenu, spMenu, 32);
        tblMenu.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblMenu.getColumnModel().getColumn(0).setCellRenderer(Theme.cellCenter());
        tblMenu.getColumnModel().getColumn(1).setPreferredWidth(210);
        tblMenu.getColumnModel().getColumn(2).setCellRenderer(Theme.cellCenter());
        tblMenu.getColumnModel().getColumn(3).setCellRenderer(Theme.cellRight());

        tblMenu.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    themMonVaoOrder();
                }
            }
        });

        pnlLeft.add(spMenu, BorderLayout.CENTER);

        JPanel pnlAddBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        pnlAddBtn.setOpaque(false);
        RoundedButton btnAdd = new RoundedButton("+ Thêm vào phiếu care >>", Theme.GREEN, new Color(0x278C44), Color.WHITE, null, 8).pad(14, 6);
        btnAdd.addActionListener(e -> themMonVaoOrder());
        pnlAddBtn.add(btnAdd);
        pnlLeft.add(pnlAddBtn, BorderLayout.SOUTH);

        center.add(pnlLeft);

        // ================= NỬA PHẢI: ORDER CHO BÉ PET =================
        RoundedPanel pnlRight = new RoundedPanel(12, Color.WHITE, Theme.BORDER);
        pnlRight.setLayout(new BorderLayout(0, 10));
        pnlRight.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel titleOrder = new JLabel("📋 PHIẾU DỊCH VỤ & CHĂM SÓC");
        titleOrder.setFont(Theme.font(Font.BOLD, 14));
        titleOrder.setForeground(Theme.SIDEBAR);
        pnlRight.add(titleOrder, BorderLayout.NORTH);

        String[] colsOrder = {"Mặt hàng / Dịch vụ", "SL", "Đơn giá", "Thành tiền"};
        modelOrder = new DefaultTableModel(colsOrder, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrder = new JTable(modelOrder);
        JScrollPane spOrder = new JScrollPane(tblOrder);
        Theme.styleTable(tblOrder, spOrder, 32);
        tblOrder.getColumnModel().getColumn(0).setPreferredWidth(200);
        tblOrder.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblOrder.getColumnModel().getColumn(1).setCellRenderer(Theme.cellCenter());
        tblOrder.getColumnModel().getColumn(2).setCellRenderer(Theme.cellRight());
        tblOrder.getColumnModel().getColumn(3).setCellRenderer(Theme.cellRight());

        pnlRight.add(spOrder, BorderLayout.CENTER);

        // Controls tăng giảm số lượng & xóa
        JPanel pnlOrderControl = new JPanel(new BorderLayout(0, 8));
        pnlOrderControl.setOpaque(false);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlBtns.setOpaque(false);

        RoundedButton btnPlus = new RoundedButton("+ 1", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 6).pad(10, 4);
        RoundedButton btnMinus = new RoundedButton("- 1", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 6).pad(10, 4);
        RoundedButton btnDel = new RoundedButton("Xóa", new Color(0xFADBD8), new Color(0xF5B7B1), Theme.RED, null, 6).pad(10, 4);

        btnPlus.addActionListener(e -> thayDoiSoLuong(1));
        btnMinus.addActionListener(e -> thayDoiSoLuong(-1));
        btnDel.addActionListener(e -> xoaMonOrder());

        pnlBtns.add(btnPlus);
        pnlBtns.add(btnMinus);
        pnlBtns.add(btnDel);
        pnlOrderControl.add(pnlBtns, BorderLayout.NORTH);

        // Chi phí
        JPanel pnlBill = new JPanel(new GridLayout(3, 1, 0, 3));
        pnlBill.setOpaque(false);
        lblTongTien = new JLabel("Tổng tiền dịch vụ: 0₫");
        lblTongTien.setFont(Theme.font(Font.PLAIN, 12));
        lblGiamGia = new JLabel("Ưu đãi thành viên: 0₫");
        lblGiamGia.setFont(Theme.font(Font.PLAIN, 12));
        lblCanTra = new JLabel("CẦN THANH TOÁN: 0₫");
        lblCanTra.setFont(Theme.font(Font.BOLD, 14));
        lblCanTra.setForeground(Theme.RED);

        pnlBill.add(lblTongTien);
        pnlBill.add(lblGiamGia);
        pnlBill.add(lblCanTra);
        pnlOrderControl.add(pnlBill, BorderLayout.CENTER);

        pnlRight.add(pnlOrderControl, BorderLayout.SOUTH);

        center.add(pnlRight);
        return center;
    }

    private JPanel buildFooter() {
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        foot.setBackground(Color.WHITE);
        foot.setBorder(new LineBorder(Theme.BORDER, 1));

        RoundedButton btnDong = new RoundedButton("Hủy bỏ", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 8).pad(16, 8);
        RoundedButton btnLuu = new RoundedButton("✓ Lưu Phiếu Chăm Sóc", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(18, 8);

        btnDong.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luuOrder());

        foot.add(btnDong);
        foot.add(btnLuu);
        return foot;
    }

    private void filterMenu() {
        modelMenu.setRowCount(0);
        String kw = txtTimMenu != null ? txtTimMenu.getText().trim().toLowerCase() : "";
        String cat = cbLoaiMenu != null ? (String) cbLoaiMenu.getSelectedItem() : "Tất cả";

        for (MenuItem m : menuItems) {
            boolean matchCat = "Tất cả".equals(cat) || m.loai.equalsIgnoreCase(cat);
            boolean matchKw = kw.isEmpty() || m.ma.toLowerCase().contains(kw) || m.ten.toLowerCase().contains(kw);
            if (matchCat && matchKw) {
                modelMenu.addRow(new Object[]{
                    m.ma, m.ten, m.loai, Theme.money((long) m.gia)
                });
            }
        }
    }

    private void themMonVaoOrder() {
        int r = tblMenu.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mục trong danh mục bên trái!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ma = (String) modelMenu.getValueAt(r, 0);
        String ten = (String) modelMenu.getValueAt(r, 1);
        String loai = (String) modelMenu.getValueAt(r, 2);

        MenuItem item = null;
        for (MenuItem m : menuItems) if (m.ma.equalsIgnoreCase(ma)) { item = m; break; }
        if (item == null) return;

        // Kiểm tra xem đã có trong order chưa
        boolean daCo = false;
        for (ChiTietDichVuChuong d : currentList) {
            if (d.getMaDV().equalsIgnoreCase(ma)) {
                d.setSoLuong(d.getSoLuong() + 1);
                daCo = true;
                break;
            }
        }
        if (!daCo) {
            currentList.add(new ChiTietDichVuChuong(item.ma, item.ten, item.loai, item.gia, 1, LocalDateTime.now(), ""));
        }
        capNhatBangOrder();
    }

    private void thayDoiSoLuong(int delta) {
        int r = tblOrder.getSelectedRow();
        if (r == -1) return;
        ChiTietDichVuChuong d = currentList.get(r);
        int slMoi = d.getSoLuong() + delta;
        if (slMoi <= 0) {
            currentList.remove(r);
        } else {
            d.setSoLuong(slMoi);
        }
        capNhatBangOrder();
    }

    private void xoaMonOrder() {
        int r = tblOrder.getSelectedRow();
        if (r == -1) return;
        currentList.remove(r);
        capNhatBangOrder();
    }

    private void capNhatBangOrder() {
        modelOrder.setRowCount(0);
        double sum = 0;
        for (ChiTietDichVuChuong d : currentList) {
            modelOrder.addRow(new Object[]{
                d.getTenDV(),
                d.getSoLuong(),
                Theme.money((long) d.getDonGia()),
                Theme.money((long) d.getThanhTien())
            });
            sum += d.getThanhTien();
        }

        Chuong c = bus.timTheoMa(maChuong);
        double giam = 0;
        // Ưu đãi thành viên nếu có
        lblTongTien.setText("Tổng tiền dịch vụ & thức ăn: " + Theme.money((long) sum));
        lblGiamGia.setText("Ưu đãi thành viên: " + Theme.money((long) giam));
        lblCanTra.setText("TỔNG DỊCH VỤ: " + Theme.money((long) (sum - giam)));
    }

    private void luuOrder() {
        // Cập nhật lại danh sách dịch vụ của lồng
        List<ChiTietDichVuChuong> old = bus.layDichVuCuaChuong(maChuong);
        while (!old.isEmpty()) {
            bus.xoaDichVu(maChuong, 0);
            old = bus.layDichVuCuaChuong(maChuong);
        }
        for (ChiTietDichVuChuong d : currentList) {
            bus.themDichVu(maChuong, d);
        }

        JOptionPane.showMessageDialog(this, "Đã cập nhật danh sách dịch vụ & chăm sóc cho lồng " + maChuong + " thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        if (onSuccess != null) onSuccess.run();
        dispose();
    }
}
