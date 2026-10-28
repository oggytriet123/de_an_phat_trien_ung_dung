package GUI;

import java.awt.*;
import javax.swing.JToggleButton;

/** Nút lọc danh mục dạng viên thuốc. */
final class Pill extends JToggleButton {
    Pill(String text) {
        super(text);
        setFont(Theme.font(Font.PLAIN, 12));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        FontMetrics fm = getFontMetrics(getFont());
        setPreferredSize(new Dimension(fm.stringWidth(text) + 30, 32));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        if (isSelected()) {
            g2.setColor(Theme.PRIMARY);
            g2.fillRoundRect(0, 0, w, h, h, h);
        } else {
            g2.setColor(Theme.FIELD_BG);
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        }
        g2.setFont(getFont());
        g2.setColor(isSelected() ? Color.WHITE : Theme.TEXT);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
