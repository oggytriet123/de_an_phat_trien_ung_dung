package GUI;

import java.awt.*;
import javax.swing.JTextField;

/** Ô nhập có chữ gợi ý mờ khi còn trống. */
public class PlaceholderField extends JTextField {
    private final String placeholder;

    public PlaceholderField(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(0x9AA6B0));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            Insets in = getInsets();
            g2.drawString(placeholder, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
