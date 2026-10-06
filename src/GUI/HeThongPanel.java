package GUI;

import Entity.TaiKhoan;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HeThongPanel extends JPanel {
    private final TaiKhoan taiKhoan;
    private final MainFrame mainFrame;

    public HeThongPanel(TaiKhoan taiKhoan, MainFrame mainFrame) {
        super(new BorderLayout(0, 20)); // Add vertical gap
        this.taiKhoan = taiKhoan;
        this.mainFrame = mainFrame;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        // Top Section: Title and Subtitle
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        
        JLabel title = new JLabel("Hệ Thống");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);
        
        JLabel subtitle = new JLabel("Quản lý phiên đăng nhập, mật khẩu và thông tin cá nhân");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);
        
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(subtitle);
        
        add(top, BorderLayout.NORTH);

        // Center Section: 3 Cards using GridLayout
        JPanel center = new JPanel(new GridLayout(1, 3, 20, 0));
        center.setOpaque(false);

        center.add(createCard("Đăng xuất", "(F1)", "exit", e -> mainFrame.dangXuat()));
        center.add(createCard("Đổi mật khẩu", "(F2)", "lock", e -> showDoiMatKhau()));
        center.add(createCard("Thông tin cá nhân", "(F3)", "person", e -> showThongTin()));

        // We wrap it in a panel to make it stick to the top
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        // Add a preferred height to center so it doesn't stretch too much vertically
        center.setPreferredSize(new Dimension(0, 260));
        centerWrapper.add(center, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        // Bottom Section: Status bar in a RoundedPanel
        RoundedPanel bottom = new RoundedPanel(12, Theme.CARD, null);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel sessionTitle = new JLabel("PHIÊN LÀM VIỆC");
        sessionTitle.setFont(Theme.font(Font.BOLD, 11));
        sessionTitle.setForeground(Theme.MUTED);
        
        JLabel sessionInfo = new JLabel("Đang đăng nhập: " + taiKhoan.getHoTen() + " - " + taiKhoan.getVaiTro());
        sessionInfo.setFont(Theme.font(Font.PLAIN, 14));
        sessionInfo.setForeground(Theme.TEXT);

        bottom.add(sessionTitle);
        bottom.add(Box.createVerticalStrut(4));
        bottom.add(sessionInfo);

        add(bottom, BorderLayout.SOUTH);

        // Key bindings
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"), "f1");
        getActionMap().put("f1", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { mainFrame.dangXuat(); }
        });
        
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F2"), "f2");
        getActionMap().put("f2", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { showDoiMatKhau(); }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F3"), "f3");
        getActionMap().put("f3", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { showThongTin(); }
        });
    }

    private JPanel createCard(String text, String sub, String icon, ActionListener action) {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, null); // no border line in image
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ic = new JLabel(Icons.of(icon, 28, new Color(0x768B99)));
        ic.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add a round background behind icon
        JPanel iconBg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE8EEF2));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconBg.setOpaque(false);
        iconBg.setPreferredSize(new Dimension(72, 72));
        iconBg.setMaximumSize(new Dimension(72, 72));
        iconBg.add(ic);
        iconBg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel(text);
        t.setFont(Theme.font(Font.BOLD, 16));
        t.setForeground(Theme.TEXT);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel s = new JLabel(sub);
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        s.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(iconBg);
        card.add(Box.createVerticalStrut(16));
        card.add(t);
        card.add(Box.createVerticalStrut(8));
        card.add(s);
        card.add(Box.createVerticalGlue());

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
            @Override public void mouseEntered(MouseEvent e) { 
                card.setBackground(new Color(0xFAFCFD));
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(Theme.CARD);
                card.repaint();
            }
        });

        return card;
    }

    private void showDoiMatKhau() {
        JOptionPane.showMessageDialog(this, "Chức năng Đổi mật khẩu đang được phát triển.");
    }

    private void showThongTin() {
        JOptionPane.showMessageDialog(this, "Chức năng Thông tin cá nhân đang được phát triển.");
    }
}
