package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/** Nút bo góc tự vẽ: có màu nền, màu hover, viền tuỳ chọn. */
public class RoundedButton extends JButton {
    private Color base, hover, border;
    private final int arc;
    private int padX = 16, padY = 8;
    private boolean hovering;

    public RoundedButton(String text, Color bg, Color hoverBg, Color fg, Color border, int arc) {
        super(text);
        this.arc = arc;
        style(bg, hoverBg, fg, border);
        setFont(Theme.font(Font.BOLD, 13));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
    }

    public void style(Color bg, Color hoverBg, Color fg, Color border) {
        this.base = bg;
        this.hover = hoverBg;
        this.border = border;
        setForeground(fg);
        repaint();
    }

    public RoundedButton pad(int x, int y) {
        padX = x;
        padY = y;
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + padX * 2, fm.getHeight() + padY * 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        Color bg = base;
        if (!isEnabled()) bg = mix(base, Color.WHITE, 0.45f);
        else if (getModel().isPressed()) bg = hover.darker();
        else if (hovering) bg = hover;
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        }
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        Color fg = isEnabled() ? getForeground() : mix(getForeground(), Color.WHITE, 0.45f);
        g2.setColor(fg);
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    private static Color mix(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed() * (1 - t) + b.getRed() * t),
                Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
                Math.round(a.getBlue() * (1 - t) + b.getBlue() * t));
    }
}
