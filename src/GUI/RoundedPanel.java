package GUI;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

/** Panel bo góc (thẻ trắng có viền nhạt). */
public class RoundedPanel extends JPanel {
    private final int arc;
    private final Color border;

    public RoundedPanel(int arc, Color bg, Color border) {
        this.arc = arc;
        this.border = border;
        setBackground(bg);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
        super.paintChildren(g2);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (border == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
    }
}
