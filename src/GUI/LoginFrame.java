package GUI;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.DangNhapBUS;
import Entity.TaiKhoan;

public class LoginFrame extends JFrame {
    private static final Color FIELD_BORDER = new Color(0xD5DCE1);
    private static final char ECHO = '\u2022';

    private final DangNhapBUS bus = new DangNhapBUS();
    private final JTextField txtTaiKhoan = new JTextField();
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JCheckBox chkHien = new JCheckBox("Hiện mật khẩu");
    private final JLabel lblLoi = new JLabel(" ");
    private final RoundedButton btnDangNhap =
            new RoundedButton("ĐĂNG NHẬP", Theme.DARK, Theme.ACCENT, Color.WHITE, null, 14);
    private final RoundedButton btnThoat =
            new RoundedButton("THOÁT", new Color(0xE4E8EB), new Color(0xD5DBE0), Theme.TEXT, null, 14);

    public LoginFrame() {
        super("Hệ Thống Quản Lý PetCare");
        setIconImage(Icons.appIcon());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { thoat(); }
        });

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildLeft());
        root.add(buildRight());
        setContentPane(root);

        wireEvents();
        setSize(1100, 690);
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnDangNhap);
    }

    // ---------- Bên trái: thương hiệu ----------
    private JPanel buildLeft() {
        JPanel p = new JPanel();
        p.setBackground(Theme.DARK);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JComponent logo = new LogoBox();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("QUẢN LÝ PETCARE");
        title.setFont(Theme.font(Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Chăm sóc tận tâm - Yêu thương trọn vẹn");
        sub.setFont(Theme.font(Font.PLAIN, 16));
        sub.setForeground(new Color(0xB5C3CD));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(logo);
        p.add(Box.createVerticalStrut(30));
        p.add(title);
        p.add(Box.createVerticalStrut(6));
        p.add(sub);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private static class LogoBox extends JComponent {
        LogoBox() {
            Dimension d = new Dimension(260, 260);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(Theme.BG);
            g2.fillRoundRect(0, 0, w, h, 10, 10);
            g2.setColor(new Color(0xD3E0E8));
            g2.fillRect(17, 17, w - 34, h - 34);
            Color ink = new Color(0x2C4757);
            Icons.draw(g2, "paw", w / 2.0 - 52, 52, 104, ink, true);
            g2.setColor(ink);
            g2.setFont(Theme.font(Font.BOLD, 26));
            FontMetrics fm = g2.getFontMetrics();
            String s = "PETCARE";
            g2.drawString(s, (w - fm.stringWidth(s)) / 2, 185);
            g2.dispose();
        }
    }

    // ---------- Bên phải: form ----------
    private JPanel buildRight() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 34));
        title.setForeground(Color.BLACK);
        JLabel sub = new JLabel("Vui lòng đăng nhập để tiếp tục", SwingConstants.CENTER);
        sub.setFont(Theme.font(Font.PLAIN, 16));
        sub.setForeground(new Color(0x98A3AC));

        styleField(txtTaiKhoan);
        styleField(txtMatKhau);
        txtMatKhau.setEchoChar(ECHO);

        chkHien.setOpaque(false);
        chkHien.setFont(Theme.font(Font.PLAIN, 13));
        chkHien.setFocusPainted(false);
        chkHien.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblLoi.setFont(Theme.font(Font.PLAIN, 13));
        lblLoi.setForeground(Theme.RED);

        btnDangNhap.setFont(Theme.font(Font.BOLD, 15));
        btnDangNhap.setPreferredSize(new Dimension(440, 42));
        btnThoat.setFont(Theme.font(Font.BOLD, 15));
        btnThoat.setPreferredSize(new Dimension(440, 42));

        add(form, c, 0, title, 6);
        add(form, c, 1, sub, 34);
        add(form, c, 2, boldLabel("Mã Tài Khoản / Số Điện Thoại:"), 8);
        add(form, c, 3, txtTaiKhoan, 20);
        add(form, c, 4, boldLabel("Mật Khẩu:"), 8);
        add(form, c, 5, txtMatKhau, 10);
        add(form, c, 6, chkHien, 6);
        add(form, c, 7, lblLoi, 14);
        add(form, c, 8, btnDangNhap, 12);
        add(form, c, 9, btnThoat, 0);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(form);

        JLabel foot = new JLabel("© 2026 PetCare Management System", SwingConstants.CENTER);
        foot.setFont(Theme.font(Font.PLAIN, 13));
        foot.setForeground(new Color(0xB3BCC3));
        foot.setBorder(new EmptyBorder(0, 0, 28, 0));

        right.add(center, BorderLayout.CENTER);
        right.add(foot, BorderLayout.SOUTH);
        return right;
    }

    private static void add(JPanel form, GridBagConstraints c, int row, JComponent comp, int bottom) {
        c.gridy = row;
        c.insets = new Insets(0, 0, bottom, 0);
        form.add(comp, c);
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.BOLD, 14));
        l.setForeground(Color.BLACK);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(Theme.font(Font.PLAIN, 15));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(440, 42));
        f.setBorder(fieldBorder(false));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(fieldBorder(true)); }
            @Override public void focusLost(FocusEvent e) { f.setBorder(fieldBorder(false)); }
        });
    }

    private Border fieldBorder(boolean focus) {
        return new CompoundBorder(new LineBorder(focus ? Theme.ACCENT : FIELD_BORDER, 1), new EmptyBorder(0, 12, 0, 12));
    }

    // ---------- Chức năng ----------
    private void wireEvents() {
        btnDangNhap.addActionListener(e -> dangNhap());
        btnThoat.addActionListener(e -> thoat());
        chkHien.addActionListener(e -> txtMatKhau.setEchoChar(chkHien.isSelected() ? (char) 0 : ECHO));

        DocumentListener clear = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { lblLoi.setText(" "); }
            @Override public void removeUpdate(DocumentEvent e) { lblLoi.setText(" "); }
            @Override public void changedUpdate(DocumentEvent e) { }
        };
        txtTaiKhoan.getDocument().addDocumentListener(clear);
        txtMatKhau.getDocument().addDocumentListener(clear);

        // Enter ở ô tài khoản -> chuyển xuống ô mật khẩu
        txtTaiKhoan.addActionListener(e -> txtMatKhau.requestFocusInWindow());
    }

    private void dangNhap() {
        String id = txtTaiKhoan.getText().trim();
        String pw = new String(txtMatKhau.getPassword());
        if (id.isEmpty()) {
            baoLoi("Vui lòng nhập mã tài khoản hoặc số điện thoại.");
            txtTaiKhoan.requestFocusInWindow();
            return;
        }
        if (pw.isEmpty()) {
            baoLoi("Vui lòng nhập mật khẩu.");
            txtMatKhau.requestFocusInWindow();
            return;
        }
        setBusy(true);
        new SwingWorker<TaiKhoan, Void>() {
            @Override protected TaiKhoan doInBackground() { return bus.dangNhap(id, pw); }

            @Override protected void done() {
                try {
                    TaiKhoan tk = get();
                    setBusy(false);
                    if (tk == null) {
                        baoLoi("Tài khoản hoặc mật khẩu không đúng.");
                        txtMatKhau.setText("");
                        txtMatKhau.requestFocusInWindow();
                    } else {
                        dispose();
                        new MainFrame(tk).setVisible(true);
                    }
                } catch (Exception ex) {
                    setBusy(false);
                    baoLoi("Không thể đăng nhập: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        btnDangNhap.setEnabled(!busy);
        btnThoat.setEnabled(!busy);
        txtTaiKhoan.setEnabled(!busy);
        txtMatKhau.setEnabled(!busy);
        btnDangNhap.setText(busy ? "ĐANG ĐĂNG NHẬP..." : "ĐĂNG NHẬP");
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void baoLoi(String msg) {
        lblLoi.setText(msg);
    }

    private void thoat() {
        int r = JOptionPane.showOptionDialog(this, "Bạn có chắc chắn muốn thoát ứng dụng?", "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[] {"Thoát", "Ở lại"}, "Ở lại");
        if (r == 0) System.exit(0);
    }
}
