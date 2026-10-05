package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Khung tạm cho các chức năng chưa làm: có tiêu đề + mô tả như các màn hình thật. */
public class ModulePlaceholderPanel extends JPanel {

    public ModulePlaceholderPanel(String title, String subtitle, String iconName) {
        super(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 28, 24, 28));

        JLabel t = new JLabel(title);
        t.setFont(Theme.font(Font.BOLD, 24));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.font(Font.PLAIN, 13));
        s.setForeground(Theme.MUTED);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(t);
        head.add(Box.createVerticalStrut(4));
        head.add(s);
        add(head, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new GridBagLayout());
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        JLabel ic = new JLabel(Icons.of(iconName, 44, new Color(0xB8C6D0)));
        ic.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel msg = new JLabel("Chức năng đang được phát triển");
        msg.setFont(Theme.font(Font.PLAIN, 14));
        msg.setForeground(Theme.MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(ic);
        inner.add(Box.createVerticalStrut(12));
        inner.add(msg);
        card.add(inner);
        add(card, BorderLayout.CENTER);
    }
}
