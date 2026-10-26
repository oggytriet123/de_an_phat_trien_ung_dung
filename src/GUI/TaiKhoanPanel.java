package GUI;

import java.awt.*;
import java.time.LocalDate;
import javax.swing.JPasswordField;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import Bus.TaiKhoanBUS;
import Entity.TaiKhoan;

/** Thông tin tài khoản đang đăng nhập: họ tên cố định; SĐT, ngày sinh, email sửa được. */
public class TaiKhoanPanel extends JPanel {
    private final TaiKhoan tk;
    private final TaiKhoanBUS bus = new TaiKhoanBUS();
    private final PlaceholderField txtSdt = new PlaceholderField("0901234567");
    private final PlaceholderField txtNgaySinh = new PlaceholderField("dd/MM/yyyy");
    private final PlaceholderField txtEmail = new PlaceholderField("ten@gmail.com");
    private final JLabel lblThongBao = new JLabel(" ");
    private final JPasswordField txtMkCu = new JPasswordField();
    private final JPasswordField txtMkMoi = new JPasswordField();
    private final JPasswordField txtMkLai = new JPasswordField();
    private final JLabel lblMk = new JLabel(" ");

    public TaiKhoanPanel(TaiKhoan tk) {
        super(new BorderLayout(0, 20));
        this.tk = tk;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        JLabel title = new JLabel("Tài Khoản");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Xem và cập nhật thông tin cá nhân của tài khoản đang đăng nhập");
        sub.setFont(Theme.font(Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(title);
        head.add(Box.createVerticalStrut(4));
        head.add(sub);
        add(head, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.anchor = GridBagConstraints.WEST;
        int row = 0;

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 14, 0);
        card.add(avatarHeader(), c);
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 0, 8, 0);

        row = section(card, c, row, "THÔNG TIN CỐ ĐỊNH (không thể chỉnh sửa)");
        row = field(card, c, row, "Mã nhân viên", locked(tk.getMaTaiKhoan()));
        row = field(card, c, row, "Họ và tên", locked(tk.getHoTen()));
        row = field(card, c, row, "Tên đăng nhập", locked(tk.getTenDangNhap()));
        row = field(card, c, row, "Vai trò", locked(tk.getVaiTro()));
        row = field(card, c, row, "Ngày vào làm", locked(tk.getNgayVaoLam() == null ? "" : tk.getNgayVaoLam().format(TaiKhoanBUS.NGAY)));
        row = field(card, c, row, "Ca làm việc", locked(tk.getCaLamViec()));
        row = field(card, c, row, "Trạng thái", locked(tk.getTrangThai()));

        c.insets = new Insets(24, 0, 8, 0);
        row = section(card, c, row, "THÔNG TIN LIÊN HỆ (có thể chỉnh sửa)");
        c.insets = new Insets(8, 0, 8, 0);
        style(txtSdt);
        style(txtNgaySinh);
        style(txtEmail);
        row = field(card, c, row, "Số điện thoại", txtSdt);
        row = field(card, c, row, "Ngày sinh", txtNgaySinh);
        row = field(card, c, row, "Email", txtEmail);

        lblThongBao.setFont(Theme.font(Font.PLAIN, 13));
        c.gridx = 1; c.gridy = row++; c.insets = new Insets(4, 0, 0, 0);
        card.add(lblThongBao, c);

        RoundedButton btnLuu = new RoundedButton("Lưu thay đổi", Theme.DARK, Theme.ACCENT, Color.WHITE, null, 12);
        RoundedButton btnHoan = new RoundedButton("Hoàn tác", new Color(0xE4E8EB), new Color(0xD5DBE0), Theme.TEXT, null, 12);
        btnLuu.addActionListener(e -> luu());
        btnHoan.addActionListener(e -> { napDuLieu(); thongBao(" ", Theme.MUTED); });
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        btns.add(btnLuu);
        btns.add(btnHoan);
        c.gridx = 1; c.gridy = row++; c.insets = new Insets(14, 0, 0, 0);
        card.add(btns, c);

        // ---- Đổi mật khẩu
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(22, 0, 0, 0);
        card.add(new JSeparator(), c);
        c.fill = GridBagConstraints.NONE;
        row++;
        c.insets = new Insets(18, 0, 8, 0);
        row = section(card, c, row, "ĐỔI MẬT KHẨU");
        c.insets = new Insets(8, 0, 8, 0);
        styleMk(txtMkCu); styleMk(txtMkMoi); styleMk(txtMkLai);
        row = fieldMk(card, c, row, "Mật khẩu hiện tại", txtMkCu);
        row = fieldMk(card, c, row, "Mật khẩu mới", txtMkMoi);
        row = fieldMk(card, c, row, "Nhập lại mật khẩu", txtMkLai);
        lblMk.setFont(Theme.font(Font.PLAIN, 13));
        c.gridx = 1; c.gridy = row++; c.insets = new Insets(4, 0, 0, 0);
        card.add(lblMk, c);
        RoundedButton btnDoi = new RoundedButton("Đổi mật khẩu", Theme.DARK, Theme.ACCENT, Color.WHITE, null, 12);
        btnDoi.addActionListener(e -> doiMatKhau());
        JPanel btnMk = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnMk.setOpaque(false);
        btnMk.add(btnDoi);
        c.gridx = 1; c.gridy = row++; c.insets = new Insets(14, 0, 0, 0);
        card.add(btnMk, c);

        // card có bề rộng cố định và nằm GIỮA vùng nội dung
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        GridBagConstraints wc = new GridBagConstraints();
        wc.gridx = 0; wc.gridy = 0; wc.anchor = GridBagConstraints.NORTH; wc.weightx = 1; wc.weighty = 1;
        wrap.add(card, wc);
        JScrollPane sp = new JScrollPane(wrap);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        napDuLieu();
    }

    private void napDuLieu() {
        txtSdt.setText(tk.getSoDienThoai() == null ? "" : tk.getSoDienThoai());
        txtNgaySinh.setText(tk.getNgaySinh() == null ? "" : tk.getNgaySinh().format(TaiKhoanBUS.NGAY));
        txtEmail.setText(tk.getEmail() == null ? "" : tk.getEmail());
    }

    private void luu() {
        String loi = bus.capNhatThongTin(tk, txtSdt.getText(), txtNgaySinh.getText(), txtEmail.getText());
        if (loi != null) {
            thongBao(loi, Theme.RED);
            return;
        }
        napDuLieu(); // hiển thị lại dạng đã chuẩn hoá
        thongBao("Đã lưu thông tin thành công.", Theme.GREEN);
    }

    private void doiMatKhau() {
        String loi = bus.doiMatKhau(tk, new String(txtMkCu.getPassword()), new String(txtMkMoi.getPassword()),
                new String(txtMkLai.getPassword()));
        lblMk.setText(loi == null ? "Đổi mật khẩu thành công." : loi);
        lblMk.setForeground(loi == null ? Theme.GREEN : Theme.RED);
        if (loi == null) { txtMkCu.setText(""); txtMkMoi.setText(""); txtMkLai.setText(""); }
    }

    /** Avatar tròn (chữ cái đầu) + họ tên + vai trò, đặt giữa đầu thẻ. */
    private JPanel avatarHeader() {
        String ten = tk.getHoTen() == null ? "?" : tk.getHoTen().trim();
        String chu = ten.isEmpty() ? "?" : ten.substring(ten.lastIndexOf(' ') + 1, ten.lastIndexOf(' ') + 2).toUpperCase();
        JLabel av = new JLabel(chu, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        av.setFont(Theme.font(Font.BOLD, 28));
        av.setForeground(Color.WHITE);
        av.setPreferredSize(new Dimension(72, 72));
        av.setMinimumSize(new Dimension(72, 72));
        av.setMaximumSize(new Dimension(72, 72));
        av.setOpaque(false);
        av.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = new JLabel(ten);
        name.setFont(Theme.font(Font.BOLD, 17));
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel role = new JLabel(tk.getVaiTro());
        role.setFont(Theme.font(Font.PLAIN, 13));
        role.setForeground(Theme.MUTED);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(av);
        p.add(Box.createVerticalStrut(8));
        p.add(name);
        p.add(Box.createVerticalStrut(2));
        p.add(role);
        return p;
    }

    private static void styleMk(JPasswordField f) {
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setForeground(Theme.TEXT);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0xB8C6D0), 1, true), new EmptyBorder(8, 12, 8, 12)));
    }

    private static int fieldMk(JPanel p, GridBagConstraints c, int row, String label, JPasswordField f) {
        return field(p, c, row, label, f);
    }

    private void thongBao(String s, Color color) {
        lblThongBao.setText(s);
        lblThongBao.setForeground(color);
    }

    private static JTextField locked(String text) {
        JTextField f = new JTextField(text == null ? "" : text);
        f.setEditable(false);
        f.setFocusable(false);
        f.setBackground(new Color(0xF0F3F5));
        f.setForeground(Theme.MUTED);
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        f.setCursor(Cursor.getDefaultCursor());
        f.setToolTipText("Thông tin này không thể chỉnh sửa");
        return f;
    }

    private static void style(JTextField f) {
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setForeground(Theme.TEXT);
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0xB8C6D0), 1, true), new EmptyBorder(8, 12, 8, 12)));
    }

    private static int section(JPanel p, GridBagConstraints c, int row, String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 11));
        l.setForeground(Theme.MUTED);
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        p.add(l, c);
        c.gridwidth = 1;
        return row + 1;
    }

    private static int field(JPanel p, GridBagConstraints c, int row, String label, JTextField f) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.PLAIN, 14));
        l.setForeground(Theme.TEXT);
        l.setPreferredSize(new Dimension(150, 20));
        c.gridx = 0; c.gridy = row; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        p.add(l, c);
        f.setPreferredSize(new Dimension(380, 40));
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.NONE;
        p.add(f, c);
        return row + 1;
    }
}