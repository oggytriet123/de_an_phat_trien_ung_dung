package GUI;

import java.awt.*;
import java.time.LocalDate;
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

    public TaiKhoanPanel(TaiKhoan tk) {
        super(new BorderLayout(0, 20));
        this.tk = tk;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        JLabel title = new JLabel("Tài Khoản");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);
        JLabel sub = new JLabel("Xem và cập nhật thông tin cá nhân của tài khoản đang đăng nhập");
        sub.setFont(Theme.font(Font.PLAIN, 13));
        sub.setForeground(Theme.MUTED);
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

        row = section(card, c, row, "THÔNG TIN CỐ ĐỊNH (không thể chỉnh sửa)");
        row = field(card, c, row, "Mã nhân viên", locked(tk.getMaTaiKhoan()));
        row = field(card, c, row, "Họ và tên", locked(tk.getHoTen()));
        row = field(card, c, row, "Tên đăng nhập", locked(tk.getTenDangNhap()));
        row = field(card, c, row, "Vai trò", locked(tk.getVaiTro()));

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

        // đẩy form lên trên, không giãn hết chiều cao
        c.gridx = 0; c.gridy = row; c.weighty = 1; c.gridwidth = 2;
        card.add(Box.createGlue(), c);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
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
